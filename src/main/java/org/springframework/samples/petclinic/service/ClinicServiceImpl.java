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
import org.springframework.samples.petclinic.util.E164Telephone;
import org.springframework.samples.petclinic.util.Sha256Hex;
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Mostly used as a facade for all Petclinic controllers
 * Also a placeholder for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Service
public class ClinicServiceImpl implements ClinicService {

    /** Dedicated audit trail logger for owner-lifecycle side-effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Serializes structured audit events to compact JSON. */
    private static final ObjectMapper AUDIT_MAPPER = JsonMapper.builder().build();

    /** Monotonically increasing sequence number stamped on each structured owner-create event. */
    private static final AtomicLong OWNER_CREATED_SEQ = new AtomicLong();

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
        Collection<Owner> owners = ownerRepository.findAll();
        populateHouseholdSizes(owners);
        return owners;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Owner> findOwners(String lastName, Pageable pageable) throws DataAccessException {
        Page<Owner> owners = (lastName != null)
            ? ownerRepository.findByLastName(lastName, pageable)
            : ownerRepository.findAll(pageable);
        populateHouseholdSizes(owners.getContent());
        return owners;
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
        Owner owner = findEntityById(() -> ownerRepository.findById(id));
        populateHouseholdSize(owner);
        return owner;
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

    /**
     * Rejects saving {@code owner} when it carries a postcode that is out of range for its city's
     * region. The postcode is optional: a {@code null} postcode is left untouched. When present it
     * is assumed to already be four digits (enforced by request validation); if the owner's city has
     * a known region (see {@link Region}), the postcode must fall within that region's range
     * ({@link Region#postcodeRange(String)}). A city with no known region accepts any 4-digit
     * postcode.
     *
     * @throws InvalidPostcodeException if the postcode is out of range for the city's region
     */
    private void assertPostcodeValid(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String region = Region.forCity(owner.getCity());
        int[] range = region == null ? null : Region.postcodeRange(region);
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, owner.getCity(), region);
        }
    }

    /**
     * Rejects saving {@code owner} when it carries a {@code registrationDate} later than the current
     * server date. The registration date is optional: a {@code null} date is left untouched (it is
     * defaulted to today by {@link #saveOwner(Owner)}). A registration cannot be dated in the future.
     *
     * @throws FutureRegistrationDateException if the supplied registration date is after today
     */
    private void assertRegistrationDateNotFuture(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (registrationDate.isAfter(today)) {
            throw new FutureRegistrationDateException(registrationDate, today);
        }
    }

    /** Email domains rejected as disposable/throwaway addresses. */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Rejects saving {@code owner} when its email's domain is on the disposable-domain blocklist
     * ({@link #DISPOSABLE_EMAIL_DOMAINS}). The email is optional: a {@code null} email is left
     * untouched. The comparison is case-insensitive on the part after the last {@code '@'}; the
     * email has already been lower-cased by {@link #saveOwner(Owner)} before this runs.
     *
     * @throws DisposableEmailDomainException if the email domain is blocklisted
     */
    private void assertEmailDomainAllowed(Owner owner) {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }

    @Override
    @Transactional
    public void saveOwner(Owner owner) throws DataAccessException {
        assertPostcodeValid(owner);
        assertRegistrationDateNotFuture(owner);
        if (owner.getTelephone() != null) {
            owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        }
        if (owner.getEmail() != null) {
            owner.setEmail(normalizeEmail(owner.getEmail()));
        }
        assertEmailDomainAllowed(owner);
        applyAddress(owner);
        LocalDate effectiveDate = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(rollToBusinessDay(effectiveDate));
        if (owner.getCustomerCode() == null) {
            owner.setCustomerCode(generateCustomerCode(owner));
        }
        boolean creating = owner.isNew();
        if (creating) {
            owner.setNamesakeCount(countNamesakes(owner));
            assertDailyCapacity(owner);
            assertCityCapacity(owner);
        }
        assignHouseholdId(owner);
        assertIdentityUnique(owner);
        applySoftMatch(owner);
        if (creating) {
            applyMembershipLevelCap(owner);
        }
        ownerRepository.save(owner);
        populateHouseholdSize(owner);
        if (creating) {
            AUDIT.info(
                "owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), owner.getMembershipNumber());
            AUDIT.info(ownerCreatedEvent(owner));
        }
    }

    /**
     * Builds the immutable structured {@code OWNER_CREATED} audit event as a compact JSON object
     * {@code {seq, ownerId, customerCode, membershipLevel, event}}. The {@code seq} is a
     * process-wide, monotonically increasing integer stamped once per created owner.
     *
     * <p>The identifier field carries the owner's <em>current primary identifier</em>, sourced
     * through {@link #primaryIdentifier(Owner)}; today that is the {@code customerCode}. When the
     * primary identifier is later unified into the {@code memberId}, that single helper changes and
     * the event automatically carries the new identifier.
     */
    private String ownerCreatedEvent(Owner owner) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("seq", OWNER_CREATED_SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("customerCode", primaryIdentifier(owner));
        event.put("membershipLevel", owner.getMembershipLevel());
        event.put("event", "OWNER_CREATED");
        return AUDIT_MAPPER.writeValueAsString(event);
    }

    /**
     * The owner's current primary identifier. Today this is the {@code customerCode}; when the
     * customerCode is later unified into the {@code memberId}, this is the single place to switch
     * so every consumer (notably the structured audit event) follows.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * Build a {@code customerCode} formatted {@code '<REGION>-<HASH8>'} where REGION is the region
     * code derived from the owner's postcode (see {@link Region#regionForPostcode(String)}) and
     * HASH8 is the first 8 upper-case hex characters of the SHA-256 digest over the concatenation of
     * the owner's normalized telephone and {@code lastName}. The telephone is already normalized by
     * {@link #saveOwner(Owner)} before this runs. The {@code '<REGION>-<HASH8>'} layout is read back
     * by {@link Region#regionOfCustomerCode(String)}; keep the two in sync.
     */
    private String generateCustomerCode(Owner owner) {
        String region = Region.regionForPostcode(owner.getPostcode());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String hash8 = Sha256Hex.upperHex(telephone + lastName, 8);
        return deduplicateCustomerCode(owner, region + "-" + hash8);
    }

    /**
     * Returns {@code base} unchanged when no other owner already holds it; otherwise appends
     * {@code '-<n>'} with the smallest {@code n} of 2 or more that makes the result unique among the
     * existing owners' {@code customerCode}s. The {@code owner} being saved is excluded from the
     * comparison so re-saving it does not treat its own code as a collision.
     */
    private String deduplicateCustomerCode(Owner owner, String base) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId()) && existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }
        if (!taken.contains(base)) {
            return base;
        }
        int n = 2;
        while (taken.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /** Maximum number of owners permitted in a single city. */
    private static final long CITY_OWNER_LIMIT = 50L;

    /**
     * Rejects creating {@code owner} when its city already contains {@link #CITY_OWNER_LIMIT} or
     * more owners, so no city can grow beyond the capacity limit.
     *
     * @throws CityOwnerLimitException if the owner's city is already at capacity
     */
    private void assertCityCapacity(Owner owner) {
        if (owner.getCity() == null) {
            return;
        }
        if (countOwnersInCity(owner.getCity()) >= CITY_OWNER_LIMIT) {
            throw new CityOwnerLimitException(owner.getCity(), CITY_OWNER_LIMIT);
        }
    }

    /** Fixed public holidays that the business-day roll skips. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    /**
     * Rolls {@code date} forward to the next business day. A business day is Monday to Friday that is
     * not a listed public holiday; Saturdays, Sundays and public holidays are skipped by advancing
     * one day at a time until a business day is reached. Business days are returned unchanged.
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
            case SUNDAY:
                return true;
            default:
                return false;
        }
    }

    /** Maximum number of owners permitted to be registered on a single day. */
    private static final long DAILY_OWNER_LIMIT = 100L;

    /** More than this many owners registered on a single day raises the bulk-signup warning. */
    private static final long BULK_SIGNUP_THRESHOLD = 80L;

    /**
     * Reports whether more than {@link #BULK_SIGNUP_THRESHOLD} owners have already been registered
     * today, reusing the same daily accumulation as the per-day create limit ({@link
     * #assertDailyCapacity}).
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isBulkSignupWarningActive() throws DataAccessException {
        return countOwnersRegisteredOn(LocalDate.now()) > BULK_SIGNUP_THRESHOLD;
    }

    /**
     * Rejects creating {@code owner} when its {@code registrationDate} (defaulted to today for a new
     * owner) already has {@link #DAILY_OWNER_LIMIT} or more owners registered, so no more than that
     * many owners can be created on any single day.
     *
     * @throws DailyOwnerLimitException if the owner's registration day is already at capacity
     */
    private void assertDailyCapacity(Owner owner) {
        LocalDate date = owner.getRegistrationDate();
        if (date == null) {
            return;
        }
        if (countOwnersRegisteredOn(date) >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitException(date, DAILY_OWNER_LIMIT);
        }
    }

    private long countOwnersRegisteredOn(LocalDate date) {
        return ownerRepository.findAll().stream()
                .filter(existing -> date.equals(existing.getRegistrationDate()))
                .count();
    }

    private long countOwnersInCity(String city) {
        String normalized = normalizeHouseholdValue(city);
        return ownerRepository.findAll().stream()
                .filter(existing -> normalized.equals(normalizeHouseholdValue(existing.getCity())))
                .count();
    }

    /**
     * Counts the existing owners that share both {@code firstName} and {@code lastName} with the
     * given new {@code owner}, compared case-insensitively (with incidental whitespace collapsed).
     * The owner being created is not itself persisted yet, so the result is the number of namesakes
     * that already existed before this create.
     */
    private int countNamesakes(Owner owner) {
        String firstName = normalizeHouseholdValue(owner.getFirstName());
        String lastName = normalizeHouseholdValue(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId())
                    && firstName.equals(normalizeHouseholdValue(existing.getFirstName()))
                    && lastName.equals(normalizeHouseholdValue(existing.getLastName()))) {
                count++;
            }
        }
        return count;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes whichever address fields {@code owner} supplied and derives the effective flat
     * {@code address} from them. The structured form is preferred: when a non-blank
     * {@code addressLine1} is present, {@code address} is set to the normalized {@code addressLine1},
     * with a single space and the normalized {@code addressLine2} appended when {@code addressLine2}
     * is present. Otherwise the flat {@code address} is normalized in place (backward-compatible). The
     * structured fields are also stored in their normalized form. Because everything downstream reads
     * {@link Owner#getAddress()}, the household hash, postcode validation and locality all see the
     * structured value when present and fall back to the flat address otherwise.
     */
    private static void applyAddress(Owner owner) {
        String line1 = normalizeAddress(owner.getAddressLine1());
        String line2 = normalizeAddress(owner.getAddressLine2());
        owner.setAddressLine1(line1);
        owner.setAddressLine2(line2);
        if (line1 != null && !line1.isEmpty()) {
            String composed = (line2 != null && !line2.isEmpty()) ? line1 + " " + line2 : line1;
            owner.setAddress(composed);
        } else if (owner.getAddress() != null) {
            owner.setAddress(normalizeAddress(owner.getAddress()));
        }
    }

    /** Common street-type abbreviations expanded during address normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Canonicalizes a postal address: trims and collapses runs of whitespace into a single space,
     * upper-cases, and expands common street-type abbreviations (ST->STREET, RD->ROAD, AVE->AVENUE)
     * as whole words. Returns {@code null} for a {@code null} input and an empty string when the
     * address is blank once whitespace is stripped, so the required-field check sees the normalized
     * form.
     */
    private static String normalizeAddress(String address) {
        if (address == null) {
            return null;
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

    private static String normalizeTelephone(String telephone) {
        return telephone == null ? null : E164Telephone.toE164(telephone);
    }

    /**
     * Rejects saving {@code owner} when its whole {@link Owner#getIdentityKey() identity key} equals
     * that of another non-deleted owner. The key is the SHA-256 hex over the normalized telephone,
     * lower-cased email and {@code soundex(lastName)}, so it consolidates the former separate
     * telephone, email and household duplicate rules into a single check — only an exact full-key
     * match is a hard duplicate. Owners flagged {@link Owner#isDeleted() deleted} are ignored.
     *
     * @throws DuplicateIdentityException if another owner already has the same identity key
     */
    private void assertIdentityUnique(Owner owner) {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId()) && !existing.isDeleted()
                    && identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }

    /**
     * Flags {@code owner} as a soft match ({@link Owner#isPossibleDuplicate()} /
     * {@link Owner#getPossibleDuplicateOf()}) when its {@link Owner#getIdentityKey() identity key}
     * differs from every existing owner's (so it is not a hard duplicate) yet it shares an existing,
     * non-deleted owner's {@code soundex(lastName)} and {@code postcode}. This is what now admits two
     * owners with the same {@code lastName} and {@code postcode} but different telephones: they are
     * created as a soft match rather than rejected. The first matching owner (by store iteration
     * order) is recorded. Any prior flag is cleared first, and no match is possible when the owner
     * has no postcode or a last name that yields an empty soundex.
     */
    private void applySoftMatch(Owner owner) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        String soundex = Soundex.encode(owner.getLastName());
        String postcode = owner.getPostcode();
        if (soundex.isEmpty() || postcode == null || postcode.isBlank()) {
            return;
        }
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId().equals(owner.getId()) || existing.isDeleted()) {
                continue;
            }
            if (!identityKey.equals(existing.getIdentityKey())
                    && soundex.equals(Soundex.encode(existing.getLastName()))
                    && postcode.equals(existing.getPostcode())) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    /**
     * Assigns {@code owner} its deterministic {@code householdId}: the first 12 upper-case hex
     * characters of the SHA-256 digest over {@code normalizedLastName + '|' + postcode}, so all
     * owners that share a normalized {@code lastName} and {@code postcode} automatically share the
     * same household. Left {@code null} when either the lastName or the postcode is absent, since a
     * household is keyed on both.
     */
    private void assignHouseholdId(Owner owner) {
        String lastName = normalizeHouseholdValue(owner.getLastName());
        String postcode = owner.getPostcode();
        if (lastName.isEmpty() || postcode == null || postcode.isBlank()) {
            owner.setHouseholdId(null);
            return;
        }
        owner.setHouseholdId(generateHouseholdId(lastName, postcode));
    }

    /**
     * Captures the membership-level ceiling for a newly created {@code owner}: its
     * {@link Owner#getMembershipLevel() membership level} may not exceed one above the highest level
     * among the household members (owners sharing its {@code householdId}) that already exist. When
     * the owner belongs to no household, or no existing member shares it, no cap applies and the
     * level is left uncapped. Each existing member's household size is stamped via
     * {@link #populateHouseholdSizes(Collection)} first so its level reflects the household-size
     * membership points.
     *
     * @see Owner#getMembershipLevelCap()
     */
    private void applyMembershipLevelCap(Owner owner) {
        owner.setMembershipLevelCap(null);
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return;
        }
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId()) && !existing.isDeleted()
                    && householdId.equals(existing.getHouseholdId())) {
                members.add(existing);
            }
        }
        if (members.isEmpty()) {
            return;
        }
        populateHouseholdSizes(members);
        int maxLevel = 0;
        for (Owner member : members) {
            maxLevel = Math.max(maxLevel, member.getMembershipLevel());
        }
        owner.setMembershipLevelCap(maxLevel + 1);
    }

    /**
     * Stamps {@code owner} with the number of owners sharing its {@code householdId} (see {@link
     * Owner#getHouseholdSize()}), so presentation code can derive the membership tier without
     * reaching into the data store. A {@code null} owner or one without a household id is left at
     * size 0.
     */
    private void populateHouseholdSize(Owner owner) {
        if (owner == null) {
            return;
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setHouseholdSize(0);
            return;
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }

    /**
     * Stamps every owner in {@code owners} with its household size in a single pass over the data
     * store, so a list/page of owners is counted without a per-owner query.
     *
     * @see #populateHouseholdSize(Owner)
     */
    private void populateHouseholdSizes(Collection<Owner> owners) {
        if (owners.isEmpty()) {
            return;
        }
        Map<String, Integer> sizes = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            String householdId = existing.getHouseholdId();
            if (householdId != null && !householdId.isBlank()) {
                sizes.merge(householdId, 1, Integer::sum);
            }
        }
        for (Owner owner : owners) {
            String householdId = owner.getHouseholdId();
            owner.setHouseholdSize(householdId == null ? 0 : sizes.getOrDefault(householdId, 0));
        }
    }

    /**
     * Derives the deterministic {@code householdId} from the normalized {@code lastName} and
     * {@code postcode}: the first 12 upper-case hex characters of SHA-256 over
     * {@code lastName + '|' + postcode}, so all owners in the same household share the same
     * identifier.
     */
    private static String generateHouseholdId(String lastName, String postcode) {
        return Sha256Hex.upperHex(lastName + "|" + postcode, 12);
    }

    /**
     * Normalizes a household comparison value by trimming, collapsing runs of whitespace into a
     * single space and lower-casing, so comparisons ignore case and incidental whitespace.
     */
    private static String normalizeHouseholdValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        Collection<Owner> owners = ownerRepository.findByLastName(lastName);
        populateHouseholdSizes(owners);
        return owners;
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
