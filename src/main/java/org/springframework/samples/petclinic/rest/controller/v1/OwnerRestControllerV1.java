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
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidFieldValueException;
import org.springframework.samples.petclinic.rest.advice.MissingRequiredFieldsException;
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
     * Basic syntactic email check: a non-empty local part, an '@', and a domain containing at least
     * one dot, with no whitespace anywhere. Deliberately permissive but requires the essential shape.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Dedicated audit logger; create side-effects are recorded here so they can be observed independently. */
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
        List<OwnerDto> ownerDtos = owners.stream()
            .map(this::toOwnerDtoWithBulkWarning)
            .toList();
        return new ResponseEntity<>(ownerDtos, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(toOwnerDtoWithBulkWarning(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        requireNonBlank(missingFields, "firstName", ownerFieldsDto.getFirstName());
        requireNonBlank(missingFields, "lastName", ownerFieldsDto.getLastName());
        String normalizedAddress = normalizeAddress(ownerFieldsDto.getAddress());
        requireNonBlank(missingFields, "address", normalizedAddress);
        requireNonBlank(missingFields, "city", ownerFieldsDto.getCity());
        requireNonBlank(missingFields, "telephone", ownerFieldsDto.getTelephone());
        if (!missingFields.isEmpty()) {
            throw new MissingRequiredFieldsException(missingFields);
        }
        LocalDate suppliedOrDefaultDate = ownerFieldsDto.getRegistrationDate() != null
            ? ownerFieldsDto.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(suppliedOrDefaultDate);
        requireDailyLimitNotReached(registrationDate);
        requireCityBelowCapacity(ownerFieldsDto.getCity());
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        requireUniqueTelephone(normalizedTelephone);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (!sharesHousehold) {
            requireUniqueHousehold(ownerFieldsDto.getLastName(), normalizedAddress);
        }
        ownerFieldsDto.setTelephone(normalizedTelephone);
        if (ownerFieldsDto.getEmail() != null) {
            ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizedAddress);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        if (sharesHousehold) {
            owner.setHouseholdId(joinHousehold(owner.getLastName(), owner.getAddress()));
        }
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = toOwnerDtoWithBulkWarning(owner);
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
        currentOwner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
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

    /**
     * Builds the owner's customer code as {@code '<CITY3>-<LAST3>-<NNNN>'}, where {@code CITY3} is the
     * upper-cased first three letters of {@code city}, {@code LAST3} the upper-cased first three letters
     * of {@code lastName}, and {@code NNNN} a per-city 4-digit zero-padded sequence equal to one more
     * than the number of owners already in that city (e.g. {@code SYD-SMI-0007}).
     *
     * @param city the owner's city (already validated non-blank)
     * @param lastName the owner's last name (already validated non-blank)
     * @return the formatted customer code for the owner being created
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Counts the existing owners (those already persisted before this create) whose first and last
     * names both match the incoming owner's, compared case-insensitively. This snapshot is stored on
     * the new owner as its {@code namesakeCount} and echoed back on reads.
     *
     * @param firstName the incoming owner's first name (already validated non-blank)
     * @param lastName the incoming owner's last name (already validated non-blank)
     * @return the number of existing namesakes sharing both names case-insensitively
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Records {@code fieldName} as missing when {@code value} is {@code null} or blank (empty or
     * whitespace-only), so the caller can reject the request listing every offending field.
     */
    private static void requireNonBlank(List<String> missingFields, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missingFields.add(fieldName);
        }
    }

    /**
     * Normalizes a telephone number to E.164 form. Spaces, dashes and brackets are stripped. When the
     * value carries a leading {@code '+'} its country code is kept; otherwise a {@code '+61'} country
     * code is assumed and a single leading {@code '0'} is dropped from the national digits. The result
     * must be a {@code '+'} followed by 8 to 15 digits. For example {@code "0412 345 678"} becomes
     * {@code "+61412345678"}.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized E.164 telephone number (a {@code '+'} followed by 8 to 15 digits)
     * @throws InvalidFieldValueException if the value cannot form a valid E.164 number
     */
    private static String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidFieldValueException("telephone",
                "Telephone must be a valid E.164 number ('+' followed by 8 to 15 digits)");
        }
        return e164;
    }

    /**
     * Converts a raw telephone value to E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. Used both to normalize incoming values and to compare against existing
     * owners' stored numbers when detecting duplicates.
     */
    private static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.trim().replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Validates and normalizes an owner's email address. A syntactically valid address is required;
     * the value is returned lower-cased so it is stored and echoed back in canonical form.
     *
     * @param email the raw email value from the request (already known to be non-null)
     * @return the lower-cased email to be stored and returned
     * @throws InvalidFieldValueException if the value is not a syntactically valid email address
     */
    private static String normalizeEmail(String email) {
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidFieldValueException("email", "Email must be a syntactically valid address");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /** The maximum number of owners permitted in a single city; the {@code (50 + 1)}th is rejected. */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Rejects the request when the owner's city already contains {@link #MAX_OWNERS_PER_CITY} or more
     * owners, comparing city names case-insensitively (consistent with the per-city customer-code
     * sequence). Callers reach this only once {@code city} has been validated non-blank.
     *
     * @param city the incoming owner's city (already validated non-blank)
     * @throws CityAtCapacityException if the city is already at or above its owner capacity
     */
    /** The maximum number of owners that may be registered in a single day; the {@code (100 + 1)}th is rejected. */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Rejects the request when {@link #MAX_OWNERS_PER_DAY} or more owners have already been registered
     * on {@code businessDay}, counting existing owners whose {@code registrationDate} equals that date.
     * New owners take {@code businessDay} as their {@code registrationDate} (the supplied or defaulted
     * date rolled forward to a business day), so this caps the number that may be created per day.
     *
     * @param businessDay the adjusted business-day registration date of the owner being created
     * @throws DailyOwnerLimitException if the daily owner-creation limit has already been reached
     */
    /**
     * The number of owners for a single day beyond which the {@code bulkSignupWarning} flag is
     * raised; the warning is true once <em>more than</em> this many owners share a registration date.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Maps {@code owner} to its DTO and sets the {@code bulkSignupWarning} flag: {@code true} when
     * more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created for this
     * owner's registration date, otherwise {@code false}.
     *
     * @param owner the owner to map
     * @return the owner DTO with its bulk-signup warning flag populated
     */
    private OwnerDto toOwnerDtoWithBulkWarning(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(exceedsBulkSignupThreshold(owner.getRegistrationDate()));
        return ownerDto;
    }

    /**
     * Returns whether more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been
     * created on {@code registrationDate}, counting existing owners whose {@code registrationDate}
     * equals that date. Returns {@code false} when {@code registrationDate} is {@code null}.
     */
    private boolean exceedsBulkSignupThreshold(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        long registeredThatDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return registeredThatDay > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    private void requireDailyLimitNotReached(LocalDate businessDay) {
        long registeredThatDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
            .count();
        if (registeredThatDay >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(MAX_OWNERS_PER_DAY);
        }
    }

    /**
     * Rolls {@code date} forward to a business day: when it falls on a Saturday or Sunday it advances
     * to the following Monday; a weekday is returned unchanged. Applied to the effective registration
     * date (whether supplied in the request or defaulted to the server date) so a weekend registration
     * is recorded on the next business day, and every value derived from the registration date uses the
     * adjusted date.
     *
     * @param date the effective registration date (supplied or defaulted), never {@code null}
     * @return the same date, or the next Monday when {@code date} is a weekend
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    private void requireCityBelowCapacity(String city) {
        long cityOwners = this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (cityOwners >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * Rejects the request when {@code normalizedTelephone} is already used by any existing owner,
     * comparing on the E.164 form so differently-formatted stored numbers still collide.
     *
     * @param normalizedTelephone the incoming owner's normalized E.164 telephone
     * @throws DuplicateTelephoneException if another owner already uses this telephone
     */
    private void requireUniqueTelephone(String normalizedTelephone) {
        boolean taken = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .map(OwnerRestControllerV1::toE164)
            .filter(existing -> existing != null)
            .anyMatch(normalizedTelephone::equals);
        if (taken) {
            throw new DuplicateTelephoneException(normalizedTelephone);
        }
    }

    /**
     * Rejects the request when another owner already shares this household, i.e. has both the same
     * last name and the same address once each value is normalized (trimmed, internal whitespace
     * runs collapsed to a single space, and compared case-insensitively). Callers skip this check
     * when the request opts in via {@code sharesHousehold}.
     *
     * @param lastName the incoming owner's last name (already validated non-blank)
     * @param address the incoming owner's address (already validated non-blank)
     * @throws DuplicateHouseholdException if another owner already has this last name and address
     */
    private void requireUniqueHousehold(String lastName, String address) {
        String normalizedLastName = normalizeForHousehold(lastName);
        String normalizedAddress = householdAddressKey(address);
        boolean taken = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizeForHousehold(existing.getLastName()).equals(normalizedLastName)
                && householdAddressKey(existing.getAddress()).equals(normalizedAddress));
        if (taken) {
            throw new DuplicateHouseholdException(lastName, address);
        }
    }

    /**
     * Normalizes a value for household-duplicate comparison: trims leading and trailing whitespace,
     * collapses every internal run of whitespace to a single space, and lower-cases the result so
     * comparisons are case-insensitive.
     */
    private static String normalizeForHousehold(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner's address to its canonical stored form: leading and trailing whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded on a whole-word basis
     * ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}). A {@code null} or whitespace-only
     * value normalizes to the empty string, so the required-field check rejects an address that is
     * blank once normalized. For example {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}.
     *
     * @param address the raw address value from the request (may be {@code null})
     * @return the normalized address, or the empty string when blank after normalization
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
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            String expanded = switch (token) {
                case "ST" -> "STREET";
                case "RD" -> "ROAD";
                case "AVE" -> "AVENUE";
                default -> token;
            };
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(expanded);
        }
        return sb.toString();
    }

    /**
     * Derives the comparison key for an address used by household-duplicate detection and the shared
     * household identifier: the address is first put in its canonical normalized form and then
     * lower-cased and whitespace-collapsed, so every comparison and the derived id agree on the
     * normalized address regardless of the raw casing, spacing or abbreviations supplied.
     */
    private static String householdAddressKey(String address) {
        return normalizeForHousehold(normalizeAddress(address));
    }

    /**
     * Joins the household identified by this last name and address, returning the stable shared
     * {@code householdId} to store on the joining owner. The identifier is derived deterministically
     * from the normalized last name and address, so every owner in the same household resolves to the
     * same value. Any existing household members that predate this feature (or were created without
     * opting in) are back-filled with the shared identifier so the whole household agrees.
     *
     * @param lastName the joining owner's last name (already validated non-blank)
     * @param address the joining owner's address (already validated non-blank)
     * @return the shared household identifier for this last name and address
     */
    private String joinHousehold(String lastName, String address) {
        String normalizedLastName = normalizeForHousehold(lastName);
        String normalizedAddress = householdAddressKey(address);
        String householdId = householdIdFor(normalizedLastName, normalizedAddress);
        this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getLastName()).equals(normalizedLastName)
                && householdAddressKey(existing.getAddress()).equals(normalizedAddress))
            .filter(existing -> !householdId.equals(existing.getHouseholdId()))
            .forEach(existing -> {
                existing.setHouseholdId(householdId);
                this.clinicService.saveOwner(existing);
            });
        return householdId;
    }

    /**
     * Derives a stable household identifier from the already-normalized last name and address as the
     * upper-cased first 12 hex characters of their SHA-256 digest. Deterministic so the same household
     * always maps to the same identifier.
     */
    private static String householdIdFor(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((normalizedLastName + "|" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, 12);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
