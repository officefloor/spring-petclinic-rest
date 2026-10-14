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
import org.springframework.samples.petclinic.rest.validation.OwnerPostcode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HexFormat;
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
        owner.setHouseholdId(householdIdOf(owner));
        if (owner.isNew()) {
            owner.setCustomerCode(uniqueCustomerCode(customerCode(owner)));
            owner.setNamesakeCount((int) ownerRepository.countByFirstNameAndLastNameIgnoringCase(
                lowerCase(owner.getFirstName()), lowerCase(owner.getLastName())));
            owner.setHouseholdSize(owner.getHouseholdId() == null ? 1
                : (int) ownerRepository.countByHouseholdId(owner.getHouseholdId()) + 1);
        }
        ownerRepository.save(owner);

    }

    @Override
    @Transactional(readOnly = true)
    public boolean ownerIdentityKeyExists(Owner owner) throws DataAccessException {
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findByTelephone(owner.getTelephone()).stream()
            .anyMatch(existing -> !existing.getId().equals(owner.getId())
                && !existing.isDeleted()
                && existing.getIdentityKey().equals(identityKey));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer findPossibleDuplicateOf(Owner owner) throws DataAccessException {
        String householdId = householdIdOf(owner);
        if (householdId == null) {
            return null;
        }
        return ownerRepository.findByLastNameIgnoringCase(lowerCase(owner.getLastName())).stream()
            .filter(existing -> !existing.getId().equals(owner.getId())
                && !existing.isDeleted()
                && householdId.equals(existing.getHouseholdId())
                && !Objects.equals(owner.getTelephone(), existing.getTelephone()))
            .map(Owner::getId)
            .min(Integer::compare)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOwnersInCity(String city) throws DataAccessException {
        return ownerRepository.countByCityIgnoringCase(lowerCase(city));
    }

    @Override
    @Transactional(readOnly = true)
    public long countOwnersRegisteredOn(LocalDate registrationDate) throws DataAccessException {
        return ownerRepository.countByRegistrationDate(registrationDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean householdExists(Owner owner) throws DataAccessException {
        String householdId = householdIdOf(owner);
        return householdId != null && ownerRepository.countNotDeletedByHouseholdId(householdId) > 0;
    }

    @Override
    public String householdIdOf(Owner owner) {
        if (owner.getLastName() == null || owner.getPostcode() == null) {
            return null;
        }
        return sha256Hex(normalizeForComparison(owner.getLastName()) + "|" + owner.getPostcode()).substring(0, 12);
    }

    /** Trims the value, collapses runs of whitespace into a single space and lower-cases it. */
    private static String normalizeForComparison(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String lowerCase(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
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

    /**
     * De-duplicates a computed customer code: returns it unchanged when no existing owner has it, otherwise
     * appends '-&lt;n&gt;' with the smallest n &gt;= 2 that makes it unique.
     */
    private String uniqueCustomerCode(String code) {
        if (ownerRepository.countByCustomerCode(code) == 0) {
            return code;
        }
        int n = 2;
        while (ownerRepository.countByCustomerCode(code + "-" + n) > 0) {
            n++;
        }
        return code + "-" + n;
    }

    /**
     * Builds a customer code '&lt;REGION&gt;-&lt;HASH8&gt;': the region derived from the postcode (see
     * {@link OwnerPostcode#regionOf(String, String)}) and the first 8 upper-case hex characters of the
     * SHA-256 of the UTF-8 bytes of the normalized telephone followed by the last name.
     */
    private static String customerCode(Owner owner) {
        String region = OwnerPostcode.regionOf(owner.getPostcode(), owner.getCity());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        return region + "-" + sha256Hex(telephone + lastName).substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
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
