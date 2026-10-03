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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.samples.petclinic.util.PostcodeRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
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
        // Whether this owner was accepted into an existing household because the request declared it
        // shares one (sharesHousehold). A declared member is deliberately not treated as a suspected
        // duplicate below.
        boolean declaredHouseholdMember = false;
        if (owner.isNew()) {
            // The householdId is deterministic: the first 12 hex characters of SHA-256 over
            // (normalizedLastName | postcode), so owners with the same last name and postcode share
            // it automatically. It is part of the owner's identityKey, so it is resolved first.
            owner.setHouseholdId(computeHouseholdId(owner));
            // The household is keyed on (last name, postcode), so the members are the existing owners
            // sharing both with this owner.
            List<Owner> householdMembers = findHouseholdMembers(owner);
            // Size of the household after this create: the existing members plus this new owner.
            owner.setHouseholdMemberCount(householdMembers.size() + 1);
            // A second owner in an existing household is the same household and is rejected as a
            // household duplicate, UNLESS the request declares (via sharesHousehold) that it genuinely
            // shares the household. sharesHousehold only bypasses this block; it no longer creates the
            // link, which is now derived from (last name, postcode).
            if (!householdMembers.isEmpty()) {
                if (!owner.isSharesHousehold()) {
                    throw new HouseholdDuplicateException(owner.getHouseholdId());
                }
                declaredHouseholdMember = true;
            }
        }
        // Single consolidated duplicate check: reject only when the new owner's WHOLE identityKey
        // (normalizedTelephone | email-or-empty | householdId) equals an existing owner's. This
        // subsumes the former separate telephone, email and household checks.
        if (owner.isNew() && isIdentityKeyAlreadyUsed(owner)) {
            throw new DuplicateIdentityException(owner.getIdentityKey());
        }
        // Soft-match: not a hard identityKey duplicate, but shares an existing owner's last name and
        // postcode while carrying a different telephone. Still created, but flagged as a possible
        // duplicate pointing at the matching owner. A declared household member is not a suspected
        // duplicate, so it is never flagged.
        if (owner.isNew() && !declaredHouseholdMember) {
            Owner softMatch = findPossibleDuplicate(owner);
            if (softMatch != null) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(softMatch.getId());
            }
        }
        if (owner.isNew()
            && countOwnersInCity(owner) >= CityOwnerLimitExceededException.MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitExceededException(owner.getCity());
        }
        if (owner.isNew()
            && countOwnersRegisteredOn(registrationDate) >= DailyOwnerLimitExceededException.MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(registrationDate);
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
     * Report whether {@code owner}'s derived {@code identityKey} is already used by any existing
     * owner. The key consolidates telephone, email and household into one value, so two owners
     * collide only when their whole key is equal (see {@link Owner#getIdentityKey()}). By this point
     * the owner's {@code householdId} has been resolved, so its key is complete.
     */
    private boolean isIdentityKeyAlreadyUsed(Owner owner) {
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    /**
     * Find an existing owner this (new) owner soft-matches against: one sharing the same last name
     * (compared case-insensitively with collapsed whitespace, like household matching) and the same
     * postcode, but carrying a different telephone. Returns the lowest-id such owner, or {@code null}
     * when the owner has no last name or postcode to match on, or nothing matches. The caller only
     * invokes this once the hard {@code identityKey} duplicate check has already passed.
     */
    private Owner findPossibleDuplicate(Owner owner) {
        String key = householdKey(owner);
        if (key == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        return ownerRepository.findAll().stream()
            .filter(existing -> key.equals(householdKey(existing))
                && !Objects.equals(telephone, existing.getTelephone()))
            .min(Comparator.comparing(Owner::getId))
            .orElse(null);
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
     * Find the owners that already belong to the same household as {@code owner}, i.e. share both its
     * last name and its postcode. Last names are compared case-insensitively with collapsed
     * whitespace; postcodes are compared exactly. This is the same keying used by
     * {@link #computeHouseholdId(Owner)}, so these are exactly the existing owners that carry the
     * owner's computed {@code householdId}. Returns an empty list when the owner has no last name or
     * postcode to match on.
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        String key = householdKey(owner);
        if (key == null) {
            return List.of();
        }
        return ownerRepository.findAll().stream()
            .filter(existing -> key.equals(householdKey(existing)))
            .toList();
    }

    /**
     * Compute an owner's deterministic {@code householdId}: the first 12 lower-case hex characters of
     * SHA-256 over the UTF-8 bytes of {@code normalizedLastName + '|' + postcode} (see
     * {@link #householdKey(Owner)}). Owners with the same last name and postcode therefore share the
     * same {@code householdId} automatically. Returns {@code null} when the owner has no last name or
     * postcode, so it does not belong to a shared household.
     */
    private String computeHouseholdId(Owner owner) {
        String key = householdKey(owner);
        return key == null ? null : sha256Hex(key, 6, false);
    }

    /**
     * Build the owner's household key, {@code normalizedLastName + '|' + postcode}, the single value
     * that identifies a household. The last name is normalized case-insensitively with collapsed
     * whitespace (see {@link #normalizeHouseholdKey}) and the postcode is used exactly as stored.
     * Returns {@code null} when the owner has no last name or postcode to key on. This is the shared
     * basis for both {@link #computeHouseholdId(Owner)} and household membership matching, so the two
     * cannot drift apart.
     */
    private String householdKey(Owner owner) {
        String lastName = normalizeHouseholdKey(owner.getLastName());
        String postcode = owner.getPostcode();
        if (lastName == null || postcode == null || postcode.isBlank()) {
            return null;
        }
        return lastName + "|" + postcode;
    }

    /**
     * Normalize a value for case-insensitive, whitespace-insensitive household comparison by
     * trimming, collapsing internal runs of whitespace to a single space and lower-casing it.
     */
    private String normalizeHouseholdKey(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Build an owner's {@code customerCode} as {@code '<REGION>-<HASH8>'}: REGION is the region
     * code resolved from the owner's postcode (falling back to the owner's city, else
     * {@code 'UNKNOWN'}), and HASH8 the first 8 upper-case hex characters of SHA-256 computed over
     * the owner's normalized telephone concatenated with the owner's last name (e.g.
     * {@code 'NSW-1A2B3C4D'}). There are no per-city sequence numbers; the identity is fully
     * derived from the owner's own attributes.
     * <p>
     * When the derived code collides with an existing owner's {@code customerCode}, a
     * {@code '-<n>'} suffix is appended with the smallest {@code n} of 2 or more that makes it
     * unique, so distinct owners always receive distinct customer codes.
     */
    private String generateCustomerCode(Owner owner) {
        String base = PostcodeRules.resolveRegion(owner.getPostcode(), owner.getCity()) + "-" + customerHash(owner);
        return deduplicateCustomerCode(base);
    }

    /**
     * Return {@code base} when no existing owner already uses it as their {@code customerCode};
     * otherwise append {@code '-<n>'} with the smallest {@code n} of 2 or more that is not already
     * taken (counting both the bare {@code base} and any earlier {@code '-<n>'} de-duplications).
     */
    private String deduplicateCustomerCode(String base) {
        Set<String> existing = ownerRepository.findAll().stream()
            .map(Owner::getCustomerCode)
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
     * Compute the hash segment of an owner's {@code customerCode}: the first 8 upper-case hex
     * characters of SHA-256 over the UTF-8 bytes of the owner's normalized telephone concatenated
     * with the owner's last name.
     */
    private String customerHash(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        return sha256Hex(telephone + lastName, 4, true);
    }

    /**
     * Compute the leading hex of SHA-256 over the UTF-8 bytes of {@code input}: the first
     * {@code bytes} bytes of the digest rendered as {@code 2 * bytes} hex characters, upper-case when
     * {@code upperCase} is {@code true}, otherwise lower-case. Shared by the derived identifiers that
     * are built from a SHA-256 prefix (e.g. the household id and the customer-code hash), so the
     * digest handling and the "SHA-256 not available" error live in one place.
     */
    private String sha256Hex(String input, int bytes, boolean upperCase) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but not available", e);
        }
        String format = upperCase ? "%02X" : "%02x";
        StringBuilder hex = new StringBuilder(bytes * 2);
        for (int i = 0; i < bytes; i++) {
            hex.append(String.format(format, digest[i]));
        }
        return hex.toString();
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
     * last two digits of the owner's {@code registrationDate} year (e.g. {@code 'NSW-1A2B3C4D-M26'}).
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
