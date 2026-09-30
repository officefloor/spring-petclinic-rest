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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        owner.setAddress(normalizeAddress(owner.getAddress()));
        LocalDate effectiveRegistrationDate = owner.getRegistrationDate();
        if (effectiveRegistrationDate == null) {
            effectiveRegistrationDate = LocalDate.now();
        }
        effectiveRegistrationDate = rollToBusinessDay(effectiveRegistrationDate);
        owner.setRegistrationDate(effectiveRegistrationDate);
        if (countOwnersRegisteredOn(effectiveRegistrationDate) >= DAILY_CREATE_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "the maximum number of owners that may be created today has been reached");
        }
        if (isTelephoneInUse(normalizedTelephone, null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "telephone is already in use by another owner");
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (!sharesHousehold
            && this.clinicService.isHouseholdInUse(owner.getLastName(), owner.getAddress(), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "another owner with the same last name already lives at this address");
        }
        if (countOwnersInCity(owner.getCity()) >= CITY_CAPACITY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "city has reached its maximum number of owners");
        }
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        if (sharesHousehold) {
            this.clinicService.assignHousehold(owner);
        }
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={}",
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

    /**
     * Builds the customer code for a new owner, formatted {@code '<CITY3>-<LAST3>-<NNNN>'} where CITY3 is
     * the upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the
     * last name, and NNNN a per-city 4-digit zero-padded sequence equal to one more than the number of
     * owners that already live in that city (e.g. {@code 'LON-SMI-0007'}).
     *
     * @param city     the owner's city
     * @param lastName the owner's last name
     * @return the assigned customer code
     */
    private String nextCustomerCode(String city, String lastName) {
        String cityPrefix = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String lastPrefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = countOwnersInCity(city) + 1;
        return String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence);
    }

    /**
     * The maximum number of owners allowed to live in a single city. A create is rejected once the
     * owner's city already contains this many owners.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * The maximum number of owners that may be created on a single day. A create is rejected with
     * {@link HttpStatus#TOO_MANY_REQUESTS} once this many owners already have today's
     * {@code registrationDate}.
     */
    private static final int DAILY_CREATE_LIMIT = 100;

    /**
     * Rolls a registration date forward onto a business day: a date that falls on a Saturday or
     * Sunday is moved forward to the following Monday, while a date already on a weekday is returned
     * unchanged. Applied to the effective registration date (supplied or defaulted to the server
     * date) so that the stored {@code registrationDate}, and every value derived from it, always
     * falls on a business day.
     *
     * @param date the effective registration date
     * @return the same date when it is a weekday, otherwise the following Monday
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    /**
     * Counts the existing owners whose {@code registrationDate} equals the given (already
     * business-day-adjusted) date, i.e. the number of owners that have already been created for that
     * business day. Called before a new owner is saved, so the returned value reflects the number of
     * owners created for that day prior to this create.
     *
     * @param date the adjusted business-day registration date to count against
     * @return the number of existing owners registered on that date
     */
    private int countOwnersRegisteredOn(LocalDate date) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Counts the existing owners that live in the given city, comparing city names case-insensitively.
     *
     * @param city the city to count owners for
     * @return the number of existing owners in that city
     */
    private int countOwnersInCity(String city) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
    }

    /**
     * Builds the membership number for a new owner, formatted {@code '<customerCode>-M<YY>'} where YY is
     * the last two digits of the registration date's year (e.g. {@code 'LON-SMI-0007-M26'}).
     *
     * @param customerCode     the owner's assigned customer code
     * @param registrationDate the owner's registration date
     * @return the assigned membership number
     */
    private String membershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", registrationDate.getYear() % 100);
        return String.format("%s-M%s", customerCode, yy);
    }

    /**
     * Counts the existing owners that already share the given first name and last name, comparing
     * both case-insensitively. Called before a new owner is saved, so the returned value reflects
     * the number of namesakes that existed prior to this create.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of existing owners with the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Normalizes a telephone number into E.164 form. Spaces, dashes and brackets are stripped. When
     * the value carries a leading {@code '+'} the following country code and digits are kept as-is;
     * otherwise country code {@code +61} is assumed and a single leading {@code '0'} is dropped from
     * the national digits. The result must be a {@code '+'} followed by 8 to 15 digits. This E.164
     * string is what gets stored and returned.
     *
     * <p>For example {@code "0412 345 678"} is normalized to {@code "+61412345678"}.
     *
     * @param telephone the raw telephone value supplied by the client
     * @return the normalized E.164 telephone number
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if the value cannot form a
     *                                 valid E.164 number (unexpected characters, or not 8 to 15
     *                                 digits after the {@code '+'})
     */
    private static String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "telephone could not be normalized to a valid E.164 number");
        }
        return "+" + digits;
    }

    /**
     * Returns the E.164 form of an already-stored telephone for duplicate comparison, falling back
     * to the raw stored value if it cannot be normalized (e.g. legacy seed data in another format).
     *
     * @param telephone the stored telephone value, possibly {@code null}
     * @return the E.164 form, or the raw value when it cannot be normalized
     */
    private static String toComparableTelephone(String telephone) {
        try {
            return normalizeTelephone(telephone);
        } catch (ResponseStatusException ex) {
            return telephone;
        }
    }

    /**
     * Basic syntactic email validation: a non-empty local part, an {@code @}, and a domain
     * containing at least one dot, none of the three sections containing whitespace or an
     * additional {@code @}.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Validates and normalizes an optional owner email address. A {@code null} value is left as is
     * (email is optional). When a value is supplied it must be a syntactically valid address; the
     * value is then stored and returned lower-cased.
     *
     * @param email the raw email value supplied by the client, possibly {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if a value is present but
     *                                 is not a syntactically valid address
     */
    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "email must be a syntactically valid address");
        }
        return email.toLowerCase(Locale.ROOT);
    }

    /**
     * Common street-type abbreviations expanded to their full word during address normalization.
     * Keys and values are upper-case; matching is done per whitespace-delimited token.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Normalizes an owner address for storage. Leading/trailing whitespace is trimmed and internal
     * runs of whitespace are collapsed to a single space, the value is upper-cased, and common
     * street-type abbreviations are expanded per token ({@code ST -> STREET}, {@code RD -> ROAD},
     * {@code AVE -> AVENUE}). This normalized value is what gets stored, returned, and used for every
     * address comparison (household duplicate detection and the shared household id).
     *
     * <p>For example {@code "  12  main  st "} is normalized to {@code "12 MAIN STREET"}.
     *
     * @param address the raw address supplied by the client, possibly {@code null}
     * @return the normalized address
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if the address is blank
     *                                 after normalization (required field)
     */
    private static String normalizeAddress(String address) {
        String collapsed = address == null ? "" : address.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "address must not be blank");
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }

    /**
     * Determines whether the given normalized E.164 telephone is already used by any owner other
     * than the one identified by {@code excludeId} (pass {@code null} to consider every owner, e.g.
     * on create). Stored telephones are compared by their E.164 value so that differently formatted
     * inputs representing the same number are treated as duplicates.
     *
     * @param normalizedTelephone the already-normalized E.164 telephone to look for
     * @param excludeId           id of an owner to ignore during the check, or {@code null} for none
     * @return {@code true} if another owner already uses the telephone
     */
    private boolean isTelephoneInUse(String normalizedTelephone, Integer excludeId) {
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue;
            }
            if (normalizedTelephone.equals(toComparableTelephone(existing.getTelephone()))) {
                return true;
            }
        }
        return false;
    }

}
