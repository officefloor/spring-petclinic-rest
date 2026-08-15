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
import org.springframework.samples.petclinic.rest.controller.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.controller.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.controller.InvalidEmailException;
import org.springframework.samples.petclinic.rest.controller.InvalidTelephoneException;
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

    /**
     * Pragmatic syntactic check for an email address: a non-empty local part, a single {@code @}, and a
     * domain with at least one dot and a two-or-more-letter final label. Whitespace is not permitted.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@.]{2,}$");

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
        rejectMissingOrBlankFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        owner.setEmail(normalizeEmail(owner.getEmail()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        rejectDuplicateTelephone(owner.getTelephone());
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            assignHousehold(owner);
        } else {
            rejectDuplicateHousehold(owner.getLastName(), owner.getAddress());
        }
        owner.setCustomerCode(generateCustomerCode(owner.getLastName()));
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
     * Rejects an owner payload that is missing or blank in any mandatory field. This complements Bean
     * Validation, which cannot reject whitespace-only values that still satisfy a minimum-length constraint.
     *
     * @param fields the submitted owner fields
     * @throws RequiredFieldsMissingException if any of firstName, lastName, address, city or telephone is
     *                                        {@code null} or blank
     */
    private void rejectMissingOrBlankFields(OwnerFieldsDto fields) {
        List<String> missing = new ArrayList<>();
        if (isBlank(fields.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(fields.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(fields.getAddress())) {
            missing.add("address");
        }
        if (isBlank(fields.getCity())) {
            missing.add("city");
        }
        if (isBlank(fields.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes a submitted telephone number into E.164 form, which becomes the stored and returned
     * value. Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are
     * kept as supplied; otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
     * is dropped from the national digits. The result must be a {@code '+'} followed by 8 to 15 digits.
     *
     * @param telephone the raw telephone value from the request
     * @return the E.164 telephone (a {@code '+'} followed by 8 to 15 digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            // No country code supplied: assume Australia (+61) and drop a single leading '0'.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(cleaned);
        }
        return "+" + digits;
    }

    /**
     * Normalizes a submitted email address. A {@code null} email is left absent (the field is optional).
     * When present, it must be a syntactically valid address; the stored and returned value is trimmed and
     * lower-cased.
     *
     * @param email the raw email value from the request, or {@code null} if omitted
     * @return the lower-cased email, or {@code null} if none was supplied
     * @throws InvalidEmailException if a non-null email is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects a create request whose normalized telephone is already used by another owner. Owners store
     * their telephone in normalized E.164 form, so an exact string comparison of E.164 values is sufficient.
     *
     * @param telephone the normalized (E.164) telephone of the owner being created
     * @throws DuplicateTelephoneException if any existing owner already uses the telephone
     */
    /**
     * Builds the customer code assigned to a newly created owner, formatted {@code '<LAST3>-<NNNN>'} where
     * {@code LAST3} is the upper-cased first three letters of the owner's last name and {@code NNNN} is a
     * global 4-digit zero-padded sequence equal to one more than the current number of owners (e.g.
     * {@code 'SMI-0007'}). The code is computed before the new owner is persisted, so the count excludes it.
     *
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    private String generateCustomerCode(String lastName) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = this.clinicService.findAllOwners().size() + 1;
        return String.format("%s-%04d", last3, sequence);
    }

    private void rejectDuplicateTelephone(String telephone) {
        boolean taken = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> telephone.equals(existing.getTelephone()));
        if (taken) {
            throw new DuplicateTelephoneException(telephone);
        }
    }

    /**
     * Rejects a create request whose owner shares both last name and address with an existing owner.
     * Last name and address are compared case-insensitively with runs of whitespace collapsed to a
     * single space and leading/trailing whitespace removed, so cosmetic differences (extra spaces,
     * differing case) still count as the same household.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @throws DuplicateHouseholdException if an existing owner has the same last name and address
     */
    private void rejectDuplicateHousehold(String lastName, String address) {
        String normalizedLastName = normalizeForComparison(lastName);
        String normalizedAddress = normalizeForComparison(address);
        boolean taken = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizedLastName.equals(normalizeForComparison(existing.getLastName()))
                && normalizedAddress.equals(normalizeForComparison(existing.getAddress())));
        if (taken) {
            throw new DuplicateHouseholdException(lastName, address);
        }
    }

    /**
     * Assigns the owner being created into a shared household. When at least one existing owner has the
     * same last name and address (compared with {@link #normalizeForComparison}), the joining owner and
     * those existing owners are all given the same stable {@code householdId}. An existing member's id is
     * reused when present; otherwise a deterministic id derived from the household's last name and address
     * is minted, so independently created members of the same household converge on one value. When no
     * existing owner shares the household, no id is assigned (the owner simply founds a new address).
     *
     * @param owner the owner being created, already opted in via {@code sharesHousehold}
     */
    private void assignHousehold(Owner owner) {
        String normalizedLastName = normalizeForComparison(owner.getLastName());
        String normalizedAddress = normalizeForComparison(owner.getAddress());
        List<Owner> housemates = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedLastName.equals(normalizeForComparison(existing.getLastName()))
                && normalizedAddress.equals(normalizeForComparison(existing.getAddress())))
            .toList();
        if (housemates.isEmpty()) {
            return;
        }
        String householdId = housemates.stream()
            .map(Owner::getHouseholdId)
            .filter(id -> id != null && !id.isBlank())
            .findFirst()
            .orElseGet(() -> householdIdFor(normalizedLastName, normalizedAddress));
        owner.setHouseholdId(householdId);
        for (Owner housemate : housemates) {
            if (!householdId.equals(housemate.getHouseholdId())) {
                housemate.setHouseholdId(householdId);
                this.clinicService.saveOwner(housemate);
            }
        }
    }

    /**
     * Derives a stable household identifier from the normalized last name and address of a household, as
     * the first 16 hex characters of the SHA-256 digest of {@code '<lastName>|<address>'}. The value is
     * deterministic, so any owner independently joining the same household computes the same identifier.
     *
     * @param normalizedLastName the household's last name, already normalized for comparison
     * @param normalizedAddress the household's address, already normalized for comparison
     * @return a stable, shareable household identifier
     */
    private String householdIdFor(String normalizedLastName, String normalizedAddress) {
        String key = normalizedLastName + "|" + normalizedAddress;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    /**
     * Normalizes a value for household-identity comparison: leading and trailing whitespace is
     * removed, internal runs of whitespace are collapsed to a single space, and the result is
     * lower-cased.
     *
     * @param value the raw value to normalize (may be {@code null})
     * @return the normalized value, or an empty string when {@code value} is {@code null}
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
