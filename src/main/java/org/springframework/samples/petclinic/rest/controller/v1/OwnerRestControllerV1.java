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
import org.springframework.samples.petclinic.rest.advice.CityCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidRequestException;
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
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Syntactic email validation: a non-empty local part and a dotted domain, with no whitespace.
     * Matches addresses such as {@code test.user@example.com} and rejects strings without an '@'
     * and a dotted domain (e.g. {@code not-an-email}).
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$");

    /**
     * Disposable email domains that are not allowed for an owner. An email whose domain (the part
     * after the '@', compared case-insensitively) is listed here is rejected with 400 Bad Request.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS = java.util.Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /** Default country code assumed for national numbers that carry no explicit '+' prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Maximum number of owners a single city may contain; a create is rejected once it is reached. */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /** Maximum number of owners that may be registered on a single day; a create is rejected once it is reached. */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /** Once more than this many owners have already been created on a day, a create carries a bulk-signup warning. */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /** Fixed public-holiday calendar: a registration date landing on any of these is rolled forward past it. */
    private static final java.util.Set<LocalDate> PUBLIC_HOLIDAYS = java.util.Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /** Separators (spaces, dashes and brackets) stripped from a telephone before parsing. */
    private static final Pattern TELEPHONE_SEPARATORS = Pattern.compile("[\\s()\\[\\]-]");

    /** A valid E.164 body: a leading '+' followed by 8 to 15 digits. */
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[0-9]{8,15}$");

    /**
     * Country calling codes with a fixed national-number length. An E.164 number whose country code
     * is listed here must carry exactly the required number of national digits, otherwise it is
     * rejected ({@code +61} requires 9 national digits, {@code +1} requires 10). Ordered so that a
     * longer prefix is matched before a shorter one (e.g. {@code 61} before {@code 1}).
     */
    private static final Map<String, Integer> COUNTRY_NATIONAL_LENGTH = Map.of(
        "61", 9,
        "1", 10);

    /** Common street-type abbreviations expanded (on upper-cased tokens) during address normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /** A well-formed postcode: exactly four digits. */
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("^[0-9]{4}$");

    /** City to canonical region for the fixed city-to-region table (cities not listed have no region). */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region to its inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Dedicated audit logger; a create side-effect is recorded here on success. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Request header carrying the client-supplied idempotency key for a create. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers the id of the owner created for each already-seen idempotency key, so a create that
     * repeats with a key already seen returns the originally created owner (with 200) instead of
     * creating a duplicate.
     */
    private final Map<String, Integer> idempotencyKeyToOwnerId = new java.util.concurrent.ConcurrentHashMap<>();

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
        owners.forEach(this::populateHouseholdSize);
        List<OwnerDto> ownerDtos = new ArrayList<>();
        for (Owner owner : owners) {
            OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
            applyHouseholdMembershipCap(owner, ownerDto);
            ownerDtos.add(ownerDto);
        }
        return new ResponseEntity<>(ownerDtos, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        populateHouseholdSize(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        applyHouseholdMembershipCap(owner, ownerDto);
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        // Idempotent create: when the request carries an 'Idempotency-Key' already seen, return the
        // originally created owner with 200 instead of creating (and duplicate-checking) a new one.
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotencyKeyToOwnerId.get(idempotencyKey);
            if (existingId != null) {
                Owner existingOwner = this.clinicService.findOwnerById(existingId);
                if (existingOwner != null) {
                    populateHouseholdSize(existingOwner);
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existingOwner), HttpStatus.OK);
                }
            }
        }
        List<String> missingFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        // Normalize each supplied address form up front. The structured fields are preferred when
        // present; the flat 'address' input remains accepted for backward compatibility. An owner is
        // valid when it supplies an address in EITHER form (a non-blank addressLine1, or the flat
        // address). A whitespace-only value collapses to the empty string and counts as absent.
        String normalizedAddressLine1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
        String normalizedAddressLine2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
        String normalizedFlatAddress = normalizeAddress(ownerFieldsDto.getAddress());
        boolean hasStructuredAddress = StringUtils.hasText(normalizedAddressLine1);
        boolean hasFlatAddress = StringUtils.hasText(normalizedFlatAddress);
        if (!hasStructuredAddress && !hasFlatAddress) {
            missingFields.add("address");
        }
        // Compose the stored/returned address: the structured lines when present (addressLine1, with a
        // single space and addressLine2 appended when addressLine2 is present), otherwise the flat form.
        String composedAddress;
        if (hasStructuredAddress) {
            composedAddress = StringUtils.hasText(normalizedAddressLine2)
                ? normalizedAddressLine1 + " " + normalizedAddressLine2
                : normalizedAddressLine1;
        } else {
            composedAddress = normalizedFlatAddress;
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new InvalidRequestException(missingFields);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        // A newly created owner is never soft-deleted.
        owner.setDeleted(false);
        // Store (and later return) the normalized structured lines and the composed address. The
        // structured lines are kept only when the structured form was supplied; otherwise they are
        // cleared so the response reflects the flat form that was actually used.
        owner.setAddressLine1(hasStructuredAddress ? normalizedAddressLine1 : null);
        owner.setAddressLine2(hasStructuredAddress && StringUtils.hasText(normalizedAddressLine2)
            ? normalizedAddressLine2 : null);
        owner.setAddress(composedAddress);
        // Normalize the telephone into E.164 form (reject with 400 when it cannot form a valid one).
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        // Normalize the (optional) email: reject a syntactically invalid address, otherwise store it lower-cased.
        owner.setEmail(normalizeEmail(owner.getEmail()));
        // Validate the (optional) postcode: reject with 400 when present but malformed or out of range
        // for the city's region. The value (mapped straight from the request) is stored and returned as given.
        validatePostcode(owner.getCity(), owner.getPostcode());
        // Reject a supplied registration date that lies in the future (later than the server's current
        // date) with a 400 Bad Request. Checked against the raw supplied value, before defaulting below.
        if (owner.getRegistrationDate() != null && owner.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new InvalidRequestException(List.of("registrationDate"));
        }
        // Default the (optional) registration date to the server's current date when none was supplied.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        // The effective registration date must fall on a business day: when it lands on a Saturday,
        // Sunday or a listed public holiday (whether supplied or defaulted above), roll it forward to
        // the next non-holiday business day. Every value derived from the registration date below
        // (daily create-limit, membership number) uses this adjusted date.
        owner.setRegistrationDate(rollToBusinessDay(owner.getRegistrationDate()));
        // Reject the request once the maximum number of owners for the owner's registration day has
        // been reached (100 or more owners already created on that date).
        int ownersRegisteredToday = countOwnersRegisteredOn(owner.getRegistrationDate());
        if (ownersRegisteredToday >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(owner.getRegistrationDate());
        }
        // Flag a bulk-signup warning when more than 80 owners had already been created on this
        // owner's registration day at the moment this owner was created; otherwise clear it.
        owner.setBulkSignupWarning(ownersRegisteredToday > BULK_SIGNUP_WARNING_THRESHOLD);
        // Reject the request when the owner's city has already reached its owner capacity.
        if (countOwnersInCity(owner.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityCapacityException(owner.getCity());
        }
        // The household is keyed deterministically on (normalizedLastName, postcode): the householdId
        // is the first 12 hex characters of the SHA-256 digest of '<normalizedLastName>|<postcode>',
        // so any two owners with the same last name and postcode automatically resolve to the same
        // household without an explicit opt-in. It is assigned up front so it feeds duplicate
        // detection (the identity key below), the household-size read-back and household-duplicate
        // detection alike.
        String householdId = generateHouseholdId(owner.getLastName(), owner.getPostcode());
        owner.setHouseholdId(householdId);
        // Household-duplicate detection: because the household is keyed on (lastName, postcode), any
        // existing owner already carrying this householdId is a member of the same household, so this
        // create looks like a household duplicate. Reject it with 409 unless it is a declared distinct
        // household member: either the caller opts in via 'sharesHousehold', or the new owner supplies
        // an email, a per-person identifier that marks it as a genuinely distinct member rather than a
        // mistyped duplicate of an existing one. Either way the household link already exists by
        // construction; an exact full-identity repeat is still caught by the identity-key check below.
        boolean householdMemberExists = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        boolean declaredHouseholdMember = false;
        if (householdMemberExists) {
            if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())
                || StringUtils.hasText(owner.getEmail())) {
                declaredHouseholdMember = true;
            } else {
                throw new DuplicateHouseholdException(owner.getLastName(), owner.getPostcode());
            }
        }
        // All duplicate detection is consolidated into this single derived identity key
        // ('<normalizedTelephone>|<email or empty>|<householdId or empty>'): reject with 409 only
        // when the new owner's WHOLE identity key exactly matches an existing owner's. Because the
        // telephone is part of the key, two members of the same household with different telephones
        // have different keys and are both allowed; only an exact full-key match is a duplicate.
        String identityKey = owner.getIdentityKey();
        boolean identityCollision = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (identityCollision) {
            throw new DuplicateIdentityException(identityKey);
        }
        // Soft-match detection: the new owner is not a hard (identity-key) duplicate, but it may
        // still be a likely duplicate of an existing owner. Flag it when an existing owner shares
        // this owner's last name (compared case-insensitively with collapsed whitespace) and
        // postcode while carrying a DIFFERENT normalized telephone. When several existing owners
        // match, the one with the lowest id is chosen so the result is deterministic. A declared
        // household member is an intentional co-resident, not a suspected duplicate, so it is never
        // flagged (a create that reaches this point sharing a last name and postcode with an
        // existing owner is precisely a declared household member).
        Owner softMatch = declaredHouseholdMember ? null : findPossibleDuplicate(owner);
        if (softMatch != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(softMatch.getId());
        } else {
            owner.setPossibleDuplicate(false);
        }
        // Record how many existing owners already share this first name and last name
        // (compared case-insensitively) before this owner is created.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Assign the customer code '<REGION>-<HASH8>' before persisting.
        owner.setCustomerCode(generateCustomerCode(owner));
        // Assign the membership number '<customerCode>-M<YY>' where YY is the last two
        // digits of the fiscal year the registration date falls in (e.g. 'NSW-1A2B3C4D-M27').
        owner.setMembershipNumber(generateMembershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        this.clinicService.saveOwner(owner);
        // Remember the created owner under the request's idempotency key so a later create repeating
        // the same key returns this owner instead of creating a duplicate.
        if (idempotencyKey != null) {
            idempotencyKeyToOwnerId.put(idempotencyKey, owner.getId());
        }
        populateHouseholdSize(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        // Cap the new owner's membership level at one above the current maximum among their
        // existing household members (no cap when the household has no other member).
        applyHouseholdMembershipCap(owner, ownerDto);
        // Emit an audit line carrying the new owner's id, customer code, registration date,
        // numeric membership level and membership number.
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel(), owner.getMembershipNumber());
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Return the current request's {@code Idempotency-Key} header value, or {@code null} when the
     * header is absent/blank or there is no bound servlet request.
     *
     * @return the trimmed idempotency key, or {@code null} when none was supplied
     */
    private String currentIdempotencyKey() {
        org.springframework.web.context.request.RequestAttributes attributes =
            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttributes) {
            String key = servletAttributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
            if (StringUtils.hasText(key)) {
                return key.trim();
            }
        }
        return null;
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

    /**
     * Populate the owner's household size: the number of owners sharing this owner's
     * {@code householdId} (including the owner itself). An owner with no household is a household
     * of one.
     *
     * @param owner the owner whose household size should be computed and set
     */
    private void populateHouseholdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (!StringUtils.hasText(householdId)) {
            owner.setHouseholdSize(1);
            return;
        }
        int size = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
        owner.setHouseholdSize(size);
    }

    /**
     * Cap the owner's membership level so it never exceeds one above the current maximum membership
     * level among the owner's household members (the other non-deleted owners sharing this owner's
     * {@code householdId}). When the household has no other member, no cap applies and the level is
     * left untouched. Any reduction is written back onto the given dto.
     *
     * @param owner    the owner whose household members bound the level (excluded from its own household)
     * @param ownerDto the mapped dto whose {@code membershipLevel} is capped in place
     */
    private void applyHouseholdMembershipCap(Owner owner, OwnerDto ownerDto) {
        String householdId = owner.getHouseholdId();
        if (!StringUtils.hasText(householdId) || ownerDto.getMembershipLevel() == null) {
            return;
        }
        Integer maxMemberLevel = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .filter(existing -> !java.util.Objects.equals(existing.getId(), owner.getId()))
            .map(this::membershipLevelOf)
            .max(Integer::compareTo)
            .orElse(null);
        if (maxMemberLevel != null && ownerDto.getMembershipLevel() > maxMemberLevel + 1) {
            ownerDto.setMembershipLevel(maxMemberLevel + 1);
        }
    }

    /**
     * Compute an existing household member's numeric membership level. The member's household size is
     * populated first so the level reflects the current household (the household-of-3 membership
     * points depend on it), then the mapper's derived {@code membershipLevel} is read back.
     *
     * @param member the household member to evaluate
     * @return the member's derived membership level
     */
    private int membershipLevelOf(Owner member) {
        populateHouseholdSize(member);
        return ownerMapper.toOwnerDto(member).getMembershipLevel();
    }

    /**
     * Generate a customer code formatted {@code <REGION>-<HASH8>}, where {@code REGION} is the region
     * code derived from the owner's identity (postcode range first, then the city-to-region table,
     * otherwise {@code UNKNOWN}) and {@code HASH8} is the first eight upper-case hex characters of the
     * SHA-256 digest of {@code normalizedTelephone + lastName} (e.g. {@code NSW-1A2B3C4D}). There is no
     * longer any per-city sequence number.
     *
     * @param owner the owner whose (already-normalized) telephone, last name, postcode and city are read
     * @return the generated (collision-free) customer code
     */
    private String generateCustomerCode(Owner owner) {
        String region = deriveRegion(owner.getPostcode(), owner.getCity());
        String hash8 = shortSha256Hex(owner.getTelephone() + owner.getLastName(), 8);
        String baseCode = region + "-" + hash8;
        return deduplicateCustomerCode(baseCode);
    }

    /**
     * Return a customer code that does not collide with any existing owner's customer code. When the
     * given base code is already unique it is returned unchanged; otherwise {@code -<n>} is appended
     * with the smallest {@code n} of 2 or more that makes the result unique (e.g. {@code NSW-1A2B3C4D},
     * then {@code NSW-1A2B3C4D-2}, {@code NSW-1A2B3C4D-3}, ...). The incoming owner is not yet
     * persisted, so it never counts as a collision with itself.
     *
     * @param baseCode the computed {@code <REGION>-<HASH8>} customer code
     * @return the de-duplicated customer code
     */
    private String deduplicateCustomerCode(String baseCode) {
        java.util.Set<String> existingCodes = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        if (!existingCodes.contains(baseCode)) {
            return baseCode;
        }
        int n = 2;
        String candidate = baseCode + "-" + n;
        while (existingCodes.contains(candidate)) {
            n++;
            candidate = baseCode + "-" + n;
        }
        return candidate;
    }

    /**
     * Derive the region code for an owner's identity. The postcode is preferred: when present and it
     * falls within a known region's inclusive 4-digit range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099) that region is returned. Otherwise fall back to the pinned city-to-region table
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), and finally {@code UNKNOWN} for a city with no
     * known region.
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @param city     the owner's city
     * @return the derived region code
     */
    private String deriveRegion(String postcode, String city) {
        if (postcode != null && POSTCODE_PATTERN.matcher(postcode).matches()) {
            int value = Integer.parseInt(postcode);
            for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGE.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        String region = CITY_REGION.get(city);
        return region != null ? region : "UNKNOWN";
    }

    /**
     * Return the first {@code length} upper-case hex characters of the SHA-256 digest of the UTF-8
     * bytes of {@code input}.
     *
     * @param input  the value to digest
     * @param length the number of leading hex characters to return
     * @return the truncated upper-case hex digest
     */
    private String shortSha256Hex(String input, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase(Locale.ROOT);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Roll a registration date forward onto a business day: a Saturday, Sunday or listed public
     * holiday is advanced day-by-day until it lands on the next non-holiday weekday, while a date
     * that is already a non-holiday weekday is returned unchanged. This is the effective registration
     * date that is stored, returned and used for every value derived from it.
     *
     * @param date the effective registration date (supplied or defaulted)
     * @return the same date when it is a non-holiday weekday, otherwise the next non-holiday business day
     */
    private LocalDate rollToBusinessDay(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * A business day is a weekday (Monday to Friday) that is not a listed public holiday.
     *
     * @param date the date to test
     * @return {@code true} when the date is a non-holiday weekday
     */
    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        return !PUBLIC_HOLIDAYS.contains(date);
    }

    /**
     * Generate a membership number formatted {@code <customerCode>-M<YY>}, where {@code YY} is the
     * last two digits of the fiscal year that the (business-day-adjusted) {@code registrationDate}
     * falls in (e.g. {@code SYD-SMI-0007-M27} for a registration in FY27).
     *
     * @param customerCode     the owner's already-assigned customer code
     * @param registrationDate the owner's business-day-adjusted registration date
     * @return the generated membership number
     */
    private String generateMembershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", fiscalYear(registrationDate) % 100);
        return String.format("%s-M%s", customerCode, yy);
    }

    /**
     * The fiscal year that the given date falls in. The fiscal year starts on 1 July, so a date in
     * July to December belongs to the fiscal year ending in the following calendar year, and a date
     * in January to June belongs to the fiscal year ending in the same calendar year. The fiscal
     * year is identified by that ending calendar year (e.g. 1 July 2026 to 30 June 2027 is 2027).
     *
     * @param date the date whose fiscal year is required
     * @return the fiscal year's ending calendar year
     */
    private int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Count the existing owners that share the given first name and last name, comparing each
     * case-insensitively (trimmed, with internal whitespace collapsed). The incoming owner is not
     * yet persisted, so it is not included in the count.
     *
     * @param firstName the incoming owner's first name
     * @param lastName  the incoming owner's last name
     * @return the number of existing namesakes
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeForComparison(firstName);
        String normalizedLastName = normalizeForComparison(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeForComparison(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    /**
     * Count the existing owners whose city matches the given city, comparing case-insensitively with
     * collapsed whitespace. The incoming owner is not yet persisted, so it is not included in the count.
     *
     * @param city the incoming owner's city
     * @return the number of existing owners already in that city
     */
    private int countOwnersInCity(String city) {
        String normalizedCity = normalizeForComparison(city);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getCity()).equals(normalizedCity))
            .count();
    }

    /**
     * Count the existing owners whose registration date equals the given date. The incoming owner is
     * not yet persisted, so it is not included in the count.
     *
     * @param registrationDate the incoming owner's registration date
     * @return the number of existing owners already registered on that date
     */
    private int countOwnersRegisteredOn(LocalDate registrationDate) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Find the existing owner the given (not-yet-persisted) owner is a soft duplicate of: one that
     * shares the owner's last name (compared case-insensitively with collapsed whitespace) and
     * postcode while carrying a different normalized telephone. A blank postcode never soft-matches
     * (postcode is required for the match). When several owners match, the one with the lowest id is
     * returned so the result is deterministic; {@code null} when there is no soft match.
     *
     * @param owner the incoming owner (with normalized telephone and last name resolved)
     * @return the lowest-id matching existing owner, or {@code null} when none matches
     */
    private Owner findPossibleDuplicate(Owner owner) {
        if (!StringUtils.hasText(owner.getPostcode())) {
            return null;
        }
        String normalizedLastName = normalizeForComparison(owner.getLastName());
        String telephone = owner.getTelephone();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getLastName()).equals(normalizedLastName)
                && owner.getPostcode().equals(existing.getPostcode())
                && !java.util.Objects.equals(telephone, existing.getTelephone()))
            .min(java.util.Comparator.comparing(Owner::getId))
            .orElse(null);
    }

    /**
     * Generate the deterministic household identifier for the given last name and postcode. The value
     * is derived from the normalized (case-insensitive, whitespace-collapsed) last name and the
     * postcode, so every owner with the same last name and postcode resolves to the same identifier
     * automatically. It is the first 12 upper-case hex characters of the SHA-256 digest of
     * {@code <normalizedLastName>|<postcode>} (an absent postcode contributes the empty string).
     *
     * @param lastName the owner's last name
     * @param postcode the owner's postcode, may be {@code null}
     * @return the deterministic household identifier
     */
    private String generateHouseholdId(String lastName, String postcode) {
        String key = normalizeForComparison(lastName) + "|" + (postcode == null ? "" : postcode);
        return shortSha256Hex(key, 12);
    }

    /**
     * Normalize an owner address for storage: trim and collapse every internal run of whitespace to a
     * single space, upper-case the result and expand common street-type abbreviations token by token
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} value normalizes
     * to the empty string. The result is the canonical form that is stored, returned and used for every
     * address comparison (household duplicate detection and the shared household id).
     *
     * @param address the raw address, may be {@code null}
     * @return the normalized address
     */
    private String normalizeAddress(String address) {
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
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Normalize a value for identity comparison: trim, collapse every internal run of whitespace to a
     * single space and lower-case the result. A {@code null} value normalizes to the empty string.
     *
     * @param value the raw value, may be {@code null}
     * @return the normalized value
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalize an optional owner email address. A blank/absent value is treated as "not provided"
     * and returns {@code null}. When present, the value must be a syntactically valid address; if it
     * is not, an {@link InvalidRequestException} is thrown (mapped to 400 Bad Request). A valid value
     * is returned lower-cased.
     *
     * @param email the raw email from the request payload, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was provided
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidRequestException(List.of("email"));
        }
        String lowerCased = email.toLowerCase(Locale.ROOT);
        // Reject an address whose domain is on the disposable-domain blocklist.
        String domain = lowerCased.substring(lowerCased.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidRequestException(List.of("email"));
        }
        return lowerCased;
    }

    /**
     * Validate an optional owner postcode. A blank/absent value is treated as "not provided" and
     * passes (postcode is optional). When present it must be exactly four digits, and when the owner's
     * city has a known region (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) the postcode must fall within
     * that region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
     * region accepts any 4-digit postcode. A violation throws an {@link InvalidRequestException} (mapped
     * to 400 Bad Request).
     *
     * @param city     the owner's city (used to resolve the region)
     * @param postcode the raw postcode from the request payload, may be {@code null}
     */
    private void validatePostcode(String city, String postcode) {
        if (!StringUtils.hasText(postcode)) {
            return;
        }
        if (!POSTCODE_PATTERN.matcher(postcode).matches()) {
            throw new InvalidRequestException(List.of("postcode"));
        }
        String region = CITY_REGION.get(city);
        int[] range = region == null ? null : REGION_POSTCODE_RANGE.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidRequestException(List.of("postcode"));
            }
        }
    }

    /**
     * Normalize a telephone into E.164 form. Spaces, dashes and brackets are stripped. A value that
     * already carries a leading '+' keeps its country code; otherwise the default country code
     * ({@code +61}) is assumed and a single leading '0' is dropped from the national digits. The
     * result must be a '+' followed by 8 to 15 digits, otherwise an {@link InvalidRequestException}
     * is thrown (mapped to 400 Bad Request).
     *
     * @param telephone the raw telephone from the request payload
     * @return the E.164 string (e.g. {@code +61412345678})
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = TELEPHONE_SEPARATORS.matcher(telephone).replaceAll("");
        String candidate;
        if (cleaned.startsWith("+")) {
            candidate = cleaned;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            candidate = "+" + DEFAULT_COUNTRY_CODE + national;
        }
        if (!E164_PATTERN.matcher(candidate).matches()) {
            throw new InvalidRequestException(List.of("telephone"));
        }
        // For country codes with a known fixed national-number length, reject a candidate whose
        // national digits (everything after the '+' and the country code) do not match that length.
        String digits = candidate.substring(1);
        for (Map.Entry<String, Integer> entry : COUNTRY_NATIONAL_LENGTH.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidRequestException(List.of("telephone"));
                }
                break;
            }
        }
        return candidate;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> deleteOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Soft delete: flag the owner deleted and retain the record rather than removing it. The
        // owner is still readable afterwards (with 'deleted' true) but is ignored by the create
        // endpoint's duplicate/identity checks.
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
