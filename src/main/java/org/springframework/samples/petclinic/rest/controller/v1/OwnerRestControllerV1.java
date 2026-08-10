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
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.Soundex;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /** Dedicated audit logger for owner lifecycle side-effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Dedicated notification logger for owner welcome notifications. */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /** Serializes {@link OwnerCreatedEvent} instances to their JSON audit representation. */
    private static final ObjectMapper AUDIT_MAPPER = JsonMapper.builder().build();

    /** Monotonically increasing sequence stamped on every structured owner-created event. */
    private static final AtomicLong AUDIT_SEQ = new AtomicLong();

    /**
     * Immutable structured audit event emitted once per owner create. The {@code memberId}
     * field carries the owner's <em>current primary identifier</em> as returned by
     * {@link #primaryIdentifier(Owner)} (the unified memberId).
     */
    private record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
            Integer membershipLevel, String event) {
    }

    /**
     * Pragmatic check for a syntactically valid email address: a non-empty local
     * part, a single '@', and a domain with at least one dot-separated label.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");

    /**
     * Disposable email domains that are rejected: an owner email whose domain is one of
     * these is treated as invalid so the create/update is rejected with 400. Compared
     * case-insensitively against the normalized (lower-cased) email domain.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Common street-type abbreviations expanded during address normalization,
     * keyed by their upper-cased form.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Required national-number length keyed by E.164 country calling code: a
     * {@code '+61'} (Australia) number carries 9 national digits and a {@code '+1'}
     * (NANP) number carries 10. A number whose country code is listed here must have
     * exactly the mandated number of national digits.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        Map.of("61", 9, "1", 10);

    /**
     * Inclusive 4-digit postcode range {@code {low, high}} keyed by the region derived
     * from the owner's city (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city whose
     * region is not listed here (see {@link OwnerMapper#CITY_REGION}) accepts any 4-digit
     * postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** HTTP header carrying the client-supplied idempotency key for owner creation. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers the owner created for each seen {@code Idempotency-Key}, so a create that
     * repeats with an already-seen key returns the originally created owner (200) instead of
     * creating a duplicate. Kept in-memory and keyed by the raw header value.
     */
    private final Map<String, Integer> idempotentCreates = new ConcurrentHashMap<>();

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
        // Idempotent create: when the request carries an 'Idempotency-Key' already seen on a
        // previous successful create, return the originally created owner with 200 instead of
        // creating a duplicate.
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        // Validate & normalize the optional email; reject the create when it is present but invalid.
        if (!normalizeEmail(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Validate & normalize the optional postcode; reject the create when it is present but
        // not valid for the owner's city (see normalizePostcode).
        if (!normalizePostcode(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Normalize the supplied address — the structured 'addressLine1'/'addressLine2' when
        // present, otherwise the flat 'address' — storing the normalized form (and the composed
        // 'address') back on the DTO so it is what gets persisted and compared. Reject the create
        // when no address is supplied in either form (see normalizeAddresses).
        if (!normalizeAddresses(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Reject the create when the owner's city is already at capacity, i.e. it already
        // contains 50 or more owners (compared case-insensitively).
        int ownersInCity = countOwnersInCity(ownerFieldsDto.getCity());
        if (ownersInCity >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Normalize the telephone to E.164; reject the create when it cannot form a valid number.
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ownerFieldsDto.setTelephone(normalizedTelephone);
        // Consolidated duplicate detection. All duplicate checks are now expressed through the
        // single derived identity key: the SHA-256 hex of
        // '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>' (see OwnerMapper#identityKey).
        // A create is rejected with 409 only when the new owner's identity key collides with an
        // existing (non-deleted) owner's. Because the telephone is part of the key, two owners with
        // the same last name and postcode but different telephones no longer collide — they are
        // created (and may be flagged as a soft match below), not rejected. The former separate
        // household-duplicate 409 keyed on the computed householdId no longer applies.
        Owner candidate = ownerMapper.toOwner(ownerFieldsDto);
        String identityKey = ownerMapper.identityKey(candidate);
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(ownerMapper.identityKey(existing)));
        if (identityInUse) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Determine the effective registration date — the value supplied on the request, or the
        // server's current date when none was supplied — and roll it forward to the next business
        // day: a Saturday or Sunday moves to the following Monday. Everything derived from the
        // registration date (membership-number year segment, per-day create-limit) uses this
        // adjusted date.
        LocalDate registrationDate = ownerFieldsDto.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
        // Reject a supplied registration date that lies in the future (later than the
        // server's current date): an owner cannot be registered ahead of time.
        else if (registrationDate.isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        registrationDate = toBusinessDay(registrationDate);
        // Reject the create when the daily sign-up limit has been reached, i.e. 100 or more
        // owners have already been registered on this (adjusted) business day.
        if (countOwnersRegisteredOn(registrationDate) >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = candidate;
        owner.setRegistrationDate(registrationDate);
        // Assign the unified memberId as '<REGION><FY><HASH8><CHK>': the region derived from the
        // owner's postcode (falling back to the city, then 'UNKNOWN'), the two-digit fiscal year of
        // the registration date, the first 8 upper-case hex characters of the SHA-256 of the
        // normalized telephone concatenated with the last name, and a single Luhn check digit over
        // the digits of '<REGION><FY><HASH8>'.
        owner.setMemberId(memberId(owner));
        // Record how many existing owners already share this owner's first and last name
        // (compared case-insensitively) at the moment before this owner is created.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Flag a bulk sign-up when more than 80 owners have already been created today, i.e.
        // registered on this (adjusted) business day before this owner is created.
        owner.setBulkSignupWarning(countOwnersRegisteredOn(registrationDate) > 80);
        // Flag that the owner's city is approaching its capacity limit: it already held between
        // 40 and 49 owners (inclusive) before this owner was created. The hard rejection at 50
        // owners is handled above, so reaching this point means the count is below 50.
        owner.setCapacityWarning(ownersInCity >= 40 && ownersInCity < 50);
        // Record the size of this owner's household after this create: the number of existing
        // owners sharing the same household (i.e. the same computed householdId) plus this owner.
        owner.setHouseholdSize(countHouseholdMembers(owner) + 1);
        // Cap this owner's membership level at one above the current maximum membership level among
        // their existing household members. When the owner has no existing household member no cap
        // applies (a null cap; see OwnerMapper#membershipLevel).
        owner.setMembershipLevelCap(membershipLevelCap(owner));
        // Flag a possible (soft) duplicate: this owner is not a hard identity duplicate (its
        // identity key is distinct), but it shares an existing owner's Soundex last name and
        // postcode. When such a match exists, record it as 'possibleDuplicate' with
        // 'possibleDuplicateOf' set to the matching owner's id; otherwise it is not a possible
        // duplicate. A declared household member (the request sets 'sharesHousehold') is an
        // acknowledged member, not a suspected duplicate, so it is never flagged.
        Owner softMatch = findPossibleDuplicate(owner);
        boolean declaredMember = softMatch != null && Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (declaredMember) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
        else {
            owner.setPossibleDuplicate(softMatch != null);
            owner.setPossibleDuplicateOf(softMatch == null ? null : softMatch.getId());
        }
        this.clinicService.saveOwner(owner);
        // Emit an audit line recording the new owner's id, memberId, registration date and
        // membership level.
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            ownerMapper.membershipLevel(owner));
        // Emit the immutable structured OWNER_CREATED event carrying a monotonically increasing
        // sequence and the owner's current primary identifier (the memberId).
        OwnerCreatedEvent event = new OwnerCreatedEvent(AUDIT_SEQ.incrementAndGet(), owner.getId(),
            primaryIdentifier(owner), ownerMapper.membershipLevel(owner), "OWNER_CREATED");
        AUDIT.info(AUDIT_MAPPER.writeValueAsString(event));
        // Enqueue a welcome notification for the newly created owner, carrying its id and memberId.
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
        // Remember this create against its idempotency key so a later repeat returns the same
        // owner instead of creating a duplicate.
        if (idempotencyKey != null) {
            idempotentCreates.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * The non-blank {@code Idempotency-Key} header of the current request, or {@code null} when
     * absent, blank, or there is no bound request.
     */
    private static String currentIdempotencyKey() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttributes)) {
            return null;
        }
        String key = servletAttributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        return (key == null || key.isBlank()) ? null : key;
    }

    /**
     * Validate and normalize the optional owner email in place.
     *
     * <p>An absent (or blank) email is allowed and is normalized to {@code null}.
     * When present it must be a syntactically valid address; a valid address is
     * stored back on the DTO lower-cased. Returns {@code false} when an email is
     * present but syntactically invalid, so the caller can reject with 400.
     */
    private boolean normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null || email.isBlank()) {
            ownerFieldsDto.setEmail(null);
            return true;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        // Reject addresses whose domain is on the disposable-domain blocklist.
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            return false;
        }
        ownerFieldsDto.setEmail(normalized);
        return true;
    }

    /**
     * Validate the optional owner postcode in place.
     *
     * <p>An absent (or blank) postcode is allowed and is normalized to {@code null}.
     * When present it must be exactly 4 digits, and — when the owner's city maps to a
     * known region via {@link OwnerMapper#CITY_REGION} — it must fall within that
     * region's inclusive postcode range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     * A city with no known region accepts any 4-digit postcode. Returns {@code false}
     * when a postcode is present but malformed or out of range, so the caller can
     * reject with 400.
     */
    private boolean normalizePostcode(OwnerFieldsDto ownerFieldsDto) {
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            ownerFieldsDto.setPostcode(null);
            return true;
        }
        if (!postcode.matches("\\d{4}")) {
            return false;
        }
        String region = OwnerMapper.CITY_REGION.get(ownerFieldsDto.getCity());
        int[] range = region == null ? null : REGION_POSTCODE_RANGES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate and normalize the owner's address fields in place, preferring the structured
     * form over the flat 'address' for backward compatibility.
     *
     * <p>When a non-blank {@code addressLine1} is supplied it is the structured form: both
     * {@code addressLine1} and (when present) {@code addressLine2} are normalized and stored
     * back, and {@code address} is set to the composed value — the normalized addressLine1,
     * with a single space and the normalized addressLine2 appended when addressLine2 is present.
     * Otherwise the flat {@code address} is normalized in place and the structured lines are
     * cleared. Returns {@code false} when no address is supplied in either form (both the
     * structured addressLine1 and the flat address are blank once normalized), so the caller
     * can reject with 400.
     */
    private boolean normalizeAddresses(OwnerFieldsDto ownerFieldsDto) {
        String rawLine1 = ownerFieldsDto.getAddressLine1();
        if (rawLine1 != null && !rawLine1.isBlank()) {
            String line1 = normalizeAddress(rawLine1);
            if (line1.isBlank()) {
                return false;
            }
            ownerFieldsDto.setAddressLine1(line1);
            String rawLine2 = ownerFieldsDto.getAddressLine2();
            String composed = line1;
            if (rawLine2 != null && !rawLine2.isBlank()) {
                String line2 = normalizeAddress(rawLine2);
                ownerFieldsDto.setAddressLine2(line2);
                composed = line1 + " " + line2;
            }
            else {
                ownerFieldsDto.setAddressLine2(null);
            }
            ownerFieldsDto.setAddress(composed);
            return true;
        }
        // No structured address: fall back to the flat 'address' input.
        String canonicalAddress = normalizeAddress(ownerFieldsDto.getAddress());
        if (canonicalAddress.isBlank()) {
            return false;
        }
        ownerFieldsDto.setAddress(canonicalAddress);
        ownerFieldsDto.setAddressLine1(null);
        ownerFieldsDto.setAddressLine2(null);
        return true;
    }

    /**
     * Normalize an owner address: trim, collapse each run of whitespace to a single
     * space, upper-case, and expand common street-type abbreviations
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
     * A {@code null} value normalizes to the empty string.
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
     * Normalize a telephone number to E.164 form.
     *
     * <p>Spaces, dashes and brackets are stripped. When the number carries an
     * explicit leading {@code '+'} and country code it is kept; otherwise the
     * country code {@code '+61'} is assumed and a single leading {@code '0'} is
     * dropped from the national digits. The result must contain 8 to 15 digits
     * after the {@code '+'}, and — for a recognised country calling code — exactly
     * the national-number length that code mandates ({@code '+61'} => 9 national
     * digits, {@code '+1'} => 10).
     *
     * @return the E.164 string (e.g. {@code +61412345678}), or {@code null} when
     *         the input is absent or cannot form a valid E.164 number.
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        // Strip spaces, dashes and brackets.
        String stripped = trimmed.replaceAll("[\\s()\\-]", "");
        String digits;
        if (hasCountryCode) {
            // Keep the explicit country code; drop the leading '+' for validation.
            digits = stripped.substring(1);
        } else {
            // No country code: assume '+61' and drop a single leading '0'.
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        // For a recognised country calling code, enforce the exact national-number length it
        // mandates (e.g. '+61' => 9 national digits, '+1' => 10). Longer codes win so a '+61'
        // number is not mistaken for a '+6...' one. Unknown codes keep the generic 8-15 rule.
        for (String countryCode : NATIONAL_NUMBER_LENGTHS.keySet().stream()
                .sorted((a, b) -> b.length() - a.length()).toList()) {
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != NATIONAL_NUMBER_LENGTHS.get(countryCode)) {
                    return null;
                }
                break;
            }
        }
        return "+" + digits;
    }

    /**
     * Count the existing owners whose first and last name match the given names,
     * compared case-insensitively. Used to record an owner's namesake count at the
     * moment before it is created.
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null
                && existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName() != null
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
    }

    /**
     * Count the existing owners in the same household as the given owner, i.e. those sharing its
     * computed householdId (derived from the normalized last name and postcode). Used, together
     * with the owner being created, to derive the household size recorded on the owner.
     */
    private int countHouseholdMembers(Owner owner) {
        String householdId = ownerMapper.householdId(owner);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(ownerMapper.householdId(existing)))
            .count();
    }

    /**
     * Compute the membership-level cap for the owner being created: one above the current maximum
     * effective membership level among its existing (non-deleted) household members, i.e. those
     * sharing its computed householdId. Each member's effective level is read through
     * {@link OwnerMapper#membershipLevel(Owner)}, so it already reflects any cap applied to that
     * member. Returns {@code null} when the owner has no existing household member, in which case
     * no cap applies.
     */
    private Integer membershipLevelCap(Owner owner) {
        String householdId = ownerMapper.householdId(owner);
        java.util.OptionalInt maxLevel = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> existing != owner)
            .filter(existing -> householdId.equals(ownerMapper.householdId(existing)))
            .mapToInt(existing -> ownerMapper.membershipLevel(existing))
            .max();
        return maxLevel.isPresent() ? maxLevel.getAsInt() + 1 : null;
    }

    /**
     * Find an existing owner that the given (not-yet-created) owner is a possible (soft) duplicate
     * of: one whose identity key differs (so it is not a hard duplicate) yet whose last name shares
     * the same Soundex code and whose postcode is the same. Owners without a postcode never match
     * (the postcode must be shared) and deleted owners are ignored. When several existing owners
     * match, the one with the lowest id is returned so the result is deterministic. Returns
     * {@code null} when there is no such match.
     */
    private Owner findPossibleDuplicate(Owner candidate) {
        if (candidate.getPostcode() == null) {
            return null;
        }
        String candidateKey = ownerMapper.identityKey(candidate);
        String candidateSoundex = Soundex.of(candidate.getLastName());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> !candidateKey.equals(ownerMapper.identityKey(existing)))
            .filter(existing -> candidate.getPostcode().equals(existing.getPostcode())
                && candidateSoundex.equals(Soundex.of(existing.getLastName())))
            .min(java.util.Comparator.comparing(Owner::getId))
            .orElse(null);
    }

    /**
     * The owner's current primary identifier, as carried by structured audit events: the
     * unified memberId.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Build the owner's unified memberId, formatted {@code '<REGION><FY><HASH8><CHK>'}.
     *
     * <p>{@code REGION} is the region derived from the owner's postcode (see
     * {@link #deriveRegion}). {@code FY} is the two-digit fiscal year (starting 1 July) of the
     * business-day-adjusted registration date. {@code HASH8} is the first 8 upper-case hex
     * characters of the SHA-256 of the normalized telephone concatenated with the last name.
     * {@code CHK} is a single Luhn check digit computed over the digits of
     * {@code '<REGION><FY><HASH8>'} (e.g. {@code 'NSW271A2B3C4D5'}).
     *
     * <p>When the computed memberId collides with an existing owner's memberId, it is
     * de-duplicated by appending {@code '-<n>'} with the smallest {@code n} of 2 or more
     * that makes it unique.
     */
    private String memberId(Owner owner) {
        String region = deriveRegion(owner.getPostcode(), owner.getCity());
        String fy = String.format("%02d", fiscalYearTwoDigits(owner.getRegistrationDate()));
        String hash8 = sha256HexUpper(owner.getTelephone() + owner.getLastName(), 8);
        String base = region + fy + hash8;
        String memberId = base + luhn(base);
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
        if (!existing.contains(memberId)) {
            return memberId;
        }
        int n = 2;
        while (existing.contains(memberId + "-" + n)) {
            n++;
        }
        return memberId + "-" + n;
    }

    /**
     * The two-digit fiscal year (starting 1 July) of the given date: the last two digits of the
     * calendar year in which the fiscal year ends. Dates on or after 1 July belong to the fiscal
     * year ending the following calendar year (e.g. 2026-08-10 -&gt; 27); earlier dates belong to
     * the fiscal year ending in the same calendar year (e.g. 2026-03-01 -&gt; 26).
     */
    private static int fiscalYearTwoDigits(LocalDate date) {
        int endYear = date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
        return endYear % 100;
    }

    /**
     * A single Luhn check digit (0-9) computed over the digit characters contained in
     * {@code value}; non-digit characters are ignored.
     */
    private static int luhn(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Derive the owner's region, preferring the postcode. The postcode is matched against
     * each region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); only when
     * it is absent or in no known range does this fall back to the fixed city-to-region table
     * (see {@link OwnerMapper#CITY_REGION}), and finally {@code 'UNKNOWN'}.
     */
    private static String deriveRegion(String postcode, String city) {
        if (postcode != null && postcode.matches("\\d{4}")) {
            int value = Integer.parseInt(postcode);
            for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        return OwnerMapper.CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The first {@code length} upper-case hex characters of the SHA-256 digest of the UTF-8
     * bytes of {@code value}.
     */
    private static String sha256HexUpper(String value, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, length);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Count the existing owners located in the given city, compared case-insensitively.
     * Used to enforce the per-city capacity limit and capacity warning.
     */
    private int countOwnersInCity(String city) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null
                && existing.getCity().equalsIgnoreCase(city))
            .count();
    }

    /**
     * Count the existing owners whose registration date is the given (adjusted business) date.
     * Used to enforce the per-day sign-up limit.
     */
    private int countOwnersRegisteredOn(LocalDate date) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Roll a registration date forward onto a business day: a Saturday, Sunday, or listed
     * public holiday is moved forward to the next non-holiday weekday; a plain weekday is
     * returned unchanged.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY
            || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
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
        if (!normalizeEmail(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!normalizePostcode(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!normalizeAddresses(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setAddressLine1(ownerFieldsDto.getAddressLine1());
        currentOwner.setAddressLine2(ownerFieldsDto.getAddressLine2());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(normalizedTelephone);
        currentOwner.setEmail(ownerFieldsDto.getEmail());
        currentOwner.setPostcode(ownerFieldsDto.getPostcode());
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
        // Soft-delete: flag the owner deleted and retain the record so it can still be
        // read back (with 'deleted' true) and is ignored by later duplicate/identity checks.
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
