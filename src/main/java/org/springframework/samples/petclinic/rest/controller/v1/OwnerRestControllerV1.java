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
        return new ResponseEntity<>(toOwnerDtoWithDerived(owner), HttpStatus.OK);
    }

    /**
     * Builds the read representation of a persisted owner, populating the derived fields
     * ({@code householdSize}, {@code bulkSignupWarning}) that are not stored on the entity.
     */
    private OwnerDto toOwnerDtoWithDerived(Owner owner) {
        owner.setHouseholdSize(this.clinicService.countOwnersByHouseholdId(owner.getHouseholdId()));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignup(owner.getRegistrationDate()));
        return ownerDto;
    }

    /**
     * Whether more than 80 owners have already been created on {@code registrationDate},
     * signalling a bulk-signup surge for the read-only {@code bulkSignupWarning} field.
     */
    private boolean isBulkSignup(LocalDate registrationDate) {
        return this.clinicService.countOwnersByRegistrationDate(registrationDate) > 80;
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
        if (this.clinicService.countOwnersByCity(owner.getCity()) >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // The household id is deterministic: owners that share a (normalized lastName, postcode)
        // share it automatically. It feeds the identity key, the membership-level cap and the
        // possible-duplicate suppression below.
        owner.setHouseholdId(this.clinicService.computeHouseholdId(owner));
        boolean householdExists = this.clinicService.existsOwnerInHousehold(owner.getHouseholdId());
        // A new owner joining an existing household (owners sharing a normalized lastName and
        // postcode) has its membershipLevel capped at one above the current maximum membershipLevel
        // among the existing household members. With no existing household member, no cap applies.
        owner.setMembershipLevelCap(this.clinicService.computeMembershipLevelCap(owner.getHouseholdId()));
        // Hard duplicate detection is consolidated into the owner's derived identity key (see
        // Owner#getIdentityKey): reject only when the new owner's whole key matches an existing one.
        if (this.clinicService.existsOwnerWithIdentityKey(owner.getIdentityKey())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Soft-match: shares an existing owner's lastName and postcode with a different telephone,
        // flagged 'possibleDuplicate'. A declared household member is not a suspected duplicate, so
        // the flag is suppressed when this owner joins an existing household.
        Integer possibleDuplicateOf = householdExists ? null
            : this.clinicService.findPossibleDuplicateOwnerId(owner);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        owner.setNamesakeCount(
            this.clinicService.countOwnersByName(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        this.clinicService.registerIdempotencyKey(idempotencyKey, owner.getId());
        OwnerDto ownerDto = toOwnerDtoWithDerived(owner);
        auditLogger.info(
            "owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
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
