/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<List<OwnerDto>> listOwners(String lastName) {
        Collection<Owner> owners;
        if (lastName != null) {
            owners = this.clinicService.findOwnerByLastName(lastName);
        } else {
            owners = this.clinicService.findAllOwners();
        }
        if (owners.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (!normalizeNewOwner(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (dailyLimitReached(owner.getRegistrationDate())) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        if (cityIsAtCapacity(owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Optional<Owner> householdMember = findHouseholdMember(owner);
        if (householdMember.isPresent() && Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            joinHousehold(owner, householdMember.get());
        }
        if (identityKeyAlreadyUsed(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        assignDerivedAttributes(owner);
        this.clinicService.saveOwner(owner);
        auditOwnerCreated(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        String email = ownerFieldsDto.getEmail();
        currentOwner.setEmail(email == null ? null : email.toLowerCase());
        this.clinicService.saveOwner(currentOwner);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> deleteOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deleteOwner(owner);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PetDto> addPetToOwner(Integer ownerId, PetFieldsDto petFieldsDto) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        Pet pet = petMapper.toPet(petFieldsDto);
        owner.setId(ownerId);
        pet.setOwner(owner);
        pet.getType().setName(null);
        this.clinicService.savePet(pet);
        PetDto petDto = petMapper.toPetDto(pet);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/pets/{id}")
            .buildAndExpand(pet.getId()).toUri());
        return new ResponseEntity<>(petDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<Void> updateOwnersPet(Integer ownerId, Integer petId, PetFieldsDto petFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner != null) {
            Pet currentPet = this.clinicService.findPetById(petId);
            if (currentPet != null) {
                currentPet.setBirthDate(petFieldsDto.getBirthDate());
                currentPet.setName(petFieldsDto.getName());
                currentPet.setType(petMapper.toPetType(petFieldsDto.getType()));
                this.clinicService.savePet(currentPet);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<VisitDto> addVisitToOwner(Integer ownerId, Integer petId, VisitFieldsDto visitFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Visit visit = visitMapper.toVisit(visitFieldsDto);
        Pet pet = new Pet();
        pet.setId(petId);
        visit.setPet(pet);
        this.clinicService.saveVisit(visit);
        VisitDto visitDto = visitMapper.toVisitDto(visit);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/visits/{id}")
            .buildAndExpand(visit.getId()).toUri());
        return new ResponseEntity<>(visitDto, headers, HttpStatus.CREATED);
    }


    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PetDto> getOwnersPet(Integer ownerId, Integer petId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner != null) {
            Pet pet = owner.getPet(petId);
            if (pet != null) {
                return new ResponseEntity<>(petMapper.toPetDto(pet), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Normalise the incoming fields of a freshly-mapped owner in place, applying
     * the canonical form each stored value must take: the address is trimmed,
     * whitespace-collapsed, upper-cased and its common abbreviations expanded, the
     * telephone is converted to E.164, any email is lower-cased and a missing
     * registration date defaults to today. Field-level rejections are reported here
     * rather than inline in {@link #addOwner}, and normalisation runs before any
     * household comparison so those comparisons see the canonical values.
     *
     * @param owner the freshly-mapped owner to normalise
     * @return {@code true} if the fields are valid, or {@code false} if the address
     *         is blank after normalisation, the telephone cannot form a valid
     *         E.164 number, or a supplied registration date is later than the
     *         server date, in which case the request must be rejected with
     *         {@code 400 Bad Request}
     */
    private boolean normalizeNewOwner(Owner owner) {
        String address = Owner.normalizeAddress(owner.getAddress());
        if (address.isEmpty()) {
            return false;
        }
        owner.setAddress(address);
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return false;
        }
        owner.setTelephone(telephone);
        if (owner.getEmail() != null) {
            owner.setEmail(owner.getEmail().toLowerCase());
        }
        if (!owner.postcodeValidForLocality()) {
            return false;
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        } else if (owner.getRegistrationDate().isAfter(LocalDate.now())) {
            return false;
        }
        owner.setRegistrationDate(rollToBusinessDay(owner.getRegistrationDate()));
        return true;
    }

    /**
     * Roll the given date forward to the next business day when it falls on a
     * weekend: a Saturday or Sunday is advanced to the following Monday, while a
     * weekday is returned unchanged. Applied to the effective registration date
     * (whether supplied in the request or defaulted to the server date) so that
     * every value derived from it uses the adjusted business day.
     *
     * @param date the effective registration date
     * @return the same date if it is a weekday, otherwise the next Monday
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
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
     * Determine whether the candidate owner's identity key collides with any existing
     * owner's. This is the single, consolidated duplicate check: it subsumes the
     * former separate telephone, email and household checks by comparing the whole
     * derived {@code identityKey} ({@code normalizedTelephone + '|' + (email or empty)
     * + '|' + householdId}). Only an exact full-key match is a duplicate, so two owners
     * that agree on some — but not all — of the key's parts (for example two members of
     * the same household with different telephones) are not duplicates. Evaluated after
     * normalisation and household resolution, so the candidate's key reflects its
     * canonical field values and any household it has joined.
     *
     * @param owner the candidate owner being created, after household resolution
     * @return {@code true} if an existing owner has the same identity key
     */
    private boolean identityKeyAlreadyUsed(Owner owner) {
        String identityKey = owner.getIdentityKey();
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    /**
     * Find an existing owner that belongs to the same household as the given owner,
     * i.e. shares the same last name and address (compared case-insensitively with
     * runs of whitespace collapsed to a single space).
     *
     * @param owner the candidate owner being created
     * @return the matching existing owner, or empty if none exists
     */
    private Optional<Owner> findHouseholdMember(Owner owner) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.sameHouseholdAs(owner))
            .findFirst();
    }

    /**
     * Assign the joining owner and an existing household member the same stable
     * household identifier. The identifier is derived deterministically from the
     * shared household key (last name + address, normalised), so every owner in a
     * household resolves to the same value; the existing member is updated when it
     * does not already carry it.
     *
     * @param owner    the owner being created
     * @param existing an existing owner in the same household
     */
    private void joinHousehold(Owner owner, Owner existing) {
        String householdId = owner.computeHouseholdId();
        owner.setHouseholdId(householdId);
        if (!householdId.equals(existing.getHouseholdId())) {
            existing.setHouseholdId(householdId);
            this.clinicService.saveOwner(existing);
        }
    }

    /**
     * Assign the owner's computed-at-creation attributes in place: its customer code,
     * namesake count, membership number, bulk-signup warning and household size. Unlike
     * the {@code @Transient} attributes {@link Owner} recomputes on demand, these are
     * derived once here — from the owner's own (already normalised) fields and from the
     * wider owner population — and persisted with the owner. Run after normalisation,
     * household resolution and the identity-key check, so every value reflects the
     * owner's final field values and any household it has joined, and immediately before
     * the owner is saved.
     *
     * <p>Ordering matters: the customer code is assigned first because the membership
     * number is built from it.
     *
     * @param owner the candidate owner being created, after household resolution
     */
    private void assignDerivedAttributes(Owner owner) {
        owner.setCustomerCode(owner.computeCustomerCode());
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setMembershipNumber(membershipNumber(owner));
        owner.setBulkSignupWarning(bulkSignupWarning(owner.getRegistrationDate()));
        owner.setHouseholdSize(householdSize(owner));
        assignPossibleDuplicate(owner);
    }

    /**
     * Flag the candidate owner as a possible duplicate of an existing owner. A soft
     * match — distinct from the hard {@code identityKey} duplicate already rejected with
     * 409 — is an existing owner that shares this owner's last name (compared
     * case-insensitively) and postcode but carries a different (normalised) telephone.
     * When such a match exists the owner is still created, but with
     * {@code possibleDuplicate} true and {@code possibleDuplicateOf} set to the matching
     * owner's id; otherwise {@code possibleDuplicate} is false and no match id is
     * recorded. The earliest-created matching owner (lowest id) is chosen so the result
     * is deterministic. Evaluated before the owner is saved, so only pre-existing owners
     * are considered.
     *
     * @param owner the candidate owner being created, after household resolution
     */
    private void assignPossibleDuplicate(Owner owner) {
        String lastName = normalizeName(owner.getLastName());
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Optional<Owner> match = this.clinicService.findAllOwners().stream()
            .filter(existing -> postcode != null && postcode.equals(existing.getPostcode())
                && normalizeName(existing.getLastName()).equals(lastName)
                && telephone != null && !telephone.equals(existing.getTelephone()))
            .min(Comparator.comparing(Owner::getId));
        if (match.isPresent()) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.get().getId());
        } else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }

    /**
     * Count how many owners belong to the given owner's household once this create
     * completes: the owners that already share its {@code householdId} plus the owner
     * being created. Evaluated after any {@link #joinHousehold(Owner, Owner)} has run,
     * so the household identifier (and any existing member updated to it) is already in
     * place. An owner that has not joined a household (no {@code householdId}) is a
     * household of one.
     *
     * @param owner the candidate owner being created, after household resolution
     * @return the household member count after this create (at least {@code 1})
     */
    private int householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(other -> householdId.equals(other.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * Count the existing owners that share the given owner's first and last name,
     * compared case-insensitively (surrounding whitespace trimmed). Evaluated
     * before the new owner is saved, so the result reflects only owners that
     * already existed at creation time.
     *
     * @param owner the candidate owner being created
     * @return the number of existing namesakes
     */
    private int countNamesakes(Owner owner) {
        String firstName = normalizeName(owner.getFirstName());
        String lastName = normalizeName(owner.getLastName());
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(firstName)
                && normalizeName(existing.getLastName()).equals(lastName))
            .count();
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    /**
     * Determine whether the given city has already reached its owner capacity, i.e.
     * it already contains 50 or more existing owners (compared case-insensitively
     * with surrounding whitespace trimmed). A new owner may not be created in a city
     * that is at capacity.
     *
     * @param city the candidate owner's city
     * @return {@code true} if the city already holds 50 or more owners
     */
    private boolean cityIsAtCapacity(String city) {
        String normalizedCity = normalizeName(city);
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getCity()).equals(normalizedCity))
            .count();
        return count >= 50;
    }

    /**
     * Determine whether the daily owner-creation limit has already been reached, i.e.
     * 100 or more owners already carry the given registration date. A new owner may
     * not be created once that day's cap is reached.
     *
     * @param registrationDate the registration date the new owner would be created with
     * @return {@code true} if 100 or more existing owners share that registration date
     */
    private boolean dailyLimitReached(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count >= 100;
    }

    /**
     * Determine whether more than 80 owners have already been created for the given
     * registration date, i.e. strictly more than 80 existing owners carry that date.
     * Evaluated before the new owner is saved, so it reflects only owners that
     * already existed at creation time.
     *
     * @param registrationDate the registration date the new owner would be created with
     * @return {@code true} if more than 80 existing owners share that registration date
     */
    private boolean bulkSignupWarning(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count > 80;
    }

    /**
     * Write the create audit record for a freshly-saved owner. Keeps the audit format in
     * one place so it stays in step with the owner's identity: it reports the assigned
     * customer code alongside the owner's id, registration date and membership level.
     *
     * @param owner the owner that has just been created and saved
     */
    private void auditOwnerCreated(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel());
    }

    /**
     * Build the owner's membership number, formatted {@code <customerCode>-M<YY>}
     * where {@code <customerCode>} is the owner's already-assigned customer code and
     * YY is the last two digits of the registration date's year (e.g.
     * {@code NSW-9F86D081-M26}). Evaluated after the customer code and registration date
     * have been set.
     *
     * @param owner the owner being created, with customer code and registration date
     *              already assigned
     * @return the assigned membership number
     */
    private String membershipNumber(Owner owner) {
        int yy = owner.getRegistrationDate().getYear() % 100;
        return String.format("%s-M%02d", owner.getCustomerCode(), yy);
    }

    /**
     * Normalise a telephone number to E.164 form: keep a leading '+' and country
     * code when present, otherwise assume country code '+61' and drop a single
     * leading '0' from the national digits. Spaces, dashes and brackets are
     * stripped. The result must contain 8 to 15 digits after the '+'.
     *
     * <p>In addition, the national-number length is validated against the country
     * code: a {@code +61} (Australia) number must have exactly 9 national digits
     * and a {@code +1} (NANP) number must have exactly 10 national digits. A number
     * whose national-number length is wrong for its country code is rejected.
     *
     * @return the E.164 string (e.g. "+61412345678"), or {@code null} if the
     *         input cannot form a valid E.164 number.
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        if (!nationalNumberLengthValid(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Validate the length of the national number (the digits after the country
     * code) against the country code carried by the given E.164 digit string: a
     * {@code 61} (Australia) number requires exactly 9 national digits and a
     * {@code 1} (NANP) number requires exactly 10 national digits. Country codes
     * without a pinned length are accepted (only the generic 8-15 digit rule
     * applies to them).
     *
     * @param digits the E.164 digits without the leading '+'
     * @return {@code true} if the national-number length is correct for the country
     */
    private static boolean nationalNumberLengthValid(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }
}
