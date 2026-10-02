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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitReachedException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityAtCapacityException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.HashUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Syntactic check for an email address: a non-empty local part, a single {@code @}, and a
     * domain containing at least one dot, with no whitespace in any part.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Email domains belonging to disposable/throwaway mail providers. An owner whose email domain
     * (the part after the {@code @}, compared case-insensitively) is listed here is rejected: such
     * addresses are not durable contact details. Entries are stored lower-cased.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /**
     * Dedicated audit logger. On a successful owner create two records are emitted here: a
     * human-readable audit line carrying the new owner's id, customer code, registration date,
     * membership level and membership number, and — separately — an immutable structured
     * {@code OWNER_CREATED} event ({@link OwnerCreatedEvent}) serialized as JSON.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Serializer for the structured {@code OWNER_CREATED} audit event. A record's components are
     * emitted in declaration order, giving the event a stable field layout.
     */
    private static final ObjectMapper AUDIT_MAPPER = JsonMapper.builder().build();

    /**
     * Monotonically increasing sequence stamped onto each structured {@code OWNER_CREATED} event. It
     * advances by one per emitted event across all creates for the application's lifetime, giving the
     * audit stream a total order independent of owner ids or timestamps.
     */
    private static final AtomicInteger OWNER_CREATED_SEQ = new AtomicInteger(0);

    /**
     * Common street-type abbreviations expanded during address normalization. Keys are the
     * upper-cased abbreviation as it appears as a whole whitespace-delimited token; values are the
     * expanded form it is replaced with.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * The maximum number of owners a single city may contain. Once a city holds this many owners it
     * is at capacity and cannot take any more.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * The number of owners a city must already hold before a response starts carrying a
     * {@code capacityWarning}. Once a city holds at least this many owners (but still fewer than the
     * hard {@link #CITY_CAPACITY} limit), it is flagged as approaching capacity.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * The maximum number of owners that may be registered in a single day. Once this many owners
     * already carry today's {@code registrationDate}, the day's quota is exhausted and no further
     * owners can be created that day.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * The number of owners that may be registered on a single business day before a response starts
     * carrying a {@code bulkSignupWarning}. Once <em>more than</em> this many owners already carry the
     * current business day's {@code registrationDate}, that day's signups are flagged as unusually
     * high volume.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Fixed public holidays that the business-day roll skips over. A registration date that lands on
     * one of these dates is rolled forward to the next non-holiday business day, exactly as a weekend
     * date is rolled forward off a Saturday or Sunday.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"),
        LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * E.164 country codes mapped to the exact number of national (significant) digits a telephone for
     * that country must carry. For example {@code '+61'} (Australia) requires 9 national digits and
     * {@code '+1'} (NANP) requires 10. A country code listed here enforces that national-number length;
     * a number whose country code is not listed is accepted at any otherwise-valid E.164 length.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of(
        "61", 9,
        "1", 10);

    /**
     * Fixed region-to-postcode-range lookup used both to validate an owner's optional postcode against
     * the region derived from the owner's city ({@link #regionForCity}) and to derive the postcode's own
     * region for the {@code customerCode} ({@link #regionForPostcode}). Each value is the inclusive
     * {@code {low, high}} 4-digit range for that region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A
     * region that is not listed ({@code 'UNKNOWN'}) carries no range and accepts any 4-digit postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed city-to-region lookup used to validate an owner's optional postcode against the region
     * derived from the owner's city. Any city not listed has region {@code 'UNKNOWN'} and so accepts
     * any 4-digit postcode.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Remembers the owner created for each {@code Idempotency-Key} seen on a create request, keyed by
     * the header value. A repeated create carrying a key already present here is served the originally
     * created owner instead of creating a duplicate, so retries are safe. The map only ever grows with
     * distinct keys and is held for the application's lifetime, consistent with the in-memory, single
     * instance scope of this sample.
     */
    private final Map<String, Integer> idempotentCreateOwnerIds = new ConcurrentHashMap<>();

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
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        applyBulkSignupWarning(ownerDto);
        applyCapacityWarning(ownerDto);
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        ResponseEntity<OwnerDto> replay = replayIdempotentCreate(idempotencyKey);
        if (replay != null) {
            return replay;
        }
        validateRequiredFields(ownerFieldsDto);
        rejectFutureRegistrationDate(ownerFieldsDto.getRegistrationDate());
        LocalDate registrationDate = toBusinessDay(
            ownerFieldsDto.getRegistrationDate() != null ? ownerFieldsDto.getRegistrationDate() : LocalDate.now());
        rejectDailyLimitReached(registrationDate);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        applyAddress(owner, ownerFieldsDto);
        rejectCityAtCapacity(owner.getCity());
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        owner.setTelephone(telephone);
        String email = normalizeEmail(ownerFieldsDto.getEmail());
        owner.setEmail(email);
        owner.setPostcode(validatePostcode(ownerFieldsDto.getPostcode(), regionForCity(owner.getCity())));
        owner.setHouseholdId(computeHouseholdId(owner.getLastName(), owner.getPostcode()));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        rejectDuplicateIdentity(owner);
        applyPossibleDuplicate(owner, sharesHousehold);
        owner.setRegistrationDate(registrationDate);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setCustomerCode(generateCustomerCode(owner.getPostcode(), owner.getTelephone(), owner.getLastName()));
        owner.setMembershipLevel(cappedMembershipLevel(owner));
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            owner.getMembershipLevel(), owner.getMembershipNumber());
        emitOwnerCreatedEvent(owner);
        rememberIdempotentCreate(idempotencyKey, owner.getId());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        applyBulkSignupWarning(ownerDto);
        applyCapacityWarning(ownerDto);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path(OwnerMapper.OWNER_PATH_TEMPLATE).buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Serves a repeated create that carries an {@code Idempotency-Key} already seen on an earlier
     * successful create. When the key has a remembered owner that still exists, the originally created
     * owner is returned with {@code 200 OK} (carrying its {@code Location}) so the retry neither creates
     * a duplicate nor re-runs the create rules. A {@code null} or blank key, an unseen key, or a key
     * whose owner no longer exists yields {@code null}, signalling the caller to perform a normal create.
     *
     * @param idempotencyKey the {@code Idempotency-Key} header value, or {@code null} if absent
     * @return the originally created owner as a {@code 200 OK} response, or {@code null} to create normally
     */
    private ResponseEntity<OwnerDto> replayIdempotentCreate(String idempotencyKey) {
        if (isBlank(idempotencyKey)) {
            return null;
        }
        Integer ownerId = idempotentCreateOwnerIds.get(idempotencyKey);
        if (ownerId == null) {
            return null;
        }
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path(OwnerMapper.OWNER_PATH_TEMPLATE).buildAndExpand(owner.getId()).toUri());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        applyBulkSignupWarning(ownerDto);
        applyCapacityWarning(ownerDto);
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.OK);
    }

    /**
     * Remembers the owner just created under its request's {@code Idempotency-Key}, so a later create
     * repeating that key is served the same owner ({@link #replayIdempotentCreate}). A {@code null} or
     * blank key is ignored, leaving that create non-idempotent.
     *
     * @param idempotencyKey the {@code Idempotency-Key} header value, or {@code null} if absent
     * @param ownerId        the id of the owner that was just created
     */
    private void rememberIdempotentCreate(String idempotencyKey, Integer ownerId) {
        if (!isBlank(idempotencyKey)) {
            idempotentCreateOwnerIds.put(idempotencyKey, ownerId);
        }
    }

    /**
     * Emits the immutable structured {@code OWNER_CREATED} event for a freshly created owner on the
     * {@code AUDIT} logger, serialized as a JSON object {@code {seq, ownerId, customerCode,
     * membershipLevel, event}}. The {@code seq} is drawn from {@link #OWNER_CREATED_SEQ}, advancing by
     * one per create so the events carry a total order. The {@code customerCode} field carries the
     * owner's current primary identifier ({@link #primaryIdentifier}) — the customer code today — so
     * that when a later evolution unifies the customer code into the member id, the event tracks that
     * identifier with no change here.
     *
     * @param owner the owner that was just created and saved
     */
    private void emitOwnerCreatedEvent(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(
            OWNER_CREATED_SEQ.incrementAndGet(),
            owner.getId(),
            primaryIdentifier(owner),
            owner.getMembershipLevel());
        AUDIT.info("{}", AUDIT_MAPPER.writeValueAsString(event));
    }

    /**
     * Returns the owner's current primary identifier — the single value that externally names the
     * owner and is carried by the structured audit event. Today that is the {@code customerCode}; this
     * is the one place to change when a later evolution replaces it (for example when the customer code
     * is unified into the member id), so every consumer of the primary identifier follows automatically.
     *
     * @param owner the owner whose primary identifier is wanted
     * @return the owner's current primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * Immutable structured audit event emitted on a successful owner create. Serialized to JSON on the
     * {@code AUDIT} logger, its component order fixes the field order {@code {seq, ownerId,
     * customerCode, membershipLevel, event}}. The {@code event} discriminator is always
     * {@code 'OWNER_CREATED'}. The {@code customerCode} component holds the owner's current primary
     * identifier rather than necessarily the raw customer-code field, so the event follows the primary
     * identifier if a later evolution changes what that is.
     *
     * @param seq             monotonically increasing sequence across all creates
     * @param ownerId         the new owner's id
     * @param customerCode    the owner's current primary identifier
     * @param membershipLevel the owner's stored membership level
     */
    private record OwnerCreatedEvent(int seq, Integer ownerId, String customerCode, Integer membershipLevel,
                                     String event) {
        private OwnerCreatedEvent(int seq, Integer ownerId, String customerCode, Integer membershipLevel) {
            this(seq, ownerId, customerCode, membershipLevel, "OWNER_CREATED");
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
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
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

    /**
     * Rejects an owner payload that is missing or blank in any required field. Bean Validation
     * already rejects {@code null} values and some formats, but a whitespace-only value can still
     * satisfy a {@code @Size(min = 1)} constraint; this guard catches those remaining cases.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if any required field is missing or blank, carrying the
     *                                     name of every offending field
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (!hasAddress(ownerFieldsDto)) {
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
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Reports whether an existing owner has not been soft-deleted. The create endpoint's duplicate
     * and identity checks consider only live owners, so an owner flagged {@code deleted} no longer
     * blocks a new owner that would otherwise match it.
     *
     * @param owner an existing owner
     * @return {@code true} unless the owner is flagged deleted
     */
    private boolean isNotDeleted(Owner owner) {
        return !Boolean.TRUE.equals(owner.getDeleted());
    }

    /**
     * Rejects a supplied {@code registrationDate} that is later than the current server date. A
     * {@code null} value means the field was omitted and the registration date will default to the
     * server date, so there is nothing to reject. A supplied date on or before today is accepted; the
     * raw supplied value is compared against the server date before any business-day adjustment.
     *
     * @param registrationDate the registration date supplied by the client, or {@code null} if omitted
     * @throws InvalidOwnerFieldsException if the supplied date is later than the server date, yielding a
     *                                     400 Bad Request response
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidOwnerFieldsException(List.of("registrationDate"));
        }
    }

    /**
     * Validates an owner's optional postcode. A {@code null} value means the field was omitted and is
     * left unset, keeping the create contract backward-compatible. When a value is present it must be
     * exactly 4 digits, and when the owner's region (derived from its city, {@link #regionForCity})
     * has a known postcode range in {@link #REGION_POSTCODES} the postcode must fall within that
     * inclusive range. A region with no known range ({@code 'UNKNOWN'}) accepts any 4-digit postcode.
     * The returned value is stored and returned unchanged.
     *
     * @param postcode the raw postcode value submitted by the client, or {@code null} if omitted
     * @param region   the owner's region derived from its city
     * @return the postcode to store, or {@code null} if none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but not a valid 4-digit postcode, or is
     *                                     out of range for the city's region, yielding a 400 response
     */
    private String validatePostcode(String postcode, String region) {
        if (postcode == null) {
            return null;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
        int[] range = REGION_POSTCODES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidOwnerFieldsException(List.of("postcode"));
            }
        }
        return postcode;
    }

    /**
     * Normalizes a telephone number for owner creation into E.164 form. Spaces, dashes and brackets
     * are stripped. When the value carries a leading {@code '+'} the country code it supplies is kept;
     * otherwise the number is treated as a national Australian number, so a single leading {@code '0'}
     * is dropped and the {@code '+61'} country code is prepended. The result must be a {@code '+'}
     * followed by 8 to 15 digits. This E.164 string is what gets stored and returned.
     *
     * @param telephone the raw telephone value submitted by the client
     * @return the telephone in E.164 form, e.g. {@code +61412345678}
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number, yielding a
     *                                     400 response
     */
    private String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Converts a raw telephone value to E.164 form, or returns {@code null} if it cannot form a valid
     * E.164 number. Spaces, dashes and brackets are removed; a leading {@code '+'} keeps the supplied
     * country code, while a bare national number has a single leading {@code '0'} dropped and the
     * {@code '+61'} country code prepended. A valid result is a {@code '+'} followed by 8 to 15 digits,
     * and when the supplied country code is one with a known national-number length
     * ({@link #NATIONAL_NUMBER_LENGTHS}, e.g. {@code '+61'} => 9, {@code '+1'} => 10) the national
     * number must have exactly that many digits.
     *
     * @param telephone the raw telephone value, possibly {@code null}
     * @return the E.164 string, or {@code null} if the value is not a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String digits = (hasPlus ? cleaned.substring(1) : cleaned).replaceAll("\\D", "");
        String nationalSignificant = hasPlus
            ? digits
            : "61" + (digits.startsWith("0") ? digits.substring(1) : digits);
        if (!nationalSignificant.matches("[0-9]{8,15}")) {
            return null;
        }
        if (!hasValidNationalNumberLength(nationalSignificant)) {
            return null;
        }
        return "+" + nationalSignificant;
    }

    /**
     * Checks the national-number length of an E.164 number (country code plus national significant
     * digits, no leading {@code '+'}) against the per-country requirement in
     * {@link #NATIONAL_NUMBER_LENGTHS}. The longest listed country code that prefixes the number
     * selects the requirement, and the remaining digits must number exactly that many. A number whose
     * country code is not listed carries no per-country length requirement and is accepted.
     *
     * @param nationalSignificant the digits of the E.164 number, country code first, without the
     *                            leading {@code '+'}
     * @return {@code true} if the national-number length is correct for the country code, or the
     *         country code has no listed requirement; {@code false} otherwise
     */
    private boolean hasValidNationalNumberLength(String nationalSignificant) {
        String countryCode = NATIONAL_NUMBER_LENGTHS.keySet().stream()
            .filter(nationalSignificant::startsWith)
            .max(Comparator.comparingInt(String::length))
            .orElse(null);
        if (countryCode == null) {
            return true;
        }
        int nationalLength = nationalSignificant.length() - countryCode.length();
        return nationalLength == NATIONAL_NUMBER_LENGTHS.get(countryCode);
    }

    /**
     * Normalizes an optional owner email address. A {@code null} value means the field was omitted and
     * is left unset. When a value is present it must be a syntactically valid address whose domain is
     * not on the disposable-domain blocklist ({@link #DISPOSABLE_EMAIL_DOMAINS}); the stored and
     * returned value is that address lower-cased.
     *
     * @param email the raw email value submitted by the client, or {@code null} if omitted
     * @return the lower-cased email, or {@code null} if none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but not a syntactically valid address,
     *                                     or its domain is a disposable-mail domain, yielding a 400
     *                                     response
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return normalized;
    }

    /**
     * Counts the existing owners that, before this create, already share the given first name and
     * last name. Both names are compared case-insensitively with surrounding and internal whitespace
     * collapsed, consistent with the other owner-name comparisons. The owner being created has not
     * been saved yet, so it is never included in the count.
     *
     * @param firstName the first name of the owner being created
     * @param lastName  the last name of the owner being created
     * @return the number of existing owners sharing both names
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
     * Builds the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'}. {@code REGION} is
     * the region code derived from the owner's postcode ({@link #regionForPostcode}, {@code 'UNKNOWN'}
     * when the postcode is absent or in no known range), and {@code HASH8} is the first eight
     * upper-cased hex characters of the SHA-256 digest of the owner's normalized telephone followed by
     * the last name (e.g. {@code 'NSW-1A2B3C4D'}). The identity carries no sequence number, so the
     * same postcode region, telephone and last name always yield the same customer code.
     *
     * <p>If the computed code collides with an existing owner's {@code customerCode}, it is
     * de-duplicated by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that makes
     * it unique, so distinct owners always receive distinct customer codes.
     *
     * @param postcode  the owner's validated postcode, or {@code null} if none was supplied
     * @param telephone the owner's normalized (E.164) telephone
     * @param lastName  the owner's last name
     * @return the formatted, de-duplicated customer code
     */
    private String generateCustomerCode(String postcode, String telephone, String lastName) {
        String region = regionForPostcode(postcode);
        String hash8 = hashHex(telephone + lastName, 8);
        String baseCode = region + "-" + hash8;
        Set<String> existingCodes = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (!existingCodes.contains(baseCode)) {
            return baseCode;
        }
        int n = 2;
        while (existingCodes.contains(baseCode + "-" + n)) {
            n++;
        }
        return baseCode + "-" + n;
    }

    /**
     * Resolves the region code for a postcode by its known 4-digit range in {@link #REGION_POSTCODES}
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), returning {@code 'UNKNOWN'}
     * when the postcode is {@code null}, not exactly 4 digits, or falls in no known range. This is the
     * {@code <REGION>} segment baked into the {@code customerCode}.
     *
     * @param postcode the owner's postcode, or {@code null}
     * @return the region code, or {@code 'UNKNOWN'}
     */
    private String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return "UNKNOWN";
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return "UNKNOWN";
    }

    /**
     * Resolves the region code for a city via the fixed {@link #CITY_REGION} table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}), returning {@code 'UNKNOWN'}
     * when the city is not listed. Used to validate an owner's optional postcode against its city's
     * region.
     *
     * @param city the owner's city
     * @return the region code, or {@code 'UNKNOWN'}
     */
    private String regionForCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Returns the first {@code length} upper-cased hex characters of the SHA-256 digest of the given
     * input. Used for the {@code <HASH8>} segment of a {@code customerCode} (length 8) and for the
     * deterministic {@code householdId} (length 12).
     *
     * @param input  the value to hash
     * @param length the number of leading hex characters to return
     * @return the first {@code length} upper-cased hex characters of its SHA-256 digest
     */
    private String hashHex(String input, int length) {
        return HashUtils.sha256Hex(input).substring(0, length).toUpperCase(Locale.ROOT);
    }

    /**
     * Counts the existing owners already in the given city, used to derive the per-city sequence of a
     * {@code customerCode}. Cities are compared case-insensitively with surrounding and internal
     * whitespace collapsed, consistent with the other owner-field comparisons. The owner being created
     * has not been saved yet, so it is never included in the count.
     *
     * @param city the city of the owner being created
     * @return the number of existing owners in that city
     */
    private int countOwnersInCity(String city) {
        String normalizedCity = normalizeForComparison(city);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getCity()).equals(normalizedCity))
            .count();
    }

    /**
     * Rejects creation of an owner whose city already contains the maximum number of owners
     * ({@value #CITY_CAPACITY}). Owners are counted per city case-insensitively with surrounding and
     * internal whitespace collapsed, consistent with the other owner-field comparisons.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityAtCapacityException if the city already contains {@value #CITY_CAPACITY} or more
     *                                      owners, yielding a 409 Conflict response
     */
    private void rejectCityAtCapacity(String city) {
        if (countOwnersInCity(city) >= CITY_CAPACITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }

    /**
     * Rejects creation of an owner once the given business day has already reached its daily quota
     * of {@value #DAILY_OWNER_LIMIT} owners. Owners are counted by their {@code registrationDate}
     * being that day — the adjusted business day the owner being created will carry — so the quota
     * applies per business day. The owner being created has not been saved yet, so it is never
     * included in the count.
     *
     * @param registrationDate the adjusted business-day registration date of the owner being created
     * @throws DailyOwnerLimitReachedException if {@value #DAILY_OWNER_LIMIT} or more owners already
     *                                         carry that registration date, yielding a
     *                                         429 Too Many Requests response
     */
    private void rejectDailyLimitReached(LocalDate registrationDate) {
        if (countOwnersRegisteredOn(registrationDate) >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitReachedException(registrationDate);
        }
    }

    /**
     * Counts the existing owners that carry the given day as their {@code registrationDate} — the same
     * per-business-day bucket the daily create-limit and the bulk-signup warning both accumulate over.
     *
     * @param registrationDate the business day to count owners for
     * @return the number of existing owners registered on that day
     */
    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Sets the {@code bulkSignupWarning} flag on an owner response. The flag is {@code true} when more
     * than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry the current business day's
     * {@code registrationDate} — the business day a create would land on right now — and {@code false}
     * otherwise.
     *
     * @param ownerDto the owner response to flag
     */
    private void applyBulkSignupWarning(OwnerDto ownerDto) {
        LocalDate today = toBusinessDay(LocalDate.now());
        ownerDto.setBulkSignupWarning(countOwnersRegisteredOn(today) > BULK_SIGNUP_WARNING_THRESHOLD);
    }

    /**
     * Sets the {@code capacityWarning} flag on an owner response. The flag is {@code true} when the
     * owner's city already holds between {@value #CITY_CAPACITY_WARNING_THRESHOLD} and
     * {@value #CITY_CAPACITY} minus one owners inclusive — approaching the hard {@link #CITY_CAPACITY}
     * limit — and {@code false} otherwise. Owners are counted per city the same way the capacity
     * rejection counts them ({@link #countOwnersInCity}).
     *
     * @param ownerDto the owner response to flag
     */
    private void applyCapacityWarning(OwnerDto ownerDto) {
        int ownersInCity = countOwnersInCity(ownerDto.getCity());
        ownerDto.setCapacityWarning(
            ownersInCity >= CITY_CAPACITY_WARNING_THRESHOLD && ownersInCity < CITY_CAPACITY);
    }

    /**
     * Rolls a registration date forward to a business day. A date that falls on a Saturday, a Sunday
     * or a listed {@linkplain #PUBLIC_HOLIDAYS public holiday} is rolled forward one day at a time
     * until it reaches the next non-holiday weekday; a date already on a non-holiday weekday is
     * returned unchanged. This applies to the effective registration date whether it was supplied in
     * the request or defaulted to the server date, and the adjusted value is what gets stored as
     * {@code registrationDate} and feeds every value derived from it (such as the membership number's
     * year segment and the daily create-limit's per-day bucket).
     *
     * @param date the effective registration date, supplied or defaulted
     * @return the same date when it is a non-holiday weekday, otherwise the next non-holiday business day
     */
    private LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (adjusted.getDayOfWeek() == DayOfWeek.SATURDAY
            || adjusted.getDayOfWeek() == DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    /**
     * Rejects creation of an owner whose derived {@code identityKey} exactly matches an existing
     * owner's. The identity key — the SHA-256 hex digest of {@code '<normalizedTelephone>|<lowerEmail>
     * |<soundex(lastName)>'} — is the single value that consolidates all duplicate detection: the
     * previously separate telephone, email and household checks are all expressed through it. Because
     * the telephone is part of the key, two owners with the same last name and postcode but different
     * telephones have different identity keys and are both allowed (the second is a soft match); only a
     * whole-key match is a duplicate. The owner being created already carries its normalized telephone,
     * email and last name, and existing owners store the same normalized forms, so the keys are
     * compared directly. Owners flagged deleted are ignored.
     *
     * @param owner the owner being created, with its telephone, email and last name already set
     * @throws DuplicateOwnerIdentityException if another owner already has the same identity key,
     *                                         yielding a 409 Conflict response
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean inUse = this.clinicService.findAllOwners().stream()
            .filter(this::isNotDeleted)
            .map(Owner::getIdentityKey)
            .anyMatch(identityKey::equals);
        if (inUse) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * Flags the owner being created as a possible (soft) duplicate. A possible duplicate is an owner
     * that is not a hard duplicate (its identity key is unique, so it has already passed
     * {@link #rejectDuplicateIdentity}) yet whose {@code soundex(lastName)} and postcode both match an
     * existing owner's. Because the telephone is part of the identity key, two owners that share a last
     * name and postcode but carry different telephones have different identity keys, so the second is
     * created as a soft match rather than rejected. When such a match exists the owner is created with
     * {@code possibleDuplicate} set to {@code true} and {@code possibleDuplicateOf} set to the matching
     * owner's id (the earliest matching owner by id); otherwise {@code possibleDuplicate} is
     * {@code false} and {@code possibleDuplicateOf} is left unset.
     *
     * <p>An owner that declares household membership via {@code sharesHousehold} is never flagged: a
     * declared household member is not a suspected duplicate. An owner without a postcode cannot match
     * on postcode and so is never flagged.
     *
     * @param owner           the owner being created, with its telephone, email, postcode and last
     *                        name already set
     * @param sharesHousehold whether the owner declared itself a member of an existing household
     */
    private void applyPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        owner.setPossibleDuplicate(false);
        if (sharesHousehold) {
            return;
        }
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String soundex = owner.getLastNameSoundex();
        String identityKey = owner.getIdentityKey();
        Owner match = this.clinicService.findAllOwners().stream()
            .filter(this::isNotDeleted)
            .filter(existing -> !identityKey.equals(existing.getIdentityKey())
                && soundex.equals(existing.getLastNameSoundex())
                && postcode.equals(existing.getPostcode()))
            .min(Comparator.comparingInt(Owner::getId))
            .orElse(null);
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    /**
     * Caps the membership level of the owner being created so it cannot exceed one above the current
     * maximum membership level among its existing household members. A household member is a live (not
     * soft-deleted) owner carrying the same computed {@code householdId}. The owner's own derived level
     * ({@link Owner#getDerivedMembershipLevel()}) is returned capped at that maximum plus one; when the
     * owner belongs to no household, or no existing household member is found, no cap applies and the
     * derived level is returned unchanged. The owner being created has not been saved yet, so it is
     * never considered its own household member.
     *
     * @param owner the owner being created, with its household id and level-affecting fields set
     * @return the membership level to store, capped to the household ceiling where one exists
     */
    private Integer cappedMembershipLevel(Owner owner) {
        int derivedLevel = owner.getDerivedMembershipLevel();
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return derivedLevel;
        }
        OptionalInt maxHouseholdLevel = this.clinicService.findAllOwners().stream()
            .filter(this::isNotDeleted)
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .mapToInt(Owner::getMembershipLevel)
            .max();
        if (maxHouseholdLevel.isEmpty()) {
            return derivedLevel;
        }
        return Math.min(derivedLevel, maxHouseholdLevel.getAsInt() + 1);
    }

    /**
     * Computes the owner's deterministic {@code householdId}: the first twelve upper-cased hex
     * characters of the SHA-256 digest of the normalized last name and the postcode joined by a
     * {@code '|'}. Because the value derives only from the last name and postcode, every owner that
     * shares both automatically shares the same household id, so the household is keyed on
     * {@code (lastName, postcode)} with no opt-in required. The last name is normalized for comparison
     * (trimmed, internal whitespace collapsed, lower-cased) so values differing only in case or spacing
     * still land in the same household.
     *
     * @param lastName the owner's last name
     * @param postcode the owner's validated postcode, or {@code null} if none was supplied
     * @return the derived household id, or {@code null} when the owner has no postcode and so belongs
     *         to no postcode-keyed household
     */
    private String computeHouseholdId(String lastName, String postcode) {
        if (postcode == null) {
            return null;
        }
        return hashHex(normalizeForComparison(lastName) + "|" + postcode, 12);
    }

    /**
     * Normalizes a text value for case- and whitespace-insensitive comparison: surrounding
     * whitespace is trimmed, every internal run of whitespace is collapsed to a single space, and
     * the result is lower-cased.
     *
     * @param value the value to normalize, possibly {@code null}
     * @return the normalized value, or an empty string if {@code value} is {@code null}
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner address for storage, display and comparison. Surrounding whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations appearing as whole tokens are expanded
     * ({@code ST} to {@code STREET}, {@code RD} to {@code ROAD}, {@code AVE} to {@code AVENUE}).
     * This normalized value is what gets stored and returned.
     *
     * @param address the raw address value submitted by the client, possibly {@code null}
     * @return the normalized address, or an empty string if {@code address} is {@code null}
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }

    /**
     * Reports whether the submitted owner supplies an address in either accepted form: a non-blank
     * structured {@code addressLine1}, or a non-blank flat {@code address}. The structured form is
     * preferred when present, but either satisfies the address requirement, keeping the create
     * contract backward-compatible with earlier flat-address-only owners.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @return {@code true} if an address is supplied in either form
     */
    private boolean hasAddress(OwnerFieldsDto ownerFieldsDto) {
        return !isBlank(ownerFieldsDto.getAddressLine1())
            || !isBlank(normalizeAddress(ownerFieldsDto.getAddress()));
    }

    /**
     * Applies the owner's address, preferring the structured form over the flat one. When a non-blank
     * {@code addressLine1} is supplied, it and the optional {@code addressLine2} are normalized
     * ({@link #normalizeAddress}) and stored, and the owner's composed {@code address} is set to the
     * normalized {@code addressLine1} with a single space and the normalized {@code addressLine2}
     * appended when {@code addressLine2} is present. Otherwise the flat {@code address} is normalized
     * and stored and the structured lines are left unset. This keeps earlier flat-address owners
     * working while letting everything that reads the address use the composed, normalized value.
     *
     * @param owner          the owner being created
     * @param ownerFieldsDto the submitted owner fields
     */
    private void applyAddress(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        if (!isBlank(ownerFieldsDto.getAddressLine1())) {
            String line1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
            String line2 = isBlank(ownerFieldsDto.getAddressLine2())
                ? null
                : normalizeAddress(ownerFieldsDto.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2);
            owner.setAddress(line2 != null ? line1 + " " + line2 : line1);
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        }
    }
}
