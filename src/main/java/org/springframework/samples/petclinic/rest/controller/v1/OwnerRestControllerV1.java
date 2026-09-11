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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.HouseholdResolver;
import org.springframework.samples.petclinic.mapper.IdentityKeyResolver;
import org.springframework.samples.petclinic.mapper.MembershipLevelResolver;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.Region;
import org.springframework.samples.petclinic.mapper.SoundexResolver;
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

import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * Source of the {@link OwnerCreatedEvent}'s {@code seq}: a process-wide, monotonically increasing counter across
     * every owner create, so the structured audit events carry a strictly ordered sequence regardless of which
     * controller instance handled the request.
     */
    private static final AtomicLong OWNER_CREATED_SEQUENCE = new AtomicLong();

    /** Maximum number of owners a single city may contain; creating an owner in a full city is rejected. */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /** Once a city already holds at least this many owners (but is not yet full), a further create there is flagged with a capacity warning. */
    private static final int CAPACITY_WARNING_THRESHOLD = 40;

    /** Maximum number of owners that may be created in a single day; creating an owner past this cap is rejected. */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /** Once more than this many owners already exist for a day, a further create for that day is flagged as a bulk signup. */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /** Request header carrying the client-supplied idempotency key for a create; see {@link #addOwner}. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per idempotency key, the id of the owner the first create with that key produced, so a repeated
     * create with an already-seen key returns the original owner instead of creating a duplicate.
     */
    private final Map<String, Integer> idempotentOwnerIds = new ConcurrentHashMap<>();

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

    /** The current request, used to read the optional {@code Idempotency-Key} header on a create. */
    private final HttpServletRequest request;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 TelephoneNormalizer telephoneNormalizer,
                                 AddressNormalizer addressNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 IdentityKeyResolver identityKeyResolver,
                                 HouseholdResolver householdResolver,
                                 HttpServletRequest request) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.telephoneNormalizer = telephoneNormalizer;
        this.addressNormalizer = addressNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.identityKeyResolver = identityKeyResolver;
        this.householdResolver = householdResolver;
        this.request = request;
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
     * Maps an owner to its DTO and stamps on the two derived values the mapper cannot produce on its own, each because
     * it depends on more than the single owner: the {@code identityKey} (see
     * {@link IdentityKeyResolver#deriveIdentityKey}), which is not a stored field of the owner, and the household-capped
     * {@code membershipLevel} (see {@link #cappedMembershipLevel}), which the mapper derives from the owner alone but
     * which the ceiling rule then caps against the owner's fellow household members. The capped level replaces the
     * mapper's uncapped one on the returned DTO.
     *
     * @param owner the owner to map
     * @return the owner DTO carrying its identity key and household-capped membership level
     */
    private OwnerDto toOwnerDto(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setIdentityKey(identityKeyResolver.deriveIdentityKey(owner));
        ownerDto.setMembershipLevel(cappedMembershipLevel(owner));
        return ownerDto;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            ResponseEntity<OwnerDto> replay = replayIdempotentCreate(idempotencyKey);
            if (replay != null) {
                return replay;
            }
        }
        Owner owner = createOwner(ownerFieldsDto);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotentOwnerIds.put(idempotencyKey, owner.getId());
        }
        return createdOwnerResponse(owner);
    }

    /**
     * Replays the create that a previously-seen {@code Idempotency-Key} produced. When the key has already created an
     * owner that still exists, the original owner is returned with {@code 200 OK} instead of a duplicate being created;
     * otherwise (key unseen, or its owner has since been removed) {@code null} is returned and the create proceeds
     * normally.
     *
     * @param idempotencyKey the client-supplied idempotency key from the request header
     * @return a {@code 200 OK} response carrying the original owner, or {@code null} when there is nothing to replay
     */
    private ResponseEntity<OwnerDto> replayIdempotentCreate(String idempotencyKey) {
        Integer existingId = idempotentOwnerIds.get(idempotencyKey);
        if (existingId == null) {
            return null;
        }
        Owner owner = this.clinicService.findOwnerById(existingId);
        if (owner == null) {
            return null;
        }
        populateHouseholdMemberCount(owner);
        return new ResponseEntity<>(toOwnerDto(owner), HttpStatus.OK);
    }

    /**
     * Creates and persists a new owner from an incoming payload, applying every creation rule in order. The payload is
     * normalized and validated (see {@link #normalizeAndValidate}), mapped to an owner and stamped with its fields
     * derived at creation (see {@link #populateDerivedFields}); the duplicate guard then runs against those derived
     * fields (see {@link #requireUniqueIdentity}) before the soft-duplicate
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
     * its id, member id and registration date alongside the membership level (see
     * {@link MembershipLevelResolver#deriveMembershipLevel}) derived purely for the audit trail.
     *
     * <p>In addition to the human-readable line, a single immutable structured event is emitted as JSON (see
     * {@link OwnerCreatedEvent}), carrying a monotonically increasing {@code seq} (see
     * {@link #OWNER_CREATED_SEQUENCE}), the owner's id, its primary identifier (see
     * {@link #primaryIdentifier(Owner)}) and its membership level under the {@code OWNER_CREATED} event type.
     *
     * @param owner the newly created, persisted owner, with all its derived fields already in place
     */
    private void auditOwnerCreated(Owner owner) {
        AUDIT.info("owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), primaryIdentifier(owner), owner.getRegistrationDate(),
            MembershipLevelResolver.deriveMembershipLevel(owner));
        OwnerCreatedEvent event = new OwnerCreatedEvent(OWNER_CREATED_SEQUENCE.incrementAndGet(),
            owner.getId(), primaryIdentifier(owner), cappedMembershipLevel(owner));
        AUDIT.info(event.toJson());
    }

    /**
     * Returns the owner's primary identifier — the unified {@code memberId} the structured {@link OwnerCreatedEvent}
     * carries as the owner's identity.
     *
     * @param owner the newly created, persisted owner
     * @return the owner's member id
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Stamps onto a newly mapped owner every field derived at creation from the owner itself and from the owners that
     * already exist, in the order each field depends on. The {@code memberId} is derived from the owner's own
     * fields (see {@link IdentityKeyResolver#deriveMemberId}); the {@code namesakeCount} (see
     * {@link #countNamesakes}), {@code bulkSignupWarning} (see {@link #computeBulkSignupWarning}) and
     * {@code capacityWarning} (see {@link #computeCapacityWarning}) are snapshots of the existing owners taken before
     * this owner is saved, so none counts the new owner itself; and the shared
     * {@code householdId} is assigned last (see {@link #assignHouseholdId}), derived deterministically from the owner's
     * last name and postcode, so it is in place before the owner's household membership level is capped (see
     * {@link #cappedMembershipLevel}). The owner is mutated in place; the caller applies the duplicate guards and saves it.
     *
     * @param owner          the newly mapped owner about to be saved
     * @param ownerFieldsDto the incoming owner payload
     */
    private void populateDerivedFields(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        owner.setMemberId(deriveUniqueMemberId(owner));
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setBulkSignupWarning(computeBulkSignupWarning(owner.getRegistrationDate()));
        owner.setCapacityWarning(computeCapacityWarning(owner.getCity()));
        assignHouseholdId(owner);
    }

    /**
     * Derives an owner's {@code memberId} and de-duplicates it against the owners that already exist. The base id
     * is the pure derivation (see {@link IdentityKeyResolver#deriveMemberId}); when it collides with an existing
     * owner's {@code memberId} it is suffixed with {@code -<n>}, taking the smallest {@code n} of 2 or more that
     * makes the whole id unique. Evaluated before the new owner is saved, so the collision check reflects only owners
     * that predate this create; distinct owners therefore always receive distinct member ids.
     *
     * @param owner the newly mapped owner about to be saved, with its telephone already normalized
     * @return the de-duplicated member id, unique across all existing owners
     */
    private String deriveUniqueMemberId(Owner owner) {
        String base = identityKeyResolver.deriveMemberId(owner);
        Collection<Owner> existing = this.clinicService.findAllOwners();
        String candidate = base;
        int n = 2;
        while (isMemberIdInUse(candidate, existing)) {
            candidate = base + "-" + n;
            n++;
        }
        return candidate;
    }

    private boolean isMemberIdInUse(String memberId, Collection<Owner> existing) {
        return existing.stream().anyMatch(other -> memberId.equals(other.getMemberId()));
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
     * duplicate only when its normalized telephone, email and last-name Soundex all match another owner's. This single
     * identity key is now the sole duplicate rule: because the telephone is part of the key, owners that share only
     * their last name and postcode but carry different telephones have distinct keys and are not caught here — they are
     * created and flagged as a soft match instead (see {@link #flagPossibleDuplicate}), their membership level being
     * capped where they join an existing household (see {@link #cappedMembershipLevel}). Owners that have been
     * soft-deleted (see {@link #deleteOwner}) are ignored, so a matching identity that belongs only to a deleted owner
     * does not block the create.
     *
     * @param owner the newly mapped owner about to be saved, with its normalized fields already in place
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
     * Flags a newly created owner as a possible (soft) duplicate. The owner has already cleared the hard-duplicate
     * guard (see {@link #requireUniqueIdentity}), so it is still being created; this only records a warning. A declared
     * household member (one created with {@code sharesHousehold} set to {@code true}) is never flagged: it deliberately
     * shares its household's last name and postcode, so it is not a suspected duplicate. Otherwise it is a possible
     * duplicate when some existing owner shares its last-name Soundex (see {@link SoundexResolver#soundex}) and its
     * postcode but has a different {@code identityKey}. When such an owner is found, {@code possibleDuplicate} is set to {@code true} and
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
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (isPossibleDuplicateOf(owner, existing)) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    /**
     * Decides whether {@code existing} makes {@code owner} a possible (soft) duplicate: some other owner whose last-name
     * Soundex (see {@link SoundexResolver#soundex}) and postcode both match {@code owner}'s but whose derived
     * {@code identityKey} (see {@link IdentityKeyResolver#deriveIdentityKey}) differs. An equal identity key would make
     * the two a hard duplicate, already rejected by {@link #requireUniqueIdentity}; a differing key with a matching
     * name-sound and postcode — for instance two owners sharing a last name and postcode but carrying different
     * telephones — is the soft match this flags. The postcode-present guard is applied by the caller (see
     * {@link #flagPossibleDuplicate}), so {@code owner}'s postcode is non-blank here.
     *
     * @param owner    the newly mapped owner about to be saved, with its normalized fields already in place
     * @param existing an already-persisted owner to test the new owner against
     * @return {@code true} when {@code existing} makes {@code owner} a possible duplicate
     */
    private boolean isPossibleDuplicateOf(Owner owner, Owner existing) {
        return SoundexResolver.soundex(owner.getLastName()).equals(SoundexResolver.soundex(existing.getLastName()))
            && owner.getPostcode().equals(existing.getPostcode())
            && !identityKeyResolver.deriveIdentityKey(owner).equals(identityKeyResolver.deriveIdentityKey(existing));
    }

    /**
     * Rejects creating an owner whose city (compared case-insensitively) already contains the maximum number of
     * owners. The cap is 50, so a city holding 50 or more owners is full and no further owner may be created there.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityFullException if the city already contains 50 or more owners
     */
    private void requireCityHasCapacity(String city) {
        if (countOwnersInCity(city) >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityFullException("the owner's city already contains the maximum number of owners");
        }
    }

    /**
     * Counts how many existing owners already belong to the given {@code city}, compared case-insensitively. Invoked
     * before the new owner is saved, so the count reflects only owners that predate this create; it never counts the
     * new owner itself. Factored out of {@link #requireCityHasCapacity} so the per-city owner count is expressed in one
     * place, mirroring how {@link #countOwnersRegisteredOn} backs the per-day rules.
     *
     * @param city the city to count owners for
     * @return the number of existing owners in that city
     */
    private long countOwnersInCity(String city) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
    }

    /**
     * Determines whether creating an owner in the given city should carry a capacity warning. The warning is raised
     * when the city (compared case-insensitively) already holds between {@value #CAPACITY_WARNING_THRESHOLD} and
     * {@code MAX_OWNERS_PER_CITY - 1} owners, i.e. it is approaching but has not yet reached the hard per-city limit of
     * {@value #MAX_OWNERS_PER_CITY}. Evaluated before the new owner is saved, so the count reflects only owners that
     * predate this create; the hard rejection at the limit (see {@link #requireCityHasCapacity}) still runs.
     *
     * @param city the city of the owner being created
     * @return {@code true} when the city already holds 40 to 49 owners, {@code false} otherwise
     */
    private boolean computeCapacityWarning(String city) {
        long count = countOwnersInCity(city);
        return count >= CAPACITY_WARNING_THRESHOLD && count < MAX_OWNERS_PER_CITY;
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
     * Returns an owner's household-capped membership level: the level the {@link MembershipLevelResolver} derives from
     * the owner (see {@link MembershipLevelResolver#deriveMembershipLevel(Owner)}), lowered where necessary so it never
     * exceeds one above the highest capped level among the owner's <em>existing</em> household members — the members
     * that predate the owner, identified as those sharing its non-null {@code householdId} with a smaller id. When the
     * owner has no such existing household member (it is the first in its household, or has no household at all) no cap
     * applies and the derived level is returned unchanged. The owner's transient household member count is populated
     * first (see {@link #populateHouseholdMemberCount}) so the derived level reflects the current household size.
     *
     * @param owner the owner whose capped membership level should be computed
     * @return the membership level, capped at one above the existing household maximum
     */
    private int cappedMembershipLevel(Owner owner) {
        populateHouseholdMemberCount(owner);
        int level = MembershipLevelResolver.deriveMembershipLevel(owner);
        Integer cap = householdMembershipLevelCap(owner);
        return cap == null ? level : Math.min(level, cap);
    }

    /**
     * Computes the ceiling the {@link #cappedMembershipLevel level-ceiling rule} imposes on a new owner: one above the
     * highest capped membership level among the owner's existing household members, or {@code null} when there is none.
     * An existing household member is an owner sharing this owner's non-null {@code householdId} whose id is smaller (so
     * it predates this owner); their levels are read with {@link #cappedMembershipLevel}, so each member's own ceiling
     * is respected in turn. An owner with no household ({@code null} householdId), no id yet, or no existing household
     * member has no cap.
     *
     * @param owner the owner whose household level cap should be computed
     * @return one above the existing household maximum level, or {@code null} when no cap applies
     */
    private Integer householdMembershipLevelCap(Owner owner) {
        String householdId = owner.getHouseholdId();
        Integer id = owner.getId();
        if (householdId == null || id == null) {
            return null;
        }
        Integer maxLevel = null;
        for (Owner member : this.clinicService.findAllOwners()) {
            Integer memberId = member.getId();
            if (memberId != null && memberId < id && householdId.equals(member.getHouseholdId())) {
                int memberLevel = cappedMembershipLevel(member);
                maxLevel = (maxLevel == null) ? memberLevel : Math.max(maxLevel, memberLevel);
            }
        }
        return maxLevel == null ? null : maxLevel + 1;
    }

    /**
     * Returns the owners that belong to the given owner's household: those sharing the same non-null
     * {@code householdId}. An owner with no household ({@code null} householdId) is a household of one and is returned on
     * its own. The lookup runs against {@link ClinicService#findAllOwners()} and, like the household member count it
     * backs (see {@link #populateHouseholdMemberCount}), does not exclude soft-deleted owners. When the owner has
     * already been saved it is itself among the members returned.
     *
     * @param owner the owner whose household members should be resolved
     * @return the members of the owner's household, never empty
     */
    private List<Owner> householdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return List.of(owner);
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
    }

    /**
     * Populates the owner's transient {@code householdMemberCount} with the number of owners that belong to its
     * household (see {@link #householdMembers}), i.e. those sharing the same non-null {@code householdId} (including the
     * owner itself). An owner with no household is counted as a household of one.
     *
     * @param owner the owner whose household size should be resolved
     */
    private void populateHouseholdMemberCount(Owner owner) {
        owner.setHouseholdMemberCount(householdMembers(owner).size());
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
