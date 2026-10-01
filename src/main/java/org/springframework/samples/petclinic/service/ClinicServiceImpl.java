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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
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

    /**
     * Dedicated audit logger. Successful owner creates emit a single line here carrying the new
     * owner's id, member id and registration date.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Dedicated notification logger. A successful owner create enqueues a welcome notification by
     * emitting a single line here carrying the new owner's id and member id.
     */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Monotonically increasing sequence number stamped onto each {@link OwnerCreatedEvent}. Shared
     * across all creates so the emitted events form a strictly ordered, gap-free series.
     */
    private static final AtomicLong OWNER_CREATED_SEQ = new AtomicLong();

    /**
     * Maximum number of owners allowed in a single city. Creating an owner in a city that already
     * contains this many owners is rejected with {@link CityAtCapacityException}.
     */
    static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Threshold of owners already in an owner's city (compared case-insensitively) at or above which
     * a newly created owner is flagged with {@code capacityWarning} to signal the city is approaching
     * the {@link #MAX_OWNERS_PER_CITY} hard limit. A city that already contains between this many and
     * {@code MAX_OWNERS_PER_CITY - 1} owners warns; reaching {@code MAX_OWNERS_PER_CITY} is rejected.
     */
    static final int CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Maximum number of owners that may be registered on a single day. Creating an owner on a day
     * that already has this many owners (counted by {@code registrationDate}) is rejected with
     * {@link DailyRegistrationLimitException}.
     */
    static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Threshold of owners registered on a single day (counted by {@code registrationDate}) above
     * which a newly created owner is flagged with {@code bulkSignupWarning}. When more than this
     * many owners have already been created on that day, the new owner's warning is {@code true}.
     */
    static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /** Fixed public holidays that the business-day roll skips over. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;
    private final VisitRepository visitRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PetTypeRepository petTypeRepository;
    private final ObjectMapper objectMapper;

    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository,
        ObjectMapper objectMapper) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
        this.objectMapper = objectMapper;
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
        saveOwner(owner, false);
    }

    /**
     * Save an owner, enforcing the create-time business rules.
     *
     * <p>Duplicate rejection on create is expressed solely through the single {@code identityKey} (the
     * SHA-256 hex over the normalized telephone, lower-cased email and last-name Soundex, see
     * {@link Owner#identityKey(String, String, String)}): {@link #assertIdentityIsUnique(Owner)}
     * rejects an owner whose whole identity key already belongs to another owner. Because the
     * telephone is part of the key, two members of the same household (same last name and postcode)
     * with different telephones have different identity keys and are both allowed; the former separate
     * household-duplicate 409 no longer applies. Such an undeclared household member is created but
     * flagged as a possible (suspected) duplicate when its last-name Soundex and postcode match an
     * existing owner (see {@link #assignPossibleDuplicate(Owner, boolean)}), whereas a declared
     * household member ({@code sharesHousehold} set) is left unflagged; {@code sharesHousehold} now
     * governs only that flagging. This keeps the sole {@code POST /api/owners} duplicate rule in one
     * place and lets household members coexist, which the household membership-level ceiling (see
     * {@link #computeMembershipLevel(Owner)}) relies on to compare a new owner against the existing
     * members.
     */
    @Override
    @Transactional
    public void saveOwner(Owner owner, boolean sharesHousehold) throws DataAccessException {
        boolean isCreate = owner.isNew();
        assertEmailDomainAllowed(owner);
        assertIdentityIsUnique(owner);
        if (owner.isNew()) {
            assertCityHasCapacity(owner);
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        assertRegistrationDateNotInFuture(owner);
        owner.setRegistrationDate(rollToBusinessDay(owner.getRegistrationDate()));
        if (owner.isNew()) {
            assertDailyRegistrationLimit(owner);
            owner.setBulkSignupWarning(computeBulkSignupWarning(owner));
            owner.setCapacityWarning(computeCapacityWarning(owner));
        }
        if (owner.isNew() && owner.getMemberId() == null) {
            owner.setMemberId(generateMemberId(owner));
        }
        if (owner.isNew() && owner.getNamesakeCount() == null) {
            owner.setNamesakeCount(countNamesakes(owner));
        }
        if (owner.isNew() && owner.getHouseholdCount() == null) {
            owner.setHouseholdCount(countHouseholdMembers(owner));
        }
        if (owner.isNew() && owner.getMembershipLevel() == null) {
            owner.setMembershipLevel(computeMembershipLevel(owner));
        }
        if (owner.isNew()) {
            assignPossibleDuplicate(owner, sharesHousehold);
        }
        ownerRepository.save(owner);
        if (isCreate) {
            int membershipLevel = owner.effectiveMembershipLevel();
            AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
            OwnerCreatedEvent event = new OwnerCreatedEvent(OWNER_CREATED_SEQ.incrementAndGet(),
                owner.getId(), owner.primaryIdentifier(), membershipLevel);
            AUDIT.info(objectMapper.writeValueAsString(event));
            NOTIFY.info("welcome notification enqueued id={} memberId={}",
                owner.getId(), owner.getMemberId());
        }
    }

    /**
     * Count the existing owners that, at the moment this owner is created, share its first name
     * and last name (both compared case-insensitively). The owner being saved is excluded by id so
     * it never counts itself. This value is deterministic given the current data, so it is computed
     * once at creation time and stored.
     */
    private Integer countNamesakes(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        if (firstName == null || lastName == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Count the members of this owner's household at the moment it is created, i.e. the number of
     * owners sharing its household id (same last name and postcode, see
     * {@link Owner#householdId(String, String)}) plus this owner itself. An owner with no derivable
     * household id (missing last name or postcode) is counted as a single-member household. The value
     * is deterministic given the current data, so it is computed once at creation time and stored.
     */
    private Integer countHouseholdMembers(Owner owner) {
        String householdId = Owner.householdId(owner.getLastName(), owner.getPostcode());
        if (householdId == null) {
            return 1;
        }
        long others = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> householdId.equals(
                Owner.householdId(existing.getLastName(), existing.getPostcode())))
            .count();
        return (int) others + 1;
    }

    /**
     * Compute a newly created owner's {@code membershipLevel} after applying the household level
     * ceiling, the value stored on the owner so every later read returns the capped level. The
     * owner's own uncapped level is first derived from its membership points (see
     * {@link Owner#membershipLevel(int)} and
     * {@link Owner#membershipPoints(Integer, String, Integer, java.time.LocalDate)}). The ceiling is
     * then one above the current maximum {@code membershipLevel} among the owner's existing household
     * members&mdash;other owners sharing its household id (same last name and postcode, see
     * {@link Owner#householdId(String, String)}), excluding soft-deleted owners and the owner itself.
     * Each level (the owner's own uncapped level and each member's) is read through
     * {@link Owner#effectiveMembershipLevel()}, the single resolver shared with the read-side mapping.
     * The returned level is the smaller of the uncapped level and that ceiling. When the owner has no
     * derivable household id or no existing household member, no ceiling applies and the uncapped
     * level is returned.
     */
    private Integer computeMembershipLevel(Owner owner) {
        int uncapped = owner.effectiveMembershipLevel();
        String householdId = Owner.householdId(owner.getLastName(), owner.getPostcode());
        if (householdId == null) {
            return uncapped;
        }
        Integer maxHouseholdLevel = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> householdId.equals(
                Owner.householdId(existing.getLastName(), existing.getPostcode())))
            .map(Owner::effectiveMembershipLevel)
            .max(Integer::compareTo)
            .orElse(null);
        if (maxHouseholdLevel == null) {
            return uncapped;
        }
        return Math.min(uncapped, maxHouseholdLevel + 1);
    }

    /**
     * Build an owner's {@code memberId} formatted {@code '<REGION><FY><HASH8><CHK>'}, where
     * {@code REGION} is the region code derived from the owner's postcode (or {@code 'UNKNOWN'} when no
     * region can be derived), {@code FY} is the two-digit fiscal year of the registration date,
     * {@code HASH8} is the first eight upper-cased hex characters of SHA-256 over the owner's normalized
     * telephone concatenated with the last name, and {@code CHK} is a single Luhn check digit over the
     * digits of the {@code <REGION><FY><HASH8>} prefix (e.g. {@code 'NSW271A2B3C4D5'}). The base value
     * is deterministic in those fields and carries no sequence number (see
     * {@link Owner#memberId(String, String, String, java.time.LocalDate)}).
     *
     * <p>When that computed id collides with an existing owner's {@code memberId}, this appends
     * {@code '-<n>'} with the smallest {@code n} of 2 or more that makes it unique (e.g. a second owner
     * computing {@code 'NSW271A2B3C4D5'} is stored as {@code 'NSW271A2B3C4D5-2'}), so distinct owners
     * always receive distinct member ids. A {@code null} computed id (missing telephone, last name or
     * registration date) is returned unchanged.
     */
    private String generateMemberId(Owner owner) {
        String base = Owner.memberId(owner.getPostcode(), owner.getTelephone(), owner.getLastName(),
            owner.getRegistrationDate());
        if (base == null) {
            return null;
        }
        Set<String> existing = ownerRepository.findAll().stream()
            .filter(other -> !Objects.equals(other.getId(), owner.getId()))
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
     * Flag a newly created owner as a possible (soft) duplicate of an existing owner. A hard
     * duplicate (an owner sharing this owner's whole {@code identityKey}) has already been rejected by
     * {@link #assertIdentityIsUnique(Owner)} before this runs. This detects the weaker overlap: an
     * existing owner whose identity key <em>differs</em> but whose last-name Soundex (see
     * {@link Owner#soundex(String)}) and postcode both match this owner. When such an owner exists,
     * {@code possibleDuplicate} is set {@code true} and {@code possibleDuplicateOf} to the matching
     * owner's id (the lowest id when several match); otherwise {@code possibleDuplicate} is set
     * {@code false} and {@code possibleDuplicateOf} left unset. In particular, two owners with the same
     * last name and postcode but different telephones are no longer a hard duplicate and surface here
     * as a soft match.
     *
     * <p>A declared household member ({@code sharesHousehold} set) is never a suspected duplicate: it
     * deliberately joins an existing household, so it is left unflagged.
     */
    private void assignPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        if (sharesHousehold) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String identityKey = Owner.identityKey(
            owner.getTelephone(), owner.getEmail(), owner.getLastName());
        String soundex = Owner.soundex(owner.getLastName());
        String postcode = owner.getPostcode();
        Integer match = null;
        if (soundex != null && !soundex.isEmpty() && postcode != null) {
            match = ownerRepository.findAll().stream()
                .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> !identityKey.equals(Owner.identityKey(
                    existing.getTelephone(), existing.getEmail(), existing.getLastName())))
                .filter(existing -> soundex.equals(Owner.soundex(existing.getLastName())))
                .filter(existing -> postcode.equals(existing.getPostcode()))
                .map(Owner::getId)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);
        }
        owner.setPossibleDuplicate(match != null);
        owner.setPossibleDuplicateOf(match);
    }

    /**
     * Enforce that the owner's email domain is not on the disposable-domain blocklist, throwing
     * {@link DisposableEmailDomainException} when it is. The blocklist check is the single source
     * of truth in {@link Owner#isDisposableEmailDomain(String)}; an owner with no email places no
     * constraint.
     */
    private void assertEmailDomainAllowed(Owner owner) {
        if (Owner.isDisposableEmailDomain(owner.getEmail())) {
            throw new DisposableEmailDomainException(owner.getEmail());
        }
    }

    /**
     * Enforce that no <em>other</em> owner already shares this owner's derived identity, throwing
     * {@link DuplicateIdentityException} if one does. Duplicate detection is now the single
     * {@code identityKey}: the SHA-256 hex over the normalized telephone, lower-cased email and the
     * Soundex of the last name (see {@link Owner#identityKey(String, String, String)}). Two owners
     * are a hard duplicate exactly when their whole identity keys are equal.
     *
     * <p>Because the telephone is part of the key, two members of the same household (same last name
     * and postcode) with different telephones have different identity keys and are both allowed —
     * they surface instead as a soft match (see {@link #assignPossibleDuplicate(Owner, boolean)}).
     * The email-domain blocklist is applied first (see {@link #assertEmailDomainAllowed(Owner)}), and
     * soft-deleted owners are ignored. The owner being saved is excluded by id so updating an owner in
     * place never conflicts with itself.
     */
    private void assertIdentityIsUnique(Owner owner) {
        String identityKey = Owner.identityKey(
            owner.getTelephone(), owner.getEmail(), owner.getLastName());
        boolean duplicate = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> !existing.isDeleted())
            .map(existing -> Owner.identityKey(
                existing.getTelephone(), existing.getEmail(), existing.getLastName()))
            .anyMatch(identityKey::equals);
        if (duplicate) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * Enforce that the owner's city has not yet reached its capacity, throwing
     * {@link CityAtCapacityException} when the city (compared case-insensitively) already contains
     * {@link #MAX_OWNERS_PER_CITY} or more owners. The owner being saved is excluded by id.
     */
    private void assertCityHasCapacity(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        long count = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (count >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * Enforce that the owner's registration date is not later than the current server date, throwing
     * {@link FutureRegistrationDateException} when it is. A registration date may not be in the
     * future; a date defaulted to {@link LocalDate#now()} is never in the future, so this check is a
     * harmless no-op for that case.
     */
    private void assertRegistrationDateNotInFuture(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }

    /**
     * Roll a registration date forward to the next business day. A date that falls on a Saturday,
     * Sunday or a listed public holiday is advanced to the following non-holiday weekday; a plain
     * weekday is returned unchanged. This is applied to the effective registration date (whether
     * supplied in the request or defaulted to the server date) so that every value derived from
     * it&mdash;such as the member id's fiscal-year segment and the daily create-limit
     * counting&mdash;uses the adjusted business day.
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Enforce that the owner's registration day has not yet reached its capacity, throwing
     * {@link DailyRegistrationLimitException} when {@link #MAX_OWNERS_PER_DAY} or more owners are
     * already registered on that day (counted by {@code registrationDate}). The owner being saved
     * is excluded by id.
     */
    private void assertDailyRegistrationLimit(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return;
        }
        long count = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new DailyRegistrationLimitException(registrationDate);
        }
    }

    /**
     * Compute whether a newly created owner should carry a bulk-signup warning, i.e. whether more
     * than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created on this owner's
     * registration day (counted by {@code registrationDate}, excluding the owner being saved). Like
     * the daily-limit rule this reflects the state at creation time, so it is computed once and
     * stored on the owner.
     */
    private boolean computeBulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long count = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Compute whether a newly created owner should carry a capacity warning, i.e. whether its city
     * (compared case-insensitively) already contains at least {@link #CAPACITY_WARNING_THRESHOLD} but
     * fewer than {@link #MAX_OWNERS_PER_CITY} owners, signalling the city is approaching its hard
     * capacity limit. The owner being saved is excluded by id, mirroring the counting done by
     * {@link #assertCityHasCapacity(Owner)}. A city with no name never warns. Like the city-capacity
     * rule this reflects the state at creation time, so it is computed once and stored on the owner.
     */
    private boolean computeCapacityWarning(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return false;
        }
        long count = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        return count >= CAPACITY_WARNING_THRESHOLD && count < MAX_OWNERS_PER_CITY;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
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
