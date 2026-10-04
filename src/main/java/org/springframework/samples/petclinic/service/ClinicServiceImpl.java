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
import org.springframework.samples.petclinic.util.BusinessDays;
import org.springframework.samples.petclinic.util.Regions;
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        // Soft delete: the owner's row is retained and flagged deleted rather than removed, so it
        // can still be read back while being excluded from the create-time duplicate/identity checks.
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
        if (owner.getMemberId() == null) {
            owner.setMemberId(generateMemberId(owner));
        }
        ownerRepository.save(owner);

    }

    /**
     * Build a unified {@code memberId} of the form {@code <REGION><FY><HASH8><CHK>}: the region code
     * derived from the owner's postcode (see {@link Regions#forPostcode}), the 2-digit fiscal year of
     * the owner's (business-day-adjusted) registration date, {@code HASH8} — the first eight
     * upper-case hex characters of the SHA-256 of the owner's normalized telephone concatenated with
     * its last name — and {@code CHK}, a single Luhn check digit over the digits of
     * {@code <REGION><FY><HASH8>} (e.g. {@code NSW263F5A9C2E4}). Aside from the fiscal year the
     * identity carries no sequence number, so it depends only on the owner's own postcode, telephone,
     * last name and registration date.
     *
     * <p>If the computed id collides with an existing owner's {@code memberId}, {@code -<n>} is
     * appended with the smallest {@code n} of 2 or more that makes it unique, and the de-duplicated
     * id is returned.
     */
    private String generateMemberId(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String region = Regions.forPostcode(owner.getPostcode());
        LocalDate registrationDate = BusinessDays.effectiveRegistrationDate(owner.getRegistrationDate());
        int fiscalYear = BusinessDays.fiscalYearEndYear(registrationDate) % 100;
        String prefix = String.format("%s%02d%s", region, fiscalYear, hash8(telephone + lastName));
        String base = prefix + luhn(prefix);
        return deduplicateMemberId(base, owner);
    }

    /**
     * Return {@code base} if no other owner already uses it as a {@code memberId}; otherwise
     * append {@code -<n>} with the smallest {@code n >= 2} that is not already taken, so distinct
     * owners always receive distinct member ids.
     */
    private String deduplicateMemberId(String base, Owner owner) {
        Set<String> existing = ownerRepository.findAll().stream()
            .filter(other -> other != owner)
            .map(Owner::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
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
     * A single Luhn check digit (0-9) over the digits contained in {@code value}; non-digit
     * characters (the region segment and any hex letters of the hash) are ignored. This is the
     * {@code CHK} segment of a {@code memberId}.
     */
    private static int luhn(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * The first eight upper-case hex characters (the first four bytes) of the SHA-256 of
     * {@code value}, the {@code HASH8} segment of a {@code memberId}.
     */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
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
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHouseholdInUse(Owner owner) throws DataAccessException {
        return !findHouseholdMembers(owner).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findHouseholdMembers(Owner owner) throws DataAccessException {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return List.of();
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> existing.getId() != null)
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .collect(Collectors.toList());
    }

    /**
     * Canonicalize a free-text value for equality comparison: trim, collapse every run of
     * whitespace to a single space and lower-case the result, so values that differ only in
     * letter case or incidental whitespace are treated as equal. Used by the per-city matching
     * rule.
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isIdentityKeyInUse(Owner owner) throws DataAccessException {
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer findPossibleDuplicateOwnerId(Owner owner) throws DataAccessException {
        if (owner.getLastName() == null || owner.getPostcode() == null) {
            return null;
        }
        String soundex = Soundex.encode(owner.getLastName());
        String postcode = owner.getPostcode();
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .filter(existing -> existing.getId() != null)
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> soundex.equals(Soundex.encode(existing.getLastName())))
            .filter(existing -> postcode.equals(existing.getPostcode()))
            .filter(existing -> !identityKey.equals(existing.getIdentityKey()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
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
    public int countOwnersRegisteredOn(LocalDate date) throws DataAccessException {
        if (date == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
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
