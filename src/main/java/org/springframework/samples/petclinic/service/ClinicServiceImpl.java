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
import org.springframework.samples.petclinic.util.BusinessDay;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    private final HouseholdIdGenerator householdIdGenerator;

    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository,
        CustomerCodeGenerator customerCodeGenerator,
        HouseholdIdGenerator householdIdGenerator) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
        this.customerCodeGenerator = customerCodeGenerator;
        this.householdIdGenerator = householdIdGenerator;
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
            long citySequence = ownerRepository.countByCityIgnoreCase(owner.getCity()) + 1;
            owner.setCustomerCode(customerCodeGenerator.generate(owner.getCity(), owner.getLastName(), citySequence));
        }
        ownerRepository.save(owner);

    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOwnerByTelephone(String telephone) throws DataAccessException {
        return ownerRepository.existsByTelephone(telephone);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOwnerInHousehold(String lastName, String address) throws DataAccessException {
        return !findHouseholdMembers(lastName, address).isEmpty();
    }

    /**
     * The maximum number of owners allowed in a single city; once a city holds this many
     * owners it is considered at capacity and no further owners may be created there.
     */
    static final long CITY_CAPACITY = 50;

    @Override
    @Transactional(readOnly = true)
    public boolean isCityAtCapacity(String city) throws DataAccessException {
        return ownerRepository.countByCityIgnoreCase(city) >= CITY_CAPACITY;
    }

    /**
     * The maximum number of owners that may be created in a single day; once this many owners
     * have already been registered on a given business day no further owners may be created for
     * that day.
     */
    static final long DAILY_OWNER_LIMIT = 100;

    @Override
    public LocalDate resolveRegistrationDate(LocalDate registrationDate) {
        LocalDate effectiveDate = registrationDate == null ? LocalDate.now() : registrationDate;
        return BusinessDay.rollForward(effectiveDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDailyOwnerLimitReached(LocalDate registrationDate) throws DataAccessException {
        return ownerRepository.countByRegistrationDate(registrationDate) >= DAILY_OWNER_LIMIT;
    }

    /**
     * The number of owners that may be registered on a given day before further sign-ups for that
     * day are flagged with a bulk-signup warning. Once more than this many owners already exist for
     * the day, subsequent creates carry the warning.
     */
    static final long BULK_SIGNUP_WARNING_THRESHOLD = 80;

    @Override
    @Transactional(readOnly = true)
    public boolean isBulkSignupWarranted(LocalDate registrationDate) throws DataAccessException {
        return ownerRepository.countByRegistrationDate(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    @Override
    @Transactional(readOnly = true)
    public long countNamesakes(String firstName, String lastName) throws DataAccessException {
        return ownerRepository.findByLastNameIgnoreCase(lastName).stream()
            .filter(owner -> owner.getFirstName().equalsIgnoreCase(firstName))
            .count();
    }

    @Override
    @Transactional
    public String assignHousehold(Owner owner) throws DataAccessException {
        List<Owner> members = findHouseholdMembers(owner.getLastName(), owner.getAddress());
        String householdId = members.stream()
            .map(Owner::getHouseholdId)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> householdIdGenerator.generate(owner.getLastName(), owner.getAddress()));
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (member.getHouseholdId() == null) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
        return householdId;
    }

    /**
     * Find the existing owners that share {@code owner}'s household, i.e. those with the same last
     * name (ignoring case) and the same canonical address.
     */
    private List<Owner> findHouseholdMembers(String lastName, String address) {
        String canonicalAddress = householdIdGenerator.canonical(address);
        return ownerRepository.findByLastNameIgnoreCase(lastName).stream()
            .filter(owner -> householdIdGenerator.canonical(owner.getAddress()).equals(canonicalAddress))
            .toList();
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
