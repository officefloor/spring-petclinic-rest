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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.advice.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityFullException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
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

    /** Dedicated audit logger; a successful create emits a single line here with the new owner's key details. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Maximum number of owners a single city may contain; creating an owner in a full city is rejected. */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /** Maximum number of owners that may be created in a single day; creating an owner past this cap is rejected. */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /** Once more than this many owners already exist for a day, a further create for that day is flagged as a bulk signup. */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final TelephoneNormalizer telephoneNormalizer;

    private final AddressNormalizer addressNormalizer;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 TelephoneNormalizer telephoneNormalizer,
                                 AddressNormalizer addressNormalizer) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.telephoneNormalizer = telephoneNormalizer;
        this.addressNormalizer = addressNormalizer;
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
        normalizeAndValidate(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setCustomerCode(generateCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setBulkSignupWarning(computeBulkSignupWarning(owner.getRegistrationDate()));
        assignHousehold(owner, ownerFieldsDto);
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Prepares a new owner's payload for persistence, applying every rule an incoming owner must satisfy before it is
     * saved. The address is first normalized to its canonical stored form and written back onto the payload; required
     * fields are then checked (so an address that is blank after normalization is rejected); the telephone is
     * normalized to its canonical stored form, written back onto the payload and rejected if another owner already uses
     * it; the email is canonicalized; and the registration date is resolved to its effective business day. The
     * effective registration date is the supplied value or, when none was supplied, the current server date; when it
     * falls on a weekend it is rolled forward to the following Monday and that adjusted date is written back onto the
     * payload, so every value derived from it (such as the membership number's year segment and the per-day limit)
     * uses the adjusted date. The payload is mutated in place so the caller can map and save it directly.
     *
     * @param ownerFieldsDto the incoming owner payload, mutated in place
     * @throws MissingOwnerFieldsException if any required field is missing or blank
     * @throws DuplicateTelephoneException if another owner already uses the normalized telephone
     */
    private void normalizeAndValidate(OwnerFieldsDto ownerFieldsDto) {
        ownerFieldsDto.setAddress(addressNormalizer.normalize(ownerFieldsDto.getAddress()));
        validateRequiredFields(ownerFieldsDto);
        String telephone = telephoneNormalizer.normalize(ownerFieldsDto.getTelephone());
        ownerFieldsDto.setTelephone(telephone);
        requireUniqueTelephone(telephone);
        requireUniqueHousehold(ownerFieldsDto);
        requireCityHasCapacity(ownerFieldsDto.getCity());
        LocalDate registrationDate = resolveRegistrationDate(ownerFieldsDto);
        ownerFieldsDto.setRegistrationDate(registrationDate);
        requireDailyLimitNotReached(registrationDate);
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
    }

    /**
     * Resolves an owner's effective registration date and rolls it onto a business day. The effective date is the value
     * supplied on the payload, or the current server date when none was supplied; when that date is a Saturday or
     * Sunday it is rolled forward to the following Monday. The returned date is always a business day.
     *
     * @param ownerFieldsDto the incoming owner payload
     * @return the effective registration date rolled forward onto a business day
     */
    private LocalDate resolveRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate effective = ownerFieldsDto.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        while (effective.getDayOfWeek() == DayOfWeek.SATURDAY || effective.getDayOfWeek() == DayOfWeek.SUNDAY) {
            effective = effective.plusDays(1);
        }
        return effective;
    }

    /**
     * Rejects creating an owner once the maximum number of owners for the adjusted business day has already been
     * reached. The cap is 100, so when 100 or more existing owners carry the given adjusted {@code registrationDate} no
     * further owner may be created for that day.
     *
     * @param registrationDate the adjusted business-day registration date of the owner being created
     * @throws DailyOwnerLimitExceededException if 100 or more owners have already been created for that day
     */
    private void requireDailyLimitNotReached(LocalDate registrationDate) {
        long createdThatDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (createdThatDay >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(
                "the maximum number of owners for today has already been reached");
        }
    }

    /**
     * Determines whether creating an owner for the given adjusted business-day registration date should carry a bulk
     * signup warning. The warning is raised once more than 80 owners already carry that {@code registrationDate},
     * i.e. this create is at least the 82nd for the day. Evaluated before the new owner is saved, so the count
     * reflects only owners that predate it.
     *
     * @param registrationDate the adjusted business-day registration date of the owner being created
     * @return {@code true} when more than 80 owners already exist for that day, {@code false} otherwise
     */
    private boolean computeBulkSignupWarning(LocalDate registrationDate) {
        long createdThatDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return createdThatDay > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Builds an owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}. {@code CITY3} is the upper-cased
     * first three letters of the city, {@code LAST3} the upper-cased first three letters of the last name, and
     * {@code NNNN} a per-city 4-digit zero-padded sequence equal to one more than the number of owners already in that
     * city (e.g. {@code LON-SMI-0007}).
     *
     * @param city     the owner's city
     * @param lastName the owner's last name
     * @return the generated customer code
     */
    private String generateCustomerCode(String city, String lastName) {
        String cityPrefix = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String lastPrefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count() + 1;
        return String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence);
    }

    /**
     * Counts how many existing owners already share the given owner's first and last name, compared
     * case-insensitively. Invoked before the new owner is saved, so the result reflects only owners that predate this
     * create; it is zero when the owner's name is unique.
     *
     * @param owner the newly mapped owner about to be saved
     * @return the number of existing owners sharing the same first and last name (case-insensitively)
     */
    private int countNamesakes(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Rejects an owner payload that omits or leaves blank any required field. Bean Validation already rejects missing
     * ({@code null}) required fields, but permits values that are blank (empty or whitespace-only) for fields without a
     * stricter pattern, so this check enforces the "missing or blank" rule uniformly across every required field.
     *
     * @param ownerFieldsDto the owner payload to validate
     * @throws MissingOwnerFieldsException if any of firstName, lastName, address, city or telephone is missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        requireText(missingFields, "firstName", ownerFieldsDto.getFirstName());
        requireText(missingFields, "lastName", ownerFieldsDto.getLastName());
        requireText(missingFields, "address", ownerFieldsDto.getAddress());
        requireText(missingFields, "city", ownerFieldsDto.getCity());
        requireText(missingFields, "telephone", ownerFieldsDto.getTelephone());
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
    }

    private void requireText(List<String> missingFields, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missingFields.add(fieldName);
        }
    }

    /**
     * Normalizes an optional owner email. A syntactically valid address is already enforced by Bean Validation on the
     * request payload, so this only canonicalizes the value that gets stored and returned by lower-casing it. A missing
     * (blank or {@code null}) email is left untouched, since the field is optional.
     *
     * @param email the raw email value from the request, may be {@code null}
     * @return the lower-cased email, or the original value when none was supplied
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        return email.toLowerCase();
    }

    /**
     * Rejects creating an owner whose normalized telephone is already used by another owner. Existing owners' stored
     * telephones are canonicalized the same way before comparison, so the rule holds regardless of how their number was
     * originally formatted.
     *
     * @param telephone the normalized telephone of the owner being created
     * @throws DuplicateTelephoneException if any existing owner already uses the same normalized telephone
     */
    private void requireUniqueTelephone(String telephone) {
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(existing -> telephoneNormalizer.canonicalize(existing.getTelephone()))
            .anyMatch(telephone::equals);
        if (inUse) {
            throw new DuplicateTelephoneException("telephone is already in use by another owner");
        }
    }

    /**
     * Rejects creating an owner whose city (compared case-insensitively) already contains the maximum number of
     * owners. The cap is 50, so a city holding 50 or more owners is full and no further owner may be created there.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityFullException if the city already contains 50 or more owners
     */
    private void requireCityHasCapacity(String city) {
        long cityOwners = this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (cityOwners >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityFullException("the owner's city already contains the maximum number of owners");
        }
    }

    /**
     * Rejects creating an owner who shares a household (see {@link #findHouseholdMembers}) with an existing owner,
     * unless the request opts in by setting {@code sharesHousehold} to {@code true}.
     *
     * @param ownerFieldsDto the incoming owner payload
     * @throws DuplicateOwnerException if another owner already belongs to the same household and the request does not
     *                                 set {@code sharesHousehold}
     */
    private void requireUniqueHousehold(OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        if (!findHouseholdMembers(ownerFieldsDto).isEmpty()) {
            throw new DuplicateOwnerException(
                "an owner with the same last name and address already exists in this household");
        }
    }

    /**
     * Finds the existing owners that belong to the same household as the given payload, i.e. those sharing both its
     * last name and address. The last name is compared case-insensitively after collapsing runs of whitespace to a
     * single space and trimming; the address is compared in its normalized form (see {@link AddressNormalizer}), so
     * incidental formatting differences and abbreviation variants do not affect membership.
     *
     * @param ownerFieldsDto the incoming owner payload
     * @return the existing owners in the same household, in the order {@link ClinicService#findAllOwners()} returns
     *         them; empty when none match
     */
    private List<Owner> findHouseholdMembers(OwnerFieldsDto ownerFieldsDto) {
        String lastName = collapseWhitespace(ownerFieldsDto.getLastName());
        String address = addressNormalizer.normalize(ownerFieldsDto.getAddress());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> collapseWhitespace(existing.getLastName()).equalsIgnoreCase(lastName)
                && addressNormalizer.normalize(existing.getAddress()).equals(address))
            .toList();
    }

    /**
     * Canonicalizes a value for household comparison by trimming it and collapsing every run of whitespace to a single
     * space. Case is deliberately preserved here; callers compare the result case-insensitively.
     *
     * @param value the raw field value, may be {@code null}
     * @return the whitespace-collapsed value, or an empty string when {@code null}
     */
    private String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ");
    }

    /**
     * Assigns the shared {@code householdId} for an owner joining an existing household. Only owners created with
     * {@code sharesHousehold} set to {@code true} that actually match an existing household (see
     * {@link #findHouseholdMembers}) are given an identifier; single owners keep a {@code null} household. The
     * identifier is derived deterministically from the household's last name and address, so every member of the same
     * household resolves to the same stable value regardless of creation order. Existing members that predate the
     * feature and still lack the identifier are backfilled so the whole household shares it.
     *
     * @param owner          the newly mapped owner about to be saved
     * @param ownerFieldsDto the incoming owner payload
     */
    private void assignHousehold(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        List<Owner> members = findHouseholdMembers(ownerFieldsDto);
        if (members.isEmpty()) {
            return;
        }
        String householdId = generateHouseholdId(ownerFieldsDto);
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
        }
    }

    /**
     * Derives a household's stable identifier, formatted {@code HH-<12 hex chars>}, from the case-insensitive,
     * whitespace-collapsed last name and the normalized address (see {@link AddressNormalizer}). Being a pure function
     * of those fields, it is identical for every owner in the same household and never changes over time.
     *
     * @param ownerFieldsDto the incoming owner payload
     * @return the household identifier
     */
    private String generateHouseholdId(OwnerFieldsDto ownerFieldsDto) {
        String key = collapseWhitespace(ownerFieldsDto.getLastName()).toLowerCase()
            + "\n" + addressNormalizer.normalize(ownerFieldsDto.getAddress()).toLowerCase();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return "HH-" + hex;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
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
        currentOwner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
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
}
