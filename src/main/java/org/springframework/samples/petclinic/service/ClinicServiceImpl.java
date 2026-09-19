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
import org.springframework.samples.petclinic.util.DisposableEmailDomains;
import org.springframework.samples.petclinic.util.HouseholdIdGenerator;
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.OwnerIdentityKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
        if (DisposableEmailDomains.isDisposable(owner.getEmail())) {
            throw new DisposableEmailDomainException(owner.getEmail());
        }
        owner.setHouseholdId(HouseholdIdGenerator.generate(owner.getLastName(), owner.getPostcode()));
        rejectIdentityCollision(owner);
        List<Owner> householdMembers = findHouseholdMembers(owner);
        if (!householdMembers.isEmpty() && !sharesHousehold) {
            throw new HouseholdDuplicateException(owner.getHouseholdId());
        }
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
        flagPossibleDuplicate(owner, householdMembers, sharesHousehold);
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setHouseholdSize(householdMembers.size() + 1);
        owner.setCustomerCode(assignCustomerCode(owner));
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
            .filter(ClinicServiceImpl::isActive)
            .anyMatch(existing -> identityKey.equals(OwnerIdentityKey.of(existing)));
        if (collides) {
            throw new DuplicateOwnerException(identityKey);
        }
    }

    /**
     * Whether an existing owner is still active, i.e. has not been soft-deleted. Soft-deleted owners
     * are retained for history but ignored by the create-time duplicate and identity checks.
     */
    private static boolean isActive(Owner owner) {
        return !Boolean.TRUE.equals(owner.getDeleted());
    }

    /**
     * Build the new owner's {@code customerCode} and de-duplicate it against existing owners. The
     * base code is derived from the owner's locality, telephone and last name; if it already belongs
     * to another owner it is suffixed with the smallest {@code "-<n>"} (n &gt;= 2) that is still free.
     */
    private String assignCustomerCode(Owner owner) {
        String baseCode = CustomerCodeGenerator.format(
            LocalityResolver.resolve(owner.getCity(), owner.getPostcode()),
            owner.getTelephone(), owner.getLastName());
        Set<String> takenCodes = ownerRepository.findByCustomerCodeStartingWith(baseCode).stream()
            .map(Owner::getCustomerCode)
            .collect(Collectors.toSet());
        return CustomerCodeGenerator.deduplicate(baseCode, takenCodes::contains);
    }

    /**
     * Flag the new owner as a possible (soft) duplicate of an existing household member carrying a
     * different telephone. The new owner is still created; {@code possibleDuplicate} is set to
     * whether such a match exists and {@code possibleDuplicateOf} to the matching owner's id (the
     * lowest when several match), or {@code null} when there is no match.
     *
     * <p>An owner that explicitly declares {@code sharesHousehold} is a <em>declared</em> household
     * member rather than a suspected duplicate, so it is never flagged.
     */
    private void flagPossibleDuplicate(Owner owner, List<Owner> householdMembers, boolean sharesHousehold) {
        Owner match = sharesHousehold ? null : householdMembers.stream()
            .filter(existing -> !owner.getTelephone().equals(existing.getTelephone()))
            .min(Comparator.comparingInt(Owner::getId))
            .orElse(null);
        owner.setPossibleDuplicate(match != null);
        owner.setPossibleDuplicateOf(match == null ? null : match.getId());
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
     * Find the existing owners that already share this owner's household, i.e. that derive the same
     * computed {@code householdId} (the same last name and postcode). An owner without a postcode
     * has no household peers.
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        if (owner.getPostcode() == null) {
            return List.of();
        }
        String householdId = owner.getHouseholdId();
        return ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(ClinicServiceImpl::isActive)
            .filter(existing -> householdId.equals(
                HouseholdIdGenerator.generate(existing.getLastName(), existing.getPostcode())))
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
