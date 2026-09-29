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
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.CityCapacityExceededException;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.validation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.validation.IdentityKeyNormalizer;
import org.springframework.samples.petclinic.rest.validation.IdentityNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        ownerRepository.save(owner);

    }

    @Override
    @Transactional
    public void createOwner(Owner owner) throws DataAccessException {
        createOwner(owner, false);
    }

    @Override
    @Transactional
    public void createOwner(Owner owner, boolean sharesHousehold) throws DataAccessException {
        rejectWhenDailyLimitReached(owner);
        rejectWhenCityAtCapacity(owner);
        List<Owner> householdMembers = assignHousehold(owner, sharesHousehold);
        rejectWhenDuplicateIdentity(owner);
        owner.setHouseholdMemberCount(householdMembers.size() + 1);
        if (owner.getCustomerCode() == null) {
            owner.setCustomerCode(deriveCustomerCode(owner));
        }
        if (owner.getNamesakeCount() == null) {
            owner.setNamesakeCount(countNamesakes(owner));
        }
        owner.setBulkSignupWarning(exceedsBulkSignupThreshold(owner));
        ownerRepository.save(owner);
    }

    /** Number of owners created on a single day beyond which the bulk-signup warning is raised. */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80L;

    /**
     * Whether more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created
     * today (compared by {@code registrationDate}), evaluated before this owner is persisted so it
     * reflects the owners present at creation time. The new owner's registration date, defaulted to
     * today by the controller, determines which day is counted — the same accumulation path as the
     * daily-limit rule.
     */
    private boolean exceedsBulkSignupThreshold(Owner owner) {
        LocalDate day = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        return countOwnersRegisteredOn(day) > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Count the existing owners that already share this owner's first name and last name,
     * compared case-insensitively (with surrounding/collapsed whitespace ignored). Evaluated
     * before the owner is persisted, so it reflects the owners present at creation time.
     */
    private int countNamesakes(Owner owner) {
        String firstName = IdentityNormalizer.normalize(owner.getFirstName());
        String lastName = IdentityNormalizer.normalize(owner.getLastName());
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> firstName.equals(IdentityNormalizer.normalize(existing.getFirstName()))
                && lastName.equals(IdentityNormalizer.normalize(existing.getLastName())))
            .count();
    }

    /**
     * Reconcile the owner's household and return its existing members. Owners are in the same
     * household when their last name and address match (compared case-insensitively with collapsed
     * whitespace). When another owner already lives there and {@code sharesHousehold} is set, the
     * new owner and every existing member are given the same stable shared {@code householdId} so
     * they form one household; without the opt-in the owner is treated as living alone (no shared
     * identifier). Duplicate detection no longer happens here: it is consolidated into the derived
     * {@code identityKey} (see {@link #rejectWhenDuplicateIdentity(Owner)}). The returned list is the
     * existing members that make up this owner's household (empty when the owner lives alone), so
     * callers can derive the household size after this create.
     */
    private List<Owner> assignHousehold(Owner owner, boolean sharesHousehold) {
        if (!sharesHousehold) {
            return List.of();
        }
        String lastName = IdentityNormalizer.normalize(owner.getLastName());
        String address = AddressNormalizer.canonical(owner.getAddress());
        List<Owner> householdMembers = ownerRepository.findAll().stream()
            .filter(existing -> lastName.equals(IdentityNormalizer.normalize(existing.getLastName()))
                && address.equals(AddressNormalizer.canonical(existing.getAddress())))
            .toList();
        if (householdMembers.isEmpty()) {
            return householdMembers;
        }
        String householdId = householdMembers.stream()
            .map(Owner::getHouseholdId)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> deriveHouseholdId(lastName, address));
        owner.setHouseholdId(householdId);
        for (Owner member : householdMembers) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
        return householdMembers;
    }

    /**
     * Derive a stable, shared household identifier from the normalized last name and address, so
     * every owner in the same household deterministically maps to the same value.
     */
    private static String deriveHouseholdId(String normalizedLastName, String normalizedAddress) {
        String key = normalizedLastName + "\n" + normalizedAddress;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * Reject the create when the owner's derived {@code identityKey} exactly matches that of an
     * existing owner. The key —
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId} — is the single value
     * into which the former separate telephone, e-mail and household duplicate checks are
     * consolidated: only an exact whole-key match is a duplicate, so two members of the same
     * household with different telephones (hence different keys) are both allowed. Evaluated after
     * the household has been reconciled (so the {@code householdId} segment is settled) and before
     * the owner is persisted, so it reflects the owners present at creation time.
     */
    private void rejectWhenDuplicateIdentity(Owner owner) {
        String identityKey = IdentityKeyNormalizer.derive(owner);
        boolean duplicate = ownerRepository.findAll().stream()
            .anyMatch(existing -> identityKey.equals(IdentityKeyNormalizer.derive(existing)));
        if (duplicate) {
            throw new DuplicateIdentityException(
                "an owner with identity key " + identityKey + " already exists");
        }
    }

    /** Maximum number of owners that may be created on any single day (by registration date). */
    private static final long DAILY_OWNER_LIMIT = 100L;

    /**
     * Reject the create when {@value #DAILY_OWNER_LIMIT} or more owners have already been created
     * today (compared by {@code registrationDate}). Evaluated before the owner is persisted, so it
     * reflects the owners present at creation time. The new owner's own registration date, defaulted
     * to today by the controller, determines which day the limit is checked against.
     */
    private void rejectWhenDailyLimitReached(Owner owner) {
        LocalDate day = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        if (countOwnersRegisteredOn(day) >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                DAILY_OWNER_LIMIT + " or more owners have already been created on " + day);
        }
    }

    /**
     * Count the existing owners whose {@code registrationDate} equals the given day. Evaluated
     * before the owner is persisted, so it reflects the owners present at creation time.
     */
    private long countOwnersRegisteredOn(LocalDate day) {
        return ownerRepository.findAll().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
    }

    /** Maximum number of owners allowed to live in any single city. */
    private static final long CITY_CAPACITY = 50L;

    /**
     * Reject the create when the owner's city already contains {@value #CITY_CAPACITY} or more
     * owners (compared case-insensitively with collapsed whitespace). Evaluated before the owner
     * is persisted, so it reflects the owners present at creation time.
     */
    private void rejectWhenCityAtCapacity(Owner owner) {
        if (countOwnersInCity(owner) >= CITY_CAPACITY) {
            throw new CityCapacityExceededException(
                "city " + owner.getCity() + " already contains " + CITY_CAPACITY + " or more owners");
        }
    }

    /**
     * Count the existing owners that already live in this owner's city, compared
     * case-insensitively (with surrounding/collapsed whitespace ignored). Evaluated before the
     * owner is persisted, so it reflects the owners present at creation time.
     */
    private long countOwnersInCity(Owner owner) {
        String city = IdentityNormalizer.normalize(owner.getCity());
        return ownerRepository.findAll().stream()
            .filter(existing -> city.equals(IdentityNormalizer.normalize(existing.getCity())))
            .count();
    }

    /** Region-to-inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region code used when the postcode maps to no known region (or is absent). */
    private static final String UNKNOWN_REGION = "UNKNOWN";

    /**
     * Builds the customer code {@code '<REGION>-<HASH8>'}: the region code derived from the
     * owner's postcode, a hyphen, and the first eight upper-case hexadecimal characters of the
     * SHA-256 digest over the owner's normalized (E.164) telephone concatenated with the last
     * name. No sequence number is used; the identity is fully derived from the region and hash.
     */
    private String deriveCustomerCode(Owner owner) {
        String region = regionForPostcode(owner.getPostcode());
        String telephone = TelephoneNormalizer.toE164(owner.getTelephone());
        if (telephone == null) {
            telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        return region + "-" + sha256Hex8(telephone + lastName);
    }

    /**
     * Returns the region code whose inclusive postcode range contains {@code postcode}
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), or
     * {@code "UNKNOWN"} when the postcode is absent, not a 4-digit value, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return UNKNOWN_REGION;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return UNKNOWN_REGION;
    }

    /**
     * Returns the first eight upper-case hexadecimal characters (the first four bytes) of the
     * SHA-256 digest of the UTF-8 bytes of {@code value}.
     */
    private static String sha256Hex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
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
