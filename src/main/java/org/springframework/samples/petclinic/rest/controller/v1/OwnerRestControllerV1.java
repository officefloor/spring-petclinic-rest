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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerIdentity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
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

import jakarta.servlet.http.HttpServletRequest;
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
     * Disposable email domains that are not accepted for an owner. An email whose domain (the part
     * after the {@code @}, compared case-insensitively) is listed here is rejected with a 400.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Dedicated audit logger. Successful owner creation emits a single line here carrying the new
     * owner's id, customer code, registration date and membership level, so audit side-effects can
     * be observed independently of the HTTP response.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Dedicated notification logger. On successful owner creation a single welcome-notification line is
     * enqueued here carrying the new owner's id and member id, so the welcome side-effect can be observed
     * independently of the HTTP response and the audit stream.
     */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Monotonically increasing sequence number stamped on each {@code OWNER_CREATED} audit event, so
     * the immutable event stream has a strict total order across creates. Shared across all instances
     * and requests; incremented once per successful owner creation.
     */
    private static final java.util.concurrent.atomic.AtomicLong OWNER_EVENT_SEQ =
        new java.util.concurrent.atomic.AtomicLong(0);

    /**
     * Common street-type abbreviations expanded during address normalization. Keys and values are
     * upper-cased, matching the state of the address after whitespace and case normalization.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Required national-number length per E.164 country calling code. After normalization the leading
     * country code is stripped and the remaining national digits must match the expected count exactly
     * (e.g. {@code +61} requires 9 national digits, {@code +1} requires 10). Country codes not listed
     * here are only subject to the generic E.164 length bounds.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        Map.of("1", 10, "61", 9);

    /**
     * Fixed city-to-region table used to validate an owner's postcode. Cities not listed have no
     * known region and accept any well-formed (4-digit) postcode.
     */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Inclusive 4-digit postcode range {@code [low, high]} allowed for each region: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099. A supplied postcode outside its city's region range is rejected.
     */
    private static final Map<String, int[]> REGION_POSTCODES =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * Request header carrying the caller-supplied idempotency key for owner creation. When a create
     * repeats with a key already seen, the originally created owner is returned instead of a duplicate.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Maps a previously seen idempotency key to the id of the owner created under it. Populated on each
     * successful create that carried an {@code Idempotency-Key}, and consulted on subsequent creates so a
     * repeat with the same key returns the original owner rather than creating a duplicate.
     */
    private final Map<String, Integer> idempotentOwnerIds = new ConcurrentHashMap<>();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final HttpServletRequest request;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 HttpServletRequest request) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.request = request;
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
        String idempotencyKey = idempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotentOwnerIds.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        validateRequiredOwnerFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        applyAddress(owner);
        validatePostcode(owner.getPostcode(), owner.getCity());
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        LocalDate effectiveRegistrationDate =
            owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(rollToBusinessDay(effectiveRegistrationDate));
        rejectDailyLimitReached(owner.getRegistrationDate());
        rejectCityAtCapacity(owner.getCity());
        owner.setCapacityWarning(isApproachingCityCapacity(owner.getCity()));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        owner.setHouseholdId(householdId(owner));
        rejectDuplicateIdentity(owner, sharesHousehold);
        assignPossibleDuplicate(owner, sharesHousehold);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(isBulkSignupDay(owner.getRegistrationDate()));
        owner.setHouseholdSize(countHousehold(owner.getHouseholdId()));
        owner.setMembershipLevelCap(membershipLevelCap(owner.getHouseholdId()));
        owner.setMemberId(buildMemberId(owner, normalizedTelephone));
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotentOwnerIds.put(idempotencyKey, owner.getId());
        }
        AUDIT.info("Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            ownerMapper.membershipLevel(owner));
        emitOwnerCreatedEvent(owner);
        enqueueWelcomeNotification(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Reads the caller-supplied {@code Idempotency-Key} header from the current request, returning
     * {@code null} when the header is absent or blank so callers can treat those uniformly as "no key".
     *
     * @return the trimmed idempotency key, or {@code null} when none was supplied
     */
    private String idempotencyKey() {
        String value = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
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
        owner.setDeleted(Boolean.TRUE);
        this.clinicService.saveOwner(owner);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
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

    /**
     * Emits the immutable, structured {@code OWNER_CREATED} audit event for a freshly persisted owner.
     * The event is a JSON object {@code {seq, ownerId, memberId, membershipLevel, event}} published
     * to the {@code AUDIT} logger alongside the human-readable audit line. {@code seq} is a strictly
     * increasing sequence across all creates (see {@link #OWNER_EVENT_SEQ}), giving the event stream a
     * total order.
     *
     * <p>The identifier field carries the owner's <em>current primary identifier</em>, obtained from
     * {@link #primaryIdentifier(Owner)}: the unified member id, which the event carries to identify the
     * owner.
     *
     * @param owner the owner that was just created and persisted
     */
    /**
     * Enqueues a welcome notification for a freshly created owner by emitting a single line to the
     * dedicated {@code NOTIFY} logger carrying the new owner's id and member id. The notification is a
     * side-effect of a successful create only, observable independently of the HTTP response.
     *
     * @param owner the owner that was just created and persisted
     */
    private void enqueueWelcomeNotification(Owner owner) {
        NOTIFY.info("Welcome notification: ownerId={} memberId={}", owner.getId(), owner.getMemberId());
    }

    private void emitOwnerCreatedEvent(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(
            OWNER_EVENT_SEQ.incrementAndGet(),
            owner.getId(),
            primaryIdentifier(owner),
            ownerMapper.membershipLevel(owner),
            ownerMapper.ownerSegment(owner).getValue());
        AUDIT.info(event.toJson());
    }

    /**
     * The schema version of the {@code OWNER_CREATED} audit event. Version 2 accompanies the version-2
     * owner identity: the event carries an explicit {@code schemaVersion} and the owner segment recomputed
     * from the version-2 identity.
     */
    private static final int OWNER_EVENT_SCHEMA_VERSION = 2;

    /**
     * Returns the owner's current primary identifier, the single value the {@code OWNER_CREATED} audit
     * event carries to identify the owner: the owner's unified {@link Owner#getMemberId() member id}. It
     * is intentionally the only place that decides which field is "primary", so the audit event references
     * the member id.
     *
     * @param owner the owner being created
     * @return the owner's current primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Immutable, structured {@code OWNER_CREATED} audit event, at schema version 2. Serialized to a compact
     * JSON object {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment,
     * event:'OWNER_CREATED'}} for the {@code AUDIT} log. Version 2 adds the explicit {@code schemaVersion}
     * and the {@code ownerSegment} recomputed from the version-2 identity, and the {@code memberId} carried
     * is the version-2 member id. Being a record, once constructed its fields cannot change, so the emitted
     * event is a faithful snapshot of the owner at creation time.
     *
     * @param seq strictly increasing sequence number across all creates
     * @param ownerId the created owner's id
     * @param memberId the owner's version-2 primary identifier at creation time
     * @param membershipLevel the owner's effective membership level at creation time
     * @param ownerSegment the owner's segment recomputed from the version-2 identity at creation time
     */
    private record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
                                     String ownerSegment) {

        private String toJson() {
            return "{\"schemaVersion\":" + OWNER_EVENT_SCHEMA_VERSION
                + ",\"seq\":" + seq
                + ",\"ownerId\":" + ownerId
                + ",\"memberId\":" + jsonValue(memberId)
                + ",\"membershipLevel\":" + membershipLevel
                + ",\"ownerSegment\":" + jsonValue(ownerSegment)
                + ",\"event\":\"OWNER_CREATED\"}";
        }

        /** Renders a nullable string as a JSON string literal (with escaping) or {@code null}. */
        private static String jsonValue(String value) {
            if (value == null) {
                return "null";
            }
            StringBuilder sb = new StringBuilder(value.length() + 2);
            sb.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> sb.append(c);
                }
            }
            sb.append('"');
            return sb.toString();
        }
    }

    /**
     * Builds the unified member id for a newly created owner, formatted {@code '<REGION><FY><HASH8><CHK>'}
     * where {@code REGION} is the plain region code derived from the owner's postcode (see
     * {@link #deriveRegion}), {@code FY} is the two-digit fiscal year of the owner's business-day-adjusted
     * registration date, {@code HASH8} is the first 8 upper-case hex characters of the SHA-256 digest of
     * {@code '<regionCodeV2>|<normalizedTelephone><lastName>'} — the {@link #regionCodeV2(String) version-2
     * region code} (which mixes in the fixed {@code 'V2'} tag) followed by the owner's normalized telephone
     * and last name — and {@code CHK} is a single Luhn check digit computed over the digits of
     * {@code '<REGION><FY><HASH8>'}. Mixing the {@code 'V2'} tag into the hash makes every member id differ
     * from the value version 1 produced, while the leading {@code REGION} segment stays the plain region so
     * {@code locality}, {@code timezone} and the owner segment never see the tag.
     *
     * <p>When the computed member id collides with an existing owner's member id, it is de-duplicated by
     * appending {@code '-<n>'} with the smallest {@code n} of 2 or more that makes the result unique across
     * all existing owners' member ids. The de-duplicated id is what gets assigned.
     *
     * @param owner the owner being created, whose postcode, city, registration date and last name feed the id
     * @param normalizedTelephone the owner's E.164-normalized telephone
     * @return the assigned member id, de-duplicated against existing owners if necessary
     */
    private String buildMemberId(Owner owner, String normalizedTelephone) {
        String region = deriveRegion(owner.getPostcode(), owner.getCity());
        String fiscalYear = String.format("%02d", ownerMapper.fiscalYearValue(owner.getRegistrationDate()) % 100);
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String hash8 = sha256Hex8(regionCodeV2(region) + "|" + normalizedTelephone + lastName);
        String base = region + fiscalYear + hash8;
        String memberId = base + luhnCheckDigit(base);
        Set<String> existingIds = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(id -> id != null)
            .collect(java.util.stream.Collectors.toSet());
        if (!existingIds.contains(memberId)) {
            return memberId;
        }
        int n = 2;
        while (existingIds.contains(memberId + "-" + n)) {
            n++;
        }
        return memberId + "-" + n;
    }

    /**
     * Computes a single Luhn check digit (0-9) over the decimal digits contained in the given string.
     * Non-digit characters are ignored, so the check digit covers the region, fiscal-year and HASH8
     * digits of a member id's {@code '<REGION><FY><HASH8>'} base.
     *
     * @param value the string whose digits the check digit is computed over
     * @return the Luhn check digit, a single digit in {@code 0-9}
     */
    private int luhnCheckDigit(String value) {
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
     * Derives an owner's region code, preferring the postcode: the region whose {@link #REGION_POSTCODES}
     * range contains the (4-digit) postcode is returned, falling back to the fixed {@link #CITY_REGION}
     * city table when the postcode is absent or in no known range, and finally to {@code "UNKNOWN"}.
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @param city the owner's city, used to resolve the region when the postcode does not
     * @return the canonical region string, or {@code "UNKNOWN"} when neither source resolves a region
     */
    /**
     * The fixed version tag mixed into the region code used inside the version-2 identifiers (member id,
     * household id and identity key), so every identifier differs from the value version 1 produced. The
     * tag is only ever mixed into the identifiers' derivation; it never appears in the plain region used
     * for {@code locality}, {@code timezone} or the owner segment.
     */
    private static final String IDENTITY_VERSION_TAG = "V2";

    /**
     * Derives the version-2 region code used inside the identifiers: the plain region with the fixed
     * {@link #IDENTITY_VERSION_TAG 'V2'} version tag prefixed (e.g. {@code "V2NSW"}). This mirrors the
     * mapper's {@code regionCodeV2} so the member id, household id and identity key are all derived over
     * the same tagged region.
     *
     * @param region the plain region code (see {@link #deriveRegion})
     * @return the version-2 region code
     */
    private String regionCodeV2(String region) {
        return IDENTITY_VERSION_TAG + region;
    }

    private String deriveRegion(String postcode, String city) {
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            } catch (NumberFormatException ex) {
                // Not a numeric postcode; fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Computes the first 8 upper-case hex characters (the first 4 bytes) of the SHA-256 digest of the
     * given input's UTF-8 bytes.
     *
     * @param input the string to hash
     * @return the 8-character upper-case hex prefix of the SHA-256 digest
     */
    private String sha256Hex8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
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
        if (isBlank(ownerFieldsDto.getAddressLine1())
            && isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
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
     * Whether an existing owner has been soft-deleted. A soft-deleted owner retains its row but is
     * excluded from the create endpoint's duplicate and identity checks, so it never blocks the
     * creation of a new owner. A {@code null} flag (an owner predating the soft-delete column) is
     * treated as not deleted.
     *
     * @param owner an existing owner
     * @return {@code true} if the owner is flagged deleted
     */
    private boolean isDeleted(Owner owner) {
        return Boolean.TRUE.equals(owner.getDeleted());
    }

    /**
     * Validates a supplied owner postcode. The postcode is optional: a {@code null} value is
     * accepted and the request stays backward-compatible. When present it must be a 4-digit value,
     * and must fall within the inclusive range allowed for the owner's city region per the fixed
     * {@link #CITY_REGION} / {@link #REGION_POSTCODES} tables (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099). A city with no known region accepts any 4-digit postcode. The value is stored
     * and returned exactly as given.
     *
     * @param postcode the submitted postcode, may be {@code null} when absent
     * @param city the owner's city, used to resolve the region whose range the postcode must satisfy
     * @throws InvalidOwnerFieldsException if a present postcode is not 4 digits or is out of range
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
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
        validateNationalNumberLength(digits);
        return "+" + digits;
    }

    /**
     * Validates that the national-number portion of a normalized E.164 number (the digits after the
     * country calling code) has the exact length required for that country. The country code is matched
     * against {@link #NATIONAL_NUMBER_LENGTHS} (e.g. {@code +61} requires 9 national digits, {@code +1}
     * requires 10). Numbers whose country code is not listed are left to the generic length bounds.
     *
     * @param digits the normalized E.164 digits, without the leading {@code '+'}
     * @throws InvalidOwnerFieldsException if the national-number length is wrong for the country code
     */
    private void validateNationalNumberLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_NUMBER_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidOwnerFieldsException(List.of("telephone"));
                }
                return;
            }
        }
    }

    /**
     * Normalizes an optional owner email. A {@code null} value is left untouched (the field is optional).
     * When a value is present it must be a syntactically valid address (see {@link #EMAIL_PATTERN}); the
     * accepted value is trimmed and lower-cased before it is stored and returned.
     *
     * @param email the raw email value from the submitted owner payload, may be {@code null}
     * @return {@code null} if no email was supplied, otherwise the lower-cased email
     * @throws InvalidOwnerFieldsException if a value is present but not a syntactically valid address,
     *         or if its domain is on the disposable-domain blocklist ({@link #DISPOSABLE_EMAIL_DOMAINS})
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return normalized;
    }

    /**
     * Rejects creating an owner whose derived identity key (the SHA-256 hex digest of
     * {@code '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'}, see {@link OwnerMapper#identityKey})
     * exactly equals that of an existing non-deleted owner. Two owners are treated as duplicates only when
     * their whole identity keys match, so owners who share a last-name Soundex but differ in telephone or
     * email are not duplicates and are admitted (flagged as a soft match instead). Such an owner is rejected
     * with a 409, <em>unless</em> the request opts into {@code sharesHousehold}, which bypasses the block
     * entirely.
     *
     * @param owner the owner being created, whose identity key is matched
     * @param sharesHousehold whether the request declared the owner a shared-household member
     * @throws DuplicateOwnerIdentityException if another owner already has the same identity key
     */
    private void rejectDuplicateIdentity(Owner owner, boolean sharesHousehold) {
        if (sharesHousehold) {
            return;
        }
        String identityKey = ownerMapper.identityKey(owner);
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .filter(existing -> !isDeleted(existing))
            .anyMatch(existing -> identityKey.equals(ownerMapper.identityKey(existing)));
        if (duplicate) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * Flags a soft ("possible") duplicate on the owner being created. Unlike a hard duplicate (which is
     * rejected outright by {@link #rejectDuplicateIdentity}), a soft duplicate is still created: it is an
     * owner whose {@link OwnerMapper#identityKey identity key} <em>differs</em> from an existing owner's
     * but which shares that owner's last-name {@link OwnerMapper#soundex Soundex code} and postcode. When
     * such an existing owner is found, {@code possibleDuplicate} is set {@code true} and
     * {@code possibleDuplicateOf} is set to that owner's id (the earliest by id when several match); otherwise
     * {@code possibleDuplicate} is {@code false} and {@code possibleDuplicateOf} stays {@code null}. Both values
     * are snapshotted on the owner so they reflect the population as it stood when the owner was created.
     *
     * <p>Because the telephone is part of the identity key, two owners with the same last name and postcode
     * but different telephones no longer collide on the identity key, so they are admitted as a soft match
     * here rather than rejected as a hard household duplicate.
     *
     * <p>A declared household member (a request that set {@code sharesHousehold} to bypass the duplicate
     * block) is never a <em>suspected</em> duplicate: it is knowingly created as another member of the
     * household, so {@code possibleDuplicate} is left {@code false} and {@code possibleDuplicateOf}
     * {@code null} without consulting the existing population.
     *
     * @param owner the owner being created, whose last-name Soundex and postcode are matched
     * @param sharesHousehold whether the request declared the owner a shared-household member
     */
    private void assignPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        if (sharesHousehold) {
            owner.setPossibleDuplicateOf(null);
            owner.setPossibleDuplicate(false);
            return;
        }
        String postcode = owner.getPostcode();
        Integer matchId = null;
        if (postcode != null) {
            String identityKey = ownerMapper.identityKey(owner);
            String lastNameCode = OwnerIdentity.soundex(owner.getLastName());
            for (Owner existing : this.clinicService.findAllOwners()) {
                if (!isDeleted(existing)
                    && postcode.equals(existing.getPostcode())
                    && lastNameCode.equals(OwnerIdentity.soundex(existing.getLastName()))
                    && !identityKey.equals(ownerMapper.identityKey(existing))
                    && existing.getId() != null
                    && (matchId == null || existing.getId() < matchId)) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicateOf(matchId);
        owner.setPossibleDuplicate(matchId != null);
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
     * Fixed list of public holidays. A registration date that lands on one of these dates is not a
     * business day and is rolled forward to the next non-holiday weekday.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

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
     * on a weekday that is not a public holiday: when the supplied or defaulted date is a Saturday, a
     * Sunday, or a listed public holiday it is rolled forward one day at a time until it reaches the
     * next non-holiday business day, and that adjusted date becomes the owner's {@code registrationDate}.
     * A date that is already a non-holiday weekday is returned unchanged. Every value derived from the
     * registration date (the daily create-limit count, the membership number's year segment, and so on)
     * uses this adjusted date.
     *
     * @param date the effective registration date (supplied in the request or defaulted to the server date)
     * @return the same date if it is a non-holiday weekday, otherwise the next non-holiday business day
     */
    /**
     * Rejects creating an owner whose supplied {@code registrationDate} is later than the server's
     * current date. A registration date is not permitted to lie in the future; a {@code null} date
     * (defaulted to the server date) and any date on or before today are accepted. The check runs
     * against the date exactly as supplied, before it is rolled forward onto a business day.
     *
     * @param registrationDate the registration date supplied in the request, may be {@code null}
     * @throws FutureRegistrationDateException if the supplied date is after the server date
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }

    private LocalDate rollToBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (!isBusinessDay(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        return !PUBLIC_HOLIDAYS.contains(date);
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
     * The number of owners a city must already hold before a newly created owner in that city is
     * flagged as approaching capacity. Once a city already contains at least this many owners (but
     * still fewer than {@link #MAX_OWNERS_PER_CITY}), the new owner's {@code capacityWarning} is set.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Determines whether the owner being created is approaching the per-city capacity limit. The
     * warning is raised when the owner's city already contains between {@link
     * #CITY_CAPACITY_WARNING_THRESHOLD} and {@code MAX_OWNERS_PER_CITY - 1} owners (inclusive) at the
     * time this owner is created; the hard rejection at {@link #MAX_OWNERS_PER_CITY} is unchanged.
     * Existing owners' cities are compared case-insensitively with surrounding and repeated internal
     * whitespace collapsed (see {@link #normalizeForComparison}), matching {@link #rejectCityAtCapacity}.
     *
     * @param city the city of the owner being created
     * @return {@code true} if the city already holds 40-49 owners
     */
    private boolean isApproachingCityCapacity(String city) {
        String normalizedCity = normalizeForComparison(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedCity.equals(normalizeForComparison(existing.getCity())))
            .count();
        return ownersInCity >= CITY_CAPACITY_WARNING_THRESHOLD && ownersInCity < MAX_OWNERS_PER_CITY;
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
     * Computes the membership-level ceiling for the owner being created, snapshotted at creation time.
     * A new owner's membership level cannot exceed one above the current maximum membership level among
     * their existing household members: the returned cap is that maximum plus one. Existing household
     * members are the non-deleted owners already carrying the same {@code householdId}, and their
     * membership level is their effective (already-capped) {@link OwnerMapper#membershipLevel level}.
     * Returns {@code null} when there is no existing household member, in which case no cap applies.
     *
     * @param householdId the household identifier assigned to the owner being created, may be {@code null}
     * @return one above the highest household member's membership level, or {@code null} when none exist
     */
    private Integer membershipLevelCap(String householdId) {
        if (householdId == null) {
            return null;
        }
        java.util.OptionalInt maxLevel = this.clinicService.findAllOwners().stream()
            .filter(existing -> !isDeleted(existing))
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .mapToInt(existing -> ownerMapper.membershipLevel(existing))
            .max();
        return maxLevel.isPresent() ? maxLevel.getAsInt() + 1 : null;
    }

    /**
     * Derives the deterministic version-2 household identifier for an owner: the first 12 hex characters
     * of the SHA-256 digest of {@code '<regionCodeV2>|<normalizedLastName>|<postcode>'}, where the leading
     * segment is the {@link #regionCodeV2(String) version-2 region code} (which mixes in the fixed
     * {@code 'V2'} tag), the last name is normalized for case- and whitespace-insensitive comparison (see
     * {@link #normalizeForComparison}) and a {@code null} postcode contributes an empty segment. Mixing the
     * {@code 'V2'} tag in makes the identifier differ from the value version 1 produced; every owner that
     * shares a last name and postcode still resolves to the exact same identifier, regardless of creation
     * order, making them members of one household.
     *
     * @param owner the owner being created, whose last name, postcode and derived region feed the id
     * @return the deterministic household identifier
     */
    private String householdId(Owner owner) {
        String region = deriveRegion(owner.getPostcode(), owner.getCity());
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String key = regionCodeV2(region) + "|" + normalizeForComparison(lastName)
            + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
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
    /**
     * Applies address normalization to the owner and derives the composed {@code address}, preferring
     * the structured fields over the flat {@code address} input for backward compatibility. When a
     * non-blank {@code addressLine1} is supplied, both structured lines are normalized (see
     * {@link #normalizeAddress}) and stored, and the composed {@code address} becomes the normalized
     * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when an
     * {@code addressLine2} was supplied. Otherwise the flat {@code address} is normalized and stored, and
     * the structured lines are cleared. The stored {@code address} is the form every later step reads.
     *
     * @param owner the owner being created, whose raw address fields are normalized in place
     */
    private void applyAddress(Owner owner) {
        String line1 = owner.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String normalizedLine1 = normalizeAddress(line1);
            String normalizedLine2 = normalizeAddress(owner.getAddressLine2());
            owner.setAddressLine1(normalizedLine1);
            if (!normalizedLine2.isEmpty()) {
                owner.setAddressLine2(normalizedLine2);
                owner.setAddress(normalizedLine1 + " " + normalizedLine2);
            } else {
                owner.setAddressLine2(null);
                owner.setAddress(normalizedLine1);
            }
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(normalizeAddress(owner.getAddress()));
        }
    }

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
