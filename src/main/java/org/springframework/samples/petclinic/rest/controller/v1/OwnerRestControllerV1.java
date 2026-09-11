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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.FiscalYearResolver;
import org.springframework.samples.petclinic.mapper.HouseholdResolver;
import org.springframework.samples.petclinic.mapper.IdentityKeyResolver;
import org.springframework.samples.petclinic.mapper.MembershipLevelResolver;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.Region;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidPostcodeException;
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
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
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

    /** Fixed public holidays the business-day roll skips: the adjusted registration date never lands on one of these. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"), LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final TelephoneNormalizer telephoneNormalizer;

    private final AddressNormalizer addressNormalizer;

    private final EmailNormalizer emailNormalizer;

    private final IdentityKeyResolver identityKeyResolver;

    private final HouseholdResolver householdResolver;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 TelephoneNormalizer telephoneNormalizer,
                                 AddressNormalizer addressNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 IdentityKeyResolver identityKeyResolver,
                                 HouseholdResolver householdResolver) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.telephoneNormalizer = telephoneNormalizer;
        this.addressNormalizer = addressNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.identityKeyResolver = identityKeyResolver;
        this.householdResolver = householdResolver;
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
        owners.forEach(this::populateHouseholdMemberCount);
        List<OwnerDto> ownerDtos = owners.stream().map(this::toOwnerDto).toList();
        return new ResponseEntity<>(ownerDtos, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        populateHouseholdMemberCount(owner);
        return new ResponseEntity<>(toOwnerDto(owner), HttpStatus.OK);
    }

    /**
     * Maps an owner to its DTO and stamps on the derived {@code identityKey} (see
     * {@link IdentityKeyResolver#deriveIdentityKey}), which is not a stored field of the owner and so cannot be produced
     * by the mapper alone.
     *
     * @param owner the owner to map
     * @return the owner DTO carrying its identity key
     */
    private OwnerDto toOwnerDto(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setIdentityKey(identityKeyResolver.deriveIdentityKey(owner));
        return ownerDto;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        Owner owner = createOwner(ownerFieldsDto);
        return createdOwnerResponse(owner);
    }

    /**
     * Creates and persists a new owner from an incoming payload, applying every creation rule in order. The payload is
     * normalized and validated (see {@link #normalizeAndValidate}), mapped to an owner and stamped with its fields
     * derived at creation (see {@link #populateDerivedFields}); the duplicate guards then run against those derived
     * fields (see {@link #requireUniqueIdentity} and {@link #requireHouseholdNotDuplicate}) before the soft-duplicate
     * flag is recorded (see {@link #flagPossibleDuplicate}); the owner is saved, its transient household member count
     * is resolved (see {@link #populateHouseholdMemberCount}) and the creation is audited (see
     * {@link #auditOwnerCreated}). The returned owner carries its generated id and every field a response needs.
     *
     * @param ownerFieldsDto the incoming owner payload, mutated in place during normalization
     * @return the newly created, persisted owner
     */
    private Owner createOwner(OwnerFieldsDto ownerFieldsDto) {
        normalizeAndValidate(ownerFieldsDto);
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        populateDerivedFields(owner, ownerFieldsDto);
        requireUniqueIdentity(owner);
        requireHouseholdNotDuplicate(owner, ownerFieldsDto);
        flagPossibleDuplicate(owner, ownerFieldsDto);
        this.clinicService.saveOwner(owner);
        populateHouseholdMemberCount(owner);
        auditOwnerCreated(owner);
        return owner;
    }

    /**
     * Builds the {@code 201 Created} response for a newly created owner: the owner mapped to its DTO (carrying its
     * identity key, see {@link #toOwnerDto}) as the body, with a {@code Location} header pointing at the new owner's
     * canonical URL.
     *
     * @param owner the newly created, persisted owner
     * @return a {@code 201 Created} response carrying the owner DTO and its {@code Location} header
     */
    private ResponseEntity<OwnerDto> createdOwnerResponse(Owner owner) {
        OwnerDto ownerDto = toOwnerDto(owner);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Emits the single audit line for a successfully created owner to the dedicated {@link #AUDIT} logger, recording
     * its id, customer code and registration date alongside the two values derived purely for the audit trail: the
     * membership level (see {@link MembershipLevelResolver#deriveMembershipLevel}) and the membership number, the
     * customer code suffixed with {@code -M} and the two-digit fiscal year of the registration date (see
     * {@link FiscalYearResolver#fiscalYear}).
     *
     * @param owner the newly created, persisted owner, with all its derived fields already in place
     */
    private void auditOwnerCreated(Owner owner) {
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            MembershipLevelResolver.deriveMembershipLevel(owner.getEmail(), owner.getNamesakeCount(),
                owner.getHouseholdMemberCount(), owner.getRegistrationDate()),
            owner.getCustomerCode() + "-M" + String.format("%02d",
                FiscalYearResolver.fiscalYear(owner.getRegistrationDate()) % 100));
    }

    /**
     * Stamps onto a newly mapped owner every field derived at creation from the owner itself and from the owners that
     * already exist, in the order each field depends on. The {@code customerCode} is derived from the owner's own
     * fields (see {@link IdentityKeyResolver#deriveCustomerCode}); the {@code namesakeCount} (see
     * {@link #countNamesakes}) and {@code bulkSignupWarning} (see {@link #computeBulkSignupWarning}) are snapshots of
     * the existing owners taken before this owner is saved, so neither counts the new owner itself; and the shared
     * {@code householdId} is assigned last (see {@link #assignHouseholdId}), derived deterministically from the owner's
     * last name and postcode, so it is in place before {@link #requireUniqueIdentity} derives the identity key. The
     * owner is mutated in place; the caller applies the duplicate guards and saves it.
     *
     * @param owner          the newly mapped owner about to be saved
     * @param ownerFieldsDto the incoming owner payload
     */
    private void populateDerivedFields(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        owner.setCustomerCode(deriveUniqueCustomerCode(owner));
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setBulkSignupWarning(computeBulkSignupWarning(owner.getRegistrationDate()));
        assignHouseholdId(owner);
    }

    /**
     * Derives an owner's {@code customerCode} and de-duplicates it against the owners that already exist. The base code
     * is the pure derivation (see {@link IdentityKeyResolver#deriveCustomerCode}); when it collides with an existing
     * owner's {@code customerCode} it is suffixed with {@code -<n>}, taking the smallest {@code n} of 2 or more that
     * makes the whole code unique. Evaluated before the new owner is saved, so the collision check reflects only owners
     * that predate this create; distinct owners therefore always receive distinct customer codes.
     *
     * @param owner the newly mapped owner about to be saved, with its telephone already normalized
     * @return the de-duplicated customer code, unique across all existing owners
     */
    private String deriveUniqueCustomerCode(Owner owner) {
        String base = identityKeyResolver.deriveCustomerCode(owner);
        Collection<Owner> existing = this.clinicService.findAllOwners();
        String candidate = base;
        int n = 2;
        while (isCustomerCodeInUse(candidate, existing)) {
            candidate = base + "-" + n;
            n++;
        }
        return candidate;
    }

    private boolean isCustomerCodeInUse(String customerCode, Collection<Owner> existing) {
        return existing.stream().anyMatch(other -> customerCode.equals(other.getCustomerCode()));
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
     * uses the adjusted date. The payload is mutated in place so the caller can map and save it directly. Duplicate
     * detection is deferred: it runs once, against the derived {@code identityKey}, after the household id has been
     * assigned (see {@link #requireUniqueIdentity}).
     *
     * @param ownerFieldsDto the incoming owner payload, mutated in place
     * @throws MissingOwnerFieldsException if any required field is missing or blank
     */
    private void normalizeAndValidate(OwnerFieldsDto ownerFieldsDto) {
        normalizeAddress(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        validatePostcode(ownerFieldsDto);
        String telephone = telephoneNormalizer.normalize(ownerFieldsDto.getTelephone());
        ownerFieldsDto.setTelephone(telephone);
        requireCityHasCapacity(ownerFieldsDto.getCity());
        LocalDate registrationDate = resolveRegistrationDate(ownerFieldsDto);
        ownerFieldsDto.setRegistrationDate(registrationDate);
        requireDailyLimitNotReached(registrationDate);
        normalizeEmail(ownerFieldsDto);
    }

    /**
     * Normalizes an owner's address to its canonical stored form and writes it back onto the payload. The structured
     * form is preferred over the flat one: when a non-blank {@code addressLine1} is supplied it (and the optional
     * {@code addressLine2}) are normalized (see {@link AddressNormalizer#normalize}) and the flat {@code address} is
     * recomposed as the normalized {@code addressLine1}, with a single space and the normalized {@code addressLine2}
     * appended when {@code addressLine2} is present. Otherwise the flat {@code address} is normalized on its own and the
     * structured fields are cleared. Either way the canonical {@code address} is what gets stored and returned and what
     * every downstream reader uses; a blank result is uniformly treated as a missing address by the required-field
     * check that follows (an owner is valid when it supplies either a non-blank {@code addressLine1} or a non-blank flat
     * {@code address}). The payload is mutated in place so the caller can map and save it directly.
     *
     * @param ownerFieldsDto the incoming owner payload, mutated in place
     */
    private void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        String line1 = addressNormalizer.normalize(ownerFieldsDto.getAddressLine1());
        String line2 = addressNormalizer.normalize(ownerFieldsDto.getAddressLine2());
        if (!line1.isEmpty()) {
            ownerFieldsDto.setAddressLine1(line1);
            ownerFieldsDto.setAddressLine2(line2.isEmpty() ? null : line2);
            ownerFieldsDto.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        }
        else {
            ownerFieldsDto.setAddressLine1(null);
            ownerFieldsDto.setAddressLine2(null);
            ownerFieldsDto.setAddress(addressNormalizer.normalize(ownerFieldsDto.getAddress()));
        }
    }

    /**
     * Normalizes an owner's optional email to its canonical stored form and writes it back onto the payload. The
     * canonical form (see {@link EmailNormalizer#normalize}) is what gets stored and returned as {@code email} and what
     * every email comparison uses; a missing (blank or {@code null}) email is left untouched. The payload is mutated in
     * place so the caller can map and save it directly.
     *
     * @param ownerFieldsDto the incoming owner payload, mutated in place
     */
    private void normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        ownerFieldsDto.setEmail(emailNormalizer.normalize(ownerFieldsDto.getEmail()));
    }

    /**
     * Resolves an owner's effective registration date and rolls it onto a business day. The effective date is the value
     * supplied on the payload, or the current server date when none was supplied; when that date is a Saturday, a Sunday
     * or a listed public holiday (see {@link #PUBLIC_HOLIDAYS}) it is rolled forward one day at a time until it lands on
     * a non-holiday weekday. The returned date is always a business day. A supplied date later than the current server
     * date is rejected.
     *
     * @param ownerFieldsDto the incoming owner payload
     * @return the effective registration date rolled forward onto a business day
     * @throws FutureRegistrationDateException if a supplied registration date is later than the current server date
     */
    private LocalDate resolveRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate effective = ownerFieldsDto.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        else if (effective.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                "the registration date must not be later than the current server date");
        }
        while (effective.getDayOfWeek() == DayOfWeek.SATURDAY || effective.getDayOfWeek() == DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(effective)) {
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
        if (countOwnersRegisteredOn(registrationDate) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(
                "the maximum number of owners for today has already been reached");
        }
    }

    /**
     * Counts how many existing owners already carry the given adjusted business-day {@code registrationDate}. Invoked
     * before the new owner is saved, so the count reflects only owners that predate this create. Shared by
     * {@link #requireDailyLimitNotReached} (the hard per-day cap) and {@link #computeBulkSignupWarning} (the soft
     * warning threshold) so both express the same per-day count in one place.
     *
     * @param registrationDate the adjusted business-day registration date to count owners for
     * @return the number of existing owners registered on that day
     */
    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
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
        return countOwnersRegisteredOn(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD;
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
     * <p>The address is checked after normalization has composed the canonical {@code address} from whichever form was
     * supplied, so a blank {@code address} here means the owner supplied neither a non-blank {@code addressLine1} nor a
     * flat {@code address}.
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
     * Validates an owner's optional {@code postcode} against its city's region. The postcode is validated only when
     * present; an owner created without one is accepted. Its 4-digit shape is already enforced by Bean Validation, so
     * this check only applies the region rule: when the city belongs to a known region (Sydney-&gt;NSW,
     * Melbourne-&gt;VIC, Brisbane-&gt;QLD) the postcode must fall within that region's inclusive range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
     *
     * @param ownerFieldsDto the incoming owner payload
     * @throws InvalidPostcodeException if a supplied postcode is out of range for its city's region
     */
    private void validatePostcode(OwnerFieldsDto ownerFieldsDto) {
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null) {
            return;
        }
        Region region = Region.forCity(ownerFieldsDto.getCity());
        if (region != null && !region.acceptsPostcode(Integer.parseInt(postcode))) {
            throw new InvalidPostcodeException(
                "postcode " + postcode + " is not valid for the city's region " + region.name());
        }
    }

    /**
     * Rejects creating an owner whose whole derived {@code identityKey} (see
     * {@link IdentityKeyResolver#deriveIdentityKey}) equals that of an existing owner, so an owner is an identity
     * duplicate only when its normalized telephone, email and {@code householdId} all match another owner's. It runs
     * after the household id has been assigned, so the key reflects the household the owner belongs to. Owners that
     * share only their household (same last name and postcode, different telephone) are not caught here but by the
     * separate household-duplicate guard (see {@link #requireHouseholdNotDuplicate}). Owners that have been
     * soft-deleted (see {@link #deleteOwner}) are ignored, so a matching identity that belongs only to a deleted
     * owner does not block the create.
     *
     * @param owner the newly mapped owner about to be saved, with its household id already assigned
     * @throws DuplicateOwnerException if any existing owner already has the same identity key
     */
    private void requireUniqueIdentity(Owner owner) {
        String identityKey = identityKeyResolver.deriveIdentityKey(owner);
        boolean inUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(identityKeyResolver.deriveIdentityKey(existing)));
        if (inUse) {
            throw new DuplicateOwnerException("an owner with the same identity key already exists");
        }
    }

    /**
     * Flags a newly created owner as a possible (soft) duplicate. The owner has already cleared both the hard-duplicate
     * guard (see {@link #requireUniqueIdentity}) and the household-duplicate guard (see
     * {@link #requireHouseholdNotDuplicate}), so it is still being created; this only records a warning. A declared
     * household member (one created with {@code sharesHousehold} set to {@code true}) is never flagged: it deliberately
     * shares its household's last name and postcode, so it is not a suspected duplicate. Otherwise it is a possible
     * duplicate when some existing owner shares its last name (compared case-insensitively) and its postcode but
     * carries a different telephone. When such an owner is found, {@code possibleDuplicate} is set to {@code true} and
     * {@code possibleDuplicateOf} to that existing owner's id (the first match in
     * {@link ClinicService#findAllOwners()} order); otherwise {@code possibleDuplicate} is {@code false} and
     * {@code possibleDuplicateOf} is left null. An owner created without a postcode can never soft-match.
     *
     * @param owner          the newly mapped owner about to be saved, with its normalized fields already in place
     * @param ownerFieldsDto the incoming owner payload
     */
    private void flagPossibleDuplicate(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String lastName = owner.getLastName();
        String telephone = owner.getTelephone();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (lastName.equalsIgnoreCase(existing.getLastName())
                && postcode.equals(existing.getPostcode())
                && !java.util.Objects.equals(telephone, existing.getTelephone())) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
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
     * Assigns the shared {@code householdId} for a newly mapped owner. The identifier is derived deterministically from
     * the owner's last name and postcode (see {@link HouseholdResolver#deriveHouseholdId}), so every owner sharing
     * those two fields resolves to the same stable value automatically, regardless of creation order and without any
     * explicit linking. An owner created without a postcode has no household and keeps a {@code null} identifier, so
     * such owners never match one another as a household.
     *
     * @param owner the newly mapped owner about to be saved
     */
    private void assignHouseholdId(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            owner.setHouseholdId(null);
            return;
        }
        owner.setHouseholdId(householdResolver.deriveHouseholdId(owner.getLastName(), postcode));
    }

    /**
     * Rejects creating an owner that would join an existing household it has not declared. Because the household is
     * keyed on the last name and postcode (see {@link #assignHouseholdId}), any existing owner carrying the same
     * derived {@code householdId} is a member of the same household. A second such owner is a household duplicate and is
     * rejected, unless the payload sets {@code sharesHousehold} to {@code true}, which declares the owner a genuine
     * household member and lets the create proceed. An owner with no household ({@code null} householdId, i.e. created
     * without a postcode) can never be a household duplicate. Owners that have been soft-deleted (see
     * {@link #deleteOwner}) are ignored, so a household whose only member has been deleted does not block the create.
     *
     * @param owner          the newly mapped owner about to be saved, with its household id already assigned
     * @param ownerFieldsDto the incoming owner payload
     * @throws DuplicateOwnerException if an existing owner already belongs to this household and the create did not
     *                                 declare {@code sharesHousehold}
     */
    private void requireHouseholdNotDuplicate(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        boolean sharedByExisting = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (sharedByExisting) {
            throw new DuplicateOwnerException("an owner in the same household already exists");
        }
    }

    /**
     * Populates the owner's transient {@code householdMemberCount} with the number of owners that belong to its
     * household, i.e. those sharing the same non-null {@code householdId} (including the owner itself). An owner with no
     * household is counted as a household of one.
     *
     * @param owner the owner whose household size should be resolved
     */
    private void populateHouseholdMemberCount(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdMemberCount(1);
            return;
        }
        long members = this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
        owner.setHouseholdMemberCount((int) members);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        validatePostcode(ownerFieldsDto);
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setAddressLine1(ownerFieldsDto.getAddressLine1());
        currentOwner.setAddressLine2(ownerFieldsDto.getAddressLine2());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        currentOwner.setPostcode(ownerFieldsDto.getPostcode());
        currentOwner.setEmail(emailNormalizer.normalize(ownerFieldsDto.getEmail()));
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
        owner.setDeleted(true);
        this.clinicService.saveOwner(owner);
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
