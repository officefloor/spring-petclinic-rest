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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

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

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedAddress = normalizeAddress(owner.getAddress());
        if (normalizedAddress.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Address must not be blank after normalization");
        }
        owner.setAddress(normalizedAddress);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        java.time.LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        java.time.LocalDate businessDate = toBusinessDay(effectiveDate);
        owner.setRegistrationDate(businessDate);
        long createdToday = this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(businessDate::equals)
            .count();
        if (createdToday >= 100) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "The maximum number of owners for today has already been reached");
        }
        boolean telephoneInUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(java.util.Objects::nonNull)
            .anyMatch(normalizedTelephone::equals);
        if (telephoneInUse) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "An owner with this telephone number already exists");
        }
        String lastNameKey = normalizeForHousehold(owner.getLastName());
        String addressKey = normalizeForHousehold(owner.getAddress());
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getLastName()).equals(lastNameKey)
                && normalizeForHousehold(existing.getAddress()).equals(addressKey))
            .toList();
        if (!householdMembers.isEmpty()) {
            if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An owner with this last name already exists at this address");
            }
            String householdId = householdId(lastNameKey, addressKey);
            owner.setHouseholdId(householdId);
            for (Owner member : householdMembers) {
                if (!householdId.equals(member.getHouseholdId())) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        String cityKey = normalizeForHousehold(owner.getCity());
        long cityCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(cityKey))
            .count();
        if (cityCount >= 50) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This city already has the maximum number of owners");
        }
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Rolls a registration date forward to a business day. When {@code date} falls on a Saturday or
     * Sunday it is advanced to the following Monday; a weekday is returned unchanged. This is applied
     * to the effective registration date (whether supplied in the request or defaulted to the server
     * date) so that every stored {@code registrationDate}, and any value derived from it, lands on a
     * business day.
     *
     * @param date the effective registration date
     * @return the same date if it is a weekday, otherwise the following Monday
     */
    private java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    /**
     * Counts the owners that already exist (before the current create) whose first and last names
     * both match the supplied names, compared case-insensitively. The returned value is captured on
     * the new owner at creation time and does not change as further owners are added later.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of pre-existing namesake owners
     */
    private int namesakeCount(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null
                && existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName() != null
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
    }

    /**
     * Builds the customer code for a new owner, formatted {@code '<CITY3>-<LAST3>-<NNNN>'} where
     * {@code CITY3} is the upper-cased first three letters of the city, {@code LAST3} is the
     * upper-cased first three letters of the last name, and {@code NNNN} is a per-city 4-digit
     * zero-padded sequence equal to one more than the number of owners already in that city
     * (e.g. {@code 'SYD-SMI-0007'}).
     *
     * @param city     the owner's city
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    private String nextCustomerCode(String city, String lastName) {
        String cityPrefix = first3Upper(city);
        String lastPrefix = first3Upper(lastName);
        String cityKey = normalizeForHousehold(city);
        long sequence = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(cityKey))
            .count() + 1L;
        return String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence);
    }

    /**
     * Returns the upper-cased first three characters of {@code value}, or fewer if the value is
     * shorter. A {@code null} value yields the empty string.
     *
     * @param value the source value
     * @return the upper-cased first three characters
     */
    private String first3Upper(String value) {
        String v = (value == null ? "" : value);
        return v.substring(0, Math.min(3, v.length())).toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Normalizes a street address applied whenever an owner is created: leading and trailing
     * whitespace is trimmed, any internal run of whitespace is collapsed to a single space, the
     * value is upper-cased, and common street-type abbreviations are expanded to their full form
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The expansion is applied
     * per whitespace-delimited token, so only standalone abbreviations are expanded. A {@code null}
     * value normalizes to the empty string. So {@code '  12  main  st '} becomes
     * {@code '12 MAIN STREET'}.
     *
     * @param address the raw address as submitted, or {@code null} when absent
     * @return the normalized address, or the empty string when the value is blank
     */
    private String normalizeAddress(String address) {
        String collapsed = (address == null ? "" : address)
            .trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "ST" -> tokens[i] = "STREET";
                case "RD" -> tokens[i] = "ROAD";
                case "AVE" -> tokens[i] = "AVENUE";
                default -> { }
            }
        }
        return String.join(" ", tokens);
    }

    /**
     * Normalizes a value for household-duplicate comparison: leading and trailing whitespace is
     * trimmed, any internal run of whitespace is collapsed to a single space, and the result is
     * lower-cased so the comparison is case-insensitive. A {@code null} value normalizes to the
     * empty string.
     *
     * @param value the raw value (last name or address) as submitted
     * @return the normalized comparison key
     */
    private String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Derives the stable shared household identifier for a household, keyed on the normalized last
     * name and address. Because it is a pure function of those normalized keys, every owner in the
     * same household deterministically computes the same value. The identifier is the first 12
     * upper-case hex characters of the SHA-256 digest of the two keys joined with a NUL separator
     * (the separator prevents distinct name/address pairs from colliding).
     *
     * @param lastNameKey the normalized last-name comparison key
     * @param addressKey  the normalized address comparison key
     * @return the stable household identifier
     */
    private String householdId(String lastNameKey, String addressKey) {
        String seed = lastNameKey + " " + addressKey;
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase(java.util.Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a telephone number to E.164 form. Spaces, dashes and brackets (indeed any
     * non-digit character) are stripped. If the submitted value carries a leading {@code '+'}
     * its country code is kept as-is; otherwise the default country code {@code '+61'} is
     * assumed and a single leading {@code '0'} is dropped from the national digits. The
     * resulting value must be a {@code '+'} followed by 8 to 15 digits. So {@code '0412 345 678'}
     * is stored as {@code '+61412345678'}.
     *
     * @param telephone the raw telephone number as submitted
     * @return the normalized E.164 telephone number
     * @throws ResponseStatusException with a 400 status if the value cannot form a valid E.164
     *         number (8 to 15 digits after the '+')
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.trim();
        String digits = raw.replaceAll("\\D", "");
        String e164;
        if (raw.startsWith("+")) {
            e164 = "+" + digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            e164 = "+61" + digits;
        }
        int digitCount = e164.length() - 1;
        if (digitCount < 8 || digitCount > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        return e164;
    }

    /**
     * Normalizes an optional email address by lower-casing it. Syntactic validity is enforced
     * by Bean Validation on the request DTO, so a value reaching this point is either {@code null}
     * (absent) or already valid.
     *
     * @param email the email address as submitted, or {@code null} when absent
     * @return the lower-cased email, or {@code null} when absent
     */
    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(java.util.Locale.ROOT);
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
