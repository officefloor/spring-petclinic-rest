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

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
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

    /** Dedicated audit channel; create side-effects are published here for auditing. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * When more owners than this have already been created on a given day (by {@code
     * registrationDate}), a newly created owner is flagged with {@code bulkSignupWarning}.
     */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80L;

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
        boolean isNew = owner.isNew();
        // Resolve the effective registration date (supplied, or defaulted to the server date) and
        // roll it forward to the next business day so it never lands on a weekend. Everything
        // derived from the registration date below must use this adjusted value.
        LocalDate registrationDate = toBusinessDay(
            owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now());
        owner.setRegistrationDate(registrationDate);
        if (owner.isNew() && ownerRepository.existsByTelephone(owner.getTelephone())) {
            throw new DuplicateTelephoneException(owner.getTelephone());
        }
        if (owner.isNew()
            && countOwnersInCity(owner) >= CityOwnerLimitExceededException.MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitExceededException(owner.getCity());
        }
        if (owner.isNew()
            && countOwnersRegisteredOn(registrationDate) >= DailyOwnerLimitExceededException.MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
        if (owner.isNew()) {
            List<Owner> householdMembers = findHouseholdMembers(owner);
            if (!householdMembers.isEmpty()) {
                if (!owner.isSharesHousehold()) {
                    throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
                }
                owner.setHouseholdId(resolveHouseholdId(householdMembers));
            }
            // Size of the household after this create: the existing members plus this new owner.
            owner.setHouseholdMemberCount(householdMembers.size() + 1);
        }
        if (owner.isNew() && owner.getCustomerCode() == null) {
            owner.setCustomerCode(generateCustomerCode(owner));
        }
        if (owner.isNew()) {
            owner.setNamesakeCount(countNamesakes(owner));
            owner.setBulkSignupWarning(
                countOwnersRegisteredOn(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD);
        }
        if (owner.getMembershipNumber() == null) {
            owner.setMembershipNumber(generateMembershipNumber(owner));
        }
        ownerRepository.save(owner);
        if (isNew) {
            AUDIT.info("owner created id={} customerCode={} membershipLevel={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getMembershipLevel(),
                owner.getRegistrationDate());
        }
    }

    /**
     * Count the owners that already exist (before this create) sharing the same first name and last
     * name as {@code owner}, compared case-insensitively. Returns zero when the owner's name is
     * unique among the existing owners.
     */
    private int countNamesakes(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        if (firstName == null || lastName == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Find the owners that already belong to the same household as {@code owner}, i.e. have the same
     * last name and the same address. Last names are compared case-insensitively with collapsed
     * whitespace; addresses are compared in their canonical normalized form (see
     * {@link AddressNormalizer}), so differences in letter case, runs of spaces or street-type
     * abbreviations are ignored. Returns an empty list when the owner has no last name or address to
     * match on.
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        String lastName = normalizeHouseholdKey(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        if (lastName == null || address == null) {
            return List.of();
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> lastName.equals(normalizeHouseholdKey(existing.getLastName()))
                && address.equals(AddressNormalizer.normalize(existing.getAddress())))
            .toList();
    }

    /**
     * Resolve the stable {@code householdId} shared by a household. If any existing member already
     * carries one it is reused so the identifier stays stable as further owners join; otherwise a
     * new one is generated. The resolved value is back-filled onto every member that is missing it
     * (or has a different one) so all members of the household share the same identifier.
     */
    private String resolveHouseholdId(List<Owner> members) {
        String householdId = members.stream()
            .map(Owner::getHouseholdId)
            .filter(id -> id != null && !id.isBlank())
            .findFirst()
            .orElseGet(() -> UUID.randomUUID().toString());
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
        return householdId;
    }

    /**
     * Normalize a value for case-insensitive, whitespace-insensitive household comparison by
     * trimming, collapsing internal runs of whitespace to a single space and lower-casing it.
     */
    private String normalizeHouseholdKey(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Build an owner's {@code customerCode} as {@code '<CITY3>-<LAST3>-<NNNN>'}: CITY3 is the
     * upper-cased first three letters of the owner's city, LAST3 the upper-cased first three
     * letters of the owner's last name, and NNNN a per-city 4-digit zero-padded sequence equal
     * to one more than the number of owners already in that city (e.g. {@code 'LON-SMI-0007'}).
     */
    private String generateCustomerCode(Owner owner) {
        String city3 = upperPrefix3(owner.getCity());
        String last3 = upperPrefix3(owner.getLastName());
        long sequence = countOwnersInCity(owner) + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Return the upper-cased first three letters of {@code value} (or fewer when it is shorter),
     * the shared truncate-and-upper idiom used for each {@code customerCode} prefix segment.
     */
    private String upperPrefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }

    /**
     * Count the owners that already exist (before this create) in the same city as {@code owner},
     * compared case-insensitively.
     */
    private long countOwnersInCity(Owner owner) {
        String city = owner.getCity();
        return ownerRepository.findAll().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
    }

    /**
     * Count the owners that already exist (before this create) whose {@code registrationDate} falls
     * on the given {@code date}. Used to enforce the per-day create cap.
     */
    private long countOwnersRegisteredOn(LocalDate date) {
        return ownerRepository.findAll().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Roll {@code date} forward to the next business day when it falls on a weekend: a Saturday or
     * Sunday becomes the following Monday, while a weekday is returned unchanged. Registration dates
     * are required to fall on a business day.
     */
    private LocalDate toBusinessDay(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.plusDays(2);
            case SUNDAY:
                return date.plusDays(1);
            default:
                return date;
        }
    }

    /**
     * Build an owner's {@code membershipNumber} as {@code '<customerCode>-M<YY>'}, where YY is the
     * last two digits of the owner's {@code registrationDate} year (e.g. {@code 'SMI-0007-M26'}).
     */
    private String generateMembershipNumber(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        return String.format("%s-M%s", owner.getCustomerCode(), yy);
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
