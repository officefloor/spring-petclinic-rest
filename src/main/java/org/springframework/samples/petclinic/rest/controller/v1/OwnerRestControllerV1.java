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
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
     * Pattern for a syntactically valid email address: a non-empty local part, an '@', and a
     * dotted domain ending in a letters-only label. Applied to the trimmed, lower-cased value.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Common address abbreviations expanded to their full form during address normalization.
     * Matching is performed per whitespace-separated token on the upper-cased value.
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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String address = normalizeAddress(owner.getAddress());
        if (address == null || address.isEmpty()) {
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
        if (owner.getEmail() != null) {
            String email = normalizeEmail(owner.getEmail());
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            owner.setEmail(email);
        }
        boolean telephoneInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> telephone.equals(toE164(existing.getTelephone())));
        if (telephoneInUse) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        String cityKey = householdKey(owner.getCity());
        long cityCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null
                && cityKey.equals(householdKey(existing.getCity())))
            .count();
        if (cityCount >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        String lastNameKey = householdKey(owner.getLastName());
        String addressKey = householdKey(owner.getAddress());
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing -> lastNameKey.equals(householdKey(existing.getLastName()))
                && addressKey.equals(householdKey(existing.getAddress())))
            .toList();
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            String householdId = householdMembers.stream()
                .map(Owner::getHouseholdId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> householdId(owner));
            owner.setHouseholdId(householdId);
            for (Owner member : householdMembers) {
                if (member.getHouseholdId() == null) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        } else if (!householdMembers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        String firstNameKey = householdKey(owner.getFirstName());
        String namesakeLastNameKey = householdKey(owner.getLastName());
        long namesakeCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> firstNameKey.equals(householdKey(existing.getFirstName()))
                && namesakeLastNameKey.equals(householdKey(existing.getLastName())))
            .count();
        owner.setNamesakeCount((int) namesakeCount);
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Normalizes a telephone number into E.164 form. Spaces, dashes and brackets are stripped.
     * When the value carries a leading {@code '+'} its country code is kept as given; otherwise the
     * Australian country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped
     * from the national digits. The result must carry 8 to 15 digits after the {@code '+'}.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the E.164 telephone (e.g. {@code "+61412345678"}), or {@code null} if it cannot form
     *         a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String trimmed = telephone.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        String cleaned = trimmed.replaceAll("[\\s()\\[\\]-]", "");
        if (hasCountryCode) {
            cleaned = cleaned.substring(1);
        }
        if (!cleaned.matches("[0-9]+")) {
            return null;
        }
        String digits;
        if (hasCountryCode) {
            digits = cleaned;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Normalizes an email address for storage by trimming surrounding whitespace and lower-casing
     * it. The result is validated by the caller against {@link #EMAIL_PATTERN}.
     *
     * @param email the raw email value (must not be {@code null})
     * @return the trimmed, lower-cased email
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes a postal address for storage and comparison: surrounding whitespace is trimmed,
     * internal runs of whitespace are collapsed to a single space, the value is upper-cased and
     * common street-type abbreviations are expanded per token ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). The stored and returned address is this
     * normalized form, and every household comparison uses it.
     *
     * @param address the raw address value (may be {@code null})
     * @return the normalized address, or {@code null} if the input was {@code null}
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Normalizes a value for household-duplicate comparison: surrounding whitespace is trimmed,
     * internal runs of whitespace are collapsed to a single space and the result is lower-cased,
     * so that owners are compared case-insensitively with collapsed whitespace.
     *
     * @param value the raw value (must not be {@code null})
     * @return the normalized comparison key
     */
    private String householdKey(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Derives a stable, shared household identifier for the owner's household. The value is a
     * deterministic function of the normalized last name and address ({@link #householdKey}), so
     * every owner in the same household derives the identical identifier of the form
     * {@code 'HH-<12 upper-case hex>'}.
     *
     * @param owner the owner whose household identifier is derived
     * @return the stable household identifier
     */
    private String householdId(Owner owner) {
        String key = householdKey(owner.getLastName()) + "|" + householdKey(owner.getAddress());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "HH-" + sb.substring(0, 12).toUpperCase(Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Builds the customer code for a newly registered owner in the form
     * {@code '<CITY3>-<LAST3>-<NNNN>'}, where {@code CITY3} is the upper-cased first three
     * letters of the city, {@code LAST3} the upper-cased first three letters of the last name
     * and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one more than the
     * number of owners already registered in that city (e.g. {@code 'SYD-SMI-0007'}).
     *
     * @param city     the owner's city
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        String cityKey = householdKey(city);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && cityKey.equals(householdKey(existing.getCity())))
            .count() + 1;
        return String.format(Locale.ROOT, "%s-%s-%04d", city3, last3, sequence);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null) {
            email = normalizeEmail(email);
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(telephone);
        currentOwner.setEmail(email);
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
