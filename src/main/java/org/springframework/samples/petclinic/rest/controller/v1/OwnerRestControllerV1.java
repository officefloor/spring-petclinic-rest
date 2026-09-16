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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.BusinessDay;
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
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.PostcodeValidator;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.OwnerAuditLogger;
import org.springframework.samples.petclinic.service.OwnerCreationIdempotencyStore;
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
     * Maximum number of owners allowed to be registered in a single city. A new owner
     * whose city already holds this many owners is rejected as a conflict.
     */
    private static final long MAX_OWNERS_PER_CITY = 50;

    /**
     * Maximum number of owners allowed to be registered in a single day. Once this many
     * owners already carry today's registration date, a further create is rejected as
     * too many requests.
     */
    private static final long MAX_OWNERS_PER_DAY = 100;

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    private final AddressNormalizer addressNormalizer;

    private final PostcodeValidator postcodeValidator;

    private final OwnerAuditLogger ownerAuditLogger;

    private final OwnerCreationIdempotencyStore idempotencyStore;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 TelephoneNormalizer telephoneNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 AddressNormalizer addressNormalizer,
                                 PostcodeValidator postcodeValidator,
                                 OwnerAuditLogger ownerAuditLogger,
                                 OwnerCreationIdempotencyStore idempotencyStore) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.addressNormalizer = addressNormalizer;
        this.postcodeValidator = postcodeValidator;
        this.ownerAuditLogger = ownerAuditLogger;
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

    /**
     * Normalize the owner's supplied address into its canonical stored form, preferring
     * the structured fields over the flat address. When a non-blank {@code addressLine1}
     * is supplied it wins: both structured lines are normalized and the stored
     * {@code address} becomes their composed value; otherwise the flat {@code address} is
     * normalized and used. The owner is left with a normalized structured/flat address.
     *
     * @param owner the owner whose address fields are normalized in place
     * @return {@code true} if the owner supplies an address in either form, {@code false}
     * when neither a structured nor a flat address was given
     */
    private boolean normalizeAddress(Owner owner) {
        String addressLine1 = addressNormalizer.normalize(owner.getAddressLine1());
        String addressLine2 = addressNormalizer.normalize(owner.getAddressLine2());
        String flatAddress = addressNormalizer.normalize(owner.getAddress());
        owner.setAddressLine2(addressNormalizer.isBlank(addressLine2) ? null : addressLine2);
        if (!addressNormalizer.isBlank(addressLine1)) {
            owner.setAddressLine1(addressLine1);
            owner.setAddress(addressNormalizer.compose(addressLine1, addressLine2));
            return true;
        }
        owner.setAddressLine1(null);
        if (!addressNormalizer.isBlank(flatAddress)) {
            owner.setAddress(flatAddress);
            return true;
        }
        return false;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        Owner replay = replayIdempotentCreate(idempotencyKey);
        if (replay != null) {
            return new ResponseEntity<>(ownerMapper.toOwnerDto(replay), HttpStatus.OK);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (!normalizeAddress(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<String> e164Telephone = telephoneNormalizer.toE164(owner.getTelephone());
        if (e164Telephone.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String telephone = e164Telephone.get();
        String email = emailNormalizer.normalize(owner.getEmail());
        if (email != null && (!emailNormalizer.isValid(email) || emailNormalizer.isDisposable(email))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (owner.getPostcode() != null && !postcodeValidator.isValid(owner.getPostcode(), owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate suppliedRegistrationDate = owner.getRegistrationDate();
        if (suppliedRegistrationDate != null && suppliedRegistrationDate.isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate registrationDate = BusinessDay.rollForward(
            suppliedRegistrationDate == null ? LocalDate.now() : suppliedRegistrationDate);
        owner.setRegistrationDate(registrationDate);
        if (clinicService.countOwnersByRegistrationDate(registrationDate) >= MAX_OWNERS_PER_DAY) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        owner.setTelephone(telephone);
        owner.setEmail(email);
        if (clinicService.countOwnersByCity(owner.getCity()) >= MAX_OWNERS_PER_CITY) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Reject a hard duplicate: an existing live owner sharing this owner's identity key
        // (its normalized telephone, email and phonetic last name). The email-domain blocklist
        // above is applied first, so a disposable-email duplicate is a 400, not a 409.
        if (clinicService.isDuplicateOwner(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // The household is keyed on (last name, postcode): a second owner sharing both joins
        // the existing household. Unless it declares 'sharesHousehold', it is recorded as a
        // possible duplicate of the sitting member (see saveOwner); a declared member joins as
        // a (non-suspected) household member instead. Either way its membership level is capped
        // relative to the household's existing members.
        owner.setDeclaredHouseholdMember(Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold()));
        this.clinicService.saveOwner(owner);
        this.ownerAuditLogger.logCreated(owner);
        if (idempotencyKey != null) {
            this.idempotencyStore.remember(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Look up the owner already created under the given {@code Idempotency-Key}, if any.
     * A repeated create carrying a key that has already produced an owner replays that
     * original owner instead of creating a duplicate.
     *
     * @param idempotencyKey the client-supplied idempotency key, or {@code null} when the
     *                       request carries no {@code Idempotency-Key} header
     * @return the originally created owner to replay, or {@code null} when the key is
     * absent, unseen, or its owner no longer exists
     */
    private Owner replayIdempotentCreate(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        return idempotencyStore.findOwnerId(idempotencyKey)
            .map(clinicService::findOwnerById)
            .orElse(null);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<String> e164Telephone = telephoneNormalizer.toE164(ownerFieldsDto.getTelephone());
        if (e164Telephone.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String email = emailNormalizer.normalize(ownerFieldsDto.getEmail());
        if (email != null && (!emailNormalizer.isValid(email) || emailNormalizer.isDisposable(email))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode != null && !postcodeValidator.isValid(postcode, ownerFieldsDto.getCity())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(e164Telephone.get());
        currentOwner.setEmail(email);
        currentOwner.setPostcode(postcode);
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
