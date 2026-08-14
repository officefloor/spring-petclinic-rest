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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * Pattern for a syntactically valid email address: a non-empty local part, an '@', and a
     * dotted domain ending in a letters-only label. Applied to the trimmed, lower-cased value.
     */
    /**
     * Dedicated audit logger. Successful side-effects (e.g. owner creation) are recorded here so
     * they can be routed and asserted independently of the application's diagnostic logging.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Disposable email domains that are refused: an owner whose email domain is on this blocklist
     * is rejected. The value is the already-normalized (trimmed, lower-cased) email's domain.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Common address abbreviations expanded to their full form during address normalization.
     * Matching is performed per whitespace-separated token on the upper-cased value.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Required national-number length (the digits after the country code) per E.164 country code.
     * A number whose country code appears here must carry exactly the mapped number of national
     * digits; country codes not listed fall back to the generic 8-to-15 total-digit rule.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        Map.of("61", 9, "1", 10);

    /**
     * Inclusive 4-digit postcode range {@code {low, high}} per region, the fixed ground truth for
     * validating an owner's postcode against the region derived from its city. A region not listed
     * here (e.g. the {@code "UNKNOWN"} locality of an unmapped city) imposes no range constraint, so
     * any 4-digit postcode is accepted.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * City -> canonical region, the fixed ground truth for deriving the region a postcode is
     * validated against. A city not listed here derives the {@code "UNKNOWN"} region, which imposes
     * no postcode-range constraint.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        LocalDate suppliedDate = owner.getRegistrationDate();
        if (suppliedDate != null && suppliedDate.isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveDate = suppliedDate != null ? suppliedDate : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        owner.setRegistrationDate(registrationDate);
        long createdToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (createdToday >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        owner.setBulkSignupWarning(createdToday > 80);
        if (!applyAddress(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setTelephone(telephone);
        if (owner.getEmail() != null) {
            String email = normalizeEmail(owner.getEmail());
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (isDisposableEmailDomain(email)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            owner.setEmail(email);
        }
        if (!isPostcodeValidForCity(owner.getPostcode(), regionForCity(owner.getCity()))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String cityKey = householdKey(owner.getCity());
        long cityCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null
                && cityKey.equals(householdKey(existing.getCity())))
            .count();
        if (cityCount >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // The household is keyed on (normalized last name, postcode): every owner derives the same
        // deterministic householdId from those two fields, so owners sharing them are the same
        // household. This computed id is what duplicate detection and the household size below key off.
        String householdId = householdId(owner);
        owner.setHouseholdId(householdId);
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.getDeleted())
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
        owner.setHouseholdSize(householdMembers.size() + 1);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // Household-duplicate block: a second owner in an existing household (same computed
        // householdId) is rejected as a duplicate. 'sharesHousehold' now only BYPASSES this block —
        // declaring the new owner a member — rather than creating the household link itself.
        if (!householdMembers.isEmpty() && !sharesHousehold) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Single consolidated duplicate check: the telephone, email and household duplicate rules are
        // all expressed through the one derived identityKey (telephone|email|householdId). A create is
        // rejected only when a new owner's WHOLE identityKey equals an existing owner's; because the
        // telephone is part of the key, two household members with different telephones are allowed.
        // This still catches a full identity repeat even when it declares sharesHousehold.
        String identityKey = owner.getIdentityKey();
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.getDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (identityInUse) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Soft-duplicate detection. A declared household member (sharesHousehold) is never a suspected
        // duplicate. Any other owner sharing an existing owner's last name and postcode with a
        // different telephone is now, by definition, a household duplicate already rejected above, so
        // it never reaches this point; the check is retained for owners that are not household matches.
        if (sharesHousehold) {
            owner.setPossibleDuplicate(false);
        } else {
            String softDuplicateLastNameKey = householdKey(owner.getLastName());
            String softDuplicatePostcode = owner.getPostcode();
            String softDuplicateTelephone = owner.getTelephone();
            Owner possibleDuplicate = softDuplicatePostcode == null ? null
                : this.clinicService.findAllOwners().stream()
                    .filter(existing -> !existing.getDeleted())
                    .filter(existing -> softDuplicateLastNameKey.equals(householdKey(existing.getLastName())))
                    .filter(existing -> softDuplicatePostcode.equals(existing.getPostcode()))
                    .filter(existing -> !softDuplicateTelephone.equals(existing.getTelephone()))
                    .min(Comparator.comparing(Owner::getId))
                    .orElse(null);
            if (possibleDuplicate != null) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(possibleDuplicate.getId());
            } else {
                owner.setPossibleDuplicate(false);
            }
        }
        owner.setCustomerCode(customerCode(owner));
        String firstNameKey = householdKey(owner.getFirstName());
        String namesakeLastNameKey = householdKey(owner.getLastName());
        long namesakeCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> firstNameKey.equals(householdKey(existing.getFirstName()))
                && namesakeLastNameKey.equals(householdKey(existing.getLastName())))
            .count();
        owner.setNamesakeCount((int) namesakeCount);
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            owner.getMembershipLevel(), owner.getMembershipNumber());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Validates an owner's postcode against the region derived from its city. The postcode is
     * optional: a {@code null} value is always accepted. When present it must fall within the
     * inclusive range mapped to the region by {@link #REGION_POSTCODES}; a region with no known
     * range (e.g. the {@code "UNKNOWN"} locality of an unmapped city) accepts any 4-digit postcode.
     * The 4-digit syntax itself is enforced upstream by bean validation.
     *
     * @param postcode the owner's postcode (may be {@code null})
     * @param locality the region derived from the owner's city
     * @return {@code true} when the postcode is absent, the region has no range, or the postcode is
     *         within the region's range; {@code false} when it is out of range
     */
    /**
     * The canonical region for the given city via the fixed {@link #CITY_REGION} table, or
     * {@code "UNKNOWN"} when the city is {@code null} or not in the table.
     *
     * @param city the owner's city (may be {@code null})
     * @return the canonical region, or {@code "UNKNOWN"}
     */
    private String regionForCity(String city) {
        if (city == null) {
            return "UNKNOWN";
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    private boolean isPostcodeValidForCity(String postcode, String locality) {
        if (postcode == null) {
            return true;
        }
        int[] range = REGION_POSTCODES.get(locality);
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * Normalizes a telephone number into E.164 form. Spaces, dashes and brackets are stripped.
     * When the value carries a leading {@code '+'} its country code is kept as given; otherwise the
     * Australian country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped
     * from the national digits. The result must carry 8 to 15 digits after the {@code '+'}. For
     * recognised country codes the national number (the digits after the country code) must also be
     * exactly the length that country requires ({@code '+61'} needs 9 national digits, {@code '+1'}
     * needs 10); a wrong national-number length yields {@code null}.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the E.164 telephone (e.g. {@code "+61412345678"}), or {@code null} if it cannot form
     *         a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String trimmed = telephone.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        String cleaned = trimmed.replaceAll("[\\s()\\[\\]-]", "");
        if (hasCountryCode) {
            cleaned = cleaned.substring(1);
        }
        if (!cleaned.matches("[0-9]+")) {
            return null;
        }
        String digits;
        if (hasCountryCode) {
            digits = cleaned;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        if (!hasValidNationalNumberLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length of an E.164 digit string (country code plus national
     * number, without the leading {@code '+'}) against its country code. When the number's country
     * code is one of the {@link #NATIONAL_NUMBER_LENGTHS recognised codes}, the digits after the
     * country code must number exactly what that country requires; the longest matching country
     * code wins. Numbers whose country code is not recognised are accepted here (they are governed
     * only by the generic total-length rule).
     *
     * @param digits the E.164 digits without the leading {@code '+'} (e.g. {@code "61412345678"})
     * @return {@code true} if the national-number length is acceptable for the country code
     */
    private boolean hasValidNationalNumberLength(String digits) {
        return NATIONAL_NUMBER_LENGTHS.entrySet().stream()
            .filter(entry -> digits.startsWith(entry.getKey()))
            .max(Comparator.comparingInt(entry -> entry.getKey().length()))
            .map(entry -> digits.length() - entry.getKey().length() == entry.getValue())
            .orElse(true);
    }

    /**
     * Normalizes an email address for storage by trimming surrounding whitespace and lower-casing
     * it. The result is validated by the caller against {@link #EMAIL_PATTERN}.
     *
     * @param email the raw email value (must not be {@code null})
     * @return the trimmed, lower-cased email
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Whether the domain of the given normalized email is on the disposable-domain blocklist
     * ({@link #DISPOSABLE_EMAIL_DOMAINS}). The domain is the substring after the last {@code '@'};
     * an email without an {@code '@'} carries no blocked domain.
     *
     * @param email the trimmed, lower-cased email
     * @return {@code true} when the email's domain is blocklisted
     */
    private boolean isDisposableEmailDomain(String email) {
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        return DISPOSABLE_EMAIL_DOMAINS.contains(email.substring(at + 1));
    }

    /**
     * Applies the owner's address, preferring the structured form over the flat one. When a
     * non-blank {@code addressLine1} is supplied the structured fields are normalized in place and
     * the flat {@code address} is composed as the normalized {@code addressLine1} followed, when a
     * non-blank {@code addressLine2} is present, by a single space and the normalized
     * {@code addressLine2}. Otherwise the flat {@code address} is normalized in place. An owner
     * that supplies neither a non-blank {@code addressLine1} nor a non-blank flat {@code address}
     * is invalid.
     *
     * @param owner the owner whose address fields are normalized and composed in place
     * @return {@code true} when a usable address was applied, {@code false} when none was supplied
     */
    private boolean applyAddress(Owner owner) {
        String line1 = owner.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String normalizedLine1 = normalizeAddress(line1);
            owner.setAddressLine1(normalizedLine1);
            String composed = normalizedLine1;
            String line2 = owner.getAddressLine2();
            if (line2 != null && !line2.isBlank()) {
                String normalizedLine2 = normalizeAddress(line2);
                owner.setAddressLine2(normalizedLine2);
                composed = normalizedLine1 + " " + normalizedLine2;
            }
            owner.setAddress(composed);
            return true;
        }
        String address = normalizeAddress(owner.getAddress());
        if (address == null || address.isEmpty()) {
            return false;
        }
        owner.setAddress(address);
        return true;
    }

    /**
     * Normalizes a postal address for storage and comparison: surrounding whitespace is trimmed,
     * internal runs of whitespace are collapsed to a single space, the value is upper-cased and
     * common street-type abbreviations are expanded per token ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). The stored and returned address is this
     * normalized form, and every household comparison uses it.
     *
     * @param address the raw address value (may be {@code null})
     * @return the normalized address, or {@code null} if the input was {@code null}
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
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
     * Normalizes a value for household-duplicate comparison: surrounding whitespace is trimmed,
     * internal runs of whitespace are collapsed to a single space and the result is lower-cased,
     * so that owners are compared case-insensitively with collapsed whitespace.
     *
     * @param value the raw value (must not be {@code null})
     * @return the normalized comparison key
     */
    private String householdKey(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Derives a stable, shared household identifier for the owner's household: the first 12 hex
     * characters of the SHA-256 digest of {@code normalizedLastName + '|' + postcode}, where the
     * last name is normalized with {@link #householdKey} and an absent postcode is rendered as the
     * empty string. Because it is a pure function of the last name and postcode, every owner sharing
     * those two fields derives the identical identifier and therefore belongs to the same household.
     *
     * @param owner the owner whose household identifier is derived
     * @return the stable household identifier
     */
    private String householdId(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = householdKey(owner.getLastName()) + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Builds the customer code for a newly registered owner in the form
     * {@code '<REGION>-<HASH8>'}, where {@code REGION} is the region code derived from the owner's
     * postcode (the same region derivation shared with {@link Owner#getLocality() locality}:
     * postcode range first, falling back to the city, or {@code 'UNKNOWN'}) and {@code HASH8} is the
     * first 8 upper-case hex characters of the SHA-256 digest of the normalized telephone
     * concatenated with the last name (e.g. {@code 'NSW-1A2B3C4D'}). When the computed code
     * collides with an existing owner's customer code, {@code '-<n>'} is appended using the
     * smallest {@code n} of 2 or more that makes the result unique, guaranteeing distinct owners
     * always receive distinct customer codes.
     *
     * @param owner the owner whose customer code is derived (with normalized telephone already set)
     * @return the assigned customer code
     */
    private String customerCode(Owner owner) {
        String region = owner.getLocality();
        String hash8 = sha256UpperHex(owner.getTelephone() + owner.getLastName(), 8);
        String base = region + "-" + hash8;
        Set<String> existingCodes = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(code -> code != null)
            .collect(Collectors.toSet());
        if (!existingCodes.contains(base)) {
            return base;
        }
        int n = 2;
        while (existingCodes.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /**
     * Returns the first {@code length} upper-case hex characters of the SHA-256 digest of the
     * UTF-8 bytes of {@code input}.
     *
     * @param input  the value to hash
     * @param length the number of leading hex characters to keep
     * @return the truncated, upper-cased hex digest
     */
    private String sha256UpperHex(String input, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase(Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Rolls a registration date forward onto a business day. When the given date falls on a
     * Saturday or Sunday it is advanced to the following Monday; a weekday is returned unchanged.
     *
     * @param date the effective registration date (must not be {@code null})
     * @return the same date if it is a weekday, otherwise the next Monday
     */
    private LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null) {
            email = normalizeEmail(email);
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(telephone);
        currentOwner.setEmail(email);
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
        // Soft-delete: flag the owner deleted and retain the record so it can still be read back.
        owner.setDeleted(true);
        this.clinicService.saveOwner(owner);
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
