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
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
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

    /**
     * Syntactic email check: a non-empty local part, a single {@code @}, and a domain that
     * contains at least one dot, with no whitespace anywhere. Deliberately lenient — it accepts
     * ordinary addresses while rejecting clearly malformed ones such as {@code not-an-email}.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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

    /**
     * Rejects an owner payload that is missing or blank in any required field, listing the name of
     * each offending field so the caller receives a 400 with a populated {@code errors} array.
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missing = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(ownerFieldsDto.getAddress())) {
            missing.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missing.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes a telephone on create into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading {@code +} and its country code are kept as given; otherwise the country code
     * {@code +61} is assumed and a single leading {@code 0} is dropped from the national digits.
     * The result must carry 8 to 15 digits after the {@code +}. Returns the E.164 string to be
     * stored and returned.
     *
     * @param telephone the raw telephone as supplied by the caller
     * @return the normalized E.164 telephone (a {@code +} followed by 8 to 15 digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private static String normalizeTelephone(String telephone) {
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
        String digits = e164.substring(1);
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(
                "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        return e164;
    }

    /**
     * Normalizes an optional email. An absent or blank email is left unset (returns {@code null});
     * a present value must be a syntactically valid address and is stored and returned lower-cased.
     *
     * @param email the raw email as supplied by the caller, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidEmailException if a value is present but is not a syntactically valid address
     */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects a create whose E.164 telephone is already used by any existing owner, so that
     * telephones stay unique across owners. Telephones are stored in E.164 form on create, so a
     * direct equality comparison of the E.164 values against the stored values is sufficient.
     *
     * @param normalizedTelephone the E.164 telephone of the owner being created
     * @throws DuplicateTelephoneException if another owner already uses this telephone
     */
    private void rejectDuplicateTelephone(String normalizedTelephone) {
        boolean inUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizedTelephone.equals(existing.getTelephone()));
        if (inUse) {
            throw new DuplicateTelephoneException(
                "Telephone is already used by another owner");
        }
    }

    /**
     * Rejects a create that would place the owner in a household already occupied by another owner,
     * i.e. one whose last name and address match the supplied values. The two fields are compared
     * case-insensitively after whitespace is collapsed (leading/trailing whitespace trimmed and each
     * run of internal whitespace reduced to a single space), so {@code '110 W.  Liberty St. '} and
     * {@code '110 w. liberty st.'} are treated as the same address. The caller may bypass this check
     * by setting {@code sharesHousehold} to {@code true}.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @throws DuplicateHouseholdException if another owner already shares this last name and address
     */
    private void rejectDuplicateHousehold(String lastName, String address) {
        String normalizedLastName = normalizeForHousehold(lastName);
        String normalizedAddress = normalizeForHousehold(address);
        boolean inHousehold = this.clinicService.findAllOwners().stream()
            .anyMatch(existing ->
                normalizedLastName.equals(normalizeForHousehold(existing.getLastName()))
                    && normalizedAddress.equals(normalizeForHousehold(existing.getAddress())));
        if (inHousehold) {
            throw new DuplicateHouseholdException(
                "An owner with the same last name and address already exists");
        }
    }

    /**
     * Normalizes a value for household comparison by trimming, collapsing every run of whitespace to
     * a single space and lower-casing, so the comparison is case-insensitive with collapsed whitespace.
     *
     * @param value the raw value, may be {@code null}
     * @return the normalized value ({@code ""} when {@code value} is {@code null})
     */
    private static String normalizeForHousehold(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Derives the stable household identifier for an owner from the normalized last name and address.
     * The value is a deterministic function of those two fields (compared case-insensitively with
     * collapsed whitespace, exactly as {@link #rejectDuplicateHousehold}), so every owner in the same
     * household — including owners who knowingly join it via {@code sharesHousehold} — is assigned the
     * same identifier without any existing record having to be updated. Formatted {@code 'HH-<HEX12>'}
     * where {@code HEX12} is the upper-cased first twelve hex characters of the SHA-256 of the two
     * normalized fields.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @return the stable household identifier
     */
    private static String householdId(String lastName, String address) {
        String key = normalizeForHousehold(lastName) + "\n" + normalizeForHousehold(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "HH-" + sb.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Builds the customer code assigned to an owner on create, formatted {@code '<LAST3>-<NNNN>'}
     * where {@code LAST3} is the upper-cased first three letters of the owner's last name and
     * {@code NNNN} is a global 4-digit zero-padded sequence equal to one more than the current
     * number of owners, e.g. {@code 'SMI-0007'}.
     *
     * @param lastName the last name of the owner being created
     * @return the formatted customer code
     */
    private String nextCustomerCode(String lastName) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = this.clinicService.findAllOwners().size() + 1;
        return String.format("%s-%04d", last3, sequence);
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
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            rejectDuplicateHousehold(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        }
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateTelephone(normalizedTelephone);
        ownerFieldsDto.setTelephone(normalizedTelephone);
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        if (ownerFieldsDto.getRegistrationDate() == null) {
            ownerFieldsDto.setRegistrationDate(LocalDate.now());
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setCustomerCode(nextCustomerCode(ownerFieldsDto.getLastName()));
        owner.setHouseholdId(householdId(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress()));
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
