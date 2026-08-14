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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

    /** Dedicated audit trail for owner-lifecycle side effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Monotonically increasing sequence stamped onto each {@code OWNER_CREATED} audit
     * event, so consumers can order and de-duplicate events across the lifetime of the
     * application. Shared across all creates; never reset.
     */
    private static final AtomicInteger CREATE_SEQUENCE = new AtomicInteger();

    /** Serializer for the structured audit event; records serialize by their components. */
    private static final ObjectMapper AUDIT_MAPPER = JsonMapper.builder().build();

    /**
     * Immutable structured audit event emitted on owner creation. Its component order
     * ({@code seq}, {@code ownerId}, {@code memberId}, {@code membershipLevel},
     * {@code event}) is also the field order of the serialized JSON. The
     * {@code memberId} slot carries the owner's primary identifier.
     */
    private record OwnerCreatedEvent(int seq, Integer ownerId, String memberId,
                                     Integer membershipLevel, String event) {
    }

    /** Email domains from disposable/throwaway providers, rejected on create. */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Request header carrying an idempotency token for owner creation. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers the owner created under each seen {@code Idempotency-Key}, so a repeated
     * create with an already-seen key returns the original owner (200) instead of creating
     * a duplicate. Keyed by the header value, mapped to the created owner's id.
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
        // Idempotent create: if this request carries an 'Idempotency-Key' we have already
        // seen, return the owner originally created under it (200) rather than creating a
        // duplicate. The key is remembered only after a successful create below.
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existingOwner = this.clinicService.findOwnerById(existingId);
                if (existingOwner != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existingOwner), HttpStatus.OK);
                }
            }
        }
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (isDisposableEmailDomain(ownerFieldsDto.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        // The owner must supply an address in EITHER form: a non-blank structured
        // 'addressLine1' or the flat 'address'. getAddress() prefers the structured
        // fields (composing line 1 with an optional line 2) and falls back to the flat
        // value; a blank result means neither form was provided. The composed value is
        // written back so the persisted flat 'address' stays consistent with it.
        String composedAddress = owner.getAddress();
        if (composedAddress == null || composedAddress.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(composedAddress);
        owner.setTelephone(normalizedTelephone);
        if (!owner.isPostcodeValidForCity()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // The household id is deterministic: it is derived purely from the owner's
        // (last name, postcode), so every owner at the same last name and postcode resolves
        // to the same value. It is assigned up front, before any duplicate detection, so the
        // identity key and the household check below both key off it.
        owner.setHouseholdId(householdId(owner.getLastName(), owner.getPostcode()));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // Consolidated hard-duplicate detection: reject only when the new owner's WHOLE
        // identityKey (the SHA-256 over normalizedTelephone|lowerEmail|soundex(lastName))
        // equals an existing owner's. Because the telephone is part of the key, two owners
        // sharing only a last name and postcode differ here whenever their telephone or email
        // differs, and so are both allowed (the second is surfaced as a soft match below). This
        // single identity key is now the only 409 duplicate check — the former separate
        // household-duplicate block keyed on the computed household id no longer applies.
        String identityKey = owner.getIdentityKey();
        if (this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()))) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // The household id is still computed for household-size and membership-level purposes,
        // but sharing it no longer rejects the create.
        List<Owner> householdMembers = findHouseholdMembers(owner.getHouseholdId());
        long ownersInCity = countOwnersInCity(ownerFieldsDto.getCity());
        if (ownersInCity >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        boolean capacityWarning = ownersInCity >= 40 && ownersInCity <= 49;
        if (owner.getRegistrationDate() != null && owner.getRegistrationDate().isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveDate =
            owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        long ownersRegisteredToday = countOwnersRegisteredOn(registrationDate);
        if (ownersRegisteredToday >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        boolean bulkSignupWarning = ownersRegisteredToday > 80;
        HttpHeaders headers = new HttpHeaders();
        owner.setRegistrationDate(registrationDate);
        owner.setMemberId(memberId(owner));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(bulkSignupWarning);
        owner.setCapacityWarning(capacityWarning);
        // The household size (an input to the membership computation) keys off the computed
        // household id: this owner plus everyone already sharing it.
        owner.setHouseholdSize(householdMembers.size() + 1);
        // Level ceiling: a new owner joining a household they did not explicitly declare
        // may not out-rank the household. Cap the derived membership level at one above the
        // current maximum among the existing household members; with no existing member no
        // cap applies.
        if (!sharesHousehold && !householdMembers.isEmpty()) {
            int maxHouseholdLevel = householdMembers.stream()
                .mapToInt(Owner::getMembershipLevel)
                .max()
                .orElse(0);
            owner.setMembershipLevelCap(maxHouseholdLevel + 1);
        }
        // Soft-match ("possible duplicate"): a declared household member is not a suspected
        // duplicate, so only a non-declared create is scored. A non-declared owner whose
        // identity key differs from an existing owner's but whose last name (by Soundex) and
        // postcode match is flagged here as a possible duplicate rather than rejected.
        Owner possibleDuplicate = sharesHousehold ? null : findPossibleDuplicate(owner);
        owner.setPossibleDuplicate(possibleDuplicate != null);
        owner.setPossibleDuplicateOf(possibleDuplicate == null ? null : possibleDuplicate.getId());
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotentCreates.put(idempotencyKey, owner.getId());
        }
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), owner.getMembershipLevel());
        emitOwnerCreatedEvent(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
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
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(normalizedTelephone);
        currentOwner.setEmail(ownerFieldsDto.getEmail());
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
        // Soft-delete: retain the row and flag it deleted rather than removing it. The owner
        // remains readable via GET, and is subsequently ignored by the create endpoint's
        // duplicate and identity checks.
        owner.setDeleted(true);
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


    /**
     * Normalize a telephone number to E.164 form. Spaces, dashes and brackets are
     * stripped. When a leading {@code '+'} and country code are present they are kept;
     * otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
     * is dropped from the national digits. The result must be a {@code '+'} followed
     * by 8 to 15 digits, and is returned as {@code +<digits>}; otherwise {@code null}
     * is returned to signal a bad request.
     */
    /**
     * Build the member id for a newly created owner, formatted
     * {@code '<REGION><FY><HASH8><CHK>'}: {@code REGION} is the region derived from the
     * owner's postcode (falling back to the city, or {@code 'UNKNOWN'}, via the same
     * derivation the locality uses); {@code FY} is the two-digit fiscal year derived from
     * the (business-day-adjusted) registration date; {@code HASH8} is the first eight
     * upper-case hex characters of the SHA-256 of the owner's normalized telephone
     * concatenated with its last name (the same hash used by the region-and-hash
     * identity); and {@code CHK} is a single Luhn check digit computed over the digits of
     * {@code <REGION><FY><HASH8>}. On the rare collision with an existing owner's member
     * id a {@code '-<n>'} suffix is appended.
     */
    private String memberId(Owner owner) {
        String base = memberIdBase(owner);
        java.util.Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        if (!existing.contains(base)) {
            return base;
        }
        int n = 2;
        while (existing.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /**
     * Build the collision-free base of an owner's member id: {@code <REGION><FY><HASH8>}
     * followed by its single Luhn check digit. {@code FY} is the two-digit fiscal year
     * taken from {@link Owner#getFiscalYear()} (its {@code 'FY'} prefix stripped), which
     * is set by the time the member id is assigned.
     */
    private String memberIdBase(Owner owner) {
        String region = owner.getLocality();
        String fiscalYear = owner.getFiscalYear();
        String fy = fiscalYear == null ? "" : fiscalYear.substring(2);
        String hash8 = memberIdHash(owner.getTelephone(), owner.getLastName());
        String core = region + fy + hash8;
        return core + luhnCheckDigit(core);
    }

    /**
     * Compute a single Luhn check digit (0-9) over the digits contained in {@code s};
     * non-digit characters are ignored and the rightmost digit is doubled first,
     * following the standard Luhn algorithm.
     */
    private int luhnCheckDigit(String s) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Emit the immutable structured {@code OWNER_CREATED} audit event for a freshly
     * created owner: a JSON object {@code {seq, ownerId, memberId, membershipLevel,
     * event}} logged to the dedicated {@code AUDIT} trail alongside the human-readable
     * audit line. Each event is stamped with the next value of the shared monotonic
     * sequence, and carries the owner's current primary identifier.
     */
    private void emitOwnerCreatedEvent(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(
            CREATE_SEQUENCE.incrementAndGet(),
            owner.getId(),
            primaryIdentifier(owner),
            owner.getMembershipLevel(),
            "OWNER_CREATED");
        AUDIT.info(AUDIT_MAPPER.writeValueAsString(event));
    }

    /**
     * The owner's primary identifier, as carried by the structured audit event: the
     * unified member id.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Compute the {@code HASH8} portion of a member id: the first eight upper-case
     * hex characters of the SHA-256 digest of {@code telephone + lastName} (each
     * treated as empty when {@code null}), already in their normalized forms.
     */
    private String memberIdHash(String telephone, String lastName) {
        String key = (telephone == null ? "" : telephone) + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Count the existing owners whose city matches the given one, compared after
     * collapsing whitespace and lower-casing. Used to enforce the per-city capacity
     * cap: a city that already holds this many owners is at capacity.
     */
    private long countOwnersInCity(String city) {
        String normalizedCity = normalizeForHousehold(city);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(normalizedCity))
            .count();
    }

    /**
     * Count the existing owners whose registration date falls on the given day. Used to
     * enforce the per-day create limit: once this many owners have been registered today,
     * the day is at capacity and further creates are rejected.
     */
    private long countOwnersRegisteredOn(LocalDate day) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * The fixed public holidays that the business-day roll skips. A registration date
     * landing on one of these is treated like a weekend and advanced to the next
     * non-holiday business day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"),
        LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Roll a registration date forward to the next business day: a date falling on a
     * Saturday, Sunday or listed public holiday is advanced one day at a time until it
     * lands on a non-holiday weekday, while a plain weekday is returned unchanged.
     */
    private LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Find the existing owners that already belong to the same household as the given
     * computed household id. Because the household id is derived deterministically from
     * the owner's (last name, postcode), this is exactly the set of other owners sharing
     * that last name and postcode.
     */
    private List<Owner> findHouseholdMembers(String householdId) {
        if (householdId == null) {
            return List.of();
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
    }

    /**
     * Find an existing owner that this new owner softly duplicates: one whose last name has
     * the same Soundex code and whose postcode matches, but whose whole identity key differs
     * (an equal identity key is a hard duplicate, already rejected above with 409). Returns
     * the lowest-id such owner, or {@code null} when the new owner has no postcode or no soft
     * match exists. Owners flagged deleted are ignored.
     */
    private Owner findPossibleDuplicate(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return null;
        }
        String soundex = Owner.soundex(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> Owner.soundex(existing.getLastName()).equals(soundex)
                && postcode.equals(existing.getPostcode())
                && !identityKey.equals(existing.getIdentityKey()))
            .min(java.util.Comparator.comparing(Owner::getId))
            .orElse(null);
    }

    /**
     * Count the existing owners that already share both the given first and last
     * name, compared case-insensitively. Used to populate an owner's namesake count
     * at creation time, reflecting how many owners with the same name existed before
     * this one was created.
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeName(firstName);
        String normalizedLastName = normalizeName(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeName(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    /**
     * Build the deterministic identifier shared by all owners of one household: the first
     * 12 hex characters of the SHA-256 digest of {@code normalizedLastName + '|' + postcode}.
     * Because it depends only on the (last name, postcode) pair, every owner at the same last
     * name and postcode resolves to the same value regardless of creation order.
     */
    private String householdId(String lastName, String postcode) {
        String key = normalizeForHousehold(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** True when the email's domain is on the disposable-provider blocklist. */
    private boolean isDisposableEmailDomain(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
    }

    /**
     * Read the {@code Idempotency-Key} header from the current request, or {@code null}
     * when it is absent or blank (the generated API signature does not expose it, so it is
     * pulled from the bound request attributes).
     */
    private String currentIdempotencyKey() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        String key = attributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        return (key == null || key.isBlank()) ? null : key;
    }

    private String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
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
     * Validate the national-number length of an E.164 number (the digits, without the
     * leading {@code '+'}) against its country code. Australia ({@code '+61'}) requires
     * exactly 9 national digits and North America ({@code '+1'}) exactly 10; country
     * codes without a known rule are accepted as-is.
     */
    private boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
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
