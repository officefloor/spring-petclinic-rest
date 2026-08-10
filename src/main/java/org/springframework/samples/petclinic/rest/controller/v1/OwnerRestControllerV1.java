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
     * Pragmatic check for a syntactically valid email address: a non-empty local
     * part, a single '@', and a domain with at least one dot-separated label.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");

    /**
     * Common street-type abbreviations expanded during address normalization,
     * keyed by their upper-cased form.
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
        // Validate & normalize the optional email; reject the create when it is present but invalid.
        if (!normalizeEmail(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Normalize the required address (trim/collapse whitespace, upper-case, expand
        // abbreviations) and store the normalized form back on the DTO so it is what gets
        // persisted and compared. Reject the create when the address is blank once normalized.
        String canonicalAddress = normalizeAddress(ownerFieldsDto.getAddress());
        if (canonicalAddress.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ownerFieldsDto.setAddress(canonicalAddress);
        // Reject the create when the owner's city is already at capacity, i.e. it already
        // contains 50 or more owners (compared case-insensitively).
        if (countOwnersInCity(ownerFieldsDto.getCity()) >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Normalize the telephone to E.164; reject the create when it cannot form a valid number.
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Reject the create if any existing owner already uses this telephone (compared in E.164 form).
        boolean telephoneInUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .map(OwnerRestControllerV1::toE164)
            .filter(existing -> existing != null)
            .anyMatch(normalizedTelephone::equals);
        if (telephoneInUse) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Reject the create when another owner already shares this owner's last name and address
        // (compared case-insensitively with collapsed whitespace), unless the request opts into
        // sharing a household.
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            String normalizedLastName = normalizeHouseholdKey(ownerFieldsDto.getLastName());
            String normalizedAddress = normalizeHouseholdKey(ownerFieldsDto.getAddress());
            boolean householdInUse = this.clinicService.findAllOwners().stream()
                .anyMatch(existing ->
                    normalizeHouseholdKey(existing.getLastName()).equals(normalizedLastName)
                        && normalizeHouseholdKey(existing.getAddress()).equals(normalizedAddress));
            if (householdInUse) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        ownerFieldsDto.setTelephone(normalizedTelephone);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        // Default the registration date to the server's current date when none was supplied.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        // Assign the customer code as '<CITY3>-<LAST3>-<NNNN>': the upper-cased first three
        // letters of the city, the upper-cased first three letters of the last name, and a
        // per-city 4-digit sequence one greater than the number of owners already in that city.
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        // Record how many existing owners already share this owner's first and last name
        // (compared case-insensitively) at the moment before this owner is created.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Validate and normalize the optional owner email in place.
     *
     * <p>An absent (or blank) email is allowed and is normalized to {@code null}.
     * When present it must be a syntactically valid address; a valid address is
     * stored back on the DTO lower-cased. Returns {@code false} when an email is
     * present but syntactically invalid, so the caller can reject with 400.
     */
    private boolean normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null || email.isBlank()) {
            ownerFieldsDto.setEmail(null);
            return true;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        ownerFieldsDto.setEmail(normalized);
        return true;
    }

    /**
     * Normalize an owner address: trim, collapse each run of whitespace to a single
     * space, upper-case, and expand common street-type abbreviations
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
     * A {@code null} value normalizes to the empty string.
     */
    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
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
     * Normalize an owner household field (last name or address) for duplicate detection:
     * trimmed, lower-cased and with all runs of whitespace collapsed to a single space.
     * A {@code null} value normalizes to the empty string.
     */
    private static String normalizeHouseholdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalize a telephone number to E.164 form.
     *
     * <p>Spaces, dashes and brackets are stripped. When the number carries an
     * explicit leading {@code '+'} and country code it is kept; otherwise the
     * country code {@code '+61'} is assumed and a single leading {@code '0'} is
     * dropped from the national digits. The result must contain 8 to 15 digits
     * after the {@code '+'}.
     *
     * @return the E.164 string (e.g. {@code +61412345678}), or {@code null} when
     *         the input is absent or cannot form a valid E.164 number.
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        // Strip spaces, dashes and brackets.
        String stripped = trimmed.replaceAll("[\\s()\\-]", "");
        String digits;
        if (hasCountryCode) {
            // Keep the explicit country code; drop the leading '+' for validation.
            digits = stripped.substring(1);
        } else {
            // No country code: assume '+61' and drop a single leading '0'.
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Build the next customer code, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}.
     *
     * <p>{@code CITY3} is the upper-cased first three letters of {@code city} and
     * {@code LAST3} the upper-cased first three letters of {@code lastName} (fewer if
     * the value is shorter). {@code NNNN} is a per-city 4-digit zero-padded sequence
     * equal to one more than the number of owners already in that city
     * (e.g. {@code 'SYD-SMI-0007'}).
     */
    /**
     * Count the existing owners whose first and last name match the given names,
     * compared case-insensitively. Used to record an owner's namesake count at the
     * moment before it is created.
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null
                && existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName() != null
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
    }

    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = countOwnersInCity(city) + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Count the existing owners located in the given city, compared case-insensitively.
     * Used to derive the per-city 4-digit sequence in the customer code.
     */
    private int countOwnersInCity(String city) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null
                && existing.getCity().equalsIgnoreCase(city))
            .count();
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!normalizeEmail(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(normalizedTelephone);
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
