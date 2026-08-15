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
import org.springframework.samples.petclinic.rest.advice.InvalidAddressException;
import org.springframework.samples.petclinic.rest.advice.InvalidEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.Households;
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
        owner.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            rejectDuplicateHousehold(owner.getLastName(), owner.getAddress());
        }
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateTelephone(telephone);
        owner.setTelephone(telephone);
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(java.time.LocalDate.now());
        }
        owner.setCustomerCode(assignCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Assigns an owner's {@code customerCode} on create, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}
     * where CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased first
     * three letters of the last name and NNNN a per-city 4-digit zero-padded sequence equal to one
     * more than the number of owners already in that city (e.g. {@code 'SYD-SMI-0007'}).
     *
     * @param city the owner's city
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    private String assignCustomerCode(String city, String lastName) {
        String city3 = prefix3(city);
        String last3 = prefix3(lastName);
        String cityValue = city == null ? "" : city;
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> cityValue.equalsIgnoreCase(
                existing.getCity() == null ? "" : existing.getCity()))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Returns the upper-cased first three letters of the given value, or fewer if the value is
     * shorter, treating {@code null} as empty.
     */
    private String prefix3(String value) {
        String name = value == null ? "" : value;
        return name.substring(0, Math.min(3, name.length()))
            .toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Counts how many existing owners share the given first and last name, compared
     * case-insensitively, at the time this owner is created. The value excludes the owner
     * being created (which has not yet been saved) and is stored on the new owner and returned
     * as its {@code namesakeCount}.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners with the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> first.equalsIgnoreCase(existing.getFirstName())
                && last.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Normalizes an owner's telephone on create into E.164 form. Spaces, dashes and brackets are
     * stripped. A leading '+' and its country code are kept as given; otherwise country code '+61' is
     * assumed and a single leading '0' is dropped from the national digits. The resulting number must
     * have 8 to 15 digits after the '+'. For example {@code "0412 345 678"} becomes
     * {@code "+61412345678"}. The E.164 value is stored and returned.
     *
     * @param telephone the raw telephone as submitted
     * @return the normalized E.164 telephone (a '+' followed by 8 to 15 digits)
     * @throws InvalidTelephoneException (400 Bad Request) if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.trim();
        boolean hasCountryCode = raw.startsWith("+");
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        if (hasCountryCode) {
            cleaned = cleaned.substring(1);
        }
        String digits;
        if (hasCountryCode) {
            digits = cleaned;
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(telephone);
        }
        return "+" + digits;
    }

    /**
     * Syntactic email pattern: a non-empty local part, an '@', a domain with at least one dot and a
     * multi-character top-level label. Deliberately lenient about the exact character set while still
     * rejecting values that are clearly not addresses (e.g. missing '@').
     */
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    /**
     * Normalizes an owner's email. Email is optional: a {@code null} or blank value is left as
     * {@code null} (no email). When present it must be a syntactically valid address; the value is
     * lower-cased before being stored and returned.
     *
     * @param email the raw email as submitted, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidEmailException (400 Bad Request) if a non-blank value is not a valid address
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Common street-type abbreviations expanded to their full word during address normalization.
     */
    private static final java.util.Map<String, String> ADDRESS_ABBREVIATIONS = java.util.Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes an owner's address on create. Surrounding whitespace is trimmed and internal runs of
     * whitespace collapse to a single space, the value is upper-cased and common street-type
     * abbreviations are expanded to their full word ({@code ST} to {@code STREET}, {@code RD} to
     * {@code ROAD}, {@code AVE} to {@code AVENUE}). For example {@code "  12  main  st "} becomes
     * {@code "12 MAIN STREET"}. The normalized value is stored and returned, and is also the form used
     * for every address comparison (household duplicate detection and the shared household id).
     *
     * @param address the raw address as submitted
     * @return the normalized address
     * @throws InvalidAddressException (400 Bad Request) if the address is blank after normalization
     */
    private String normalizeAddress(String address) {
        String collapsed = (address == null ? "" : address).trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            throw new InvalidAddressException(address);
        }
        String[] tokens = collapsed.toUpperCase(java.util.Locale.ROOT).split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }

    /**
     * Rejects creating an owner whose E.164 telephone is already used by another owner. Telephones of
     * existing owners are normalized to E.164 the same way before comparison, so equivalent numbers
     * submitted in different formats (e.g. national {@code "0412 345 678"} and international
     * {@code "+61 412 345 678"}) are still treated as duplicates.
     *
     * @param telephone the E.164 telephone of the owner being created
     * @throws DuplicateTelephoneException (409 Conflict) if another owner already uses this telephone
     */
    private void rejectDuplicateTelephone(String telephone) {
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(existing -> existing != null)
            .map(this::toE164OrNull)
            .filter(existing -> existing != null)
            .anyMatch(telephone::equals);
        if (duplicate) {
            throw new DuplicateTelephoneException(telephone);
        }
    }

    /**
     * Normalizes an existing owner's telephone to E.164 for duplicate comparison, returning
     * {@code null} instead of throwing when the stored value cannot form a valid E.164 number.
     */
    private String toE164OrNull(String telephone) {
        try {
            return normalizeTelephone(telephone);
        } catch (InvalidTelephoneException e) {
            return null;
        }
    }

    /**
     * Rejects creating an owner who shares a household with an existing owner, i.e. another owner
     * already has the same last name and the same address. Last names are compared case-insensitively
     * with whitespace collapsed; addresses are compared in their normalized form (see
     * {@link #normalizeAddress}, which every owner's stored address already uses), so values that
     * differ only in letter case, spacing or a common street-type abbreviation still count as a match.
     * This check is skipped when the request sets {@code sharesHousehold} to {@code true}.
     *
     * @param lastName the last name of the owner being created
     * @param address the normalized address of the owner being created
     * @throws DuplicateHouseholdException (409 Conflict) if another owner shares this household
     */
    private void rejectDuplicateHousehold(String lastName, String address) {
        String normalizedLastName = Households.normalize(lastName);
        String normalizedAddress = Households.normalize(address);
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(existing ->
                Households.normalize(existing.getLastName()).equals(normalizedLastName)
                    && Households.normalize(existing.getAddress()).equals(normalizedAddress));
        if (duplicate) {
            throw new DuplicateHouseholdException(lastName, address);
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
