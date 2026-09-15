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
import org.springframework.samples.petclinic.rest.CustomerCodeGenerator;
import org.springframework.samples.petclinic.rest.NamesakeCounter;
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
import org.springframework.samples.petclinic.rest.validation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.validation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.HouseholdDuplicateValidator;
import org.springframework.samples.petclinic.rest.validation.HouseholdIdGenerator;
import org.springframework.samples.petclinic.rest.validation.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.validation.OwnerFieldsValidator;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.BusinessDayResolver;
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

    private final HouseholdDuplicateValidator householdDuplicateValidator;

    private final HouseholdIdGenerator householdIdGenerator;

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    private final AddressNormalizer addressNormalizer;

    private final CustomerCodeGenerator customerCodeGenerator;

    private final NamesakeCounter namesakeCounter;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerFieldsValidator ownerFieldsValidator,
                                 HouseholdDuplicateValidator householdDuplicateValidator,
                                 HouseholdIdGenerator householdIdGenerator,
                                 TelephoneNormalizer telephoneNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 AddressNormalizer addressNormalizer,
                                 CustomerCodeGenerator customerCodeGenerator,
                                 NamesakeCounter namesakeCounter) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerFieldsValidator = ownerFieldsValidator;
        this.householdDuplicateValidator = householdDuplicateValidator;
        this.householdIdGenerator = householdIdGenerator;
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.addressNormalizer = addressNormalizer;
        this.customerCodeGenerator = customerCodeGenerator;
        this.namesakeCounter = namesakeCounter;
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
        applyHouseholdPolicy(owner, ownerFieldsDto, sameLastName);
        String normalizedTelephone = telephoneNormalizer.normalize(owner.getTelephone());
        if (!this.clinicService.findOwnersByTelephone(normalizedTelephone).isEmpty()) {
            throw new DuplicateTelephoneException(normalizedTelephone);
        }
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(emailNormalizer.normalize(owner.getEmail()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setRegistrationDate(BusinessDayResolver.toBusinessDay(owner.getRegistrationDate()));
        LocalDate registrationDate = owner.getRegistrationDate();
        if (clinicService.countOwnersByRegistrationDate(registrationDate)
                >= DailyOwnerLimitException.MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(registrationDate);
        }
        long cityOwnerCount = clinicService.countOwnersByCity(owner.getCity());
        if (cityOwnerCount >= CityOwnerLimitException.MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitException(owner.getCity());
        }
        owner.setCustomerCode(customerCodeGenerator.generate(
            owner.getCity(), owner.getLastName(), cityOwnerCount));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Applies the household rule to an owner about to be created. When the owner shares a
     * household (same last name and address, compared case-insensitively with collapsed
     * whitespace) with an existing owner, creation is rejected unless the request opted in via
     * {@code sharesHousehold}. When it opts in, the new owner and every existing household
     * member are stamped with the same stable {@code householdId}.
     *
     * @throws DuplicateHouseholdException if a conflicting owner exists and the caller did not opt in
     */
    private void applyHouseholdPolicy(Owner owner, OwnerFieldsDto ownerFieldsDto, Collection<Owner> sameLastName) {
        List<Owner> members = householdDuplicateValidator.findHouseholdMembers(owner, sameLastName);
        if (members.isEmpty()) {
            return;
        }
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
        }
        String householdId = householdIdGenerator.generate(owner);
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
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
