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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
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
     * Syntactic email validation: a non-empty local part, a single '@', and a
     * domain with at least one dot-separated label ending in a letter-only TLD.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /**
     * Disposable / throw-away email domains whose owners are rejected: an email on one of these
     * domains is treated as invalid input and rejected with 400. Compared case-insensitively.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Dedicated audit logger for owner lifecycle side-effects. */
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

    /**
     * Normalise a raw telephone number to E.164 form.
     *
     * <p>Spaces, dashes and brackets are stripped. A leading '+' and its country
     * code are kept when present; otherwise country code '+61' is assumed and a
     * single leading '0' is dropped from the national digits. The result must have
     * between 8 and 15 digits after the '+'.
     *
     * <p>When the caller supplies the country code explicitly (a leading '+'), the
     * national-number length is additionally validated against that country code:
     * '+61' requires 9 national digits and '+1' requires 10. A number whose national
     * length is wrong for its country is rejected.
     *
     * @return the E.164 string, or {@code null} if the input cannot form a valid one.
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\[\\]-]", "");
        String digits;
        boolean explicitCountryCode;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
            explicitCountryCode = true;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
            explicitCountryCode = false;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        if (explicitCountryCode && !hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Validate the national-number length of an E.164 number (its digits without the
     * leading '+') against the requirement for its country code: country code '+61'
     * (Australia) requires 9 national digits and '+1' (NANP) requires 10. Country codes
     * without a configured length requirement are accepted unchanged.
     */
    private static boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - "61".length() == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - "1".length() == 10;
        }
        return true;
    }

    /**
     * Normalise a text field for case-insensitive, whitespace-insensitive identity
     * comparison: leading/trailing whitespace is trimmed, internal runs of whitespace
     * are collapsed to a single space, and the result is lower-cased. A {@code null}
     * input normalises to the empty string.
     */
    private static String normaliseIdentity(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Normalise a postal address into its canonical stored form: leading/trailing whitespace is
     * trimmed, internal runs of whitespace are collapsed to a single space, the text is upper-cased,
     * and common street-type abbreviations are expanded ({@code ST -> STREET}, {@code RD -> ROAD},
     * {@code AVE -> AVENUE}). A {@code null} input normalises to the empty string. This is the value
     * that is both stored and used for every address comparison.
     */
    static String normaliseAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(expandAddressAbbreviation(tokens[i]));
        }
        return result.toString();
    }

    /**
     * Expand a single upper-cased address token if it is a known street-type abbreviation, ignoring a
     * trailing period (so both {@code ST} and {@code ST.} become {@code STREET}). Unknown tokens are
     * returned unchanged.
     */
    private static String expandAddressAbbreviation(String token) {
        String core = token.endsWith(".") ? token.substring(0, token.length() - 1) : token;
        return switch (core) {
            case "ST" -> "STREET";
            case "RD" -> "ROAD";
            case "AVE" -> "AVENUE";
            default -> token;
        };
    }

    /**
     * Derive the stable, shared household identifier for owners in the same household: the first 12
     * lower-case hex characters of the SHA-256 digest over {@code '<normalizedLastName>|<postcode>'},
     * so every owner with the same last name and postcode resolves to the same value regardless of
     * the order in which they were created.
     */
    private static String householdId(String lastNameKey, String postcode) {
        return hashHex(lastNameKey + "|" + (postcode == null ? "" : postcode), 12, false);
    }

    /**
     * Count how many owners belong to the given owner's household (owners sharing its normalized last
     * name and postcode). An owner without a postcode has no household peers and counts as a household
     * of one.
     */
    private int householdSize(Owner owner) {
        if (owner.getPostcode() == null) {
            return 1;
        }
        String householdId = householdId(normaliseIdentity(owner.getLastName()), owner.getPostcode());
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getPostcode() != null
                && householdId.equals(
                    householdId(normaliseIdentity(existing.getLastName()), existing.getPostcode())))
            .count();
    }

    /**
     * Build an owner's derived duplicate-detection identity key as
     * {@code '<normalizedTelephone>|<email or empty>|<householdId or empty>'}. Each {@code null}
     * component contributes the empty string, and the email is lower-cased so the comparison is
     * case-insensitive. Two owners are duplicates only when their whole identity keys are equal.
     */
    private static String identityKey(String normalizedTelephone, String email, String householdId) {
        String telephone = normalizedTelephone == null ? "" : normalizedTelephone;
        String emailKey = email == null ? "" : email.toLowerCase();
        String household = householdId == null ? "" : householdId;
        return telephone + "|" + emailKey + "|" + household;
    }

    /**
     * Build the customer code for a new owner as {@code '<REGION>-<HASH8>'}, where REGION is the region
     * code derived from the postcode (falling back to the city-to-region table, else {@code "UNKNOWN"})
     * and HASH8 is the first 8 upper-case hex characters of the SHA-256 digest over
     * {@code normalizedTelephone + lastName}. Sequence numbers are no longer used, so the code depends
     * only on the owner's own region and identity, not on how many owners already exist.
     */
    private String nextCustomerCode(String region, String normalizedTelephone, String lastName) {
        return region + "-" + hash8(normalizedTelephone + (lastName == null ? "" : lastName));
    }

    /**
     * First 8 upper-case hex characters of the SHA-256 digest of the UTF-8 bytes of {@code input}.
     */
    private static String hash8(String input) {
        return hashHex(input, 8, true);
    }

    /**
     * First {@code length} hex characters of the SHA-256 digest of the UTF-8 bytes of {@code input},
     * upper-cased when {@code upperCase} is true and lower-cased otherwise.
     */
    private static String hashHex(String input, int length, boolean upperCase) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hex = sb.substring(0, length);
            return upperCase ? hex.toUpperCase() : hex;
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Resolve an owner's region preferring the postcode: the region is taken from the postcode range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) when the postcode resolves, otherwise from the
     * fixed city-to-region table, else {@code "UNKNOWN"}.
     */
    private static String region(String postcode, String city) {
        String fromPostcode = regionForPostcode(postcode);
        return fromPostcode != null ? fromPostcode : regionForCity(city);
    }

    /**
     * Region for a 4-digit postcode by inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099),
     * or {@code null} when the postcode is absent, not exactly 4 digits, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Roll a registration date forward to the next business day: when it falls on a Saturday or
     * Sunday it is advanced to the following Monday; weekday dates are returned unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Count the owners already created on the business day that {@code registrationDate} rolls to,
     * using the same per-day accumulation path as the daily create-limit rule.
     */
    private long ownersCreatedOn(LocalDate registrationDate) {
        LocalDate day = toBusinessDay(registrationDate);
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(existing -> existing != null)
            .map(OwnerRestControllerV1::toBusinessDay)
            .filter(day::equals)
            .count();
    }

    /**
     * Region -> inclusive 4-digit postcode range {@code {low, high}} used to validate a supplied
     * postcode against the owner's city region.
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Resolve an owner's region from the city using the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}); {@code "UNKNOWN"}
     * when the city is not listed.
     */
    private static String regionForCity(String city) {
        return switch (city == null ? "" : city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Validate a supplied postcode: it must be exactly 4 digits, and when the owner's city maps to a
     * known region the numeric value must fall within that region's inclusive range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
     */
    private static boolean isPostcodeValid(String postcode, String city) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(regionForCity(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * Whether the email's domain (the part after the final '@') is on the disposable-domain
     * blocklist. A {@code null} email is not disposable; the comparison is case-insensitive.
     */
    private static boolean isDisposableEmailDomain(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
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
        owners.forEach(owner -> owner.setHouseholdSize(householdSize(owner)));
        return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        owner.setHouseholdSize(householdSize(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(owner.getRegistrationDate() != null
            && ownersCreatedOn(owner.getRegistrationDate()) > 80);
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (isDisposableEmailDomain(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String address = normaliseAddress(ownerFieldsDto.getAddress());
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode != null && !isPostcodeValid(postcode, ownerFieldsDto.getCity())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (ownerFieldsDto.getRegistrationDate() != null
            && ownerFieldsDto.getRegistrationDate().isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveDate = ownerFieldsDto.getRegistrationDate() != null
            ? ownerFieldsDto.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        long ownersCreatedThatDay = this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(existing -> existing != null)
            .map(OwnerRestControllerV1::toBusinessDay)
            .filter(registrationDate::equals)
            .count();
        if (ownersCreatedThatDay >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        String cityKey = normaliseIdentity(ownerFieldsDto.getCity());
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normaliseIdentity(existing.getCity()).equals(cityKey))
            .count();
        if (ownersInCity >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        String lastNameKey = normaliseIdentity(ownerFieldsDto.getLastName());
        // The household is deterministically keyed on (normalized last name, postcode): every owner
        // with a postcode belongs to the household identified by this computed id, and owners sharing
        // a last name and postcode share it automatically.
        String householdId = (postcode == null) ? null : householdId(lastNameKey, postcode);
        // Existing owners already in this household (same last name and postcode).
        List<Owner> householdMembers = (householdId == null) ? List.of()
            : this.clinicService.findAllOwners().stream()
                .filter(existing -> householdId.equals(
                    householdId(normaliseIdentity(existing.getLastName()), existing.getPostcode())))
                .toList();
        // A second owner in an existing household is a household duplicate: rejected with 409 unless
        // it declares 'sharesHousehold', which only bypasses this block (the household link itself is
        // already assigned deterministically above).
        if (!householdMembers.isEmpty() && !sharesHousehold) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        String identityKey = identityKey(telephone, email, householdId);
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey(toE164(existing.getTelephone()),
                existing.getEmail(), existing.getHouseholdId()).equals(identityKey));
        if (identityInUse) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        String firstNameKey = normaliseIdentity(ownerFieldsDto.getFirstName());
        int namesakeCount = (int) this.clinicService.findAllOwners().stream()
            .filter(existing ->
                normaliseIdentity(existing.getFirstName()).equals(firstNameKey)
                    && normaliseIdentity(existing.getLastName()).equals(lastNameKey))
            .count();
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setNamesakeCount(namesakeCount);
        owner.setAddress(address);
        owner.setTelephone(telephone);
        owner.setEmail(email == null ? null : email.toLowerCase());
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(
            region(postcode, owner.getCity()), telephone, owner.getLastName()));
        owner.setHouseholdId(householdId);
        // A created owner is never flagged as a suspected duplicate: a lone owner has no household
        // peer, and a same-household owner can only be created by declaring 'sharesHousehold', which
        // makes it an intentional household member rather than a suspected duplicate.
        owner.setPossibleDuplicateOf(null);
        this.clinicService.saveOwner(owner);
        owner.setHouseholdSize(householdSize(owner));
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerMapper.formatMembershipLevel(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(ownersCreatedThatDay > 80);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(telephone);
        currentOwner.setEmail(email == null ? null : email.toLowerCase());
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
