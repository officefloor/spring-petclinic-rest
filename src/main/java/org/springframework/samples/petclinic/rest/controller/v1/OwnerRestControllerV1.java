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
import java.util.Map;
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
import org.springframework.samples.petclinic.rest.advice.MissingOwnerFieldsException;
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
     * Matches a syntactically valid email: a non-empty local part and domain separated by a single
     * '@', with at least one dot-separated label in the domain and no whitespace anywhere.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Common street-type abbreviations expanded during address normalization, keyed by the
     * upper-cased abbreviation.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        applyHousehold(owner, ownerFieldsDto.getSharesHousehold());
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateTelephone(telephone);
        owner.setTelephone(telephone);
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Builds the next customer code, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is the
     * upper-cased first three letters of the owner's city, LAST3 the upper-cased first three letters
     * of the owner's last name and NNNN is a per-city 4-digit zero-padded sequence equal to one more
     * than the number of owners already in that city (e.g. {@code MAD-SMI-0007}).
     *
     * @param city     the owner's city
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getCity(), city))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Builds the owner's membership number, formatted {@code <customerCode>-M<YY>} where YY is the
     * last two digits of the registrationDate year, zero-padded (e.g. {@code MAD-SMI-0007-M26}).
     *
     * @param customerCode     the owner's assigned customer code
     * @param registrationDate the owner's registration date
     * @return the assigned membership number
     */
    private String membershipNumber(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /**
     * Counts the existing owners that already share the given firstName and lastName, compared
     * case-insensitively. Evaluated before the owner being created is saved, so it reflects only
     * the owners that pre-existed this create.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of existing namesakes
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getFirstName(), firstName)
                && equalsIgnoreCase(existing.getLastName(), lastName))
            .count();
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    /**
     * Rejects an owner whose required fields are missing or blank. Collects the names of every
     * offending field so the client learns about all of them at once.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws MissingOwnerFieldsException if firstName, lastName, address, city or telephone is
     *                                     missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> errors = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            errors.add("lastName");
        }
        if (isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
            errors.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            errors.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            errors.add("telephone");
        }
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes a submitted telephone into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading '+' with its country code is kept; otherwise the country code '+61' is assumed and
     * a single leading '0' is dropped from the national digits. The resulting value must be a '+'
     * followed by 8 to 15 digits.
     *
     * @param telephone the raw telephone value as submitted
     * @return the E.164 normalized telephone (e.g. {@code +61412345678})
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String nationalSignificantNumber;
        if (cleaned.startsWith("+")) {
            nationalSignificantNumber = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            nationalSignificantNumber = "61" + national;
        }
        if (!nationalSignificantNumber.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(telephone);
        }
        return "+" + nationalSignificantNumber;
    }

    /**
     * Normalizes a submitted email. An owner may omit the email entirely (a {@code null} or blank
     * value is treated as absent and returns {@code null}); when present the value must be a
     * syntactically valid address, and is stored and returned lower-cased.
     *
     * @param email the raw email value as submitted, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidEmailException if a non-blank value is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException(email);
        }
        return normalized;
    }

    /**
     * Rejects a normalized telephone that is already used by any other owner, so telephones stay
     * unique across owners. Existing telephones are normalized to E.164 the same way before the
     * comparison, so numbers that differ only in formatting still count as duplicates.
     *
     * @param e164Telephone the E.164 telephone of the owner being created
     * @throws DuplicateTelephoneException if another owner already uses the same E.164 telephone
     */
    private void rejectDuplicateTelephone(String e164Telephone) {
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(existing -> existing != null)
            .map(this::toComparableTelephone)
            .filter(existing -> existing != null)
            .anyMatch(e164Telephone::equals);
        if (inUse) {
            throw new DuplicateTelephoneException(e164Telephone);
        }
    }

    /**
     * Normalizes an existing owner's stored telephone to E.164 for duplicate comparison, returning
     * {@code null} when it cannot be normalized so a malformed legacy value is simply skipped rather
     * than aborting the whole create.
     */
    private String toComparableTelephone(String telephone) {
        try {
            return normalizeTelephone(telephone);
        } catch (InvalidTelephoneException ex) {
            return null;
        }
    }

    /**
     * Applies the household rule to an owner being created. An owner belongs to the same household
     * as an existing owner when their lastName and address both match (compared case-insensitively
     * with runs of whitespace collapsed to a single space).
     *
     * <p>When no such existing owner exists there is nothing to do. Otherwise the request must
     * deliberately opt in to sharing that household by setting {@code sharesHousehold=true}: when it
     * does, the new owner and every existing owner in the household are assigned the same stable
     * {@link #householdId(String, String) householdId}; when it does not, the create is rejected.
     *
     * @param owner            the owner being created
     * @param sharesHousehold  the request's {@code sharesHousehold} flag, may be {@code null}
     * @throws DuplicateHouseholdException if another owner shares the same lastName and address and
     *                                     the request did not opt in with {@code sharesHousehold=true}
     */
    private void applyHousehold(Owner owner, Boolean sharesHousehold) {
        String lastNameKey = toHouseholdKey(owner.getLastName());
        String addressKey = toHouseholdKey(owner.getAddress());
        List<Owner> household = this.clinicService.findAllOwners().stream()
            .filter(existing -> toHouseholdKey(existing.getLastName()).equals(lastNameKey)
                && toHouseholdKey(existing.getAddress()).equals(addressKey))
            .toList();
        if (household.isEmpty()) {
            return;
        }
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
        }
        String householdId = householdId(lastNameKey, addressKey);
        owner.setHouseholdId(householdId);
        for (Owner existing : household) {
            if (!householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                this.clinicService.saveOwner(existing);
            }
        }
    }

    /**
     * Derives the stable, shared household identifier for a household, formatted {@code HH-<HEX12>}
     * where HEX12 is the first 12 upper-cased hex characters of the SHA-256 of the normalized
     * lastName and address keys. Being a pure function of the household, every owner in it derives
     * the same value regardless of creation order.
     *
     * @param lastNameKey the normalized household lastName key
     * @param addressKey  the normalized household address key
     * @return the household identifier
     */
    private String householdId(String lastNameKey, String addressKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((lastNameKey + "|" + addressKey).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return "HH-" + hex.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Normalizes a lastName or address for household comparison: leading and trailing whitespace is
     * trimmed, internal runs of whitespace are collapsed to a single space, and the value is
     * lower-cased so the comparison is case-insensitive. A {@code null} value normalizes to an empty
     * string.
     */
    private String toHouseholdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes a submitted address into the canonical form stored and returned for an owner:
     * leading and trailing whitespace is trimmed, internal runs of whitespace are collapsed to a
     * single space, the value is upper-cased, and common street-type abbreviations are expanded on a
     * whole-word basis ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}). A {@code null}
     * value, or one that is blank once trimmed, normalizes to an empty string.
     *
     * @param address the raw address value as submitted, may be {@code null}
     * @return the normalized address, or an empty string when none was supplied
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
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
