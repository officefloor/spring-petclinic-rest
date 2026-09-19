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
import org.springframework.samples.petclinic.audit.OwnerAuditLogger;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.samples.petclinic.util.BusinessDayAdjuster;
import org.springframework.samples.petclinic.util.CustomerCodeGenerator;
import org.springframework.samples.petclinic.util.HouseholdIdGenerator;
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.OwnerIdentityKey;
import org.springframework.samples.petclinic.util.TextNormalizer;
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
    private final OwnerAuditLogger ownerAuditLogger;

    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository,
        OwnerAuditLogger ownerAuditLogger) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
        this.ownerAuditLogger = ownerAuditLogger;
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

    /**
     * Maximum number of owners allowed in a single city. Creating an owner in a city that has
     * already reached this many owners is rejected as a conflict.
     */
    static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Maximum number of owners allowed to register on a single day. Creating an owner once a day has
     * already reached this many registrations is rejected as a rate-limit violation.
     */
    static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Number of owners that must already have registered on a day before a new registration is
     * flagged with a bulk-signup warning. Once the count already recorded for the day exceeds this
     * value, the new owner carries {@code bulkSignupWarning == true}.
     */
    static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    @Override
    @Transactional
    public String createOwner(Owner owner, boolean sharesHousehold) throws DataAccessException {
        rejectIdentityCollision(owner);
        List<Owner> householdMembers = findHouseholdMembers(owner);
        long ownersInCity = ownerRepository.countByCity(owner.getCity());
        if (ownersInCity >= MAX_OWNERS_PER_CITY) {
            throw new CityCapacityExceededException(owner.getCity(), MAX_OWNERS_PER_CITY);
        }
        LocalDate effectiveDate = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(BusinessDayAdjuster.toBusinessDay(effectiveDate));
        long ownersOnDate = ownerRepository.countByRegistrationDate(owner.getRegistrationDate());
        if (ownersOnDate >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(owner.getRegistrationDate(), MAX_OWNERS_PER_DAY);
        }
        owner.setBulkSignupWarning(ownersOnDate > BULK_SIGNUP_WARNING_THRESHOLD);
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setHouseholdSize(householdMembers.size() + 1);
        owner.setCustomerCode(CustomerCodeGenerator.format(
            LocalityResolver.resolve(owner.getCity(), owner.getPostcode()),
            owner.getTelephone(), owner.getLastName()));
        if (sharesHousehold && !householdMembers.isEmpty()) {
            owner.setHouseholdId(joinHousehold(owner, householdMembers));
        }
        ownerRepository.save(owner);
        ownerAuditLogger.ownerCreated(owner);
        return owner.getHouseholdId();
    }

    /**
     * Reject creating an owner whose whole {@link OwnerIdentityKey identity key} already belongs to
     * another owner. Because the telephone is part of the key, only an existing owner sharing every
     * key component (normalized telephone, email and household identifier) is a duplicate.
     */
    private void rejectIdentityCollision(Owner owner) {
        String identityKey = OwnerIdentityKey.of(owner);
        boolean collides = ownerRepository.findByTelephone(owner.getTelephone()).stream()
            .anyMatch(existing -> identityKey.equals(OwnerIdentityKey.of(existing)));
        if (collides) {
            throw new DuplicateOwnerException(identityKey);
        }
    }

    /**
     * Assign the stable household identifier to the new owner and to every existing member of the
     * household, persisting the members whose identifier changes, and return the shared identifier.
     */
    private String joinHousehold(Owner owner, List<Owner> householdMembers) {
        String householdId = HouseholdIdGenerator.generate(owner.getLastName(), owner.getAddress());
        for (Owner member : householdMembers) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
        return householdId;
    }

    @Override
    @Transactional
    public void saveOwner(Owner owner) throws DataAccessException {
        ownerRepository.save(owner);
    }

    /**
     * Count the owners that already exist sharing this owner's first name and last name, compared
     * case-insensitively. Invoked before the new owner is persisted, so it never counts itself.
     */
    private int countNamesakes(Owner owner) {
        return (int) ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(existing -> owner.getFirstName().equalsIgnoreCase(existing.getFirstName()))
            .count();
    }

    /**
     * Find the existing owners that already share this owner's household, i.e. that have the same last name and
     * address compared case-insensitively and with runs of whitespace collapsed.
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        String address = TextNormalizer.normalize(owner.getAddress());
        if (address == null) {
            return List.of();
        }
        return ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(existing -> address.equals(TextNormalizer.normalize(existing.getAddress())))
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
