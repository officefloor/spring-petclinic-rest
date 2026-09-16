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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    /**
     * Number of owners that must already carry a day's registration date before a
     * further owner registered that day is flagged with a bulk signup warning.
     */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Number of owners a city must already hold before a further owner registered in it
     * is flagged with a capacity warning, signalling the city is approaching (but has not
     * yet reached) the hard per-city capacity limit enforced by the create endpoint.
     */
    private static final long CITY_CAPACITY_WARNING_THRESHOLD = 40;

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
        // Soft delete: retain the row and flag it so the owner stays retrievable but is
        // ignored by the create endpoint's duplicate/identity checks.
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
        if (owner.isNew()) {
            if (owner.getCustomerCode() == null) {
                owner.setCustomerCode(customerCodeGenerator.generateUnique(
                    RegionResolver.regionFor(owner.getPostcode(), owner.getCity()),
                    owner.getTelephone(), owner.getLastName(),
                    ownerRepository::existsByCustomerCode));
            }
            owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
            owner.setBulkSignupWarning(
                ownerRepository.countByRegistrationDate(owner.getRegistrationDate()) > BULK_SIGNUP_WARNING_THRESHOLD);
            owner.setCapacityWarning(
                ownerRepository.countByCity(owner.getCity()) >= CITY_CAPACITY_WARNING_THRESHOLD);
            owner.setHouseholdId(householdIdFor(owner));
            owner.setHouseholdSize(householdSizeIncluding(owner));
            owner.setMembershipLevelCap(membershipLevelCapFor(owner));
            Optional<Owner> possibleDuplicate = findPossibleDuplicate(owner);
            owner.setPossibleDuplicate(possibleDuplicate.isPresent());
            owner.setPossibleDuplicateOf(possibleDuplicate.map(Owner::getId).orElse(null));
        }
        ownerRepository.save(owner);

    }

    /**
     * The deterministic, shared household id this owner maps to, derived from its last
     * name and postcode so that owners sharing both belong to the same household.
     */
    private String householdIdFor(Owner owner) {
        return householdIdGenerator.generate(owner.getLastName(), owner.getPostcode());
    }

    /**
     * Size of the household this new owner will belong to once persisted: the owners
     * already sharing its household id, plus the owner itself. An owner with no shared
     * household id stands alone, so its household size is one.
     */
    private int householdSizeIncluding(Owner owner) {
        if (owner.getHouseholdId() == null) {
            return 1;
        }
        return (int) ownerRepository.countByHouseholdId(owner.getHouseholdId()) + 1;
    }

    /**
     * Count the existing owners that already share the given first and last name,
     * compared case-insensitively. Used to stamp a new owner's namesake count at
     * registration, before it is itself persisted.
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) ownerRepository.findByLastNameIgnoreCase(lastName).stream()
            .filter(existing -> existing.getFirstName().equalsIgnoreCase(firstName))
            .count();
    }

    /**
     * The existing owners that already belong to this owner's household, i.e. the persisted
     * owners sharing its computed household id. An owner with no household id (that maps to no
     * shared household) has no existing household members. As the household id is derived from
     * the last name, the candidates are narrowed by last name before the id is matched.
     */
    private List<Owner> existingHouseholdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return List.of();
        }
        return ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
    }

    /**
     * The ceiling to stamp on a new owner's membership level: one above the highest membership
     * level currently held by an existing member of its household, so the new owner's level
     * cannot exceed one above the household maximum. Null when the owner has no existing
     * household member, in which case no ceiling applies. See {@link Owner#getMembershipLevel()}.
     */
    private Integer membershipLevelCapFor(Owner owner) {
        return existingHouseholdMembers(owner).stream()
            .map(Owner::getMembershipLevel)
            .max(Comparator.naturalOrder())
            .map(max -> max + 1)
            .orElse(null);
    }

    /**
     * Find the existing owner, if any, that this new owner is a possible (soft) duplicate of:
     * a live owner that is not a hard duplicate (its {@link Owner#getIdentityKey() identity
     * key} differs) yet shares the new owner's phonetic last name (see {@link Soundex}) and
     * postcode. When several match, the earliest (lowest id) is chosen. An owner with no
     * postcode belongs to no such group, and one that deliberately declared it shares a
     * household is never a suspected duplicate.
     */
    private Optional<Owner> findPossibleDuplicate(Owner owner) {
        String postcode = owner.getPostcode();
        if (owner.isDeclaredHouseholdMember() || postcode == null || postcode.isBlank()) {
            return Optional.empty();
        }
        String identityKey = owner.getIdentityKey();
        return liveOwnersSoundingLike(owner).stream()
            .filter(existing -> postcode.equals(existing.getPostcode()))
            .filter(existing -> !identityKey.equals(existing.getIdentityKey()))
            .min(Comparator.comparing(Owner::getId));
    }

    /**
     * The live (non-deleted) owners whose last name sounds like this owner's, i.e. shares its
     * Soundex code. This is the shared candidate set for both hard-duplicate detection and the
     * soft match, which group owners by the phonetic sound of the last name (as the identity
     * key does) rather than by an exact last-name match.
     */
    private List<Owner> liveOwnersSoundingLike(Owner owner) {
        String soundex = Soundex.of(owner.getLastName());
        return ownerRepository.findByDeletedFalse().stream()
            .filter(existing -> soundex.equals(Soundex.of(existing.getLastName())))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicateOwner(Owner owner) throws DataAccessException {
        String identityKey = owner.getIdentityKey();
        return liveOwnersSoundingLike(owner).stream()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOwnersByCity(String city) throws DataAccessException {
        return ownerRepository.countByCity(city);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOwnersByRegistrationDate(LocalDate registrationDate) throws DataAccessException {
        return ownerRepository.countByRegistrationDate(registrationDate);
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
