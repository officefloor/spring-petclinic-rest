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
import org.springframework.samples.petclinic.rest.validation.CityCapacityExceededException;
import org.springframework.samples.petclinic.rest.validation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.validation.IdentityNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
        ownerRepository.save(owner);

    }

    @Override
    @Transactional
    public void createOwner(Owner owner) throws DataAccessException {
        createOwner(owner, false);
    }

    @Override
    @Transactional
    public void createOwner(Owner owner, boolean sharesHousehold) throws DataAccessException {
        rejectWhenCityAtCapacity(owner);
        assignHousehold(owner, sharesHousehold);
        if (owner.getCustomerCode() == null) {
            owner.setCustomerCode(nextCustomerCode(owner));
        }
        if (owner.getNamesakeCount() == null) {
            owner.setNamesakeCount(countNamesakes(owner));
        }
        ownerRepository.save(owner);
    }

    /**
     * Count the existing owners that already share this owner's first name and last name,
     * compared case-insensitively (with surrounding/collapsed whitespace ignored). Evaluated
     * before the owner is persisted, so it reflects the owners present at creation time.
     */
    private int countNamesakes(Owner owner) {
        String firstName = IdentityNormalizer.normalize(owner.getFirstName());
        String lastName = IdentityNormalizer.normalize(owner.getLastName());
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> firstName.equals(IdentityNormalizer.normalize(existing.getFirstName()))
                && lastName.equals(IdentityNormalizer.normalize(existing.getLastName())))
            .count();
    }

    /**
     * Reconcile the owner's household. Owners are in the same household when their last name and
     * address match (compared case-insensitively with collapsed whitespace). When another owner
     * already lives there, the create is rejected with {@link DuplicateHouseholdException} unless
     * {@code sharesHousehold} is set, in which case the new owner and every existing member are
     * given the same stable shared {@code householdId}.
     */
    private void assignHousehold(Owner owner, boolean sharesHousehold) {
        String lastName = IdentityNormalizer.normalize(owner.getLastName());
        String address = AddressNormalizer.canonical(owner.getAddress());
        List<Owner> householdMembers = ownerRepository.findAll().stream()
            .filter(existing -> lastName.equals(IdentityNormalizer.normalize(existing.getLastName()))
                && address.equals(AddressNormalizer.canonical(existing.getAddress())))
            .toList();
        if (householdMembers.isEmpty()) {
            return;
        }
        if (!sharesHousehold) {
            throw new DuplicateHouseholdException(
                "an owner with last name " + owner.getLastName() + " already lives at " + owner.getAddress());
        }
        String householdId = householdMembers.stream()
            .map(Owner::getHouseholdId)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> deriveHouseholdId(lastName, address));
        owner.setHouseholdId(householdId);
        for (Owner member : householdMembers) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    /**
     * Derive a stable, shared household identifier from the normalized last name and address, so
     * every owner in the same household deterministically maps to the same value.
     */
    private static String deriveHouseholdId(String normalizedLastName, String normalizedAddress) {
        String key = normalizedLastName + "\n" + normalizedAddress;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /** Maximum number of owners allowed to live in any single city. */
    private static final long CITY_CAPACITY = 50L;

    /**
     * Reject the create when the owner's city already contains {@value #CITY_CAPACITY} or more
     * owners (compared case-insensitively with collapsed whitespace). Evaluated before the owner
     * is persisted, so it reflects the owners present at creation time.
     */
    private void rejectWhenCityAtCapacity(Owner owner) {
        if (countOwnersInCity(owner) >= CITY_CAPACITY) {
            throw new CityCapacityExceededException(
                "city " + owner.getCity() + " already contains " + CITY_CAPACITY + " or more owners");
        }
    }

    /**
     * Count the existing owners that already live in this owner's city, compared
     * case-insensitively (with surrounding/collapsed whitespace ignored). Evaluated before the
     * owner is persisted, so it reflects the owners present at creation time.
     */
    private long countOwnersInCity(Owner owner) {
        String city = IdentityNormalizer.normalize(owner.getCity());
        return ownerRepository.findAll().stream()
            .filter(existing -> city.equals(IdentityNormalizer.normalize(existing.getCity())))
            .count();
    }

    /**
     * Builds the customer code {@code '<CITY3>-<LAST3>-<NNNN>'}: the upper-cased first
     * three letters of the owner's {@code city}, a hyphen, the upper-cased first three
     * letters of {@code lastName}, a hyphen, and a 4-digit zero-padded per-city sequence
     * equal to one more than the number of owners already in that city (compared
     * case-insensitively with collapsed whitespace).
     */
    private String nextCustomerCode(Owner owner) {
        String city3 = prefix3(owner.getCity());
        String last3 = prefix3(owner.getLastName());
        long sequence = countOwnersInCity(owner) + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Returns the upper-cased first three letters of {@code value}, or fewer when the value
     * is shorter; a {@code null} value yields an empty string.
     */
    private static String prefix3(String value) {
        String name = value == null ? "" : value;
        return name.substring(0, Math.min(3, name.length())).toUpperCase();
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
