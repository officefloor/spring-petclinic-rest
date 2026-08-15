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

import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
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

    /** Dedicated audit trail logger; carries a line for each successful owner create. */
    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("AUDIT");

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

    /** Matches a syntactically valid email address: a local part and domain (with at least one dot)
     *  separated by '@', none of which contain whitespace or a second '@'. */
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Disposable email domains that are not accepted as an owner's email. */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Normalize the optional email: absent stays absent; when present it must be syntactically
     * valid and is lower-cased. Returns {@code false} if present but invalid, including when its
     * domain is on the disposable-domain blocklist.
     */
    private boolean normalizeEmail(Owner owner) {
        String email = owner.getEmail();
        if (email == null || email.isEmpty()) {
            owner.setEmail(null);
            return true;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        email = email.toLowerCase(java.util.Locale.ROOT);
        String domain = email.substring(email.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            return false;
        }
        owner.setEmail(email);
        return true;
    }

    /**
     * Normalize a telephone number to E.164 form: strip spaces, dashes and brackets; keep a leading
     * '+' and country code when present, otherwise assume country code '+61' and drop a single leading
     * '0' from the national digits. The result must have 8 to 15 digits after the '+'.
     *
     * @return the E.164 string (e.g. {@code +61412345678}), or {@code null} when the input cannot form
     *         a valid E.164 number.
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Validate the national-number length of a canonical E.164 string against its country code:
     * '+61' (Australia) requires 9 national digits and '+1' (NANP) requires 10. Numbers carrying any
     * other country code are not length-checked here and are accepted. Expects the '+'-prefixed digit
     * string produced by {@link #toE164}.
     *
     * @return {@code true} when the national-number length is correct for the country code.
     */
    private static boolean hasValidNationalLength(String e164) {
        String digits = e164.substring(1);
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }

    /**
     * Build the customer code {@code <REGION>-<HASH8>}: REGION is the region code derived from the
     * owner's postcode (NSW/VIC/QLD, or {@code UNKNOWN} when the postcode is absent or in no known
     * range) and HASH8 is the first 8 upper-case hex characters of SHA-256 over the concatenation of
     * the owner's normalized (E.164) telephone and last name (e.g. {@code NSW-3F9A0C17}). No sequence
     * numbers are used, so the base code is a pure function of the owner's region-and-hash identity.
     * When that base code collides with an existing owner's customerCode, {@code -<n>} is appended with
     * the smallest {@code n} of 2 or more that makes it unique, returning the de-duplicated code.
     */
    private String customerCodeFor(Owner owner) {
        String region = ownerMapper.regionFromPostcode(owner);
        if (region == null) {
            region = "UNKNOWN";
        }
        String base = region + "-" + hash8(owner.getTelephone(), owner.getLastName());

        java.util.Set<String> existingCodes = new java.util.HashSet<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            String code = existing.getCustomerCode();
            if (code != null) {
                existingCodes.add(code);
            }
        }
        if (!existingCodes.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!existingCodes.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * First 8 upper-case hex characters (the first 4 bytes) of SHA-256 over
     * {@code normalizedTelephone + lastName}, treating a {@code null} component as empty.
     */
    private static String hash8(String normalizedTelephone, String lastName) {
        String input = (normalizedTelephone == null ? "" : normalizedTelephone)
            + (lastName == null ? "" : lastName);
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Common street-type abbreviations expanded during address normalization. */
    private static final java.util.Map<String, String> ADDRESS_ABBREVIATIONS =
        java.util.Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Normalize an address: {@code null} becomes empty, surrounding whitespace is trimmed, internal
     * runs of whitespace are collapsed to a single space, the value is upper-cased and common
     * street-type abbreviations (ST-&gt;STREET, RD-&gt;ROAD, AVE-&gt;AVENUE) are expanded token by token.
     */
    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
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
     * Normalize a value for household-duplicate comparison: {@code null} becomes empty, surrounding
     * whitespace is trimmed, internal runs of whitespace are collapsed to a single space and the result
     * is lower-cased, so the comparison is case-insensitive with collapsed whitespace.
     */
    private static String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Return the owners that already share this owner's household, i.e. resolve to the same
     * deterministic {@link #householdIdFor(Owner) householdId} (same normalized last name and
     * postcode).
     */
    private List<Owner> householdMembers(Owner owner) {
        String householdId = householdIdFor(owner);
        List<Owner> members = new java.util.ArrayList<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (householdId.equals(householdIdFor(existing))) {
                members.add(existing);
            }
        }
        return members;
    }

    /**
     * Return {@code true} when another owner already shares this owner's household, i.e. resolves to
     * the same deterministic {@link #householdIdFor(Owner) householdId}.
     */
    private boolean sharesHouseholdWithExisting(Owner owner) {
        return !householdMembers(owner).isEmpty();
    }

    /**
     * Count the existing owners that share this owner's first name and last name, compared
     * case-insensitively. Used to populate {@code namesakeCount} on create (before this owner is saved).
     */
    private int namesakeCount(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (firstName != null && lastName != null
                && firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }

    /**
     * A stable, shared household identifier: the first 12 hex characters of SHA-256 over
     * {@code normalizedLastName + '|' + postcode} (an absent postcode contributes an empty string).
     * Because it is a pure function of the last name and postcode, every owner in the same household
     * deterministically resolves to the same value.
     */
    private static String householdIdFor(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = normalizeForHousehold(owner.getLastName()) + "|" + postcode;
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final java.util.Map<String, int[]> REGION_POSTCODE_RANGES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validate the optional postcode. Absent (null/empty) is valid — postcode is optional. When
     * present it must be exactly 4 digits; and when the owner's city maps to a known region (via the
     * mapper's fixed city-to-region table) it must fall inside that region's inclusive range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit
     * postcode.
     */
    private boolean isPostcodeValid(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isEmpty()) {
            return true;
        }
        if (!postcode.matches("\\d{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODE_RANGES.get(ownerMapper.cityRegion(owner));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * Find an existing owner that makes this (already known to be non-hard-duplicate) owner a
     * "possible duplicate": one that shares the owner's last name (case-insensitive) and postcode
     * but carries a different (normalized) telephone. When several match, the one with the smallest
     * id is chosen so the result is deterministic. Returns {@code null} when the owner has no
     * postcode or no such existing owner exists.
     */
    private Owner possibleDuplicateOf(Owner owner) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (lastName == null || postcode == null || postcode.isEmpty()) {
            return null;
        }
        Owner match = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (lastName.equalsIgnoreCase(existing.getLastName())
                && postcode.equals(existing.getPostcode())
                && !java.util.Objects.equals(telephone, existing.getTelephone())) {
                if (match == null || (existing.getId() != null && match.getId() != null
                    && existing.getId() < match.getId())) {
                    match = existing;
                }
            }
        }
        return match;
    }

    /** Maximum number of owners permitted in a single city. */
    private static final int CITY_CAPACITY = 50;

    /** Maximum number of owners that may be created (by registrationDate) on a single day. */
    private static final int DAILY_CREATE_LIMIT = 100;

    /** Number of owners already created on a day beyond which a bulk-signup warning is flagged. */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Count the existing owners whose city matches this owner's city, compared case-insensitively.
     * Used to enforce the per-city capacity on create (before this owner is saved).
     */
    private int cityOwnerCount(Owner owner) {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Roll a registration date forward onto a business day: a Saturday or Sunday is advanced to the
     * following Monday, any weekday is returned unchanged. Applied to the effective registration date
     * (supplied or defaulted) so that everything derived from it — the stored {@code registrationDate},
     * the membership number's year segment and the per-day create limit — uses the adjusted date.
     */
    private static java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.plusDays(2);
            case SUNDAY:
                return date.plusDays(1);
            default:
                return date;
        }
    }

    /**
     * Count the existing owners whose registrationDate equals the given day. Used to enforce the
     * per-day create limit on create (before this owner is saved).
     */
    private int ownersRegisteredOn(java.time.LocalDate day) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (!isPostcodeValid(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String telephone = toE164(owner.getTelephone());
        if (telephone == null || !hasValidNationalLength(telephone)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!normalizeEmail(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String address = normalizeAddress(owner.getAddress());
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(address);
        owner.setTelephone(telephone);
        if (owner.getRegistrationDate() != null
            && owner.getRegistrationDate().isAfter(java.time.LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        java.time.LocalDate registrationDate = toBusinessDay(
            owner.getRegistrationDate() == null ? java.time.LocalDate.now() : owner.getRegistrationDate());
        int ownersRegisteredToday = ownersRegisteredOn(registrationDate);
        if (ownersRegisteredToday >= DAILY_CREATE_LIMIT) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        if (cityOwnerCount(owner) >= CITY_CAPACITY) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // The householdId is deterministic (SHA-256 over normalized lastName + '|' + postcode), so
        // owners with the same last name and postcode automatically share it. The link is no longer
        // created by copying an existing owner's id around.
        owner.setHouseholdId(householdIdFor(owner));
        List<Owner> householdMembers = householdMembers(owner);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        boolean declaredHouseholdMember = sharesHousehold && !householdMembers.isEmpty();

        // Hard duplicate: a new owner whose WHOLE identityKey (telephone|email|householdId) equals an
        // existing owner's is a byte-for-byte identity match and is always rejected.
        String identityKey = owner.getIdentityKey();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (identityKey.equals(existing.getIdentityKey())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        // Household duplicate: another owner already shares this (last name, postcode) household.
        // Rejected as a duplicate unless the caller sets 'sharesHousehold', which now only bypasses
        // this block (the shared householdId itself is already assigned deterministically above).
        if (!householdMembers.isEmpty() && !sharesHousehold) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // A declared household member is not a suspected duplicate. Otherwise, flag an owner that
        // shares an existing owner's last name and postcode with a different telephone.
        if (declaredHouseholdMember) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
        else {
            Owner duplicateOf = possibleDuplicateOf(owner);
            owner.setPossibleDuplicate(duplicateOf != null);
            owner.setPossibleDuplicateOf(duplicateOf == null ? null : duplicateOf.getId());
        }
        owner.setNamesakeCount(namesakeCount(owner));
        owner.setHouseholdSize(householdMembers.size() + 1);
        owner.setBulkSignupWarning(ownersRegisteredToday > BULK_SIGNUP_WARNING_THRESHOLD);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(customerCodeFor(owner));
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
        currentOwner.setEmail(ownerFieldsDto.getEmail());
        if (!normalizeEmail(currentOwner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
