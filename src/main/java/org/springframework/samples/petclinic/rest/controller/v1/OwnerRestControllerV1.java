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
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
     * carrying the new owner's id, member id and registration date.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Dedicated notification logger. A single line is emitted here on each successful owner create,
     * enqueuing the new owner's welcome notification. The line carries the owner's id and its unified
     * {@code memberId}.
     */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Source of the {@code seq} carried by each {@link OwnerCreatedEvent}: a monotonically increasing
     * counter bumped once per successful create, so every emitted event carries a strictly larger
     * sequence number than the create before it.
     */
    private static final AtomicLong EVENT_SEQ = new AtomicLong();

    /**
     * Maximum number of owners a single city may hold. A create whose city already contains this many
     * owners is rejected with a {@code 409 Conflict} (see {@link #rejectCityAtCapacity}).
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * The number of owners a city must already hold before a create made for that city is flagged with
     * {@code capacityWarning}. Once the city holds at least this many owners (but still fewer than
     * {@link #MAX_OWNERS_PER_CITY}, so the create is not rejected), the new owner's {@code
     * capacityWarning} is {@code true}, signalling the city is approaching its capacity limit.
     */
    private static final int CAPACITY_WARNING_THRESHOLD = 40;

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

    /**
     * Request header carrying the client-supplied idempotency key for a create. When a create repeats
     * with a key already seen, the originally created owner is returned with {@code 200 OK} instead of
     * creating a duplicate (see {@link #addOwner}).
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per seen {@code Idempotency-Key}, the id of the owner that create originally produced,
     * so a repeated create carrying the same key replays that owner rather than creating a duplicate.
     */
    private final Map<String, Integer> idempotentCreates = new ConcurrentHashMap<>();

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
        String idempotencyKey = idempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = this.idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        normalizeAddress(ownerFieldsDto);
        rejectBlankOwnerFields(ownerFieldsDto);
        rejectInvalidPostcode(ownerFieldsDto);
        normalizeTelephone(ownerFieldsDto);
        normalizeEmail(ownerFieldsDto);
        String householdId = householdIdFor(ownerFieldsDto);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        rejectDuplicateIdentity(ownerFieldsDto);
        long ownersInCity = countOwnersInCity(ownerFieldsDto.getCity());
        rejectCityAtCapacity(ownerFieldsDto.getCity(), ownersInCity);
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
        owner.setCapacityWarning(ownersInCity >= CAPACITY_WARNING_THRESHOLD);
        owner.setHouseholdSize(countHouseholdMembers(owner.getHouseholdId()) + 1);
        owner.setMembershipLevel(cappedMembershipLevel(owner));
        markPossibleDuplicate(owner, sharesHousehold);
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            this.idempotentCreates.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        emitOwnerCreatedAudit(owner, ownerDto);
        enqueueWelcomeNotification(owner);
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
        normalizeAddress(ownerFieldsDto);
        currentOwner.setAddress(addressFor(ownerFieldsDto));
        currentOwner.setAddressLine1(ownerFieldsDto.getAddressLine1());
        currentOwner.setAddressLine2(ownerFieldsDto.getAddressLine2());
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
     * Emits the audit trail for a freshly created owner: the human-readable {@code AUDIT} line and the
     * structured {@link OwnerCreatedEvent}, in that order. Both carry the owner's primary identifier,
     * the unified {@code memberId} returned by {@link #primaryIdentifier}, so keeping the two emissions
     * together in one place lets the audit trail's view of the owner's identity change in a single spot.
     * The event's {@code seq} is bumped exactly once here, so every emitted event carries a strictly
     * larger sequence number than the create before it.
     *
     * @param owner    the freshly created and persisted owner, already stamped with its identity
     * @param ownerDto the owner's mapped DTO, the source of the derived {@code membershipLevel} carried
     *                 on the audit trail
     */
    private void emitOwnerCreatedAudit(Owner owner, OwnerDto ownerDto) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel());
        AUDIT.info(ownerCreatedEvent(owner, ownerDto).toJson());
    }

    /**
     * Builds the structured {@link OwnerCreatedEvent} published for a freshly created owner. The
     * event's payload is composed here in one place: its {@code seq} by bumping {@link #EVENT_SEQ}
     * exactly once, its primary identifier through {@link #primaryIdentifier}, and its
     * {@code membershipLevel} from the mapped DTO. Keeping the event's shape behind this single builder
     * lets the audit event's payload change in one spot, mirroring {@link #primaryIdentifier}'s single
     * accessor for the owner's identity.
     *
     * @param owner    the freshly created and persisted owner, already stamped with its identity
     * @param ownerDto the owner's mapped DTO, the source of the derived {@code membershipLevel} carried
     *                 on the event
     * @return the structured audit event to publish
     */
    private OwnerCreatedEvent ownerCreatedEvent(Owner owner, OwnerDto ownerDto) {
        OwnerDto.OwnerSegmentEnum ownerSegment = ownerDto.getOwnerSegment();
        return new OwnerCreatedEvent(EVENT_SEQ.incrementAndGet(),
            owner.getId(), primaryIdentifier(owner), ownerDto.getMembershipLevel(),
            ownerSegment == null ? null : ownerSegment.getValue());
    }

    /**
     * Enqueues the freshly created owner's welcome notification by emitting a single line to the
     * dedicated {@code NOTIFY} logger. The line carries the owner's id and its unified
     * {@code memberId} (routed through {@link #primaryIdentifier}), the two values a downstream
     * consumer needs to address the welcome to the new member.
     *
     * @param owner the freshly created and persisted owner, already stamped with its {@code memberId}
     */
    private void enqueueWelcomeNotification(Owner owner) {
        NOTIFY.info("welcome notification queued ownerId={} memberId={}",
            owner.getId(), primaryIdentifier(owner));
    }

    /**
     * The owner's primary identifier, as carried on the {@link OwnerCreatedEvent} audit event: the
     * unified {@code memberId}. Routing the audit event through this single accessor keeps the audit
     * trail's notion of the owner's identity in one place.
     *
     * @param owner the freshly created owner, already stamped with its {@code memberId}
     * @return the owner's primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Reads the optional {@code Idempotency-Key} header from the current request. The generated
     * {@code addOwner} signature carries only the request body, so the header is pulled from the
     * request bound to the current thread. Returns the trimmed key, or {@code null} when the header is
     * absent or blank (in which case the create proceeds normally, without idempotent replay).
     *
     * @return the non-blank idempotency key, or {@code null} when none was supplied
     */
    private String idempotencyKey() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        String key = attributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        return isBlank(key) ? null : key.trim();
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
        if (isBlank(ownerFieldsDto.getAddressLine1()) && isBlank(ownerFieldsDto.getAddress())) {
            missing.add("address");
        }
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
        if (isBlank(value)) {
            missing.add(fieldName);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes the submitted address on create to its canonical form (see
     * {@link AddressNormalizer#normalize}) and writes it back onto the request so it is the value
     * stored and returned, and the form every later address comparison (the required-field check,
     * household duplicate detection and the shared household id) is judged on. The structured fields
     * are preferred: {@code addressLine1} and the optional {@code addressLine2} are each normalized in
     * place, and the flat {@code address} is set to the {@linkplain #composedAddress composed} value
     * derived from them when a structured {@code addressLine1} is present, falling back to the
     * normalized flat {@code address} otherwise. Any absent field is left absent so
     * {@link #rejectBlankOwnerFields} still reports a wholly missing address; a value that is blank
     * after normalization likewise reduces to the empty string and is rejected there.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        String addressLine1 = AddressNormalizer.normalizeOrNull(ownerFieldsDto.getAddressLine1());
        String addressLine2 = AddressNormalizer.normalizeOrNull(ownerFieldsDto.getAddressLine2());
        String flatAddress = AddressNormalizer.normalizeOrNull(ownerFieldsDto.getAddress());
        ownerFieldsDto.setAddressLine1(addressLine1);
        ownerFieldsDto.setAddressLine2(addressLine2);
        ownerFieldsDto.setAddress(composedAddress(addressLine1, addressLine2, flatAddress));
    }

    /**
     * Composes the flat {@code address} an owner should carry from its (already normalized) address
     * fields, preferring the structured form. When a non-blank {@code addressLine1} is present the
     * composed address is {@code addressLine1}, with a single space and {@code addressLine2} appended
     * when {@code addressLine2} is present; otherwise the flat {@code address} is used unchanged. This
     * is the single value every later reader of the address (the required-field check, household
     * duplicate detection and the shared household id) is judged on.
     *
     * @param addressLine1 the normalized first address line, or {@code null}/blank when absent
     * @param addressLine2 the normalized second address line, or {@code null}/blank when absent
     * @param flatAddress  the normalized flat address, or {@code null}/blank when absent
     * @return the composed flat address, or {@code null} when no address form was supplied
     */
    private String composedAddress(String addressLine1, String addressLine2, String flatAddress) {
        if (isBlank(addressLine1)) {
            return flatAddress;
        }
        if (isBlank(addressLine2)) {
            return addressLine1;
        }
        return addressLine1 + " " + addressLine2;
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
     * ({@code sha256hex(normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName))}, see
     * {@link OwnerIdentity}), which replaces the previously separate telephone, email and household
     * checks. Two owners denote the same person when their whole identity keys are equal, so a create
     * is rejected as a {@code 409 Conflict} when another (non-deleted) owner already carries the same
     * key. Because the telephone is part of the key, two owners who share a last name and postcode but
     * carry different telephones have different keys and are both allowed (they are instead recorded as
     * a {@linkplain #findPossibleDuplicate possible duplicate}); an email or a household on its own no
     * longer makes a duplicate.
     *
     * @param ownerFieldsDto the submitted owner fields, with telephone and email already normalized
     *                       ({@link #normalizeTelephone}, {@link #normalizeEmail})
     * @throws DuplicateOwnerIdentityException if another owner already carries the same identity
     */
    private void rejectDuplicateIdentity(OwnerFieldsDto ownerFieldsDto) {
        String identityKey = OwnerIdentity.identityKey(
            ownerFieldsDto.getTelephone(), ownerFieldsDto.getEmail(), ownerFieldsDto.getLastName());
        boolean inUse = activeOwners().stream()
            .map(OwnerIdentity::identityKey)
            .anyMatch(identityKey::equals);
        if (inUse) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * The owners considered by duplicate detection: every persisted owner except those flagged
     * {@code deleted}. A soft-deleted owner is retained but must never make a later create look like a
     * duplicate, so both the hard-duplicate identity check ({@link #rejectDuplicateIdentity}) and the
     * soft {@linkplain #findPossibleDuplicate possible-duplicate} match are judged against these owners
     * alone. Only owners already persisted (before this create) are included.
     *
     * @return the non-deleted owners currently persisted
     */
    private List<Owner> activeOwners() {
        return this.clinicService.findAllOwners().stream()
            .filter(owner -> !owner.isDeleted())
            .toList();
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
        return householdMembers(householdId).size();
    }

    /**
     * Returns the existing owners (before this create) belonging to the given household, i.e. those
     * whose stored {@code householdId} equals the value derived for the owner being created. As with
     * {@link #countHouseholdMembers}, only owners already persisted are included, so the owner being
     * created is never among them.
     *
     * @param householdId the household identifier derived for the owner being created (see
     *                    {@link HouseholdNormalizer#toHouseholdId})
     * @return the existing owners already sharing that household (empty when {@code householdId} is
     *         {@code null})
     */
    private List<Owner> householdMembers(String householdId) {
        if (householdId == null) {
            return List.of();
        }
        return this.clinicService.findAllOwners().stream()
            .filter(member -> householdId.equals(member.getHouseholdId()))
            .toList();
    }

    /**
     * Caps a new owner's {@code membershipLevel} so it cannot exceed one above the current maximum
     * {@code membershipLevel} among their existing household members. The owner's own level is the
     * value {@linkplain OwnerMapper#membershipLevel(Owner) it would otherwise report}; it is capped at
     * {@code maxHouseholdLevel + 1}. When the household has no existing member no cap applies and the
     * owner's own level is returned unchanged.
     *
     * @param owner the owner being created, already stamped with its household id and size
     * @return the (possibly capped) membership level to stamp on the owner
     */
    private int cappedMembershipLevel(Owner owner) {
        int ownLevel = this.ownerMapper.membershipLevel(owner);
        OptionalInt maxHouseholdLevel = householdMembers(owner.getHouseholdId()).stream()
            .mapToInt(this.ownerMapper::membershipLevel)
            .max();
        return maxHouseholdLevel.isPresent()
            ? Math.min(ownLevel, maxHouseholdLevel.getAsInt() + 1)
            : ownLevel;
    }

    /**
     * Derives the address an owner should carry from its submitted fields. The update path stamps an
     * owner with an address taken from the request, so routing that through this single method keeps the
     * derivation of "the owner's address" in one place, mirroring {@link #householdIdFor} for the
     * household id.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @return the address to store on the owner
     */
    private String addressFor(OwnerFieldsDto ownerFieldsDto) {
        return ownerFieldsDto.getAddress();
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
     * Finds an existing owner that the owner being created soft-matches on, or {@code null} when there
     * is none. The create has already cleared the hard-duplicate identity check
     * ({@link #rejectDuplicateIdentity}), so its {@code identityKey} matches no existing owner; it is a
     * <em>possible</em> duplicate when it nevertheless shares an existing owner's {@code postcode} and
     * the {@linkplain Soundex#soundex Soundex} of its {@code lastName} while their identity keys still
     * differ. Because the telephone is part of the identity key, two owners with the same last name and
     * postcode but different telephones have differing keys and so soft-match here rather than colliding
     * as a hard duplicate. The oldest such owner (the earliest persisted) is returned so the flag points
     * at the original record; owners with no postcode never match, since a shared postcode is required.
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
        String identityKey = OwnerIdentity.identityKey(owner);
        String lastNameCode = Soundex.soundex(owner.getLastName());
        return activeOwners().stream()
            .filter(existing -> !identityKey.equals(OwnerIdentity.identityKey(existing)))
            .filter(existing -> lastNameCode.equals(Soundex.soundex(existing.getLastName())))
            .filter(existing -> postcode.equals(existing.getPostcode()))
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
     * Counts how many existing owners already live in the given city. The submitted city is compared
     * case-insensitively against every existing owner's stored city. The result drives both the hard
     * capacity rejection (see {@link #rejectCityAtCapacity}) and the {@code capacityWarning} flag.
     *
     * @param city the submitted city (non-blank here, {@link #rejectBlankOwnerFields} having already
     *             run)
     * @return the number of existing owners whose city matches the submitted city
     */
    private long countOwnersInCity(String city) {
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getCity)
            .filter(Objects::nonNull)
            .filter(city::equalsIgnoreCase)
            .count();
    }

    /**
     * Rejects creating an owner whose city already holds {@link #MAX_OWNERS_PER_CITY} or more owners.
     * A city that is already at (or over) capacity is reported as a {@code 409 Conflict}.
     *
     * @param city         the submitted city (used only to report the conflict)
     * @param ownersInCity the number of owners the city already holds (see {@link #countOwnersInCity})
     * @throws OwnerCityAtCapacityException if the city already contains the maximum number of owners
     */
    private void rejectCityAtCapacity(String city, long ownersInCity) {
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
