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

    /** Dedicated audit trail logger; carries a line for each successful owner create. */
    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("AUDIT");

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

    /** Matches a syntactically valid email address: a local part and domain (with at least one dot)
     *  separated by '@', none of which contain whitespace or a second '@'. */
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Normalize the optional email: absent stays absent; when present it must be syntactically
     * valid and is lower-cased. Returns {@code false} if present but invalid.
     */
    private boolean normalizeEmail(Owner owner) {
        String email = owner.getEmail();
        if (email == null || email.isEmpty()) {
            owner.setEmail(null);
            return true;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        owner.setEmail(email.toLowerCase(java.util.Locale.ROOT));
        return true;
    }

    /**
     * Normalize a telephone number to E.164 form: strip spaces, dashes and brackets; keep a leading
     * '+' and country code when present, otherwise assume country code '+61' and drop a single leading
     * '0' from the national digits. The result must have 8 to 15 digits after the '+'.
     *
     * @return the E.164 string (e.g. {@code +61412345678}), or {@code null} when the input cannot form
     *         a valid E.164 number.
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
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Build the customer code {@code <CITY3>-<LAST3>-<NNNN>}: CITY3 is the upper-cased first three
     * letters of the city, LAST3 the upper-cased first three letters of the last name and NNNN a
     * per-city 4-digit zero-padded sequence equal to one more than the owners already in that city
     * (e.g. {@code SYD-SMI-0007}).
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(java.util.Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(java.util.Locale.ROOT);
        int sequence = 1;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                sequence++;
            }
        }
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /** Common street-type abbreviations expanded during address normalization. */
    private static final java.util.Map<String, String> ADDRESS_ABBREVIATIONS =
        java.util.Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Normalize an address: {@code null} becomes empty, surrounding whitespace is trimmed, internal
     * runs of whitespace are collapsed to a single space, the value is upper-cased and common
     * street-type abbreviations (ST-&gt;STREET, RD-&gt;ROAD, AVE-&gt;AVENUE) are expanded token by token.
     */
    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
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
     * Normalize a value for household-duplicate comparison: {@code null} becomes empty, surrounding
     * whitespace is trimmed, internal runs of whitespace are collapsed to a single space and the result
     * is lower-cased, so the comparison is case-insensitive with collapsed whitespace.
     */
    private static String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Return the owners that already share this owner's household, i.e. have the same last name and
     * address compared case-insensitively with collapsed whitespace.
     */
    private List<Owner> householdMembers(Owner owner) {
        String lastName = normalizeForHousehold(owner.getLastName());
        String address = normalizeForHousehold(owner.getAddress());
        List<Owner> members = new java.util.ArrayList<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (normalizeForHousehold(existing.getLastName()).equals(lastName)
                && normalizeForHousehold(existing.getAddress()).equals(address)) {
                members.add(existing);
            }
        }
        return members;
    }

    /**
     * Return {@code true} when another owner already shares this owner's household, i.e. has the same
     * last name and address compared case-insensitively with collapsed whitespace.
     */
    private boolean sharesHouseholdWithExisting(Owner owner) {
        return !householdMembers(owner).isEmpty();
    }

    /**
     * Count the existing owners that share this owner's first name and last name, compared
     * case-insensitively. Used to populate {@code namesakeCount} on create (before this owner is saved).
     */
    private int namesakeCount(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (firstName != null && lastName != null
                && firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }

    /**
     * A stable, shared household identifier derived from the normalized last name and address, so every
     * owner in the same household deterministically resolves to the same value.
     */
    private static String householdIdFor(Owner owner) {
        String key = normalizeForHousehold(owner.getLastName()) + "|" + normalizeForHousehold(owner.getAddress());
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(24);
            for (int i = 0; i < 12; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Maximum number of owners permitted in a single city. */
    private static final int CITY_CAPACITY = 50;

    /** Maximum number of owners that may be created (by registrationDate) on a single day. */
    private static final int DAILY_CREATE_LIMIT = 100;

    /** Number of owners already created on a day beyond which a bulk-signup warning is flagged. */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Count the existing owners whose city matches this owner's city, compared case-insensitively.
     * Used to enforce the per-city capacity on create (before this owner is saved).
     */
    private int cityOwnerCount(Owner owner) {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Roll a registration date forward onto a business day: a Saturday or Sunday is advanced to the
     * following Monday, any weekday is returned unchanged. Applied to the effective registration date
     * (supplied or defaulted) so that everything derived from it — the stored {@code registrationDate},
     * the membership number's year segment and the per-day create limit — uses the adjusted date.
     */
    private static java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.plusDays(2);
            case SUNDAY:
                return date.plusDays(1);
            default:
                return date;
        }
    }

    /**
     * Count the existing owners whose registrationDate equals the given day. Used to enforce the
     * per-day create limit on create (before this owner is saved).
     */
    private int ownersRegisteredOn(java.time.LocalDate day) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!normalizeEmail(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String address = normalizeAddress(owner.getAddress());
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(address);
        java.time.LocalDate registrationDate = toBusinessDay(
            owner.getRegistrationDate() == null ? java.time.LocalDate.now() : owner.getRegistrationDate());
        int ownersRegisteredToday = ownersRegisteredOn(registrationDate);
        if (ownersRegisteredToday >= DAILY_CREATE_LIMIT) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        if (cityOwnerCount(owner) >= CITY_CAPACITY) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        for (Owner existing : this.clinicService.findAllOwners()) {
            String existingTelephone = toE164(existing.getTelephone());
            if (telephone.equals(existingTelephone)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        List<Owner> householdMembers = householdMembers(owner);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (!sharesHousehold && !householdMembers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (sharesHousehold && !householdMembers.isEmpty()) {
            String householdId = householdMembers.stream()
                .map(Owner::getHouseholdId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .orElseGet(() -> householdIdFor(owner));
            owner.setHouseholdId(householdId);
            for (Owner member : householdMembers) {
                if (member.getHouseholdId() == null || member.getHouseholdId().isBlank()) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        owner.setNamesakeCount(namesakeCount(owner));
        owner.setBulkSignupWarning(ownersRegisteredToday > BULK_SIGNUP_WARNING_THRESHOLD);
        owner.setTelephone(telephone);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
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
        currentOwner.setEmail(ownerFieldsDto.getEmail());
        if (!normalizeEmail(currentOwner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
