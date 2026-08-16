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
import org.springframework.samples.petclinic.mapper.IdentityKeyDeriver;
import org.springframework.samples.petclinic.mapper.LocalityDeriver;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyRegistrationLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidPostcodeException;
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
     * A pragmatic syntactic check for an email address: a non-empty local part, an {@code @},
     * and a dotted domain. Deliberately conservative so obviously malformed values (e.g. a bare
     * word with no {@code @}) are rejected while ordinary addresses are accepted.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Dedicated audit logger; carries owner lifecycle side-effects. */
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
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        String normalizedAddress = normalizeAddress(ownerFieldsDto.getAddress());
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
            throw new MissingOwnerFieldsException(missingFields);
        }
        validatePostcode(ownerFieldsDto.getCity(), ownerFieldsDto.getPostcode());
        LocalDate suppliedRegistrationDate = ownerFieldsDto.getRegistrationDate();
        if (suppliedRegistrationDate != null && suppliedRegistrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                "registrationDate must not be later than the server date");
        }
        LocalDate registrationDate = toBusinessDay(
            suppliedRegistrationDate != null
                ? suppliedRegistrationDate
                : LocalDate.now());
        long registeredOnDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (registeredOnDay >= 100) {
            throw new DailyRegistrationLimitException(
                "100 or more owners have already been registered today");
        }
        boolean bulkSignupWarning = registeredOnDay > 80;
        String city = ownerFieldsDto.getCity();
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (ownersInCity >= 50) {
            throw new CityAtCapacityException(
                "the owner's city already contains 50 or more owners");
        }
        ownerFieldsDto.setAddress(normalizedAddress);
        String telephone = toE164(ownerFieldsDto.getTelephone());
        ownerFieldsDto.setTelephone(telephone);
        String email = normalizeEmail(ownerFieldsDto.getEmail());
        ownerFieldsDto.setEmail(email);
        String lastName = normalizeHousehold(ownerFieldsDto.getLastName());
        String postcode = ownerFieldsDto.getPostcode();
        // Deterministic household id: owners with the same normalized last name and postcode
        // share it automatically, regardless of creation order. It is no longer created by the
        // 'sharesHousehold' flag; that flag now only bypasses the duplicate block below.
        String householdId = householdId(lastName, postcode);
        // The household is keyed on (last name, postcode), so its existing members are the
        // existing owners with the same computed household id. Both the duplicate block and the
        // household size below key off this value.
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(
                householdId(normalizeHousehold(existing.getLastName()), existing.getPostcode())))
            .toList();
        // Household duplicate block: a second owner in an existing household is rejected as a
        // household duplicate (409) unless the request opts in via 'sharesHousehold'. Opting in
        // only bypasses this block; the owner is then created as a declared household member.
        if (!householdMembers.isEmpty()
            && !Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            throw new DuplicateHouseholdException(
                "an owner with the same last name and postcode already exists");
        }
        // Single, consolidated duplicate check: reject only when the new owner's whole
        // derived identity key (telephone|email|householdId) equals an existing owner's.
        String identityKey = IdentityKeyDeriver.identityKey(telephone, email, householdId);
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(IdentityKeyDeriver.identityKey(
                existing.getTelephone(), existing.getEmail(),
                householdId(normalizeHousehold(existing.getLastName()), existing.getPostcode()))));
        if (identityInUse) {
            throw new DuplicateIdentityException(
                "an owner with the same identity key already exists");
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(
            customerCode(owner.getPostcode(), owner.getTelephone(), owner.getLastName()));
        owner.setHouseholdId(householdId);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(bulkSignupWarning);
        // Household size after this create: existing members plus the owner being created.
        owner.setHouseholdSize(householdMembers.size() + 1);
        // A declared household member is not a suspected duplicate, and no other create path
        // reaches here for a same-household owner, so a created owner is never flagged.
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel());
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
        return value == null || value.isBlank();
    }

    /**
     * Rolls a registration date forward to the next business day: when it falls on a Saturday or
     * Sunday it is advanced to the following Monday; a weekday is returned unchanged. This is applied
     * to the effective registration date (whether supplied in the request or defaulted to the server
     * date), so every value derived from the registration date sees the adjusted business day.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Normalizes a last name for household-duplicate comparison: leading and trailing whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, and the result is
     * lower-cased so that last names are compared case-insensitively.
     */
    private static String normalizeHousehold(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Common street-type abbreviations expanded to their canonical full form during normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Normalizes an address to its canonical stored form: leading and trailing whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, the result is
     * upper-cased, and common street-type abbreviations are expanded token-by-token
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} or
     * whitespace-only value normalizes to the empty string. This form is both stored/returned and
     * used for every address comparison (household-duplicate detection and the shared household id).
     */
    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Derives the stable {@code householdId} shared by all owners in a household: the first 12
     * hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. It is a pure,
     * deterministic function of the normalized last name and postcode, so every owner that shares
     * those values maps to the same identifier regardless of creation order. A {@code null}
     * postcode contributes the empty string.
     */
    private static String householdId(String normalizedLastName, String postcode) {
        String key = normalizedLastName + "|" + (postcode == null ? "" : postcode);
        return sha256Hex12(key);
    }

    /** The first 12 lower-case hex characters (6 bytes) of the SHA-256 digest of {@code s}. */
    private static String sha256Hex12(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Builds the {@code customerCode} assigned to a new owner, formatted
     * {@code '<REGION>-<HASH8>'}: {@code REGION} is the region code derived from the owner's postcode
     * ({@code NSW}/{@code VIC}/{@code QLD}, or {@code UNKNOWN}), and {@code HASH8} is the first 8
     * upper-case hex characters of SHA-256 over the owner's normalized E.164 telephone concatenated
     * with the last name (e.g. {@code 'NSW-1A2B3C4D'}). No sequence number is used, so the code is a
     * pure function of the owner's own identity fields.
     */
    private static String customerCode(String postcode, String telephone, String lastName) {
        String region = LocalityDeriver.region(postcode);
        String hash8 = sha256Hex8((telephone == null ? "" : telephone)
            + (lastName == null ? "" : lastName));
        return region + "-" + hash8;
    }

    /** The first 8 upper-case hex characters (4 bytes) of the SHA-256 digest of {@code s}. */
    private static String sha256Hex8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Counts the existing owners that share the given first name and last name, compared
     * case-insensitively. This is evaluated over the owners that already exist (i.e. before the
     * owner currently being created is saved), so it reflects how many namesakes preceded the
     * new owner.
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName != null && firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName != null && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Normalizes a telephone number to E.164 form. Spaces, dashes and brackets are stripped. A
     * leading {@code '+'} and its country code are kept as-is; otherwise the number is treated as a
     * national one, a single leading {@code '0'} is dropped, and the default country code
     * {@code '+61'} is prepended. The result must have 8 to 15 digits after the {@code '+'}, and its
     * national number must have the length required by its country code ({@code '+61'} requires 9
     * national digits, {@code '+1'} requires 10); otherwise an {@link InvalidTelephoneException} is
     * raised (reported to the client as 400).
     */
    private static String toE164(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(
                "telephone must form a valid E.164 number with 8 to 15 digits");
        }
        validateNationalLength(digits);
        return "+" + digits;
    }

    /** National-number length required for each supported country code (digits after the code). */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY_CODE = Map.of(
        "61", 9, "1", 10);

    /**
     * Validates the national-number length of an E.164 number (digits after the {@code '+'}) against
     * its country code. {@code '+61'} requires 9 national digits and {@code '+1'} requires 10; a
     * mismatch raises an {@link InvalidTelephoneException} (reported to the client as 400). Country
     * codes without a configured requirement are left to the general 8-to-15-digit rule.
     */
    private static void validateNationalLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH_BY_COUNTRY_CODE.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code)) {
                int nationalLength = digits.length() - code.length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidTelephoneException(
                        "telephone national number must have " + entry.getValue()
                            + " digits for country code +" + code);
                }
                return;
            }
        }
    }

    /**
     * Inclusive 4-digit postcode range fixed for each region: NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099. A region absent from this table (i.e. a city with no known region) accepts
     * any 4-digit postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's optional {@code postcode}. When absent (or blank) it is left unvalidated
     * and the owner is accepted, keeping the request contract backward-compatible. When present it
     * must be a 4-digit value; and when the owner's city maps to a known region it must fall within
     * that region's fixed inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with
     * no known region accepts any 4-digit postcode. A violation raises an
     * {@link InvalidPostcodeException} (reported to the client as 400).
     */
    private static void validatePostcode(String city, String postcode) {
        if (isBlank(postcode)) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException("postcode must be a 4-digit value");
        }
        int[] range = REGION_POSTCODE_RANGE.get(LocalityDeriver.localityForCity(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(
                "postcode is out of range for the owner's city region");
        }
    }

    /**
     * Email is optional: an absent (or blank) value is left as {@code null}. When a value is
     * present it must be a syntactically valid address, otherwise an {@link InvalidEmailException}
     * is raised (reported to the client as 400). A valid value is returned lower-cased so it is
     * stored and returned in canonical form.
     */
    private static String normalizeEmail(String email) {
        if (isBlank(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException("email must be a syntactically valid address");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
