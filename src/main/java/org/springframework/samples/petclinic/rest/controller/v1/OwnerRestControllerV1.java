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
import java.util.concurrent.atomic.AtomicLong;
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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidFieldValueException;
import org.springframework.samples.petclinic.rest.advice.MissingRequiredFieldsException;
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

import tools.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Basic syntactic email check: a non-empty local part, an '@', and a domain containing at least
     * one dot, with no whitespace anywhere. Deliberately permissive but requires the essential shape.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Domains known to hand out disposable/throwaway mailboxes. An owner whose email domain is on
     * this blocklist is rejected: such addresses cannot be relied on for contacting the owner.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Dedicated audit logger; create side-effects are recorded here so they can be observed independently. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Source of the monotonically increasing {@code seq} stamped on each {@link OwnerCreatedEvent}.
     * Incremented once per successful create so the emitted events form a gap-free, strictly increasing
     * sequence across all creates handled by this application instance.
     */
    private static final AtomicLong OWNER_CREATED_SEQUENCE = new AtomicLong();

    /** Request header carrying the client-supplied idempotency key for owner creation. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per already-seen {@code Idempotency-Key}, the id of the owner that create originally
     * produced for it. A repeated create carrying a key found here returns that same owner instead of
     * creating a duplicate, making owner creation idempotent with respect to the key.
     */
    private final Map<String, Integer> ownersByIdempotencyKey = new ConcurrentHashMap<>();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    /** Serializes the structured {@link OwnerCreatedEvent} to JSON for the {@code AUDIT} logger. */
    private final ObjectMapper objectMapper;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 ObjectMapper objectMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.objectMapper = objectMapper;
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
        List<OwnerDto> ownerDtos = owners.stream()
            .map(this::toOwnerDtoWithBulkWarning)
            .toList();
        return new ResponseEntity<>(ownerDtos, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(toOwnerDtoWithBulkWarning(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = this.ownersByIdempotencyKey.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(toOwnerDtoWithBulkWarning(existing), HttpStatus.OK);
                }
            }
        }
        List<String> missingFields = new ArrayList<>();
        requireNonBlank(missingFields, "firstName", ownerFieldsDto.getFirstName());
        requireNonBlank(missingFields, "lastName", ownerFieldsDto.getLastName());
        String normalizedAddressLine1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
        String normalizedAddressLine2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
        String normalizedFlatAddress = normalizeAddress(ownerFieldsDto.getAddress());
        boolean hasStructuredAddress = !normalizedAddressLine1.isEmpty();
        String normalizedAddress = hasStructuredAddress
            ? composeAddress(normalizedAddressLine1, normalizedAddressLine2)
            : normalizedFlatAddress;
        requireNonBlank(missingFields, "address", normalizedAddress);
        requireNonBlank(missingFields, "city", ownerFieldsDto.getCity());
        requireNonBlank(missingFields, "telephone", ownerFieldsDto.getTelephone());
        if (!missingFields.isEmpty()) {
            throw new MissingRequiredFieldsException(missingFields);
        }
        validatePostcode(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        validateRegistrationDate(ownerFieldsDto.getRegistrationDate());
        LocalDate suppliedOrDefaultDate = ownerFieldsDto.getRegistrationDate() != null
            ? ownerFieldsDto.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(suppliedOrDefaultDate);
        requireDailyLimitNotReached(registrationDate);
        requireCityBelowCapacity(ownerFieldsDto.getCity());
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        ownerFieldsDto.setTelephone(normalizedTelephone);
        if (ownerFieldsDto.getEmail() != null) {
            ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizedAddress);
        owner.setAddressLine1(hasStructuredAddress ? normalizedAddressLine1 : null);
        owner.setAddressLine2(hasStructuredAddress && !normalizedAddressLine2.isEmpty()
            ? normalizedAddressLine2 : null);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(buildCustomerCode(owner, normalizedTelephone));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdId(householdIdFor(owner.getLastName(), owner.getPostcode()));
        requireUniqueIdentity(owner);
        markPossibleDuplicate(owner, sharesHousehold);
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            this.ownersByIdempotencyKey.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = toOwnerDtoWithBulkWarning(owner);
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel(), ownerDto.getMembershipNumber());
        OwnerCreatedEvent event = OwnerCreatedEvent.forCreatedOwner(
            OWNER_CREATED_SEQUENCE.incrementAndGet(), owner, ownerDto.getMembershipLevel());
        AUDIT.info(this.objectMapper.writeValueAsString(event));
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Returns the {@code Idempotency-Key} header of the request in progress, or {@code null} when the
     * header is absent, blank, or there is no active servlet request. The generated {@link OwnersApi}
     * signature carries no header parameter, so it is read from the current request context.
     */
    private static String currentIdempotencyKey() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String key = attributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
            if (key != null && !key.isBlank()) {
                return key;
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
        currentOwner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
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

    /**
     * Builds the owner's customer code as {@code '<REGION>-<HASH8>'}, where {@code REGION} is the
     * region derived from the owner's postcode (falling back to its city, else {@code 'UNKNOWN'}) via
     * {@link OwnerMapper#toRegion}, and {@code HASH8} is the first 8 upper-case hex characters of the
     * SHA-256 digest of {@code normalizedTelephone + lastName} (e.g. {@code NSW-1A2B3C4D}).
     *
     * <p>When the computed code collides with an existing owner's {@code customerCode}, it is
     * de-duplicated by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that makes it
     * unique (e.g. {@code NSW-1A2B3C4D-2}); the de-duplicated code is returned. Absent a collision the
     * base region-and-hash code is returned unchanged.
     *
     * @param owner the owner being created, with its region-determining postcode and city populated
     * @param normalizedTelephone the owner's normalized (E.164) telephone, hashed with the last name
     * @return the (de-duplicated) region-and-hash customer code for the owner being created
     */
    private String buildCustomerCode(Owner owner, String normalizedTelephone) {
        String region = ownerMapper.toRegion(owner);
        String hash8 = sha256Hex8(normalizedTelephone + owner.getLastName());
        String baseCode = region + "-" + hash8;
        Set<String> existingCodes = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
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
     * Returns the first 8 upper-case hex characters of the SHA-256 digest of {@code value}'s UTF-8
     * bytes. Deterministic, so the same input always yields the same 8-character hash.
     */
    private static String sha256Hex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Counts the existing owners (those already persisted before this create) whose first and last
     * names both match the incoming owner's, compared case-insensitively. This snapshot is stored on
     * the new owner as its {@code namesakeCount} and echoed back on reads.
     *
     * @param firstName the incoming owner's first name (already validated non-blank)
     * @param lastName the incoming owner's last name (already validated non-blank)
     * @return the number of existing namesakes sharing both names case-insensitively
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Records {@code fieldName} as missing when {@code value} is {@code null} or blank (empty or
     * whitespace-only), so the caller can reject the request listing every offending field.
     */
    private static void requireNonBlank(List<String> missingFields, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missingFields.add(fieldName);
        }
    }

    /**
     * Normalizes a telephone number to E.164 form. Spaces, dashes and brackets are stripped. When the
     * value carries a leading {@code '+'} its country code is kept; otherwise a {@code '+61'} country
     * code is assumed and a single leading {@code '0'} is dropped from the national digits. The result
     * must be a {@code '+'} followed by 8 to 15 digits. For example {@code "0412 345 678"} becomes
     * {@code "+61412345678"}.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized E.164 telephone number (a {@code '+'} followed by 8 to 15 digits)
     * @throws InvalidFieldValueException if the value cannot form a valid E.164 number
     */
    private static String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidFieldValueException("telephone",
                "Telephone must be a valid E.164 number ('+' followed by 8 to 15 digits)");
        }
        return e164;
    }

    /**
     * Converts a raw telephone value to E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. Used both to normalize incoming values and to compare against existing
     * owners' stored numbers when detecting duplicates.
     */
    private static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.trim().replaceAll("[\\s\\-()]", "");
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
        if (!hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length against the country code for the country codes that pin one.
     * A {@code '+61'} (Australia) number must carry exactly 9 national digits and a {@code '+1'}
     * (NANP) number exactly 10; every other country code is accepted on the general 8-to-15-digit
     * rule alone. {@code digits} is the E.164 digit string without the leading {@code '+'}.
     */
    private static boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }

    /**
     * Validates and normalizes an owner's email address. A syntactically valid address is required;
     * the value is returned lower-cased so it is stored and echoed back in canonical form.
     *
     * @param email the raw email value from the request (already known to be non-null)
     * @return the lower-cased email to be stored and returned
     * @throws InvalidFieldValueException if the value is not a syntactically valid email address
     */
    private static String normalizeEmail(String email) {
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidFieldValueException("email", "Email must be a syntactically valid address");
        }
        String canonical = trimmed.toLowerCase(Locale.ROOT);
        String domain = canonical.substring(canonical.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidFieldValueException("email", "Email must not use a disposable-domain address");
        }
        return canonical;
    }

    /** The maximum number of owners permitted in a single city; the {@code (50 + 1)}th is rejected. */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Rejects the request when the owner's city already contains {@link #MAX_OWNERS_PER_CITY} or more
     * owners, comparing city names case-insensitively. Callers reach this only once {@code city} has
     * been validated non-blank.
     *
     * @param city the incoming owner's city (already validated non-blank)
     * @throws CityAtCapacityException if the city is already at or above its owner capacity
     */
    /** The maximum number of owners that may be registered in a single day; the {@code (100 + 1)}th is rejected. */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Rejects the request when {@link #MAX_OWNERS_PER_DAY} or more owners have already been registered
     * on {@code businessDay}, counting existing owners whose {@code registrationDate} equals that date.
     * New owners take {@code businessDay} as their {@code registrationDate} (the supplied or defaulted
     * date rolled forward to a business day), so this caps the number that may be created per day.
     *
     * @param businessDay the adjusted business-day registration date of the owner being created
     * @throws DailyOwnerLimitException if the daily owner-creation limit has already been reached
     */
    /**
     * The number of owners for a single day beyond which the {@code bulkSignupWarning} flag is
     * raised; the warning is true once <em>more than</em> this many owners share a registration date.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Maps {@code owner} to its DTO and sets the {@code bulkSignupWarning} flag: {@code true} when
     * more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created for this
     * owner's registration date, otherwise {@code false}.
     *
     * @param owner the owner to map
     * @return the owner DTO with its bulk-signup warning flag populated
     */
    private OwnerDto toOwnerDtoWithBulkWarning(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(exceedsBulkSignupThreshold(owner.getRegistrationDate()));
        int points = OwnerMapper.membershipPointsFor(owner, householdSize(owner.getHouseholdId()));
        ownerDto.setMembershipPoints(points);
        ownerDto.setMembershipLevel(capMembershipLevel(owner, OwnerMapper.membershipLevelFor(points)));
        return ownerDto;
    }

    /**
     * Caps the owner's {@code membershipLevel} so it cannot exceed one above the current maximum
     * membership level among the other members of its household (existing owners carrying the same
     * {@code householdId}, excluding the owner itself and any soft-deleted owner). When the household
     * has no other member — including when the owner has no {@code householdId} — no cap applies and
     * {@code level} is returned unchanged. Each household member's level is its own base level (points
     * mapped to a level for its real household size), so the cap does not depend on other capped
     * values.
     *
     * @param owner the owner whose membership level is being computed
     * @param level the owner's uncapped membership level derived from its membership points
     * @return the membership level capped at {@code maxHouseholdMemberLevel + 1}, or {@code level}
     *         when the owner has no other household member
     */
    private int capMembershipLevel(Owner owner, int level) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return level;
        }
        Integer maxMemberLevel = this.clinicService.findAllOwners().stream()
            .filter(existing -> !isDeleted(existing))
            .filter(existing -> !java.util.Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .map(this::membershipLevelForOwner)
            .max(Integer::compareTo)
            .orElse(null);
        if (maxMemberLevel == null) {
            return level;
        }
        return Math.min(level, maxMemberLevel + 1);
    }

    /**
     * Computes an existing household member's membership level: its membership points for its real
     * household size mapped to the numeric level. Used to establish the household's current maximum
     * level when capping a new owner's level.
     */
    private int membershipLevelForOwner(Owner owner) {
        int points = OwnerMapper.membershipPointsFor(owner, householdSize(owner.getHouseholdId()));
        return OwnerMapper.membershipLevelFor(points);
    }

    /**
     * Returns the number of owners sharing the given {@code householdId} (the household's size),
     * counting every existing owner that carries the same {@code householdId}. Returns {@code 0} when
     * {@code householdId} is {@code null}, since an owner with no postcode has no household key and so
     * no household.
     *
     * @param householdId the owner's computed household identifier, or {@code null} when absent
     * @return the number of owners in the household
     */
    private int householdSize(String householdId) {
        if (householdId == null) {
            return 0;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * Returns whether more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been
     * created on {@code registrationDate}, counting existing owners whose {@code registrationDate}
     * equals that date. Returns {@code false} when {@code registrationDate} is {@code null}.
     */
    private boolean exceedsBulkSignupThreshold(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        long registeredThatDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return registeredThatDay > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    private void requireDailyLimitNotReached(LocalDate businessDay) {
        long registeredThatDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
            .count();
        if (registeredThatDay >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(MAX_OWNERS_PER_DAY);
        }
    }

    /**
     * Fixed list of public holidays. A registration date landing on one of these dates is rolled
     * forward, just like a weekend, until it reaches a non-holiday business day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Rolls {@code date} forward to a business day: while it falls on a Saturday, Sunday, or a listed
     * public holiday it advances one day at a time until it reaches a non-holiday weekday. Applied to
     * the effective registration date (whether supplied in the request or defaulted to the server date)
     * so a weekend or holiday registration is recorded on the next business day, and every value derived
     * from the registration date uses the adjusted date.
     *
     * @param date the effective registration date (supplied or defaulted), never {@code null}
     * @return the same date, or the next non-holiday business day when {@code date} is a weekend or holiday
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (isWeekend(adjusted) || PUBLIC_HOLIDAYS.contains(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    /** Whether {@code date} falls on a Saturday or Sunday. */
    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /** Matches a well-formed postcode: exactly four decimal digits. */
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("^[0-9]{4}$");

    /**
     * Fixed region-to-postcode range table: each region admits an inclusive 4-digit range
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). Regions are derived
     * from the owner's city via {@link OwnerMapper#CITY_REGION}; a city with no known region is not
     * present here and accepts any well-formed postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's optional {@code postcode}. When absent nothing is checked (postcode is
     * optional, so the request contract stays backward-compatible). When present it must be exactly
     * four digits and, for a city whose region is known, fall within that region's inclusive range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); a city with no known region accepts any 4-digit
     * postcode. A malformed or out-of-range value is rejected with a 400.
     *
     * @param postcode the raw postcode from the request (may be {@code null} when not supplied)
     * @param city the owner's city, used to look up the region whose range constrains the postcode
     * @throws InvalidFieldValueException if the postcode is present but malformed or out of range
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!POSTCODE_PATTERN.matcher(postcode).matches()) {
            throw new InvalidFieldValueException("postcode", "Postcode must be exactly four digits");
        }
        String region = OwnerMapper.CITY_REGION.getOrDefault(city, "UNKNOWN");
        int[] range = REGION_POSTCODES.get(region);
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidFieldValueException("postcode",
                "Postcode must be valid for the city's region " + region);
        }
    }

    /**
     * Validates an owner's optional {@code registrationDate}. When absent nothing is checked (the
     * date defaults to the server date). When present it must not be later than the current server
     * date: a registration cannot be recorded in the future, so a future date is rejected with a 400.
     *
     * @param registrationDate the supplied registration date (may be {@code null} when not supplied)
     * @throws InvalidFieldValueException if the supplied date is later than the server date
     */
    private void validateRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidFieldValueException("registrationDate",
                "Registration date must not be later than the current date");
        }
    }

    private void requireCityBelowCapacity(String city) {
        long cityOwners = this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (cityOwners >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * Rejects the request when the incoming owner's derived {@code identityKey} exactly matches that
     * of any existing owner. The key (the SHA-256 hex digest of {@code normalizedTelephone + '|' +
     * lowerEmail + '|' + soundex(lastName)}) is the single consolidated duplicate-detection key that
     * replaces the former separate telephone, email and household checks. Because the telephone is
     * part of the key, two owners with the same last name and postcode but different telephones have
     * different keys and are both allowed (they surface as a soft match instead); only an exact
     * full-key match is a duplicate. Soft-deleted owners are ignored, and the email-domain blocklist
     * has already been applied when the email was normalized. The key is derived identically for the
     * incoming and existing owners via {@link OwnerMapper#toIdentityKey}.
     *
     * @param owner the incoming owner, with its normalized telephone, email and last name
     *              already populated
     * @throws DuplicateIdentityException if another owner already has this exact identity key
     */
    private void requireUniqueIdentity(Owner owner) {
        String identityKey = ownerMapper.toIdentityKey(owner);
        boolean taken = this.clinicService.findAllOwners().stream()
            .filter(existing -> !isDeleted(existing))
            .map(ownerMapper::toIdentityKey)
            .anyMatch(identityKey::equals);
        if (taken) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * Returns whether an existing owner has been soft-deleted. A soft-deleted owner retains its row
     * but is ignored by the create endpoint's duplicate and identity checks, so a normally-blocking
     * duplicate is allowed when the only matching owner has been deleted.
     */
    private static boolean isDeleted(Owner owner) {
        return Boolean.TRUE.equals(owner.getDeleted());
    }

    /**
     * Flags the incoming owner as a possible (soft) duplicate. Called after the hard-duplicate identity
     * check has passed, so the owner is known not to be a rejected duplicate. When an existing owner
     * has the same {@code soundex(lastName)} and the same {@code postcode} as this owner but a
     * <em>different</em> {@code identityKey}, the owner is a possible duplicate: {@code possibleDuplicate}
     * is set {@code true} and {@code possibleDuplicateOf} to that existing owner's id (the earliest such
     * owner by id when more than one matches). Otherwise both are cleared. A declared household member
     * ({@code sharesHousehold} true) is never flagged: it is a deliberate member, not a suspected
     * duplicate. An owner with no {@code postcode} never matches, since the soft match is keyed on it.
     *
     * @param owner the incoming owner, with its telephone, email and postcode populated
     * @param sharesHousehold whether the request opted in as a declared household member
     */
    private void markPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        if (sharesHousehold) {
            return;
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String soundex = OwnerIdentity.soundex(owner.getLastName());
        String identityKey = ownerMapper.toIdentityKey(owner);
        this.clinicService.findAllOwners().stream()
            .filter(existing -> !isDeleted(existing))
            .filter(existing -> postcode.equals(existing.getPostcode()))
            .filter(existing -> soundex.equals(OwnerIdentity.soundex(existing.getLastName())))
            .filter(existing -> !identityKey.equals(ownerMapper.toIdentityKey(existing)))
            .min(java.util.Comparator.comparing(Owner::getId))
            .ifPresent(match -> {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(match.getId());
            });
    }

    /**
     * Normalizes a value for household-duplicate comparison: trims leading and trailing whitespace,
     * collapses every internal run of whitespace to a single space, and lower-cases the result so
     * comparisons are case-insensitive.
     */
    private static String normalizeForHousehold(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner's address to its canonical stored form: leading and trailing whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded on a whole-word basis
     * ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}). A {@code null} or whitespace-only
     * value normalizes to the empty string, so the required-field check rejects an address that is
     * blank once normalized. For example {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}.
     *
     * @param address the raw address value from the request (may be {@code null})
     * @return the normalized address, or the empty string when blank after normalization
     */
    private static String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            String expanded = switch (token) {
                case "ST" -> "STREET";
                case "RD" -> "ROAD";
                case "AVE" -> "AVENUE";
                default -> token;
            };
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(expanded);
        }
        return sb.toString();
    }

    /**
     * Composes the stored/returned {@code address} from the normalized structured lines: the
     * normalized {@code addressLine1}, with a single space and the normalized {@code addressLine2}
     * appended when {@code addressLine2} is present (non-empty). Both arguments are already
     * normalized via {@link #normalizeAddress(String)}.
     *
     * @param normalizedAddressLine1 the normalized first address line (non-empty)
     * @param normalizedAddressLine2 the normalized second address line, or the empty string when absent
     * @return the composed address string
     */
    private static String composeAddress(String normalizedAddressLine1, String normalizedAddressLine2) {
        if (normalizedAddressLine2.isEmpty()) {
            return normalizedAddressLine1;
        }
        return normalizedAddressLine1 + " " + normalizedAddressLine2;
    }

    /**
     * Derives the stable household identifier for an owner from its last name and postcode as the
     * upper-cased first 12 hex characters of the SHA-256 digest of
     * {@code normalizedLastName + '|' + postcode}, where the last name is normalized (trimmed,
     * internal whitespace collapsed, lower-cased) so casing and spacing do not affect it. Because the
     * identifier is a deterministic function of the normalized last name and postcode, owners that
     * share a last name and postcode share the identifier automatically — no back-fill or opt-in is
     * required. Returns {@code null} when no postcode is present, since the household is keyed on the
     * postcode and there is then nothing to key on.
     *
     * @param lastName the owner's last name (already validated non-blank)
     * @param postcode the owner's postcode (a validated 4-digit value, or {@code null} when absent)
     * @return the 12-hex-character household identifier, or {@code null} when no postcode is present
     */
    private static String householdIdFor(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String normalizedLastName = normalizeForHousehold(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((normalizedLastName + "|" + postcode).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, 12);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
