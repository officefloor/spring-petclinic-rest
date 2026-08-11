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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyRegistrationLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateEmailException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
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
        boolean bulkSignupWarning = isBulkSignupWarning();
        List<OwnerDto> ownerDtos = ownerMapper.toOwnerDtoCollection(owners);
        ownerDtos.forEach(ownerDto -> ownerDto.setBulkSignupWarning(bulkSignupWarning));
        return new ResponseEntity<>(ownerDtos, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarning());
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String address = normalizeAddress(ownerFieldsDto.getAddress());
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (isBlank(address)) {
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
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        if (isTelephoneInUse(telephone)) {
            throw new DuplicateTelephoneException(ownerFieldsDto.getTelephone());
        }
        if (isEmailInUse(ownerFieldsDto.getEmail())) {
            throw new DuplicateEmailException(ownerFieldsDto.getEmail());
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (!sharesHousehold
            && isHouseholdInUse(ownerFieldsDto.getLastName(), address)) {
            throw new DuplicateHouseholdException(ownerFieldsDto.getLastName(), address);
        }
        if (isCityAtCapacity(ownerFieldsDto.getCity())) {
            throw new CityAtCapacityException(ownerFieldsDto.getCity());
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(address);
        owner.setTelephone(telephone);
        LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        if (isDailyRegistrationLimitReached(registrationDate)) {
            throw new DailyRegistrationLimitException(registrationDate);
        }
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        if (sharesHousehold) {
            owner.setHouseholdId(joinHousehold(owner.getLastName(), owner.getAddress()));
        }
        owner.setHouseholdSize(householdSizeAfterCreate(owner.getHouseholdId()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarning());
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
        this.clinicService.saveOwner(currentOwner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(currentOwner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarning());
        return new ResponseEntity<>(ownerDto, HttpStatus.NO_CONTENT);
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
     * Normalizes a street address into the canonical form stored on and returned for a newly
     * created owner. A {@code null} value becomes the empty string; otherwise surrounding
     * whitespace is trimmed, internal runs of whitespace are collapsed to a single space, the
     * text is upper-cased, and common street-type abbreviations are expanded on a whole-word
     * basis ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The normalized
     * value is what gets persisted, echoed back, and used for every address comparison (household
     * duplicate detection and the shared household identifier).
     *
     * @param rawAddress the address value as supplied by the client
     * @return the normalized address (possibly empty, never {@code null})
     */
    private static String normalizeAddress(String rawAddress) {
        if (rawAddress == null) {
            return "";
        }
        String collapsed = rawAddress.trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            switch (token) {
                case "ST" -> token = "STREET";
                case "RD" -> token = "ROAD";
                case "AVE" -> token = "AVENUE";
                default -> { }
            }
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Builds the customer code assigned to a newly created owner. The code is formatted
     * {@code <CITY3>-<LAST3>-<NNNN>} where {@code CITY3} is the upper-cased first three letters
     * of the owner's city, {@code LAST3} is the upper-cased first three letters of the owner's
     * last name and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one more
     * than the number of owners already in that city (e.g. {@code LON-SMI-0007}).
     *
     * @param city the city of the owner being created
     * @param lastName the last name of the owner being created
     * @return the customer code to assign
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = letterPrefix3(city);
        String last3 = letterPrefix3(lastName);
        String normalizedCity = normalizeIdentity(city);
        long sequence = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeIdentity(existing.getCity()).equals(normalizedCity))
            .count() + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Returns the upper-cased first three letters of the given value, skipping any non-letter
     * characters. Values with fewer than three letters yield a shorter prefix.
     *
     * @param value the source text
     * @return the upper-cased letter prefix (at most three characters, never {@code null})
     */
    private static String letterPrefix3(String value) {
        StringBuilder prefix = new StringBuilder(3);
        if (value != null) {
            for (int i = 0; i < value.length() && prefix.length() < 3; i++) {
                char c = value.charAt(i);
                if (Character.isLetter(c)) {
                    prefix.append(Character.toUpperCase(c));
                }
            }
        }
        return prefix.toString();
    }

    /**
     * Normalizes a telephone into E.164 form. Spaces, dashes and brackets are stripped. A value
     * already carrying a leading {@code '+'} keeps its country code; otherwise country code
     * {@code +61} is assumed and a single leading {@code '0'} is dropped from the national digits.
     * The result must carry a leading {@code '+'} followed by 8 to 15 digits.
     *
     * @param rawTelephone the telephone value as supplied by the client
     * @return the telephone in E.164 form (e.g. {@code +61412345678})
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private static String normalizeTelephone(String rawTelephone) {
        if (rawTelephone == null) {
            throw new InvalidTelephoneException(rawTelephone);
        }
        String cleaned = rawTelephone.replaceAll("[\\s\\-()]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            String digits = cleaned.substring(1);
            if (!digits.matches("[0-9]+")) {
                throw new InvalidTelephoneException(rawTelephone);
            }
            e164 = "+" + digits;
        } else {
            if (!cleaned.matches("[0-9]+")) {
                throw new InvalidTelephoneException(rawTelephone);
            }
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = "+61" + national;
        }
        int digitCount = e164.length() - 1;
        if (digitCount < 8 || digitCount > 15) {
            throw new InvalidTelephoneException(rawTelephone);
        }
        return e164;
    }

    /**
     * Determines whether any existing owner already uses the given E.164 telephone. Stored
     * telephones are themselves E.164, so the values are compared directly.
     *
     * @param e164Telephone the E.164 telephone of the owner being created
     * @return {@code true} if another owner already has the same E.164 telephone
     */
    private boolean isTelephoneInUse(String e164Telephone) {
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(existing -> existing != null)
            .anyMatch(e164Telephone::equals);
    }

    /**
     * Determines whether any existing owner already uses the given email, compared case-insensitively
     * on its lower-cased value. Stored emails are themselves lower-cased, so the supplied value is
     * lower-cased and compared directly. A {@code null} or blank email is never considered in use.
     *
     * @param email the email of the owner being created, as supplied by the client
     * @return {@code true} if another owner already has the same lower-cased email
     */
    private boolean isEmailInUse(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String normalizedEmail = email.toLowerCase(java.util.Locale.ROOT);
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getEmail)
            .filter(existing -> existing != null)
            .anyMatch(normalizedEmail::equals);
    }

    /**
     * Determines whether any existing owner already shares a household with the owner being created,
     * i.e. has both the same last name and the same address. Both fields are compared
     * case-insensitively after collapsing runs of whitespace to a single space and trimming.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @return {@code true} if another owner already has the same last name and address
     */
    private boolean isHouseholdInUse(String lastName, String address) {
        String normalizedLastName = normalizeIdentity(lastName);
        String normalizedAddress = normalizeIdentity(address);
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizeIdentity(existing.getLastName()).equals(normalizedLastName)
                && normalizeIdentity(existing.getAddress()).equals(normalizedAddress));
    }

    /**
     * Counts how many existing owners already share the given first name and last name with the
     * owner being created. Both names are compared case-insensitively (after trimming and
     * collapsing runs of whitespace). The count reflects the state before the new owner is
     * persisted, so a first, otherwise-unique owner yields {@code 0}.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners sharing the same first and last name
     */
    private int namesakeCount(String firstName, String lastName) {
        String normalizedFirstName = normalizeIdentity(firstName);
        String normalizedLastName = normalizeIdentity(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeIdentity(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeIdentity(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    /**
     * Counts how many members the owner being created belongs to in its household, including
     * itself, as of this create. Members are the owners sharing the given {@code householdId};
     * the new owner is not yet persisted, so its own membership is added to the existing count.
     * An owner not assigned to a shared household ({@code householdId} is {@code null}) is a
     * household of one.
     *
     * @param householdId the shared household identifier assigned to the owner being created, or
     *                    {@code null} if the owner does not share a household
     * @return the number of household members after this create (at least 1)
     */
    private int householdSizeAfterCreate(String householdId) {
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> householdId.equals(owner.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * The maximum number of owners permitted in a single city. Once a city already contains this
     * many owners it is considered full and no further owners may be created there.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Determines whether the given city has already reached its capacity, i.e. already contains
     * {@value #CITY_CAPACITY} or more existing owners. Cities are compared case-insensitively after
     * collapsing runs of whitespace to a single space and trimming.
     *
     * @param city the city of the owner being created
     * @return {@code true} if the city already contains {@value #CITY_CAPACITY} or more owners
     */
    private boolean isCityAtCapacity(String city) {
        String normalizedCity = normalizeIdentity(city);
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeIdentity(existing.getCity()).equals(normalizedCity))
            .count();
        return count >= CITY_CAPACITY;
    }

    /**
     * The maximum number of owners that may be created on any single day. Once this many owners
     * already carry a {@code registrationDate} of the current day, no further owners may be created
     * that day.
     */
    private static final int DAILY_REGISTRATION_LIMIT = 100;

    /**
     * Determines whether the daily registration limit has already been reached, i.e. whether
     * {@value #DAILY_REGISTRATION_LIMIT} or more existing owners already carry the given date as
     * their {@code registrationDate}.
     *
     * @param date the current day
     * @return {@code true} if {@value #DAILY_REGISTRATION_LIMIT} or more owners were already
     *         registered on the given date
     */
    /**
     * Rolls a registration date forward onto a business day. A date that already falls on a
     * weekday is returned unchanged; a Saturday or Sunday is rolled forward to the following
     * Monday. This adjusted date is what gets stored as the {@code registrationDate} and drives
     * everything derived from it (the membership number's year segment, the daily create-limit).
     *
     * @param date the effective registration date (supplied by the client or defaulted to the
     *             server's current date)
     * @return the same date if it is a weekday, otherwise the following Monday
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    private boolean isDailyRegistrationLimitReached(LocalDate date) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
        return count >= DAILY_REGISTRATION_LIMIT;
    }

    /**
     * The number of owners that may be created on a single day before the bulk-signup warning is
     * raised. Once <em>more than</em> this many owners already carry the current business day as
     * their {@code registrationDate}, responses flag {@code bulkSignupWarning} true.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been
     * created today, i.e. already carry the current business day as their {@code registrationDate}.
     * This shares the daily create-limit's accumulation path (owners counted by registration date).
     *
     * @return {@code true} if more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners are already
     *         registered on today's business day
     */
    private boolean isBulkSignupWarning() {
        LocalDate today = toBusinessDay(LocalDate.now());
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Assigns the owner being created to the household identified by the given last name and
     * address and returns the household's stable shared identifier. The identifier is derived
     * deterministically from the normalized last name and address, so every member of the same
     * household resolves to the same value regardless of creation order. Any existing member that
     * does not yet carry the identifier is back-filled so all household members share it.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @return the stable household identifier shared by all members of the household
     */
    private String joinHousehold(String lastName, String address) {
        String householdId = householdIdFor(lastName, address);
        String normalizedLastName = normalizeIdentity(lastName);
        String normalizedAddress = normalizeIdentity(address);
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (normalizeIdentity(existing.getLastName()).equals(normalizedLastName)
                && normalizeIdentity(existing.getAddress()).equals(normalizedAddress)
                && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                this.clinicService.saveOwner(existing);
            }
        }
        return householdId;
    }

    /**
     * Derives the stable household identifier for a given last name and address. The value is the
     * first 16 upper-case hex characters of the SHA-256 digest of the normalized last name and
     * address, so it is stable across calls and identical for every member of the household.
     *
     * @param lastName the last name of the household
     * @param address the address of the household
     * @return the stable household identifier
     */
    private static String householdIdFor(String lastName, String address) {
        String key = normalizeIdentity(lastName) + "\n" + normalizeIdentity(address);
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 16).toUpperCase(java.util.Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a text field for household-identity comparison: {@code null} becomes the empty
     * string, surrounding whitespace is trimmed, internal runs of whitespace are collapsed to a
     * single space, and the result is lower-cased.
     *
     * @param value the raw field value
     * @return the normalized value used for case-insensitive, whitespace-insensitive comparison
     */
    private static String normalizeIdentity(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }
}
