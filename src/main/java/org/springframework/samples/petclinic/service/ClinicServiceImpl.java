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
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
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
     */
    private String generateCustomerCode(Owner owner) {
        return String.format("%s-%s", deriveRegion(owner), hash8(owner));
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
        String seed = telephone + lastName;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
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

    @Override
    @Transactional(readOnly = true)
    public boolean existsOwnerInHousehold(String lastName, String address) throws DataAccessException {
        return !findHouseholdMembers(lastName, address).isEmpty();
    }

    /**
     * Returns the existing owners that belong to the household identified by the given last name
     * and address, comparing both fields in their normalized form. An empty list is returned when
     * either field is absent (no household can be formed).
     */
    private List<Owner> findHouseholdMembers(String lastName, String address) {
        String normalizedLastName = normalizeHouseholdField(lastName);
        String normalizedAddress = normalizeHouseholdAddress(address);
        if (normalizedLastName == null || normalizedAddress == null) {
            return List.of();
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> normalizedLastName.equals(normalizeHouseholdField(existing.getLastName()))
                && normalizedAddress.equals(normalizeHouseholdAddress(existing.getAddress())))
            .toList();
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

    /**
     * Normalizes an address for household comparison and identity using the same canonical form
     * applied when an owner is created (trim/collapse whitespace, upper-case, expand common
     * abbreviations), so comparisons always use the normalized address regardless of the form in
     * which either value was originally supplied. Returns {@code null} when the address is absent
     * or blank, since no household can be formed without one.
     */
    private static String normalizeHouseholdAddress(String address) {
        String normalized = AddressNormalizer.normalize(address);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        return normalized;
    }

    /**
     * Places {@code owner} into the household formed by owners that share its (normalized) last
     * name and address, and returns the household's stable shared identifier. If any existing
     * member already carries a household id it is reused; otherwise a stable id is derived from
     * the shared last name and address. Every member missing an id (including the given owner)
     * is assigned the shared value so all owners in the household agree on it.
     */
    @Override
    @Transactional
    public String assignHousehold(Owner owner) throws DataAccessException {
        List<Owner> members = findHouseholdMembers(owner.getLastName(), owner.getAddress());
        if (members.isEmpty()) {
            return null;
        }
        String householdId = members.stream()
            .map(Owner::getHouseholdId)
            .filter(id -> id != null && !id.isBlank())
            .findFirst()
            .orElseGet(() -> generateHouseholdId(owner.getLastName(), owner.getAddress()));
        for (Owner member : members) {
            if (member.getHouseholdId() == null || member.getHouseholdId().isBlank()) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
        owner.setHouseholdId(householdId);
        return householdId;
    }

    /**
     * Derives a stable household identifier from the normalized last name and address as the
     * first 16 upper-case hex characters of their SHA-256 digest, so the same household always
     * maps to the same value regardless of creation order.
     */
    private static String generateHouseholdId(String lastName, String address) {
        String seed = normalizeHouseholdField(lastName) + "\n" + normalizeHouseholdAddress(address);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, 16);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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
