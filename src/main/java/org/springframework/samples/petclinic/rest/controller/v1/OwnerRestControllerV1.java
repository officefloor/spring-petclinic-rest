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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.advice.RequiredFieldsMissingException;
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
     * Syntactic email check: a non-empty local part, a single {@code @}, and a domain that
     * contains at least one dot, with no whitespace anywhere. Deliberately lenient — it accepts
     * ordinary addresses while rejecting clearly malformed ones such as {@code not-an-email}.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Disposable email domains that are refused on create. An email whose domain (the part after the
     * {@code @}, compared case-insensitively) is in this set is rejected with a 400, since such
     * addresses are throwaway and unsuitable for contacting an owner.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Dedicated audit logger. On a successful create an audit line carrying the owner id, the
     * customerCode, the registrationDate and the membershipLevel is emitted through this logger.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Remembers the owner created for each {@code Idempotency-Key} seen on a create, so a repeated
     * create carrying a key already in this map returns the originally created owner (200) instead of
     * creating a duplicate. Keys are supplied by the caller and are process-wide.
     */
    private final java.util.Map<String, Integer> idempotencyKeys = new java.util.concurrent.ConcurrentHashMap<>();

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

    /**
     * Rejects an owner payload that is missing or blank in any required field, listing the name of
     * each offending field so the caller receives a 400 with a populated {@code errors} array.
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missing = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(ownerFieldsDto.getAddress())) {
            missing.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missing.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes an address on create: leading/trailing whitespace is trimmed, every run of internal
     * whitespace is collapsed to a single space, the value is upper-cased and common abbreviations are
     * expanded token-by-token ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The
     * normalized value is what gets stored, returned and used for every address comparison; a value
     * that is blank (or {@code null}) normalizes to the empty string so the required-field check
     * rejects it.
     *
     * @param address the raw address as supplied by the caller, may be {@code null}
     * @return the normalized address ({@code ""} when blank or {@code null})
     */
    private static String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
     * Resolves and normalizes the owner's address on create, supporting both the structured form
     * ({@code addressLine1} / {@code addressLine2}) and the flat {@code address} form kept for
     * backward compatibility. Whichever address fields are supplied are normalized (see
     * {@link #normalizeAddress(String)}). The structured form is preferred when present: when
     * {@code addressLine1} is non-blank the composed {@code address} is the normalized
     * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when
     * {@code addressLine2} is present; otherwise the composed {@code address} is the normalized flat
     * {@code address}. The normalized structured fields are written back so they are stored and
     * returned, and the composed value is written to {@code address} so everything that reads the
     * address (the household hash, postcode validation and locality) uses the structured fields when
     * present, falling back to the flat address. When neither form supplies an address the composed
     * {@code address} is blank, so the required-field check rejects it.
     *
     * @param ownerFieldsDto the owner payload whose address fields are normalized in place
     */
    private static void applyAddress(OwnerFieldsDto ownerFieldsDto) {
        String line1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
        String line2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
        String flat = normalizeAddress(ownerFieldsDto.getAddress());
        String composed;
        if (!line1.isEmpty()) {
            composed = line2.isEmpty() ? line1 : line1 + " " + line2;
        } else {
            composed = flat;
        }
        ownerFieldsDto.setAddressLine1(line1.isEmpty() ? null : line1);
        ownerFieldsDto.setAddressLine2(line2.isEmpty() ? null : line2);
        ownerFieldsDto.setAddress(composed);
    }

    /**
     * Normalizes a telephone on create into E.164 form. Spaces, dashes and brackets are stripped.
     * A leading {@code +} and its country code are kept as given; otherwise the country code
     * {@code +61} is assumed and a single leading {@code 0} is dropped from the national digits.
     * The result must carry 8 to 15 digits after the {@code +}. In addition, when the country code
     * is recognized the national number must have the exact length that code requires ({@code +61}
     * expects 9 national digits, {@code +1} expects 10). Returns the E.164 string to be stored and
     * returned.
     *
     * @param telephone the raw telephone as supplied by the caller
     * @return the normalized E.164 telephone (a {@code +} followed by 8 to 15 digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number, or its
     *     national-number length is wrong for its country code
     */
    private static String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = "+" + cleaned.substring(1);
        } else {
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            e164 = "+61" + national;
        }
        String digits = e164.substring(1);
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(
                "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        if (digits.startsWith("61")) {
            requireNationalLength(digits.substring(2), 9, "+61");
        } else if (digits.startsWith("1")) {
            requireNationalLength(digits.substring(1), 10, "+1");
        }
        return e164;
    }

    /**
     * Enforces that the national portion of an E.164 number has the exact number of digits its
     * country code requires.
     *
     * @param national the national-number digits (the part after the country code)
     * @param expected the exact national-number length the country code requires
     * @param countryCode the country code, for the error message
     * @throws InvalidTelephoneException if {@code national} does not have exactly {@code expected} digits
     */
    private static void requireNationalLength(String national, int expected, String countryCode) {
        if (national.length() != expected) {
            throw new InvalidTelephoneException(
                "Telephone with country code '" + countryCode + "' must have " + expected
                    + " national digits");
        }
    }

    /**
     * Normalizes an optional email. An absent or blank email is left unset (returns {@code null});
     * a present value must be a syntactically valid address and is stored and returned lower-cased.
     *
     * @param email the raw email as supplied by the caller, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidEmailException if a value is present but is not a syntactically valid address
     */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        String lowerCased = trimmed.toLowerCase(Locale.ROOT);
        String domain = lowerCased.substring(lowerCased.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("Email domain is not allowed");
        }
        return lowerCased;
    }

    /**
     * Derives the single duplicate-detection key that consolidates the former separate telephone,
     * email and household checks. The key is {@code normalizedTelephone + '|' + email + '|' +
     * householdId}, where the email segment is the empty string when the owner has no email. Because
     * the telephone is part of the key, two owners that differ in any one segment (for example two
     * members of the same household with different telephones) produce different keys.
     *
     * @param normalizedTelephone the E.164 telephone of the owner
     * @param email the lower-cased email of the owner, may be {@code null}
     * @param householdId the stable household identifier of the owner, may be {@code null}
     * @return the derived identity key
     */
    private static String deriveIdentityKey(String normalizedTelephone, String email, String householdId) {
        return (normalizedTelephone == null ? "" : normalizedTelephone)
            + "|" + (email == null ? "" : email)
            + "|" + (householdId == null ? "" : householdId);
    }

    /**
     * Rejects a create only when the new owner's whole {@code identityKey} equals that of an existing
     * owner. This one check replaces the former separate telephone, email and household duplicate
     * checks: only an exact full-key match is a duplicate, so two owners that differ in any single
     * segment are both allowed. Each existing owner's key is derived from its stored, already
     * normalized telephone, email and household identifier.
     *
     * @param identityKey the derived identity key of the owner being created
     * @throws DuplicateIdentityException if another owner already has this identity key
     */
    /**
     * The existing owners considered by the create endpoint's duplicate and identity checks:
     * every owner that is not soft-deleted. A soft-deleted owner retains its row but is ignored
     * here, so a create that would otherwise be blocked by it is allowed.
     *
     * @return a stream of the owners not flagged deleted
     */
    private java.util.stream.Stream<Owner> activeOwners() {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted());
    }

    private void rejectDuplicateIdentity(String identityKey) {
        boolean inUse = activeOwners()
            .anyMatch(existing -> identityKey.equals(
                deriveIdentityKey(existing.getTelephone(), existing.getEmail(), existing.getHouseholdId())));
        if (inUse) {
            throw new DuplicateIdentityException(
                "An owner with the same identity key already exists");
        }
    }

    /**
     * Rejects a create when an existing owner already belongs to the same household (i.e. carries the
     * same computed {@code householdId}, derived from last name and postcode), unless the request
     * knowingly opts in via {@code sharesHousehold}. Setting {@code sharesHousehold} bypasses this
     * block so the owner is created as a declared member of the existing household; without it, a
     * second owner in the same household is a conflict.
     *
     * @param householdId the computed household identifier of the owner being created
     * @param sharesHousehold whether the request opted in to sharing an existing household
     * @throws DuplicateHouseholdException if the household already exists and the request did not opt in
     */
    private void rejectDuplicateHousehold(String householdId, boolean sharesHousehold) {
        if (sharesHousehold) {
            return;
        }
        boolean exists = activeOwners()
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (exists) {
            throw new DuplicateHouseholdException(
                "An owner with the same last name and postcode already exists");
        }
    }

    /**
     * Finds a soft-match "possible duplicate" for the owner being created: an existing owner that is
     * not a hard identity duplicate but shares this owner's last name (compared case-insensitively) and
     * postcode while carrying a different telephone. When the postcode is absent no owner can share it,
     * so there is never a match. When several existing owners qualify the one with the lowest id is
     * returned, so the result is deterministic.
     *
     * @param lastName the last name of the owner being created
     * @param postcode the postcode of the owner being created, may be {@code null}
     * @param normalizedTelephone the E.164 telephone of the owner being created
     * @return the id of the matching existing owner, or {@code null} when there is no soft match
     */
    private Integer findPossibleDuplicateOf(String lastName, String postcode, String normalizedTelephone) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String normalizedLastName = normalizeName(lastName);
        return activeOwners()
            .filter(existing -> normalizedLastName.equals(normalizeName(existing.getLastName())))
            .filter(existing -> postcode.equals(existing.getPostcode()))
            .filter(existing -> !normalizedTelephone.equals(existing.getTelephone()))
            .map(Owner::getId)
            .filter(java.util.Objects::nonNull)
            .min(Integer::compareTo)
            .orElse(null);
    }

    /**
     * Derives the stable household identifier for an owner from its last name and postcode. The value
     * is the first twelve hex characters of the SHA-256 of {@code normalizedLastName + '|' + postcode}
     * (the last name normalized case-insensitively with surrounding whitespace trimmed; an absent
     * postcode contributes the empty string). Because it is a pure function of those two fields, every
     * owner with the same last name and postcode is assigned the same identifier automatically, without
     * any existing record having to be updated.
     *
     * @param lastName the last name of the owner being created
     * @param postcode the postcode of the owner being created, may be {@code null}
     * @return the stable household identifier
     */
    private static String householdId(String lastName, String postcode) {
        String key = normalizeName(lastName) + "|" + (postcode == null ? "" : postcode);
        return sha256HexPrefix(key, 12);
    }

    /**
     * The maximum number of owners a single city may contain. A create whose city already holds this
     * many owners is rejected, so a city never grows beyond this capacity.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Rejects a create whose city already contains {@link #CITY_CAPACITY} or more owners, so no city
     * ever exceeds its capacity. Cities are compared case-insensitively with surrounding whitespace
     * trimmed. The count reflects the
     * state before the new owner is persisted, so it excludes the owner being created.
     *
     * @param city the city of the owner being created
     * @throws CityAtCapacityException if the city already contains {@link #CITY_CAPACITY} owners
     */
    private void rejectCityAtCapacity(String city) {
        String normalizedCity = normalizeName(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedCity.equals(normalizeName(existing.getCity())))
            .count();
        if (ownersInCity >= CITY_CAPACITY) {
            throw new CityAtCapacityException(
                "The city already contains the maximum number of owners");
        }
    }

    /**
     * The maximum number of owners that may be created on a single day. A create made once this many
     * owners already carry today's {@code registrationDate} is rejected, so no more than this many
     * owners are registered per day.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Rejects a create once {@link #DAILY_OWNER_LIMIT} or more owners have already been created for
     * the given business day, counted by {@code registrationDate} equal to the effective (weekend
     * rolled forward) registration date of the owner being created. The count reflects the state
     * before the new owner is persisted, so it excludes the owner being created.
     *
     * @param registrationDate the effective business-day registration date of the owner being created
     * @throws DailyOwnerLimitExceededException if the day already holds {@link #DAILY_OWNER_LIMIT} owners
     */
    private void rejectDailyOwnerLimit(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (ownersToday >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                "The maximum number of owners for today has already been reached");
        }
    }

    /**
     * The number of owners that may already exist for a business day before a create is flagged with
     * a bulk-signup warning. Once more than this many owners already carry the effective registration
     * date, the created owner's {@code bulkSignupWarning} is set to {@code true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether a create should be flagged with a bulk-signup warning, i.e. whether more than
     * {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created for the given business day,
     * counted by {@code registrationDate} equal to the effective (weekend rolled forward) registration
     * date of the owner being created. The count reflects the state before the new owner is persisted,
     * so it excludes the owner being created.
     *
     * @param registrationDate the effective business-day registration date of the owner being created
     * @return {@code true} when more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry the date
     */
    private boolean bulkSignupWarning(LocalDate registrationDate) {
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return ownersToday > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Rolls a registration date forward to a business day: any date landing on a Saturday, Sunday or
     * listed public holiday is advanced one day at a time until it reaches the next non-holiday
     * weekday, and any other weekday is returned unchanged.
     *
     * @param date the effective registration date, supplied or defaulted to the server date
     * @return the same date when it is a non-holiday weekday, otherwise the next non-holiday business day
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Builds the customer code assigned to an owner on create, formatted {@code '<REGION>-<HASH8>'}
     * where {@code REGION} is the region code derived from the owner's postcode (the region whose
     * inclusive range contains the postcode, or {@code 'UNKNOWN'} when the postcode is absent or in
     * no known range) and {@code HASH8} is the first eight upper-cased hex characters of the SHA-256
     * of {@code normalizedTelephone + lastName}, e.g. {@code 'NSW-1A2B3C4D'}. There is no sequence
     * component: the identity is a pure region-and-hash value.
     *
     * @param postcode the postcode of the owner being created, may be {@code null}
     * @param normalizedTelephone the E.164 telephone of the owner being created
     * @param lastName the last name of the owner being created
     * @return the formatted customer code
     */
    private String customerCode(String postcode, String normalizedTelephone, String lastName) {
        return regionFromPostcode(postcode) + "-" + sha256HexPrefix(normalizedTelephone + lastName, 8);
    }

    /**
     * De-duplicates a freshly computed customer code against the customer codes already held by
     * existing owners. When the code is unused it is returned unchanged; otherwise {@code '-<n>'} is
     * appended with the smallest {@code n} of 2 or more that yields a code no existing owner carries.
     *
     * @param customerCode the computed customer code for the owner being created
     * @return the same code when unique, otherwise the code with a {@code '-<n>'} suffix that makes
     *     it unique
     */
    private String deduplicateCustomerCode(String customerCode) {
        Set<String> existing = new java.util.HashSet<>();
        for (Owner owner : this.clinicService.findAllOwners()) {
            if (owner.getCustomerCode() != null) {
                existing.add(owner.getCustomerCode());
            }
        }
        if (!existing.contains(customerCode)) {
            return customerCode;
        }
        int n = 2;
        while (existing.contains(customerCode + "-" + n)) {
            n++;
        }
        return customerCode + "-" + n;
    }

    /**
     * Returns the region code whose inclusive postcode range contains the given 4-digit postcode
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or {@code 'UNKNOWN'} when the postcode is
     * {@code null}, non-numeric, or in no known range.
     *
     * @param postcode the postcode, may be {@code null}
     * @return the region code, or {@code 'UNKNOWN'}
     */
    private static String regionFromPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return "UNKNOWN";
        }
        int code;
        try {
            code = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException e) {
            return "UNKNOWN";
        }
        for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (code >= range[0] && code <= range[1]) {
                return entry.getKey();
            }
        }
        return "UNKNOWN";
    }

    /**
     * Computes the SHA-256 of {@code value} and returns its first {@code length} hex characters,
     * upper-cased.
     *
     * @param value the string to hash
     * @param length the number of leading hex characters to return
     * @return the upper-cased hex prefix of the digest
     */
    private static String sha256HexPrefix(String value, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Counts the existing owners that share the owner-to-be's first and last name, compared
     * case-insensitively (surrounding whitespace trimmed). The count reflects the state before the
     * new owner is persisted, so it excludes the owner being created.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners with the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeName(firstName);
        String normalizedLastName = normalizeName(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing ->
                normalizedFirstName.equals(normalizeName(existing.getFirstName()))
                    && normalizedLastName.equals(normalizeName(existing.getLastName())))
            .count();
    }

    /**
     * Counts the members of the owner-to-be's household after this create, i.e. one more than the
     * number of existing owners already carrying the same {@code householdId}. The household
     * identifier is a deterministic function of last name and postcode, so a solo owner (the only one
     * with that identifier) is a household of one. The count includes the owner being created (hence
     * the {@code + 1}).
     *
     * @param householdId the stable household identifier of the owner being created
     * @return the number of household members after this create (always at least 1)
     */
    private int householdSize(String householdId) {
        if (householdId == null) {
            return 1;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count() + 1;
    }

    /**
     * Normalizes a name for case-insensitive comparison by trimming surrounding whitespace and
     * lower-casing.
     *
     * @param value the raw name, may be {@code null}
     * @return the normalized name ({@code ""} when {@code value} is {@code null})
     */
    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * The fixed city-to-region table used to validate a postcode against its city
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD). A city not in this table has no known region and
     * therefore accepts any 4-digit postcode.
     */
    private static final java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * The fixed region-to-postcode-range table: each region admits an inclusive 4-digit range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's optional postcode on create. A {@code null} or blank postcode is left
     * untouched (postcode is optional when absent). When present it must be exactly four digits and,
     * when the city maps to a known region, must fall within that region's inclusive range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit
     * postcode.
     *
     * @param city the city of the owner being created
     * @param postcode the raw postcode as supplied by the caller, may be {@code null}
     * @throws InvalidPostcodeException if a present postcode is not four digits or is out of range
     *     for the city's region
     */
    private static void validatePostcode(String city, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!postcode.matches("\\d{4}")) {
            throw new InvalidPostcodeException("Postcode must be exactly 4 digits");
        }
        String region = CITY_REGION.get(city);
        int[] range = region == null ? null : REGION_POSTCODES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(
                    "Postcode is out of range for the city's region");
            }
        }
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

    /**
     * Reads the caller-supplied {@code Idempotency-Key} header from the current request, if any. The
     * header is absent for ordinary creates; when present and non-blank it enables the idempotent-create
     * behaviour keyed on its value.
     *
     * @return the trimmed idempotency key, or {@code null} when the header is absent or blank
     */
    private static String currentIdempotencyKey() {
        var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttributes)) {
            return null;
        }
        String key = servletAttributes.getRequest().getHeader("Idempotency-Key");
        return (key == null || key.isBlank()) ? null : key.trim();
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotencyKeys.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        applyAddress(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        LocalDate registrationDate = ownerFieldsDto.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        } else if (registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                "registrationDate must not be later than the server date");
        }
        registrationDate = toBusinessDay(registrationDate);
        ownerFieldsDto.setRegistrationDate(registrationDate);
        rejectDailyOwnerLimit(registrationDate);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        validatePostcode(ownerFieldsDto.getCity(), ownerFieldsDto.getPostcode());
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        ownerFieldsDto.setTelephone(normalizedTelephone);
        String normalizedEmail = normalizeEmail(ownerFieldsDto.getEmail());
        ownerFieldsDto.setEmail(normalizedEmail);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        String ownerHouseholdId = householdId(ownerFieldsDto.getLastName(), ownerFieldsDto.getPostcode());
        rejectDuplicateIdentity(deriveIdentityKey(normalizedTelephone, normalizedEmail, ownerHouseholdId));
        rejectDuplicateHousehold(ownerHouseholdId, sharesHousehold);
        Integer possibleDuplicateOf = sharesHousehold ? null : findPossibleDuplicateOf(
            ownerFieldsDto.getLastName(), ownerFieldsDto.getPostcode(), normalizedTelephone);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        owner.setCustomerCode(deduplicateCustomerCode(customerCode(
            ownerFieldsDto.getPostcode(), normalizedTelephone, ownerFieldsDto.getLastName())));
        owner.setHouseholdId(ownerHouseholdId);
        owner.setHouseholdSize(householdSize(ownerHouseholdId));
        owner.setNamesakeCount(countNamesakes(ownerFieldsDto.getFirstName(), ownerFieldsDto.getLastName()));
        owner.setBulkSignupWarning(bulkSignupWarning(registrationDate));
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotencyKeys.put(idempotencyKey, owner.getId());
        }
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
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
        owner.setDeleted(true);
        this.clinicService.saveOwner(owner);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
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
