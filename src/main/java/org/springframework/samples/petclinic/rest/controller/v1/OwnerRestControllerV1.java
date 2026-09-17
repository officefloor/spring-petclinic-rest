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
import org.springframework.samples.petclinic.rest.assignment.CustomerCodeGenerator;
import org.springframework.samples.petclinic.rest.assignment.HouseholdIdGenerator;
import org.springframework.samples.petclinic.rest.assignment.HouseholdSizeCalculator;
import org.springframework.samples.petclinic.rest.assignment.MembershipLevelCapCalculator;
import org.springframework.samples.petclinic.rest.audit.OwnerAuditLogger;
import org.springframework.samples.petclinic.rest.idempotency.OwnerCreationIdempotencyStore;
import org.springframework.samples.petclinic.rest.signup.BulkSignupWarningEvaluator;
import org.springframework.samples.petclinic.rest.validation.AddressFormNormalizer;
import org.springframework.samples.petclinic.rest.validation.CityOwnerLimitValidator;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitValidator;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailDomainValidator;
import org.springframework.samples.petclinic.rest.validation.DuplicateOwnerValidator;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.OwnerRequestValidator;
import org.springframework.samples.petclinic.rest.validation.PossibleDuplicateDetector;
import org.springframework.samples.petclinic.rest.validation.PostcodeValidator;
import org.springframework.samples.petclinic.rest.validation.RegistrationDateValidator;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.StringUtils;

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

    private final OwnerRequestValidator ownerRequestValidator;

    private final PostcodeValidator postcodeValidator;

    private final DuplicateOwnerValidator duplicateOwnerValidator;

    private final PossibleDuplicateDetector possibleDuplicateDetector;

    private final CityOwnerLimitValidator cityOwnerLimitValidator;

    private final DailyOwnerLimitValidator dailyOwnerLimitValidator;

    private final RegistrationDateValidator registrationDateValidator;

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    private final DisposableEmailDomainValidator disposableEmailDomainValidator;

    private final AddressFormNormalizer addressFormNormalizer;

    private final CustomerCodeGenerator customerCodeGenerator;

    private final HouseholdIdGenerator householdIdGenerator;

    private final HouseholdSizeCalculator householdSizeCalculator;

    private final MembershipLevelCapCalculator membershipLevelCapCalculator;

    private final OwnerAuditLogger ownerAuditLogger;

    private final BulkSignupWarningEvaluator bulkSignupWarningEvaluator;

    private final OwnerCreationIdempotencyStore idempotencyStore;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerRequestValidator ownerRequestValidator,
                                 PostcodeValidator postcodeValidator,
                                 DuplicateOwnerValidator duplicateOwnerValidator,
                                 PossibleDuplicateDetector possibleDuplicateDetector,
                                 CityOwnerLimitValidator cityOwnerLimitValidator,
                                 DailyOwnerLimitValidator dailyOwnerLimitValidator,
                                 RegistrationDateValidator registrationDateValidator,
                                 TelephoneNormalizer telephoneNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 DisposableEmailDomainValidator disposableEmailDomainValidator,
                                 AddressFormNormalizer addressFormNormalizer,
                                 CustomerCodeGenerator customerCodeGenerator,
                                 HouseholdIdGenerator householdIdGenerator,
                                 HouseholdSizeCalculator householdSizeCalculator,
                                 MembershipLevelCapCalculator membershipLevelCapCalculator,
                                 OwnerAuditLogger ownerAuditLogger,
                                 BulkSignupWarningEvaluator bulkSignupWarningEvaluator,
                                 OwnerCreationIdempotencyStore idempotencyStore) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerRequestValidator = ownerRequestValidator;
        this.postcodeValidator = postcodeValidator;
        this.duplicateOwnerValidator = duplicateOwnerValidator;
        this.possibleDuplicateDetector = possibleDuplicateDetector;
        this.cityOwnerLimitValidator = cityOwnerLimitValidator;
        this.dailyOwnerLimitValidator = dailyOwnerLimitValidator;
        this.registrationDateValidator = registrationDateValidator;
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.disposableEmailDomainValidator = disposableEmailDomainValidator;
        this.addressFormNormalizer = addressFormNormalizer;
        this.customerCodeGenerator = customerCodeGenerator;
        this.householdIdGenerator = householdIdGenerator;
        this.householdSizeCalculator = householdSizeCalculator;
        this.membershipLevelCapCalculator = membershipLevelCapCalculator;
        this.ownerAuditLogger = ownerAuditLogger;
        this.bulkSignupWarningEvaluator = bulkSignupWarningEvaluator;
        this.idempotencyStore = idempotencyStore;
    }

    /**
     * Map an owner to its DTO and stamp the current bulk-signup warning onto it, so every
     * single-owner response reflects whether more than 80 owners have been created today.
     */
    private OwnerDto toOwnerDto(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(this.bulkSignupWarningEvaluator.isBulkSignupInEffect());
        return ownerDto;
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
        return new ResponseEntity<>(toOwnerDto(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        if (StringUtils.hasText(idempotencyKey)) {
            Optional<Owner> alreadyCreated = this.idempotencyStore.find(idempotencyKey)
                .map(this.clinicService::findOwnerById);
            if (alreadyCreated.isPresent()) {
                return new ResponseEntity<>(toOwnerDto(alreadyCreated.get()), HttpStatus.OK);
            }
        }
        Owner owner = createOwner(ownerFieldsDto);
        if (StringUtils.hasText(idempotencyKey)) {
            this.idempotencyStore.remember(idempotencyKey, owner.getId());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(toOwnerDto(owner), headers, HttpStatus.CREATED);
    }

    /**
     * Apply every create-time rule to the submitted fields and persist the resulting owner.
     */
    private Owner createOwner(OwnerFieldsDto ownerFieldsDto) {
        this.addressFormNormalizer.normalize(ownerFieldsDto);
        this.ownerRequestValidator.validate(ownerFieldsDto);
        this.postcodeValidator.validate(ownerFieldsDto);
        this.cityOwnerLimitValidator.validate(ownerFieldsDto);
        this.registrationDateValidator.validate(ownerFieldsDto.getRegistrationDate());
        this.dailyOwnerLimitValidator.validate(ownerFieldsDto.getRegistrationDate());
        ownerFieldsDto.setTelephone(this.telephoneNormalizer.normalize(ownerFieldsDto.getTelephone()));
        ownerFieldsDto.setEmail(this.emailNormalizer.normalize(ownerFieldsDto.getEmail()));
        this.disposableEmailDomainValidator.validate(ownerFieldsDto.getEmail());
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // The householdId is deterministic from (lastName, postcode), so every owner carries the
        // identifier of its household automatically.
        owner.setHouseholdId(
            this.householdIdGenerator.generate(owner.getLastName(), owner.getPostcode()));
        if (sharesHousehold) {
            // A declared household member is deliberately allowed and, being declared, is never a
            // suspected duplicate.
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        } else {
            this.duplicateOwnerValidator.validate(owner);
            Integer possibleDuplicateOf = this.possibleDuplicateDetector.findPossibleDuplicate(owner);
            owner.setPossibleDuplicate(possibleDuplicateOf != null);
            owner.setPossibleDuplicateOf(possibleDuplicateOf);
        }
        owner.setCustomerCode(
            this.customerCodeGenerator.generate(owner.getPostcode(), owner.getTelephone(),
                owner.getLastName()));
        owner.setNamesakeCount(
            (int) this.clinicService.countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(this.householdSizeCalculator.size(owner));
        owner.setMembershipLevelCap(this.membershipLevelCapCalculator.cap(owner));
        this.clinicService.saveOwner(owner);
        this.ownerAuditLogger.created(owner);
        return owner;
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
        currentOwner.setTitle(ownerFieldsDto.getTitle());
        currentOwner.setTelephone(this.telephoneNormalizer.normalize(ownerFieldsDto.getTelephone()));
        currentOwner.setEmail(this.emailNormalizer.normalize(ownerFieldsDto.getEmail()));
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
