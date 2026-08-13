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
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
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

    /**
     * Syntactic email check: a non-empty local part, a single {@code @}, and a domain that contains
     * at least one dot. Whitespace and additional {@code @} characters are disallowed.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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
        validateRequiredOwnerFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        rejectDuplicateTelephone(normalizedTelephone);
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
     * Rejects an owner payload that is missing or blank in any of the required fields
     * (firstName, lastName, address, city, telephone). The bean-validation constraints on
     * {@link OwnerFieldsDto} already reject {@code null} values and most malformed input, but
     * a present-yet-blank {@code address} or {@code city} would otherwise slip through, so this
     * guard enforces the rule uniformly for every required field.
     *
     * @param ownerFieldsDto the submitted owner payload
     * @throws InvalidOwnerFieldsException if one or more required fields are missing or blank
     */
    private void validateRequiredOwnerFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> invalidFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            invalidFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            invalidFields.add("lastName");
        }
        if (isBlank(ownerFieldsDto.getAddress())) {
            invalidFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            invalidFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            invalidFields.add("telephone");
        }
        if (!invalidFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(invalidFields);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes a submitted telephone number into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading {@code '+'} and its country code are kept as-is; otherwise the country code {@code '+61'}
     * is assumed and a single leading {@code '0'} is dropped from the national digits. The result must be
     * a {@code '+'} followed by 8 to 15 digits. For example {@code "0412 345 678"} normalizes to
     * {@code "+61412345678"}. The E.164 string is what gets stored and returned.
     *
     * @param telephone the raw telephone value from the submitted owner payload
     * @return the normalized E.164 telephone number
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.strip();
        boolean hasCountryCode = raw.startsWith("+");
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (hasCountryCode) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return "+" + digits;
    }

    /**
     * Best-effort conversion of an existing telephone value to E.164 form for duplicate comparison,
     * returning {@code null} when the value cannot form a valid E.164 number.
     *
     * @param telephone an existing owner's stored telephone value
     * @return the E.164 form, or {@code null} if it cannot be normalized
     */
    private String toE164OrNull(String telephone) {
        try {
            return normalizeTelephone(telephone);
        } catch (InvalidOwnerFieldsException ex) {
            return null;
        }
    }

    /**
     * Normalizes an optional owner email. A {@code null} value is left untouched (the field is optional).
     * When a value is present it must be a syntactically valid address (see {@link #EMAIL_PATTERN}); the
     * accepted value is trimmed and lower-cased before it is stored and returned.
     *
     * @param email the raw email value from the submitted owner payload, may be {@code null}
     * @return {@code null} if no email was supplied, otherwise the lower-cased email
     * @throws InvalidOwnerFieldsException if a value is present but not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects creating an owner whose E.164 telephone is already used by any other owner.
     * Existing owners' telephones are converted to E.164 form before comparison so that
     * differently-formatted representations of the same number are treated as duplicates.
     *
     * @param normalizedTelephone the E.164 telephone of the owner being created
     * @throws DuplicateOwnerTelephoneException if another owner already uses the same E.164 telephone
     */
    private void rejectDuplicateTelephone(String normalizedTelephone) {
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(telephone -> telephone != null)
            .map(this::toE164OrNull)
            .filter(telephone -> telephone != null)
            .anyMatch(normalizedTelephone::equals);
        if (duplicate) {
            throw new DuplicateOwnerTelephoneException(normalizedTelephone);
        }
    }
}
