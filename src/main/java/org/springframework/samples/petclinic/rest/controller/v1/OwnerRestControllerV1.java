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
import java.util.Objects;

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
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        if (isDuplicateOwner(owner) || isDuplicateEmail(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        if (hasReachedDailyRegistrationLimit(owner.getRegistrationDate())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (hasReachedCityCapacity(owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setNamesakeCount(countNamesakes(owner.getLastName()));
        owner.setSharesHousehold(sharesHousehold(owner));
        owner.setMembershipNumber(this.clinicService.findAllOwners().size() + 1);
        owner.setMembershipTier(determineMembershipTier(owner.getMembershipNumber()));
        owner.setCity(normalizeCity(owner.getCity()));
        owner.setCustomerCode(generateCustomerCode(owner.getCity()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * The maximum number of owners that may be registered on any single day.
     */
    private static final long MAX_OWNERS_PER_DAY = 20;

    /**
     * Determines whether the number of owners already registered on the given date has reached the
     * daily registration limit. When 20 owners share the same registration date, no further owner may
     * be created for that day.
     */
    private boolean hasReachedDailyRegistrationLimit(LocalDate registrationDate) {
        long ownersRegisteredOnDate = this.clinicService.findAllOwners().stream()
            .filter(existing -> Objects.equals(existing.getRegistrationDate(), registrationDate))
            .count();
        return ownersRegisteredOnDate >= MAX_OWNERS_PER_DAY;
    }

    /**
     * The maximum number of owners that may share a single city.
     */
    private static final long MAX_OWNERS_PER_CITY = 8;

    /**
     * Determines whether the given city has already reached its capacity of owners. When a city
     * already contains 8 owners, no further owner may be created in that city. Cities are matched
     * ignoring letter case.
     */
    private boolean hasReachedCityCapacity(String city) {
        if (city == null) {
            return false;
        }
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
        return ownersInCity >= MAX_OWNERS_PER_CITY;
    }

    /**
     * Counts how many existing owners share the given last name at the moment a new owner is
     * created. The newly created owner itself is not yet persisted and so is never included in
     * the count. Last names are compared exactly.
     */
    private int countNamesakes(String lastName) {
        if (lastName == null) {
            return 0;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> Objects.equals(existing.getLastName(), lastName))
            .count();
    }

    /**
     * Determines whether the given owner shares a household with an owner that already exists,
     * meaning another owner already has the same address and city. Addresses and cities are
     * compared ignoring letter case and any surrounding or repeated whitespace. The newly created
     * owner itself is not yet persisted and so is never matched against.
     */
    private boolean sharesHousehold(Owner owner) {
        String address = normalize(owner.getAddress());
        String city = normalize(owner.getCity());
        if (address == null || city == null) {
            return false;
        }
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(normalize(existing.getAddress()), address)
                && Objects.equals(normalize(existing.getCity()), city));
    }

    /**
     * The number of owners that receive the founding membership tier. The first 100 owners ever
     * created are 'FOUNDING'; every owner created thereafter is 'STANDARD'.
     */
    private static final int FOUNDING_TIER_LIMIT = 100;

    /**
     * Determines the membership tier for a newly created owner based on its sequential membership
     * number. Owners whose membership number is within the first {@value #FOUNDING_TIER_LIMIT} are
     * 'FOUNDING'; all later owners are 'STANDARD'.
     */
    private String determineMembershipTier(int membershipNumber) {
        return membershipNumber <= FOUNDING_TIER_LIMIT ? "FOUNDING" : "STANDARD";
    }

    /**
     * Generates the customer code for a newly created owner. The code is formatted as
     * '&lt;UPPERCASE_CITY&gt;-&lt;NNNN&gt;' where NNNN is one more than the number of owners that
     * already exist in that city, zero-padded to four digits (for example 'LONDON-0007').
     * Cities are matched ignoring letter case.
     */
    private String generateCustomerCode(String city) {
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
        return String.format("%s-%04d", city.toUpperCase(Locale.ROOT), ownersInCity + 1);
    }

    /**
     * Normalizes the city of a newly created owner. If an owner already exists in the same
     * city (matched ignoring letter case), that existing owner's exact spelling of the city is
     * reused so that all owners in a city share one canonical spelling. Otherwise the supplied
     * city is title-cased, capitalizing the first letter of each whitespace-separated word and
     * lowercasing the rest, so for example "new york" becomes "New York".
     */
    private String normalizeCity(String city) {
        if (city == null) {
            return null;
        }
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getCity)
            .filter(existing -> existing != null && existing.equalsIgnoreCase(city))
            .findFirst()
            .orElseGet(() -> toTitleCase(city));
    }

    /**
     * Title-cases the given value by capitalizing the first letter of each whitespace-separated
     * word and lowercasing the remaining letters, preserving the original whitespace: for example
     * "new  YORK" becomes "New  York".
     */
    private static String toTitleCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toTitleCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    /**
     * Determines whether an owner already exists that uses the same telephone number
     * as the given one. Telephone numbers must be unique across all owners. The
     * comparison ignores letter case and any surrounding or repeated whitespace.
     */
    private boolean isDuplicateOwner(Owner owner) {
        String telephone = normalizeTelephone(owner.getTelephone());
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(normalizeTelephone(existing.getTelephone()), telephone));
    }

    /**
     * Normalizes a telephone number for storage and comparison by stripping spaces, dashes
     * and parentheses so that only digits remain: for example "(613) 555-0100" becomes
     * "6135550100". This lets differently punctuated forms of the same number be treated as
     * equal for the uniqueness check and stored in a single canonical form.
     */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("[\\s()-]", "");
    }

    /**
     * Determines whether another owner already uses the same email address as the given one.
     * Email addresses, when supplied, must be unique across all owners. An owner without an
     * email address is never considered a duplicate. The comparison ignores letter case and
     * any surrounding or repeated whitespace.
     */
    private boolean isDuplicateEmail(Owner owner) {
        String email = normalize(owner.getEmail());
        if (email == null || email.isEmpty()) {
            return false;
        }
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(normalize(existing.getEmail()), email));
    }

    /**
     * Normalizes a user-supplied value for duplicate detection so that comparisons ignore
     * letter case and any surrounding or repeated whitespace: for example "  john   smith "
     * and "John Smith" normalize to the same key and so count as the same person.
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
        currentOwner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
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
