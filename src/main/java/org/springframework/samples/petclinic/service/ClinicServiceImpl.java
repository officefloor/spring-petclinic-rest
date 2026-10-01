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
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.InvalidTelephoneException;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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

    /** Maximum number of owners allowed per city. */
    private static final long CITY_CAPACITY = 50L;

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
        if (owner.isNew()) {
            if (owner.getCustomerCode() == null) {
                owner.setCustomerCode(generateCustomerCode(owner.getCity(), owner.getLastName()));
            }
            if (owner.getNamesakeCount() == null) {
                owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
            }
        }
        ownerRepository.save(owner);

    }

    /**
     * Count the existing owners (before this create) that share the given first and last name,
     * compared case-insensitively, as required by the read-only {@code namesakeCount} field.
     */
    private int countNamesakes(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(owner -> firstName.equalsIgnoreCase(owner.getFirstName())
                && lastName.equalsIgnoreCase(owner.getLastName()))
            .count();
    }

    /**
     * Build a customer code formatted {@code '<CITY3>-<LAST3>-<NNNN>'}, where {@code CITY3}
     * is the upper-cased first three letters of the owner's city, {@code LAST3} the
     * upper-cased first three letters of the owner's last name and {@code NNNN} a per-city,
     * 4-digit zero-padded sequence equal to one more than the current number of owners
     * already in that city (e.g. {@code 'LON-SMI-0007'}).
     */
    private String generateCustomerCode(String city, String lastName) {
        String cityName = city == null ? "" : city;
        String city3 = cityName.substring(0, Math.min(3, cityName.length())).toUpperCase();
        String name = lastName == null ? "" : lastName;
        String last3 = name.substring(0, Math.min(3, name.length())).toUpperCase();
        long sequence = countOwnersInCity(cityName) + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Count the existing owners whose city matches the given city, compared case-insensitively,
     * used to derive the per-city sequence of the customer code.
     */
    private long countOwnersInCity(String city) {
        if (city == null) {
            return 0L;
        }
        return ownerRepository.findAll().stream()
            .filter(owner -> city.equalsIgnoreCase(owner.getCity()))
            .count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCityAtCapacity(String city) throws DataAccessException {
        return countOwnersInCity(city) >= CITY_CAPACITY;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTelephoneInUse(String telephone) throws DataAccessException {
        String normalized = normalizeTelephone(telephone);
        if (normalized == null) {
            return false;
        }
        return ownerRepository.findAll().stream()
            .map(owner -> normalizeTelephone(owner.getTelephone()))
            .anyMatch(normalized::equals);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHouseholdInUse(String lastName, String address) throws DataAccessException {
        String normalizedLastName = normalizeHouseholdValue(lastName);
        String normalizedAddress = normalizeHouseholdAddress(address);
        if (normalizedLastName.isEmpty() || normalizedAddress.isEmpty()) {
            return false;
        }
        return ownerRepository.findAll().stream()
            .anyMatch(owner -> normalizedLastName.equals(normalizeHouseholdValue(owner.getLastName()))
                && normalizedAddress.equals(normalizeHouseholdAddress(owner.getAddress())));
    }

    @Override
    @Transactional(readOnly = true)
    public String householdIdFor(String lastName, String address) throws DataAccessException {
        String key = normalizeHouseholdValue(lastName) + "\u0000" + normalizeHouseholdAddress(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }

    /**
     * Normalizes a household field (last name or address) for duplicate comparison by trimming,
     * collapsing runs of whitespace to a single space and lower-casing, so differently cased or
     * spaced but equivalent values compare equal.
     */
    private static String normalizeHouseholdValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an address for household comparison by first applying the canonical address
     * normalization (trim, collapse whitespace, upper-case and expand common abbreviations) and
     * then the shared household-value normalization, so every address comparison uses the same
     * normalized form regardless of how the value was originally supplied.
     */
    private static String normalizeHouseholdAddress(String address) {
        return normalizeHouseholdValue(AddressNormalizer.normalize(address));
    }

    /**
     * Reduces a telephone number to its canonical E.164 form so that differently formatted but
     * equivalent numbers compare equal. A value already stored that cannot form valid E.164
     * (e.g. unusual legacy data) falls back to its digits-only form rather than failing lookup.
     */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        try {
            return TelephoneNormalizer.toE164(telephone);
        } catch (InvalidTelephoneException ex) {
            return telephone.replaceAll("\\D", "");
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
