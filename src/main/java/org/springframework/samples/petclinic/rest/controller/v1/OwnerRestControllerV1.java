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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.OwnerDailyRegistrationLimitException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.BusinessDayAdjuster;
import org.springframework.samples.petclinic.util.EmailNormalizer;
import org.springframework.samples.petclinic.util.HouseholdNormalizer;
import org.springframework.samples.petclinic.util.OwnerIdentity;
import org.springframework.samples.petclinic.util.PostcodeValidator;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;
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

    /**
     * Dedicated audit logger. A single line is emitted here on each successful owner create,
     * carrying the new owner's id, customer code and registration date.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Maximum number of owners a single city may hold. A create whose city already contains this many
     * owners is rejected with a {@code 409 Conflict} (see {@link #rejectCityAtCapacity}).
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Maximum number of owners that may be registered in a single day. A create made once this many
     * owners already carry today's {@code registrationDate} is rejected with a {@code 429 Too Many
     * Requests} (see {@link #rejectDailyRegistrationLimitReached}).
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * The number of owners that must already carry a given day's {@code registrationDate} before a
     * create made that day is flagged with {@code bulkSignupWarning}. Once more than this many owners
     * have already been created for the day, the new owner's {@code bulkSignupWarning} is {@code true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

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
        normalizeAddress(ownerFieldsDto);
        rejectBlankOwnerFields(ownerFieldsDto);
        rejectInvalidPostcode(ownerFieldsDto);
        normalizeTelephone(ownerFieldsDto);
        normalizeEmail(ownerFieldsDto);
        String householdId = householdIdFor(ownerFieldsDto);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        rejectDuplicateIdentity(ownerFieldsDto, householdId);
        rejectHouseholdDuplicate(householdId, sharesHousehold);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        rejectFutureRegistrationDate(ownerFieldsDto.getRegistrationDate());
        LocalDate registrationDate = effectiveRegistrationDate(ownerFieldsDto.getRegistrationDate());
        long registeredThatDay = countRegisteredOn(registrationDate);
        rejectDailyRegistrationLimitReached(registrationDate, registeredThatDay);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setHouseholdId(householdId);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(registeredThatDay > BULK_SIGNUP_WARNING_THRESHOLD);
        owner.setHouseholdSize(countHouseholdMembers(owner.getHouseholdId()) + 1);
        markPossibleDuplicate(owner, sharesHousehold);
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel(), ownerDto.getMembershipNumber());
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
        normalizeEmail(ownerFieldsDto);
        currentOwner.setEmail(ownerFieldsDto.getEmail());
        currentOwner.setHouseholdId(householdIdFor(ownerFieldsDto));
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
     * Rejects an owner whose {@code firstName}, {@code lastName}, {@code address}, {@code city} or
     * {@code telephone} is missing (null) or blank (empty or whitespace only). Bean Validation on
     * the request body already rejects null and empty values, but permits whitespace-only strings
     * for fields without a stricter pattern (e.g. {@code city}, {@code address}); this guard closes
     * that gap so every required field must carry a non-blank value.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if one or more required fields are missing or blank
     */
    private void rejectBlankOwnerFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", ownerFieldsDto.getFirstName());
        addIfBlank(missing, "lastName", ownerFieldsDto.getLastName());
        addIfBlank(missing, "address", ownerFieldsDto.getAddress());
        addIfBlank(missing, "city", ownerFieldsDto.getCity());
        addIfBlank(missing, "telephone", ownerFieldsDto.getTelephone());
        if (!missing.isEmpty()) {
            throw new InvalidOwnerFieldsException(missing);
        }
    }

    /**
     * Rejects an owner whose supplied {@code postcode} is invalid for its {@code city}. The postcode is
     * optional: when absent (null) the owner is accepted unchanged, so the request contract stays
     * backward-compatible. When present it must be four digits and, when the city maps to a known region,
     * must fall within that region's fixed inclusive range ({@code NSW 2000-2099}, {@code VIC 3000-3099},
     * {@code QLD 4000-4099}); a city with no known region accepts any 4-digit postcode. See
     * {@link PostcodeValidator}.
     *
     * @param ownerFieldsDto the submitted owner fields (its {@code city} is non-blank here,
     *                       {@link #rejectBlankOwnerFields} having already run)
     * @throws InvalidOwnerFieldsException if a postcode is present but not valid for the city
     */
    private void rejectInvalidPostcode(OwnerFieldsDto ownerFieldsDto) {
        if (!PostcodeValidator.isValid(ownerFieldsDto.getCity(), ownerFieldsDto.getPostcode())) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }

    private void addIfBlank(List<String> missing, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missing.add(fieldName);
        }
    }

    /**
     * Normalizes the submitted {@code address} on create to its canonical form (see
     * {@link AddressNormalizer#normalize}) and writes it back onto the request so it is the value
     * stored and returned, and the form every later address comparison (the required-field check,
     * household duplicate detection and the shared household id) is judged on. A null address is left
     * untouched so {@link #rejectBlankOwnerFields} still reports it as missing; a value that is blank
     * after normalization likewise reduces to the empty string and is rejected there.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        String address = ownerFieldsDto.getAddress();
        if (address == null) {
            return;
        }
        ownerFieldsDto.setAddress(AddressNormalizer.normalize(address));
    }

    /**
     * Normalizes the submitted {@code telephone} on create to its canonical form (see
     * {@link TelephoneNormalizer#normalize}) and writes it back onto the request so it is the value
     * stored and returned. Any input that does not reduce to a valid telephone is rejected with a
     * {@code 400 Bad Request}.
     *
     * @param ownerFieldsDto the submitted owner fields (its {@code telephone} is non-blank here,
     *                       {@link #rejectBlankOwnerFields} having already run)
     * @throws InvalidOwnerFieldsException if the telephone cannot be normalized to a valid value
     */
    private void normalizeTelephone(OwnerFieldsDto ownerFieldsDto) {
        String normalized = TelephoneNormalizer.normalize(ownerFieldsDto.getTelephone())
            .orElseThrow(() -> new InvalidOwnerFieldsException(List.of("telephone")));
        ownerFieldsDto.setTelephone(normalized);
    }

    /**
     * Normalizes an optionally-supplied {@code email}. When absent (null) or blank the field is left
     * untouched (email is optional). When present it is reduced to its canonical form (see
     * {@link EmailNormalizer#normalize}) and written back so it is the value stored and returned. Any
     * present-but-invalid address is rejected with a {@code 400 Bad Request}.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if the email is present but not a syntactically valid address,
     *                                     or its domain is on the disposable-domain blocklist
     */
    private void normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = EmailNormalizer.normalize(email)
            .orElseThrow(() -> new InvalidOwnerFieldsException(List.of("email")));
        if (EmailNormalizer.hasDisposableDomain(normalized)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        ownerFieldsDto.setEmail(normalized);
    }

    /**
     * Rejects creating an owner whose derived identity collides with an existing owner. All owner
     * duplicate detection is now expressed through the single derived {@code identityKey}
     * ({@code normalizedTelephone + '|' + email + '|' + householdId}, see {@link OwnerIdentity}),
     * which replaces the previously separate telephone, email and household checks. Two owners denote
     * the same person when their identities collide on the telephone - the identity's distinguishing
     * component - so a create is rejected as a {@code 409 Conflict} when another owner already carries
     * the same normalized telephone. Because the telephone is part of the identity, two members of the
     * same household with different telephones have different identities and are both allowed; an email
     * or a household on its own no longer makes a duplicate.
     *
     * @param ownerFieldsDto the submitted owner fields, with telephone and email already normalized
     *                       ({@link #normalizeTelephone}, {@link #normalizeEmail})
     * @param householdId    the household id derived for the owner being created (see
     *                       {@link HouseholdNormalizer#toHouseholdId})
     * @throws DuplicateOwnerIdentityException if another owner already carries the same identity
     */
    private void rejectDuplicateIdentity(OwnerFieldsDto ownerFieldsDto, String householdId) {
        String identityKey = OwnerIdentity.identityKey(
            ownerFieldsDto.getTelephone(), ownerFieldsDto.getEmail(), householdId);
        String telephoneKey = TelephoneNormalizer.toComparisonKey(ownerFieldsDto.getTelephone());
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(Objects::nonNull)
            .map(TelephoneNormalizer::toComparisonKey)
            .anyMatch(telephoneKey::equals);
        if (inUse) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * Counts the existing owners (before this create) whose {@code firstName} and {@code lastName}
     * both match the submitted values, compared case-insensitively. The count reflects only owners
     * already persisted at the time of the create, so the owner being created is never included.
     *
     * @param firstName the submitted first name (non-blank here, {@link #rejectBlankOwnerFields}
     *                  having already run)
     * @param lastName  the submitted last name (non-blank here)
     * @return the number of existing owners sharing the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(owner -> firstName.equalsIgnoreCase(owner.getFirstName())
                && lastName.equalsIgnoreCase(owner.getLastName()))
            .count();
    }

    /**
     * Counts the existing owners (before this create) belonging to the given household, i.e. those
     * whose stored {@code householdId} equals the value derived for the owner being created. The
     * count reflects only owners already persisted at the time of the create, so the owner being
     * created is never included; callers add one to obtain the household size after this create.
     *
     * @param householdId the household identifier derived for the owner being created (see
     *                    {@link HouseholdNormalizer#toHouseholdId})
     * @return the number of existing owners already sharing that household
     */
    private int countHouseholdMembers(String householdId) {
        if (householdId == null) {
            return 0;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .map(Owner::getHouseholdId)
            .filter(householdId::equals)
            .count();
    }

    /**
     * Derives the household id an owner should carry from its submitted fields (see
     * {@link HouseholdNormalizer#toHouseholdId}). Both create and update stamp an owner with a
     * household id, so routing them through this single method keeps them agreed on which of the
     * owner's fields key the household.
     *
     * @param ownerFieldsDto the submitted owner fields (its identifying fields are non-blank here,
     *                       {@link #rejectBlankOwnerFields} having already run on create)
     * @return the owner's household id
     */
    private String householdIdFor(OwnerFieldsDto ownerFieldsDto) {
        return HouseholdNormalizer.toHouseholdId(
            ownerFieldsDto.getLastName(), ownerFieldsDto.getPostcode());
    }

    /**
     * Rejects creating an owner that would join an existing household without declaring itself a
     * member. The household is keyed on (last name, postcode) through the derived {@code householdId}
     * (see {@link HouseholdNormalizer#toHouseholdId}), so a second owner sharing an existing owner's
     * last name and postcode denotes the same household. Such a create is reported as a {@code 409
     * Conflict} unless it opts in with {@code sharesHousehold}, in which case it is accepted as a
     * declared household member. Only owners already persisted (before this create) are considered.
     *
     * @param householdId     the household id derived for the owner being created
     * @param sharesHousehold whether the request opted in to sharing a household
     * @throws DuplicateOwnerHouseholdException if the household already exists and the request did not
     *                                          opt in with {@code sharesHousehold}
     */
    private void rejectHouseholdDuplicate(String householdId, boolean sharesHousehold) {
        if (sharesHousehold || householdId == null) {
            return;
        }
        boolean householdExists = this.clinicService.findAllOwners().stream()
            .map(Owner::getHouseholdId)
            .anyMatch(householdId::equals);
        if (householdExists) {
            throw new DuplicateOwnerHouseholdException(householdId);
        }
    }

    /**
     * Finds an existing owner that the owner being created soft-matches on, or {@code null} when there
     * is none. The create has already cleared the hard-duplicate identity check
     * ({@link #rejectDuplicateIdentity}), so it is not an exact match of any existing owner; it is a
     * <em>possible</em> duplicate when it nevertheless shares an existing owner's {@code lastName}
     * (compared case-insensitively) and {@code postcode} while carrying a different normalized
     * telephone. The oldest such owner (the earliest persisted) is returned so the flag points at the
     * original record; owners with no postcode never match, since a shared postcode is required.
     *
     * @param owner the owner being created, with telephone already normalized and lastName/postcode set
     * @return the id of the matching existing owner, or {@code null} when the owner is not a possible
     *         duplicate
     */
    private Integer findPossibleDuplicate(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return null;
        }
        String telephoneKey = TelephoneNormalizer.toComparisonKey(owner.getTelephone());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> owner.getLastName().equalsIgnoreCase(existing.getLastName()))
            .filter(existing -> postcode.equals(existing.getPostcode()))
            .filter(existing -> existing.getTelephone() != null
                && !telephoneKey.equals(TelephoneNormalizer.toComparisonKey(existing.getTelephone())))
            .map(Owner::getId)
            .filter(Objects::nonNull)
            .min(Integer::compareTo)
            .orElse(null);
    }

    /**
     * Records on the owner being created whether it is a possible (soft) duplicate of an existing
     * owner. The match itself is decided by {@link #findPossibleDuplicate}; this method reflects its
     * result into the two stored fields, so the interpretation of "is a possible duplicate" lives in
     * one place: {@code possibleDuplicateOf} holds the matched owner's id (or {@code null} when there
     * is none) and {@code possibleDuplicate} is {@code true} exactly when a match was found. An owner
     * that opted in with {@code sharesHousehold} is a declared household member, not a suspected
     * duplicate, so it is never flagged.
     *
     * @param owner           the owner being created, with telephone already normalized and
     *                        lastName/postcode set
     * @param sharesHousehold whether the request opted in to sharing a household
     */
    private void markPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        Integer possibleDuplicateOf = sharesHousehold ? null : findPossibleDuplicate(owner);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
    }

    /**
     * Rejects creating an owner whose city already holds {@link #MAX_OWNERS_PER_CITY} or more owners.
     * The submitted city is compared case-insensitively against every existing owner's stored city; a
     * city that is already at (or over) capacity is reported as a {@code 409 Conflict}.
     *
     * @param city the submitted city (non-blank here, {@link #rejectBlankOwnerFields} having already
     *             run)
     * @throws OwnerCityAtCapacityException if the city already contains the maximum number of owners
     */
    private void rejectCityAtCapacity(String city) {
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .map(Owner::getCity)
            .filter(Objects::nonNull)
            .filter(city::equalsIgnoreCase)
            .count();
        if (ownersInCity >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }

    /**
     * Resolves the effective registration date for a create, rolled onto a business day. The
     * supplied date is used when present, otherwise the server's current date; either way the result
     * is rolled forward to the next Monday when it falls on a Saturday or Sunday (see
     * {@link BusinessDayAdjuster#toBusinessDay}). Every value derived from the registration date - the
     * stored {@code registrationDate}, the membership number's year segment and the daily
     * create-limit's per-day count - is judged on this adjusted date.
     *
     * @param suppliedRegistrationDate the registration date from the request, or {@code null} to
     *                                 default to the server's current date
     * @return the effective registration date, guaranteed to fall on a business day
     */
    /**
     * Rejects a create whose supplied {@code registrationDate} lies after the server's current date.
     * The raw submitted value is compared against {@link LocalDate#now()} before any business-day
     * adjustment; a future date cannot describe a registration that has already happened, so it is
     * reported as a {@code 400 Bad Request}. A {@code null} registration date (defaulted to the
     * server's current date) and any date on or before today are accepted.
     *
     * @param suppliedRegistrationDate the registration date from the request, or {@code null} when
     *                                 omitted
     * @throws InvalidOwnerFieldsException if the supplied date is later than the server's current date
     */
    private void rejectFutureRegistrationDate(LocalDate suppliedRegistrationDate) {
        if (suppliedRegistrationDate != null && suppliedRegistrationDate.isAfter(LocalDate.now())) {
            throw new InvalidOwnerFieldsException(List.of("registrationDate"));
        }
    }

    private LocalDate effectiveRegistrationDate(LocalDate suppliedRegistrationDate) {
        LocalDate registrationDate =
            suppliedRegistrationDate != null ? suppliedRegistrationDate : LocalDate.now();
        return BusinessDayAdjuster.toBusinessDay(registrationDate);
    }

    /**
     * Rejects creating an owner once {@link #MAX_OWNERS_PER_DAY} or more owners have already been
     * created for the given business day, judged by their stored {@code registrationDate}. Only owners
     * already persisted (before this create) are counted, and only those whose {@code registrationDate}
     * equals the adjusted registration date; a day at (or over) that limit is reported as a {@code 429
     * Too Many Requests}.
     *
     * @param registrationDate   the create's effective registration date, already rolled onto a
     *                           business day by {@link #effectiveRegistrationDate}
     * @param registeredThatDay  the number of owners already persisted (before this create) whose
     *                           {@code registrationDate} equals {@code registrationDate}
     * @throws OwnerDailyRegistrationLimitException if that day has already reached the maximum number of
     *                                              owner registrations
     */
    private void rejectDailyRegistrationLimitReached(LocalDate registrationDate, long registeredThatDay) {
        if (registeredThatDay >= MAX_OWNERS_PER_DAY) {
            throw new OwnerDailyRegistrationLimitException(registrationDate);
        }
    }

    /**
     * Counts the owners already persisted (before this create) whose stored {@code registrationDate}
     * equals the given day. This is the shared per-day accumulation both the daily create-limit and the
     * bulk-signup warning are judged on.
     *
     * @param registrationDate the create's effective registration date, already rolled onto a business
     *                         day by {@link #effectiveRegistrationDate}
     * @return the number of existing owners registered on that day
     */
    private long countRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(registrationDate::equals)
            .count();
    }
}
