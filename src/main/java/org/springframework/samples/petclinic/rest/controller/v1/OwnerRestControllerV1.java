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
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.IdentityKeyDeriver;
import org.springframework.samples.petclinic.mapper.LocalityDeriver;
import org.springframework.samples.petclinic.mapper.MembershipPointsDeriver;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyRegistrationLimitException;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
     * A pragmatic syntactic check for an email address: a non-empty local part, an {@code @},
     * and a dotted domain. Deliberately conservative so obviously malformed values (e.g. a bare
     * word with no {@code @}) are rejected while ordinary addresses are accepted.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Dedicated audit logger; carries owner lifecycle side-effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Marker naming the structured audit event emitted when an owner is created. */
    private static final String OWNER_CREATED_EVENT = "OWNER_CREATED";

    /**
     * Monotonically increasing sequence stamped onto each structured create event, so consumers
     * can order events across creates. Shared across all creates handled by this application.
     */
    private static final AtomicLong CREATE_EVENT_SEQ = new AtomicLong();

    /** Serializes structured audit events to compact JSON. */
    private static final ObjectMapper AUDIT_MAPPER = JsonMapper.builder().build();

    /** Request header carrying the client-supplied idempotency key for creates. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers the owner created for each already-seen {@code Idempotency-Key}, so a create that
     * repeats with the same key returns the originally created owner (200) instead of creating a
     * duplicate. Keyed by the raw header value; the mapped value is the created owner's id.
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
        // Idempotent create: if this request repeats an already-seen 'Idempotency-Key', return the
        // owner created the first time (200) rather than creating a duplicate.
        String idempotencyKey = idempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        // Normalize whichever address form was supplied and fold it into the DTO: the structured
        // 'addressLine1'/'addressLine2' win when a non-blank 'addressLine1' is present, otherwise the
        // flat 'address' is used. The composed value is stored back into 'address' so every reader of
        // the owner's address sees the effective (structured-or-flat) value.
        applyAddress(ownerFieldsDto);
        if (isBlank(ownerFieldsDto.getAddress())) {
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
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> householdId.equals(
                householdId(normalizeHousehold(existing.getLastName()), existing.getPostcode())))
            .toList();
        // A second owner in an existing household is admitted as a household member; their
        // membership level is capped below (level-ceiling rule) rather than rejected.
        // Single, consolidated duplicate check: reject only when the new owner's whole derived
        // identity key (SHA-256 over telephone|email|soundex(lastName)) equals an existing,
        // non-deleted owner's. This single identity key is the only 409; there is no separate
        // household-duplicate rejection keyed off the computed householdId.
        String newLastName = ownerFieldsDto.getLastName();
        String identityKey = IdentityKeyDeriver.identityKey(telephone, email, newLastName);
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(IdentityKeyDeriver.identityKey(
                existing.getTelephone(), existing.getEmail(), existing.getLastName())));
        if (identityInUse) {
            throw new DuplicateIdentityException(
                "an owner with the same identity key already exists");
        }
        // Soft match: not a hard duplicate, but shares a household signature with an existing,
        // non-deleted owner — the same last-name Soundex and the same postcode while the identity
        // key differs (e.g. same last name and postcode but a different telephone). Such an owner
        // is still created (201) but flagged as a possible duplicate of the earliest matching owner
        // so it can be reviewed rather than rejected.
        String newSoundex = IdentityKeyDeriver.soundex(newLastName);
        Integer softMatchOf = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> !identityKey.equals(IdentityKeyDeriver.identityKey(
                existing.getTelephone(), existing.getEmail(), existing.getLastName())))
            .filter(existing -> newSoundex.equals(IdentityKeyDeriver.soundex(existing.getLastName())))
            .filter(existing -> Objects.equals(postcode, existing.getPostcode()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(deduplicateCustomerCode(
            customerCode(owner.getPostcode(), owner.getTelephone(), owner.getLastName())));
        owner.setHouseholdId(householdId);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(bulkSignupWarning);
        // Household size after this create: existing members plus the owner being created.
        owner.setHouseholdSize(householdMembers.size() + 1);
        // Level-ceiling: the new owner's membership level cannot exceed one above the current
        // maximum level among their existing household members. With no existing member no cap
        // applies. The (possibly capped) level is stored so it is returned on create and on read.
        int naturalLevel = MembershipPointsDeriver.membershipLevel(
            MembershipPointsDeriver.membershipPoints(owner));
        int cappedLevel = naturalLevel;
        if (!householdMembers.isEmpty()) {
            int maxMemberLevel = householdMembers.stream()
                .mapToInt(MembershipPointsDeriver::effectiveMembershipLevel)
                .max()
                .orElseThrow();
            cappedLevel = Math.min(naturalLevel, maxMemberLevel + 1);
        }
        owner.setMembershipLevel(cappedLevel);
        // Flag a soft match (same last-name Soundex and postcode as an existing owner, but a
        // different identity key) as a possible duplicate of the earliest such owner; a create
        // with no soft match is left unflagged.
        owner.setPossibleDuplicate(softMatchOf != null);
        owner.setPossibleDuplicateOf(softMatchOf);
        owner.setDeleted(false);
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotentCreates.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={} "
            + "membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel(), ownerDto.getMembershipNumber());
        emitOwnerCreatedEvent(owner, ownerDto);
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
        applyAddress(ownerFieldsDto);
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setAddressLine1(ownerFieldsDto.getAddressLine1());
        currentOwner.setAddressLine2(ownerFieldsDto.getAddressLine2());
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Returns the {@code Idempotency-Key} header of the current request, or {@code null} when the
     * header is absent, blank, or there is no bound request. Read from the request context so the
     * generated {@link OwnersApi#addOwner} signature is left untouched.
     */
    private static String idempotencyKey() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        String key = attrs.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        return isBlank(key) ? null : key;
    }

    /**
     * The fixed public-holiday calendar. A registration date that lands on any of these dates is
     * rolled forward, just as a weekend is, to the next non-holiday business day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Rolls a registration date forward to the next business day: when it falls on a Saturday, a
     * Sunday or a listed public holiday it is advanced one day at a time until a non-holiday weekday
     * is reached; an unaffected weekday is returned unchanged. This is applied to the effective
     * registration date (whether supplied in the request or defaulted to the server date), so every
     * value derived from the registration date sees the adjusted business day.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date)) {
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
     * Folds an owner's supplied address into its canonical stored form on the given DTO, preferring
     * the structured fields when present. When a non-blank {@code addressLine1} is supplied, it and an
     * optional {@code addressLine2} are normalized ({@link #normalizeAddress}) and kept as the
     * structured lines; the flat {@code address} is set to the composed value ({@code addressLine1},
     * plus a single space and {@code addressLine2} when the second line is present). Otherwise the
     * structured lines are cleared and the flat {@code address} is set to the normalized flat input.
     * The resulting {@code address} is the effective value every address reader sees, and is blank
     * only when neither an {@code addressLine1} nor a flat {@code address} was supplied.
     */
    private static void applyAddress(OwnerFieldsDto ownerFieldsDto) {
        String line1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
        String line2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
        if (!isBlank(line1)) {
            ownerFieldsDto.setAddressLine1(line1);
            ownerFieldsDto.setAddressLine2(isBlank(line2) ? null : line2);
            ownerFieldsDto.setAddress(isBlank(line2) ? line1 : line1 + " " + line2);
        } else {
            ownerFieldsDto.setAddressLine1(null);
            ownerFieldsDto.setAddressLine2(null);
            ownerFieldsDto.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        }
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
     * Emits the immutable structured {@code OWNER_CREATED} event to the {@code AUDIT} logger as a
     * compact JSON object {@code {seq, ownerId, customerCode, membershipLevel, event}}. {@code seq}
     * is a process-wide monotonically increasing sequence across creates. The event carries the
     * owner's current primary identifier via {@link #primaryIdentifier(Owner)} — the customerCode
     * today, and whatever supersedes it later — so consumers always see the identifier in force.
     */
    private void emitOwnerCreatedEvent(Owner owner, OwnerDto ownerDto) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("seq", CREATE_EVENT_SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("customerCode", primaryIdentifier(owner));
        event.put("membershipLevel", ownerDto.getMembershipLevel());
        event.put("event", OWNER_CREATED_EVENT);
        AUDIT.info(AUDIT_MAPPER.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier carried by structured audit events. This is the single
     * point that changes when the {@code customerCode} is unified into the {@code memberId}: today it
     * returns the {@code customerCode}, and returning the {@code memberId} here later makes every
     * event carry the {@code memberId} instead, with no other change required.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
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

    /**
     * De-duplicates a computed {@code customerCode} against the codes already assigned to existing
     * owners. If {@code base} is not currently in use it is returned unchanged; otherwise
     * {@code '-<n>'} is appended with the smallest {@code n} of 2 or more that yields a code no
     * existing owner holds (e.g. a second collision on {@code 'NSW-1A2B3C4D'} becomes
     * {@code 'NSW-1A2B3C4D-2'}, a third {@code 'NSW-1A2B3C4D-3'}, and so on).
     */
    private String deduplicateCustomerCode(String base) {
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(code -> code != null)
            .collect(Collectors.toSet());
        if (!existing.contains(base)) {
            return base;
        }
        int n = 2;
        while (existing.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
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
     * Disposable email domains that are rejected outright: an owner whose email domain is one of
     * these is refused with a 400. Compared case-insensitively (the email is lower-cased before the
     * check).
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Email is optional: an absent (or blank) value is left as {@code null}. When a value is
     * present it must be a syntactically valid address, otherwise an {@link InvalidEmailException}
     * is raised (reported to the client as 400). Its domain must not be on the disposable-domain
     * blocklist ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}), otherwise
     * an {@link InvalidEmailException} is likewise raised (400). A valid value is returned lower-cased
     * so it is stored and returned in canonical form.
     */
    private static String normalizeEmail(String email) {
        if (isBlank(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException("email must be a syntactically valid address");
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("email domain is on the disposable-domain blocklist");
        }
        return normalized;
    }
}
