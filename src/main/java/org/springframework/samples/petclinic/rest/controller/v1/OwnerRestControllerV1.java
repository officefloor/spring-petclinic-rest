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
import java.util.Locale;
import java.util.OptionalInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.audit.AuditEventPublisher;
import org.springframework.samples.petclinic.audit.OwnerCreatedEvent;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.notify.WelcomeNotifier;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.rest.validation.CityCapacityExceededException;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.validation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.validation.MissingRequiredFieldsException;
import org.springframework.samples.petclinic.rest.validation.OwnerEmail;
import org.springframework.samples.petclinic.rest.validation.OwnerPostcode;
import org.springframework.samples.petclinic.rest.validation.OwnerRegistrationDate;
import org.springframework.samples.petclinic.rest.validation.OwnerTelephone;
import org.springframework.samples.petclinic.rest.validation.RequiredOwnerFields;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.BusinessDays;
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

    /** Maximum number of owners a single city may contain; creating another owner there is rejected. */
    static final int CITY_CAPACITY = 50;

    /** Owners already in a city at or above which (and below {@link #CITY_CAPACITY}) a new owner there gets 'capacityWarning'. */
    static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /** Maximum number of owners that may be created per (business-day adjusted) registration date; further creates are rejected. */
    static final int DAILY_OWNER_LIMIT = 100;

    /** Owners already created on the same registration date beyond which a new owner gets 'bulkSignupWarning'. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final AuditEventPublisher auditEventPublisher;

    private final WelcomeNotifier welcomeNotifier;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 AuditEventPublisher auditEventPublisher,
                                 WelcomeNotifier welcomeNotifier) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.auditEventPublisher = auditEventPublisher;
        this.welcomeNotifier = welcomeNotifier;
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
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        // A repeated create with an already-seen key returns the originally created owner.
        if (idempotencyKey != null) {
            Owner existing = this.clinicService.findOwnerByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), ownerLocation(existing), HttpStatus.OK);
            }
        }
        List<String> missingFields = RequiredOwnerFields.missingFields(ownerFieldsDto);
        if (!missingFields.isEmpty()) {
            throw new MissingRequiredFieldsException(missingFields);
        }
        OwnerPostcode.validate(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        OwnerEmail.validate(ownerFieldsDto.getEmail());
        OwnerRegistrationDate.validate(ownerFieldsDto.getRegistrationDate());
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(OwnerTelephone.normalize(ownerFieldsDto.getTelephone()));
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        applyAddress(owner, ownerFieldsDto);
        // Duplicate detection uses the identity key alone (telephone, email, soundex of the last name).
        owner.setHouseholdId(this.clinicService.householdIdOf(owner));
        boolean declaredHouseholdMember = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (this.clinicService.ownerIdentityKeyExists(owner)) {
            throw new DuplicateIdentityException(owner.getIdentityKey());
        }
        long ownersInCity = this.clinicService.countOwnersInCity(ownerFieldsDto.getCity());
        if (ownersInCity >= CITY_CAPACITY) {
            throw new CityCapacityExceededException(ownerFieldsDto.getCity(), CITY_CAPACITY);
        }
        owner.setCapacityWarning(ownersInCity >= CITY_CAPACITY_WARNING_THRESHOLD);
        LocalDate registrationDate = BusinessDays.rollForward(
            owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now());
        long ownersCreatedToday = this.clinicService.countOwnersRegisteredOn(registrationDate);
        if (ownersCreatedToday >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(DAILY_OWNER_LIMIT);
        }
        owner.setRegistrationDate(registrationDate);
        owner.setBulkSignupWarning(ownersCreatedToday > BULK_SIGNUP_THRESHOLD);
        // A declared household member is not a suspected duplicate.
        owner.setPossibleDuplicateOf(declaredHouseholdMember ? null : this.clinicService.findPossibleDuplicateOf(owner));
        owner.setIdempotencyKey(idempotencyKey);
        // The level cannot exceed one above the highest level among the existing household members.
        OptionalInt maxHouseholdLevel = this.clinicService.findHouseholdMembers(owner).stream()
            .mapToInt(ownerMapper::toMembershipLevel)
            .max();
        owner.setMembershipLevelCap(maxHouseholdLevel.isPresent() ? maxHouseholdLevel.getAsInt() + 1 : null);
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={}, memberId={}, region={}, fiscalYear={}, "
                + "registrationDate={}, membershipPoints={}, membershipLevel={}",
            owner.getId(), owner.getMemberId(), ownerMapper.toLocality(owner), ownerMapper.toFiscalYear(owner),
            owner.getRegistrationDate(), ownerMapper.toMembershipPoints(owner), ownerMapper.toMembershipLevel(owner));
        OwnerCreatedEvent created = this.auditEventPublisher.ownerCreated(owner, ownerMapper.toMembershipLevel(owner));
        this.welcomeNotifier.welcome(created);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), ownerLocation(owner), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String telephone = OwnerTelephone.normalize(ownerFieldsDto.getTelephone());
        OwnerPostcode.validate(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        OwnerEmail.validate(ownerFieldsDto.getEmail());
        OwnerRegistrationDate.validate(ownerFieldsDto.getRegistrationDate());
        applyAddress(currentOwner, ownerFieldsDto);
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(telephone);
        currentOwner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        currentOwner.setPostcode(ownerFieldsDto.getPostcode());
        if (ownerFieldsDto.getRegistrationDate() != null) {
            currentOwner.setRegistrationDate(BusinessDays.rollForward(ownerFieldsDto.getRegistrationDate()));
        }
        if (ownerFieldsDto.getBirthDate() != null) {
            currentOwner.setBirthDate(ownerFieldsDto.getBirthDate());
        }
        currentOwner.setTitle(ownerFieldsDto.getTitle() == null ? null : ownerFieldsDto.getTitle().getValue());
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

    /**
     * Stores the normalized structured address lines (absent when not supplied) and the composed
     * 'address': the structured lines when 'addressLine1' is present, otherwise the flat address.
     */
    private static void applyAddress(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        owner.setAddressLine1(AddressNormalizer.normalizeOptional(ownerFieldsDto.getAddressLine1()));
        owner.setAddressLine2(owner.getAddressLine1() == null ? null
            : AddressNormalizer.normalizeOptional(ownerFieldsDto.getAddressLine2()));
        owner.setAddress(AddressNormalizer.compose(
            ownerFieldsDto.getAddressLine1(), ownerFieldsDto.getAddressLine2(), ownerFieldsDto.getAddress()));
    }

    private static HttpHeaders ownerLocation(Owner owner) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return headers;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
