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
import org.springframework.samples.petclinic.rest.CityCapacityWarningEvaluator;
import org.springframework.samples.petclinic.rest.CustomerCodeGenerator;
import org.springframework.samples.petclinic.rest.NamesakeCounter;
import org.springframework.samples.petclinic.rest.OwnerAuditLogger;
import org.springframework.samples.petclinic.rest.OwnerCreationIdempotencyStore;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.rest.validation.AddressResolver;
import org.springframework.samples.petclinic.rest.validation.CityOwnerLimitException;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailDomainValidator;
import org.springframework.samples.petclinic.rest.validation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.HouseholdDuplicateValidator;
import org.springframework.samples.petclinic.rest.validation.HouseholdIdGenerator;
import org.springframework.samples.petclinic.rest.validation.IdentityDuplicateValidator;
import org.springframework.samples.petclinic.rest.validation.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.validation.OwnerFieldsValidator;
import org.springframework.samples.petclinic.rest.validation.PossibleDuplicateDetector;
import org.springframework.samples.petclinic.rest.validation.PostcodeValidator;
import org.springframework.samples.petclinic.rest.validation.RegistrationDateValidator;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.BusinessDayResolver;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.MembershipLevelCalculator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
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

    private final RegistrationDateValidator registrationDateValidator;

    private final HouseholdDuplicateValidator householdDuplicateValidator;

    private final IdentityDuplicateValidator identityDuplicateValidator;

    private final PossibleDuplicateDetector possibleDuplicateDetector;

    private final HouseholdIdGenerator householdIdGenerator;

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    private final DisposableEmailDomainValidator disposableEmailDomainValidator;

    private final AddressResolver addressResolver;

    private final CustomerCodeGenerator customerCodeGenerator;

    private final NamesakeCounter namesakeCounter;

    private final BulkSignupWarningEvaluator bulkSignupWarningEvaluator;

    private final CityCapacityWarningEvaluator cityCapacityWarningEvaluator;

    private final OwnerAuditLogger ownerAuditLogger;

    private final OwnerCreationIdempotencyStore idempotencyStore;

    private final HttpServletRequest request;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerFieldsValidator ownerFieldsValidator,
                                 PostcodeValidator postcodeValidator,
                                 RegistrationDateValidator registrationDateValidator,
                                 HouseholdDuplicateValidator householdDuplicateValidator,
                                 IdentityDuplicateValidator identityDuplicateValidator,
                                 PossibleDuplicateDetector possibleDuplicateDetector,
                                 HouseholdIdGenerator householdIdGenerator,
                                 TelephoneNormalizer telephoneNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 DisposableEmailDomainValidator disposableEmailDomainValidator,
                                 AddressResolver addressResolver,
                                 CustomerCodeGenerator customerCodeGenerator,
                                 NamesakeCounter namesakeCounter,
                                 BulkSignupWarningEvaluator bulkSignupWarningEvaluator,
                                 CityCapacityWarningEvaluator cityCapacityWarningEvaluator,
                                 OwnerAuditLogger ownerAuditLogger,
                                 OwnerCreationIdempotencyStore idempotencyStore,
                                 HttpServletRequest request) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerFieldsValidator = ownerFieldsValidator;
        this.postcodeValidator = postcodeValidator;
        this.registrationDateValidator = registrationDateValidator;
        this.householdDuplicateValidator = householdDuplicateValidator;
        this.identityDuplicateValidator = identityDuplicateValidator;
        this.possibleDuplicateDetector = possibleDuplicateDetector;
        this.householdIdGenerator = householdIdGenerator;
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.disposableEmailDomainValidator = disposableEmailDomainValidator;
        this.addressResolver = addressResolver;
        this.customerCodeGenerator = customerCodeGenerator;
        this.namesakeCounter = namesakeCounter;
        this.bulkSignupWarningEvaluator = bulkSignupWarningEvaluator;
        this.cityCapacityWarningEvaluator = cityCapacityWarningEvaluator;
        this.ownerAuditLogger = ownerAuditLogger;
        this.idempotencyStore = idempotencyStore;
        this.request = request;
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
        String idempotencyKey = idempotencyKey();
        ResponseEntity<OwnerDto> replay = replayIfDuplicateKey(idempotencyKey);
        if (replay != null) {
            return replay;
        }
        addressResolver.resolve(ownerFieldsDto);
        List<String> missingFields = ownerFieldsValidator.findMissingOrBlankFields(ownerFieldsDto);
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        Collection<Owner> sameLastName =
            activeOwners(this.clinicService.findOwnersByLastNameIgnoreCase(owner.getLastName()));
        owner.setNamesakeCount(namesakeCounter.count(owner, sameLastName));
        List<Owner> householdMembers = assignHousehold(owner, sameLastName);
        owner.setTelephone(telephoneNormalizer.normalize(owner.getTelephone()));
        owner.setEmail(emailNormalizer.normalize(owner.getEmail()));
        disposableEmailDomainValidator.validate(owner.getEmail());
        postcodeValidator.validate(owner.getPostcode(), owner.getCity());
        rejectIfDuplicateIdentity(owner);
        applyHouseholdRule(owner, ownerFieldsDto, householdMembers, sameLastName);
        registrationDateValidator.validate(owner.getRegistrationDate());
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
        owner.setCapacityWarning(cityCapacityWarningEvaluator.isApproachingCapacity(cityOwnerCount));
        owner.setCustomerCode(assignCustomerCode(owner));
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotencyStore.record(idempotencyKey, owner.getId());
        }
        ownerAuditLogger.ownerCreated(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Reads the request's {@code Idempotency-Key} header, treating a blank value as absent.
     *
     * @return the trimmed key, or {@code null} when the request carries no usable key
     */
    private String idempotencyKey() {
        String key = request.getHeader("Idempotency-Key");
        return StringUtils.hasText(key) ? key.trim() : null;
    }

    /**
     * Replays the owner originally created for an already-seen idempotency key, so a repeated
     * create returns that owner with 200 instead of storing a duplicate. Returns {@code null}
     * when there is no key or the key has not been seen, in which case the create proceeds
     * normally.
     */
    private ResponseEntity<OwnerDto> replayIfDuplicateKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        return idempotencyStore.find(idempotencyKey)
            .map(clinicService::findOwnerById)
            .map(ownerMapper::toOwnerDto)
            .map(ownerDto -> new ResponseEntity<>(ownerDto, HttpStatus.OK))
            .orElse(null);
    }

    /**
     * Assigns the household attributes of an owner about to be created, without touching any
     * existing owner. The {@code householdId} is derived deterministically from the owner's last
     * name and postcode, so every owner in the same household shares it automatically regardless of
     * creation order. The new owner's household size after this create is recorded: the existing
     * members that share its household (same last name and postcode) plus the owner itself.
     *
     * @return the existing members of the owner's household (empty when it starts a new household)
     */
    private List<Owner> assignHousehold(Owner owner, Collection<Owner> sameLastName) {
        owner.setHouseholdId(householdIdGenerator.generate(owner));
        List<Owner> members = householdDuplicateValidator.findHouseholdMembers(owner, sameLastName);
        owner.setHouseholdSize(members.size() + 1);
        return members;
    }

    /**
     * Computes the customer code for an owner about to be created, de-duplicated against the codes
     * already assigned to existing owners so that distinct owners always receive distinct codes.
     *
     * @return the owner's unique customer code
     */
    private String assignCustomerCode(Owner owner) {
        String region = LocalityResolver.localityOf(owner.getPostcode(), owner.getCity());
        String baseCode = customerCodeGenerator.generate(
            region, owner.getTelephone(), owner.getLastName());
        List<String> takenCodes = clinicService.findOwnersByCustomerCodeStartingWith(baseCode)
            .stream()
            .map(Owner::getCustomerCode)
            .toList();
        return customerCodeGenerator.deduplicate(baseCode, takenCodes);
    }

    /**
     * Applies the household-membership rule once the owner has cleared hard duplicate detection.
     * When the owner joins an existing household (same last name and postcode) its membership level
     * is capped to at most one above the highest level among the existing members. A member declared
     * with {@code sharesHousehold} is, being knowingly registered, not treated as a suspected
     * duplicate; an undeclared one still runs the ordinary possible-duplicate detection. When the
     * owner starts a new household no cap applies and possible-duplicate detection runs as usual.
     */
    private void applyHouseholdRule(Owner owner, OwnerFieldsDto ownerFieldsDto,
                                    List<Owner> householdMembers, Collection<Owner> sameLastName) {
        if (householdMembers.isEmpty()) {
            flagPossibleDuplicate(owner, sameLastName);
            return;
        }
        owner.setMembershipLevelCap(MembershipLevelCalculator.householdLevelCeiling(householdMembers));
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        } else {
            flagPossibleDuplicate(owner, sameLastName);
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
        Collection<Owner> sameTelephone =
            activeOwners(this.clinicService.findOwnersByTelephone(owner.getTelephone()));
        if (identityDuplicateValidator.isDuplicate(owner, sameTelephone)) {
            throw new DuplicateIdentityException(IdentityKey.of(owner));
        }
    }

    /**
     * Filters out soft-deleted owners, so the create endpoint's duplicate and identity checks
     * consider only owners that are still active. A soft-deleted owner keeps its row but is
     * treated as absent for the purpose of detecting duplicates of a newly created owner.
     */
    private List<Owner> activeOwners(Collection<Owner> owners) {
        return owners.stream().filter(candidate -> !candidate.isDeleted()).toList();
    }

    /**
     * Flags the owner as a soft (possible) duplicate when it is not a hard duplicate yet shares an
     * existing owner's last name and postcode with a different telephone. The matching owner's id
     * is recorded in {@code possibleDuplicateOf}; when nothing matches the owner is stamped as not
     * a possible duplicate. Called after {@link #rejectIfDuplicateIdentity(Owner)}, so any candidate
     * seen here is guaranteed not to be a hard duplicate.
     */
    private void flagPossibleDuplicate(Owner owner, Collection<Owner> sameLastName) {
        Owner match = possibleDuplicateDetector.findPossibleDuplicate(owner, sameLastName).orElse(null);
        owner.setPossibleDuplicate(match != null);
        owner.setPossibleDuplicateOf(match == null ? null : match.getId());
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
        owner.setDeleted(true);
        this.clinicService.saveOwner(owner);
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
