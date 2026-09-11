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
import org.springframework.samples.petclinic.rest.advice.CityCapacityExceededException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.AddressNormalizer;
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

    /** Maximum number of owners a single city may contain; creating an owner in a city that
     *  already has this many owners is rejected with 409 Conflict. */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /** Maximum number of owners that may be created on a single day (by registration date); creating
     *  an owner once this many owners already carry today's registration date is rejected with
     *  429 Too Many Requests. */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /** When more than this many owners already carry today's registration date at creation time, the
     *  new owner's 'bulkSignupWarning' flag is set true; otherwise it is false. */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /** Dedicated audit logger; a line is emitted here for each successful owner create. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final TelephoneNormalizer telephoneNormalizer;

    private final HouseholdNormalizer householdNormalizer;

    private final AddressNormalizer addressNormalizer;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 TelephoneNormalizer telephoneNormalizer,
                                 HouseholdNormalizer householdNormalizer,
                                 AddressNormalizer addressNormalizer) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.telephoneNormalizer = telephoneNormalizer;
        this.householdNormalizer = householdNormalizer;
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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        normalizeOwnerFields(owner);
        // Enforce the pre-persistence policies that can reject the new owner outright.
        rejectDisallowedOwnerCreation(owner);
        // Identify any existing owners who share a household (same last name and address,
        // compared case-insensitively with collapsed whitespace) with the owner being created.
        List<Owner> householdMembers = sameHouseholdOwners(owner.getLastName(), owner.getAddress());
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // Reject creating an owner who shares a household with an existing owner, unless the
        // request explicitly opts in with 'sharesHousehold' true.
        if (!sharesHousehold && !householdMembers.isEmpty()) {
            throw new DuplicateHouseholdException(
                "An owner with the same last name and address already exists");
        }
        // When opting in and joining an existing household, assign the new owner and every
        // existing member the same stable household identifier derived from the last name and
        // address, so all owners of the household share one 'householdId'.
        if (sharesHousehold && !householdMembers.isEmpty()) {
            String householdId = householdNormalizer.householdId(owner.getLastName(), owner.getAddress());
            owner.setHouseholdId(householdId);
            for (Owner member : householdMembers) {
                if (!householdId.equals(member.getHouseholdId())) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        // Record the size of this owner's household after this create, i.e. the number of owners
        // sharing this owner's 'householdId' once it is persisted. An owner that shares an existing
        // household counts the matched members plus itself; an owner without a shared household is a
        // household of one. Fixed at creation time and used to derive the 'GOLD' membership tier.
        owner.setHouseholdSize(owner.getHouseholdId() == null ? 1 : householdMembers.size() + 1);
        // Record how many existing owners already share this owner's first and last name
        // (compared case-insensitively) before this create, fixed at creation time.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Assign the customer code '<CITY3>-<LAST3>-<NNNN>' from the city, last name and a
        // per-city sequence equal to one more than the owners already in that city, fixed at
        // creation time.
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        // Assign the membership number '<customerCode>-M<YY>', where YY is the last two digits
        // of the registration date's year (e.g. 'LON-SMI-0007-M26'), fixed at creation time.
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        // Flag the create when more than 80 owners have already been created on this owner's
        // (adjusted business-day) registration date, fixed at creation time.
        owner.setBulkSignupWarning(
            countOwnersRegisteredOn(owner.getRegistrationDate()) > BULK_SIGNUP_WARNING_THRESHOLD);
        this.clinicService.saveOwner(owner);
        // Emit an audit line for the successful create, carrying the owner id, customer code and
        // registration date so the create can be traced from the dedicated AUDIT log.
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Enforces the pre-persistence policies that reject a newly mapped and normalized owner
     * outright, in order, before any derived fields are assigned or the owner is saved. An owner
     * in a city that already contains {@link #MAX_OWNERS_PER_CITY} owners is rejected with 409
     * ({@link CityCapacityExceededException}), and an owner whose normalized telephone is already
     * used by another owner is rejected with 409 ({@link DuplicateTelephoneException}). Grouping the
     * independent create-time rejection rules here keeps {@link #addOwner} focused on the household,
     * derived-field and persistence steps. The household rule stays in {@link #addOwner} itself
     * because it shares the matched-member lookup with the shared household-id assignment.
     *
     * @param owner the newly mapped and normalized owner being created
     */
    private void rejectDisallowedOwnerCreation(Owner owner) {
        // Reject creating an owner once 100 or more owners already carry this owner's adjusted
        // business-day registration date.
        if (countOwnersRegisteredOn(owner.getRegistrationDate()) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(
                "The maximum number of owners that may be created today has already been reached");
        }
        // Reject creating an owner when the owner's city already contains 50 or more owners.
        if (countOwnersInCity(owner.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityCapacityExceededException(
                "The city " + owner.getCity() + " already contains the maximum number of owners");
        }
        // Reject creating an owner whose normalized telephone is already used by another owner.
        String normalizedTelephone = owner.getTelephone();
        if (isTelephoneInUse(normalizedTelephone)) {
            throw new DuplicateTelephoneException(
                "An owner with telephone " + normalizedTelephone + " already exists");
        }
    }

    /**
     * Canonicalizes the fields of a newly mapped owner on create, in place, so each value is
     * persisted and returned in its canonical form. The address is normalized (trimmed, whitespace
     * collapsed, upper-cased and common abbreviations expanded, rejecting a value blank after
     * normalization with 400), the telephone is normalized to E.164 (rejecting an unformattable
     * value with 400), the email is lower-cased, and a missing registration date defaults to the
     * server's current date. Canonicalising the address here, before the household checks read it,
     * ensures duplicate detection and the shared household id both compare the normalized form.
     * Centralising these per-field canonicalizations here keeps {@link #addOwner} focused on the
     * duplicate, household and customer-code rules.
     *
     * @param owner the newly mapped owner to canonicalize
     */
    private void normalizeOwnerFields(Owner owner) {
        // Normalize the address on create so it is stored and returned in canonical form and every
        // later comparison (household duplicate detection and the shared household id) uses it.
        // A value that is blank after normalization is rejected with 400.
        owner.setAddress(addressNormalizer.normalize(owner.getAddress()));
        // Normalize the telephone on create so it is stored and returned in canonical E.164 form.
        // A value that cannot form a valid E.164 number is rejected with 400.
        owner.setTelephone(telephoneNormalizer.normalize(owner.getTelephone()));
        // Store the (already syntactically validated) email lower-cased so it is persisted and
        // returned in canonical form. A missing email is left untouched.
        owner.setEmail(normalizeEmail(owner.getEmail()));
        // Default the registration date to the server's current date when the client did not
        // supply one, then roll the effective date forward to a business day so a Saturday or
        // Sunday (whether supplied or defaulted) becomes the following Monday. The adjusted date
        // is persisted and returned in ISO 'YYYY-MM-DD' form and is the value every registration-
        // date-derived field (such as the membership number's year segment) and the per-day
        // create-limit are computed from.
        java.time.LocalDate effectiveDate = owner.getRegistrationDate() == null
            ? java.time.LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(toBusinessDay(effectiveDate));
    }

    /**
     * Rolls a registration date forward to a business day: a Saturday or Sunday advances to the
     * following Monday, while a weekday is returned unchanged. Applied to the effective
     * registration date (whether supplied in the request or defaulted to the server date) so the
     * persisted registration date, every value derived from it and the per-day create-limit all
     * use the adjusted business day.
     *
     * @param date the effective registration date
     * @return the same date if it is a weekday, otherwise the following Monday
     */
    private java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        java.time.LocalDate adjusted = date;
        while (adjusted.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
            || adjusted.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    /**
     * Lower-cases the supplied email address so it is stored and returned in canonical form.
     * Bean validation on the request DTO has already guaranteed that any non-null value is a
     * syntactically valid address (rejecting anything else with 400).
     *
     * @param email the email value (may be {@code null})
     * @return the lower-cased email, or {@code null} if the input was {@code null}
     */
    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Builds the customer code '<CITY3>-<LAST3>-<NNNN>' for a newly created owner, where CITY3 is
     * the upper-cased first three letters of the city, LAST3 is the upper-cased first three letters
     * of the last name and NNNN is a per-city 4-digit zero-padded sequence equal to one more than
     * the owners already in that city (e.g. 'LON-SMI-0007').
     *
     * @param city     the owner's city
     * @param lastName the owner's last name
     * @return the formatted customer code
     */
    private String nextCustomerCode(String city, String lastName) {
        String cityPrefix = city.substring(0, Math.min(3, city.length()))
            .toUpperCase(java.util.Locale.ROOT);
        String lastPrefix = lastName.substring(0, Math.min(3, lastName.length()))
            .toUpperCase(java.util.Locale.ROOT);
        int sequence = countOwnersInCity(city) + 1;
        return String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence);
    }

    /**
     * Counts the existing owners located in the given city, compared case-insensitively. Used to
     * fix the per-city sequence of an owner's customer code at creation time.
     *
     * @param city the city of the owner being created
     * @return the number of existing owners in a matching city
     */
    private int countOwnersInCity(String city) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null
                && existing.getCity().equalsIgnoreCase(city))
            .count();
    }

    /**
     * Counts the existing owners whose registration date is the given adjusted business day. Used
     * to enforce the per-day cap that rejects creating an owner once {@link #MAX_OWNERS_PER_DAY}
     * owners already carry that business day as their registration date.
     *
     * @param date the adjusted business-day registration date to match
     * @return the number of existing owners registered on that date
     */
    private int countOwnersRegisteredOn(java.time.LocalDate date) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Builds the membership number '<customerCode>-M<YY>' for a newly created owner, where YY is
     * the last two digits of the registration date's year (e.g. 'LON-SMI-0007-M26').
     *
     * @param customerCode     the owner's customer code
     * @param registrationDate the owner's registration date
     * @return the formatted membership number
     */
    private String membershipNumber(String customerCode, java.time.LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /**
     * Determines whether the given normalized telephone is already used by an existing owner.
     * Stored telephones are normalized to E.164 before comparison so values that differ only in
     * formatting (spaces, dashes, brackets) or in national vs. international notation are treated
     * as the same number.
     *
     * @param normalizedTelephone the normalized (E.164) telephone to look for
     * @return {@code true} if another owner already uses this telephone
     */
    private boolean isTelephoneInUse(String normalizedTelephone) {
        if (normalizedTelephone == null) {
            return false;
        }
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .map(telephoneNormalizer::normalize)
            .anyMatch(normalizedTelephone::equals);
    }

    /**
     * Returns the existing owners who share the given household, i.e. have the same last name and
     * the same address. The comparison delegates to {@link HouseholdNormalizer} so that values
     * differing only in letter case or in incidental whitespace are treated as the same household.
     *
     * @param lastName the last name of the owner being created
     * @param address  the address of the owner being created
     * @return the existing owners with a matching last name and address (possibly empty)
     */
    private List<Owner> sameHouseholdOwners(String lastName, String address) {
        String householdKey = householdNormalizer.householdKey(lastName, address);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdNormalizer
                .householdKey(existing.getLastName(), existing.getAddress()).equals(householdKey))
            .toList();
    }

    /**
     * Counts the existing owners who share the given first and last name, compared
     * case-insensitively. Used to fix an owner's namesake count at creation time.
     *
     * @param firstName the first name of the owner being created
     * @param lastName  the last name of the owner being created
     * @return the number of existing owners with a matching first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null
                && existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName() != null
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
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
        // Normalize the telephone to canonical E.164 form so it is stored and returned
        // consistently with create; an unformattable value is rejected with 400.
        currentOwner.setTelephone(telephoneNormalizer.normalize(ownerFieldsDto.getTelephone()));
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
