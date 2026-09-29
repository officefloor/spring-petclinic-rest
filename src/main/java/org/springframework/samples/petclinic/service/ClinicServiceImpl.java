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
import org.springframework.samples.petclinic.util.E164Telephone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
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
        if (owner.getTelephone() != null) {
            owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        }
        if (owner.getEmail() != null) {
            owner.setEmail(normalizeEmail(owner.getEmail()));
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        if (owner.getCustomerCode() == null) {
            owner.setCustomerCode(generateCustomerCode(owner.getLastName()));
        }
        assertTelephoneUnique(owner, owner.getTelephone());
        List<Owner> household = findHouseholdMembers(owner);
        assertHouseholdAllowed(owner, household);
        assignHousehold(owner, household);
        ownerRepository.save(owner);
    }

    /**
     * Build a {@code customerCode} formatted {@code '<LAST3>-<NNNN>'} where LAST3 is the
     * upper-cased first three letters of {@code lastName} and NNNN is a global 4-digit
     * zero-padded sequence equal to one more than the current number of owners.
     */
    private String generateCustomerCode(String lastName) {
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        long sequence = ownerRepository.findAll().size() + 1L;
        return String.format("%s-%04d", prefix, sequence);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    private static String normalizeTelephone(String telephone) {
        return telephone == null ? null : E164Telephone.toE164(telephone);
    }

    /**
     * Rejects saving {@code owner} when its {@code normalized} telephone is already used by any
     * other owner, so no two owners can share a telephone number.
     *
     * @throws DuplicateTelephoneException if another owner already has the same normalized telephone
     */
    private void assertTelephoneUnique(Owner owner, String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId())
                    && normalized.equals(normalizeTelephone(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(normalized);
            }
        }
    }

    /**
     * Returns the existing owners that share both {@code lastName} and {@code address} (compared
     * case-insensitively with collapsed whitespace) with a new {@code owner} — i.e. the members of
     * the household it would join. Returns an empty list for an update to an existing owner or when
     * either household value is blank, so household rules only apply to genuine new members.
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        if (!owner.isNew()) {
            return List.of();
        }
        String lastName = normalizeHouseholdValue(owner.getLastName());
        String address = normalizeHouseholdValue(owner.getAddress());
        if (lastName.isEmpty() || address.isEmpty()) {
            return List.of();
        }
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.getId().equals(owner.getId())
                    && lastName.equals(normalizeHouseholdValue(existing.getLastName()))
                    && address.equals(normalizeHouseholdValue(existing.getAddress()))) {
                household.add(existing);
            }
        }
        return household;
    }

    /**
     * Pure guard: rejects creating {@code owner} when it would join a non-empty {@code household}
     * without acknowledging the shared household via {@link Owner#isSharesHousehold()}. Returns
     * without side effects otherwise.
     *
     * @throws DuplicateHouseholdException if another owner already lives at the same household and
     *                                     the request does not acknowledge the shared household
     */
    private void assertHouseholdAllowed(Owner owner, List<Owner> household) {
        if (!household.isEmpty() && !owner.isSharesHousehold()) {
            throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
        }
    }

    /**
     * Assigns household membership when {@code owner} joins a non-empty {@code household}: the new
     * owner and every matching existing member that lacks one are given the same stable
     * {@code householdId}, and any updated existing members are persisted. No-op when the household
     * is empty.
     */
    private void assignHousehold(Owner owner, List<Owner> household) {
        if (household.isEmpty()) {
            return;
        }
        String householdId = resolveHouseholdId(owner, household);
        owner.setHouseholdId(householdId);
        for (Owner existing : household) {
            if (existing.getHouseholdId() == null || existing.getHouseholdId().isBlank()) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /**
     * Returns the {@code householdId} shared by a household: the first non-blank identifier already
     * carried by an existing member, or otherwise a freshly derived stable identifier for the
     * joining {@code owner}'s normalized lastName and address.
     */
    private String resolveHouseholdId(Owner owner, List<Owner> household) {
        for (Owner existing : household) {
            if (existing.getHouseholdId() != null && !existing.getHouseholdId().isBlank()) {
                return existing.getHouseholdId();
            }
        }
        return generateHouseholdId(normalizeHouseholdValue(owner.getLastName()),
            normalizeHouseholdValue(owner.getAddress()));
    }

    /**
     * Derives a stable {@code householdId} formatted {@code 'HH-<12 upper-hex>'} from the SHA-256
     * digest of the normalized {@code lastName} and {@code address}, so all owners in the same
     * household deterministically share the same identifier.
     */
    private static String generateHouseholdId(String lastName, String address) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((lastName + "\u0000" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return "HH-" + sb;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a household comparison value by trimming, collapsing runs of whitespace into a
     * single space and lower-casing, so comparisons ignore case and incidental whitespace.
     */
    private static String normalizeHouseholdValue(String value) {
        if (value == null) {
            return "";
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
