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
import org.springframework.samples.petclinic.rest.validation.CityCapacityExceededException;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailException;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailValidator;
import org.springframework.samples.petclinic.rest.validation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.validation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.validation.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.validation.PostcodeValidator;
import org.springframework.samples.petclinic.rest.validation.OwnerFieldsValidator;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.IdempotencyKeyStore;
import org.springframework.samples.petclinic.service.OwnerAuditor;
import org.springframework.samples.petclinic.util.HouseholdIdGenerator;
import org.springframework.samples.petclinic.util.RegistrationDatePolicy;
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
     * Maximum number of owners a single city may hold. A create request whose city already contains
     * this many owners is rejected as a conflict.
     */
    static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Maximum number of owners that may be registered on a single day. A create request whose
     * registration date already holds this many owners is rejected with 429 Too Many Requests.
     */
    static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Number of owners that may be registered on the current business day before a create/read
     * response flags {@code bulkSignupWarning}. Once more than this many owners exist for the day,
     * the warning is raised.
     */
    static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final OwnerAuditor ownerAuditor;

    private final IdempotencyKeyStore idempotencyKeyStore;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerAuditor ownerAuditor,
                                 IdempotencyKeyStore idempotencyKeyStore) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerAuditor = ownerAuditor;
        this.idempotencyKeyStore = idempotencyKeyStore;
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
        return new ResponseEntity<>(toOwnerDtoWithBulkSignupWarning(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<OwnerDto> existing = idempotencyKeyStore.findOwnerId(idempotencyKey)
                .map(this.clinicService::findOwnerById)
                .map(this::toOwnerDtoWithBulkSignupWarning);
            if (existing.isPresent()) {
                return new ResponseEntity<>(existing.get(), HttpStatus.OK);
            }
        }
        resolveAddress(ownerFieldsDto);
        List<String> missingFields = OwnerFieldsValidator.findMissingFields(ownerFieldsDto);
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (!PostcodeValidator.isValidForCity(owner.getCity(), owner.getPostcode())) {
            throw new InvalidPostcodeException(owner.getPostcode(), owner.getCity());
        }
        if (this.clinicService.countOwnersByCity(owner.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityCapacityExceededException(owner.getCity());
        }
        if (RegistrationDatePolicy.isAfterServerDate(owner.getRegistrationDate())) {
            throw new FutureRegistrationDateException(owner.getRegistrationDate());
        }
        LocalDate registrationDate = RegistrationDatePolicy.effectiveDate(owner.getRegistrationDate());
        if (this.clinicService.countOwnersRegisteredOn(registrationDate) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
        owner.setTelephone(TelephoneNormalizer.normalize(owner.getTelephone()));
        owner.setEmail(EmailNormalizer.normalize(owner.getEmail()));
        if (DisposableEmailValidator.isDisposable(owner.getEmail())) {
            throw new DisposableEmailException(owner.getEmail());
        }
        // The household is keyed on (last name, postcode): the id is deterministic, so owners with
        // the same last name and postcode share it automatically.
        owner.setHouseholdId(HouseholdIdGenerator.generate(owner.getLastName(), owner.getPostcode()));
        boolean declaredHouseholdMember = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        owner.setDeclaredHouseholdMember(declaredHouseholdMember);
        // A second owner in an existing household is a duplicate, unless it declares shared
        // membership — in which case it is created as a known household member.
        if (!declaredHouseholdMember && this.clinicService.isHouseholdDuplicate(owner)) {
            throw new DuplicateIdentityException(owner.getIdentityKey());
        }
        this.clinicService.saveOwner(owner);
        this.ownerAuditor.ownerCreated(owner);
        if (idempotencyKey != null) {
            this.idempotencyKeyStore.remember(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = toOwnerDtoWithBulkSignupWarning(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Normalize the submitted address form and resolve the canonical stored {@code address}.
     * Whichever fields are supplied are normalized: the structured {@code addressLine1}/
     * {@code addressLine2} are kept (blank lines dropped to {@code null}), and {@code address}
     * is set to the composed structured address when {@code addressLine1} is present, otherwise
     * to the normalized flat {@code address}. Everything downstream reads the resolved
     * {@code address}, so it reflects the structured fields when present and the flat address
     * otherwise.
     */
    private void resolveAddress(OwnerFieldsDto ownerFieldsDto) {
        String line1 = AddressNormalizer.normalize(ownerFieldsDto.getAddressLine1());
        String line2 = AddressNormalizer.normalize(ownerFieldsDto.getAddressLine2());
        ownerFieldsDto.setAddressLine1(line1.isEmpty() ? null : line1);
        ownerFieldsDto.setAddressLine2(line2.isEmpty() ? null : line2);
        if (ownerFieldsDto.getAddressLine1() != null) {
            ownerFieldsDto.setAddress(
                AddressNormalizer.compose(ownerFieldsDto.getAddressLine1(), ownerFieldsDto.getAddressLine2()));
        } else {
            ownerFieldsDto.setAddress(AddressNormalizer.normalize(ownerFieldsDto.getAddress()));
        }
    }

    /**
     * Map an owner to its DTO and stamp {@code bulkSignupWarning}, which reflects whether more than
     * {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been registered on the current
     * business day.
     */
    private OwnerDto toOwnerDtoWithBulkSignupWarning(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        LocalDate today = RegistrationDatePolicy.effectiveDate(null);
        ownerDto.setBulkSignupWarning(
            this.clinicService.countOwnersRegisteredOn(today) > BULK_SIGNUP_WARNING_THRESHOLD);
        return ownerDto;
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
        currentOwner.setTelephone(TelephoneNormalizer.normalize(ownerFieldsDto.getTelephone()));
        currentOwner.setEmail(EmailNormalizer.normalize(ownerFieldsDto.getEmail()));
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
