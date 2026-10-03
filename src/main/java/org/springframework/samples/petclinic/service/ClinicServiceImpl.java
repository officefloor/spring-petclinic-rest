/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Mostly used as a facade for all Petclinic controllers
 * Also a placeholder for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Service
public class ClinicServiceImpl implements ClinicService {

    /** Dedicated audit channel; create side-effects are published here for auditing. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Dedicated notification channel; welcome notifications are enqueued here on successful create. */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /** Monotonically increasing sequence number stamped onto each structured owner-created event. */
    private static final AtomicLong OWNER_CREATED_SEQ = new AtomicLong(0);

    /** Serializes the structured audit events emitted on the {@code AUDIT} channel. */
    private static final ObjectMapper AUDIT_MAPPER = new ObjectMapper();

    /**
     * When more owners than this have already been created on a given day (by {@code
     * registrationDate}), a newly created owner is flagged with {@code bulkSignupWarning}.
     */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80L;

    /**
     * When a new owner's city already holds at least this many owners (but fewer than the hard
     * {@link CityOwnerLimitExceededException#MAX_OWNERS_PER_CITY} limit), the owner is created but
     * flagged with {@code capacityWarning} to signal the city is approaching capacity.
     */
    private static final long CITY_CAPACITY_WARNING_THRESHOLD = 40L;

    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;
    private final VisitRepository visitRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PetTypeRepository petTypeRepository;

    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Pet> findAllPets() throws DataAccessException {
        return petRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pet> findPets(Pageable pageable) throws DataAccessException {
        return petRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void deletePet(Pet pet) throws DataAccessException {
        petRepository.delete(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public Visit findVisitById(int visitId) throws DataAccessException {
        return findEntityById(() -> visitRepository.findById(visitId));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Visit> findAllVisits() throws DataAccessException {
        return visitRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteVisit(Visit visit) throws DataAccessException {
        visitRepository.delete(visit);
    }

    @Override
    @Transactional(readOnly = true)
    public Vet findVetById(int id) throws DataAccessException {
        return findEntityById(() -> vetRepository.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Vet> findAllVets() throws DataAccessException {
        return vetRepository.findAll();
    }

    @Override
    @Transactional
    public void saveVet(Vet vet) throws DataAccessException {
        vetRepository.save(vet);
    }

    @Override
    @Transactional
    public void deleteVet(Vet vet) throws DataAccessException {
        vetRepository.delete(vet);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findAllOwners() throws DataAccessException {
        return ownerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Owner> findOwners(String lastName, Pageable pageable) throws DataAccessException {
        if (lastName != null) {
            return ownerRepository.findByLastName(lastName, pageable);
        }
        return ownerRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void deleteOwner(Owner owner) throws DataAccessException {
        // Soft-delete: flag the owner deleted and retain the row rather than removing it. The owner
        // stays readable, but is ignored by the create endpoint's duplicate and identity checks.
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public PetType findPetTypeById(int petTypeId) {
        return findEntityById(() -> petTypeRepository.findById(petTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<PetType> findAllPetTypes() throws DataAccessException {
        return petTypeRepository.findAll();
    }

    @Override
    @Transactional
    public void savePetType(PetType petType) throws DataAccessException {
        petTypeRepository.save(petType);
    }

    @Override
    @Transactional
    public void deletePetType(PetType petType) throws DataAccessException {
        petTypeRepository.delete(petType);
    }

    @Override
    @Transactional(readOnly = true)
    public Specialty findSpecialtyById(int specialtyId) {
        return findEntityById(() -> specialtyRepository.findById(specialtyId));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Specialty> findAllSpecialties() throws DataAccessException {
        return specialtyRepository.findAll();
    }

    @Override
    @Transactional
    public void saveSpecialty(Specialty specialty) throws DataAccessException {
        specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    public void deleteSpecialty(Specialty specialty) throws DataAccessException {
        specialtyRepository.delete(specialty);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<PetType> findPetTypes() throws DataAccessException {
        return petRepository.findPetTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public Owner findOwnerById(int id) throws DataAccessException {
        return findEntityById(() -> ownerRepository.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Pet findPetById(int id) throws DataAccessException {
        return findEntityById(() -> petRepository.findById(id));
    }

    @Override
    @Transactional
    public void savePet(Pet pet) throws DataAccessException {
        pet.setType(findPetTypeById(pet.getType().getId()));
        petRepository.save(pet);
    }

    @Override
    @Transactional
    public void saveVisit(Visit visit) throws DataAccessException {
        visitRepository.save(visit);

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Vet> findVets() throws DataAccessException {
        return vetRepository.findAll();
    }

    @Override
    @Transactional
    public void saveOwner(Owner owner) throws DataAccessException {
        boolean isNew = owner.isNew();
        // Resolve the effective registration date (supplied, or defaulted to the server date) and
        // roll it forward to the next business day so it never lands on a weekend. Everything
        // derived from the registration date below must use this adjusted value.
        LocalDate registrationDate = toBusinessDay(
            owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now());
        owner.setRegistrationDate(registrationDate);
        if (owner.isNew()) {
            // The householdId is deterministic: the first 12 hex characters of SHA-256 over
            // (normalizedLastName | postcode), so owners with the same last name and postcode share
            // it automatically. It no longer feeds the identityKey, but is still reported and drives
            // the household member count and level cap below.
            owner.setHouseholdId(computeHouseholdId(owner));
            // The household is keyed on (last name, postcode), so the members are the existing owners
            // sharing both with this owner.
            List<Owner> householdMembers = findHouseholdMembers(owner);
            // Size of the household after this create: the existing members plus this new owner.
            owner.setHouseholdMemberCount(householdMembers.size() + 1);
            // A new owner joining an existing household has its derived membershipLevel capped at one
            // above the current maximum membershipLevel among its household members, so a new member
            // can never leap more than one level past the household. With no existing member no cap
            // applies.
            if (!householdMembers.isEmpty()) {
                int maxLevel = householdMembers.stream()
                    .mapToInt(Owner::getMembershipLevel)
                    .max()
                    .getAsInt();
                owner.setMembershipLevelCap(maxLevel + 1);
            }
        }
        // Single duplicate check: reject only when the new owner's WHOLE identityKey (the SHA-256 of
        // normalizedTelephone | email-or-empty | soundex(lastName)) equals an existing owner's. This
        // is the only 409; there is no separate household-duplicate rejection.
        if (owner.isNew() && isIdentityKeyAlreadyUsed(owner)) {
            throw new DuplicateIdentityException(owner.getIdentityKey());
        }
        // Soft-match: not a hard identityKey duplicate, but shares an existing owner's
        // soundex(lastName) and postcode. Still created, but flagged as a possible duplicate pointing
        // at the matching owner. Because the telephone is part of the identityKey, two owners with the
        // same last name and postcode but different telephones are flagged here rather than rejected.
        if (owner.isNew()) {
            Owner softMatch = findPossibleDuplicate(owner);
            if (softMatch != null) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(softMatch.getId());
            }
        }
        if (owner.isNew()) {
            long ownersInCity = countOwnersInCity(owner);
            if (ownersInCity >= CityOwnerLimitExceededException.MAX_OWNERS_PER_CITY) {
                throw new CityOwnerLimitExceededException(owner.getCity());
            }
            // Between 40 and 49 existing owners (inclusive): created, but flagged as approaching the
            // per-city capacity limit of 50.
            owner.setCapacityWarning(ownersInCity >= CITY_CAPACITY_WARNING_THRESHOLD);
        }
        if (owner.isNew()
            && countOwnersRegisteredOn(registrationDate) >= DailyOwnerLimitExceededException.MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
        if (owner.isNew() && owner.getMemberId() == null) {
            owner.setMemberId(generateMemberId(owner));
        }
        if (owner.isNew()) {
            owner.setNamesakeCount(countNamesakes(owner));
            owner.setBulkSignupWarning(
                countOwnersRegisteredOn(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD);
        }
        ownerRepository.save(owner);
        if (isNew) {
            AUDIT.info("owner created id={} memberId={} membershipLevel={} registrationDate={}",
                owner.getId(), owner.getMemberId(), owner.getMembershipLevel(),
                owner.getRegistrationDate());
            AUDIT.info(ownerCreatedEvent(owner));
            // Enqueue a welcome notification carrying the owner id and the memberId.
            NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
        }
    }

    /**
     * The schema version of the structured owner-created audit event. Version 2 carries the
     * {@code schemaVersion} field itself and the owner's {@code ownerSegment} recomputed from the
     * version-2 identity, alongside the version-2 {@code memberId}.
     */
    private static final int AUDIT_SCHEMA_VERSION = 2;

    /**
     * Build the immutable structured owner-created event emitted alongside the human-readable audit
     * line and serialize it to JSON. {@code seq} is a process-wide monotonically increasing integer
     * stamped once per create, so the ordering of creates is recoverable from the audit stream.
     *
     * <p>The schema-version-2 event stamps {@code schemaVersion} of 2 and carries the owner's
     * {@code ownerSegment} recomputed from the version-2 identity. It carries the owner's
     * <em>primary identifier</em> under the {@code memberId} field (see {@link #primaryIdentifier(Owner)}),
     * the unified version-2 member id that replaced the former customerCode and membershipNumber.
     */
    private String ownerCreatedEvent(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(
            AUDIT_SCHEMA_VERSION, OWNER_CREATED_SEQ.incrementAndGet(), owner.getId(),
            primaryIdentifier(owner), owner.getMembershipLevel(), owner.getOwnerSegment(),
            "OWNER_CREATED");
        return AUDIT_MAPPER.writeValueAsString(event);
    }

    /**
     * The owner's primary identifier carried by audit events: the unified {@code memberId}. The
     * single source of truth lives here so every emitted event references the same identifier.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Immutable structured owner-created audit event (schema version 2). Serialized to the JSON object
     * {@code {schemaVersion:2, seq, ownerId, memberId, membershipLevel, ownerSegment,
     * event:'OWNER_CREATED'}}; the {@code memberId} field carries the owner's version-2 primary
     * identifier and {@code ownerSegment} the owner segment recomputed from the version-2 identity.
     */
    private record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {
    }

    /**
     * Report whether {@code owner}'s derived {@code identityKey} is already used by any existing
     * (non-deleted) owner. The key is the SHA-256 of telephone, email and {@code soundex(lastName)}
     * combined, so two owners collide only when their whole key is equal (see
     * {@link Owner#getIdentityKey()}).
     */
    private boolean isIdentityKeyAlreadyUsed(Owner owner) {
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    /**
     * Find an existing owner this (new) owner soft-matches against: one sharing the same
     * {@code soundex(lastName)} and postcode, but carrying a different {@code identityKey}. Returns
     * the lowest-id such owner, or {@code null} when the owner has no last name or postcode to match
     * on, or nothing matches. The caller only invokes this once the hard {@code identityKey}
     * duplicate check has already passed.
     */
    private Owner findPossibleDuplicate(Owner owner) {
        String key = soundexPostcodeKey(owner);
        if (key == null) {
            return null;
        }
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> key.equals(soundexPostcodeKey(existing))
                && !identityKey.equals(existing.getIdentityKey()))
            .min(Comparator.comparing(Owner::getId))
            .orElse(null);
    }

    /**
     * Build the soft-match key, {@code soundex(lastName) + '|' + postcode}: the basis on which a new
     * owner is flagged a possible duplicate of an existing owner. Returns {@code null} when the owner
     * has no last name to encode or no postcode to key on.
     */
    private String soundexPostcodeKey(Owner owner) {
        String soundex = Soundex.encode(owner.getLastName());
        String postcode = owner.getPostcode();
        if (soundex.isEmpty() || postcode == null || postcode.isBlank()) {
            return null;
        }
        return soundex + "|" + postcode;
    }

    /**
     * Count the owners that already exist (before this create) sharing the same first name and last
     * name as {@code owner}, compared case-insensitively. Returns zero when the owner's name is
     * unique among the existing owners.
     */
    private int countNamesakes(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        if (firstName == null || lastName == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Find the owners that already belong to the same household as {@code owner}, i.e. share both its
     * last name and its postcode. Last names are compared case-insensitively with collapsed
     * whitespace; postcodes are compared exactly. This is the same keying used by
     * {@link #computeHouseholdId(Owner)}, so these are exactly the existing owners that carry the
     * owner's computed {@code householdId}. Returns an empty list when the owner has no last name or
     * postcode to match on.
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        String key = householdKey(owner);
        if (key == null) {
            return List.of();
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> key.equals(householdKey(existing)))
            .toList();
    }

    /**
     * Compute an owner's deterministic version-2 {@code householdId}: the first 12 lower-case hex
     * characters of SHA-256 over the UTF-8 bytes of {@code 'V2' + '|' + normalizedLastName + '|' +
     * postcode} (see {@link #householdKey(Owner)}), with the fixed {@link Owner#IDENTITY_VERSION_TAG
     * 'V2'} tag mixed in so the value never coincides with the version-1 householdId. Owners with the
     * same last name and postcode therefore share the same {@code householdId} automatically. Returns
     * {@code null} when the owner has no last name or postcode, so it does not belong to a shared
     * household.
     */
    private String computeHouseholdId(Owner owner) {
        String key = householdKey(owner);
        return key == null ? null
            : Sha256.hexPrefix(Owner.IDENTITY_VERSION_TAG + "|" + key, 6, false);
    }

    /**
     * Build the owner's household key, {@code normalizedLastName + '|' + postcode}, the single value
     * that identifies a household. The last name is normalized case-insensitively with collapsed
     * whitespace (see {@link #normalizeHouseholdKey}) and the postcode is used exactly as stored.
     * Returns {@code null} when the owner has no last name or postcode to key on. This is the shared
     * basis for both {@link #computeHouseholdId(Owner)} and household membership matching, so the two
     * cannot drift apart.
     */
    private String householdKey(Owner owner) {
        String lastName = normalizeHouseholdKey(owner.getLastName());
        String postcode = owner.getPostcode();
        if (lastName == null || postcode == null || postcode.isBlank()) {
            return null;
        }
        return lastName + "|" + postcode;
    }

    /**
     * Normalize a value for case-insensitive, whitespace-insensitive household comparison by
     * trimming, collapsing internal runs of whitespace to a single space and lower-casing it.
     */
    private String normalizeHouseholdKey(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Build an owner's unified version-2 {@code memberId} as {@code '<REGION><FY><HASH8><CHK>'}:
     * REGION the version-2 region code — the region resolved from the owner's postcode (falling back
     * to the owner's city, else {@code 'UNKNOWN'}) with the fixed {@link Owner#IDENTITY_VERSION_TAG
     * 'V2'} tag mixed in (e.g. {@code 'NSWV2'}); FY the two-digit fiscal year of the owner's
     * business-day-adjusted {@code registrationDate} (the fiscal year starting 1 July); HASH8 the
     * first 8 upper-case hex characters of SHA-256 computed over the {@code 'V2'} tag, the owner's
     * normalized telephone and the owner's last name; and CHK a single Luhn check digit computed over
     * the digits of {@code <REGION><FY><HASH8>} (e.g. {@code 'NSWV2271A2B3C4D5'}). The {@code 'V2'}
     * tag mixed into both the region and the hash guarantees the id never coincides with the owner's
     * version-1 memberId. The identity is fully derived from the owner's own attributes.
     * <p>
     * When the derived id collides with an existing owner's {@code memberId}, a {@code '-<n>'}
     * suffix is appended with the smallest {@code n} of 2 or more that makes it unique, so distinct
     * owners always receive distinct member ids.
     */
    private String generateMemberId(Owner owner) {
        // version-2 region code: the owner's plain region (its locality) with the fixed 'V2' tag
        // mixed in (e.g. 'NSWV2'), used only inside the identifier. The reported locality stays plain.
        String region = owner.getLocality() + Owner.IDENTITY_VERSION_TAG;
        String fy = String.format("%02d", Owner.fiscalYearOf(owner.getRegistrationDate()) % 100);
        String core = region + fy + memberHash(owner);
        return deduplicateMemberId(core + luhnCheckDigit(core));
    }

    /**
     * Return {@code base} when no existing owner already uses it as their {@code memberId};
     * otherwise append {@code '-<n>'} with the smallest {@code n} of 2 or more that is not already
     * taken (counting both the bare {@code base} and any earlier {@code '-<n>'} de-duplications).
     */
    private String deduplicateMemberId(String base) {
        Set<String> existing = ownerRepository.findAll().stream()
            .map(Owner::getMemberId)
            .filter(Objects::nonNull)
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

    /**
     * Compute the HASH8 segment of an owner's version-2 {@code memberId}: the first 8 upper-case hex
     * characters of SHA-256 over the UTF-8 bytes of the fixed {@link Owner#IDENTITY_VERSION_TAG 'V2'}
     * tag concatenated with the owner's normalized telephone and the owner's last name.
     */
    private String memberHash(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        return Sha256.hexPrefix(Owner.IDENTITY_VERSION_TAG + telephone + lastName, 4, true);
    }

    /**
     * Compute the single Luhn check digit (0-9) over the digits contained in {@code s}; non-digit
     * characters are ignored. This is the CHK segment appended to {@code <REGION><FY><HASH8>} to
     * form the owner's {@code memberId}.
     */
    private int luhnCheckDigit(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
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
     * Count the owners that already exist (before this create) in the same city as {@code owner},
     * compared case-insensitively.
     */
    private long countOwnersInCity(Owner owner) {
        String city = owner.getCity();
        return ownerRepository.findAll().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
    }

    /**
     * Count the owners that already exist (before this create) whose {@code registrationDate} falls
     * on the given {@code date}. Used to enforce the per-day create cap.
     */
    private long countOwnersRegisteredOn(LocalDate date) {
        return ownerRepository.findAll().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Fixed public holidays that registration dates must never fall on. Together with weekends these
     * are the non-business days skipped by {@link #toBusinessDay(LocalDate)}.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"),
        LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Roll {@code date} forward to the next business day, skipping weekends (Saturday and Sunday) and
     * the fixed {@link #PUBLIC_HOLIDAYS public holidays}. A date already on a business day is returned
     * unchanged. Registration dates are required to fall on a business day.
     */
    private LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
    }

    /**
     * Find the owner originally created under the given {@code Idempotency-Key}, or {@code null} when
     * the key is blank or no (non-deleted) owner carries it. Used by the create endpoint to recognise
     * a repeated create and return the first-created owner instead of creating a duplicate.
     */
    @Override
    @Transactional(readOnly = true)
    public Owner findOwnerByIdempotencyKey(String idempotencyKey) throws DataAccessException {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return ownerRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Visit> findVisitsByPetId(int petId) {
        return visitRepository.findByPetId(petId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Specialty> findSpecialtiesByNameIn(Set<String> names) {
        return findEntityById(() -> specialtyRepository.findSpecialtiesByNameIn(names));
    }

    private <T> T findEntityById(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            // Just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
    }

}
