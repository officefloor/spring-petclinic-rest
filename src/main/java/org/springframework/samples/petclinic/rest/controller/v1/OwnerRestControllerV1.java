/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.advice.RequiredFieldsMissingException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Syntactic email check: a non-empty local part, a single {@code @}, and a domain that
     * contains at least one dot, with no whitespace anywhere. Deliberately lenient — it accepts
     * ordinary addresses while rejecting clearly malformed ones such as {@code not-an-email}.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Dedicated audit logger. On a successful create an audit line carrying the owner id, the
     * customerCode and the registrationDate is emitted through this logger.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<List<OwnerDto>> listOwners(String lastName) {
        Collection<Owner> owners;
        if (lastName != null) {
            owners = this.clinicService.findOwnerByLastName(lastName);
        } else {
            owners = this.clinicService.findAllOwners();
        }
        if (owners.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
    }

    /**
     * Rejects an owner payload that is missing or blank in any required field, listing the name of
     * each offending field so the caller receives a 400 with a populated {@code errors} array.
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missing = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(ownerFieldsDto.getAddress())) {
            missing.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missing.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes an address on create: leading/trailing whitespace is trimmed, every run of internal
     * whitespace is collapsed to a single space, the value is upper-cased and common abbreviations are
     * expanded token-by-token ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The
     * normalized value is what gets stored, returned and used for every address comparison; a value
     * that is blank (or {@code null}) normalizes to the empty string so the required-field check
     * rejects it.
     *
     * @param address the raw address as supplied by the caller, may be {@code null}
     * @return the normalized address ({@code ""} when blank or {@code null})
     */
    private static String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            switch (token) {
                case "ST" -> token = "STREET";
                case "RD" -> token = "ROAD";
                case "AVE" -> token = "AVENUE";
                default -> { }
            }
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Normalizes a telephone on create into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading {@code +} and its country code are kept as given; otherwise the country code
     * {@code +61} is assumed and a single leading {@code 0} is dropped from the national digits.
     * The result must carry 8 to 15 digits after the {@code +}. Returns the E.164 string to be
     * stored and returned.
     *
     * @param telephone the raw telephone as supplied by the caller
     * @return the normalized E.164 telephone (a {@code +} followed by 8 to 15 digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private static String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = "+" + cleaned.substring(1);
        } else {
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            e164 = "+61" + national;
        }
        String digits = e164.substring(1);
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(
                "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        return e164;
    }

    /**
     * Normalizes an optional email. An absent or blank email is left unset (returns {@code null});
     * a present value must be a syntactically valid address and is stored and returned lower-cased.
     *
     * @param email the raw email as supplied by the caller, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidEmailException if a value is present but is not a syntactically valid address
     */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects a create whose E.164 telephone is already used by any existing owner, so that
     * telephones stay unique across owners. Telephones are stored in E.164 form on create, so a
     * direct equality comparison of the E.164 values against the stored values is sufficient.
     *
     * @param normalizedTelephone the E.164 telephone of the owner being created
     * @throws DuplicateTelephoneException if another owner already uses this telephone
     */
    private void rejectDuplicateTelephone(String normalizedTelephone) {
        boolean inUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizedTelephone.equals(existing.getTelephone()));
        if (inUse) {
            throw new DuplicateTelephoneException(
                "Telephone is already used by another owner");
        }
    }

    /**
     * Rejects a create that would place the owner in a household already occupied by another owner,
     * i.e. one whose last name and address match the supplied values. The two fields are compared
     * case-insensitively after whitespace is collapsed (leading/trailing whitespace trimmed and each
     * run of internal whitespace reduced to a single space), so {@code '110 W.  Liberty St. '} and
     * {@code '110 w. liberty st.'} are treated as the same address. The caller may bypass this check
     * by setting {@code sharesHousehold} to {@code true}.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @throws DuplicateHouseholdException if another owner already shares this last name and address
     */
    private void rejectDuplicateHousehold(String lastName, String address) {
        String normalizedLastName = normalizeForHousehold(lastName);
        String normalizedAddress = normalizeForHousehold(address);
        boolean inHousehold = this.clinicService.findAllOwners().stream()
            .anyMatch(existing ->
                normalizedLastName.equals(normalizeForHousehold(existing.getLastName()))
                    && normalizedAddress.equals(normalizeForHousehold(existing.getAddress())));
        if (inHousehold) {
            throw new DuplicateHouseholdException(
                "An owner with the same last name and address already exists");
        }
    }

    /**
     * Normalizes a value for household comparison by trimming, collapsing every run of whitespace to
     * a single space and lower-casing, so the comparison is case-insensitive with collapsed whitespace.
     *
     * @param value the raw value, may be {@code null}
     * @return the normalized value ({@code ""} when {@code value} is {@code null})
     */
    private static String normalizeForHousehold(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Derives the stable household identifier for an owner from the normalized last name and address.
     * The value is a deterministic function of those two fields (compared case-insensitively with
     * collapsed whitespace, exactly as {@link #rejectDuplicateHousehold}), so every owner in the same
     * household — including owners who knowingly join it via {@code sharesHousehold} — is assigned the
     * same identifier without any existing record having to be updated. Formatted {@code 'HH-<HEX12>'}
     * where {@code HEX12} is the upper-cased first twelve hex characters of the SHA-256 of the two
     * normalized fields.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @return the stable household identifier
     */
    private static String householdId(String lastName, String address) {
        String key = normalizeForHousehold(lastName) + "\n" + normalizeForHousehold(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "HH-" + sb.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * The maximum number of owners a single city may contain. A create whose city already holds this
     * many owners is rejected, so a city never grows beyond this capacity.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Rejects a create whose city already contains {@link #CITY_CAPACITY} or more owners, so no city
     * ever exceeds its capacity. Cities are compared case-insensitively with surrounding whitespace
     * trimmed, exactly as {@link #nextCustomerCode} counts a city's owners. The count reflects the
     * state before the new owner is persisted, so it excludes the owner being created.
     *
     * @param city the city of the owner being created
     * @throws CityAtCapacityException if the city already contains {@link #CITY_CAPACITY} owners
     */
    private void rejectCityAtCapacity(String city) {
        String normalizedCity = normalizeName(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedCity.equals(normalizeName(existing.getCity())))
            .count();
        if (ownersInCity >= CITY_CAPACITY) {
            throw new CityAtCapacityException(
                "The city already contains the maximum number of owners");
        }
    }

    /**
     * The maximum number of owners that may be created on a single day. A create made once this many
     * owners already carry today's {@code registrationDate} is rejected, so no more than this many
     * owners are registered per day.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Rejects a create once {@link #DAILY_OWNER_LIMIT} or more owners have already been created for
     * the given business day, counted by {@code registrationDate} equal to the effective (weekend
     * rolled forward) registration date of the owner being created. The count reflects the state
     * before the new owner is persisted, so it excludes the owner being created.
     *
     * @param registrationDate the effective business-day registration date of the owner being created
     * @throws DailyOwnerLimitExceededException if the day already holds {@link #DAILY_OWNER_LIMIT} owners
     */
    private void rejectDailyOwnerLimit(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (ownersToday >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                "The maximum number of owners for today has already been reached");
        }
    }

    /**
     * The number of owners that may already exist for a business day before a create is flagged with
     * a bulk-signup warning. Once more than this many owners already carry the effective registration
     * date, the created owner's {@code bulkSignupWarning} is set to {@code true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether a create should be flagged with a bulk-signup warning, i.e. whether more than
     * {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created for the given business day,
     * counted by {@code registrationDate} equal to the effective (weekend rolled forward) registration
     * date of the owner being created. The count reflects the state before the new owner is persisted,
     * so it excludes the owner being created.
     *
     * @param registrationDate the effective business-day registration date of the owner being created
     * @return {@code true} when more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry the date
     */
    private boolean bulkSignupWarning(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return ownersToday > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Rolls a registration date forward to a business day: a Saturday or Sunday is advanced to the
     * following Monday, any weekday is returned unchanged.
     *
     * @param date the effective registration date, supplied or defaulted to the server date
     * @return the same date when it is a weekday, otherwise the next Monday
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /**
     * Builds the customer code assigned to an owner on create, formatted
     * {@code '<CITY3>-<LAST3>-<NNNN>'} where {@code CITY3} is the upper-cased first three letters
     * of the owner's city, {@code LAST3} is the upper-cased first three letters of the owner's last
     * name and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one more than the
     * current number of owners already in that city, e.g. {@code 'MEL-SMI-0007'}.
     *
     * @param city the city of the owner being created
     * @param lastName the last name of the owner being created
     * @return the formatted customer code
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        String normalizedCity = normalizeName(city);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedCity.equals(normalizeName(existing.getCity())))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Counts the existing owners that share the owner-to-be's first and last name, compared
     * case-insensitively (surrounding whitespace trimmed). The count reflects the state before the
     * new owner is persisted, so it excludes the owner being created.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners with the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeName(firstName);
        String normalizedLastName = normalizeName(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing ->
                normalizedFirstName.equals(normalizeName(existing.getFirstName()))
                    && normalizedLastName.equals(normalizeName(existing.getLastName())))
            .count();
    }

    /**
     * Normalizes a name for case-insensitive comparison by trimming surrounding whitespace and
     * lower-casing.
     *
     * @param value the raw name, may be {@code null}
     * @return the normalized name ({@code ""} when {@code value} is {@code null})
     */
    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        ownerFieldsDto.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        validateRequiredFields(ownerFieldsDto);
        LocalDate registrationDate = ownerFieldsDto.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
        registrationDate = toBusinessDay(registrationDate);
        ownerFieldsDto.setRegistrationDate(registrationDate);
        rejectDailyOwnerLimit(registrationDate);
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            rejectDuplicateHousehold(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        }
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateTelephone(normalizedTelephone);
        ownerFieldsDto.setTelephone(normalizedTelephone);
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setCustomerCode(nextCustomerCode(ownerFieldsDto.getCity(), ownerFieldsDto.getLastName()));
        owner.setHouseholdId(householdId(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress()));
        owner.setNamesakeCount(countNamesakes(ownerFieldsDto.getFirstName(), ownerFieldsDto.getLastName()));
        owner.setBulkSignupWarning(bulkSignupWarning(registrationDate));
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        currentOwner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        this.clinicService.saveOwner(currentOwner);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> deleteOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deleteOwner(owner);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PetDto> addPetToOwner(Integer ownerId, PetFieldsDto petFieldsDto) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        Pet pet = petMapper.toPet(petFieldsDto);
        owner.setId(ownerId);
        pet.setOwner(owner);
        pet.getType().setName(null);
        this.clinicService.savePet(pet);
        PetDto petDto = petMapper.toPetDto(pet);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/pets/{id}")
            .buildAndExpand(pet.getId()).toUri());
        return new ResponseEntity<>(petDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<Void> updateOwnersPet(Integer ownerId, Integer petId, PetFieldsDto petFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner != null) {
            Pet currentPet = this.clinicService.findPetById(petId);
            if (currentPet != null) {
                currentPet.setBirthDate(petFieldsDto.getBirthDate());
                currentPet.setName(petFieldsDto.getName());
                currentPet.setType(petMapper.toPetType(petFieldsDto.getType()));
                this.clinicService.savePet(currentPet);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<VisitDto> addVisitToOwner(Integer ownerId, Integer petId, VisitFieldsDto visitFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Visit visit = visitMapper.toVisit(visitFieldsDto);
        Pet pet = new Pet();
        pet.setId(petId);
        visit.setPet(pet);
        this.clinicService.saveVisit(visit);
        VisitDto visitDto = visitMapper.toVisitDto(visit);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/visits/{id}")
            .buildAndExpand(visit.getId()).toUri());
        return new ResponseEntity<>(visitDto, headers, HttpStatus.CREATED);
    }


    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PetDto> getOwnersPet(Integer ownerId, Integer petId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner != null) {
            Pet pet = owner.getPet(petId);
            if (pet != null) {
                return new ResponseEntity<>(petMapper.toPetDto(pet), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
