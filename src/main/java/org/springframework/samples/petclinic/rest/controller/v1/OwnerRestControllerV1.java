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
        if (ownerAlreadyExists(owner) || emailAlreadyExists(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCity(resolveCity(owner.getCity()));
        owner.setMembershipNumber(this.clinicService.findAllOwners().size() + 1);
        owner.setCustomerCode(nextCustomerCode(owner.getCity()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Resolves the city to store for a newly created owner. If another owner is already stored in
     * the same city (compared ignoring letter case), that existing owner's exact spelling of the
     * city name is reused so that a single city is always represented consistently. Otherwise the
     * supplied city is title-cased (see {@link #titleCase(String)}) before being stored.
     *
     * @param city the raw city supplied for the owner being created, may be {@code null}
     * @return the city to store, or {@code null} if {@code city} is {@code null}
     */
    private String resolveCity(String city) {
        if (city == null) {
            return null;
        }
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getCity)
            .filter(existing -> existing != null && existing.equalsIgnoreCase(city))
            .findFirst()
            .orElseGet(() -> titleCase(city));
    }

    /**
     * Title-cases a value so that the first letter of every whitespace-separated word is
     * upper-cased and every other letter is lower-cased, preserving the original whitespace. For
     * example {@code "new  york"} becomes {@code "New  York"} and {@code "LONDON"} becomes
     * {@code "London"}.
     *
     * @param value the raw value, may be {@code null}
     * @return the title-cased value, or {@code null} if {@code value} is {@code null}
     */
    private static String titleCase(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(value.length());
        boolean startOfWord = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                startOfWord = true;
                result.append(c);
            } else {
                result.append(startOfWord ? Character.toUpperCase(c) : Character.toLowerCase(c));
                startOfWord = false;
            }
        }
        return result.toString();
    }

    /**
     * Builds the customer code for a newly created owner in the given city, formatted as
     * {@code <UPPERCASE_CITY>-<NNNN>} where {@code NNNN} is one more than the number of owners
     * already stored in that city, zero-padded to four digits (e.g. {@code LONDON-0007}). The
     * count of existing owners in the city ignores letter case.
     *
     * @param city the city of the owner being created
     * @return the assigned customer code
     */
    private String nextCustomerCode(String city) {
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count();
        return String.format("%s-%04d", city.toUpperCase(), ownersInCity + 1);
    }

    /**
     * Determines whether an owner already exists using the same telephone number.
     * Comparison ignores letter case and surrounding or repeated whitespace.
     *
     * @param owner the candidate owner to check
     * @return {@code true} if any other owner with the same telephone is already stored
     */
    private boolean ownerAlreadyExists(Owner owner) {
        String telephone = normalizeTelephone(owner.getTelephone());
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(normalizeTelephone(existing.getTelephone()), telephone));
    }

    /**
     * Normalizes a telephone number to digits only, stripping spaces, dashes and
     * parentheses (any non-digit character). This is the canonical stored form and is
     * used for the telephone-uniqueness check, so {@code "(613) 555-0100"} and
     * {@code "6135550100"} are treated as the same number.
     *
     * @param telephone the raw telephone value, may be {@code null}
     * @return the digits-only value, or {@code null} if {@code telephone} is {@code null}
     */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("[^0-9]", "");
    }

    /**
     * Determines whether another owner already uses the same email address.
     * Owners without an email are ignored, so a missing email never conflicts.
     * Comparison ignores letter case and surrounding or repeated whitespace.
     *
     * @param owner the candidate owner to check
     * @return {@code true} if the candidate supplies an email already used by a stored owner
     */
    private boolean emailAlreadyExists(Owner owner) {
        String email = normalize(owner.getEmail());
        if (email == null || email.isEmpty()) {
            return false;
        }
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(normalize(existing.getEmail()), email));
    }

    /**
     * Normalizes a value for duplicate detection so that comparisons ignore letter
     * case and surrounding or repeated whitespace. Leading and trailing whitespace is
     * trimmed, any run of internal whitespace is collapsed to a single space, and the
     * result is lower-cased. For example {@code "  john   smith "} and {@code "John Smith"}
     * both normalize to {@code "john smith"}.
     *
     * @param value the raw value, may be {@code null}
     * @return the normalized value, or {@code null} if {@code value} is {@code null}
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase();
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
