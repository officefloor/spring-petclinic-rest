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
import java.util.Map;
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
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerEmailException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityCapacityExceededException;
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
     * Syntactic email check: a non-empty local part, a single {@code @}, and a domain that contains
     * at least one dot. Whitespace and additional {@code @} characters are disallowed.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Dedicated audit logger. Successful owner creation emits a single line here carrying the new
     * owner's id, customer code, registration date and membership level, so audit side-effects can
     * be observed independently of the HTTP response.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Common street-type abbreviations expanded during address normalization. Keys and values are
     * upper-cased, matching the state of the address after whitespace and case normalization.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

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

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        validateRequiredOwnerFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        owner.setAddress(normalizeAddress(owner.getAddress()));
        LocalDate effectiveRegistrationDate =
            owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(rollToBusinessDay(effectiveRegistrationDate));
        rejectDuplicateTelephone(normalizedTelephone);
        rejectDuplicateEmail(owner.getEmail());
        rejectDailyLimitReached(owner.getRegistrationDate());
        rejectCityAtCapacity(owner.getCity());
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            assignHousehold(owner);
        } else {
            rejectDuplicateHousehold(owner.getLastName(), owner.getAddress());
        }
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(isBulkSignupDay(owner.getRegistrationDate()));
        owner.setHouseholdSize(countHousehold(owner.getHouseholdId()));
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerMapper.membershipLevel(owner));
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
        currentOwner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
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

    /**
     * Builds the customer code for a newly created owner, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}
     * where {@code CITY3} is the upper-cased first three letters of the owner's city, {@code LAST3}
     * is the upper-cased first three letters of the owner's last name, and {@code NNNN} is a per-city
     * 4-digit zero-padded sequence equal to one more than the number of owners already in that city.
     * For example an owner named "Smithers" in "Springfield" created when 10 owners already live in
     * Springfield gets {@code 'SPR-SMI-0011'}.
     *
     * @param city the owner's city
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    /**
     * Counts how many existing owners share the given first and last name, compared
     * case-insensitively, at the moment before the new owner is persisted. The result is
     * stored on the owner as its {@code namesakeCount}, so it reflects the population as it
     * stood when the owner was created rather than being recomputed on later reads.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of pre-existing owners with the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> first.equalsIgnoreCase(existing.getFirstName())
                && last.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    private String nextCustomerCode(String city, String lastName) {
        String cityLetters = city == null ? "" : city;
        String city3 = cityLetters.substring(0, Math.min(3, cityLetters.length())).toUpperCase(Locale.ROOT);
        String letters = lastName == null ? "" : lastName;
        String last3 = letters.substring(0, Math.min(3, letters.length())).toUpperCase(Locale.ROOT);
        String normalizedCity = normalizeForComparison(city);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedCity.equals(normalizeForComparison(existing.getCity())))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Rejects an owner payload that is missing or blank in any of the required fields
     * (firstName, lastName, address, city, telephone). The bean-validation constraints on
     * {@link OwnerFieldsDto} already reject {@code null} values and most malformed input, but
     * a present-yet-blank {@code address} or {@code city} would otherwise slip through, so this
     * guard enforces the rule uniformly for every required field.
     *
     * @param ownerFieldsDto the submitted owner payload
     * @throws InvalidOwnerFieldsException if one or more required fields are missing or blank
     */
    private void validateRequiredOwnerFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> invalidFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            invalidFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            invalidFields.add("lastName");
        }
        if (isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
            invalidFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            invalidFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            invalidFields.add("telephone");
        }
        if (!invalidFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(invalidFields);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes a submitted telephone number into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading {@code '+'} and its country code are kept as-is; otherwise the country code {@code '+61'}
     * is assumed and a single leading {@code '0'} is dropped from the national digits. The result must be
     * a {@code '+'} followed by 8 to 15 digits. For example {@code "0412 345 678"} normalizes to
     * {@code "+61412345678"}. The E.164 string is what gets stored and returned.
     *
     * @param telephone the raw telephone value from the submitted owner payload
     * @return the normalized E.164 telephone number
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.strip();
        boolean hasCountryCode = raw.startsWith("+");
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (hasCountryCode) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return "+" + digits;
    }

    /**
     * Best-effort conversion of an existing telephone value to E.164 form for duplicate comparison,
     * returning {@code null} when the value cannot form a valid E.164 number.
     *
     * @param telephone an existing owner's stored telephone value
     * @return the E.164 form, or {@code null} if it cannot be normalized
     */
    private String toE164OrNull(String telephone) {
        try {
            return normalizeTelephone(telephone);
        } catch (InvalidOwnerFieldsException ex) {
            return null;
        }
    }

    /**
     * Normalizes an optional owner email. A {@code null} value is left untouched (the field is optional).
     * When a value is present it must be a syntactically valid address (see {@link #EMAIL_PATTERN}); the
     * accepted value is trimmed and lower-cased before it is stored and returned.
     *
     * @param email the raw email value from the submitted owner payload, may be {@code null}
     * @return {@code null} if no email was supplied, otherwise the lower-cased email
     * @throws InvalidOwnerFieldsException if a value is present but not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects creating an owner whose E.164 telephone is already used by any other owner.
     * Existing owners' telephones are converted to E.164 form before comparison so that
     * differently-formatted representations of the same number are treated as duplicates.
     *
     * @param normalizedTelephone the E.164 telephone of the owner being created
     * @throws DuplicateOwnerTelephoneException if another owner already uses the same E.164 telephone
     */
    private void rejectDuplicateTelephone(String normalizedTelephone) {
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(telephone -> telephone != null)
            .map(this::toE164OrNull)
            .filter(telephone -> telephone != null)
            .anyMatch(normalizedTelephone::equals);
        if (duplicate) {
            throw new DuplicateOwnerTelephoneException(normalizedTelephone);
        }
    }

    /**
     * Rejects creating an owner whose lower-cased email is already used by any other owner. The email
     * of the owner being created has already been normalized (trimmed and lower-cased) by
     * {@link #normalizeEmail}; existing owners' emails are lower-cased before comparison so that
     * differently-cased representations of the same address are treated as duplicates. Owners without
     * an email (the field is optional) are ignored, and a {@code null} email for the new owner skips
     * the check entirely.
     *
     * @param normalizedEmail the lower-cased email of the owner being created, may be {@code null}
     * @throws DuplicateOwnerEmailException if another owner already uses the same lower-cased email
     */
    private void rejectDuplicateEmail(String normalizedEmail) {
        if (normalizedEmail == null) {
            return;
        }
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .map(Owner::getEmail)
            .filter(email -> email != null)
            .map(email -> email.toLowerCase(Locale.ROOT))
            .anyMatch(normalizedEmail::equals);
        if (duplicate) {
            throw new DuplicateOwnerEmailException(normalizedEmail);
        }
    }

    /**
     * The maximum number of owners a single city may contain. Once a city already holds this many
     * owners, creating another owner in that city is rejected.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Rejects creating an owner in a city that already contains {@link #MAX_OWNERS_PER_CITY} or more
     * owners. Existing owners' cities are compared case-insensitively with surrounding and repeated
     * internal whitespace collapsed (see {@link #normalizeForComparison}), matching the per-city
     * counting used elsewhere.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityCapacityExceededException if the city already contains the maximum number of owners
     */
    /**
     * The maximum number of owners that may be registered on a single day. Once this many owners
     * already share a {@code registrationDate}, creating another owner for that day is rejected.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Rejects creating an owner on a day that has already reached {@link #MAX_OWNERS_PER_DAY} or more
     * owner registrations. Existing owners are counted by their {@code registrationDate} matching the
     * registration date of the owner being created.
     *
     * @param registrationDate the registration date of the owner being created
     * @throws DailyOwnerLimitExceededException if the day has already reached the maximum number of owners
     */
    /**
     * Rolls an effective registration date forward onto a business day. A registration date must fall
     * on a weekday: when the supplied or defaulted date is a Saturday or Sunday it is rolled forward to
     * the next Monday and that adjusted date becomes the owner's {@code registrationDate}. A weekday date
     * is returned unchanged. Every value derived from the registration date (the daily create-limit
     * count, the membership number's year segment, and so on) uses this adjusted date.
     *
     * @param date the effective registration date (supplied in the request or defaulted to the server date)
     * @return the same date if it is a weekday, otherwise the following Monday
     */
    private LocalDate rollToBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    private void rejectDailyLimitReached(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (ownersToday >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
    }

    /**
     * The number of owners that must already share a {@code registrationDate} before a newly created
     * owner for that day is flagged with a bulk-signup warning. Once more than this many owners have
     * been created for the day, the new owner's {@code bulkSignupWarning} is {@code true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether the owner being created should carry a bulk-signup warning. The warning is
     * raised when more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created for
     * the same registration date at the time this owner is created. Existing owners are counted by their
     * {@code registrationDate} matching the registration date of the owner being created, mirroring the
     * daily create-limit accumulation.
     *
     * @param registrationDate the registration date of the owner being created
     * @return {@code true} if the day already holds more than the threshold number of owners
     */
    private boolean isBulkSignupDay(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return ownersToday > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    private void rejectCityAtCapacity(String city) {
        String normalizedCity = normalizeForComparison(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedCity.equals(normalizeForComparison(existing.getCity())))
            .count();
        if (ownersInCity >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityCapacityExceededException(city);
        }
    }

    /**
     * Rejects creating an owner whose last name and address both match an existing owner. Both fields
     * are compared case-insensitively with surrounding and repeated internal whitespace collapsed, so
     * that e.g. {@code "  110  W. Liberty  St. "} and {@code "110 w. liberty st."} are treated as the
     * same address. This guard is bypassed for a request that sets {@code sharesHousehold} true.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @throws DuplicateOwnerHouseholdException if another owner shares the same last name and address
     */
    private void rejectDuplicateHousehold(String lastName, String address) {
        String normalizedLastName = normalizeForComparison(lastName);
        String normalizedAddress = normalizeAddress(address);
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizedLastName.equals(normalizeForComparison(existing.getLastName()))
                && normalizedAddress.equals(normalizeAddress(existing.getAddress())));
        if (duplicate) {
            throw new DuplicateOwnerHouseholdException(lastName, address);
        }
    }

    /**
     * Assigns the owner being created a household identifier when the request opts into sharing a
     * household. The identifier is a stable value derived from the owner's normalized last name and
     * address (see {@link #householdId}), so every owner in the same household is given the exact same
     * value regardless of creation order. Any already-existing owner in the same household that has no
     * identifier yet is back-filled with the same value so the whole household stays consistent.
     *
     * @param owner the owner being created, whose household id is set in place
     */
    private void assignHousehold(Owner owner) {
        String householdId = householdId(owner.getLastName(), owner.getAddress());
        String normalizedLastName = normalizeForComparison(owner.getLastName());
        String normalizedAddress = normalizeAddress(owner.getAddress());
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.getHouseholdId() == null
                && normalizedLastName.equals(normalizeForComparison(existing.getLastName()))
                && normalizedAddress.equals(normalizeAddress(existing.getAddress()))) {
                existing.setHouseholdId(householdId);
                this.clinicService.saveOwner(existing);
            }
        }
        owner.setHouseholdId(householdId);
    }

    /**
     * Counts how many owners belong to the household the owner being created has just been assigned to,
     * inclusive of that owner. Existing owners are matched by their {@code householdId} equalling the
     * given identifier; the owner being created is added on because it is counted before being persisted.
     * An owner that did not opt into a shared household (its {@code householdId} is {@code null}) is a
     * household of one. The value is snapshotted on the owner.
     *
     * @param householdId the household identifier assigned to the owner being created, may be {@code null}
     * @return the number of household members, including the owner being created
     */
    private int countHousehold(String householdId) {
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> householdId.equals(owner.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * Derives the stable household identifier for a given last name and address. The value is
     * {@code 'HH-' + <12 upper-case hex chars>} of the SHA-256 digest of the normalized last name and
     * address (see {@link #normalizeForComparison}), joined by a delimiter. Two owners that resolve to
     * the same household therefore always produce the same identifier.
     *
     * @param lastName the owner's last name
     * @param address the owner's address
     * @return the stable household identifier
     */
    private String householdId(String lastName, String address) {
        String key = normalizeForComparison(lastName) + "\n" + normalizeAddress(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return "HH-" + hex;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Normalizes a value for case-insensitive, whitespace-insensitive comparison: leading and trailing
     * whitespace is stripped, every run of internal whitespace is collapsed to a single space, and the
     * result is lower-cased. A {@code null} value normalizes to the empty string.
     *
     * @param value the value to normalize, may be {@code null}
     * @return the normalized value
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner address for storage and comparison: leading and trailing whitespace is
     * stripped, every run of internal whitespace is collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). For example {@code "  12  main  st "} normalizes to
     * {@code "12 MAIN STREET"}. A {@code null} value normalizes to the empty string. This is the form
     * that is stored, returned, and used for every address comparison (household duplicate detection
     * and the shared household id).
     *
     * @param address the raw address value from the submitted owner payload, may be {@code null}
     * @return the normalized address
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
