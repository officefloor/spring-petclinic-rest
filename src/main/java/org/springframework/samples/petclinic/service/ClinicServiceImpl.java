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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CustomerCodeGenerator customerCodeGenerator;
    private final MembershipNumberGenerator membershipNumberGenerator;

    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository,
        CustomerCodeGenerator customerCodeGenerator,
        MembershipNumberGenerator membershipNumberGenerator) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
        this.customerCodeGenerator = customerCodeGenerator;
        this.membershipNumberGenerator = membershipNumberGenerator;
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
        if (owner.isNew() && owner.getCustomerCode() == null) {
            String region = LocalityResolver.regionFor(owner.getPostcode(), owner.getCity());
            owner.setCustomerCode(
                customerCodeGenerator.generate(region, owner.getTelephone(), owner.getLastName()));
        }
        if (owner.isNew() && owner.getNamesakeCount() == null) {
            owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        }
        if (owner.isNew() && owner.getMembershipNumber() == null) {
            owner.setMembershipNumber(
                membershipNumberGenerator.generate(owner.getCustomerCode(), owner.getRegistrationDate()));
        }
        if (owner.isNew() && owner.getHouseholdSize() == null) {
            owner.setHouseholdSize(countHouseholdMembers(owner));
        }
        ownerRepository.save(owner);
    }

    /**
     * Count the members of the given (not yet persisted) owner's household: the already stored
     * owners sharing its {@code householdId} plus the owner itself. An owner with no household
     * ({@code householdId} is null) is a household of one. Used to capture an owner's household
     * size at creation time.
     */
    private int countHouseholdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        long existing = ownerRepository.findAll().stream()
            .filter(existingOwner -> householdId.equals(existingOwner.getHouseholdId()))
            .count();
        return (int) (existing + 1);
    }

    /**
     * Count the owners already stored in the given city, compared case-insensitively. Used to
     * enforce the per-city capacity limit at creation time.
     */
    @Override
    @Transactional(readOnly = true)
    public long countOwnersInCity(String city) throws DataAccessException {
        return ownerRepository.findAll().stream()
            .filter(existing -> equalsIgnoreCase(existing.getCity(), city))
            .count();
    }

    /**
     * Count the owners already stored whose registration date equals the given date. Used to
     * enforce the per-day registration limit at creation time.
     */
    @Override
    @Transactional(readOnly = true)
    public long countOwnersRegisteredOn(LocalDate registrationDate) throws DataAccessException {
        return ownerRepository.findAll().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Count the owners already stored that share the given first and last name, compared
     * case-insensitively. Used to capture an owner's namesake count at creation time.
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> equalsIgnoreCase(existing.getFirstName(), firstName)
                && equalsIgnoreCase(existing.getLastName(), lastName))
            .count();
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
    }

    /**
     * Find the owners already stored whose derived {@link Owner#getIdentityKey() identity key}
     * equals the given key. Used as the single duplicate-detection check at creation time: a new
     * owner is a duplicate only when its whole identity key matches an existing owner's.
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnersByIdentityKey(String identityKey) throws DataAccessException {
        return ownerRepository.findAll().stream()
            .filter(owner -> owner.getIdentityKey().equals(identityKey))
            .toList();
    }

    /**
     * Find an existing owner belonging to the same household as the given (not-yet-persisted) owner,
     * i.e. one sharing its computed {@link Owner#getHouseholdId() householdId} (derived from last name
     * and postcode). Because the household is keyed on (last name, postcode), such an owner is a
     * household duplicate of the new one. Returns the first such owner, or {@code null} when the owner
     * has no household ({@code householdId} is null) or no match exists.
     */
    @Override
    @Transactional(readOnly = true)
    public Owner findHouseholdDuplicateOf(Owner owner) throws DataAccessException {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return null;
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .findFirst()
            .orElse(null);
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
