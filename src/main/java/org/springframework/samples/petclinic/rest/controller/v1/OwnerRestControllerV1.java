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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
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

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    /**
     * Pattern for a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain containing at least one dot, with no whitespace in any segment.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            requireUniqueHousehold(owner.getLastName(), owner.getAddress());
        }
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        requireUniqueTelephone(telephone);
        owner.setTelephone(telephone);
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        owner.setCustomerCode(generateCustomerCode(owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Generates the customer code for a newly created owner, formatted {@code '<LAST3>-<NNNN>'}.
     * {@code LAST3} is the upper-cased first three letters of the last name and {@code NNNN} is a
     * global, 4-digit zero-padded sequence equal to one more than the current number of owners
     * (e.g. {@code 'SMI-0007'}).
     *
     * @param lastName the owner's last name
     * @return the generated customer code
     */
    private String generateCustomerCode(String lastName) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = this.clinicService.findAllOwners().size() + 1;
        return String.format("%s-%04d", last3, sequence);
    }

    /**
     * Rejects an owner payload that is missing or blank in any required field. Bean Validation
     * (via {@code @Valid}) already rejects {@code null} values and fields that violate their size
     * or pattern constraints; this guard additionally rejects whitespace-only values for fields
     * (such as address and city) that have no pattern constraint, so that no required field is blank.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if one or more required fields are missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> invalidFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            invalidFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            invalidFields.add("lastName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getAddress())) {
            invalidFields.add("address");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            invalidFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            invalidFields.add("telephone");
        }
        if (!invalidFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(invalidFields);
        }
    }

    /**
     * Normalizes a submitted telephone number to E.164 form and requires the result to be valid. The
     * normalized E.164 string (a leading {@code '+'} followed by country code and national digits) is
     * what gets stored and returned.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized E.164 telephone number
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Converts a telephone value to E.164 form, giving the single authoritative definition of a
     * telephone's normalized form. Spaces, dashes and brackets are stripped. A leading {@code '+'}
     * and its country code are kept when present; otherwise country code {@code '+61'} is assumed and
     * a single leading {@code '0'} is dropped from the national digits. The result is valid only when
     * it contains 8 to 15 digits after the {@code '+'}.
     *
     * <p>Returns {@code null} rather than throwing when the value cannot form a valid E.164 number, so
     * it is safe to apply to already-stored (possibly legacy) values when comparing them.
     *
     * @param telephone the raw telephone value, possibly {@code null}
     * @return the E.164 string, or {@code null} if the value cannot form a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[ \\-()]", "");
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
     * Normalizes an optional owner email. An absent (null or blank) email is allowed and yields
     * {@code null}. When present, the value must be a syntactically valid address; it is then stored
     * and returned lower-cased.
     *
     * @param email the raw email value from the request, possibly {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but not a valid email address
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects a telephone number whose E.164 form is already used by an existing owner. This enforces
     * that no two owners share the same E.164 telephone. Existing values are compared by their E.164
     * form; a stored value that cannot form a valid E.164 number is compared as-is.
     *
     * @param telephone the normalized (E.164) telephone of the owner being created
     * @throws DuplicateTelephoneException if another owner already has this E.164 telephone
     */
    private void requireUniqueTelephone(String telephone) {
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(existing -> existing != null)
            .map(existing -> {
                String e164 = toE164(existing);
                return e164 != null ? e164 : existing;
            })
            .anyMatch(telephone::equals);
        if (inUse) {
            throw new DuplicateTelephoneException(telephone);
        }
    }

    /**
     * Rejects an owner whose last name and address match those of an existing owner, where both are
     * compared case-insensitively and with runs of whitespace collapsed to a single space (a shared
     * household). The caller skips this check when the request opts in via {@code sharesHousehold}.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @throws DuplicateHouseholdException if another owner shares the same last name and address
     */
    private void requireUniqueHousehold(String lastName, String address) {
        String normalizedLastName = normalizeForHousehold(lastName);
        String normalizedAddress = normalizeForHousehold(address);
        boolean inUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizeForHousehold(existing.getLastName()).equals(normalizedLastName)
                && normalizeForHousehold(existing.getAddress()).equals(normalizedAddress));
        if (inUse) {
            throw new DuplicateHouseholdException(lastName, address);
        }
    }

    /**
     * Normalizes a value for case-insensitive, whitespace-collapsed household comparison: leading and
     * trailing whitespace is trimmed, any internal run of whitespace is collapsed to a single space,
     * and the result is lower-cased. A {@code null} value normalizes to an empty string.
     *
     * @param value the raw value, possibly {@code null}
     * @return the normalized value
     */
    private String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
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
