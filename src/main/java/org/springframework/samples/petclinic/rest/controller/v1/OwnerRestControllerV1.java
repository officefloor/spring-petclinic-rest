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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        String address = normalizeAddress(owner.getAddress());
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(address);
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (owner.getEmail() != null) {
            String email = normalizeEmail(owner.getEmail());
            if (email == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            owner.setEmail(email);
        }
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (telephone.equals(toE164(existing.getTelephone()))) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        String lastNameKey = householdKey(owner.getLastName());
        String addressKey = householdKey(owner.getAddress());
        List<Owner> householdMembers = new ArrayList<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (lastNameKey.equals(householdKey(existing.getLastName()))
                && addressKey.equals(householdKey(existing.getAddress()))) {
                householdMembers.add(existing);
            }
        }
        if (!householdMembers.isEmpty()) {
            if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            String householdId = householdId(lastNameKey, addressKey);
            owner.setHouseholdId(householdId);
            for (Owner member : householdMembers) {
                if (!householdId.equals(member.getHouseholdId())) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        owner.setTelephone(telephone);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCustomerCode(nextCustomerCode(owner.getLastName()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
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

    /**
     * Builds the customer code for a newly created owner, formatted
     * {@code '<LAST3>-<NNNN>'} where {@code LAST3} is the upper-cased first three
     * letters of the last name and {@code NNNN} is a global 4-digit zero-padded
     * sequence equal to one more than the current number of owners.
     *
     * @param lastName the owner's last name
     * @return the assigned customer code (e.g. {@code 'SMI-0007'})
     */
    private String nextCustomerCode(String lastName) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = this.clinicService.findAllOwners().size() + 1;
        return String.format("%s-%04d", last3, sequence);
    }

    /**
     * Counts how many existing owners share the given first and last name, compared
     * case-insensitively. This is evaluated before the new owner is persisted, so the
     * owner being created is not included in the count.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of existing owners with the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Canonical expansions applied to common street-type abbreviations during address
     * normalization. Keys and values are upper-cased so the lookup runs after the address
     * has itself been upper-cased.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes an owner address for creation. Leading and trailing whitespace is trimmed,
     * every run of internal whitespace is collapsed to a single space, the result is
     * upper-cased and common street-type abbreviations are expanded ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). An abbreviation is recognized as a whole
     * token, optionally carrying a single trailing full stop (so both {@code 'St'} and
     * {@code 'St.'} become {@code 'STREET'}). The normalized value is what gets stored,
     * returned and compared for household duplicates; an address that is blank once
     * normalized fails the required-field check.
     *
     * @param address the raw address value supplied by the client
     * @return the normalized address (empty string when {@code address} is null or blank)
     */
    private static String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            String core = token.endsWith(".") ? token.substring(0, token.length() - 1) : token;
            String expanded = ADDRESS_ABBREVIATIONS.get(core);
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(expanded != null ? expanded : token);
        }
        return normalized.toString();
    }

    /**
     * Normalizes a last name or address for household-duplicate comparison: leading and
     * trailing whitespace is trimmed, every run of internal whitespace is collapsed to a
     * single space and the result is lower-cased. Two owners are considered to share a
     * household when both their last names and addresses produce the same key.
     *
     * @param value the raw last name or address value
     * @return the normalized comparison key (empty string when {@code value} is null)
     */
    private static String householdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Derives the stable shared household identifier for the given normalized last name
     * and address keys. The identifier is deterministic — the same household (same
     * {@link #householdKey normalized} last name and address) always maps to the same
     * value — so every owner who joins a household is assigned an identical id. It is the
     * upper-cased first twelve hex characters of the SHA-256 digest of {@code
     * '<lastNameKey>|<addressKey>'}.
     *
     * @param lastNameKey the normalized last name key
     * @param addressKey  the normalized address key
     * @return the shared household identifier
     */
    private static String householdId(String lastNameKey, String addressKey) {
        String source = lastNameKey + "|" + addressKey;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 12).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a telephone number for owner creation into E.164 form. Spaces, dashes
     * and brackets are stripped. When the number already carries a leading {@code '+'}
     * and country code these are kept; otherwise Australia's country code {@code '+61'}
     * is assumed and a single leading {@code '0'} is dropped from the national digits.
     * The resulting number must have between 8 and 15 digits after the {@code '+'}.
     *
     * @param telephone the raw telephone value supplied by the client
     * @return the E.164 telephone (e.g. {@code +61412345678}), or {@code null} when it
     *         cannot form a valid E.164 number (signalling a 400 Bad Request)
     */
    private static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        return digits.matches("[0-9]{8,15}") ? "+" + digits : null;
    }

    /**
     * Normalizes an owner email for creation. A present email must be a syntactically
     * valid address; the returned value is lower-cased.
     *
     * @param email the raw email value supplied by the client (never {@code null} here)
     * @return the lower-cased email, or {@code null} when it is not a syntactically valid
     *         address (signalling a 400 Bad Request)
     */
    private static String normalizeEmail(String email) {
        String trimmed = email.trim();
        if (!trimmed.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return null;
        }
        return trimmed.toLowerCase();
    }
}
