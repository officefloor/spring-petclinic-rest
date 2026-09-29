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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
        ownerRepository.delete(owner);
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
        if (owner.getAddress() != null) {
            owner.setAddress(normalizeAddress(owner.getAddress()));
        }
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
        List<Owner> household = findHouseholdMembers(owner);
        assignHousehold(owner, household);
        assertIdentityUnique(owner);
        ownerRepository.save(owner);
        populateHouseholdSize(owner);
        if (creating) {
            AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel());
        }
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
        String hash8 = sha256UpperHex(telephone + lastName, 8);
        return region + "-" + hash8;
    }

    /**
     * The first {@code chars} upper-case hex characters of the SHA-256 digest of {@code value}'s
     * UTF-8 bytes.
     */
    private static String sha256UpperHex(String value, int chars) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, chars);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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

    /**
     * Rolls {@code date} forward to the next business day when it falls on a weekend: a Saturday or
     * Sunday becomes the following Monday. Business days (Monday to Friday) are returned unchanged.
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.plusDays(2);
            case SUNDAY:
                return date.plusDays(1);
            default:
                return date;
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
     * that of another owner. This single check consolidates the former separate telephone, email and
     * household duplicate rules: because the normalized telephone is part of the key, two members of
     * the same household (same {@code householdId}) with different telephones have different keys and
     * are both allowed — only an exact full-key match is a duplicate.
     *
     * @throws DuplicateIdentityException if another owner already has the same identity key
     */
    private void assertIdentityUnique(Owner owner) {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId()) && identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }

    /**
     * Returns the existing owners that share both {@code lastName} (compared case-insensitively with
     * collapsed whitespace) and the normalized {@code address} with a new {@code owner} — i.e. the
     * members of the household it would join. Returns an empty list for an update to an existing
     * owner or when either household value is blank, so household rules only apply to genuine new
     * members.
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        if (!owner.isNew()) {
            return List.of();
        }
        String lastName = normalizeHouseholdValue(owner.getLastName());
        String address = normalizeHouseholdAddress(owner.getAddress());
        if (lastName.isEmpty() || address.isEmpty()) {
            return List.of();
        }
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId())
                    && lastName.equals(normalizeHouseholdValue(existing.getLastName()))
                    && address.equals(normalizeHouseholdAddress(existing.getAddress()))) {
                household.add(existing);
            }
        }
        return household;
    }

    /**
     * Assigns household membership when {@code owner} joins a non-empty {@code household}: the new
     * owner and every matching existing member that lacks one are given the same stable
     * {@code householdId}, and any updated existing members are persisted. No-op when the household
     * is empty.
     */
    private void assignHousehold(Owner owner, List<Owner> household) {
        if (household.isEmpty()) {
            return;
        }
        String householdId = resolveHouseholdId(owner, household);
        owner.setHouseholdId(householdId);
        for (Owner existing : household) {
            if (existing.getHouseholdId() == null || existing.getHouseholdId().isBlank()) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
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
     * Returns the {@code householdId} shared by a household: the first non-blank identifier already
     * carried by an existing member, or otherwise a freshly derived stable identifier for the
     * joining {@code owner}'s normalized lastName and address.
     */
    private String resolveHouseholdId(Owner owner, List<Owner> household) {
        for (Owner existing : household) {
            if (existing.getHouseholdId() != null && !existing.getHouseholdId().isBlank()) {
                return existing.getHouseholdId();
            }
        }
        return generateHouseholdId(normalizeHouseholdValue(owner.getLastName()),
            normalizeHouseholdAddress(owner.getAddress()));
    }

    /**
     * Derives a stable {@code householdId} formatted {@code 'HH-<12 upper-hex>'} from the SHA-256
     * digest of the normalized {@code lastName} and {@code address}, so all owners in the same
     * household deterministically share the same identifier.
     */
    private static String generateHouseholdId(String lastName, String address) {
        return "HH-" + sha256UpperHex(lastName + "\u0000" + address, 12);
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

    /**
     * Household comparison value for an address: the canonical {@link #normalizeAddress(String)}
     * form (never {@code null}), so duplicate detection and the shared household id compare
     * addresses using the same normalized representation that is stored and returned.
     */
    private static String normalizeHouseholdAddress(String address) {
        String normalized = normalizeAddress(address);
        return normalized == null ? "" : normalized;
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
