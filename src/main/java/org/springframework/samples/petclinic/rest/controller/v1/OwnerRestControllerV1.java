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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityCapacityExceededException;
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
     * Syntactic email check: a non-empty local part, a single {@code @}, and a domain that contains
     * at least one dot. Whitespace and additional {@code @} characters are disallowed.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Disposable email domains that are not accepted for an owner. An email whose domain (the part
     * after the {@code @}, compared case-insensitively) is listed here is rejected with a 400.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Dedicated audit logger. Successful owner creation emits a single line here carrying the new
     * owner's id, customer code, registration date and membership level, so audit side-effects can
     * be observed independently of the HTTP response.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Common street-type abbreviations expanded during address normalization. Keys and values are
     * upper-cased, matching the state of the address after whitespace and case normalization.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Required national-number length per E.164 country calling code. After normalization the leading
     * country code is stripped and the remaining national digits must match the expected count exactly
     * (e.g. {@code +61} requires 9 national digits, {@code +1} requires 10). Country codes not listed
     * here are only subject to the generic E.164 length bounds.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        Map.of("1", 10, "61", 9);

    /**
     * Fixed city-to-region table used to validate an owner's postcode. Cities not listed have no
     * known region and accept any well-formed (4-digit) postcode.
     */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Inclusive 4-digit postcode range {@code [low, high]} allowed for each region: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099. A supplied postcode outside its city's region range is rejected.
     */
    private static final Map<String, int[]> REGION_POSTCODES =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

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
        validateRequiredOwnerFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        owner.setAddress(normalizeAddress(owner.getAddress()));
        validatePostcode(owner.getPostcode(), owner.getCity());
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        LocalDate effectiveRegistrationDate =
            owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(rollToBusinessDay(effectiveRegistrationDate));
        rejectDailyLimitReached(owner.getRegistrationDate());
        rejectCityAtCapacity(owner.getCity());
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        owner.setHouseholdId(householdId(owner.getLastName(), owner.getPostcode()));
        rejectDuplicateIdentity(owner, sharesHousehold);
        assignPossibleDuplicate(owner, normalizedTelephone, sharesHousehold);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(isBulkSignupDay(owner.getRegistrationDate()));
        owner.setHouseholdSize(countHousehold(owner.getHouseholdId()));
        owner.setCustomerCode(buildCustomerCode(owner, normalizedTelephone));
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerMapper.membershipLevel(owner), ownerMapper.membershipNumber(owner));
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
     * Counts how many existing owners share the given first and last name, compared
     * case-insensitively, at the moment before the new owner is persisted. The result is
     * stored on the owner as its {@code namesakeCount}, so it reflects the population as it
     * stood when the owner was created rather than being recomputed on later reads.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of pre-existing owners with the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> first.equalsIgnoreCase(existing.getFirstName())
                && last.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Builds the customer code for a newly created owner, formatted {@code '<REGION>-<HASH8>'} where
     * {@code REGION} is the region code derived from the owner's postcode (see {@link #deriveRegion})
     * and {@code HASH8} is the first 8 upper-case hex characters of the SHA-256 digest of the owner's
     * normalized telephone concatenated with the last name. For example an owner in the NSW postcode
     * range gets a code such as {@code 'NSW-3F2A1B9C'}.
     *
     * <p>When the computed code collides with an existing owner's customer code, it is de-duplicated
     * by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that makes the result
     * unique across all existing owners' customer codes. The de-duplicated code is what gets assigned.
     *
     * @param owner the owner being created, whose postcode, city and last name feed the code
     * @param normalizedTelephone the owner's E.164-normalized telephone
     * @return the assigned customer code, de-duplicated against existing owners if necessary
     */
    private String buildCustomerCode(Owner owner, String normalizedTelephone) {
        String region = deriveRegion(owner.getPostcode(), owner.getCity());
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String hash8 = sha256Hex8(normalizedTelephone + lastName);
        String baseCode = region + "-" + hash8;
        Set<String> existingCodes = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(code -> code != null)
            .collect(java.util.stream.Collectors.toSet());
        if (!existingCodes.contains(baseCode)) {
            return baseCode;
        }
        int n = 2;
        while (existingCodes.contains(baseCode + "-" + n)) {
            n++;
        }
        return baseCode + "-" + n;
    }

    /**
     * Derives an owner's region code, preferring the postcode: the region whose {@link #REGION_POSTCODES}
     * range contains the (4-digit) postcode is returned, falling back to the fixed {@link #CITY_REGION}
     * city table when the postcode is absent or in no known range, and finally to {@code "UNKNOWN"}.
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @param city the owner's city, used to resolve the region when the postcode does not
     * @return the canonical region string, or {@code "UNKNOWN"} when neither source resolves a region
     */
    private String deriveRegion(String postcode, String city) {
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            } catch (NumberFormatException ex) {
                // Not a numeric postcode; fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Computes the first 8 upper-case hex characters (the first 4 bytes) of the SHA-256 digest of the
     * given input's UTF-8 bytes.
     *
     * @param input the string to hash
     * @return the 8-character upper-case hex prefix of the SHA-256 digest
     */
    private String sha256Hex8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Rejects an owner payload that is missing or blank in any of the required fields
     * (firstName, lastName, address, city, telephone). The bean-validation constraints on
     * {@link OwnerFieldsDto} already reject {@code null} values and most malformed input, but
     * a present-yet-blank {@code address} or {@code city} would otherwise slip through, so this
     * guard enforces the rule uniformly for every required field.
     *
     * @param ownerFieldsDto the submitted owner payload
     * @throws InvalidOwnerFieldsException if one or more required fields are missing or blank
     */
    private void validateRequiredOwnerFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> invalidFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            invalidFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            invalidFields.add("lastName");
        }
        if (isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
            invalidFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            invalidFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            invalidFields.add("telephone");
        }
        if (!invalidFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(invalidFields);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Validates a supplied owner postcode. The postcode is optional: a {@code null} value is
     * accepted and the request stays backward-compatible. When present it must be a 4-digit value,
     * and must fall within the inclusive range allowed for the owner's city region per the fixed
     * {@link #CITY_REGION} / {@link #REGION_POSTCODES} tables (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099). A city with no known region accepts any 4-digit postcode. The value is stored
     * and returned exactly as given.
     *
     * @param postcode the submitted postcode, may be {@code null} when absent
     * @param city the owner's city, used to resolve the region whose range the postcode must satisfy
     * @throws InvalidOwnerFieldsException if a present postcode is not 4 digits or is out of range
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
        String region = city == null ? null : CITY_REGION.get(city);
        if (region == null) {
            return;
        }
        int[] range = REGION_POSTCODES.get(region);
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }

    /**
     * Normalizes a submitted telephone number into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading {@code '+'} and its country code are kept as-is; otherwise the country code {@code '+61'}
     * is assumed and a single leading {@code '0'} is dropped from the national digits. The result must be
     * a {@code '+'} followed by 8 to 15 digits. For example {@code "0412 345 678"} normalizes to
     * {@code "+61412345678"}. The E.164 string is what gets stored and returned.
     *
     * @param telephone the raw telephone value from the submitted owner payload
     * @return the normalized E.164 telephone number
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.strip();
        boolean hasCountryCode = raw.startsWith("+");
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (hasCountryCode) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        validateNationalNumberLength(digits);
        return "+" + digits;
    }

    /**
     * Validates that the national-number portion of a normalized E.164 number (the digits after the
     * country calling code) has the exact length required for that country. The country code is matched
     * against {@link #NATIONAL_NUMBER_LENGTHS} (e.g. {@code +61} requires 9 national digits, {@code +1}
     * requires 10). Numbers whose country code is not listed are left to the generic length bounds.
     *
     * @param digits the normalized E.164 digits, without the leading {@code '+'}
     * @throws InvalidOwnerFieldsException if the national-number length is wrong for the country code
     */
    private void validateNationalNumberLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_NUMBER_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidOwnerFieldsException(List.of("telephone"));
                }
                return;
            }
        }
    }

    /**
     * Best-effort conversion of an existing telephone value to E.164 form for duplicate comparison,
     * returning {@code null} when the value cannot form a valid E.164 number.
     *
     * @param telephone an existing owner's stored telephone value
     * @return the E.164 form, or {@code null} if it cannot be normalized
     */
    private String toE164OrNull(String telephone) {
        try {
            return normalizeTelephone(telephone);
        } catch (InvalidOwnerFieldsException ex) {
            return null;
        }
    }

    /**
     * Normalizes an optional owner email. A {@code null} value is left untouched (the field is optional).
     * When a value is present it must be a syntactically valid address (see {@link #EMAIL_PATTERN}); the
     * accepted value is trimmed and lower-cased before it is stored and returned.
     *
     * @param email the raw email value from the submitted owner payload, may be {@code null}
     * @return {@code null} if no email was supplied, otherwise the lower-cased email
     * @throws InvalidOwnerFieldsException if a value is present but not a syntactically valid address,
     *         or if its domain is on the disposable-domain blocklist ({@link #DISPOSABLE_EMAIL_DOMAINS})
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return normalized;
    }

    /**
     * Rejects creating an owner that would be a second member of an existing household. The household
     * is keyed on {@code (lastName, postcode)} through the deterministic {@link #householdId}, so any
     * existing owner carrying the same {@code householdId} makes the owner being created a household
     * duplicate. Such an owner is rejected with a 409, <em>unless</em> the request opts into
     * {@code sharesHousehold}: a declared household member deliberately bypasses this block and is
     * created as an additional member of the household.
     *
     * @param owner the owner being created, whose computed household id is matched
     * @param sharesHousehold whether the request declared the owner a shared-household member
     * @throws DuplicateOwnerIdentityException if another owner already belongs to the same household
     */
    private void rejectDuplicateIdentity(Owner owner, boolean sharesHousehold) {
        if (sharesHousehold) {
            return;
        }
        String householdId = owner.getHouseholdId();
        boolean duplicate = householdId != null && this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (duplicate) {
            throw new DuplicateOwnerIdentityException(householdId);
        }
    }

    /**
     * Flags a soft ("possible") duplicate on the owner being created. Unlike a hard duplicate (which is
     * rejected outright by {@link #rejectDuplicateIdentity}), a soft duplicate is still created: it is an
     * owner that is not a hard duplicate but shares an existing owner's last name (compared
     * case-insensitively, see {@link #normalizeForComparison}) and postcode while carrying a <em>different</em>
     * telephone. When such an existing owner is found, {@code possibleDuplicate} is set {@code true} and
     * {@code possibleDuplicateOf} is set to that owner's id (the earliest by id when several match); otherwise
     * {@code possibleDuplicate} is {@code false} and {@code possibleDuplicateOf} stays {@code null}. Both values
     * are snapshotted on the owner so they reflect the population as it stood when the owner was created.
     *
     * <p>A declared household member (a request that set {@code sharesHousehold} to bypass the household
     * duplicate block) is never a <em>suspected</em> duplicate: it is knowingly created as another member
     * of the household, so {@code possibleDuplicate} is left {@code false} and {@code possibleDuplicateOf}
     * {@code null} without consulting the existing population.
     *
     * @param owner the owner being created, whose last name and postcode are matched
     * @param normalizedTelephone the E.164 telephone of the owner being created
     * @param sharesHousehold whether the request declared the owner a shared-household member
     */
    private void assignPossibleDuplicate(Owner owner, String normalizedTelephone, boolean sharesHousehold) {
        if (sharesHousehold) {
            owner.setPossibleDuplicateOf(null);
            owner.setPossibleDuplicate(false);
            return;
        }
        String postcode = owner.getPostcode();
        Integer matchId = null;
        if (postcode != null) {
            String lastName = normalizeForComparison(owner.getLastName());
            for (Owner existing : this.clinicService.findAllOwners()) {
                if (postcode.equals(existing.getPostcode())
                    && lastName.equals(normalizeForComparison(existing.getLastName()))
                    && !normalizedTelephone.equals(toE164OrNull(existing.getTelephone()))
                    && existing.getId() != null
                    && (matchId == null || existing.getId() < matchId)) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicateOf(matchId);
        owner.setPossibleDuplicate(matchId != null);
    }

    /**
     * The maximum number of owners a single city may contain. Once a city already holds this many
     * owners, creating another owner in that city is rejected.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Rejects creating an owner in a city that already contains {@link #MAX_OWNERS_PER_CITY} or more
     * owners. Existing owners' cities are compared case-insensitively with surrounding and repeated
     * internal whitespace collapsed (see {@link #normalizeForComparison}), matching the per-city
     * counting used elsewhere.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityCapacityExceededException if the city already contains the maximum number of owners
     */
    /**
     * The maximum number of owners that may be registered on a single day. Once this many owners
     * already share a {@code registrationDate}, creating another owner for that day is rejected.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Rejects creating an owner on a day that has already reached {@link #MAX_OWNERS_PER_DAY} or more
     * owner registrations. Existing owners are counted by their {@code registrationDate} matching the
     * registration date of the owner being created.
     *
     * @param registrationDate the registration date of the owner being created
     * @throws DailyOwnerLimitExceededException if the day has already reached the maximum number of owners
     */
    /**
     * Rolls an effective registration date forward onto a business day. A registration date must fall
     * on a weekday: when the supplied or defaulted date is a Saturday or Sunday it is rolled forward to
     * the next Monday and that adjusted date becomes the owner's {@code registrationDate}. A weekday date
     * is returned unchanged. Every value derived from the registration date (the daily create-limit
     * count, the membership number's year segment, and so on) uses this adjusted date.
     *
     * @param date the effective registration date (supplied in the request or defaulted to the server date)
     * @return the same date if it is a weekday, otherwise the following Monday
     */
    /**
     * Rejects creating an owner whose supplied {@code registrationDate} is later than the server's
     * current date. A registration date is not permitted to lie in the future; a {@code null} date
     * (defaulted to the server date) and any date on or before today are accepted. The check runs
     * against the date exactly as supplied, before it is rolled forward onto a business day.
     *
     * @param registrationDate the registration date supplied in the request, may be {@code null}
     * @throws FutureRegistrationDateException if the supplied date is after the server date
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }

    private LocalDate rollToBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    private void rejectDailyLimitReached(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (ownersToday >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
    }

    /**
     * The number of owners that must already share a {@code registrationDate} before a newly created
     * owner for that day is flagged with a bulk-signup warning. Once more than this many owners have
     * been created for the day, the new owner's {@code bulkSignupWarning} is {@code true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether the owner being created should carry a bulk-signup warning. The warning is
     * raised when more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created for
     * the same registration date at the time this owner is created. Existing owners are counted by their
     * {@code registrationDate} matching the registration date of the owner being created, mirroring the
     * daily create-limit accumulation.
     *
     * @param registrationDate the registration date of the owner being created
     * @return {@code true} if the day already holds more than the threshold number of owners
     */
    private boolean isBulkSignupDay(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return ownersToday > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    private void rejectCityAtCapacity(String city) {
        String normalizedCity = normalizeForComparison(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedCity.equals(normalizeForComparison(existing.getCity())))
            .count();
        if (ownersInCity >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityCapacityExceededException(city);
        }
    }

    /**
     * Counts how many owners belong to the household the owner being created has just been assigned to,
     * inclusive of that owner. Existing owners are matched by their {@code householdId} equalling the
     * given identifier; the owner being created is added on because it is counted before being persisted.
     * An owner that did not opt into a shared household (its {@code householdId} is {@code null}) is a
     * household of one. The value is snapshotted on the owner.
     *
     * @param householdId the household identifier assigned to the owner being created, may be {@code null}
     * @return the number of household members, including the owner being created
     */
    private int countHousehold(String householdId) {
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> householdId.equals(owner.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * Derives the deterministic household identifier for a given last name and postcode: the first 12
     * hex characters of the SHA-256 digest of {@code '<normalizedLastName>|<postcode>'}, where the last
     * name is normalized for case- and whitespace-insensitive comparison (see
     * {@link #normalizeForComparison}) and a {@code null} postcode contributes an empty segment. Every
     * owner that shares a last name and postcode therefore resolves to the exact same identifier,
     * regardless of creation order, making them members of one household.
     *
     * @param lastName the owner's last name
     * @param postcode the owner's postcode, may be {@code null}
     * @return the deterministic household identifier
     */
    private String householdId(String lastName, String postcode) {
        String key = normalizeForComparison(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Normalizes a value for case-insensitive, whitespace-insensitive comparison: leading and trailing
     * whitespace is stripped, every run of internal whitespace is collapsed to a single space, and the
     * result is lower-cased. A {@code null} value normalizes to the empty string.
     *
     * @param value the value to normalize, may be {@code null}
     * @return the normalized value
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner address for storage and comparison: leading and trailing whitespace is
     * stripped, every run of internal whitespace is collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). For example {@code "  12  main  st "} normalizes to
     * {@code "12 MAIN STREET"}. A {@code null} value normalizes to the empty string. This is the form
     * that is stored, returned, and used for every address comparison (household duplicate detection
     * and the shared household id).
     *
     * @param address the raw address value from the submitted owner payload, may be {@code null}
     * @return the normalized address
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
