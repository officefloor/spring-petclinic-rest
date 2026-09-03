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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private final AddressNormalizer addressNormalizer;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 AddressNormalizer addressNormalizer) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.addressNormalizer = addressNormalizer;
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
        String address = addressNormalizer.normalize(owner.getAddress());
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(address);
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setTelephone(telephone);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        if (isTelephoneInUse(telephone)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (!sharesHousehold && isHouseholdInUse(owner.getLastName(), owner.getAddress())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (sharesHousehold) {
            owner.setHouseholdId(householdId(owner.getLastName(), owner.getAddress()));
        }
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Build the customer code for a newly created owner, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}
     * where CITY3 is the upper-cased first three letters of {@code city}, LAST3 the upper-cased first
     * three letters of {@code lastName} and NNNN a per-city 4-digit zero-padded sequence equal to one
     * more than the number of owners already in that city (e.g. {@code 'LON-SMI-0007'}).
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * The number of existing owners that already share the given firstName and lastName,
     * compared case-insensitively (and with surrounding whitespace trimmed and internal
     * whitespace runs collapsed). Counted over the owners present before this create.
     */
    private int namesakeCount(String firstName, String lastName) {
        String candidateFirstName = normalizeName(firstName);
        String candidateLastName = normalizeName(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(candidateFirstName)
                && normalizeName(existing.getLastName()).equals(candidateLastName))
            .count();
    }

    /**
     * Convert a raw telephone input to its E.164 representation, or {@code null} if it
     * cannot form a valid E.164 number.
     *
     * <p>A leading '+' and country code are kept when present; otherwise country code
     * '+61' is assumed and a single leading '0' is dropped from the national digits.
     * Spaces, dashes and brackets (indeed any non-digit) are stripped. The result must
     * carry 8 to 15 digits after the '+'.
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        String e164;
        if (hasCountryCode) {
            e164 = digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            e164 = "61" + digits;
        }
        if (e164.length() < 8 || e164.length() > 15) {
            return null;
        }
        return "+" + e164;
    }

    /**
     * Whether any existing owner already holds the given telephone number. Both the
     * candidate and each stored number are compared in E.164 form, so the check does not
     * depend on how each was originally formatted.
     */
    private boolean isTelephoneInUse(String telephone) {
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(Objects::nonNull)
            .map(this::toE164)
            .filter(Objects::nonNull)
            .anyMatch(telephone::equals);
    }

    /**
     * Whether any existing owner already shares a household with the candidate, i.e. has both
     * the same lastName and the same address. Both fields are compared case-insensitively and
     * with internal runs of whitespace collapsed to a single space (and surrounding whitespace
     * trimmed), so the check does not depend on casing or spacing.
     */
    private boolean isHouseholdInUse(String lastName, String address) {
        String candidateLastName = normalizeName(lastName);
        String candidateAddress = addressNormalizer.normalize(address);
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizeName(existing.getLastName()).equals(candidateLastName)
                && addressNormalizer.normalize(existing.getAddress()).equals(candidateAddress));
    }

    /**
     * Normalize an owner's name for household comparison: trim, collapse internal whitespace runs
     * to a single space and lower-case. A null value normalizes to the empty string.
     */
    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * A stable shared identifier for the household formed by a given lastName and address.
     * Derived deterministically from the normalized (case-insensitive, whitespace-collapsed)
     * lastName and address, so every owner sharing the same household is assigned the same
     * value regardless of the order in which they are created. Formatted as the first 12
     * upper-case hex characters of the SHA-256 of {@code '<lastName>|<address>'}.
     */
    private String householdId(String lastName, String address) {
        String key = normalizeName(lastName) + "|" + addressNormalizer.normalize(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
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
