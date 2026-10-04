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
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private final OwnerMapper ownerMapper;

    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository,
        OwnerMapper ownerMapper) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
        this.ownerMapper = ownerMapper;
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
        if (owner.getCustomerCode() == null) {
            owner.setCustomerCode(generateCustomerCode(owner));
        }
        ownerRepository.save(owner);

    }

    /**
     * Build a {@code customerCode} of the form {@code <CITY3>-<LAST3>-<NNNN>}: the upper-cased
     * first three letters of the owner's city, a hyphen, the upper-cased first three letters of
     * the owner's last name, a hyphen, and a per-city 4-digit zero-padded sequence equal to one
     * more than the number of owners already registered in that city (e.g. {@code TOW-SMI-0007}).
     */
    private String generateCustomerCode(Owner owner) {
        String city = owner.getCity() == null ? "" : owner.getCity();
        int sequence = (int) ownerRepository.findAll().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count() + 1;
        return String.format("%s-%s-%04d", prefix3(city), prefix3(owner.getLastName()), sequence);
    }

    /**
     * The upper-cased first three letters of {@code value} (fewer if it is shorter), treating a
     * {@code null} value as empty. This is the shared rule behind both the CITY3 and LAST3
     * segments of a {@code customerCode}.
     */
    private static String prefix3(String value) {
        String s = value == null ? "" : value;
        return s.substring(0, Math.min(3, s.length())).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTelephoneInUse(String telephone) throws DataAccessException {
        String normalized = ownerMapper.normalizeTelephone(telephone);
        if (normalized == null) {
            return false;
        }
        return ownerRepository.findAll().stream()
            .map(existing -> ownerMapper.normalizeTelephone(existing.getTelephone()))
            .anyMatch(normalized::equals);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHouseholdInUse(String lastName, String address) throws DataAccessException {
        String normalizedLastName = normalizeForComparison(lastName);
        String normalizedAddress = normalizeForComparison(address);
        if (normalizedLastName == null || normalizedAddress == null) {
            return false;
        }
        return ownerRepository.findAll().stream()
            .anyMatch(existing -> inHousehold(existing, normalizedLastName, normalizedAddress));
    }

    /**
     * Canonicalize a free-text value for equality comparison: trim, collapse every run of
     * whitespace to a single space and lower-case the result, so values that differ only in
     * letter case or incidental whitespace are treated as equal. Shared by the household
     * (last name / address) and per-city matching rules.
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Single definition of household membership: an owner belongs to the household identified by
     * the given already-normalized last name and address when its own normalized last name and
     * address match. Keeping this in one place means the household-equality rule is changed once.
     */
    private boolean inHousehold(Owner existing, String normalizedLastName, String normalizedAddress) {
        return normalizedLastName.equals(normalizeForComparison(existing.getLastName()))
            && normalizedAddress.equals(normalizeForComparison(existing.getAddress()));
    }

    @Override
    @Transactional
    public String assignHousehold(Owner owner) throws DataAccessException {
        String householdId = generateHouseholdId(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        String normalizedLastName = normalizeForComparison(owner.getLastName());
        String normalizedAddress = normalizeForComparison(owner.getAddress());
        if (normalizedLastName != null && normalizedAddress != null) {
            // Back-fill existing members of the household so the joining owner and the
            // owner(s) already at the address share the same stable identifier.
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getHouseholdId() == null
                    && inHousehold(existing, normalizedLastName, normalizedAddress)) {
                    existing.setHouseholdId(householdId);
                    ownerRepository.save(existing);
                }
            }
        }
        return householdId;
    }

    /**
     * Derive a stable household identifier from the normalized last name and address, so that
     * every owner sharing a household (same last name and address, compared case-insensitively
     * with internal whitespace collapsed) maps to the same value. The identifier is the first
     * eight upper-case hex characters of the SHA-256 of {@code <lastName>|<address>}, prefixed
     * with {@code 'H-'} (e.g. {@code H-3F5A9C2E}).
     */
    private String generateHouseholdId(String lastName, String address) {
        String key = normalizeForComparison(lastName) + "|" + normalizeForComparison(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("H-");
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countNamesakes(String firstName, String lastName) throws DataAccessException {
        if (firstName == null || lastName == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    @Override
    @Transactional(readOnly = true)
    public int countOwnersInCity(String city) throws DataAccessException {
        String normalized = normalizeForComparison(city);
        if (normalized == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> normalized.equals(normalizeForComparison(existing.getCity())))
            .count();
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
