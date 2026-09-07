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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidFieldsException;
import org.springframework.samples.petclinic.rest.advice.RequiredFieldsMissingException;
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
        prepareNewOwner(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCustomerCode(nextCustomerCode(owner.getLastName()));
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
        currentOwner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
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

    /**
     * Applies the intake rules a newly submitted owner must satisfy before it is persisted.
     * The required fields are checked, the telephone and email are normalized in place, and
     * the resulting telephone is rejected when it already belongs to another owner. Each rule
     * signals a violation by throwing, which the exception advice renders as the matching 4xx
     * response; when the method returns normally {@code ownerFieldsDto} is normalized and ready
     * to be mapped and saved.
     *
     * @param ownerFieldsDto the submitted owner fields, normalized in place
     */
    private void prepareNewOwner(OwnerFieldsDto ownerFieldsDto) {
        validateRequiredFields(ownerFieldsDto);
        ownerFieldsDto.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        rejectDuplicateTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateHousehold(ownerFieldsDto);
    }

    /**
     * Builds the customer code assigned to a newly created owner. The code is formatted as
     * {@code '<LAST3>-<NNNN>'}, where {@code LAST3} is the upper-cased first three letters of
     * the owner's last name and {@code NNNN} is a global 4-digit zero-padded sequence equal to
     * one more than the current number of owners. For example the seventh owner named
     * {@code 'Smith'} is assigned {@code 'SMI-0007'}.
     *
     * @param lastName the owner's last name
     * @return the formatted customer code
     */
    private String nextCustomerCode(String lastName) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        long sequence = this.clinicService.findAllOwners().size() + 1L;
        return String.format("%s-%04d", last3, sequence);
    }

    /**
     * Rejects an owner whose {@code firstName}, {@code lastName}, {@code address}, {@code city}
     * or {@code telephone} is missing or blank. A field that is only whitespace is treated as
     * blank. When any field fails, a {@link RequiredFieldsMissingException} is thrown carrying
     * the names of every offending field, which the exception advice renders as a 400 response
     * with an {@code errors} array.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", ownerFieldsDto.getFirstName());
        addIfBlank(missing, "lastName", ownerFieldsDto.getLastName());
        addIfBlank(missing, "address", ownerFieldsDto.getAddress());
        addIfBlank(missing, "city", ownerFieldsDto.getCity());
        addIfBlank(missing, "telephone", ownerFieldsDto.getTelephone());
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private void addIfBlank(List<String> missing, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missing.add(fieldName);
        }
    }

    /**
     * Normalizes a submitted telephone number into E.164 form. Spaces, dashes and brackets
     * are stripped. When a leading {@code '+'} and country code are present they are kept as
     * given; otherwise the country code {@code '+61'} is assumed and a single leading
     * {@code '0'} is dropped from the national digits. The result must be a {@code '+'}
     * followed by 8 to 15 digits. The normalized E.164 value is what gets stored and returned.
     * When the value cannot form a valid E.164 number an {@link InvalidFieldsException} is
     * thrown, which the exception advice renders as a 400 response naming the {@code telephone}
     * field. For example {@code "0412 345 678"} becomes {@code "+61412345678"}.
     *
     * @param telephone the submitted telephone value
     * @return the normalized E.164 telephone
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = "+" + cleaned.substring(1);
        } else {
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            e164 = "+61" + national;
        }
        if (!e164.matches("^\\+[0-9]{8,15}$")) {
            throw new InvalidFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Pattern for a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain containing at least one dot, none of the parts holding whitespace or a
     * second {@code @}.
     */
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /**
     * Normalizes an optional owner email. An email is optional, so a {@code null} value is
     * accepted and returned unchanged. When present it must be a syntactically valid address;
     * the value is stored and returned lower-cased. When present but invalid an
     * {@link InvalidFieldsException} is thrown, which the exception advice renders as a 400
     * response naming the {@code email} field.
     *
     * @param email the submitted email value, or {@code null} when omitted
     * @return the lower-cased email, or {@code null} when none was supplied
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidFieldsException(List.of("email"));
        }
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Rejects a new owner whose normalized telephone is already used by any existing owner.
     * The telephone is stored in its normalized E.164 form, so a plain equality check
     * against every owner's telephone compares E.164 values. When a match is found a
     * {@link DuplicateTelephoneException} is thrown, which the exception advice renders as a
     * 409 Conflict response.
     *
     * @param telephone the normalized telephone of the owner being created
     */
    private void rejectDuplicateTelephone(String telephone) {
        boolean taken = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> telephone.equals(existing.getTelephone()));
        if (taken) {
            throw new DuplicateTelephoneException(telephone);
        }
    }

    /**
     * Rejects a new owner whose {@code lastName} and {@code address} already belong to another
     * owner. Both fields are compared case-insensitively and with runs of whitespace collapsed to
     * a single space (and surrounding whitespace trimmed), so for example {@code "Franklin"} at
     * {@code "110  W. Liberty St."} clashes with {@code "franklin"} at {@code "110 W. Liberty St."}.
     * A request may bypass this rule by setting {@code sharesHousehold} to {@code true}, allowing
     * several owners to share one household. When a clash is found and the request did not opt in a
     * {@link DuplicateHouseholdException} is thrown, which the exception advice renders as a 409
     * Conflict response.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private void rejectDuplicateHousehold(OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String lastName = collapseWhitespace(ownerFieldsDto.getLastName());
        String address = collapseWhitespace(ownerFieldsDto.getAddress());
        boolean taken = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> lastName.equalsIgnoreCase(collapseWhitespace(existing.getLastName()))
                && address.equalsIgnoreCase(collapseWhitespace(existing.getAddress())));
        if (taken) {
            throw new DuplicateHouseholdException(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        }
    }

    /**
     * Collapses a field for household comparison: leading and trailing whitespace is trimmed and
     * every internal run of whitespace is reduced to a single space. A {@code null} value collapses
     * to the empty string. Case is left untouched so callers can compare case-insensitively.
     *
     * @param value the value to collapse, or {@code null}
     * @return the whitespace-collapsed value, never {@code null}
     */
    private String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
