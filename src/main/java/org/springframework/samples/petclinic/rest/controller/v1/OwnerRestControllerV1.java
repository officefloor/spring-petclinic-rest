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
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
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
        // Postcode is optional; when present it must be a 4-digit value that is valid for the
        // owner's city per the fixed region ranges. A malformed or out-of-range postcode is
        // rejected with a 400. An absent postcode leaves the request contract unchanged.
        validatePostcode(ownerFieldsDto.getCity(), ownerFieldsDto.getPostcode());
        // Normalize the telephone into E.164 form; a number that cannot form a valid
        // E.164 value is rejected with a 400.
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        // Email is optional; when present it must be a syntactically valid address and is
        // stored lower-cased. An invalid address is rejected with a 400.
        String normalizedEmail = normalizeEmail(ownerFieldsDto.getEmail());
        // Derive the household id up front (a deterministic function of the normalized last name and
        // address) whenever the caller opts into a shared household, so it can feed the identity key
        // below. Owners not admitted into a household have a null household id.
        String normalizedLastName = collapse(ownerFieldsDto.getLastName());
        String householdId = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())
            ? householdId(normalizedLastName, normalizedAddress)
            : null;
        // All duplicate detection is consolidated into a single derived identity key:
        // 'normalizedTelephone + | + (email or empty) + | + householdId'. Because the telephone is
        // part of the key, two members of the same household (same householdId) with different
        // telephones have different keys and are both allowed; only an exact whole-key match is a
        // duplicate. Reject with 409 when a new owner's whole identity key equals an existing one's.
        String identityKey = identityKey(normalizedTelephone, normalizedEmail, householdId);
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(
                identityKey(existing.getTelephone(), existing.getEmail(), existing.getHouseholdId())));
        if (identityInUse) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
        // For a shared household, backfill the shared identifier onto any existing member of this
        // household so the whole household carries the same stable value.
        if (householdId != null) {
            for (Owner existing : this.clinicService.findAllOwners()) {
                if (collapse(existing.getLastName()).equals(normalizedLastName)
                    && normalizeAddress(existing.getAddress()).equals(normalizedAddress)
                    && !householdId.equals(existing.getHouseholdId())) {
                    existing.setHouseholdId(householdId);
                    this.clinicService.saveOwner(existing);
                }
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
        // Assign the customer code: '<REGION>-<HASH8>' where REGION is the region derived from the
        // owner's postcode (falling back to the city-to-region table, else 'UNKNOWN', matching the
        // locality derivation) and HASH8 is the first 8 upper-case hex characters of
        // SHA-256(normalizedTelephone + lastName). Sequence numbers are no longer used.
        owner.setCustomerCode(customerCode(
            deriveRegion(owner.getPostcode(), owner.getCity()), normalizedTelephone, owner.getLastName()));
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
     * Derive the identity key into which all duplicate detection is consolidated:
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. A null email or
     * household id contributes an empty segment. Two owners are duplicates only when their whole
     * identity keys are equal.
     */
    private static String identityKey(String telephone, String email, String householdId) {
        return (telephone == null ? "" : telephone)
            + "|" + (email == null ? "" : email)
            + "|" + (householdId == null ? "" : householdId);
    }

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

    /**
     * Build the customer code for a new owner: {@code '<REGION>-<HASH8>'} where REGION is the region
     * derived from the owner's postcode/city and HASH8 is the first 8 upper-case hex characters of
     * {@code SHA-256(normalizedTelephone + lastName)} (e.g. {@code 'NSW-1A2B3C4D'}).
     */
    private static String customerCode(String region, String normalizedTelephone, String lastName) {
        return region + "-" + hash8(normalizedTelephone + (lastName == null ? "" : lastName));
    }

    /**
     * First 8 upper-case hex characters of {@code SHA-256(input)}.
     */
    private static String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 8).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Derive the owner's region, preferring the postcode: look up the region by postcode range first
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), then fall back to the city-to-region table
     * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD) when the postcode is absent or in no known
     * range. Returns {@code 'UNKNOWN'} when neither source resolves a region. This mirrors the
     * mapper's locality derivation, so the customer code's region segment and the owner's locality
     * always agree.
     */
    private static String deriveRegion(String postcode, String city) {
        if (postcode != null && postcode.matches("\\d{4}")) {
            int value = Integer.parseInt(postcode);
            for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        if (city == null) {
            return "UNKNOWN";
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The fixed city-to-region table used to validate a supplied postcode against the owner's city
     * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD). A city not present here has no known region
     * and accepts any 4-digit postcode.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * The fixed region-to-postcode ranges (inclusive): NSW 2000-2099, VIC 3000-3099, QLD 4000-4099.
     * Each entry is a two-element {@code {low, high}} array.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validate an optional owner postcode. Does nothing when no postcode was supplied (the field is
     * optional and the request contract stays backward-compatible). When supplied it must be exactly
     * 4 digits and, if the city maps to a known region, must fall within that region's inclusive
     * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any
     * 4-digit postcode. A malformed or out-of-range postcode throws an
     * {@link InvalidOwnerFieldsException} so the request is rejected with a 400.
     */
    private static void validatePostcode(String city, String postcode) {
        if (postcode == null || postcode.isEmpty()) {
            return;
        }
        if (!postcode.matches("\\d{4}")) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
        String region = city == null ? null : CITY_REGION.get(city);
        if (region == null) {
            return;
        }
        int[] range = REGION_POSTCODES.get(region);
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }

    /**
     * Required national-number length for each E.164 country calling code we validate: a
     * {@code '+61'} (Australia) number carries 9 national digits and a {@code '+1'} (NANP) number
     * carries 10. Country codes not listed here are accepted at any (generally valid) length.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of(
        "1", 10,
        "61", 9);

    /**
     * Convert a raw telephone into E.164 form. Spaces, dashes and brackets are stripped. A leading
     * {@code '+'} with its country code is preserved; otherwise the Australian country code
     * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national digits.
     * The result must be a {@code '+'} followed by 8 to 15 digits, and for a recognised country code
     * its national-number length must match ({@code '+61'} requires 9 national digits, {@code '+1'}
     * requires 10); otherwise an {@link InvalidOwnerFieldsException} is thrown so the request is
     * rejected with a 400.
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
        // For a recognised country code, the national number (the digits after the code) must be
        // exactly the expected length for that country. Match the longest known code so a shorter
        // code that is a prefix of another cannot mask it.
        for (int codeLength = 3; codeLength >= 1; codeLength--) {
            if (digits.length() <= codeLength) {
                continue;
            }
            String countryCode = digits.substring(0, codeLength);
            Integer requiredNationalLength = NATIONAL_NUMBER_LENGTHS.get(countryCode);
            if (requiredNationalLength != null) {
                if (digits.length() - codeLength != requiredNationalLength) {
                    throw new InvalidOwnerFieldsException(List.of("telephone"));
                }
                break;
            }
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
