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
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.Sha256Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    /** Maximum number of owners allowed per city. */
    private static final long CITY_CAPACITY = 50L;

    /** Maximum number of owners that may be created on a single day (by registration date). */
    private static final long DAILY_CAPACITY = 100L;

    /** Above this many owners created today, reads carry a bulk-signup warning. */
    private static final long BULK_SIGNUP_THRESHOLD = 80L;

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
        Collection<Owner> owners = ownerRepository.findAll();
        return decorateWithHouseholdMemberCount(owners, owners);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Owner> findOwners(String lastName, Pageable pageable) throws DataAccessException {
        Page<Owner> owners = (lastName != null)
            ? ownerRepository.findByLastName(lastName, pageable)
            : ownerRepository.findAll(pageable);
        return decorateWithHouseholdMemberCount(owners, owners.getContent());
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
        return decorateWithHouseholdMemberCount(findEntityById(() -> ownerRepository.findById(id)));
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
            LocalDate effectiveDate = owner.getRegistrationDate() != null
                ? owner.getRegistrationDate()
                : LocalDate.now();
            owner.setRegistrationDate(toBusinessDay(effectiveDate));
            if (owner.getCustomerCode() == null) {
                owner.setCustomerCode(generateCustomerCode(owner));
            }
            if (owner.getNamesakeCount() == null) {
                owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
            }
            if (owner.getPossibleDuplicate() == null) {
                Integer possibleDuplicateOf = findPossibleDuplicateOf(owner);
                owner.setPossibleDuplicateOf(possibleDuplicateOf);
                owner.setPossibleDuplicate(possibleDuplicateOf != null);
            }
        }
        ownerRepository.save(owner);
        decorateWithHouseholdMemberCount(owner);
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
     * Build a customer code formatted {@code '<REGION>-<HASH8>'}, where {@code REGION} is the
     * canonical region derived from the owner's postcode (falling back to the city-to-region
     * table, then {@code 'UNKNOWN'}, as resolved by {@link LocalityResolver}) and {@code HASH8}
     * is the first 8 upper-case hexadecimal characters of the SHA-256 digest over the owner's
     * normalized telephone concatenated with the last name (e.g. {@code 'NSW-1A2B3C4D'}). There
     * is no sequence number: identity is fully determined by the region and the telephone/last
     * name hash.
     */
    private String generateCustomerCode(Owner owner) {
        String region = LocalityResolver.resolve(owner.getCity(), owner.getPostcode());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        return region + "-" + Sha256Hex.upperHexPrefix(telephone + lastName, 8);
    }

    /**
     * Count the existing owners whose city matches the given city, compared case-insensitively,
     * used by the per-city capacity rule ({@link #isCityAtCapacity}).
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
    public boolean isDailyRegistrationAtCapacity() throws DataAccessException {
        return countRegisteredToday() >= DAILY_CAPACITY;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBulkSignupDay() throws DataAccessException {
        return countRegisteredToday() > BULK_SIGNUP_THRESHOLD;
    }

    /**
     * Count the owners whose registration date equals today's business day, used by the
     * daily create-limit and bulk-signup rules.
     */
    private long countRegisteredToday() {
        LocalDate businessDay = toBusinessDay(LocalDate.now());
        return ownerRepository.findAll().stream()
            .filter(owner -> businessDay.equals(owner.getRegistrationDate()))
            .count();
    }

    /**
     * Rolls a registration date forward onto a business day: a Saturday or Sunday is advanced
     * to the following Monday, while a weekday is returned unchanged. Applied to the effective
     * registration date (whether supplied or defaulted to the server date) so that the stored
     * {@code registrationDate}, every value derived from it (such as the membership number's year
     * segment) and the per-day create-limit all use the adjusted business day.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isIdentityKeyInUse(Owner owner) throws DataAccessException {
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .filter(existing -> existing != owner)
            .map(Owner::getIdentityKey)
            .anyMatch(identityKey::equals);
    }

    /**
     * Resolve the id of an existing owner that the given (not yet persisted) owner is a <em>possible</em>
     * duplicate of, or {@code null} when there is none. A possible duplicate shares an existing owner's
     * last name (compared case-insensitively) and postcode while carrying a different telephone, so it is
     * not a hard duplicate (its identity key is unique) but is worth flagging. When several existing owners
     * match, the one with the lowest id is returned so the result is deterministic. Computed once, at
     * creation, to drive the read-only {@code possibleDuplicate}/{@code possibleDuplicateOf} fields.
     */
    private Integer findPossibleDuplicateOf(Owner owner) {
        if (owner.getLastName() == null || owner.getPostcode() == null) {
            return null;
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> existing != owner && existing.getId() != null)
            .filter(existing -> owner.getLastName().equalsIgnoreCase(existing.getLastName()))
            .filter(existing -> owner.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> !Objects.equals(owner.getTelephone(), existing.getTelephone()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String householdIdFor(String lastName, String address) throws DataAccessException {
        String key = normalizeHouseholdValue(lastName) + "\u0000" + normalizeHouseholdAddress(address);
        return Sha256Hex.upperHexPrefix(key, 16);
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
     * Populate each given owner's transient {@code householdMemberCount} with the current number of
     * owners that share its {@code householdId}. Owners without a household id are left at a count
     * of zero.
     *
     * <p>The household sizes are computed in a single pass over the owner table regardless of how
     * many owners are being decorated, so decorating a whole page does not re-scan per element.
     */
    private <T> T decorateWithHouseholdMemberCount(T result, Collection<Owner> owners) {
        if (!owners.isEmpty()) {
            Map<String, Long> sizeByHousehold = ownerRepository.findAll().stream()
                .filter(owner -> owner.getHouseholdId() != null)
                .collect(Collectors.groupingBy(Owner::getHouseholdId, Collectors.counting()));
            for (Owner owner : owners) {
                long size = owner.getHouseholdId() == null
                    ? 0L
                    : sizeByHousehold.getOrDefault(owner.getHouseholdId(), 0L);
                owner.setHouseholdMemberCount((int) size);
            }
        }
        return result;
    }

    /**
     * Decorate a single (possibly {@code null}) owner with its household member count and return it.
     */
    private Owner decorateWithHouseholdMemberCount(Owner owner) {
        return decorateWithHouseholdMemberCount(owner, owner == null ? List.of() : List.of(owner));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        Collection<Owner> owners = ownerRepository.findByLastName(lastName);
        return decorateWithHouseholdMemberCount(owners, owners);
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
