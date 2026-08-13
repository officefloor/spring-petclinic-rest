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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /** Dedicated audit trail for owner-lifecycle side effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

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
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String normalizedAddress = Owner.normalizeAddress(ownerFieldsDto.getAddress());
        if (normalizedAddress == null || normalizedAddress.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!this.clinicService.findOwnerByTelephone(normalizedTelephone).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (isEmailAlreadyUsed(ownerFieldsDto.getEmail())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (countOwnersInCity(ownerFieldsDto.getCity()) >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        LocalDate effectiveDate =
            owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        long ownersRegisteredToday = countOwnersRegisteredOn(registrationDate);
        if (ownersRegisteredToday >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        boolean bulkSignupWarning = ownersRegisteredToday > 80;
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        List<Owner> householdMembers =
            findHouseholdMembers(ownerFieldsDto.getLastName(), normalizedAddress);
        if (!sharesHousehold && !householdMembers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        HttpHeaders headers = new HttpHeaders();
        owner.setTelephone(normalizedTelephone);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(bulkSignupWarning);
        if (sharesHousehold && !householdMembers.isEmpty()) {
            String householdId = householdId(owner.getLastName(), normalizedAddress);
            owner.setHouseholdId(householdId);
            owner.setHouseholdSize(householdMembers.size() + 1);
            for (Owner member : householdMembers) {
                if (member.getHouseholdId() == null || member.getHouseholdId().isBlank()) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel());
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
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
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


    /**
     * Normalize a telephone number to E.164 form. Spaces, dashes and brackets are
     * stripped. When a leading {@code '+'} and country code are present they are kept;
     * otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
     * is dropped from the national digits. The result must be a {@code '+'} followed
     * by 8 to 15 digits, and is returned as {@code +<digits>}; otherwise {@code null}
     * is returned to signal a bad request.
     */
    /**
     * Build the customer code for a newly created owner, formatted
     * {@code '<CITY3>-<LAST3>-<NNNN>'} where {@code CITY3} is the upper-cased first
     * three letters of the city, {@code LAST3} the upper-cased first three letters of
     * the last name, and {@code NNNN} a per-city 4-digit zero-padded sequence equal to
     * one more than the number of owners already in that city.
     */
    private String nextCustomerCode(String city, String lastName) {
        String cityPrefix = prefixThree(city);
        String lastNamePrefix = prefixThree(lastName);
        String normalizedCity = normalizeForHousehold(city);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(normalizedCity))
            .count() + 1;
        return String.format("%s-%s-%04d", cityPrefix, lastNamePrefix, sequence);
    }

    /**
     * Count the existing owners whose city matches the given one, compared after
     * collapsing whitespace and lower-casing. Used to enforce the per-city capacity
     * cap: a city that already holds this many owners is at capacity.
     */
    private long countOwnersInCity(String city) {
        String normalizedCity = normalizeForHousehold(city);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(normalizedCity))
            .count();
    }

    /**
     * Count the existing owners whose registration date falls on the given day. Used to
     * enforce the per-day create limit: once this many owners have been registered today,
     * the day is at capacity and further creates are rejected.
     */
    private long countOwnersRegisteredOn(LocalDate day) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Roll a registration date forward to the next business day: a date falling on a
     * Saturday or Sunday is advanced to the following Monday, while a weekday is
     * returned unchanged.
     */
    private LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(8 - dayOfWeek.getValue());
        }
        return date;
    }

    /** Upper-cased first three letters of {@code value} (fewer if shorter, empty if null). */
    private String prefixThree(String value) {
        String v = value == null ? "" : value;
        return v.substring(0, Math.min(3, v.length())).toUpperCase(Locale.ROOT);
    }

    /**
     * Find the existing owners that already belong to the same household as the given
     * last name and address, i.e. those sharing both fields. Both are compared
     * case-insensitively after collapsing runs of whitespace to a single space and
     * trimming the ends.
     */
    private List<Owner> findHouseholdMembers(String lastName, String address) {
        String normalizedLastName = normalizeForHousehold(lastName);
        String normalizedAddress = normalizeForHousehold(address);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getLastName()).equals(normalizedLastName)
                && normalizeForHousehold(existing.getAddress()).equals(normalizedAddress))
            .toList();
    }

    /**
     * Count the existing owners that already share both the given first and last
     * name, compared case-insensitively. Used to populate an owner's namesake count
     * at creation time, reflecting how many owners with the same name existed before
     * this one was created.
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeName(firstName);
        String normalizedLastName = normalizeName(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeName(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    /**
     * Whether the given email, compared case-insensitively (lower-cased), is already
     * used by any existing owner. A blank or missing email never collides, since email
     * is optional.
     */
    private boolean isEmailAlreadyUsed(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getEmail)
            .filter(existing -> existing != null)
            .anyMatch(existing -> existing.toLowerCase(Locale.ROOT).equals(normalizedEmail));
    }

    /**
     * Build a stable identifier shared by all owners of one household. It is derived
     * deterministically from the normalized last name and address, so every owner at
     * the same household resolves to the same value regardless of creation order.
     */
    private String householdId(String lastName, String address) {
        String key = normalizeForHousehold(lastName) + "\n" + normalizeForHousehold(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return "HH-" + sb;
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        return digits.matches("[0-9]{8,15}") ? "+" + digits : null;
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
