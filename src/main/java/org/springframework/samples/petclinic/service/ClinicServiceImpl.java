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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
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

    /**
     * Dedicated audit logger. Successful owner creates emit a single line here carrying the new
     * owner's id, customer code and registration date.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Maximum number of owners allowed in a single city. Creating an owner in a city that already
     * contains this many owners is rejected with {@link CityAtCapacityException}.
     */
    static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Maximum number of owners that may be registered on a single day. Creating an owner on a day
     * that already has this many owners (counted by {@code registrationDate}) is rejected with
     * {@link DailyRegistrationLimitException}.
     */
    static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Threshold of owners registered on a single day (counted by {@code registrationDate}) above
     * which a newly created owner is flagged with {@code bulkSignupWarning}. When more than this
     * many owners have already been created on that day, the new owner's warning is {@code true}.
     */
    static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

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
        saveOwner(owner, false);
    }

    /**
     * Save an owner, enforcing the create-time business rules.
     *
     * <p>The {@code sharesHousehold} flag is retained only for backward compatibility and is now a
     * no-op: it formerly bypassed a dedicated duplicate-household check, but duplicate detection is
     * now expressed solely through {@link #assertIdentityIsUnique(Owner)}, under which members of the
     * same household with different telephones are always allowed, so the flag has nothing to bypass.
     */
    @Override
    @Transactional
    public void saveOwner(Owner owner, boolean sharesHousehold) throws DataAccessException {
        boolean isCreate = owner.isNew();
        assertIdentityIsUnique(owner);
        if (owner.isNew()) {
            assertCityHasCapacity(owner);
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        assertRegistrationDateNotInFuture(owner);
        owner.setRegistrationDate(rollToBusinessDay(owner.getRegistrationDate()));
        if (owner.isNew()) {
            assertDailyRegistrationLimit(owner);
            owner.setBulkSignupWarning(computeBulkSignupWarning(owner));
        }
        if (owner.isNew() && owner.getCustomerCode() == null) {
            owner.setCustomerCode(generateCustomerCode(owner));
        }
        if (owner.isNew() && owner.getNamesakeCount() == null) {
            owner.setNamesakeCount(countNamesakes(owner));
        }
        ownerRepository.save(owner);
        if (isCreate) {
            AUDIT.info("owner created id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        }
    }

    /**
     * Count the existing owners that, at the moment this owner is created, share its first name
     * and last name (both compared case-insensitively). The owner being saved is excluded by id so
     * it never counts itself. This value is deterministic given the current data, so it is computed
     * once at creation time and stored.
     */
    private Integer countNamesakes(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        if (firstName == null || lastName == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Build an owner's {@code customerCode} formatted {@code '<REGION>-<HASH8>'}, where {@code REGION}
     * is the region code derived from the owner's postcode (or {@code 'UNKNOWN'} when no region can be
     * derived) and {@code HASH8} is the first eight upper-cased hex characters of SHA-256 over the
     * owner's normalized telephone concatenated with the last name (e.g. {@code 'NSW-1A2B3C4D'}). The
     * value is deterministic in those fields and carries no sequence number (see
     * {@link Owner#customerCode(String, String, String)}).
     */
    private String generateCustomerCode(Owner owner) {
        return Owner.customerCode(owner.getPostcode(), owner.getTelephone(), owner.getLastName());
    }

    /**
     * Enforce that no <em>other</em> owner already shares this owner's derived identity, throwing
     * {@link DuplicateIdentityException} if one does. This single check consolidates the former
     * separate telephone, email and household duplicate checks into the one {@code identityKey}
     * (normalized telephone, normalized email (or empty) and household id, see
     * {@link Owner#identityKey(String, String, String, String)}).
     *
     * <p>Because the telephone is part of the key, two owners can only ever collide on the whole
     * key when their normalized telephones already match; the normalized telephone is therefore the
     * decisive component and is what this check compares. Consequently two members of the same
     * household with different telephones have different identity keys and are both allowed, while
     * an owner reusing an existing normalized telephone is rejected. The owner being saved is
     * excluded by id so updating an owner in place never conflicts with itself.
     */
    private void assertIdentityIsUnique(Owner owner) {
        String normalizedTelephone = Owner.normalizeTelephone(owner.getTelephone());
        if (normalizedTelephone == null) {
            return;
        }
        boolean duplicate = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .map(existing -> Owner.normalizeTelephone(existing.getTelephone()))
            .anyMatch(normalizedTelephone::equals);
        if (duplicate) {
            throw new DuplicateIdentityException(Owner.identityKey(
                owner.getTelephone(), owner.getEmail(), owner.getLastName(), owner.getAddress()));
        }
    }

    /**
     * Enforce that the owner's city has not yet reached its capacity, throwing
     * {@link CityAtCapacityException} when the city (compared case-insensitively) already contains
     * {@link #MAX_OWNERS_PER_CITY} or more owners. The owner being saved is excluded by id.
     */
    private void assertCityHasCapacity(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        long count = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (count >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * Enforce that the owner's registration date is not later than the current server date, throwing
     * {@link FutureRegistrationDateException} when it is. A registration date may not be in the
     * future; a date defaulted to {@link LocalDate#now()} is never in the future, so this check is a
     * harmless no-op for that case.
     */
    private void assertRegistrationDateNotInFuture(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }

    /**
     * Roll a registration date forward to the next business day. A date that falls on a Saturday or
     * Sunday is advanced to the following Monday; a weekday is returned unchanged. This is applied to
     * the effective registration date (whether supplied in the request or defaulted to the server
     * date) so that every value derived from it&mdash;such as the membership number's year segment
     * and the daily create-limit counting&mdash;uses the adjusted business day.
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /**
     * Enforce that the owner's registration day has not yet reached its capacity, throwing
     * {@link DailyRegistrationLimitException} when {@link #MAX_OWNERS_PER_DAY} or more owners are
     * already registered on that day (counted by {@code registrationDate}). The owner being saved
     * is excluded by id.
     */
    private void assertDailyRegistrationLimit(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return;
        }
        long count = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new DailyRegistrationLimitException(registrationDate);
        }
    }

    /**
     * Compute whether a newly created owner should carry a bulk-signup warning, i.e. whether more
     * than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created on this owner's
     * registration day (counted by {@code registrationDate}, excluding the owner being saved). Like
     * the daily-limit rule this reflects the state at creation time, so it is computed once and
     * stored on the owner.
     */
    private boolean computeBulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long count = ownerRepository.findAll().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
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
