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
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

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
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerEmailException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
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
import org.springframework.samples.petclinic.util.HouseholdNormalizer;
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
     * Syntactic validation for an owner email: a non-empty local part, an '@', and a dotted domain
     * whose top-level label is at least two letters. Deliberately conservative so plainly malformed
     * input (e.g. a value without an '@') is rejected.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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
        normalizeTelephone(ownerFieldsDto);
        rejectDuplicateTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateHousehold(ownerFieldsDto);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        LocalDate registrationDate = effectiveRegistrationDate(ownerFieldsDto.getRegistrationDate());
        long registeredThatDay = countRegisteredOn(registrationDate);
        rejectDailyRegistrationLimitReached(registrationDate, registeredThatDay);
        normalizeEmail(ownerFieldsDto);
        rejectDuplicateEmail(ownerFieldsDto.getEmail());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setHouseholdId(HouseholdNormalizer.toHouseholdId(owner.getLastName(), owner.getAddress()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(registeredThatDay > BULK_SIGNUP_WARNING_THRESHOLD);
        owner.setHouseholdSize(countHouseholdMembers(owner.getHouseholdId()) + 1);
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
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
        normalizeEmail(ownerFieldsDto);
        currentOwner.setEmail(ownerFieldsDto.getEmail());
        currentOwner.setHouseholdId(
            HouseholdNormalizer.toHouseholdId(currentOwner.getLastName(), currentOwner.getAddress()));
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
     * untouched (email is optional). When present it is trimmed and, after validating that it is a
     * syntactically valid address, written back lower-cased so it is the value stored and returned.
     * Any present-but-invalid address is rejected with a {@code 400 Bad Request}.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if the email is present but not a syntactically valid address
     */
    private void normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        email = email.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        ownerFieldsDto.setEmail(email.toLowerCase(Locale.ROOT));
    }

    /**
     * Rejects creating an owner whose normalized telephone is already used by any other owner.
     * Both the submitted telephone and every existing owner's stored telephone are reduced to their
     * {@linkplain TelephoneNormalizer#toComparisonKey comparison key} before comparison, so equality
     * is judged on that key alone. A match is reported as a {@code 409 Conflict}.
     *
     * @param normalizedTelephone the submitted telephone, already reduced to its canonical form by
     *                            {@link #normalizeTelephone}
     * @throws DuplicateOwnerTelephoneException if another owner already uses the same telephone
     */
    private void rejectDuplicateTelephone(String normalizedTelephone) {
        String comparisonKey = TelephoneNormalizer.toComparisonKey(normalizedTelephone);
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(Objects::nonNull)
            .map(TelephoneNormalizer::toComparisonKey)
            .anyMatch(comparisonKey::equals);
        if (inUse) {
            throw new DuplicateOwnerTelephoneException(normalizedTelephone);
        }
    }

    /**
     * Rejects creating an owner whose lower-cased email is already used by any other owner. Both the
     * submitted email and every existing owner's stored email are compared case-insensitively (on
     * their lower-cased form), so equality is judged on that form alone. A match is reported as a
     * {@code 409 Conflict}. An absent (null or blank) email is not subject to this rule, email being
     * optional.
     *
     * @param normalizedEmail the submitted email, already trimmed and lower-cased by
     *                        {@link #normalizeEmail}, or null/blank when no email was supplied
     * @throws DuplicateOwnerEmailException if another owner already uses the same email
     */
    private void rejectDuplicateEmail(String normalizedEmail) {
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return;
        }
        String comparisonKey = normalizedEmail.toLowerCase(Locale.ROOT);
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getEmail)
            .filter(Objects::nonNull)
            .map(email -> email.toLowerCase(Locale.ROOT))
            .anyMatch(comparisonKey::equals);
        if (inUse) {
            throw new DuplicateOwnerEmailException(normalizedEmail);
        }
    }

    /**
     * Rejects creating an owner who shares a household with any existing owner, unless the request
     * opts in with {@code sharesHousehold} set to {@code true}. Two owners share a household when they
     * have the same {@code lastName} and {@code address}, compared case-insensitively and with runs of
     * whitespace collapsed (see {@link HouseholdNormalizer}); the submitted fields and every existing
     * owner's stored fields are reduced to their {@linkplain HouseholdNormalizer#toComparisonKey
     * comparison key} before comparison. A match is reported as a {@code 409 Conflict}.
     *
     * @param ownerFieldsDto the submitted owner fields (its {@code lastName} and {@code address} are
     *                       non-blank here, {@link #rejectBlankOwnerFields} having already run)
     * @throws DuplicateOwnerHouseholdException if another owner already shares the household and the
     *                                          request did not opt in with {@code sharesHousehold}
     */
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

    private void rejectDuplicateHousehold(OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String comparisonKey = HouseholdNormalizer.toComparisonKey(
            ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        boolean inUse = this.clinicService.findAllOwners().stream()
            .filter(owner -> owner.getLastName() != null && owner.getAddress() != null)
            .map(owner -> HouseholdNormalizer.toComparisonKey(owner.getLastName(), owner.getAddress()))
            .anyMatch(comparisonKey::equals);
        if (inUse) {
            throw new DuplicateOwnerHouseholdException(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        }
    }
}
