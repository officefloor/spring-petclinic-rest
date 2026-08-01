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
import java.util.Locale;
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
        owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        if (owner.isNew()) {
            if (hasIdenticalOwner(owner)) {
                throw new DuplicateOwnerException();
            }
            if (hasOwnerWithSameEmail(owner)) {
                throw new DuplicateOwnerException("An owner with the same email address already exists");
            }
            if (hasReachedDailyRegistrationLimit()) {
                throw new DailyOwnerRegistrationLimitExceededException();
            }
            if (hasReachedCityLimit(owner)) {
                throw new CityOwnerLimitExceededException();
            }
            if (owner.getRegistrationDate() == null) {
                owner.setRegistrationDate(LocalDate.now());
            }
            owner.setCity(resolveCity(owner));
            owner.setMembershipNumber(ownerRepository.findAll().size() + 1);
            owner.setCustomerCode(generateCustomerCode(owner));
            owner.setMembershipTier(resolveMembershipTier());
            owner.setNamesakeCount(countNamesakes(owner));
            owner.setSharesHousehold(sharesHousehold(owner));
        }
        ownerRepository.save(owner);

    }

    /**
     * Generates a customer code for a newly registered owner, formatted as
     * {@code <UPPERCASE_CITY>-<NNNN>} where {@code NNNN} is one more than the number
     * of owners already in that city, zero-padded to four digits (e.g. {@code LONDON-0007}).
     */
    private String generateCustomerCode(Owner owner) {
        String city = owner.getCity();
        long existingInCity = ownerRepository.findAll().stream()
            .filter(existing -> Objects.equals(normalize(existing.getCity()), normalize(city)))
            .count();
        return String.format("%s-%04d", city.toUpperCase(Locale.ROOT), existingInCity + 1);
    }

    /**
     * Resolves the city to store for a newly registered owner. If another owner already
     * lives in the same city (ignoring letter case and surrounding/repeated whitespace),
     * that owner's exact spelling of the city name is reused so a city is stored
     * consistently. Otherwise the supplied city is title-cased, i.e. each word is
     * capitalized and the remaining letters lower-cased (e.g. {@code "new york"} becomes
     * {@code "New York"}). Returns {@code null} for a {@code null} input.
     */
    private String resolveCity(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return null;
        }
        String normalized = normalize(city);
        return ownerRepository.findAll().stream()
            .map(Owner::getCity)
            .filter(existing -> Objects.equals(normalize(existing), normalized))
            .findFirst()
            .orElseGet(() -> toTitleCase(city));
    }

    /**
     * Title-cases a value by trimming and collapsing whitespace, then capitalizing the
     * first letter of each whitespace-separated word and lower-casing the rest
     * (e.g. {@code "  new   YORK "} becomes {@code "New York"}).
     */
    private static String toTitleCase(String value) {
        String collapsed = value.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        StringBuilder result = new StringBuilder(collapsed.length());
        for (String word : collapsed.split(" ")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)))
                .append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return result.toString();
    }

    /**
     * The number of owners that receive the founding membership tier. The first
     * {@link #FOUNDING_TIER_LIMIT} owners ever created are {@code FOUNDING}; all later
     * owners are {@code STANDARD}.
     */
    private static final long FOUNDING_TIER_LIMIT = 100;

    /**
     * Resolves the membership tier for a newly registered owner: {@code FOUNDING} while
     * fewer than {@link #FOUNDING_TIER_LIMIT} owners exist beforehand, otherwise {@code STANDARD}.
     */
    private String resolveMembershipTier() {
        return ownerRepository.findAll().size() < FOUNDING_TIER_LIMIT ? "FOUNDING" : "STANDARD";
    }

    /**
     * Counts how many other owners already share the newly registered owner's last name,
     * ignoring letter case and surrounding/repeated whitespace. Invoked before the owner
     * is persisted, so it reflects the number of namesakes existing at creation time.
     */
    private int countNamesakes(Owner owner) {
        String lastName = normalize(owner.getLastName());
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> Objects.equals(normalize(existing.getLastName()), lastName))
            .count();
    }

    /**
     * Determines whether the newly registered owner shares a household with an existing owner,
     * i.e. another owner already has the same address and city (each compared ignoring letter
     * case and surrounding/repeated whitespace). Invoked before the owner is persisted, so it
     * reflects the state at creation time.
     */
    private boolean sharesHousehold(Owner owner) {
        String address = normalize(owner.getAddress());
        String city = normalize(owner.getCity());
        return ownerRepository.findAll().stream()
            .anyMatch(existing -> Objects.equals(normalize(existing.getAddress()), address)
                && Objects.equals(normalize(existing.getCity()), city));
    }

    /**
     * The maximum number of owners that may be registered on a single day (by registration date).
     */
    private static final long MAX_OWNERS_PER_DAY = 20;

    /**
     * Returns {@code true} if the number of owners already registered today (by registration date)
     * has reached {@link #MAX_OWNERS_PER_DAY}, so that no further owner may be created today.
     */
    private boolean hasReachedDailyRegistrationLimit() {
        LocalDate today = LocalDate.now();
        long registeredToday = ownerRepository.findAll().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        return registeredToday >= MAX_OWNERS_PER_DAY;
    }

    /**
     * The maximum number of owners that may live in a single city.
     */
    private static final long MAX_OWNERS_PER_CITY = 8;

    /**
     * Returns {@code true} if the owner's city (compared ignoring letter case and surrounding/repeated
     * whitespace) already contains {@link #MAX_OWNERS_PER_CITY} owners, so that no further owner may be
     * registered in that city.
     */
    private boolean hasReachedCityLimit(Owner owner) {
        String city = normalize(owner.getCity());
        if (city == null) {
            return false;
        }
        long ownersInCity = ownerRepository.findAll().stream()
            .filter(existing -> Objects.equals(normalize(existing.getCity()), city))
            .count();
        return ownersInCity >= MAX_OWNERS_PER_CITY;
    }

    private boolean hasIdenticalOwner(Owner owner) {
        return ownerRepository.findAll().stream()
            .anyMatch(existing -> isSameOwner(existing, owner));
    }

    private boolean isSameOwner(Owner existing, Owner owner) {
        return Objects.equals(normalizeTelephone(existing.getTelephone()), normalizeTelephone(owner.getTelephone()));
    }

    /**
     * Normalizes a telephone number for storage and duplicate detection by stripping
     * everything except digits (e.g. spaces, dashes and parentheses), so that
     * {@code "(613) 555-0100"} and {@code "6135550100"} are treated as the same number.
     * Returns {@code null} for a {@code null} input.
     */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("[^0-9]", "");
    }

    private boolean hasOwnerWithSameEmail(Owner owner) {
        String email = normalize(owner.getEmail());
        if (email == null || email.isEmpty()) {
            return false;
        }
        return ownerRepository.findAll().stream()
            .anyMatch(existing -> email.equals(normalize(existing.getEmail())));
    }

    /**
     * Normalizes a value for duplicate detection so that values differing only in
     * letter case or in surrounding/repeated whitespace compare as equal. Surrounding
     * whitespace is trimmed, internal whitespace runs are collapsed to a single space,
     * and the result is lower-cased (e.g. {@code "  john   smith "} and {@code "John Smith"}
     * both normalize to {@code "john smith"}). Returns {@code null} for a {@code null} input.
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
