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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.samples.petclinic.rest.advice.CityOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
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
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Dedicated audit logger. A successful owner create emits here, independently of the application
     * log: first a human-readable line carrying the new owner's id, customer code, registration date
     * and membership level, then the immutable structured {@code OWNER_CREATED} event (see
     * {@link OwnerCreatedEvent}) as a compact JSON object.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Process-wide sequence backing the {@code seq} of the structured {@code OWNER_CREATED} event. It
     * is incremented once per successful create so each emitted event carries a strictly larger,
     * monotonically increasing sequence number than the one before it.
     */
    private static final AtomicInteger AUDIT_SEQUENCE = new AtomicInteger();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    /**
     * Pattern for a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain containing at least one dot, with no whitespace in any segment.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Email domains belonging to disposable/throwaway mail providers. An owner whose email address
     * is on this blocklist (compared case-insensitively on the domain, the part after the final
     * {@code '@'}) is rejected with a 400 Bad Request.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /**
     * Required national-number length per E.164 country code. A normalized telephone whose country
     * code appears here must carry exactly this many national digits after the code; Australia
     * ({@code +61}) requires 9 and the North American Numbering Plan ({@code +1}) requires 10.
     * Country codes not listed are only subject to the generic 8-to-15 total-digit rule.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTH = Map.of("61", 9, "1", 10);

    /**
     * Maximum number of owners permitted in a single city. A create whose city already holds this many
     * owners (compared case-insensitively) is rejected with a 409 Conflict.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Lower bound of the per-city capacity-warning band. When a city already holds at least this many
     * but fewer than {@value #MAX_OWNERS_PER_CITY} owners it is approaching capacity, and a create in
     * that city is flagged ({@code capacityWarning} true); the hard rejection at
     * {@value #MAX_OWNERS_PER_CITY} is unaffected.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Maximum number of owners that may be registered on a single day (by registration date). A create
     * attempted once this many owners already carry today's registration date is rejected with a
     * 429 Too Many Requests.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Threshold above which a create is flagged with a bulk-signup warning. When more than this many
     * owners already carry the new owner's registration date, the created owner is flagged
     * ({@code bulkSignupWarning} true); otherwise the flag is false.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Expansions of the common street-type abbreviations recognised by {@link #normalizeAddress}.
     * Keys and values are upper-case, matching the casing the address is folded to before expansion.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Inclusive 4-digit postcode range permitted per region, keyed by the region derived from the
     * owner's city ({@link Owner#getLocality()}): NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A
     * region not listed (the {@code 'UNKNOWN'} locality of a city with no known region) accepts any
     * 4-digit postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Name of the request header that carries the client-supplied idempotency key for an owner create.
     * When a create repeats with a key already seen, the originally created owner is returned with
     * {@code 200 OK} instead of a second owner being created.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per idempotency key, the id of the owner created by the first request that carried that
     * key, so a later create repeating the same key can return the original owner instead of creating a
     * duplicate. The key space is the client-supplied {@code Idempotency-Key} header value.
     */
    private final Map<String, Integer> idempotentCreates = new ConcurrentHashMap<>();

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
        String idempotencyKey = currentIdempotencyKey();
        OwnerDto replayed = replayIfSeen(idempotencyKey);
        if (replayed != null) {
            return new ResponseEntity<>(replayed, HttpStatus.OK);
        }
        validateRequiredFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        LocalDate registrationDate = Owner.resolveRegistrationDate(owner.getRegistrationDate());
        owner.setRegistrationDate(registrationDate);
        requireDailyCapacity(registrationDate);
        long cityOwnerCount = countOwnersInCity(owner.getCity());
        requireCityHasCapacity(owner.getCity(), cityOwnerCount);
        owner.setCapacityWarning(isApproachingCityCapacity(cityOwnerCount));
        validatePostcode(owner);
        applyAddress(owner, ownerFieldsDto);
        owner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        owner.setHouseholdId(computeHouseholdId(owner.getLastName(), owner.getPostcode()));
        boolean declaredHouseholdMember = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        requireUniqueIdentity(owner);
        flagPossibleDuplicate(owner, declaredHouseholdMember);
        owner.setCustomerCode(deduplicateCustomerCode(owner.buildCustomerCode()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        stampHouseholdSize(owner);
        capMembershipLevelToHousehold(owner);
        owner.setBulkSignupWarning(countOwnersRegisteredOn(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD);
        this.clinicService.saveOwner(owner);
        rememberCreate(idempotencyKey, owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(),
            owner.getMembershipNumber());
        emitOwnerCreatedEvent(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Emits the immutable structured {@code OWNER_CREATED} event to the {@link #AUDIT} logger as a
     * compact JSON object, alongside the human-readable audit line. The event's {@code seq} is drawn
     * from the process-wide {@link #AUDIT_SEQUENCE}, so it strictly increases across creates, and it
     * carries both the owner's id and its current primary identifier via
     * {@link #primaryIdentifier(Owner)}.
     *
     * @param owner the owner that was just created and saved
     */
    private void emitOwnerCreatedEvent(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(AUDIT_SEQUENCE.incrementAndGet(),
            owner.getId(), primaryIdentifier(owner), owner.getMembershipLevel());
        AUDIT.info(event.toJson());
    }

    /**
     * The owner's current primary identifier, the single value the structured {@code OWNER_CREATED}
     * event carries to identify the owner. Today that is the owner's {@link Owner#getCustomerCode()
     * customer code}; when the customer code is later unified into the member id, this becomes the
     * sole place to switch so the event carries the member id instead.
     *
     * @param owner the owner that was just created
     * @return the owner's current primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * The immutable structured event emitted on a successful owner create. {@code seq} is the
     * process-wide monotonically increasing sequence across creates; {@code ownerId} is the new
     * owner's id; {@code customerCode} carries the owner's current primary identifier (today the
     * customer code, whatever replaces it later); {@code membershipLevel} is the owner's membership
     * level. It serializes to a compact JSON object whose {@code event} is always
     * {@value #EVENT_TYPE}.
     *
     * @param seq the monotonically increasing create sequence number
     * @param ownerId the created owner's id
     * @param customerCode the owner's current primary identifier
     * @param membershipLevel the owner's membership level
     */
    private record OwnerCreatedEvent(int seq, Integer ownerId, String customerCode, Integer membershipLevel) {

        /** The fixed {@code event} discriminator carried by every emitted event. */
        private static final String EVENT_TYPE = "OWNER_CREATED";

        /** Shared, thread-safe mapper used only to serialize the event to its JSON form. */
        private static final ObjectMapper MAPPER = JsonMapper.builder().build();

        /**
         * Serializes this event to its compact JSON form
         * {@code {seq, ownerId, customerCode, membershipLevel, event}}, with the fields in that order.
         *
         * @return the event as a JSON object string
         */
        private String toJson() {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("seq", this.seq);
            fields.put("ownerId", this.ownerId);
            fields.put("customerCode", this.customerCode);
            fields.put("membershipLevel", this.membershipLevel);
            fields.put("event", EVENT_TYPE);
            return MAPPER.writeValueAsString(fields);
        }
    }

    /**
     * Reads the client-supplied idempotency key from the current request's {@code Idempotency-Key}
     * header. Returns {@code null} when the header is absent or blank (the create is not idempotent),
     * or when no servlet request is bound to the current thread.
     *
     * <p>The header is read from the bound request rather than taken as a {@code @RequestHeader} method
     * parameter because {@code addOwner}'s signature is fixed by the generated {@link OwnersApi}
     * interface and cannot carry an extra parameter.
     *
     * @return the trimmed idempotency key, or {@code null} when none was supplied
     */
    private String currentIdempotencyKey() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String key = attributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
            if (StringUtils.hasText(key)) {
                return key.trim();
            }
        }
        return null;
    }

    /**
     * Returns the owner originally created under the given idempotency key, so a create repeating a
     * key already seen replays the first result instead of creating a duplicate. Yields {@code null}
     * when there is no key, the key has not been seen, or the remembered owner no longer exists (in
     * which case the create proceeds normally).
     *
     * @param idempotencyKey the client-supplied idempotency key, or {@code null} when none was supplied
     * @return the originally created owner as a DTO, or {@code null} when there is nothing to replay
     */
    private OwnerDto replayIfSeen(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        Integer existingId = idempotentCreates.get(idempotencyKey);
        if (existingId == null) {
            return null;
        }
        Owner existing = this.clinicService.findOwnerById(existingId);
        return existing == null ? null : ownerMapper.toOwnerDto(existing);
    }

    /**
     * Remembers the id of a just-created owner against the idempotency key that produced it, so a later
     * create repeating the same key can be replayed by {@link #replayIfSeen}. A {@code null} key (the
     * create was not idempotent) is ignored.
     *
     * @param idempotencyKey the client-supplied idempotency key, or {@code null} when none was supplied
     * @param owner the owner that was just created and saved
     */
    private void rememberCreate(String idempotencyKey, Owner owner) {
        if (idempotencyKey != null) {
            idempotentCreates.put(idempotencyKey, owner.getId());
        }
    }

    /**
     * Rejects a create whose supplied registration date is later than the server's current date. A
     * registration may be back-dated but never post-dated, so a future date is treated as a client
     * error. A {@code null} supplied date (the client omitted it) is allowed, as it defaults to the
     * server date.
     *
     * @param suppliedRegistrationDate the registration date from the request, or {@code null} when the
     *     client omitted it
     * @throws FutureRegistrationDateException if the supplied date is after the server's current date
     */
    private void rejectFutureRegistrationDate(LocalDate suppliedRegistrationDate) {
        if (suppliedRegistrationDate != null && suppliedRegistrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(suppliedRegistrationDate);
        }
    }

    /**
     * Rejects a create once {@value #MAX_OWNERS_PER_DAY} or more owners already carry the new owner's
     * adjusted business-day registration date. Existing owners are counted by {@code registrationDate}
     * equal to that date, before the new owner is saved, so the count reflects only the owners that
     * existed prior to this create.
     *
     * @param registrationDate the new owner's effective registration date, already rolled forward onto
     *     a business day
     * @throws DailyOwnerLimitException if {@value #MAX_OWNERS_PER_DAY} or more owners are already
     *     registered on that business day
     */
    private void requireDailyCapacity(LocalDate registrationDate) {
        if (countOwnersRegisteredOn(registrationDate) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(registrationDate);
        }
    }

    /**
     * Counts the existing owners whose registration date equals the given business-day date. This is
     * evaluated before the new owner is saved, so it reflects only the owners that existed prior to
     * this create. It is the single definition of the per-day count shared by the daily-limit rule and
     * the bulk-signup warning.
     *
     * @param registrationDate the effective business-day registration date to count against
     * @return the number of pre-existing owners registered on that date
     */
    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Rejects an owner whose city already contains the maximum allowed number of owners
     * ({@value #MAX_OWNERS_PER_CITY}). The count is supplied by {@link #countOwnersInCity(String)} and
     * reflects only the owners that existed prior to this create.
     *
     * @param city the city of the owner being created
     * @param cityOwnerCount the number of pre-existing owners already in that city
     * @throws CityOwnerLimitException if the city already holds {@value #MAX_OWNERS_PER_CITY} or more owners
     */
    private void requireCityHasCapacity(String city, long cityOwnerCount) {
        if (cityOwnerCount >= MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitException(city);
        }
    }

    /**
     * Counts the existing owners whose city equals the given city, compared case-insensitively. This is
     * evaluated before the new owner is saved, so it reflects only the owners that existed prior to this
     * create. It is the single definition of the per-city count shared by the city hard-limit rule and
     * the approaching-capacity warning.
     *
     * @param city the city of the owner being created
     * @return the number of pre-existing owners in that city
     */
    private long countOwnersInCity(String city) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getCity(), city))
            .count();
    }

    /**
     * Whether a city holding the given number of owners is approaching the per-city capacity limit: at
     * least {@value #CITY_CAPACITY_WARNING_THRESHOLD} but fewer than {@value #MAX_OWNERS_PER_CITY}. A
     * create in such a city is flagged ({@code capacityWarning} true); the hard rejection at
     * {@value #MAX_OWNERS_PER_CITY} is unaffected.
     *
     * @param cityOwnerCount the number of pre-existing owners already in the city
     * @return whether the city is in the 40-49 approaching-capacity band
     */
    private boolean isApproachingCityCapacity(long cityOwnerCount) {
        return cityOwnerCount >= CITY_CAPACITY_WARNING_THRESHOLD && cityOwnerCount < MAX_OWNERS_PER_CITY;
    }

    /**
     * Validates a supplied postcode against the owner's region. Postcode is optional: an owner with no
     * postcode is accepted unchanged. When present it is a 4-digit string (enforced by the request
     * schema) that must fall within the inclusive range permitted for the region derived from the
     * owner's city ({@link #REGION_POSTCODE_RANGES}); a city whose region is unknown accepts any 4-digit
     * postcode. An out-of-range postcode is rejected with a 400 Bad Request.
     *
     * @param owner the owner being created, with its city and postcode set
     * @throws InvalidOwnerFieldsException if a supplied postcode is out of range for the city's region
     */
    private void validatePostcode(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        int[] range = REGION_POSTCODE_RANGES.get(owner.getLocality());
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }

    /**
     * Counts the existing owners that already share the given first name and last name, compared
     * case-insensitively. This is evaluated before the new owner is saved, so it reflects only the
     * owners that existed prior to this create.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of pre-existing namesakes
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getFirstName(), firstName)
                && equalsIgnoreCase(existing.getLastName(), lastName))
            .count();
    }

    /**
     * Compares two possibly-{@code null} strings for equality, ignoring case. Two {@code null} values
     * are considered equal and a {@code null} is never equal to a non-{@code null} value.
     *
     * @param a the first value, possibly {@code null}
     * @param b the second value, possibly {@code null}
     * @return whether the values are equal ignoring case
     */
    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    /**
     * Rejects an owner payload that is missing or blank in any required field. Bean Validation
     * (via {@code @Valid}) already rejects {@code null} values and fields that violate their size
     * or pattern constraints; this guard additionally rejects whitespace-only values for fields
     * (such as address and city) that have no pattern constraint, so that no required field is blank.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if one or more required fields are missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> invalidFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            invalidFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            invalidFields.add("lastName");
        }
        if (!StringUtils.hasText(normalizeAddress(ownerFieldsDto.getAddressLine1()))
            && !StringUtils.hasText(normalizeAddress(ownerFieldsDto.getAddress()))) {
            invalidFields.add("address");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            invalidFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            invalidFields.add("telephone");
        }
        if (!invalidFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(invalidFields);
        }
    }

    /**
     * Normalizes a submitted telephone number to E.164 form and requires the result to be valid. The
     * normalized E.164 string (a leading {@code '+'} followed by country code and national digits) is
     * what gets stored and returned.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized E.164 telephone number
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Converts a telephone value to E.164 form, giving the single authoritative definition of a
     * telephone's normalized form. Spaces, dashes and brackets are stripped. A leading {@code '+'}
     * and its country code are kept when present; otherwise country code {@code '+61'} is assumed and
     * a single leading {@code '0'} is dropped from the national digits. The result is valid only when
     * it contains 8 to 15 digits after the {@code '+'} and, for a recognized country code (see
     * {@link #NATIONAL_NUMBER_LENGTH}), carries exactly that country's required national-number length.
     *
     * <p>Returns {@code null} rather than throwing when the value cannot form a valid E.164 number, so
     * it is safe to apply to already-stored (possibly legacy) values when comparing them.
     *
     * @param telephone the raw telephone value, possibly {@code null}
     * @return the E.164 string, or {@code null} if the value cannot form a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[ \\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        if (!nationalNumberLengthValid(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks a normalized E.164 digit string (country code followed by national digits, without the
     * leading {@code '+'}) against the per-country national-number length rule. For a country code
     * listed in {@link #NATIONAL_NUMBER_LENGTH} the national part must have exactly the required length
     * (e.g. {@code '+61'} needs 9 national digits, {@code '+1'} needs 10); any other country code is
     * accepted, leaving only the generic total-length rule to apply.
     *
     * @param digits the E.164 digits without the leading {@code '+'}
     * @return {@code true} if the national-number length matches the country code's requirement
     */
    private boolean nationalNumberLengthValid(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_NUMBER_LENGTH.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code)) {
                return digits.length() - code.length() == entry.getValue();
            }
        }
        return true;
    }

    /**
     * Normalizes an optional owner email. An absent (null or blank) email is allowed and yields
     * {@code null}. When present, the value must be a syntactically valid address whose domain is not
     * on the disposable-domain blocklist ({@link #DISPOSABLE_EMAIL_DOMAINS}); it is then stored and
     * returned lower-cased.
     *
     * @param email the raw email value from the request, possibly {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but is not a valid email address or
     *     its domain is on the disposable-domain blocklist
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return normalized;
    }

    /**
     * Rejects an owner whose whole identity key is already used by an existing (non-deleted) owner.
     * This is the single consolidated duplicate check: the identity key
     * ({@link Owner#getIdentityKey()}) is the lower-case SHA-256 hex of the owner's normalized
     * telephone, lower-cased email (empty when absent) and the Soundex of its last name joined with
     * {@code '|'}, and a create is a duplicate only when the new owner's whole key equals an existing
     * owner's. Because the telephone is part of the key, two owners sharing a last name and postcode
     * but carrying different telephones have different identity keys and are both allowed (the second
     * is flagged a soft match by {@link #flagPossibleDuplicate}). Owners flagged deleted are ignored.
     *
     * @param owner the owner being created, with its normalized telephone, email and last name set
     * @throws DuplicateIdentityException if another owner already has this identity key
     */
    private void requireUniqueIdentity(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean inUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (inUse) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * De-duplicates a newly computed customer code against the codes already held by existing owners.
     * When the computed code is unique it is returned unchanged. When it collides with an existing
     * owner's {@code customerCode}, {@code '-<n>'} is appended with the smallest {@code n} of 2 or more
     * that makes the result unique (so a second collision yields {@code '-2'}, a third {@code '-3'}, and
     * so on), and that de-duplicated code is returned.
     *
     * @param customerCode the freshly computed customer code to de-duplicate
     * @return a customer code not held by any existing owner
     */
    private String deduplicateCustomerCode(String customerCode) {
        Set<String> inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (!inUse.contains(customerCode)) {
            return customerCode;
        }
        int n = 2;
        String candidate;
        do {
            candidate = customerCode + "-" + n;
            n++;
        } while (inUse.contains(candidate));
        return candidate;
    }

    /**
     * Flags the owner being created as a possible (soft) duplicate of an existing owner. A soft match is
     * not a hard duplicate (its identity key is unique, already enforced by {@link #requireUniqueIdentity})
     * but it shares an existing owner's last-name {@linkplain Soundex#encode(String) Soundex} and postcode
     * (both present and equal) while carrying a different identity key. When such an existing owner is
     * found the created owner is flagged ({@code possibleDuplicate} true) with {@code possibleDuplicateOf}
     * set to that owner's id; the lowest matching id is used when several owners match. Otherwise the flag
     * is false and {@code possibleDuplicateOf} is left unset.
     *
     * <p>A declared household member (one created with {@code sharesHousehold}) is never a suspected
     * duplicate: it has deliberately opted in to sharing its household, so the flag is forced false.
     *
     * @param owner the owner being created, with its normalized telephone, email, last name and postcode set
     * @param declaredHouseholdMember whether the owner opted in to its household via {@code sharesHousehold}
     */
    private void flagPossibleDuplicate(Owner owner, boolean declaredHouseholdMember) {
        if (declaredHouseholdMember || owner.getPostcode() == null) {
            owner.setPossibleDuplicate(false);
            return;
        }
        String identityKey = owner.getIdentityKey();
        String soundex = Soundex.encode(owner.getLastName());
        Integer matchId = this.clinicService.findAllOwners().stream()
            .filter(existing -> soundex.equals(Soundex.encode(existing.getLastName()))
                && owner.getPostcode().equals(existing.getPostcode())
                && !identityKey.equals(existing.getIdentityKey()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    /**
     * Stamps the household's member count after this create ({@code householdSize}) onto every member of
     * the household - the existing owners that share the deterministic {@code householdId} and the new
     * one - so the shared size can be read back from any of them. The count is the number of existing
     * members plus the owner being created.
     *
     * @param owner the owner being created, with its deterministic {@code householdId} already set
     */
    private void stampHouseholdSize(Owner owner) {
        List<Owner> members = householdMembers(owner);
        int householdSize = members.size() + 1;
        owner.setHouseholdSize(householdSize);
        for (Owner member : members) {
            member.setHouseholdSize(householdSize);
            this.clinicService.saveOwner(member);
        }
    }

    /**
     * The existing owners that belong to the same household as the given owner - those that share its
     * deterministic {@code householdId}. This is the single definition of "who is in this household",
     * shared by the household-size stamp and the membership-level cap. The owner being created is not
     * among them (it has not been saved yet); soft-deleted owners are included, so callers that need
     * only live members filter them out themselves.
     *
     * @param owner the owner being created, with its deterministic {@code householdId} already set
     * @return the existing same-household owners
     */
    private List<Owner> householdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
    }

    /**
     * Caps the new owner's membership level so it cannot exceed one above the current maximum
     * membership level among the existing members of its household - the non-deleted owners that share
     * the deterministic {@code householdId}. The new owner's level is lowered to that ceiling only when
     * its derived level sits above it; otherwise it is left to be derived normally. When the household
     * has no existing member no cap applies and the level is left untouched.
     *
     * @param owner the owner being created, with its deterministic {@code householdId} and
     *     {@code householdSize} already set
     */
    private void capMembershipLevelToHousehold(Owner owner) {
        OptionalInt maxMemberLevel = householdMembers(owner).stream()
            .filter(existing -> !existing.isDeleted())
            .mapToInt(Owner::getMembershipLevel)
            .max();
        if (maxMemberLevel.isEmpty()) {
            return;
        }
        int cap = maxMemberLevel.getAsInt() + 1;
        if (owner.getMembershipLevel() > cap) {
            owner.setMembershipLevel(cap);
        }
    }

    /**
     * Computes the deterministic, shared household identifier for a last name and postcode: the first
     * 12 upper-case hexadecimal characters (the first 6 bytes) of the SHA-256 digest of
     * {@code normalizedLastName + '|' + postcode}. The last name is normalized (case-insensitive,
     * whitespace-collapsed) and an absent postcode contributes an empty string, so every owner that
     * shares a last name and postcode computes the same identifier and therefore the same household.
     *
     * @param lastName the last name of the household
     * @param postcode the postcode of the household, possibly {@code null}
     * @return the 12-character upper-case hex household identifier
     */
    private String computeHouseholdId(String lastName, String postcode) {
        String key = normalizeForHousehold(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Normalizes a value for case-insensitive, whitespace-collapsed household comparison: leading and
     * trailing whitespace is trimmed, any internal run of whitespace is collapsed to a single space,
     * and the result is lower-cased. A {@code null} value normalizes to an empty string.
     *
     * @param value the raw value, possibly {@code null}
     * @return the normalized value
     */
    private String normalizeForHousehold(String value) {
        return collapseWhitespace(value).toLowerCase(Locale.ROOT);
    }

    /**
     * Trims leading and trailing whitespace and collapses any internal run of whitespace to a single
     * space, giving the single definition of that step shared by the owner normalizers. A {@code null}
     * value yields an empty string.
     *
     * @param value the raw value, possibly {@code null}
     * @return the trimmed, whitespace-collapsed value
     */
    private String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Normalizes an owner's address to its canonical, stored-and-returned form: leading and trailing
     * whitespace is trimmed, any internal run of whitespace is collapsed to a single space, the result
     * is upper-cased, and common street-type abbreviations are expanded as whole words
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} or
     * blank-after-collapsing value normalizes to an empty string. The transformation is idempotent, so
     * applying it to an already-normalized (or legacy) value is safe when comparing addresses.
     *
     * @param address the raw address value, possibly {@code null}
     * @return the normalized address
     */
    private String normalizeAddress(String address) {
        String collapsed = collapseWhitespace(address).toUpperCase(Locale.ROOT);
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

    /**
     * Applies the owner's address from the request, preferring the structured form over the flat one.
     * When a non-blank {@code addressLine1} is supplied it is normalized ({@link #normalizeAddress}) and
     * stored, along with the normalized {@code addressLine2} when present, and the stored {@code address}
     * is the composed string ({@link #composeAddress}). Otherwise the flat {@code address} input is
     * normalized and stored, and the structured lines are cleared. Normalization therefore applies to
     * whichever address fields are supplied, and everything that reads {@link Owner#getAddress()} sees
     * the structured address when present, falling back to the flat address.
     *
     * @param owner the owner being created
     * @param ownerFieldsDto the submitted owner fields
     */
    private void applyAddress(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        String line1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
        if (StringUtils.hasText(line1)) {
            String line2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
            String normalizedLine2 = StringUtils.hasText(line2) ? line2 : null;
            owner.setAddressLine1(line1);
            owner.setAddressLine2(normalizedLine2);
            owner.setAddress(composeAddress(line1, normalizedLine2));
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        }
    }

    /**
     * Composes the stored-and-returned {@code address} string from the normalized structured lines: the
     * normalized {@code addressLine1}, with a single space and the normalized {@code addressLine2}
     * appended when the second line is present.
     *
     * @param line1 the normalized first address line
     * @param line2 the normalized second address line, or {@code null} when absent
     * @return the composed address string
     */
    private String composeAddress(String line1, String line2) {
        return line2 == null ? line1 : line1 + " " + line2;
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
