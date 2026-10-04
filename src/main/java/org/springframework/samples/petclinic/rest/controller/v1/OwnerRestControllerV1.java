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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.OwnerCreationRejectedException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.IdempotencyStore;
import org.springframework.samples.petclinic.util.BusinessDays;
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

    /** Dedicated audit logger: on a successful create an audit line records the owner id,
     * memberId and registrationDate. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Dedicated notification logger: on a successful create a welcome notification is enqueued,
     * recording the owner id and memberId. */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /** Serializes the structured {@link OwnerCreatedEvent} emitted on each create. */
    private static final ObjectMapper AUDIT_MAPPER = JsonMapper.builder().build();

    /** Monotonically increasing sequence number assigned to each successful create's audit event. */
    private static final AtomicLong AUDIT_EVENT_SEQ = new AtomicLong();

    /** Maximum number of owners permitted in a single city; the create is rejected at this count. */
    private static final int CITY_CAPACITY = 50;

    /** Once a city already holds at least this many owners (but fewer than {@link #CITY_CAPACITY}),
     * a new owner created for that city is flagged with {@code capacityWarning} to signal the city
     * is approaching its capacity limit. */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /** Maximum number of owners that may be created on a single day (by registrationDate); the
     * create is rejected once this many owners already carry today's registration date. */
    private static final int DAILY_REGISTRATION_LIMIT = 100;

    /** Once more than this many owners already carry a given day's registration date, a new owner
     * created for that day is flagged with {@code bulkSignupWarning} to signal a bulk-signup surge. */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /** HTTP header carrying the client-supplied idempotency key for a create. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final HttpServletRequest request;

    private final IdempotencyStore idempotencyStore;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 HttpServletRequest request,
                                 IdempotencyStore idempotencyStore) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.request = request;
        this.idempotencyStore = idempotencyStore;
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
        // Idempotent create: if this request carries an Idempotency-Key we have already created an
        // owner for, return that original owner with 200 OK instead of creating a duplicate.
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        boolean hasIdempotencyKey = idempotencyKey != null && !idempotencyKey.isBlank();
        if (hasIdempotencyKey) {
            Optional<Owner> existing = this.idempotencyStore.find(idempotencyKey)
                .map(this.clinicService::findOwnerById);
            if (existing.isPresent()) {
                return new ResponseEntity<>(ownerMapper.toOwnerDto(existing.get()), HttpStatus.OK);
            }
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        // The effective registration date (supplied, or the server date when omitted) must fall on
        // a business day: a weekend rolls forward to the next Monday. Everything derived from the
        // registration date — the stored value, the per-day limit below and the membership number —
        // uses this adjusted date.
        LocalDate effectiveDate = BusinessDays.effectiveRegistrationDate(owner.getRegistrationDate());
        owner.setRegistrationDate(effectiveDate);
        // Per-day capacity: once the day has reached its limit of newly created owners, reject
        // further creates for that day with 429 Too Many Requests.
        int ownersRegisteredToday = this.clinicService.countOwnersRegisteredOn(effectiveDate);
        if (ownersRegisteredToday >= DAILY_REGISTRATION_LIMIT) {
            throw new OwnerCreationRejectedException(HttpStatus.TOO_MANY_REQUESTS,
                "The daily owner registration limit has been reached; please try again later");
        }
        // Bulk-signup warning: flag this owner when more than 80 owners have already been created
        // for its registration date, signalling a bulk-signup surge.
        owner.setBulkSignupWarning(ownersRegisteredToday > BULK_SIGNUP_WARNING_THRESHOLD);
        // Per-city capacity: a city that already holds the maximum number of owners is full.
        int ownersInCity = this.clinicService.countOwnersInCity(owner.getCity());
        if (ownersInCity >= CITY_CAPACITY) {
            throw new OwnerCreationRejectedException(HttpStatus.CONFLICT,
                "The city has reached its maximum owner capacity");
        }
        // Capacity warning: flag this owner when its city already holds between the warning
        // threshold (40) and the hard limit (49), signalling the city is approaching capacity.
        owner.setCapacityWarning(ownersInCity >= CITY_CAPACITY_WARNING_THRESHOLD);
        // The household id is derived deterministically on the owner itself (see
        // Owner.getHouseholdId()) from its normalized last name and postcode, so owners sharing a
        // last name and postcode share it automatically — no linking step and no ordering to manage.
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // Exact-identity hard duplicate: reject with 409 when the new owner's WHOLE identityKey
        // (the SHA-256 of normalized telephone | lower-cased email-or-empty | soundex(lastName))
        // equals an existing owner's. A truly identical record is always rejected, independent of
        // sharesHousehold.
        if (this.clinicService.isIdentityKeyInUse(owner)) {
            throw new OwnerCreationRejectedException(HttpStatus.CONFLICT,
                "An owner with the same identity already exists");
        }
        // Household membership: another owner may already belong to this (last name, postcode)
        // household. A new member is admitted rather than rejected — its membership level is instead
        // capped relative to the household (see below). An undeclared member (sharesHousehold not
        // set) that is not an exact-identity duplicate is still allowed through, flagged as a
        // possible duplicate for review.
        boolean householdInUse = this.clinicService.isHouseholdInUse(owner);
        // Soft "possible duplicate" detection: an existing owner's last name has the same Soundex
        // code and the same postcode, while its identity key differs (so it is not a hard
        // duplicate). A declared household member (sharesHousehold on an in-use household) is an
        // intentional share, not a suspected duplicate, so it is not flagged; otherwise the create
        // still proceeds, flagged with possibleDuplicate=true and possibleDuplicateOf set to the
        // matching owner id.
        Integer possibleDuplicateOf = (householdInUse && sharesHousehold)
            ? null
            : this.clinicService.findPossibleDuplicateOwnerId(owner);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        owner.setNamesakeCount(
            this.clinicService.countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Membership-level ceiling: a new owner's membershipLevel may not exceed one above the
        // current maximum level among its existing household members. Derive the cap from those
        // members' effective (already-capped) levels; with no existing member the cap stays null
        // and the owner's level is used uncapped.
        Collection<Owner> householdMembers = this.clinicService.findHouseholdMembers(owner);
        if (!householdMembers.isEmpty()) {
            int maxHouseholdLevel = householdMembers.stream()
                .mapToInt(member -> ownerMapper.formatMembershipLevel(member))
                .max()
                .getAsInt();
            owner.setMembershipLevelCap(maxHouseholdLevel + 1);
        }
        this.clinicService.saveOwner(owner);
        // Compute the derived membership level once so the human-readable line and the structured
        // event below are guaranteed to report the identical value.
        int membershipLevel = ownerMapper.formatMembershipLevel(owner);
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
        // In addition to the human-readable audit line above, emit an immutable structured event.
        // This is the schema-version-2 event: it carries schemaVersion=2, the owner's current
        // primary identifier (the version-2 memberId) via Owner.getPrimaryIdentifier(), the owner
        // segment recomputed from the version-2 identity, and a sequence number that strictly
        // increases across creates.
        OwnerCreatedEvent event = new OwnerCreatedEvent(
            OwnerCreatedEvent.SCHEMA_VERSION, AUDIT_EVENT_SEQ.incrementAndGet(), owner.getId(),
            owner.getPrimaryIdentifier(), membershipLevel, ownerMapper.formatOwnerSegment(owner),
            OwnerCreatedEvent.EVENT_TYPE);
        AUDIT.info(AUDIT_MAPPER.writeValueAsString(event));
        // Enqueue a welcome notification for the newly created owner, recording the owner id and
        // memberId so the notification pipeline can address the member.
        NOTIFY.info("welcome notification enqueued for owner id={} memberId={}",
            owner.getId(), owner.getMemberId());
        if (hasIdempotencyKey) {
            this.idempotencyStore.record(idempotencyKey, owner.getId());
        }
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
        currentOwner.setAddressLine1(ownerMapper.normalizeAddress(ownerFieldsDto.getAddressLine1()));
        currentOwner.setAddressLine2(ownerMapper.normalizeAddress(ownerFieldsDto.getAddressLine2()));
        currentOwner.setAddress(ownerMapper.composeAddress(ownerFieldsDto));
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerMapper.normalizeTelephone(ownerFieldsDto.getTelephone()));
        currentOwner.setEmail(ownerMapper.normalizeEmail(ownerFieldsDto.getEmail()));
        currentOwner.setPostcode(ownerFieldsDto.getPostcode());
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
