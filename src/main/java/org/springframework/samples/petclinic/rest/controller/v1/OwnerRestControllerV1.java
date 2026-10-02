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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitReachedException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityAtCapacityException;
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
     * Syntactic check for an email address: a non-empty local part, a single {@code @}, and a
     * domain containing at least one dot, with no whitespace in any part.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Dedicated audit logger. On a successful owner create an audit line is emitted here carrying the
     * new owner's id, customer code and registration date.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Common street-type abbreviations expanded during address normalization. Keys are the
     * upper-cased abbreviation as it appears as a whole whitespace-delimited token; values are the
     * expanded form it is replaced with.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * The maximum number of owners a single city may contain. Once a city holds this many owners it
     * is at capacity and cannot take any more.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * The maximum number of owners that may be registered in a single day. Once this many owners
     * already carry today's {@code registrationDate}, the day's quota is exhausted and no further
     * owners can be created that day.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * The number of owners that may be registered on a single business day before a response starts
     * carrying a {@code bulkSignupWarning}. Once <em>more than</em> this many owners already carry the
     * current business day's {@code registrationDate}, that day's signups are flagged as unusually
     * high volume.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * E.164 country codes mapped to the exact number of national (significant) digits a telephone for
     * that country must carry. For example {@code '+61'} (Australia) requires 9 national digits and
     * {@code '+1'} (NANP) requires 10. A country code listed here enforces that national-number length;
     * a number whose country code is not listed is accepted at any otherwise-valid E.164 length.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of(
        "61", 9,
        "1", 10);

    /**
     * Fixed region-to-postcode-range lookup used both to validate an owner's optional postcode against
     * the region derived from the owner's city ({@link #regionForCity}) and to derive the postcode's own
     * region for the {@code customerCode} ({@link #regionForPostcode}). Each value is the inclusive
     * {@code {low, high}} 4-digit range for that region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A
     * region that is not listed ({@code 'UNKNOWN'}) carries no range and accepts any 4-digit postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed city-to-region lookup used to validate an owner's optional postcode against the region
     * derived from the owner's city. Any city not listed has region {@code 'UNKNOWN'} and so accepts
     * any 4-digit postcode.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

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
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        applyBulkSignupWarning(ownerDto);
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        validateRequiredFields(ownerFieldsDto);
        rejectFutureRegistrationDate(ownerFieldsDto.getRegistrationDate());
        LocalDate registrationDate = toBusinessDay(
            ownerFieldsDto.getRegistrationDate() != null ? ownerFieldsDto.getRegistrationDate() : LocalDate.now());
        rejectDailyLimitReached(registrationDate);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String address = normalizeAddress(ownerFieldsDto.getAddress());
        owner.setAddress(address);
        rejectCityAtCapacity(owner.getCity());
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            assignHousehold(owner, ownerFieldsDto.getLastName(), address);
        }
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        owner.setTelephone(telephone);
        String email = normalizeEmail(ownerFieldsDto.getEmail());
        owner.setEmail(email);
        owner.setPostcode(validatePostcode(ownerFieldsDto.getPostcode(), regionForCity(owner.getCity())));
        rejectDuplicateIdentity(owner);
        applyPossibleDuplicate(owner);
        owner.setRegistrationDate(registrationDate);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setCustomerCode(generateCustomerCode(owner.getPostcode(), owner.getTelephone(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        applyBulkSignupWarning(ownerDto);
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
     * Rejects an owner payload that is missing or blank in any required field. Bean Validation
     * already rejects {@code null} values and some formats, but a whitespace-only value can still
     * satisfy a {@code @Size(min = 1)} constraint; this guard catches those remaining cases.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if any required field is missing or blank, carrying the
     *                                     name of every offending field
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
            missingFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(missingFields);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Rejects a supplied {@code registrationDate} that is later than the current server date. A
     * {@code null} value means the field was omitted and the registration date will default to the
     * server date, so there is nothing to reject. A supplied date on or before today is accepted; the
     * raw supplied value is compared against the server date before any business-day adjustment.
     *
     * @param registrationDate the registration date supplied by the client, or {@code null} if omitted
     * @throws InvalidOwnerFieldsException if the supplied date is later than the server date, yielding a
     *                                     400 Bad Request response
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidOwnerFieldsException(List.of("registrationDate"));
        }
    }

    /**
     * Validates an owner's optional postcode. A {@code null} value means the field was omitted and is
     * left unset, keeping the create contract backward-compatible. When a value is present it must be
     * exactly 4 digits, and when the owner's region (derived from its city, {@link #regionForCity})
     * has a known postcode range in {@link #REGION_POSTCODES} the postcode must fall within that
     * inclusive range. A region with no known range ({@code 'UNKNOWN'}) accepts any 4-digit postcode.
     * The returned value is stored and returned unchanged.
     *
     * @param postcode the raw postcode value submitted by the client, or {@code null} if omitted
     * @param region   the owner's region derived from its city
     * @return the postcode to store, or {@code null} if none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but not a valid 4-digit postcode, or is
     *                                     out of range for the city's region, yielding a 400 response
     */
    private String validatePostcode(String postcode, String region) {
        if (postcode == null) {
            return null;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
        int[] range = REGION_POSTCODES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidOwnerFieldsException(List.of("postcode"));
            }
        }
        return postcode;
    }

    /**
     * Normalizes a telephone number for owner creation into E.164 form. Spaces, dashes and brackets
     * are stripped. When the value carries a leading {@code '+'} the country code it supplies is kept;
     * otherwise the number is treated as a national Australian number, so a single leading {@code '0'}
     * is dropped and the {@code '+61'} country code is prepended. The result must be a {@code '+'}
     * followed by 8 to 15 digits. This E.164 string is what gets stored and returned.
     *
     * @param telephone the raw telephone value submitted by the client
     * @return the telephone in E.164 form, e.g. {@code +61412345678}
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number, yielding a
     *                                     400 response
     */
    private String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Converts a raw telephone value to E.164 form, or returns {@code null} if it cannot form a valid
     * E.164 number. Spaces, dashes and brackets are removed; a leading {@code '+'} keeps the supplied
     * country code, while a bare national number has a single leading {@code '0'} dropped and the
     * {@code '+61'} country code prepended. A valid result is a {@code '+'} followed by 8 to 15 digits,
     * and when the supplied country code is one with a known national-number length
     * ({@link #NATIONAL_NUMBER_LENGTHS}, e.g. {@code '+61'} => 9, {@code '+1'} => 10) the national
     * number must have exactly that many digits.
     *
     * @param telephone the raw telephone value, possibly {@code null}
     * @return the E.164 string, or {@code null} if the value is not a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String digits = (hasPlus ? cleaned.substring(1) : cleaned).replaceAll("\\D", "");
        String nationalSignificant = hasPlus
            ? digits
            : "61" + (digits.startsWith("0") ? digits.substring(1) : digits);
        if (!nationalSignificant.matches("[0-9]{8,15}")) {
            return null;
        }
        if (!hasValidNationalNumberLength(nationalSignificant)) {
            return null;
        }
        return "+" + nationalSignificant;
    }

    /**
     * Checks the national-number length of an E.164 number (country code plus national significant
     * digits, no leading {@code '+'}) against the per-country requirement in
     * {@link #NATIONAL_NUMBER_LENGTHS}. The longest listed country code that prefixes the number
     * selects the requirement, and the remaining digits must number exactly that many. A number whose
     * country code is not listed carries no per-country length requirement and is accepted.
     *
     * @param nationalSignificant the digits of the E.164 number, country code first, without the
     *                            leading {@code '+'}
     * @return {@code true} if the national-number length is correct for the country code, or the
     *         country code has no listed requirement; {@code false} otherwise
     */
    private boolean hasValidNationalNumberLength(String nationalSignificant) {
        String countryCode = NATIONAL_NUMBER_LENGTHS.keySet().stream()
            .filter(nationalSignificant::startsWith)
            .max(Comparator.comparingInt(String::length))
            .orElse(null);
        if (countryCode == null) {
            return true;
        }
        int nationalLength = nationalSignificant.length() - countryCode.length();
        return nationalLength == NATIONAL_NUMBER_LENGTHS.get(countryCode);
    }

    /**
     * Normalizes an optional owner email address. A {@code null} value means the field was omitted and
     * is left unset. When a value is present it must be a syntactically valid address; the stored and
     * returned value is that address lower-cased.
     *
     * @param email the raw email value submitted by the client, or {@code null} if omitted
     * @return the lower-cased email, or {@code null} if none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but not a syntactically valid address,
     *                                     yielding a 400 response
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return email.toLowerCase(Locale.ROOT);
    }

    /**
     * Counts the existing owners that, before this create, already share the given first name and
     * last name. Both names are compared case-insensitively with surrounding and internal whitespace
     * collapsed, consistent with the other owner-name comparisons. The owner being created has not
     * been saved yet, so it is never included in the count.
     *
     * @param firstName the first name of the owner being created
     * @param lastName  the last name of the owner being created
     * @return the number of existing owners sharing both names
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeForComparison(firstName);
        String normalizedLastName = normalizeForComparison(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeForComparison(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    /**
     * Builds the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'}. {@code REGION} is
     * the region code derived from the owner's postcode ({@link #regionForPostcode}, {@code 'UNKNOWN'}
     * when the postcode is absent or in no known range), and {@code HASH8} is the first eight
     * upper-cased hex characters of the SHA-256 digest of the owner's normalized telephone followed by
     * the last name (e.g. {@code 'NSW-1A2B3C4D'}). The identity carries no sequence number, so the
     * same postcode region, telephone and last name always yield the same customer code.
     *
     * @param postcode  the owner's validated postcode, or {@code null} if none was supplied
     * @param telephone the owner's normalized (E.164) telephone
     * @param lastName  the owner's last name
     * @return the formatted customer code
     */
    private String generateCustomerCode(String postcode, String telephone, String lastName) {
        String region = regionForPostcode(postcode);
        String hash8 = hash8(telephone + lastName);
        return region + "-" + hash8;
    }

    /**
     * Resolves the region code for a postcode by its known 4-digit range in {@link #REGION_POSTCODES}
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), returning {@code 'UNKNOWN'}
     * when the postcode is {@code null}, not exactly 4 digits, or falls in no known range. This is the
     * {@code <REGION>} segment baked into the {@code customerCode}.
     *
     * @param postcode the owner's postcode, or {@code null}
     * @return the region code, or {@code 'UNKNOWN'}
     */
    private String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return "UNKNOWN";
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return "UNKNOWN";
    }

    /**
     * Resolves the region code for a city via the fixed {@link #CITY_REGION} table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}), returning {@code 'UNKNOWN'}
     * when the city is not listed. Used to validate an owner's optional postcode against its city's
     * region.
     *
     * @param city the owner's city
     * @return the region code, or {@code 'UNKNOWN'}
     */
    private String regionForCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Returns the first eight upper-cased hex characters of the SHA-256 digest of the given input,
     * used as the {@code <HASH8>} segment of a {@code customerCode}.
     *
     * @param input the value to hash
     * @return the first eight upper-cased hex characters of its SHA-256 digest
     */
    private String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 8).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Counts the existing owners already in the given city, used to derive the per-city sequence of a
     * {@code customerCode}. Cities are compared case-insensitively with surrounding and internal
     * whitespace collapsed, consistent with the other owner-field comparisons. The owner being created
     * has not been saved yet, so it is never included in the count.
     *
     * @param city the city of the owner being created
     * @return the number of existing owners in that city
     */
    private int countOwnersInCity(String city) {
        String normalizedCity = normalizeForComparison(city);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getCity()).equals(normalizedCity))
            .count();
    }

    /**
     * Rejects creation of an owner whose city already contains the maximum number of owners
     * ({@value #CITY_CAPACITY}). Owners are counted per city case-insensitively with surrounding and
     * internal whitespace collapsed, consistent with the other owner-field comparisons.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityAtCapacityException if the city already contains {@value #CITY_CAPACITY} or more
     *                                      owners, yielding a 409 Conflict response
     */
    private void rejectCityAtCapacity(String city) {
        if (countOwnersInCity(city) >= CITY_CAPACITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }

    /**
     * Rejects creation of an owner once the given business day has already reached its daily quota
     * of {@value #DAILY_OWNER_LIMIT} owners. Owners are counted by their {@code registrationDate}
     * being that day — the adjusted business day the owner being created will carry — so the quota
     * applies per business day. The owner being created has not been saved yet, so it is never
     * included in the count.
     *
     * @param registrationDate the adjusted business-day registration date of the owner being created
     * @throws DailyOwnerLimitReachedException if {@value #DAILY_OWNER_LIMIT} or more owners already
     *                                         carry that registration date, yielding a
     *                                         429 Too Many Requests response
     */
    private void rejectDailyLimitReached(LocalDate registrationDate) {
        if (countOwnersRegisteredOn(registrationDate) >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitReachedException(registrationDate);
        }
    }

    /**
     * Counts the existing owners that carry the given day as their {@code registrationDate} — the same
     * per-business-day bucket the daily create-limit and the bulk-signup warning both accumulate over.
     *
     * @param registrationDate the business day to count owners for
     * @return the number of existing owners registered on that day
     */
    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Sets the {@code bulkSignupWarning} flag on an owner response. The flag is {@code true} when more
     * than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry the current business day's
     * {@code registrationDate} — the business day a create would land on right now — and {@code false}
     * otherwise.
     *
     * @param ownerDto the owner response to flag
     */
    private void applyBulkSignupWarning(OwnerDto ownerDto) {
        LocalDate today = toBusinessDay(LocalDate.now());
        ownerDto.setBulkSignupWarning(countOwnersRegisteredOn(today) > BULK_SIGNUP_WARNING_THRESHOLD);
    }

    /**
     * Rolls a registration date forward to a business day. A date that falls on a Saturday or Sunday
     * is rolled forward to the next Monday; a date already on a weekday is returned unchanged. This
     * applies to the effective registration date whether it was supplied in the request or defaulted
     * to the server date, and the adjusted value is what gets stored as {@code registrationDate} and
     * feeds every value derived from it (such as the membership number's year segment and the daily
     * create-limit's per-day bucket).
     *
     * @param date the effective registration date, supplied or defaulted
     * @return the same date when it is a weekday, otherwise the next Monday
     */
    private LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (adjusted.getDayOfWeek() == DayOfWeek.SATURDAY
            || adjusted.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    /**
     * Rejects creation of an owner whose derived {@code identityKey} exactly matches an existing
     * owner's. The identity key — {@code '<normalizedTelephone>|<email or empty>|<householdId or
     * empty>'} — is the single value that consolidates all duplicate detection: the previously
     * separate telephone, email and household checks are all expressed through it. Because the
     * telephone is part of the key, two members of the same household (same {@code householdId})
     * with different telephones have different identity keys and are both allowed; only a whole-key
     * match is a duplicate. The owner being created already carries its normalized telephone,
     * email and (if any) household id, and existing owners store the same normalized forms, so the
     * keys are compared directly.
     *
     * @param owner the owner being created, with its telephone, email and household id already set
     * @throws DuplicateOwnerIdentityException if another owner already has the same identity key,
     *                                         yielding a 409 Conflict response
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getIdentityKey)
            .anyMatch(identityKey::equals);
        if (inUse) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * Flags the owner being created as a possible (soft) duplicate. A possible duplicate is an owner
     * that is not a hard duplicate (its identity key is unique, so it has already passed
     * {@link #rejectDuplicateIdentity}) yet shares an existing owner's last name and postcode while
     * carrying a different telephone. Last names are compared case-insensitively with collapsed
     * whitespace, consistent with the other owner-field comparisons, and postcodes must both be
     * present and equal. When such a match exists the owner is still created, with
     * {@code possibleDuplicate} set to {@code true} and {@code possibleDuplicateOf} set to the matching
     * owner's id (the earliest matching owner by id); otherwise {@code possibleDuplicate} is
     * {@code false} and {@code possibleDuplicateOf} is left unset.
     *
     * @param owner the owner being created, with its last name, postcode and telephone already set
     */
    private void applyPossibleDuplicate(Owner owner) {
        String normalizedLastName = normalizeForComparison(owner.getLastName());
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Owner match = null;
        if (postcode != null) {
            match = this.clinicService.findAllOwners().stream()
                .filter(existing -> normalizeForComparison(existing.getLastName()).equals(normalizedLastName)
                    && postcode.equals(existing.getPostcode())
                    && !Objects.equals(telephone, existing.getTelephone()))
                .min(Comparator.comparingInt(Owner::getId))
                .orElse(null);
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        } else {
            owner.setPossibleDuplicate(false);
        }
    }

    /**
     * Finds every existing owner belonging to the household identified by the given last name and
     * address — that is, the owners the {@code sharesHousehold} opt-in joins. Both the last name and
     * the address are compared case-insensitively with surrounding and internal whitespace collapsed,
     * so values that differ only in letter case or spacing still match. This definition of household
     * membership is used by {@link #assignHousehold}.
     *
     * @param lastName the last name of the owner being created
     * @param address  the address of the owner being created
     * @return the existing owners in that household, or an empty list if there are none
     */
    private List<Owner> findHouseholdMembers(String lastName, String address) {
        String normalizedLastName = normalizeForComparison(lastName);
        String normalizedAddress = normalizeAddress(address);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getLastName()).equals(normalizedLastName)
                && normalizeAddress(existing.getAddress()).equals(normalizedAddress))
            .toList();
    }

    /**
     * Enrolls a newly created owner into the household of any existing owner that shares its last
     * name and address, so an owner that opts in via {@code sharesHousehold} is accepted instead of
     * rejected as a duplicate. Last name and address are compared case-insensitively with collapsed
     * whitespace. When at least one such owner already exists, every member of the household — the
     * existing owners and the new one — is assigned the same stable {@code householdId}; if a member
     * already carries one it is reused, otherwise a new identifier derived from the shared last name
     * and address is created and back-filled onto the existing members. When no owner yet lives at
     * that address under the same last name no identifier is assigned and the owner is created alone.
     *
     * @param newOwner the owner being created, whose {@code householdId} will be set on a match
     * @param lastName the last name of the owner being created
     * @param address  the address of the owner being created
     * @return the assigned {@code householdId}, or {@code null} if there was no household to join
     */
    private String assignHousehold(Owner newOwner, String lastName, String address) {
        List<Owner> members = findHouseholdMembers(lastName, address);
        if (members.isEmpty()) {
            return null;
        }
        String householdId = members.stream()
            .map(Owner::getHouseholdId)
            .filter(existing -> existing != null && !existing.isBlank())
            .findFirst()
            .orElseGet(() -> generateHouseholdId(normalizeForComparison(lastName), normalizeAddress(address)));
        for (Owner member : members) {
            if (member.getHouseholdId() == null || member.getHouseholdId().isBlank()) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
        }
        newOwner.setHouseholdId(householdId);
        return householdId;
    }

    /**
     * Derives a stable household identifier, formatted {@code 'HH-<HEX12>'}, where {@code HEX12} is
     * the first twelve upper-cased hex characters of the SHA-256 digest of the normalized last name
     * and address joined by a {@code '|'}. The same last name and address therefore always yield the
     * same identifier, so members of one household share a single value.
     *
     * @param normalizedLastName the household's last name, already normalized for comparison
     * @param normalizedAddress  the household's address, already normalized for comparison
     * @return the derived household identifier
     */
    private String generateHouseholdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((normalizedLastName + "|" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return "HH-" + hex.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Normalizes a text value for case- and whitespace-insensitive comparison: surrounding
     * whitespace is trimmed, every internal run of whitespace is collapsed to a single space, and
     * the result is lower-cased.
     *
     * @param value the value to normalize, possibly {@code null}
     * @return the normalized value, or an empty string if {@code value} is {@code null}
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner address for storage, display and comparison. Surrounding whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations appearing as whole tokens are expanded
     * ({@code ST} to {@code STREET}, {@code RD} to {@code ROAD}, {@code AVE} to {@code AVENUE}).
     * This normalized value is what gets stored and returned, and it is the form every address
     * comparison (household duplicate detection and the shared household id) uses.
     *
     * @param address the raw address value submitted by the client, possibly {@code null}
     * @return the normalized address, or an empty string if {@code address} is {@code null}
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
