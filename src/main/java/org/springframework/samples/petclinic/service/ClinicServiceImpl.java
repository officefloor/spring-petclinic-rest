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

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.samples.petclinic.util.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;
    private final VisitRepository visitRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PetTypeRepository petTypeRepository;

    /**
     * Remembers the owner id created for each already-seen {@code Idempotency-Key}, so a repeated
     * create with the same key replays the original owner instead of creating a duplicate.
     */
    private final java.util.Map<String, Integer> idempotencyKeys = new java.util.concurrent.ConcurrentHashMap<>();

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
        // Soft delete: retain the row and flag it deleted rather than removing it. Deleted owners
        // are then ignored by the create endpoint's duplicate and identity checks.
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
        if (owner.getCustomerCode() == null) {
            owner.setCustomerCode(generateCustomerCode(owner));
        }
        ownerRepository.save(owner);

    }

    /** Fixed city-to-region table used to derive an owner's region for its customer code. */
    private static final java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to inclusive 4-digit postcode range {low, high}, preferred over the city table
     *  when deriving an owner's region. */
    private static final java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Builds an owner's customer code formatted {@code '<REGION>-<HASH8>'}, where REGION is the
     * region code derived from the owner's postcode (see {@link #deriveRegion(Owner)}) and HASH8 is
     * the first 8 upper-case hex characters of the SHA-256 digest over the owner's normalized
     * telephone concatenated with its last name (e.g. {@code 'NSW-1A2B3C4D'}). The old per-city
     * sequence numbers have been dropped: the identity is now purely region-and-hash.
     *
     * <p>When the computed code collides with an existing owner's {@code customerCode}, {@code '-<n>'}
     * is appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners
     * always receive distinct customer codes.
     */
    private String generateCustomerCode(Owner owner) {
        String base = String.format("%s-%s", deriveRegion(owner), hash8(owner));
        Set<String> existing = ownerRepository.findAll().stream()
            .map(Owner::getCustomerCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (!existing.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * Derives an owner's region code, preferring the postcode: the region whose inclusive 4-digit
     * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) contains the postcode is used first. When
     * the postcode is absent or in no known range, it falls back to the fixed city-to-region table
     * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), returning {@code 'UNKNOWN'} when the
     * city is not in the table.
     */
    private static String deriveRegion(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("^[0-9]{4}$")) {
            int value = Integer.parseInt(postcode);
            for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Computes an owner's identity hash as the first 8 upper-case hex characters of the SHA-256
     * digest over its normalized telephone concatenated with its last name.
     */
    private static String hash8(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        return HashUtils.sha256UpperHex(telephone + lastName, 8);
    }

    /**
     * Whether any existing owner has the given derived identity key. This is the single basis for
     * duplicate detection: a create is rejected only when the whole key matches an existing owner
     * exactly. See {@link Owner#getIdentityKey()} for the key's definition.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsOwnerWithIdentityKey(String identityKey) throws DataAccessException {
        if (identityKey == null) {
            return false;
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    /**
     * Finds the id of an existing owner that {@code owner} soft-matches (a possible duplicate): one
     * whose surname shares this owner's {@code soundex(lastName)} and whose postcode matches, yet
     * whose identity key differs (so it is not itself a hard duplicate). This is a weaker match than
     * the hard-duplicate identity key, so it is only meaningful for an owner that is not itself a hard
     * duplicate; in particular two owners with the same lastName and postcode but different telephones
     * are soft matches rather than hard duplicates. When several owners match, the one with the lowest
     * id is returned; {@code null} is returned when there is no match (or lastName/postcode is absent),
     * meaning the owner is not a possible duplicate.
     */
    @Override
    @Transactional(readOnly = true)
    public Integer findPossibleDuplicateOwnerId(Owner owner) throws DataAccessException {
        if (owner == null || owner.getLastName() == null || owner.getPostcode() == null) {
            return null;
        }
        String soundex = Owner.soundex(owner.getLastName());
        String postcode = owner.getPostcode();
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> soundex.equals(Owner.soundex(existing.getLastName()))
                && postcode.equals(existing.getPostcode())
                && !Objects.equals(identityKey, existing.getIdentityKey()))
            .map(Owner::getId)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElse(null);
    }

    /**
     * Counts the existing owners whose firstName and lastName match the given values
     * case-insensitively. Used to record an owner's namesake count at creation time.
     */
    @Override
    @Transactional(readOnly = true)
    public int countOwnersByName(String firstName, String lastName) throws DataAccessException {
        if (firstName == null || lastName == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Counts the existing owners whose city matches the given value case-insensitively.
     * Used to enforce the per-city capacity rule at creation time.
     */
    @Override
    @Transactional(readOnly = true)
    public int countOwnersByCity(String city) throws DataAccessException {
        if (city == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
    }

    /**
     * Counts the existing owners whose registration date matches the given value.
     * Used to enforce the per-day owner creation limit.
     */
    @Override
    @Transactional(readOnly = true)
    public int countOwnersByRegistrationDate(LocalDate registrationDate) throws DataAccessException {
        if (registrationDate == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Computes an owner's deterministic household id: the first 12 upper-case hex characters of the
     * SHA-256 digest over the owner's normalized last name, a {@code '|'} separator and its postcode
     * ({@code normalizedLastName + '|' + postcode}). Owners that share a last name and postcode
     * therefore map to the same household id automatically, without any explicit linking. Returns
     * {@code null} when the last name or postcode is absent or blank, since no household can be
     * formed without both.
     */
    @Override
    @Transactional(readOnly = true)
    public String computeHouseholdId(Owner owner) throws DataAccessException {
        if (owner == null) {
            return null;
        }
        String normalizedLastName = normalizeHouseholdField(owner.getLastName());
        String postcode = owner.getPostcode();
        if (normalizedLastName == null || normalizedLastName.isBlank()
            || postcode == null || postcode.isBlank()) {
            return null;
        }
        return HashUtils.sha256UpperHex(normalizedLastName + "|" + postcode, 12);
    }

    /**
     * Counts the owners that belong to the household identified by the given (deterministic)
     * household id, i.e. the number of owners currently sharing it. Used to size a household for
     * the membership-points calculation. Returns {@code 0} when the id is absent.
     */
    @Override
    @Transactional(readOnly = true)
    public int countOwnersByHouseholdId(String householdId) throws DataAccessException {
        if (householdId == null || householdId.isBlank()) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * Returns the existing, non-deleted owners that belong to the household identified by the given
     * (deterministic) household id, i.e. the current members of that household. Used to derive the
     * membership-level ceiling a new owner joining the household is subject to. Returns an empty
     * collection when the id is absent.
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnersByHouseholdId(String householdId) throws DataAccessException {
        if (householdId == null || householdId.isBlank()) {
            return List.of();
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
    }

    /**
     * Computes the membership-level ceiling for a new owner joining the household identified by the
     * given (deterministic) household id: one above the current maximum membershipLevel among the
     * existing (non-deleted) household members. The members' live household size is passed explicitly
     * to {@link Owner#membershipLevel(Integer)}, so no persistent state is mutated. Returns
     * {@code null} when the household has no existing member, so no cap applies to the new owner.
     */
    @Override
    @Transactional(readOnly = true)
    public Integer computeMembershipLevelCap(String householdId) throws DataAccessException {
        Collection<Owner> members = findOwnersByHouseholdId(householdId);
        if (members.isEmpty()) {
            return null;
        }
        int maxLevel = 0;
        for (Owner member : members) {
            int level = member.membershipLevel(members.size());
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
        return maxLevel + 1;
    }

    /**
     * Normalizes a household-identity field for comparison by trimming, collapsing internal
     * runs of whitespace to a single space and lower-casing, so values that differ only in
     * whitespace or letter case are treated as equal.
     */
    private static String normalizeHouseholdField(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
    }

    /**
     * Returns the id of the owner previously created for the given idempotency key, or {@code null}
     * when the key has not been seen (or is absent), meaning the create should proceed normally.
     */
    @Override
    public Integer findOwnerIdByIdempotencyKey(String idempotencyKey) throws DataAccessException {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKeys.get(idempotencyKey);
    }

    /**
     * Records that the given idempotency key produced the owner with the given id, so a later create
     * repeating the key replays that owner instead of duplicating it. No-op when the key is absent.
     */
    @Override
    public void registerIdempotencyKey(String idempotencyKey, Integer ownerId) throws DataAccessException {
        if (idempotencyKey == null || idempotencyKey.isBlank() || ownerId == null) {
            return;
        }
        idempotencyKeys.put(idempotencyKey, ownerId);
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
