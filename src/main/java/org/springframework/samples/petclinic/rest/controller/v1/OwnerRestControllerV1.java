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
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidPostcodeException;
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
     * Matches a syntactically valid email: a non-empty local part and domain separated by a single
     * '@', with at least one dot-separated label in the domain and no whitespace anywhere.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Dedicated audit logger. On a successful owner create an audit line is emitted carrying the
     * new owner's id, customerCode, registrationDate and membershipLevel.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Common street-type abbreviations expanded during address normalization, keyed by the
     * upper-cased abbreviation.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Maximum number of owners a single city may contain. A create is rejected once the owner's
     * city already holds this many owners.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Maximum number of owners that may be created on a single day. A create is rejected once this
     * many owners already carry the current day as their {@code registrationDate}.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

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
        validateRequiredFields(ownerFieldsDto);
        validatePostcode(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        LocalDate effectiveDate = owner.getRegistrationDate() == null
            ? LocalDate.now()
            : owner.getRegistrationDate();
        owner.setRegistrationDate(toBusinessDay(effectiveDate));
        rejectWhenDailyLimitReached(owner.getRegistrationDate());
        rejectWhenCityAtCapacity(owner.getCity());
        applyHousehold(owner);
        owner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        rejectDuplicateIdentity(owner);
        owner.setCustomerCode(customerCode(owner));
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel());
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Builds the owner's customer code, formatted {@code <REGION>-<HASH8>} where REGION is the region
     * code derived from the owner's postcode (falling back to the city-to-region table, then
     * {@code UNKNOWN}) and HASH8 is the first 8 upper-cased hex characters of the SHA-256 of the
     * owner's normalized telephone concatenated with its last name (e.g. {@code NSW-1A2B3C4D}). The
     * code is a pure function of the owner's region and identity, carrying no sequence number.
     *
     * @param owner the owner being created, with its normalized telephone already applied
     * @return the assigned customer code
     */
    private String customerCode(Owner owner) {
        return regionCode(owner) + "-" + identityHash8(owner.getTelephone(), owner.getLastName());
    }

    /**
     * Derives the region code embedded in an owner's customer code, preferring the postcode: a
     * 4-digit postcode falling in a known region's range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099) yields that region. Otherwise the city-to-region table (Sydney->NSW,
     * Melbourne->VIC, Brisbane->QLD) is consulted, returning {@code UNKNOWN} when neither the
     * postcode nor the city identifies a region.
     *
     * @param owner the owner being created
     * @return the derived region code, never {@code null}
     */
    private String regionCode(Owner owner) {
        String fromPostcode = regionFromPostcode(owner.getPostcode());
        if (fromPostcode != null) {
            return fromPostcode;
        }
        String fromCity = region(owner.getCity());
        return fromCity == null ? "UNKNOWN" : fromCity;
    }

    /**
     * Resolves the region whose postcode range contains the given postcode, or {@code null} when the
     * postcode is absent, not 4 digits, or in no known range.
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @return the region code, or {@code null} when no known range contains the postcode
     */
    private String regionFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Computes the identity hash embedded in an owner's customer code: the first 8 upper-cased hex
     * characters of the SHA-256 of the owner's normalized telephone concatenated with its last name.
     *
     * @param telephone the owner's normalized (E.164) telephone
     * @param lastName  the owner's last name
     * @return the 8-character upper-cased hex hash
     */
    private String identityHash8(String telephone, String lastName) {
        String input = (telephone == null ? "" : telephone) + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 8).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Builds the owner's membership number, formatted {@code <customerCode>-M<YY>} where YY is the
     * last two digits of the registrationDate year, zero-padded (e.g. {@code NSW-1A2B3C4D-M26}).
     *
     * @param customerCode     the owner's assigned customer code
     * @param registrationDate the owner's registration date
     * @return the assigned membership number
     */
    private String membershipNumber(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /**
     * Counts the existing owners that already share the given firstName and lastName, compared
     * case-insensitively. Evaluated before the owner being created is saved, so it reflects only
     * the owners that pre-existed this create.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of existing namesakes
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getFirstName(), firstName)
                && equalsIgnoreCase(existing.getLastName(), lastName))
            .count();
    }

    /**
     * Rejects creating an owner when their city already contains {@link #CITY_CAPACITY} or more
     * owners. Existing owners are counted case-insensitively by city, matching how the per-city
     * customer-code sequence is derived.
     *
     * @param city the new owner's city
     * @throws CityAtCapacityException if the city already holds {@link #CITY_CAPACITY} owners
     */
    private void rejectWhenCityAtCapacity(String city) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getCity(), city))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * Rejects creating an owner once {@link #DAILY_OWNER_LIMIT} or more owners already carry the
     * given business day as their {@code registrationDate}. The day is the owner's adjusted,
     * business-day registration date, so the limit is enforced per business day and weekend
     * requests count against the Monday they roll forward to.
     *
     * @param registrationDate the owner's adjusted (business-day) registration date
     * @throws DailyOwnerLimitException if {@link #DAILY_OWNER_LIMIT} owners already carry that
     *                                  registration date
     */
    private void rejectWhenDailyLimitReached(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitException(registrationDate);
        }
    }

    /**
     * Rejects creating an owner whose supplied {@code registrationDate} lies in the future, i.e. is
     * later than the current server date. A {@code null} registration date is accepted (it defaults
     * to the server date) and today or any past date is allowed; only a future value is rejected.
     *
     * @param registrationDate the registration date supplied on the request, may be {@code null}
     * @throws FutureRegistrationDateException if the supplied date is later than the server date
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }

    /**
     * Rolls a registration date forward onto a business day: when it falls on a Saturday or Sunday
     * it advances to the following Monday, otherwise it is returned unchanged. Applied to the
     * effective registration date (whether supplied in the request or defaulted to the server date)
     * so a weekend value never persists as an owner's {@code registrationDate}.
     *
     * @param date the effective registration date
     * @return the same date when it is a weekday, otherwise the next Monday
     */
    private LocalDate toBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    /**
     * Rejects an owner whose required fields are missing or blank. Collects the names of every
     * offending field so the client learns about all of them at once.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws MissingOwnerFieldsException if firstName, lastName, address, city or telephone is
     *                                     missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> errors = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            errors.add("lastName");
        }
        if (isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
            errors.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            errors.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            errors.add("telephone");
        }
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * The fixed inclusive 4-digit postcode range allowed for each region, keyed by region code
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's postcode, but only when one is supplied. A {@code null} or blank postcode
     * is optional and accepted, keeping the create contract backward-compatible. When present the
     * value must be 4 digits and, when the owner's city has a known region (Sydney->NSW,
     * Melbourne->VIC, Brisbane->QLD), must fall within that region's inclusive range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
     *
     * @param postcode the raw postcode value as submitted, may be {@code null}
     * @param city     the owner's city, used to resolve the region whose range applies
     * @throws InvalidPostcodeException if a supplied postcode is not 4 digits or is out of range for
     *                                  the city's region
     */
    private void validatePostcode(String postcode, String city) {
        if (isBlank(postcode)) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode, city);
        }
        String region = region(city);
        int[] range = region == null ? null : REGION_POSTCODE_RANGES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode, city);
            }
        }
    }

    /**
     * Derives an owner's region from its city using the fixed city-to-region table (Sydney->NSW,
     * Melbourne->VIC, Brisbane->QLD), returning {@code null} when the city is not in the table.
     *
     * @param city the owner's city
     * @return the region code, or {@code null} when the city has no known region
     */
    private String region(String city) {
        if (city == null) {
            return null;
        }
        return switch (city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> null;
        };
    }

    /**
     * The number of national (subscriber) digits an E.164 number must carry for a given country
     * calling code, keyed by that code (without the leading '+'). Australia ('+61') requires 9
     * national digits and the North American Numbering Plan ('+1') requires 10.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        Map.of("61", 9, "1", 10);

    /**
     * Normalizes a submitted telephone into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading '+' with its country code is kept; otherwise the country code '+61' is assumed and
     * a single leading '0' is dropped from the national digits. The resulting value must be a '+'
     * followed by 8 to 15 digits, and its national-number length must match the one required by its
     * country calling code ('+61' requires 9 national digits, '+1' requires 10).
     *
     * @param telephone the raw telephone value as submitted
     * @return the E.164 normalized telephone (e.g. {@code +61412345678})
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number, or its
     *                                   national-number length is wrong for its country code
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String nationalSignificantNumber;
        if (cleaned.startsWith("+")) {
            nationalSignificantNumber = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            nationalSignificantNumber = "61" + national;
        }
        if (!nationalSignificantNumber.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(telephone);
        }
        validateNationalNumberLength(nationalSignificantNumber, telephone);
        return "+" + nationalSignificantNumber;
    }

    /**
     * Validates the national-number length of an E.164 number against its country calling code. The
     * significant number is a country code immediately followed by the national digits; for each
     * known code in {@link #NATIONAL_NUMBER_LENGTHS} the remaining national digits must number
     * exactly the required amount ('+61' requires 9, '+1' requires 10). Codes with no known rule are
     * left unchecked.
     *
     * @param nationalSignificantNumber the E.164 digits following the '+' (country code + national)
     * @param rejectedValue             the raw telephone value, reported on rejection
     * @throws InvalidTelephoneException if the national-number length is wrong for the country code
     */
    private void validateNationalNumberLength(String nationalSignificantNumber, String rejectedValue) {
        for (Map.Entry<String, Integer> rule : NATIONAL_NUMBER_LENGTHS.entrySet()) {
            String countryCode = rule.getKey();
            if (nationalSignificantNumber.startsWith(countryCode)) {
                int nationalDigits = nationalSignificantNumber.length() - countryCode.length();
                if (nationalDigits != rule.getValue()) {
                    throw new InvalidTelephoneException(rejectedValue);
                }
                return;
            }
        }
    }

    /**
     * Normalizes a submitted email. An owner may omit the email entirely (a {@code null} or blank
     * value is treated as absent and returns {@code null}); when present the value must be a
     * syntactically valid address, and is stored and returned lower-cased.
     *
     * @param email the raw email value as submitted, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidEmailException if a non-blank value is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException(email);
        }
        return normalized;
    }

    /**
     * The single, consolidated duplicate check. Rejects the owner being created when its whole
     * {@link #identityKey(Owner) identityKey} exactly equals that of an existing owner. Because the
     * key is the normalized telephone, email and householdId joined together, this one check
     * subsumes the former separate telephone, email and household duplicate rules: only an exact
     * full-key match counts as a duplicate, so two members of the same household (same householdId)
     * with different telephones have different keys and are both allowed.
     *
     * @param owner the owner being created, with its normalized telephone, email and householdId
     *              already applied
     * @throws DuplicateIdentityException if another owner already has the same identityKey
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String identityKey = identityKey(owner);
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(this::identityKey)
            .anyMatch(identityKey::equals);
        if (inUse) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * Derives an owner's {@code identityKey}: {@code normalizedTelephone + '|' + (email or empty) +
     * '|' + householdId}. A {@code null} email or householdId contributes an empty segment. Every
     * duplicate decision is made by comparing whole identityKeys.
     *
     * @param owner the owner whose identityKey to derive
     * @return the derived identityKey
     */
    private String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Applies the household rule to an owner being created. An owner belongs to the same household
     * as an existing owner when their lastName and address both match (compared case-insensitively
     * with runs of whitespace collapsed to a single space).
     *
     * <p>When no such existing owner exists there is nothing to do. Otherwise the new owner and
     * every existing owner in the household are assigned the same stable
     * {@link #householdId(String, String) householdId}. The household itself is no longer a
     * rejection: duplicate detection is now decided solely by the whole {@code identityKey}, of
     * which the householdId is one segment, so two members of the same household with different
     * telephones have different keys and are both allowed.
     *
     * @param owner the owner being created
     */
    private void applyHousehold(Owner owner) {
        String lastNameKey = toHouseholdKey(owner.getLastName());
        String addressKey = toHouseholdKey(owner.getAddress());
        List<Owner> household = this.clinicService.findAllOwners().stream()
            .filter(existing -> toHouseholdKey(existing.getLastName()).equals(lastNameKey)
                && toHouseholdKey(existing.getAddress()).equals(addressKey))
            .toList();
        if (household.isEmpty()) {
            return;
        }
        String householdId = householdId(lastNameKey, addressKey);
        owner.setHouseholdId(householdId);
        for (Owner existing : household) {
            if (!householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                this.clinicService.saveOwner(existing);
            }
        }
    }

    /**
     * Derives the stable, shared household identifier for a household, formatted {@code HH-<HEX12>}
     * where HEX12 is the first 12 upper-cased hex characters of the SHA-256 of the normalized
     * lastName and address keys. Being a pure function of the household, every owner in it derives
     * the same value regardless of creation order.
     *
     * @param lastNameKey the normalized household lastName key
     * @param addressKey  the normalized household address key
     * @return the household identifier
     */
    private String householdId(String lastNameKey, String addressKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((lastNameKey + "|" + addressKey).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return "HH-" + hex.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Normalizes a lastName or address for household comparison: leading and trailing whitespace is
     * trimmed, internal runs of whitespace are collapsed to a single space, and the value is
     * lower-cased so the comparison is case-insensitive. A {@code null} value normalizes to an empty
     * string.
     */
    private String toHouseholdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes a submitted address into the canonical form stored and returned for an owner:
     * leading and trailing whitespace is trimmed, internal runs of whitespace are collapsed to a
     * single space, the value is upper-cased, and common street-type abbreviations are expanded on a
     * whole-word basis ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}). A {@code null}
     * value, or one that is blank once trimmed, normalizes to an empty string.
     *
     * @param address the raw address value as submitted, may be {@code null}
     * @return the normalized address, or an empty string when none was supplied
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
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
