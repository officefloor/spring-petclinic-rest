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
import org.springframework.samples.petclinic.rest.advice.MissingOwnerFieldsException;
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
     * Dedicated audit logger. On a successful owner create an audit line carrying the owner id,
     * the {@code customerCode} and the {@code registrationDate} is emitted to this named logger.
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

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
    }

    /**
     * Rejects an owner payload that is missing or blank in any of the required fields
     * (firstName, lastName, address, city, telephone). Bean Validation on the request body
     * already rejects {@code null} values and empty strings, but treats a whitespace-only
     * value as present; this guard closes that gap so a blank in any required field is
     * reported. The thrown exception is translated to a 400 whose {@code errors} array lists
     * the name of each offending field.
     */
    private void rejectMissingOrBlankFields(OwnerFieldsDto ownerFieldsDto, String normalizedAddress) {
        List<String> missing = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(normalizedAddress)) {
            missing.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missing.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Matches a well-formed E.164 telephone: a leading '+' followed by 8 to 15 digits.
     */
    private static final Pattern E164_PATTERN = Pattern.compile("\\+\\d{8,15}");

    /**
     * Normalizes an owner's telephone on create into E.164 form. Spaces, dashes and brackets are
     * stripped. When a leading '+' (with its country code) is present it is kept as-is; otherwise
     * the country code '+61' is assumed and a single leading '0' is dropped from the national
     * digits. The result must be a '+' followed by 8 to 15 digits, so e.g. '0412 345 678' is
     * stored as '+61412345678'. A value that cannot form a valid E.164 number is rejected via
     * {@link InvalidTelephoneException}, which the exception handler translates to a 400.
     */
    private static String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()\\[\\]]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = cleaned;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = "+61" + national;
        }
        if (!E164_PATTERN.matcher(e164).matches()) {
            throw new InvalidTelephoneException(telephone);
        }
        return e164;
    }

    /**
     * A pragmatic syntactic email check: a non-empty local part, an {@code @}, and a domain that
     * carries at least one dot, with no whitespace anywhere. Deliberately permissive — it accepts
     * ordinary addresses while rejecting obvious non-addresses such as one lacking an {@code @}.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /**
     * Normalizes an optional owner email. A {@code null} email is left as-is (the field is
     * optional). When present it must be a syntactically valid address; a valid value is
     * lower-cased so it is stored and returned in canonical form. An invalid value is rejected via
     * {@link InvalidEmailException}, which the exception handler translates to a 400.
     */
    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        return email.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects a create whose normalized telephone is already used by another owner. Each
     * existing owner's stored telephone is reduced to its E.164 form the same way {@code
     * normalizeTelephone} does, so the comparison is on the canonical E.164 value regardless of
     * the format each was originally entered in. An existing value that cannot form a valid E.164
     * number is skipped rather than colliding. A match is reported via
     * {@link DuplicateTelephoneException}, which the exception handler translates to a 409.
     */
    private void rejectDuplicateTelephone(String normalizedTelephone) {
        for (Owner existing : this.clinicService.findAllOwners()) {
            String existingE164;
            try {
                existingE164 = normalizeTelephone(existing.getTelephone());
            } catch (InvalidTelephoneException ex) {
                continue;
            }
            if (normalizedTelephone.equals(existingE164)) {
                throw new DuplicateTelephoneException(normalizedTelephone);
            }
        }
    }

    /**
     * Collapses a value for household comparison: leading and trailing whitespace is trimmed, any
     * internal run of whitespace is reduced to a single space, and the result is lower-cased. This
     * is how both {@code lastName} and {@code address} are compared so that differences of case or
     * spacing do not defeat the duplicate-household check.
     */
    private static String collapse(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner's address into a canonical form applied on every create: leading and
     * trailing whitespace is trimmed, any internal run of whitespace is reduced to a single space,
     * the value is upper-cased, and common street-type abbreviations are expanded on a per-word
     * basis ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The result is stored
     * and returned, and is the form every address comparison (duplicate-household detection and the
     * shared {@code householdId}) uses. A {@code null} or whitespace-only input normalizes to the
     * empty string, which the required-field guard then rejects. The transformation is idempotent,
     * so normalizing an already-normalized address is a no-op.
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
                default -> {
                }
            }
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Rejects a create that would place a second owner in the same household as an existing one -
     * i.e. another owner already has the same {@code lastName} and the same {@code address},
     * compared case-insensitively with collapsed whitespace. The caller can opt in to sharing a
     * household by setting {@code sharesHousehold} true, in which case this check is skipped. A
     * match is reported via {@link DuplicateHouseholdException}, which the exception handler
     * translates to a 409.
     */
    private void rejectDuplicateHousehold(OwnerFieldsDto ownerFieldsDto, String normalizedAddress) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String lastName = collapse(ownerFieldsDto.getLastName());
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (lastName.equals(collapse(existing.getLastName()))
                && normalizedAddress.equals(normalizeAddress(existing.getAddress()))) {
                throw new DuplicateHouseholdException(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
            }
        }
    }

    /**
     * Builds the {@code customerCode} for a newly created owner, formatted
     * {@code '<CITY3>-<LAST3>-<NNNN>'} where {@code CITY3} is the upper-cased first three letters of
     * {@code city}, {@code LAST3} the upper-cased first three letters of {@code lastName}, and
     * {@code NNNN} a per-city 4-digit zero-padded sequence equal to one more than the number of
     * owners already in that city (e.g. {@code 'SYD-SMI-0007'}).
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = 1;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                sequence++;
            }
        }
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Builds the {@code membershipNumber} for a newly created owner, formatted
     * {@code '<customerCode>-M<YY>'} where {@code YY} is the last two digits of the
     * {@code registrationDate} year (e.g. {@code 'SMI-0007-M26'}).
     */
    private static String membershipNumberFor(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /**
     * Derives the stable {@code householdId} for an owner from the collapsed {@code lastName} and
     * {@code address} - the same pair used to detect a shared household. Because it is a pure
     * function of that pair (upper-case hex of SHA-256 over the two collapsed values), every owner
     * in the same household - whether created first or joining later via {@code sharesHousehold} -
     * receives the identical, non-blank identifier without needing to read any other owner's value.
     */
    private static String householdIdFor(String lastName, String address) {
        String key = collapse(lastName) + "\n" + normalizeAddress(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Counts the existing owners who already share the given {@code firstName} and {@code lastName},
     * compared case-insensitively. This is evaluated before the new owner is saved, so it reflects
     * the state prior to this create and is stored on the owner as its {@code namesakeCount}.
     */
    private int countNamesakes(String firstName, String lastName) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the members of the household the new owner will belong to, as it stands after this
     * create. Every existing owner carrying the same {@code householdId} is counted, and the new
     * owner itself is added, so a value of 3 or more means the owner joins a household of at least
     * three members. This is evaluated with the new owner's {@code householdId} already set and is
     * stored on the owner as its {@code householdMemberCount}.
     */
    private int countHouseholdMembers(String householdId) {
        int count = 1;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * The membership level ceiling assigned on create; level 4 is reserved for tenure and so is
     * never reached here.
     */
    private static final int MAX_MEMBERSHIP_LEVEL = 3;

    /**
     * Computes the numeric {@code membershipLevel} for a newly created owner. It starts at 1,
     * gains 1 when an email is present, gains 1 when the owner's {@code namesakeCount} is 0, and
     * is capped at {@value #MAX_MEMBERSHIP_LEVEL} (level 4 is reserved for tenure). Evaluated after
     * the owner's email and namesakeCount have been set.
     */
    private static int membershipLevelFor(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX_MEMBERSHIP_LEVEL);
    }

    /**
     * The maximum number of owners any single city may contain. A create whose city already
     * holds this many owners is rejected.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Rejects a create whose city already contains {@value #CITY_CAPACITY} or more owners,
     * compared case-insensitively - the same way the city is matched when building the
     * {@code customerCode} sequence. The count reflects the state prior to this create. A city
     * at capacity is reported via {@link CityAtCapacityException}, which the exception handler
     * translates to a 409.
     */
    private void rejectCityAtCapacity(String city) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * The maximum number of owners that may be created on any single day (by
     * {@code registrationDate}). A create attempted once this many owners already carry the
     * current day's registration date is rejected.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Rejects a create once {@value #DAILY_OWNER_LIMIT} or more owners already carry the given
     * business-day-adjusted {@code registrationDate} (the date the new owner will itself receive).
     * The count reflects the state prior to this create. When the limit is reached the create is
     * reported via {@link DailyOwnerLimitExceededException}, which the exception handler translates
     * to a 429 Too Many Requests.
     */
    private void rejectDailyOwnerLimit(LocalDate registrationDate) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException();
        }
    }

    /**
     * The number of owners that must already carry a given {@code registrationDate} before a
     * create on that date is flagged with a bulk-signup warning. Once <em>more than</em> this
     * many owners already exist for the day, the new owner's {@code bulkSignupWarning} is true.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Computes the {@code bulkSignupWarning} for a create on the given business-day-adjusted
     * {@code registrationDate}: true when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners
     * have already been created for that date, otherwise false. The count reflects the state prior
     * to this create, matching how the daily-limit rule accumulates.
     */
    private boolean bulkSignupWarningFor(LocalDate registrationDate) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Rolls an effective registration date forward onto a business day. A Saturday or Sunday is
     * advanced to the following Monday; a weekday is returned unchanged. This is applied to the
     * effective registration date - whether supplied in the request or defaulted to the server
     * date - so the stored {@code registrationDate}, and everything derived from it, always falls
     * on a business day.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String normalizedAddress = normalizeAddress(ownerFieldsDto.getAddress());
        rejectMissingOrBlankFields(ownerFieldsDto, normalizedAddress);
        rejectDuplicateHousehold(ownerFieldsDto, normalizedAddress);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizedAddress);
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateTelephone(normalizedTelephone);
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        LocalDate effectiveDate = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        rejectDailyOwnerLimit(registrationDate);
        owner.setBulkSignupWarning(bulkSignupWarningFor(registrationDate));
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setMembershipNumber(membershipNumberFor(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setHouseholdId(householdIdFor(owner.getLastName(), owner.getAddress()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdMemberCount(countHouseholdMembers(owner.getHouseholdId()));
        owner.setMembershipLevel(membershipLevelFor(owner));
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel());
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
