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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityFullException;
import org.springframework.samples.petclinic.rest.advice.OwnerDailyLimitException;
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
import org.springframework.util.StringUtils;
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
     * Basic syntactic check for an email address: a non-empty local part, an '@', and a domain with
     * at least one dot and a two-or-more letter top-level label. Case is ignored here; accepted
     * addresses are lower-cased before being stored.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Maximum number of owners a single city may contain. A create that would take the owner's city
     * to more than this many owners - i.e. the city already holds this many - is rejected with a 409.
     */
    private static final int CITY_OWNER_LIMIT = 50;

    /**
     * Maximum number of owners that may be registered on a single day. A create that would take the
     * number of owners sharing a {@code registrationDate} beyond this many - i.e. that many owners
     * have already been created that day - is rejected with a 429.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

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
        ownerFieldsDto.setAddress(AddressNormalizer.normalize(ownerFieldsDto.getAddress()));
        validateRequiredFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(TelephoneNormalizer.normalize(owner.getTelephone()));
        owner.setEmail(normalizeEmail(owner.getEmail()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        if (!this.clinicService.findOwnerByTelephone(owner.getTelephone()).isEmpty()) {
            throw new DuplicateOwnerTelephoneException(owner.getTelephone());
        }
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())
            && isDuplicateHousehold(owner.getLastName(), owner.getAddress())) {
            throw new DuplicateOwnerHouseholdException(owner.getLastName(), owner.getAddress());
        }
        if (countOwnersInCity(owner.getCity()) >= CITY_OWNER_LIMIT) {
            throw new OwnerCityFullException(owner.getCity());
        }
        if (countOwnersRegisteredOn(owner.getRegistrationDate()) >= DAILY_OWNER_LIMIT) {
            throw new OwnerDailyLimitException(owner.getRegistrationDate());
        }
        owner.setCustomerCode(buildCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setHouseholdId(HouseholdNormalizer.householdId(owner.getLastName(), owner.getAddress()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Rejects an owner payload that is missing or blank in any required field. Collects the name of
     * every offending field and, if there is at least one, raises a {@link MissingOwnerFieldsException}
     * so the client receives a 400 whose {@code errors} array lists each missing field. The address is
     * checked in its {@link AddressNormalizer#normalize normalized} form, so a value that is blank only
     * after normalization is still rejected.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getAddress())) {
            missingFields.add("address");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
    }

    /**
     * Normalizes an optional email address for owner creation. Email is optional, so a missing or
     * blank value is left untouched. When a value is supplied it must be a syntactically valid
     * address; the accepted value is lower-cased before it is stored and returned.
     *
     * @param email the raw email value submitted by the client, or {@code null}
     * @return the lower-cased email, or the original blank/{@code null} value when none was supplied
     * @throws InvalidOwnerFieldsException if a non-blank value is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return email.toLowerCase();
    }

    /**
     * Builds the owner's customer code formatted {@code <CITY3>-<LAST3>-<NNNN>}, where CITY3 is the
     * upper-cased first three letters of the city, LAST3 is the upper-cased first three letters of the
     * last name and NNNN is a per-city 4-digit zero-padded sequence equal to one more than the number
     * of owners already in that city (e.g. {@code SYD-SMI-0007}).
     *
     * @param city the owner's city
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    private String buildCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Determines whether an existing owner already shares a household with the supplied last name and
     * address. Two owners belong to the same household when {@link HouseholdNormalizer#sameHousehold}
     * considers their last name and address a match.
     *
     * @param lastName the submitted owner's last name
     * @param address the submitted owner's address
     * @return {@code true} if another owner already has the same normalized last name and address
     */
    /**
     * Counts how many owners already exist that share the supplied first and last name, compared
     * case-insensitively. This reflects the number of namesakes present <em>before</em> the current
     * owner is created and is stored on the new owner as its {@code namesakeCount}.
     *
     * @param firstName the submitted owner's first name
     * @param lastName the submitted owner's last name
     * @return the number of existing owners with the same first and last name, ignoring case
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    private boolean isDuplicateHousehold(String lastName, String address) {
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> HouseholdNormalizer.sameHousehold(
                lastName, address, existing.getLastName(), existing.getAddress()));
    }

    /**
     * Counts how many owners already live in the supplied city, compared case-insensitively. Used to
     * enforce the per-city capacity limit when a new owner is created.
     *
     * @param city the submitted owner's city
     * @return the number of existing owners in that city, ignoring case
     */
    private long countOwnersInCity(String city) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
    }

    /**
     * Counts how many owners have already been created on the supplied registration date. Used to
     * enforce the per-day capacity limit when a new owner is created.
     *
     * @param registrationDate the registration date of the owner being created
     * @return the number of existing owners already registered on that date
     */
    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
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
        currentOwner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
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
