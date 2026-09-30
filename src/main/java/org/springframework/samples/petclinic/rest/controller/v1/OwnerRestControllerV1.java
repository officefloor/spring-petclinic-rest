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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.audit.OwnerAuditEvents;
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
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.EmailDomainValidator;
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

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private static final Logger notifyLogger = LoggerFactory.getLogger("NOTIFY");

    /** The per-city soft capacity: at or above this owner count a city is over its soft capacity. */
    private static final int SOFT_CAPACITY = 40;

    /** The per-city hard limit: a create is rejected once a city already holds this many owners. */
    private static final int HARD_CAPACITY = 50;

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final OwnerAuditEvents ownerAuditEvents;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerAuditEvents ownerAuditEvents) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerAuditEvents = ownerAuditEvents;
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
        return new ResponseEntity<>(toOwnerDtoWithDerived(owner), HttpStatus.OK);
    }

    /**
     * Builds the read representation of a persisted owner, populating the derived fields
     * ({@code householdSize}, {@code bulkSignupWarning}, {@code capacityWarning}) that are not
     * stored on the entity.
     */
    private OwnerDto toOwnerDtoWithDerived(Owner owner) {
        owner.setHouseholdSize(this.clinicService.countOwnersByHouseholdId(owner.getHouseholdId()));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignup(owner.getRegistrationDate()));
        ownerDto.setCapacityWarning(isApproachingCapacity(owner.getCity()));
        ownerDto.setRiskFlag(isRisk(owner));
        return ownerDto;
    }

    /**
     * Whether the owner should be flagged for review via the read-only {@code riskFlag} field: true
     * when any of the following hold, otherwise false. The owner is a possible duplicate
     * ({@link Owner#getPossibleDuplicate()}); its email domain is disposable-adjacent
     * ({@link EmailDomainValidator#isDisposableAdjacent(String)}); or its city is over its soft
     * capacity (see {@link #isOverSoftCapacity(String)}).
     */
    private boolean isRisk(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || EmailDomainValidator.isDisposableAdjacent(owner.getEmail())
            || isOverSoftCapacity(owner.getCity());
    }

    /**
     * Whether {@code city} is over its soft capacity: it already holds {@link #SOFT_CAPACITY} or
     * more owners. The soft capacity sits below the per-city hard limit enforced at create time
     * (see {@link #addOwner}), so this stays true from the soft capacity right up to the full city.
     */
    private boolean isOverSoftCapacity(String city) {
        return this.clinicService.countOwnersByCity(city) >= SOFT_CAPACITY;
    }

    /**
     * Whether more than 80 owners have already been created on {@code registrationDate},
     * signalling a bulk-signup surge for the read-only {@code bulkSignupWarning} field.
     */
    private boolean isBulkSignup(LocalDate registrationDate) {
        return this.clinicService.countOwnersByRegistrationDate(registrationDate) > 80;
    }

    /**
     * Whether {@code city} already holds from the soft capacity up to one below the hard limit
     * (40 to 49 inclusive), signalling that it is approaching the per-city capacity limit of 50,
     * for the read-only {@code capacityWarning} field. The hard rejection at 50 (see
     * {@link #addOwner}) is unaffected.
     */
    private boolean isApproachingCapacity(String city) {
        int count = this.clinicService.countOwnersByCity(city);
        return count >= SOFT_CAPACITY && count < HARD_CAPACITY;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        // Idempotent replay: a repeat with an already-seen key returns the originally created
        // owner with 200 rather than creating (and possibly rejecting as) a duplicate.
        Integer existingId = this.clinicService.findOwnerIdByIdempotencyKey(idempotencyKey);
        if (existingId != null) {
            Owner existing = this.clinicService.findOwnerById(existingId);
            if (existing != null) {
                return new ResponseEntity<>(toOwnerDtoWithDerived(existing), HttpStatus.OK);
            }
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        LocalDate registrationDate = owner.resolveRegistrationDate();
        if (this.clinicService.countOwnersByRegistrationDate(registrationDate) >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        if (this.clinicService.countOwnersByCity(owner.getCity()) >= HARD_CAPACITY) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // The household id is deterministic: owners that share a (normalized lastName, postcode)
        // share it automatically. It feeds the membership-level cap and household sizing below.
        owner.setHouseholdId(this.clinicService.computeHouseholdId(owner));
        // A new owner joining an existing household (owners sharing a normalized lastName and
        // postcode) has its membershipLevel capped at one above the current maximum membershipLevel
        // among the existing household members. With no existing household member, no cap applies.
        owner.setMembershipLevelCap(this.clinicService.computeMembershipLevelCap(owner.getHouseholdId()));
        // Hard duplicate detection is consolidated into the owner's derived identity key (see
        // Owner#getIdentityKey): reject only when the new owner's whole key matches an existing one.
        // There is no separate household-duplicate 409: two owners sharing a lastName and postcode
        // but differing in telephone (or email) have distinct identity keys and are allowed.
        if (this.clinicService.existsOwnerWithIdentityKey(owner.getIdentityKey())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Soft-match: the identity key differs, yet the surname's soundex and the postcode match an
        // existing owner, flagged 'possibleDuplicate'. This is exactly the same lastName/postcode,
        // different telephone case that is no longer a hard duplicate.
        Integer possibleDuplicateOf = this.clinicService.findPossibleDuplicateOwnerId(owner);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        owner.setNamesakeCount(
            this.clinicService.countOwnersByName(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        this.clinicService.registerIdempotencyKey(idempotencyKey, owner.getId());
        OwnerDto ownerDto = toOwnerDtoWithDerived(owner);
        auditLogger.info(
            "owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel());
        // Immutable structured event, in addition to the human-readable line above. It carries the
        // owner's primary identifier (Owner#primaryIdentifier), i.e. its unified memberId.
        this.ownerAuditEvents.ownerCreated(
            owner.getId(), owner.primaryIdentifier(), ownerDto.getMembershipLevel());
        // Enqueue a welcome notification for the freshly created owner: the NOTIFY line
        // carries the owner id and its memberId so downstream consumers can address it.
        notifyLogger.info("welcome: owner id={} memberId={}", owner.getId(), owner.getMemberId());
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
        currentOwner.setAddress(AddressNormalizer.compose(
            currentOwner.getAddressLine1(), currentOwner.getAddressLine2(),
            ownerMapper.normalizeAddress(ownerFieldsDto.getAddress())));
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerMapper.normalizeTelephone(ownerFieldsDto.getTelephone()));
        currentOwner.setEmail(ownerFieldsDto.getEmail());
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
