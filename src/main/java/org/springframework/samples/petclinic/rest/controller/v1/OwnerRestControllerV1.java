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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityFullException;
import org.springframework.samples.petclinic.rest.advice.OwnerDailyLimitException;
import org.springframework.samples.petclinic.mapper.OwnerLocality;
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
import org.springframework.util.StringUtils;
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
     * Basic syntactic check for an email address: a non-empty local part, an '@', and a domain with
     * at least one dot and a two-or-more letter top-level label. Case is ignored here; accepted
     * addresses are lower-cased before being stored.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** An owner's postcode, when supplied, must be exactly four digits. */
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("^[0-9]{4}$");

    /**
     * Disposable e-mail domains an owner may not sign up with. An email whose domain (the part after
     * the '@', compared case-insensitively) is on this blocklist is rejected with a 400.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Dedicated logger for audit side-effects. Emitting audit records on a well-known, separately
     * named logger keeps them addressable independently of the class's diagnostic logging.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Maximum number of owners a single city may contain. A create that would take the owner's city
     * to more than this many owners - i.e. the city already holds this many - is rejected with a 409.
     */
    private static final int CITY_OWNER_LIMIT = 50;

    /**
     * Maximum number of owners that may be registered on a single day. A create that would take the
     * number of owners sharing a {@code registrationDate} beyond this many - i.e. that many owners
     * have already been created that day - is rejected with a 429.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Number of owners that must already have been created for a given day before the create
     * response flags a bulk sign-up. When more than this many owners already share a
     * {@code registrationDate}, the response's {@code bulkSignupWarning} is {@code true}.
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
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupDay(owner.getRegistrationDate()));
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    /**
     * Determines whether the supplied day currently holds more than {@link #BULK_SIGNUP_WARNING_THRESHOLD}
     * owners sharing that {@code registrationDate}, which flags an unusually high volume of same-day
     * owner sign-ups.
     *
     * @param registrationDate the day to inspect, or {@code null}
     * @return {@code true} when more than the threshold of owners are registered on that day
     */
    private boolean isBulkSignupDay(LocalDate registrationDate) {
        return registrationDate != null
            && countOwnersRegisteredOn(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        resolveAddressFields(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        validatePostcode(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(TelephoneNormalizer.normalize(owner.getTelephone()));
        owner.setEmail(normalizeEmail(owner.getEmail()));
        validateRegistrationDate(owner.getRegistrationDate());
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setRegistrationDate(toBusinessDay(owner.getRegistrationDate()));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        owner.setHouseholdId(HouseholdNormalizer.householdId(owner.getLastName(), owner.getPostcode()));
        owner.setIdentityKey(buildIdentityKey(owner));
        if (!sharesHousehold) {
            rejectHouseholdDuplicate(owner.getHouseholdId());
        }
        Integer possibleDuplicateOf = sharesHousehold ? null : findPossibleDuplicate(owner);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        if (countOwnersInCity(owner.getCity()) >= CITY_OWNER_LIMIT) {
            throw new OwnerCityFullException(owner.getCity());
        }
        long ownersRegisteredOnDay = countOwnersRegisteredOn(owner.getRegistrationDate());
        if (ownersRegisteredOnDay >= DAILY_OWNER_LIMIT) {
            throw new OwnerDailyLimitException(owner.getRegistrationDate());
        }
        boolean bulkSignupWarning = ownersRegisteredOnDay > BULK_SIGNUP_WARNING_THRESHOLD;
        owner.setCustomerCode(buildCustomerCode(owner));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(countHouseholdMembers(owner.getHouseholdId()) + 1);
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel(), ownerDto.getMembershipNumber());
        ownerDto.setBulkSignupWarning(bulkSignupWarning);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Canonicalizes the submitted address fields into the form that is validated, stored and
     * returned, before any required-field check or mapping runs. This is the single place a create
     * request's address input is resolved.
     *
     * <p>The structured fields are preferred over the flat input for backward compatibility: when a
     * non-blank {@code addressLine1} is supplied, {@code addressLine1} and the optional
     * {@code addressLine2} are {@link AddressNormalizer#normalize normalized} in place and the flat
     * {@code address} is set to the composed value - the normalized {@code addressLine1}, with a
     * single space and the normalized {@code addressLine2} appended when {@code addressLine2} is
     * present. When no structured line is supplied the flat {@code address} is normalized in place,
     * exactly as before. Either way everything downstream - the required-field check, the mapping onto
     * {@link Owner} and the response - sees the canonical address rather than the raw input. A value
     * that is blank only after normalization is left blank here and rejected later by
     * {@link #validateRequiredFields}.
     *
     * @param ownerFieldsDto the submitted owner fields, mutated in place with the resolved address
     */
    private void resolveAddressFields(OwnerFieldsDto ownerFieldsDto) {
        String line1 = AddressNormalizer.normalize(ownerFieldsDto.getAddressLine1());
        String line2 = AddressNormalizer.normalize(ownerFieldsDto.getAddressLine2());
        if (!line1.isEmpty()) {
            ownerFieldsDto.setAddressLine1(line1);
            ownerFieldsDto.setAddressLine2(line2.isEmpty() ? null : line2);
            ownerFieldsDto.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        }
        else {
            ownerFieldsDto.setAddress(AddressNormalizer.normalize(ownerFieldsDto.getAddress()));
        }
    }

    /**
     * Rejects an owner payload that is missing or blank in any required field. Collects the name of
     * every offending field and, if there is at least one, raises a {@link MissingOwnerFieldsException}
     * so the client receives a 400 whose {@code errors} array lists each missing field. The address is
     * checked in its resolved form (see {@link #resolveAddressFields}), which is satisfied by either a
     * non-blank {@code addressLine1} or the flat {@code address}, so a value that is blank only after
     * normalization is still rejected.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getAddress())) {
            missingFields.add("address");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
    }

    /**
     * Validates an owner's optional postcode. Postcode is optional, so a missing or blank value is
     * accepted unchanged. When a value is supplied it must be exactly four digits; and when the owner's
     * city maps to a known region the postcode must fall within that region's inclusive range (checked
     * via {@link OwnerLocality#postcodeMatchesCity}, which owns the region table). A city with no known
     * region accepts any 4-digit postcode. A malformed or out-of-range postcode is rejected with a 400
     * whose {@code errors} array lists {@code postcode}.
     *
     * @param postcode the submitted postcode, or {@code null} when none was supplied
     * @param city the owner's city, whose region determines the acceptable postcode range
     * @throws InvalidOwnerFieldsException if a supplied postcode is not four digits or is out of range
     */
    private void validatePostcode(String postcode, String city) {
        if (!StringUtils.hasText(postcode)) {
            return;
        }
        if (!POSTCODE_PATTERN.matcher(postcode).matches()) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
        if (!OwnerLocality.postcodeMatchesCity(postcode, city)) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }

    /**
     * Validates an owner's optional registration date. Registration date is optional, so a missing
     * value is accepted (the create later defaults it to the server date). When a value is supplied
     * it may not lie in the future: a date later than the server's current date is rejected with a
     * 400 whose {@code errors} array lists {@code registrationDate}.
     *
     * @param registrationDate the submitted registration date, or {@code null} when none was supplied
     * @throws InvalidOwnerFieldsException if a supplied date is later than the server date
     */
    private void validateRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidOwnerFieldsException(List.of("registrationDate"));
        }
    }

    /**
     * Rejects a submitted owner that would join a household which already has a member. Because the
     * {@code householdId} is derived deterministically from the owner's last name and postcode, any
     * existing owner sharing that id is the same household, so a second such owner is reported to the
     * client as a 409 Conflict. A request may knowingly bypass this block by opting in with
     * {@code sharesHousehold}, in which case this check is not performed.
     *
     * @param householdId the derived household identifier of the owner about to be created
     */
    private void rejectHouseholdDuplicate(String householdId) {
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (duplicate) {
            throw new DuplicateOwnerIdentityException(householdId);
        }
    }

    /**
     * Derives the owner's {@code identityKey}, formatted
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the telephone
     * and email have already been normalized. The household segment always carries the owner's
     * deterministic {@code householdId}, since owners with the same last name and postcode belong to
     * the same household automatically.
     *
     * @param owner the normalized owner about to be created, with its {@code householdId} already set
     * @return the derived identity key
     */
    private String buildIdentityKey(Owner owner) {
        String email = StringUtils.hasText(owner.getEmail()) ? owner.getEmail() : "";
        return owner.getTelephone() + "|" + email + "|" + owner.getHouseholdId();
    }

    /**
     * Detects a soft-match duplicate for the owner about to be created. A soft match is an owner that
     * shares an existing owner's {@code lastName} (case-insensitively) and {@code postcode} while
     * having a different normalized {@code telephone}. Such an owner is still created, but is flagged
     * as a possible duplicate of the matching owner. This is only consulted for owners that did not
     * opt into a shared household: an owner that declares {@code sharesHousehold} is a known household
     * member, not a suspected duplicate, so it is never flagged. The comparison only applies when a
     * postcode was supplied, since both owners must share one; when several existing owners match, the
     * earliest (lowest id) is used.
     *
     * @param owner the normalized owner about to be created, with its telephone already normalized
     * @return the id of the matching existing owner, or {@code null} when there is no soft match
     */
    private Integer findPossibleDuplicate(Owner owner) {
        if (!StringUtils.hasText(owner.getPostcode())) {
            return null;
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> owner.getLastName().equalsIgnoreCase(existing.getLastName()))
            .filter(existing -> owner.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> !owner.getTelephone().equals(existing.getTelephone()))
            .map(Owner::getId)
            .filter(id -> id != null)
            .min(Integer::compareTo)
            .orElse(null);
    }

    /**
     * Normalizes an optional email address for owner creation. Email is optional, so a missing or
     * blank value is left untouched. When a value is supplied it must be a syntactically valid
     * address; the accepted value is lower-cased before it is stored and returned.
     *
     * @param email the raw email value submitted by the client, or {@code null}
     * @return the lower-cased email, or the original blank/{@code null} value when none was supplied
     * @throws InvalidOwnerFieldsException if a non-blank value is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        String normalized = email.toLowerCase();
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return normalized;
    }

    /**
     * Assigns the owner's customer code, formatted {@code <REGION>-<HASH8>}. The region is derived
     * from the owner's postcode (falling back to its city), and the code itself - region plus the
     * hash of the owner's normalized telephone and last name - is composed by {@link CustomerCode}.
     * When the composed code collides with an existing owner's customer code, it is de-duplicated by
     * appending {@code -<n>} with the smallest {@code n} of two or more that makes it unique.
     *
     * @param owner the normalized owner about to be created
     * @return the assigned, de-duplicated customer code
     */
    private String buildCustomerCode(Owner owner) {
        String region = OwnerLocality.forPostcodeAndCity(owner.getPostcode(), owner.getCity());
        String code = CustomerCode.forRegionAndIdentity(region, owner.getTelephone(), owner.getLastName());
        return deduplicateCustomerCode(code);
    }

    /**
     * De-duplicates a freshly composed customer code against the codes of all existing owners. When
     * the code is already unique it is returned unchanged; otherwise {@code -<n>} is appended, using
     * the smallest {@code n} of two or more that yields a code no existing owner holds.
     *
     * @param code the composed customer code, before de-duplication
     * @return the same code when unique, otherwise the code with the smallest available {@code -<n>} suffix
     */
    private String deduplicateCustomerCode(String code) {
        Set<String> existingCodes = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(existing -> existing != null)
            .collect(Collectors.toSet());
        if (!existingCodes.contains(code)) {
            return code;
        }
        int suffix = 2;
        while (existingCodes.contains(code + "-" + suffix)) {
            suffix++;
        }
        return code + "-" + suffix;
    }

    /**
     * Counts how many owners already exist that share the supplied first and last name, compared
     * case-insensitively. This reflects the number of namesakes present <em>before</em> the current
     * owner is created and is stored on the new owner as its {@code namesakeCount}.
     *
     * @param firstName the submitted owner's first name
     * @param lastName the submitted owner's last name
     * @return the number of existing owners with the same first and last name, ignoring case
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Counts how many existing owners already belong to the supplied household, identified by its
     * {@code householdId}. Used when creating an owner to determine the household's size once the new
     * owner is added (this count plus one).
     *
     * @param householdId the normalized household identifier of the owner being created
     * @return the number of existing owners already sharing that {@code householdId}
     */
    private int countHouseholdMembers(String householdId) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * Counts how many owners already live in the supplied city, compared case-insensitively. Used to
     * enforce the per-city capacity limit when a new owner is created.
     *
     * @param city the submitted owner's city
     * @return the number of existing owners in that city, ignoring case
     */
    private long countOwnersInCity(String city) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
    }

    /**
     * Counts how many owners have already been created on the supplied registration date. Used to
     * enforce the per-day capacity limit when a new owner is created.
     *
     * @param registrationDate the registration date of the owner being created
     * @return the number of existing owners already registered on that date
     */
    /**
     * Rolls a registration date forward to a business day. A registration date must fall on a
     * business day, so when the supplied or defaulted date lands on a Saturday or Sunday it is
     * moved forward to the following Monday; a weekday is returned unchanged. Every value derived
     * from the registration date (such as the membership number's year segment) and the daily
     * create-limit therefore use the adjusted date.
     *
     * @param date the effective registration date, whether supplied or defaulted to the server date
     * @return the same date when it is a weekday, otherwise the next Monday
     */
    private LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
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
