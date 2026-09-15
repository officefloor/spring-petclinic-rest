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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.BulkSignupWarningEvaluator;
import org.springframework.samples.petclinic.rest.CustomerCodeGenerator;
import org.springframework.samples.petclinic.rest.NamesakeCounter;
import org.springframework.samples.petclinic.rest.OwnerAuditLogger;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.CityOwnerLimitException;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.validation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.HouseholdDuplicateValidator;
import org.springframework.samples.petclinic.rest.validation.HouseholdIdGenerator;
import org.springframework.samples.petclinic.rest.validation.IdentityDuplicateValidator;
import org.springframework.samples.petclinic.rest.validation.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.validation.OwnerFieldsValidator;
import org.springframework.samples.petclinic.rest.validation.PostcodeValidator;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.BusinessDayResolver;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.samples.petclinic.util.LocalityResolver;
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

    private final OwnerFieldsValidator ownerFieldsValidator;

    private final PostcodeValidator postcodeValidator;

    private final HouseholdDuplicateValidator householdDuplicateValidator;

    private final IdentityDuplicateValidator identityDuplicateValidator;

    private final HouseholdIdGenerator householdIdGenerator;

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    private final AddressNormalizer addressNormalizer;

    private final CustomerCodeGenerator customerCodeGenerator;

    private final NamesakeCounter namesakeCounter;

    private final BulkSignupWarningEvaluator bulkSignupWarningEvaluator;

    private final OwnerAuditLogger ownerAuditLogger;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerFieldsValidator ownerFieldsValidator,
                                 PostcodeValidator postcodeValidator,
                                 HouseholdDuplicateValidator householdDuplicateValidator,
                                 IdentityDuplicateValidator identityDuplicateValidator,
                                 HouseholdIdGenerator householdIdGenerator,
                                 TelephoneNormalizer telephoneNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 AddressNormalizer addressNormalizer,
                                 CustomerCodeGenerator customerCodeGenerator,
                                 NamesakeCounter namesakeCounter,
                                 BulkSignupWarningEvaluator bulkSignupWarningEvaluator,
                                 OwnerAuditLogger ownerAuditLogger) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerFieldsValidator = ownerFieldsValidator;
        this.postcodeValidator = postcodeValidator;
        this.householdDuplicateValidator = householdDuplicateValidator;
        this.identityDuplicateValidator = identityDuplicateValidator;
        this.householdIdGenerator = householdIdGenerator;
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.addressNormalizer = addressNormalizer;
        this.customerCodeGenerator = customerCodeGenerator;
        this.namesakeCounter = namesakeCounter;
        this.bulkSignupWarningEvaluator = bulkSignupWarningEvaluator;
        this.ownerAuditLogger = ownerAuditLogger;
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
        if (ownerFieldsDto != null) {
            ownerFieldsDto.setAddress(addressNormalizer.normalize(ownerFieldsDto.getAddress()));
        }
        List<String> missingFields = ownerFieldsValidator.findMissingOrBlankFields(ownerFieldsDto);
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        Collection<Owner> sameLastName =
            this.clinicService.findOwnersByLastNameIgnoreCase(owner.getLastName());
        owner.setNamesakeCount(namesakeCounter.count(owner, sameLastName));
        List<Owner> householdMembers = assignHousehold(owner, ownerFieldsDto, sameLastName);
        owner.setTelephone(telephoneNormalizer.normalize(owner.getTelephone()));
        owner.setEmail(emailNormalizer.normalize(owner.getEmail()));
        postcodeValidator.validate(owner.getPostcode(), owner.getCity());
        rejectIfDuplicateIdentity(owner);
        joinHousehold(owner, householdMembers);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setRegistrationDate(BusinessDayResolver.toBusinessDay(owner.getRegistrationDate()));
        LocalDate registrationDate = owner.getRegistrationDate();
        long ownersRegisteredToday = clinicService.countOwnersByRegistrationDate(registrationDate);
        if (ownersRegisteredToday >= DailyOwnerLimitException.MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(registrationDate);
        }
        owner.setBulkSignupWarning(bulkSignupWarningEvaluator.isBulkSignup(ownersRegisteredToday));
        long cityOwnerCount = clinicService.countOwnersByCity(owner.getCity());
        if (cityOwnerCount >= CityOwnerLimitException.MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitException(owner.getCity());
        }
        String region = LocalityResolver.localityOf(owner.getPostcode(), owner.getCity());
        owner.setCustomerCode(customerCodeGenerator.generate(
            region, owner.getTelephone(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        ownerAuditLogger.ownerCreated(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Assigns the household attributes of an owner about to be created, without touching any
     * existing owner. The new owner's household size after this create is recorded (the existing
     * members that share its last name and address, compared case-insensitively with collapsed
     * whitespace, plus the owner itself). When the owner shares a household with an existing one
     * and the request opted in via {@code sharesHousehold}, the new owner is stamped with the
     * household's stable {@code householdId}.
     *
     * <p>Sharing a household is no longer a rejection in itself: duplicate detection is decided
     * solely by the derived identity key in {@link #rejectIfDuplicateIdentity(Owner)}. Because the
     * telephone is part of that key, household members with different telephones are all allowed.
     *
     * @return the existing household members that the new owner joins (empty when it starts a new
     *         household or does not opt in)
     */
    private List<Owner> assignHousehold(Owner owner, OwnerFieldsDto ownerFieldsDto, Collection<Owner> sameLastName) {
        List<Owner> members = householdDuplicateValidator.findHouseholdMembers(owner, sameLastName);
        owner.setHouseholdSize(members.size() + 1);
        if (members.isEmpty() || !Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return List.of();
        }
        owner.setHouseholdId(householdIdGenerator.generate(owner));
        return members;
    }

    /**
     * Stamps every existing member the new owner joins with the owner's {@code householdId}, so
     * the whole household shares one identifier. Called only once the owner has cleared duplicate
     * detection, so no existing owner is mutated for a create that is going to be rejected.
     */
    private void joinHousehold(Owner owner, List<Owner> members) {
        String householdId = owner.getHouseholdId();
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
        }
    }

    /**
     * Rejects the owner when its whole derived {@link IdentityKey} already belongs to another
     * owner. A full-key match requires an equal telephone, so only owners with the same
     * (already normalized) telephone are compared.
     *
     * @throws DuplicateIdentityException if an existing owner shares the new owner's identity key
     */
    private void rejectIfDuplicateIdentity(Owner owner) {
        Collection<Owner> sameTelephone = this.clinicService.findOwnersByTelephone(owner.getTelephone());
        if (identityDuplicateValidator.isDuplicate(owner, sameTelephone)) {
            throw new DuplicateIdentityException(IdentityKey.of(owner));
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
