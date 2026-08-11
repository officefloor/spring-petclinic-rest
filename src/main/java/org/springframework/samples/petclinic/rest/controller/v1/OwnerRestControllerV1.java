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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerEmailException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
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

    /** Dedicated audit logger; emits one line per successful owner create. */
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

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        // Normalize the address up front so every downstream use (the required-field check,
        // household-duplicate detection, the shared household id and the stored/returned value)
        // works from the same canonical form.
        String normalizedAddress = normalizeAddress(ownerFieldsDto.getAddress());
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        // Reject an address that is blank once normalized (e.g. whitespace-only input).
        if (isBlank(normalizedAddress)) {
            missingFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(missingFields);
        }
        // Determine the effective registration date up front so every downstream use (the daily
        // create-limit, the stored value and anything derived from it such as the membership number's
        // year segment) works from the same business-day-adjusted date. The effective date is the
        // supplied value when present, otherwise the server's current date; a weekend date is rolled
        // forward to the following Monday.
        LocalDate effectiveRegistrationDate = ownerFieldsDto.getRegistrationDate() != null
            ? ownerFieldsDto.getRegistrationDate()
            : LocalDate.now();
        effectiveRegistrationDate = toBusinessDay(effectiveRegistrationDate);
        // Reject the request once the day's owner-creation limit is reached: when 100 or more owners
        // already carry this adjusted business day's registration date, no further owners may be
        // created for that day.
        int ownersRegisteredToday = countOwnersRegisteredOn(effectiveRegistrationDate);
        if (ownersRegisteredToday >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(effectiveRegistrationDate);
        }
        // Flag a bulk signup once more than 80 owners have already been created for this
        // business day, sharing the same daily accumulation path as the create-limit rule above.
        boolean bulkSignupWarning = ownersRegisteredToday > BULK_SIGNUP_WARNING_THRESHOLD;
        // Reject the request when the owner's city is already full: a city that already holds the
        // maximum of 50 owners (compared case-insensitively, as elsewhere) accepts no more.
        if (countOwnersInCity(ownerFieldsDto.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(ownerFieldsDto.getCity());
        }
        // Normalize the telephone into E.164 form; a number that cannot form a valid
        // E.164 value is rejected with a 400.
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        // Email is optional; when present it must be a syntactically valid address and is
        // stored lower-cased. An invalid address is rejected with a 400.
        String normalizedEmail = normalizeEmail(ownerFieldsDto.getEmail());
        // Reject the request if the E.164 telephone is already used by any other owner.
        boolean telephoneInUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(existing -> existing != null)
            .anyMatch(normalizedTelephone::equals);
        if (telephoneInUse) {
            throw new DuplicateOwnerTelephoneException(normalizedTelephone);
        }
        // Reject the request if the (lower-cased) email is already used by any other owner. Stored
        // emails are already normalized to lower case, but compare case-insensitively defensively.
        if (normalizedEmail != null) {
            boolean emailInUse = this.clinicService.findAllOwners().stream()
                .map(Owner::getEmail)
                .filter(existing -> existing != null)
                .anyMatch(normalizedEmail::equalsIgnoreCase);
            if (emailInUse) {
                throw new DuplicateOwnerEmailException(normalizedEmail);
            }
        }
        // Household handling for a matching last name + address (compared case-insensitively with
        // collapsed whitespace). By default such a request is rejected as a duplicate household;
        // when the caller opts in via 'sharesHousehold' the owner is instead admitted into the shared
        // household and every member (the new owner and any existing ones) is stamped with the same
        // stable 'householdId'.
        String normalizedLastName = collapse(ownerFieldsDto.getLastName());
        String householdId = null;
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            householdId = householdId(normalizedLastName, normalizedAddress);
            // Backfill the shared identifier onto any existing member of this household so the whole
            // household carries the same stable value.
            for (Owner existing : this.clinicService.findAllOwners()) {
                if (collapse(existing.getLastName()).equals(normalizedLastName)
                    && normalizeAddress(existing.getAddress()).equals(normalizedAddress)
                    && !householdId.equals(existing.getHouseholdId())) {
                    existing.setHouseholdId(householdId);
                    this.clinicService.saveOwner(existing);
                }
            }
        } else {
            boolean householdInUse = this.clinicService.findAllOwners().stream()
                .anyMatch(existing -> collapse(existing.getLastName()).equals(normalizedLastName)
                    && normalizeAddress(existing.getAddress()).equals(normalizedAddress));
            if (householdInUse) {
                throw new DuplicateOwnerHouseholdException(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
            }
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizedAddress);
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizedEmail);
        owner.setHouseholdId(householdId);
        // Record how many existing owners (before this create) share this owner's first and last
        // name, compared case-insensitively. Computed against the current owners so the new owner
        // itself is never counted.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Record the size of this owner's household (owners sharing the same householdId) after this
        // create: the existing members already stamped with the shared identifier plus this new
        // owner. An owner not admitted into any household is a household of one.
        owner.setHouseholdSize(countHouseholdMembers(householdId) + 1);
        // Persist the bulk-signup flag computed above so it is returned on subsequent reads.
        owner.setBulkSignupWarning(bulkSignupWarning);
        // Store the business-day-adjusted effective registration date computed above. This defaults
        // to the server's current date when none was supplied and rolls any weekend date forward to
        // the following Monday, so everything derived from it (e.g. the membership number's year
        // segment) uses the adjusted date.
        owner.setRegistrationDate(effectiveRegistrationDate);
        // Assign the customer code: '<CITY3>-<LAST3>-<NNNN>' where CITY3 is the upper-cased first
        // three letters of the city, LAST3 the upper-cased first three letters of the last name and
        // NNNN is a per-city 4-digit zero-padded sequence equal to one more than the number of
        // owners already in that city.
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        // Emit an audit trail line for the successful create, carrying the owner id, the assigned
        // customer code, the effective registration date and the numeric membership level.
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), ownerDto.getMembershipLevel());
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
        currentOwner.setTelephone(toE164(ownerFieldsDto.getTelephone()));
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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Normalize an owner identity field for household-duplicate comparison: trim, collapse every
     * run of whitespace to a single space, and lower-case. Used so that last name and address are
     * compared case-insensitively with collapsed whitespace.
     */
    private static String collapse(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Common street-type abbreviations expanded during address normalization.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalize an owner address into its canonical stored/compared form: trim, collapse every run
     * of whitespace to a single space, upper-case, and expand common street-type abbreviations
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). Returns an empty string for
     * a {@code null} or blank input. The transformation is idempotent, so an already-normalized
     * address maps to itself.
     */
    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Derive the stable household identifier shared by owners with the same normalized last name
     * and address. It is a deterministic function of those two values, so every member of a
     * household independently derives the same identifier: the first 12 upper-case hex characters
     * of {@code SHA-256(normalizedLastName + '\n' + normalizedAddress)}.
     */
    private static String householdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((normalizedLastName + "\n" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Build the customer code for a new owner: {@code '<CITY3>-<LAST3>-<NNNN>'} where CITY3 is the
     * upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the
     * last name and NNNN is a per-city 4-digit zero-padded sequence equal to one more than the
     * number of owners already in that city (e.g. {@code 'SYD-SMI-0007'}).
     */
    /**
     * Count the existing owners that share the given first and last name, compared
     * case-insensitively. Called before the new owner is saved, so the returned value reflects the
     * namesakes that already existed at creation time (the new owner itself is not included).
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null && existing.getLastName() != null)
            .filter(existing -> existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
    }

    /**
     * Count the existing owners that already belong to the given household (i.e. carry the same
     * non-null householdId). Called after any existing members have been backfilled with the shared
     * identifier but before the new owner is saved, so the returned value reflects the members that
     * already existed at creation time (the new owner itself is not included). Returns 0 when the
     * new owner is not part of any household ({@code householdId} is null).
     */
    private int countHouseholdMembers(String householdId) {
        if (householdId == null) {
            return 0;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * The maximum number of owners a single city may contain. Once a city holds this many owners a
     * request to create another owner in it is rejected as a conflict.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Count the existing owners registered in the given city, compared case-insensitively (as in
     * {@link #nextCustomerCode}). Called before the new owner is saved, so the returned value
     * reflects the owners that already existed at creation time.
     */
    private int countOwnersInCity(String city) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
    }

    /**
     * The maximum number of owners that may be created on a single day. Once this many owners
     * already carry a given day's registration date, a request to create another owner that day is
     * rejected with a 429.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * The number of owners that may be created on a single day before the bulk-signup warning is
     * flagged. Once more than this many owners already carry a given day's registration date, a
     * newly created owner is flagged with {@code bulkSignupWarning} set true.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Count the existing owners whose registration date is the given day. Called before the new
     * owner is saved, so the returned value reflects the owners that already existed at creation
     * time.
     */
    private int countOwnersRegisteredOn(LocalDate date) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Roll a date forward onto a business day: a Saturday or Sunday is advanced to the following
     * Monday, while a weekday is returned unchanged. Used so the effective registration date (whether
     * supplied or defaulted) always falls on a business day.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        long sequence = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count() + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Convert a raw telephone into E.164 form. Spaces, dashes and brackets are stripped. A leading
     * {@code '+'} with its country code is preserved; otherwise the Australian country code
     * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national digits.
     * The result must be a {@code '+'} followed by 8 to 15 digits, otherwise an
     * {@link InvalidOwnerFieldsException} is thrown so the request is rejected with a 400.
     */
    private static String toE164(String raw) {
        String cleaned = raw.trim().replaceAll("[\\s()\\-]", "");
        boolean hasCountryCode = cleaned.startsWith("+");
        String digits = hasCountryCode ? cleaned.substring(1) : cleaned;
        if (!digits.matches("\\d+")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        if (!hasCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return "+" + digits;
    }

    /**
     * Pragmatic syntactic check for an email address: a non-empty local part, an {@code @},
     * and a domain with at least one dot-separated label, none of which contain whitespace or
     * a second {@code @}. This is deliberately permissive but rejects obviously malformed input.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /**
     * Validate and normalize an optional owner email. Returns {@code null} when no email was
     * supplied (the field is optional). When supplied it must be a syntactically valid address,
     * otherwise an {@link InvalidOwnerFieldsException} is thrown so the request is rejected with a
     * 400. A valid address is returned lower-cased.
     */
    private static String normalizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return trimmed.toLowerCase();
    }
}
