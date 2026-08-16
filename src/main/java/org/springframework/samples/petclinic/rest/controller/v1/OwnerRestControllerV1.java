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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyRegistrationLimitException;
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
     * A pragmatic syntactic check for an email address: a non-empty local part, an {@code @},
     * and a dotted domain. Deliberately conservative so obviously malformed values (e.g. a bare
     * word with no {@code @}) are rejected while ordinary addresses are accepted.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        String normalizedAddress = normalizeAddress(ownerFieldsDto.getAddress());
        if (isBlank(normalizedAddress)) {
            missingFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
        LocalDate registrationDate = toBusinessDay(
            ownerFieldsDto.getRegistrationDate() != null
                ? ownerFieldsDto.getRegistrationDate()
                : LocalDate.now());
        long registeredOnDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (registeredOnDay >= 100) {
            throw new DailyRegistrationLimitException(
                "100 or more owners have already been registered today");
        }
        String city = ownerFieldsDto.getCity();
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (ownersInCity >= 50) {
            throw new CityAtCapacityException(
                "the owner's city already contains 50 or more owners");
        }
        ownerFieldsDto.setAddress(normalizedAddress);
        String lastName = normalizeHousehold(ownerFieldsDto.getLastName());
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing -> lastName.equals(normalizeHousehold(existing.getLastName()))
                && normalizedAddress.equals(normalizeAddress(existing.getAddress())))
            .toList();
        String householdId = null;
        if (!householdMembers.isEmpty()) {
            if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
                throw new DuplicateHouseholdException(
                    "an owner with the same last name and address already exists");
            }
            // Joining an existing household: share a stable identifier with every member,
            // backfilling any members created before the household was formed.
            householdId = householdId(lastName, normalizedAddress);
            for (Owner member : householdMembers) {
                if (!householdId.equals(member.getHouseholdId())) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        String telephone = toE164(ownerFieldsDto.getTelephone());
        boolean telephoneInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> telephone.equals(existing.getTelephone()));
        if (telephoneInUse) {
            throw new DuplicateTelephoneException(
                "an owner with the same E.164 telephone already exists");
        }
        ownerFieldsDto.setTelephone(telephone);
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setHouseholdId(householdId);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
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
        currentOwner.setTelephone(toE164(ownerFieldsDto.getTelephone()));
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Rolls a registration date forward to the next business day: when it falls on a Saturday or
     * Sunday it is advanced to the following Monday; a weekday is returned unchanged. This is applied
     * to the effective registration date (whether supplied in the request or defaulted to the server
     * date), so every value derived from the registration date sees the adjusted business day.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Normalizes a last name for household-duplicate comparison: leading and trailing whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, and the result is
     * lower-cased so that last names are compared case-insensitively.
     */
    private static String normalizeHousehold(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Common street-type abbreviations expanded to their canonical full form during normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Normalizes an address to its canonical stored form: leading and trailing whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, the result is
     * upper-cased, and common street-type abbreviations are expanded token-by-token
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} or
     * whitespace-only value normalizes to the empty string. This form is both stored/returned and
     * used for every address comparison (household-duplicate detection and the shared household id).
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Derives the stable {@code householdId} shared by all owners in a household. It is a
     * deterministic function of the normalized last name and address, so every owner that
     * shares those values maps to the same identifier regardless of creation order.
     */
    private static String householdId(String normalizedLastName, String normalizedAddress) {
        String key = normalizedLastName + "\n" + normalizedAddress;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * Builds the {@code customerCode} assigned to a new owner, formatted
     * {@code '<CITY3>-<LAST3>-<NNNN>'}: {@code CITY3} is the upper-cased first three letters of the
     * owner's city, {@code LAST3} is the upper-cased first three letters of the owner's last name,
     * and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one more than the number
     * of owners already in that city (e.g. {@code 'SYD-SMI-0007'}).
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = prefix3(city);
        String last3 = prefix3(lastName);
        long sequence = this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count() + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /** Upper-cased first three letters (any non-letter removed) of the given value. */
    private static String prefix3(String value) {
        String letters = value == null ? "" : value.replaceAll("[^\\p{L}]", "");
        return letters.substring(0, Math.min(3, letters.length())).toUpperCase(Locale.ROOT);
    }

    /**
     * Counts the existing owners that share the given first name and last name, compared
     * case-insensitively. This is evaluated over the owners that already exist (i.e. before the
     * owner currently being created is saved), so it reflects how many namesakes preceded the
     * new owner.
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName != null && firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName != null && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Normalizes a telephone number to E.164 form. Spaces, dashes and brackets are stripped. A
     * leading {@code '+'} and its country code are kept as-is; otherwise the number is treated as a
     * national one, a single leading {@code '0'} is dropped, and the default country code
     * {@code '+61'} is prepended. The result must have 8 to 15 digits after the {@code '+'},
     * otherwise an {@link InvalidTelephoneException} is raised (reported to the client as 400).
     */
    private static String toE164(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(
                "telephone must form a valid E.164 number with 8 to 15 digits");
        }
        return "+" + digits;
    }

    /**
     * Email is optional: an absent (or blank) value is left as {@code null}. When a value is
     * present it must be a syntactically valid address, otherwise an {@link InvalidEmailException}
     * is raised (reported to the client as 400). A valid value is returned lower-cased so it is
     * stored and returned in canonical form.
     */
    private static String normalizeEmail(String email) {
        if (isBlank(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException("email must be a syntactically valid address");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
