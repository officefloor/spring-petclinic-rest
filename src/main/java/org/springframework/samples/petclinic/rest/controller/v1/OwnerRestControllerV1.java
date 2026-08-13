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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("AUDIT");

    /**
     * Monotonically increasing sequence assigned to each {@code OWNER_CREATED} structured event, in
     * the order owners are created across this application instance. Shared across all requests.
     */
    private static final java.util.concurrent.atomic.AtomicLong OWNER_EVENT_SEQ =
        new java.util.concurrent.atomic.AtomicLong();

    /** Serializes the immutable {@link OwnerCreatedEvent} to its canonical JSON form. */
    private static final tools.jackson.databind.ObjectMapper AUDIT_EVENT_MAPPER =
        tools.jackson.databind.json.JsonMapper.builder().build();

    /**
     * Immutable structured audit event emitted alongside the human-readable audit line when an owner
     * is created. Its component order is also its JSON field order: {@code seq}, {@code ownerId},
     * {@code customerCode}, {@code membershipLevel}, {@code event}. The {@code customerCode} component
     * carries the owner's <em>current primary identifier</em>; today that is the customer code, and it
     * is the single field a later checkpoint repoints when the identity is unified into the memberId.
     */
    private record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        Integer membershipLevel, String event) {
    }

    /**
     * Remembers, per seen {@code Idempotency-Key} header, the id of the owner originally created for
     * that key, so a repeated create carrying the same key returns that owner instead of creating a
     * duplicate. Kept in memory and keyed on the opaque client-supplied value.
     */
    private static final java.util.Map<String, Integer> IDEMPOTENCY_KEYS =
        new java.util.concurrent.ConcurrentHashMap<>();

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
        // Idempotent create: if this request carries an 'Idempotency-Key' we have already seen, return
        // the owner originally created for that key with 200 OK, rather than creating a duplicate (which
        // the identity/household duplicate rules would otherwise reject with 409).
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = IDEMPOTENCY_KEYS.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        applyAddress(owner);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        validateEmailDomain(owner.getEmail());
        validatePostcode(owner.getPostcode(), owner.getCity());
        java.time.LocalDate serverDate = java.time.LocalDate.now();
        if (owner.getRegistrationDate() != null && owner.getRegistrationDate().isAfter(serverDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Registration date must not be later than the current server date");
        }
        java.time.LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : serverDate;
        java.time.LocalDate businessDate = toBusinessDay(effectiveDate);
        owner.setRegistrationDate(businessDate);
        long createdToday = this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(businessDate::equals)
            .count();
        if (createdToday >= 100) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "The maximum number of owners for today has already been reached");
        }
        owner.setBulkSignupWarning(createdToday > 80);
        // The household is keyed deterministically on (normalized last name, postcode): every owner
        // computes the same householdId from those two fields, so owners sharing a last name and
        // postcode belong to the same household automatically, without any explicit linking.
        String lastNameKey = normalizeForHousehold(owner.getLastName());
        String householdId = householdId(lastNameKey, owner.getPostcode());
        owner.setHouseholdId(householdId);
        // Soft-deleted owners are excluded from all duplicate/identity detection: a deleted owner
        // no longer blocks a new owner that would otherwise match it.
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> householdId.equals(
                householdId(normalizeForHousehold(existing.getLastName()), existing.getPostcode())))
            .toList();
        // All duplicate detection is expressed through the single derived identityKey, the SHA-256
        // hex of (normalizedTelephone|lowerEmail|soundex(lastName)); a new owner is rejected only
        // when its whole key matches an existing (non-deleted) owner's.
        String identityKey = owner.getIdentityKey();
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (identityInUse) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "An owner with this identity already exists");
        }
        // Duplicate detection is now the single identity key above; because the telephone is part of
        // that key, two owners sharing a last name and postcode but with different telephones have
        // different keys and are both admitted, so the former household-duplicate 409 no longer
        // applies. Declaring 'sharesHousehold' marks the new owner as a declared household member.
        boolean declaredHouseholdMember = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // An undeclared new owner joining an existing household while carrying its own (distinct)
        // email address joins as a member whose membership level is capped below.
        boolean distinguishableByEmail = owner.getEmail() != null && !owner.getEmail().isBlank()
            && householdMembers.stream()
                .noneMatch(existing -> owner.getEmail().equalsIgnoreCase(existing.getEmail()));
        boolean joinsExistingHousehold = !householdMembers.isEmpty()
            && !declaredHouseholdMember && distinguishableByEmail;
        String cityKey = normalizeForHousehold(owner.getCity());
        long cityCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(cityKey))
            .count();
        if (cityCount >= 50) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This city already has the maximum number of owners");
        }
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(householdMembers.size() + 1);
        // A new owner joining an existing household cannot rank more than one level above the current
        // maximum membership level among their household members: its level is capped there. With no
        // existing household member (the usual case) no cap applies and the level stays as derived.
        if (joinsExistingHousehold) {
            int naturalLevel = Owner.membershipLevel(Owner.membershipPoints(owner));
            int maxMemberLevel = householdMembers.stream()
                .mapToInt(Owner::effectiveMembershipLevel)
                .max().orElse(0);
            owner.setMembershipLevel(Math.min(naturalLevel, maxMemberLevel + 1));
        }
        // Soft match: a new owner whose identityKey differs from every existing owner but whose
        // soundex(lastName) and postcode both match an existing (non-deleted) owner is flagged as a
        // possible duplicate of the earliest such owner. Soft-deleted owners are ignored, matching
        // the identity check above.
        String lastNameSoundex = Owner.soundex(owner.getLastName());
        Owner softMatch = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> !identityKey.equals(existing.getIdentityKey()))
            .filter(existing -> owner.getPostcode() != null
                && owner.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> lastNameSoundex.equals(Owner.soundex(existing.getLastName())))
            .min(java.util.Comparator.comparing(Owner::getId))
            .orElse(null);
        if (softMatch != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(softMatch.getId());
        } else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
        owner.setCustomerCode(deduplicateCustomerCode(customerCode(owner.getCity(), owner.getPostcode(),
            owner.getTelephone(), owner.getLastName())));
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            IDEMPOTENCY_KEYS.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), ownerDto.getMembershipLevel(),
            ownerDto.getMembershipNumber());
        // The event's primary identifier is the owner's customerCode today; a later checkpoint unifies
        // it into the memberId and repoints this single expression, and the event carries that instead.
        String primaryIdentifier = owner.getCustomerCode();
        OwnerCreatedEvent event = new OwnerCreatedEvent(OWNER_EVENT_SEQ.incrementAndGet(),
            owner.getId(), primaryIdentifier, ownerDto.getMembershipLevel(), "OWNER_CREATED");
        AUDIT.info(AUDIT_EVENT_MAPPER.writeValueAsString(event));
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Reads the current request's {@code Idempotency-Key} header, if any. Returns the trimmed header
     * value, or {@code null} when the header is absent, blank, or there is no active request (so a
     * request without the header behaves exactly as before).
     *
     * @return the non-blank idempotency key for the current request, or {@code null}
     */
    private String currentIdempotencyKey() {
        org.springframework.web.context.request.RequestAttributes attrs =
            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttrs)) {
            return null;
        }
        String key = servletAttrs.getRequest().getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) {
            return null;
        }
        return key.trim();
    }

    /**
     * The fixed public-holiday calendar the business-day roll skips. When an adjusted registration
     * date lands on one of these dates it is rolled forward to the next non-holiday business day.
     */
    private static final java.util.Set<java.time.LocalDate> PUBLIC_HOLIDAYS = java.util.Set.of(
        java.time.LocalDate.parse("2026-01-01"), java.time.LocalDate.parse("2026-01-26"),
        java.time.LocalDate.parse("2026-04-25"), java.time.LocalDate.parse("2026-12-25"),
        java.time.LocalDate.parse("2026-12-28"));

    /**
     * Rolls a registration date forward to a business day. A Saturday, Sunday or listed public
     * holiday is advanced one day at a time until it lands on a weekday that is not a public holiday;
     * a plain weekday is returned unchanged. This is applied to the effective registration date
     * (whether supplied in the request or defaulted to the server date) so that every stored
     * {@code registrationDate}, and any value derived from it, lands on a business day.
     *
     * @param date the effective registration date
     * @return the same date if it is a non-holiday weekday, otherwise the next non-holiday business day
     */
    private java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        while (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
            || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Counts the owners that already exist (before the current create) whose first and last names
     * both match the supplied names, compared case-insensitively. The returned value is captured on
     * the new owner at creation time and does not change as further owners are added later.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of pre-existing namesake owners
     */
    private int namesakeCount(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null
                && existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName() != null
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
    }

    /**
     * Builds the customer code for a new owner, formatted {@code '<REGION>-<HASH8>'} where
     * {@code REGION} is the canonical region derived from the owner's postcode (falling back to
     * the city when the postcode is absent or in no known range, see
     * {@link org.springframework.samples.petclinic.mapper.Localities#forCityAndPostcode}), and
     * {@code HASH8} is the first 8 upper-case hex characters of the SHA-256 digest of the
     * normalized telephone concatenated with the last name (e.g. {@code 'NSW-1A2B3C4D'}). Unlike
     * the previous city-prefixed scheme it carries no per-city sequence number, so the identity is
     * a pure function of the owner's region, telephone and last name.
     *
     * @param city                the owner's city
     * @param postcode            the owner's postcode, may be {@code null}
     * @param normalizedTelephone the owner's telephone, already normalized to E.164
     * @param lastName            the owner's last name
     * @return the assigned customer code
     */
    private String customerCode(String city, String postcode, String normalizedTelephone, String lastName) {
        String region = org.springframework.samples.petclinic.mapper.Localities
            .forCityAndPostcode(city, postcode);
        String hash8 = sha256UpperHex((normalizedTelephone == null ? "" : normalizedTelephone)
            + (lastName == null ? "" : lastName), 8);
        return region + "-" + hash8;
    }

    /**
     * Ensures the computed {@code customerCode} is unique across existing owners. When {@code baseCode}
     * does not collide with any existing owner's {@code customerCode} it is returned unchanged.
     * Otherwise {@code '-<n>'} is appended, using the smallest {@code n} of 2 or more that yields a
     * value not already in use, and that de-duplicated code is returned.
     *
     * @param baseCode the freshly computed customer code
     * @return {@code baseCode} if unused, otherwise {@code baseCode + "-" + n} for the smallest free n
     */
    private String deduplicateCustomerCode(String baseCode) {
        java.util.Set<String> inUse = this.clinicService.findAllOwners().stream()
            .map(org.springframework.samples.petclinic.model.Owner::getCustomerCode)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        if (!inUse.contains(baseCode)) {
            return baseCode;
        }
        int n = 2;
        while (inUse.contains(baseCode + "-" + n)) {
            n++;
        }
        return baseCode + "-" + n;
    }

    /**
     * Returns the first {@code length} upper-case hex characters of the SHA-256 digest of the
     * UTF-8 bytes of {@code value}.
     *
     * @param value  the source value to hash
     * @param length the number of leading hex characters to return
     * @return the leading upper-case hex characters of the digest
     */
    private String sha256UpperHex(String value, int length) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase(java.util.Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a street address applied whenever an owner is created: leading and trailing
     * whitespace is trimmed, any internal run of whitespace is collapsed to a single space, the
     * value is upper-cased, and common street-type abbreviations are expanded to their full form
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The expansion is applied
     * per whitespace-delimited token, so only standalone abbreviations are expanded. A {@code null}
     * value normalizes to the empty string. So {@code '  12  main  st '} becomes
     * {@code '12 MAIN STREET'}.
     *
     * @param address the raw address as submitted, or {@code null} when absent
     * @return the normalized address, or the empty string when the value is blank
     */
    /**
     * Resolves and normalizes an owner's postal address, accepting either the structured form
     * ({@code addressLine1} plus an optional {@code addressLine2}) or the flat {@code address}
     * input for backward compatibility. The structured form is preferred: when a non-blank
     * {@code addressLine1} is supplied, both structured lines are normalized (see
     * {@link #normalizeAddress(String)}) and the stored {@code address} is composed from them —
     * the normalized {@code addressLine1}, with a single space and the normalized
     * {@code addressLine2} appended when an {@code addressLine2} is present. Otherwise the flat
     * {@code address} input is normalized and stored, and the structured lines are cleared.
     *
     * <p>An owner is valid only when it supplies an address in one of these forms; a request with
     * neither a non-blank {@code addressLine1} nor a non-blank flat {@code address} is rejected.
     *
     * @param owner the owner whose address fields are resolved and normalized in place
     * @throws ResponseStatusException with a 400 status when no address is supplied in either form
     */
    private void applyAddress(Owner owner) {
        String line1 = normalizeAddress(owner.getAddressLine1());
        String line2 = normalizeAddress(owner.getAddressLine2());
        String flat = normalizeAddress(owner.getAddress());
        String composed;
        if (!line1.isEmpty()) {
            composed = line2.isEmpty() ? line1 : line1 + " " + line2;
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2.isEmpty() ? null : line2);
        } else {
            composed = flat;
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
        }
        if (composed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Address must not be blank after normalization");
        }
        owner.setAddress(composed);
    }

    private String normalizeAddress(String address) {
        String collapsed = (address == null ? "" : address)
            .trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "ST" -> tokens[i] = "STREET";
                case "RD" -> tokens[i] = "ROAD";
                case "AVE" -> tokens[i] = "AVENUE";
                default -> { }
            }
        }
        return String.join(" ", tokens);
    }

    /**
     * Normalizes a value for household-duplicate comparison: leading and trailing whitespace is
     * trimmed, any internal run of whitespace is collapsed to a single space, and the result is
     * lower-cased so the comparison is case-insensitive. A {@code null} value normalizes to the
     * empty string.
     *
     * @param value the raw value (last name or address) as submitted
     * @return the normalized comparison key
     */
    private String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Derives the stable shared household identifier for a household, keyed on the normalized last
     * name and address. Because it is a pure function of those normalized keys, every owner in the
     * same household deterministically computes the same value. The identifier is the first 12
     * upper-case hex characters of the SHA-256 digest of the two keys joined with a NUL separator
     * (the separator prevents distinct name/address pairs from colliding).
     *
     * @param lastNameKey the normalized last-name comparison key
     * @param addressKey  the normalized address comparison key
     * @return the stable household identifier
     */
    private String householdId(String lastNameKey, String postcode) {
        String seed = lastNameKey + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase(java.util.Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * The exact number of national (subscriber) digits required for a given country calling code,
     * i.e. the digits that follow the country code in an E.164 number. A country code that is not
     * listed here has no fixed-length requirement beyond the general 8-to-15-digit E.164 bound.
     * Australia ({@code '+61'}) requires 9 national digits and the NANP ({@code '+1'}) requires 10.
     */
    private static final java.util.Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        java.util.Map.of("61", 9, "1", 10);

    /**
     * Normalizes a telephone number to E.164 form. Spaces, dashes and brackets (indeed any
     * non-digit character) are stripped. If the submitted value carries a leading {@code '+'}
     * its country code is kept as-is; otherwise the default country code {@code '+61'} is
     * assumed and a single leading {@code '0'} is dropped from the national digits. The
     * resulting value must be a {@code '+'} followed by 8 to 15 digits. So {@code '0412 345 678'}
     * is stored as {@code '+61412345678'}.
     *
     * <p>In addition, when the number's country code has a fixed national-number length (see
     * {@link #NATIONAL_NUMBER_LENGTHS}), the national digits that follow the country code must
     * match that length exactly: {@code '+61'} requires 9 national digits and {@code '+1'}
     * requires 10. So {@code '+61 123'} is rejected because its 3 national digits are not 9.
     *
     * @param telephone the raw telephone number as submitted
     * @return the normalized E.164 telephone number
     * @throws ResponseStatusException with a 400 status if the value cannot form a valid E.164
     *         number (8 to 15 digits after the '+'), or if its national-number length is wrong
     *         for its country code
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.trim();
        String digits = raw.replaceAll("\\D", "");
        String e164;
        if (raw.startsWith("+")) {
            e164 = "+" + digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            e164 = "+61" + digits;
        }
        int digitCount = e164.length() - 1;
        if (digitCount < 8 || digitCount > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        validateNationalNumberLength(e164);
        return e164;
    }

    /**
     * Validates that an E.164 number's national-number length is correct for its country code.
     * The country code is matched by longest known prefix (so {@code '+61'} is preferred over
     * {@code '+1'} would-be matches); when the code has a fixed national-number length the digits
     * following it must match exactly. Country codes without a fixed length are left unchecked.
     *
     * @param e164 the normalized E.164 number (a {@code '+'} followed by digits)
     * @throws ResponseStatusException with a 400 status if the national-number length is wrong
     *         for the country code
     */
    private void validateNationalNumberLength(String e164) {
        String allDigits = e164.substring(1);
        String bestCode = null;
        for (String code : NATIONAL_NUMBER_LENGTHS.keySet()) {
            if (allDigits.startsWith(code)
                    && (bestCode == null || code.length() > bestCode.length())) {
                bestCode = code;
            }
        }
        if (bestCode == null) {
            return;
        }
        int nationalLength = allDigits.length() - bestCode.length();
        int required = NATIONAL_NUMBER_LENGTHS.get(bestCode);
        if (nationalLength != required) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Telephone for country code '+" + bestCode + "' must have " + required
                    + " national digits");
        }
    }

    /**
     * The inclusive 4-digit postcode range permitted for each canonical region, keyed by the
     * region derived from the owner's city ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
     * {@code Brisbane -> QLD}): NSW {@code 2000-2099}, VIC {@code 3000-3099}, QLD
     * {@code 4000-4099}. A city whose region is not listed here accepts any 4-digit postcode.
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODE_RANGES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's optional postcode against their city's region. The postcode is only
     * checked when present (a {@code null} postcode is accepted, keeping the request contract
     * backward-compatible). Its four-digit shape is already enforced by Bean Validation on the
     * request DTO. When the city maps to a known region (see {@link #REGION_POSTCODE_RANGES}) the
     * postcode must fall within that region's inclusive range; a city with no known region accepts
     * any 4-digit postcode.
     *
     * @param postcode the owner's postcode as submitted, or {@code null} when absent
     * @param city     the owner's city, used to derive the region whose range applies
     * @throws ResponseStatusException with a 400 status when a supplied postcode is out of range
     *         for the city's region
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        String region = org.springframework.samples.petclinic.mapper.Localities.forCity(city);
        int[] range = REGION_POSTCODE_RANGES.get(region);
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Postcode " + postcode + " is not valid for region " + region);
        }
    }

    /**
     * Normalizes an optional email address by lower-casing it. Syntactic validity is enforced
     * by Bean Validation on the request DTO, so a value reaching this point is either {@code null}
     * (absent) or already valid.
     *
     * @param email the email address as submitted, or {@code null} when absent
     * @return the lower-cased email, or {@code null} when absent
     */
    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * The set of disposable email domains an owner's email may not use. These throwaway-mailbox
     * providers are rejected because they cannot be relied on for durable contact.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS = java.util.Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Rejects an owner whose email domain is on the disposable-domain blocklist. The email has
     * already been normalized to lower-case, so the domain (the part after the last {@code '@'}) is
     * compared case-insensitively against {@link #DISPOSABLE_EMAIL_DOMAINS}. A {@code null} email
     * (absent) is accepted, keeping the field optional.
     *
     * @param email the normalized email address, or {@code null} when absent
     * @throws ResponseStatusException with a 400 status when the email domain is blocklisted
     */
    private void validateEmailDomain(String email) {
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Email domain " + domain + " is not allowed");
        }
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
        // Soft delete: flag the owner deleted and retain the row rather than removing it.
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
