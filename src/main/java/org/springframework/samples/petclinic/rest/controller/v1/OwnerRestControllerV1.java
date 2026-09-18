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
import org.springframework.samples.petclinic.rest.controller.BulkSignupWarningAssigner;
import org.springframework.samples.petclinic.rest.controller.CityCapacityChecker;
import org.springframework.samples.petclinic.rest.controller.CustomerCodeAssigner;
import org.springframework.samples.petclinic.rest.controller.DailyRegistrationLimiter;
import org.springframework.samples.petclinic.rest.controller.HouseholdAssigner;
import org.springframework.samples.petclinic.rest.controller.HouseholdDuplicateChecker;
import org.springframework.samples.petclinic.rest.controller.HouseholdSizeAssigner;
import org.springframework.samples.petclinic.rest.controller.IdentityDuplicateChecker;
import org.springframework.samples.petclinic.rest.controller.MembershipLevelAssigner;
import org.springframework.samples.petclinic.rest.controller.NamesakeCounter;
import org.springframework.samples.petclinic.rest.controller.OwnerCreationAuditLogger;
import org.springframework.samples.petclinic.rest.controller.OwnerFieldsValidator;
import org.springframework.samples.petclinic.rest.controller.RegistrationDateAssigner;
import org.springframework.samples.petclinic.rest.controller.RegistrationDateValidator;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
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

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final OwnerFieldsValidator ownerFieldsValidator;

    private final RegistrationDateValidator registrationDateValidator;

    private final RegistrationDateAssigner registrationDateAssigner;

    private final CustomerCodeAssigner customerCodeAssigner;

    private final HouseholdDuplicateChecker householdDuplicateChecker;

    private final IdentityDuplicateChecker identityDuplicateChecker;

    private final HouseholdAssigner householdAssigner;

    private final HouseholdSizeAssigner householdSizeAssigner;

    private final NamesakeCounter namesakeCounter;

    private final MembershipLevelAssigner membershipLevelAssigner;

    private final CityCapacityChecker cityCapacityChecker;

    private final DailyRegistrationLimiter dailyRegistrationLimiter;

    private final BulkSignupWarningAssigner bulkSignupWarningAssigner;

    private final OwnerCreationAuditLogger ownerCreationAuditLogger;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerFieldsValidator ownerFieldsValidator,
                                 RegistrationDateValidator registrationDateValidator,
                                 RegistrationDateAssigner registrationDateAssigner,
                                 CustomerCodeAssigner customerCodeAssigner,
                                 HouseholdDuplicateChecker householdDuplicateChecker,
                                 IdentityDuplicateChecker identityDuplicateChecker,
                                 HouseholdAssigner householdAssigner,
                                 HouseholdSizeAssigner householdSizeAssigner,
                                 NamesakeCounter namesakeCounter,
                                 MembershipLevelAssigner membershipLevelAssigner,
                                 CityCapacityChecker cityCapacityChecker,
                                 DailyRegistrationLimiter dailyRegistrationLimiter,
                                 BulkSignupWarningAssigner bulkSignupWarningAssigner,
                                 OwnerCreationAuditLogger ownerCreationAuditLogger) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerFieldsValidator = ownerFieldsValidator;
        this.registrationDateValidator = registrationDateValidator;
        this.registrationDateAssigner = registrationDateAssigner;
        this.customerCodeAssigner = customerCodeAssigner;
        this.householdDuplicateChecker = householdDuplicateChecker;
        this.identityDuplicateChecker = identityDuplicateChecker;
        this.householdAssigner = householdAssigner;
        this.householdSizeAssigner = householdSizeAssigner;
        this.namesakeCounter = namesakeCounter;
        this.membershipLevelAssigner = membershipLevelAssigner;
        this.cityCapacityChecker = cityCapacityChecker;
        this.dailyRegistrationLimiter = dailyRegistrationLimiter;
        this.bulkSignupWarningAssigner = bulkSignupWarningAssigner;
        this.ownerCreationAuditLogger = ownerCreationAuditLogger;
    }

    @InitBinder("ownerFieldsDto")
    void initOwnerFieldsBinder(WebDataBinder binder) {
        binder.addValidators(ownerFieldsValidator, registrationDateValidator);
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
        this.registrationDateAssigner.assign(owner);
        if (this.cityCapacityChecker.isCityAtCapacity(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (this.dailyRegistrationLimiter.isDailyLimitReached(owner.getRegistrationDate())) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (sharesHousehold && this.householdDuplicateChecker.isDuplicate(owner)) {
            this.householdAssigner.assign(owner);
        }
        if (this.identityDuplicateChecker.isDuplicate(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        this.householdSizeAssigner.assign(owner);
        this.customerCodeAssigner.assign(owner);
        this.namesakeCounter.assign(owner);
        this.membershipLevelAssigner.assign(owner);
        this.bulkSignupWarningAssigner.assign(owner);
        this.clinicService.saveOwner(owner);
        this.ownerCreationAuditLogger.logCreated(owner);
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
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        currentOwner.setEmail(ownerFieldsDto.getEmail());
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
