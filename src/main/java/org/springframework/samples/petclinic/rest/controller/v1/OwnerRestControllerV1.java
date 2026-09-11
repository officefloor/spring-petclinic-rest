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
import java.util.stream.Stream;

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
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.HouseholdDuplicateException;
import org.springframework.samples.petclinic.rest.advice.InvalidRegistrationDateException;
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
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.PostcodeValidator;
import org.springframework.samples.petclinic.util.Sha256Hex;
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
        // Resolve the owner's household membership: assign the deterministic 'householdId' (a pure
        // function of last name and postcode), reject a household duplicate unless the request
        // declares it shares the household, and record the household size. This runs before the
        // identity-key check so that check sees the owner's household id.
        boolean declaredHouseholdMember =
            applyHouseholdMembership(owner, Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold()));
        // Reject the create when the owner's whole derived identity key (normalized telephone,
        // email and household id) already matches an existing owner. This single rule subsumes the
        // former separate telephone, email and household duplicate checks.
        rejectWhenIdentityInUse(owner);
        // Flag the owner as a possible (soft) duplicate when, although not a hard duplicate, it
        // shares an existing owner's last name and postcode with a different telephone. A declared
        // household member is never flagged: it is a known member, not a suspected duplicate.
        assignPossibleDuplicate(owner, declaredHouseholdMember);
        // Assign the remaining create-time derived fields (namesake count, customer code,
        // membership number and bulk-signup warning), each fixed at creation time.
        assignDerivedFields(owner);
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
     * outright, in order, before any derived fields are assigned or the owner is saved. Each
     * independent rule lives in its own {@code rejectWhen...} guard and is applied here in order, so
     * the sequence of create-time rejections stays readable and a new rule is added as one more
     * guard. Grouping the rules here keeps {@link #addOwner} focused on the household, derived-field
     * and persistence steps. The single duplicate rule that rejects an owner whose whole identity
     * key matches an existing one is applied separately in {@link #rejectWhenIdentityInUse}, after
     * the household id has been resolved, because that key includes the household id.
     *
     * @param owner the newly mapped and normalized owner being created
     */
    private void rejectDisallowedOwnerCreation(Owner owner) {
        rejectWhenDailyLimitReached(owner);
        rejectWhenCityAtCapacity(owner);
    }

    /**
     * Applies the household-membership rules for a newly normalized owner being created. The
     * household id is a deterministic function of the owner's last name and postcode, so owners
     * with the same last name and postcode share it automatically; it is assigned to the new owner
     * here. Because the household is keyed on (last name, postcode), any existing owner with the
     * same last name and postcode is a member of the same household: a second such owner is rejected
     * as a household duplicate (409) unless the request declares it shares the household via
     * {@code sharesHousehold}, in which case it is created as a declared household member. The
     * owner's {@code householdSize} (the number of owners sharing the household after this create)
     * is recorded either way. The assigned household id is part of the owner's identity key, so it
     * is resolved here before {@link #rejectWhenIdentityInUse}.
     *
     * @param owner           the newly mapped and normalized owner being created
     * @param sharesHousehold whether the request declared that the owner shares an existing household
     * @return {@code true} if the owner joined an existing household as a declared member
     */
    private boolean applyHouseholdMembership(Owner owner, boolean sharesHousehold) {
        // Assign the deterministic household id (a pure function of last name and postcode) so every
        // owner with the same last name and postcode shares it automatically.
        owner.setHouseholdId(householdNormalizer.householdId(owner.getLastName(), owner.getPostcode()));
        // Identify any existing owners in the same household (same computed household id).
        List<Owner> householdMembers = sameHouseholdOwners(owner);
        // A second owner in an existing household is a household duplicate: reject it with 409 unless
        // the request declares it shares the household, in which case create it as a declared member.
        boolean declaredHouseholdMember = false;
        if (!householdMembers.isEmpty()) {
            if (!sharesHousehold) {
                throw new HouseholdDuplicateException(
                    "An owner in the household " + owner.getHouseholdId() + " already exists");
            }
            declaredHouseholdMember = true;
        }
        // Record the size of this owner's household after this create, fixed at creation time.
        owner.setHouseholdSize(householdMembers.size() + 1);
        return declaredHouseholdMember;
    }

    /**
     * Assigns the create-time derived fields of a newly normalized owner whose household membership
     * has already been resolved: the namesake count, the per-city customer code, the membership
     * number derived from that customer code and the registration year, and the bulk-signup warning
     * flag. Each value is fixed at creation time and is independent of the others. Grouping them
     * here keeps {@link #addOwner} focused on the high-level create sequence.
     *
     * @param owner the owner being created, already normalized and with its household resolved
     */
    private void assignDerivedFields(Owner owner) {
        // Record how many existing owners already share this owner's first and last name
        // (compared case-insensitively) before this create, fixed at creation time.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Assign the customer code '<REGION>-<HASH8>' from the region derived from the postcode
        // and an 8-hex-character hash of the normalized telephone and last name, fixed at creation
        // time.
        owner.setCustomerCode(customerCode(owner));
        // Assign the membership number '<customerCode>-M<YY>', where YY is the last two digits
        // of the registration date's year (e.g. 'NSW-A1B2C3D4-M26'), fixed at creation time.
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        // Flag the create when more than 80 owners have already been created on this owner's
        // (adjusted business-day) registration date, fixed at creation time.
        owner.setBulkSignupWarning(
            countOwnersRegisteredOn(owner.getRegistrationDate()) > BULK_SIGNUP_WARNING_THRESHOLD);
    }

    /**
     * Rejects creating an owner whose whole derived identity key already matches an existing owner,
     * with 409 Conflict. The identity key ({@link Owner#getIdentityKey()}) joins the normalized
     * telephone, the email (or empty) and the household id (or empty), so this single rule replaces
     * the former separate telephone, email and household duplicate checks: two owners collide only
     * when their whole keys are equal, and members of one household with different telephones have
     * different keys and are both allowed.
     *
     * @param owner the owner being created, already normalized and with its household resolved
     */
    private void rejectWhenIdentityInUse(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean inUse = existingOwners()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (inUse) {
            throw new DuplicateIdentityException(
                "An owner with identity key " + identityKey + " already exists");
        }
    }

    /**
     * Assigns the soft-duplicate flags for a newly normalized owner that has already passed the hard
     * duplicate (identity-key) check. When an existing owner shares this owner's last name (compared
     * case-insensitively) and postcode but carries a different (normalized) telephone, the new owner
     * is still created but flagged as a possible duplicate: {@code possibleDuplicate} is set true and
     * {@code possibleDuplicateOf} to the matching owner's id (the lowest such id when several match).
     * Otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is left absent.
     * A postcode is required on both sides for a match, so an owner without a postcode is never a
     * possible duplicate. A declared household member is never flagged: it is a known member of the
     * household, not a suspected duplicate. Both values are fixed at creation time.
     *
     * @param owner                   the owner being created, already normalized and confirmed not a
     *                                hard duplicate
     * @param declaredHouseholdMember whether the owner joined an existing household as a declared
     *                                member
     */
    private void assignPossibleDuplicate(Owner owner, boolean declaredHouseholdMember) {
        Integer matchId = declaredHouseholdMember || owner.getPostcode() == null ? null : existingOwners()
            .filter(existing -> isPossibleDuplicateOf(existing, owner))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    /**
     * Tests whether an existing owner makes the owner being created a possible (soft) duplicate: the
     * two share a postcode (required on both sides, so an owner without a postcode never matches) and
     * a last name (compared case-insensitively) but carry different (normalized) telephones. A match
     * flags the new owner as a possible duplicate of the existing one without rejecting the create.
     *
     * @param existing an existing owner to compare against
     * @param owner    the owner being created
     * @return {@code true} if the existing owner makes the new owner a possible duplicate
     */
    private boolean isPossibleDuplicateOf(Owner existing, Owner owner) {
        return existing.getPostcode() != null
            && existing.getPostcode().equals(owner.getPostcode())
            && existing.getLastName() != null
            && existing.getLastName().equalsIgnoreCase(owner.getLastName())
            && existing.getTelephone() != null
            && !existing.getTelephone().equals(owner.getTelephone());
    }

    /**
     * Rejects creating an owner once {@link #MAX_OWNERS_PER_DAY} or more owners already carry this
     * owner's adjusted business-day registration date, with 429 Too Many Requests.
     *
     * @param owner the owner being created
     */
    private void rejectWhenDailyLimitReached(Owner owner) {
        if (countOwnersRegisteredOn(owner.getRegistrationDate()) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(
                "The maximum number of owners that may be created today has already been reached");
        }
    }

    /**
     * Rejects creating an owner when the owner's city already contains {@link #MAX_OWNERS_PER_CITY}
     * or more owners, with 409 Conflict.
     *
     * @param owner the owner being created
     */
    private void rejectWhenCityAtCapacity(Owner owner) {
        if (countOwnersInCity(owner.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityCapacityExceededException(
                "The city " + owner.getCity() + " already contains the maximum number of owners");
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
        // A value that cannot form a valid E.164 number, or whose national-number length is wrong
        // for its country code (+61 => 9 national digits, +1 => 10), is rejected with 400.
        owner.setTelephone(telephoneNormalizer.normalizeAndValidate(owner.getTelephone()));
        // Store the (already syntactically validated) email lower-cased so it is persisted and
        // returned in canonical form. A missing email is left untouched.
        owner.setEmail(normalizeEmail(owner.getEmail()));
        // Validate the optional postcode against the fixed range for the city's region. A postcode
        // is checked only when present (it is optional); one out of range for the city's region is
        // rejected with 400. A city with no known region accepts any 4-digit postcode. The stored
        // value is left unchanged.
        PostcodeValidator.validate(owner.getCity(), owner.getPostcode());
        // Reject a supplied registration date that is later than the server's current date with
        // 400, before any defaulting or business-day adjustment, so a future date is never
        // persisted regardless of how the roll-forward would move it.
        rejectFutureRegistrationDate(owner.getRegistrationDate());
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
     * Rejects a supplied registration date that is later than the server's current date with a
     * 400 Bad Request. A missing registration date (defaulted to the server date later) is
     * accepted, and a date on or before today is left for the normal defaulting and business-day
     * adjustment.
     *
     * @param registrationDate the client-supplied registration date, or {@code null} when absent
     */
    private void rejectFutureRegistrationDate(java.time.LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(java.time.LocalDate.now())) {
            throw new InvalidRegistrationDateException(
                "The supplied registration date " + registrationDate + " is later than the current date");
        }
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
     * Builds the customer code '<REGION>-<HASH8>' for a newly created owner, where REGION is the
     * region derived from the owner's postcode (falling back to the city, via
     * {@link LocalityResolver}) and HASH8 is the first 8 upper-case hex characters of the SHA-256
     * digest over the owner's normalized telephone concatenated with the last name (e.g.
     * 'NSW-A1B2C3D4'). The identity carries no sequence number, so it is a pure function of the
     * owner's region and identity fields.
     *
     * @param owner the owner being created, already normalized
     * @return the formatted customer code
     */
    private String customerCode(Owner owner) {
        String region = LocalityResolver.resolve(owner.getCity(), owner.getPostcode());
        String hash8 = Sha256Hex.upperHexPrefix(owner.getTelephone() + owner.getLastName(), 8);
        return region + "-" + hash8;
    }

    /**
     * Streams the existing owners that the create-time rules match a new owner against. Every
     * duplicate, quota and derived-field rule scans this same set of owners and differs only in the
     * predicate it applies, so fetching them is centralized here to keep each rule focused on its
     * own matching criterion rather than on how the existing owners are retrieved.
     *
     * @return a stream over all existing owners
     */
    private Stream<Owner> existingOwners() {
        return this.clinicService.findAllOwners().stream();
    }

    /**
     * Counts the existing owners located in the given city, compared case-insensitively. Used to
     * fix the per-city sequence of an owner's customer code at creation time.
     *
     * @param city the city of the owner being created
     * @return the number of existing owners in a matching city
     */
    private int countOwnersInCity(String city) {
        return (int) existingOwners()
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
        return (int) existingOwners()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Builds the membership number '<customerCode>-M<YY>' for a newly created owner, where YY is
     * the last two digits of the registration date's year (e.g. 'NSW-A1B2C3D4-M26').
     *
     * @param customerCode     the owner's customer code
     * @param registrationDate the owner's registration date
     * @return the formatted membership number
     */
    private String membershipNumber(String customerCode, java.time.LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /**
     * Returns the existing owners in the given owner's household, i.e. those sharing its
     * deterministic household id (the same last name and postcode). The id is computed via
     * {@link HouseholdNormalizer} so that last names differing only in letter case or in incidental
     * whitespace are treated as the same household. Centralising how "the same household" is matched
     * here keeps that definition in one place for every caller.
     *
     * @param owner the owner being created, with its household id already assigned
     * @return the existing owners in the owner's household (possibly empty)
     */
    private List<Owner> sameHouseholdOwners(Owner owner) {
        String householdId = owner.getHouseholdId();
        return existingOwners()
            .filter(existing -> householdId.equals(
                householdNormalizer.householdId(existing.getLastName(), existing.getPostcode())))
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
        return (int) existingOwners()
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
