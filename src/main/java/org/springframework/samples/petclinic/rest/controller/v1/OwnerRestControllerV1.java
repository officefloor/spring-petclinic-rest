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
import org.springframework.samples.petclinic.model.BusinessDayAdjuster;
import org.springframework.samples.petclinic.model.RegionResolver;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.audit.OwnerAuditLogger;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.rest.idempotency.IdempotentOwnerCreationStore;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.CityOwnerCapacityValidator;
import org.springframework.samples.petclinic.rest.validation.DailyRegistrationCapacityValidator;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailDomainValidator;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.HouseholdDuplicateValidator;
import org.springframework.samples.petclinic.rest.validation.HouseholdKey;
import org.springframework.samples.petclinic.rest.validation.HouseholdMembershipLevelCap;
import org.springframework.samples.petclinic.rest.validation.HouseholdSizeCounter;
import org.springframework.samples.petclinic.rest.validation.MemberIdDeduplicator;
import org.springframework.samples.petclinic.rest.validation.MemberIdGenerator;
import org.springframework.samples.petclinic.rest.validation.OwnerFieldsValidator;
import org.springframework.samples.petclinic.rest.validation.NamesakeCounter;
import org.springframework.samples.petclinic.rest.validation.PossibleDuplicateOwnerDetector;
import org.springframework.samples.petclinic.rest.validation.PostcodeValidator;
import org.springframework.samples.petclinic.rest.validation.RegistrationDateValidator;
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

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final HouseholdDuplicateValidator householdDuplicateValidator;

    private final PossibleDuplicateOwnerDetector possibleDuplicateOwnerDetector;

    private final NamesakeCounter namesakeCounter;

    private final HouseholdSizeCounter householdSizeCounter;

    private final HouseholdMembershipLevelCap householdMembershipLevelCap;

    private final CityOwnerCapacityValidator cityOwnerCapacityValidator;

    private final DailyRegistrationCapacityValidator dailyRegistrationCapacityValidator;

    private final OwnerAuditLogger ownerAuditLogger;

    private final MemberIdDeduplicator memberIdDeduplicator;

    private final IdempotentOwnerCreationStore idempotentOwnerCreationStore;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 HouseholdDuplicateValidator householdDuplicateValidator,
                                 PossibleDuplicateOwnerDetector possibleDuplicateOwnerDetector,
                                 NamesakeCounter namesakeCounter,
                                 HouseholdSizeCounter householdSizeCounter,
                                 HouseholdMembershipLevelCap householdMembershipLevelCap,
                                 CityOwnerCapacityValidator cityOwnerCapacityValidator,
                                 DailyRegistrationCapacityValidator dailyRegistrationCapacityValidator,
                                 OwnerAuditLogger ownerAuditLogger,
                                 MemberIdDeduplicator memberIdDeduplicator,
                                 IdempotentOwnerCreationStore idempotentOwnerCreationStore) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.householdDuplicateValidator = householdDuplicateValidator;
        this.possibleDuplicateOwnerDetector = possibleDuplicateOwnerDetector;
        this.namesakeCounter = namesakeCounter;
        this.householdSizeCounter = householdSizeCounter;
        this.householdMembershipLevelCap = householdMembershipLevelCap;
        this.cityOwnerCapacityValidator = cityOwnerCapacityValidator;
        this.dailyRegistrationCapacityValidator = dailyRegistrationCapacityValidator;
        this.ownerAuditLogger = ownerAuditLogger;
        this.memberIdDeduplicator = memberIdDeduplicator;
        this.idempotentOwnerCreationStore = idempotentOwnerCreationStore;
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
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        Optional<Owner> replay = replayForIdempotencyKey(idempotencyKey);
        if (replay.isPresent()) {
            return new ResponseEntity<>(ownerMapper.toOwnerDto(replay.get()), HttpStatus.OK);
        }
        Owner owner = createOwner(ownerFieldsDto);
        rememberForIdempotencyKey(idempotencyKey, owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /** Validate, derive and persist a brand-new owner from the submitted fields. */
    private Owner createOwner(OwnerFieldsDto ownerFieldsDto) {
        normalizeAddress(ownerFieldsDto);
        OwnerFieldsValidator.validateRequiredFields(ownerFieldsDto);
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(TelephoneNormalizer.normalize(owner.getTelephone()));
        owner.setEmail(EmailNormalizer.normalize(owner.getEmail()));
        DisposableEmailDomainValidator.validate(owner.getEmail());
        PostcodeValidator.validate(owner.getCity(), owner.getPostcode());
        RegistrationDateValidator.validateNotFuture(owner.getRegistrationDate());
        LocalDate effectiveDate = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(BusinessDayAdjuster.toBusinessDay(effectiveDate));
        this.dailyRegistrationCapacityValidator.validateHasCapacity(owner.getRegistrationDate());
        owner.setHouseholdId(HouseholdKey.idFor(owner.getLastName(), owner.getPostcode()));
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            // Reject only a true duplicate (an owner identical to one already on file); a distinct
            // new household member is allowed but flagged as a possible duplicate. A declared member
            // bypasses this block, and is not a suspected duplicate.
            this.householdDuplicateValidator.rejectIfDuplicate(owner);
            this.possibleDuplicateOwnerDetector.findPossibleDuplicateOf(owner)
                .ifPresent(owner::setPossibleDuplicateOf);
        }
        this.cityOwnerCapacityValidator.validateHasCapacity(owner.getCity());
        owner.setNamesakeCount(this.namesakeCounter.count(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(this.householdSizeCounter.count(owner.getHouseholdId()));
        owner.setMembershipLevelCap(this.householdMembershipLevelCap.ceilingFor(owner.getHouseholdId()));
        String region = RegionResolver.regionFor(owner.getPostcode(), owner.getCity());
        String memberId = MemberIdGenerator.generate(region, owner.getTelephone(), owner.getLastName(),
            owner.getRegistrationDate());
        owner.setMemberId(this.memberIdDeduplicator.deduplicate(memberId));
        this.clinicService.saveOwner(owner);
        this.ownerAuditLogger.logCreated(owner);
        return owner;
    }

    /** The owner originally created for this idempotency key, if the key has already been seen. */
    private Optional<Owner> replayForIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return this.idempotentOwnerCreationStore.findOwnerId(idempotencyKey)
            .map(this.clinicService::findOwnerById);
    }

    /** Remember the created owner so a repeat with the same idempotency key replays it. */
    private void rememberForIdempotencyKey(String idempotencyKey, Owner owner) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            this.idempotentOwnerCreationStore.remember(idempotencyKey, owner.getId());
        }
    }

    /**
     * Normalize the submitted address fields and resolve the effective {@code address}. The
     * structured lines are normalized in place; when a non-blank {@code addressLine1} is supplied it
     * takes precedence and {@code address} is composed from the normalized lines, otherwise the flat
     * {@code address} input is normalized and kept for backward compatibility.
     */
    private void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        String line1 = AddressNormalizer.normalize(ownerFieldsDto.getAddressLine1());
        String line2 = AddressNormalizer.normalize(ownerFieldsDto.getAddressLine2());
        ownerFieldsDto.setAddressLine1(line1);
        ownerFieldsDto.setAddressLine2(line2);
        if (line1 != null && !line1.isBlank()) {
            ownerFieldsDto.setAddress(AddressNormalizer.compose(line1, line2));
        } else {
            ownerFieldsDto.setAddress(AddressNormalizer.normalize(ownerFieldsDto.getAddress()));
        }
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
