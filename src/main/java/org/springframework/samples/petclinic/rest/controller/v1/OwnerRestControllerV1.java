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
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.controller.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.controller.InvalidEmailException;
import org.springframework.samples.petclinic.rest.controller.RequiredFieldsMissingException;
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
        validateRequiredFields(ownerFieldsDto);
        normalizeTelephone(ownerFieldsDto);
        rejectDuplicateTelephone(ownerFieldsDto.getTelephone());
        normalizeEmail(ownerFieldsDto);
        defaultRegistrationDate(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
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
        normalizeEmail(ownerFieldsDto);
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        currentOwner.setEmail(ownerFieldsDto.getEmail());
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


    /**
     * Rejects an owner whose mandatory fields ({@code firstName}, {@code lastName},
     * {@code address}, {@code city}, {@code telephone}) are missing or blank. Bean
     * Validation already rejects {@code null} values and the pattern-constrained fields
     * when blank, but {@code address} and {@code city} have no pattern, so a
     * whitespace-only value would otherwise slip through.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws RequiredFieldsMissingException if any required field is missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> errors = new ArrayList<>();
        requireText("firstName", ownerFieldsDto.getFirstName(), errors);
        requireText("lastName", ownerFieldsDto.getLastName(), errors);
        requireText("address", ownerFieldsDto.getAddress(), errors);
        requireText("city", ownerFieldsDto.getCity(), errors);
        requireText("telephone", ownerFieldsDto.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new RequiredFieldsMissingException(errors);
        }
    }

    private static void requireText(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }

    /**
     * Normalizes the submitted telephone on create to E.164 form (see {@link #toE164}) and
     * writes the canonical {@code '+'}-prefixed value back onto the request so it is persisted
     * and echoed back in that form.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws RequiredFieldsMissingException with a 400 status if the telephone cannot form a
     *                                        valid E.164 number
     */
    private static void normalizeTelephone(OwnerFieldsDto ownerFieldsDto) {
        String e164 = toE164(ownerFieldsDto.getTelephone());
        if (e164 == null) {
            throw new RequiredFieldsMissingException(List.of("telephone"));
        }
        ownerFieldsDto.setTelephone(e164);
    }

    /**
     * Converts a raw telephone to E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. Spaces, dashes and brackets are stripped. When a leading {@code '+'}
     * and country code are present they are kept; otherwise country code {@code '+61'} is
     * assumed and a single leading {@code '0'} is dropped from the national digits. The result
     * must contain 8 to 15 digits after the {@code '+'}.
     *
     * @param raw the submitted telephone, possibly {@code null}
     * @return the canonical E.164 string, or {@code null} if it is not a valid E.164 number
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Rejects a create whose E.164 telephone is already used by any existing owner. The
     * submitted telephone has already been normalized to E.164 by {@link #normalizeTelephone};
     * each stored owner's telephone is normalized the same way before comparison so differing
     * input formats collapse to the same value.
     *
     * @param normalizedTelephone the submitted, already-normalized E.164 telephone
     * @throws DuplicateTelephoneException with a 409 status if another owner already uses the
     *                                     same E.164 telephone
     */
    /**
     * Matches a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain of at least two dot-separated labels ending in an alphabetic TLD. None of
     * the parts may contain whitespace or a second {@code @}.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$");

    /**
     * Normalizes the submitted email when present: the value is trimmed, required to be a
     * syntactically valid address, and written back lower-cased so it is persisted and echoed
     * back in canonical form. A {@code null} email is left untouched — the field is optional.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidEmailException with a 400 status if the email is present but not syntactically valid
     */
    private static void normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null) {
            return;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        ownerFieldsDto.setEmail(trimmed.toLowerCase(Locale.ROOT));
    }

    /**
     * Defaults the registration date on create: when the client does not supply a
     * {@code registrationDate}, it is set to the server's current date so it is persisted
     * and echoed back in ISO 'YYYY-MM-DD' format. A supplied value is left untouched.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private static void defaultRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        if (ownerFieldsDto.getRegistrationDate() == null) {
            ownerFieldsDto.setRegistrationDate(LocalDate.now());
        }
    }

    private void rejectDuplicateTelephone(String normalizedTelephone) {
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .map(OwnerRestControllerV1::toE164)
            .filter(telephone -> telephone != null)
            .anyMatch(normalizedTelephone::equals);
        if (duplicate) {
            throw new DuplicateTelephoneException(normalizedTelephone);
        }
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
