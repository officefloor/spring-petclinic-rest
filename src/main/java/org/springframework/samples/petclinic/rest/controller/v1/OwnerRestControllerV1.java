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
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

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

    /**
     * Syntactic email validation: a non-empty local part, a single '@', and a
     * domain with at least one dot-separated label ending in a letter-only TLD.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /** Dedicated audit logger for owner lifecycle side-effects. */
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

    /**
     * Normalise a raw telephone number to E.164 form.
     *
     * <p>Spaces, dashes and brackets are stripped. A leading '+' and its country
     * code are kept when present; otherwise country code '+61' is assumed and a
     * single leading '0' is dropped from the national digits. The result must have
     * between 8 and 15 digits after the '+'.
     *
     * <p>When the caller supplies the country code explicitly (a leading '+'), the
     * national-number length is additionally validated against that country code:
     * '+61' requires 9 national digits and '+1' requires 10. A number whose national
     * length is wrong for its country is rejected.
     *
     * @return the E.164 string, or {@code null} if the input cannot form a valid one.
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\[\\]-]", "");
        String digits;
        boolean explicitCountryCode;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
            explicitCountryCode = true;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
            explicitCountryCode = false;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        if (explicitCountryCode && !hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Validate the national-number length of an E.164 number (its digits without the
     * leading '+') against the requirement for its country code: country code '+61'
     * (Australia) requires 9 national digits and '+1' (NANP) requires 10. Country codes
     * without a configured length requirement are accepted unchanged.
     */
    private static boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - "61".length() == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - "1".length() == 10;
        }
        return true;
    }

    /**
     * Normalise a text field for case-insensitive, whitespace-insensitive identity
     * comparison: leading/trailing whitespace is trimmed, internal runs of whitespace
     * are collapsed to a single space, and the result is lower-cased. A {@code null}
     * input normalises to the empty string.
     */
    private static String normaliseIdentity(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Normalise a postal address into its canonical stored form: leading/trailing whitespace is
     * trimmed, internal runs of whitespace are collapsed to a single space, the text is upper-cased,
     * and common street-type abbreviations are expanded ({@code ST -> STREET}, {@code RD -> ROAD},
     * {@code AVE -> AVENUE}). A {@code null} input normalises to the empty string. This is the value
     * that is both stored and used for every address comparison.
     */
    static String normaliseAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(expandAddressAbbreviation(tokens[i]));
        }
        return result.toString();
    }

    /**
     * Expand a single upper-cased address token if it is a known street-type abbreviation, ignoring a
     * trailing period (so both {@code ST} and {@code ST.} become {@code STREET}). Unknown tokens are
     * returned unchanged.
     */
    private static String expandAddressAbbreviation(String token) {
        String core = token.endsWith(".") ? token.substring(0, token.length() - 1) : token;
        return switch (core) {
            case "ST" -> "STREET";
            case "RD" -> "ROAD";
            case "AVE" -> "AVENUE";
            default -> token;
        };
    }

    /**
     * Derive the stable, shared household identifier for owners sharing a household. It is a
     * deterministic UUID over the normalised last-name and address, so every owner in the same
     * household resolves to the same value regardless of the order in which they were created.
     */
    private static String householdId(String lastNameKey, String addressKey) {
        String seed = lastNameKey + " " + addressKey;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * Build an owner's derived duplicate-detection identity key as
     * {@code '<normalizedTelephone>|<email or empty>|<householdId or empty>'}. Each {@code null}
     * component contributes the empty string, and the email is lower-cased so the comparison is
     * case-insensitive. Two owners are duplicates only when their whole identity keys are equal.
     */
    private static String identityKey(String normalizedTelephone, String email, String householdId) {
        String telephone = normalizedTelephone == null ? "" : normalizedTelephone;
        String emailKey = email == null ? "" : email.toLowerCase();
        String household = householdId == null ? "" : householdId;
        return telephone + "|" + emailKey + "|" + household;
    }

    /**
     * Build the customer code for a new owner as {@code '<CITY3>-<LAST3>-<NNNN>'}, where CITY3 is the
     * upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the
     * last name, and NNNN a per-city 4-digit zero-padded sequence equal to one more than the number of
     * owners already in that city at the time the owner was created.
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = prefix3(city);
        String last3 = prefix3(lastName);
        String cityKey = normaliseIdentity(city);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normaliseIdentity(existing.getCity()).equals(cityKey))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Roll a registration date forward to the next business day: when it falls on a Saturday or
     * Sunday it is advanced to the following Monday; weekday dates are returned unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Count the owners already created on the business day that {@code registrationDate} rolls to,
     * using the same per-day accumulation path as the daily create-limit rule.
     */
    private long ownersCreatedOn(LocalDate registrationDate) {
        LocalDate day = toBusinessDay(registrationDate);
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(existing -> existing != null)
            .map(OwnerRestControllerV1::toBusinessDay)
            .filter(day::equals)
            .count();
    }

    /** Upper-cased first three characters of {@code value} (fewer if it is shorter); "" when null. */
    private static String prefix3(String value) {
        String letters = value == null ? "" : value;
        return letters.substring(0, Math.min(3, letters.length())).toUpperCase();
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
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(owner.getRegistrationDate() != null
            && ownersCreatedOn(owner.getRegistrationDate()) > 80);
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String address = normaliseAddress(ownerFieldsDto.getAddress());
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveDate = ownerFieldsDto.getRegistrationDate() != null
            ? ownerFieldsDto.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        long ownersCreatedThatDay = this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(existing -> existing != null)
            .map(OwnerRestControllerV1::toBusinessDay)
            .filter(registrationDate::equals)
            .count();
        if (ownersCreatedThatDay >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        String cityKey = normaliseIdentity(ownerFieldsDto.getCity());
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normaliseIdentity(existing.getCity()).equals(cityKey))
            .count();
        if (ownersInCity >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        String lastNameKey = normaliseIdentity(ownerFieldsDto.getLastName());
        String addressKey = address;
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing ->
                normaliseIdentity(existing.getLastName()).equals(lastNameKey)
                    && normaliseAddress(existing.getAddress()).equals(addressKey))
            .toList();
        String householdId = (sharesHousehold && !householdMembers.isEmpty())
            ? householdId(lastNameKey, addressKey) : null;
        String identityKey = identityKey(telephone, email, householdId);
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey(toE164(existing.getTelephone()),
                existing.getEmail(), existing.getHouseholdId()).equals(identityKey));
        if (identityInUse) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        String firstNameKey = normaliseIdentity(ownerFieldsDto.getFirstName());
        int namesakeCount = (int) this.clinicService.findAllOwners().stream()
            .filter(existing ->
                normaliseIdentity(existing.getFirstName()).equals(firstNameKey)
                    && normaliseIdentity(existing.getLastName()).equals(lastNameKey))
            .count();
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setNamesakeCount(namesakeCount);
        owner.setAddress(address);
        owner.setTelephone(telephone);
        owner.setEmail(email == null ? null : email.toLowerCase());
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        if (householdId != null) {
            owner.setHouseholdId(householdId);
            for (Owner member : householdMembers) {
                if (member.getHouseholdId() == null) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerMapper.formatMembershipLevel(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(ownersCreatedThatDay > 80);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(telephone);
        currentOwner.setEmail(email == null ? null : email.toLowerCase());
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
