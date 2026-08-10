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
        if (owner.isNew() && owner.getCustomerCode() == null) {
            owner.setCustomerCode(generateCustomerCode(owner));
        }
        if (owner.isNew() && owner.getNamesakeCount() == null) {
            owner.setNamesakeCount(countNamesakes(owner));
        }
        if (owner.isNew() && owner.getHouseholdMemberCount() == null) {
            owner.setHouseholdMemberCount(countHouseholdMembers(owner));
        }
        ownerRepository.save(owner);

    }

    /**
     * Count the members of the new owner's household after this create: the existing owners
     * that share the given owner's last name (compared case-insensitively with collapsed
     * whitespace) and postcode - the same (last name, postcode) pair from which the household
     * identifier is derived - plus the owner being created. Invoked before the new owner is
     * persisted.
     */
    private int countHouseholdMembers(Owner owner) {
        String normalizedLastName = normalizeHouseholdField(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        long existing = ownerRepository.findAll().stream()
            .filter(other -> normalizeHouseholdField(other.getLastName()).equals(normalizedLastName)
                && postcode.equals(other.getPostcode() == null ? "" : other.getPostcode()))
            .count();
        return (int) existing + 1;
    }

    /**
     * Collapses surrounding and internal whitespace and lower-cases a value so household fields
     * can be compared case-insensitively with collapsed whitespace. A {@code null} value
     * normalizes to the empty string.
     */
    private String normalizeHouseholdField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Count the existing owners that share the given owner's first name and last name,
     * compared case-insensitively. Invoked before the new owner is persisted, so the
     * result is the number of namesakes that existed at the time of creation.
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
     * Build the owner's customer code in the form '<REGION>-<HASH8>' where REGION is the region
     * code derived from the owner's postcode (falling back to the city) and HASH8 is the first
     * 8 upper-case hex characters of the SHA-256 digest over the owner's normalized telephone
     * concatenated with the owner's last name. There is no sequence number: the identity is
     * derived entirely from the region and the telephone/last-name hash.
     */
    private String generateCustomerCode(Owner owner) {
        String base = regionFor(owner) + "-" + hash8(owner);
        return deduplicateCustomerCode(base);
    }

    /**
     * Ensure the computed customer code is unique across existing owners. When the base code
     * collides with an existing owner's {@code customerCode}, append {@code '-<n>'} with the
     * smallest {@code n} of 2 or more that makes it unique, and return that de-duplicated code.
     */
    private String deduplicateCustomerCode(String base) {
        Set<String> existing = ownerRepository.findAll().stream()
            .map(Owner::getCustomerCode)
            .filter(code -> code != null)
            .collect(java.util.stream.Collectors.toSet());
        if (!existing.contains(base)) {
            return base;
        }
        int n = 2;
        while (existing.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /**
     * Derive the owner's region, preferring the postcode over the city. The postcode is looked up
     * against the canonical region ranges first ({@code NSW 2000-2099}, {@code VIC 3000-3099},
     * {@code QLD 4000-4099}); only when the postcode is absent or falls in no known range does the
     * derivation fall back to the fixed city-to-region table ({@code Sydney->NSW},
     * {@code Melbourne->VIC}, {@code Brisbane->QLD}). The result is {@code "UNKNOWN"} when neither
     * the postcode nor the city resolves to a region.
     */
    private String regionFor(Owner owner) {
        String region = regionFromPostcode(owner.getPostcode());
        if (region != null) {
            return region;
        }
        return regionFromCity(owner.getCity());
    }

    /**
     * Look up the canonical region for a postcode by its range ({@code NSW 2000-2099},
     * {@code VIC 3000-3099}, {@code QLD 4000-4099}), returning {@code null} when the postcode is
     * absent, non-numeric or in no known range.
     */
    private String regionFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        if (value >= 2000 && value <= 2099) {
            return "NSW";
        }
        if (value >= 3000 && value <= 3099) {
            return "VIC";
        }
        if (value >= 4000 && value <= 4099) {
            return "QLD";
        }
        return null;
    }

    /**
     * Look up the canonical region for a city using the fixed city-to-region table, returning
     * {@code "UNKNOWN"} when the city is absent or not in the table.
     */
    private String regionFromCity(String city) {
        if (city == null) {
            return "UNKNOWN";
        }
        return switch (city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Compute the first 8 upper-case hex characters of the SHA-256 digest over the owner's
     * normalized telephone concatenated with the owner's last name.
     */
    private String hash8(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((telephone + lastName).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
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
