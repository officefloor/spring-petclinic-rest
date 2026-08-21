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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateEmailException;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
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
     * Dedicated audit logger. On a successful owner create an audit line carrying the owner id,
     * the {@code customerCode} and the {@code registrationDate} is emitted to this named logger.
     */
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

    /**
     * Rejects an owner payload that is missing or blank in any of the required fields
     * (firstName, lastName, address, city, telephone). Bean Validation on the request body
     * already rejects {@code null} values and empty strings, but treats a whitespace-only
     * value as present; this guard closes that gap so a blank in any required field is
     * reported. The thrown exception is translated to a 400 whose {@code errors} array lists
     * the name of each offending field.
     */
    private void rejectMissingOrBlankFields(OwnerFieldsDto ownerFieldsDto, String normalizedAddress) {
        List<String> missing = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(normalizedAddress)) {
            missing.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missing.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Matches a well-formed E.164 telephone: a leading '+' followed by 8 to 15 digits.
     */
    private static final Pattern E164_PATTERN = Pattern.compile("\\+\\d{8,15}");

    /**
     * Required national-number length per country calling code. When the normalized E.164 number
     * begins with one of these codes, the digits that follow must number exactly the mapped value:
     * '+61' (Australia) requires 9 national digits and '+1' (NANP) requires 10. Codes absent from
     * this map carry no per-country length rule beyond the generic E.164 8-to-15-digit bound.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of(
        "+61", 9,
        "+1", 10);

    /**
     * Normalizes an owner's telephone on create into E.164 form. Spaces, dashes and brackets are
     * stripped. When a leading '+' (with its country code) is present it is kept as-is; otherwise
     * the country code '+61' is assumed and a single leading '0' is dropped from the national
     * digits. The result must be a '+' followed by 8 to 15 digits, so e.g. '0412 345 678' is
     * stored as '+61412345678'. When the country code has a known national-number length ('+61'
     * requires 9 national digits, '+1' requires 10) the national digits must match it exactly. A
     * value that cannot form a valid E.164 number, or whose national length is wrong for its
     * country, is rejected via {@link InvalidTelephoneException}, which the exception handler
     * translates to a 400.
     */
    private static String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()\\[\\]]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = cleaned;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = "+61" + national;
        }
        if (!E164_PATTERN.matcher(e164).matches()) {
            throw new InvalidTelephoneException(telephone);
        }
        for (Map.Entry<String, Integer> rule : NATIONAL_NUMBER_LENGTHS.entrySet()) {
            String code = rule.getKey();
            if (e164.startsWith(code)) {
                int nationalLength = e164.length() - code.length();
                if (nationalLength != rule.getValue()) {
                    throw new InvalidTelephoneException(telephone);
                }
                break;
            }
        }
        return e164;
    }

    /**
     * Matches a well-formed postcode: exactly four digits.
     */
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("\\d{4}");

    /**
     * The canonical region for each city that has one. A city absent from this table has no known
     * region and so accepts any 4-digit postcode. This is the same fixed city-to-region table used
     * to derive an owner's locality.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * The inclusive 4-digit postcode range for each region, as {@code {low, high}}: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's optional postcode. A {@code null} postcode is left as-is (the field is
     * optional). When present it must be exactly four digits; and when the owner's city maps to a
     * known region it must fall within that region's inclusive postcode range (NSW 2000-2099, VIC
     * 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode. An
     * invalid or out-of-range value is rejected via {@link InvalidPostcodeException}, which the
     * exception handler translates to a 400.
     */
    private static void validatePostcode(String city, String postcode) {
        if (postcode == null) {
            return;
        }
        if (!POSTCODE_PATTERN.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        String region = CITY_REGION.get(city);
        if (region == null) {
            return;
        }
        int[] range = REGION_POSTCODES.get(region);
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }

    /**
     * A pragmatic syntactic email check: a non-empty local part, an {@code @}, and a domain that
     * carries at least one dot, with no whitespace anywhere. Deliberately permissive — it accepts
     * ordinary addresses while rejecting obvious non-addresses such as one lacking an {@code @}.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /**
     * Disposable-email domains that an owner's email may not use. An address whose domain (the part
     * after the final {@code @}, compared case-insensitively) is one of these is rejected. Kept as a
     * fixed set so the block list is explicit and easy to extend.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /**
     * Normalizes an optional owner email. A {@code null} email is left as-is (the field is
     * optional). When present it must be a syntactically valid address whose domain is not on the
     * disposable-domain block list; a valid value is lower-cased so it is stored and returned in
     * canonical form. An invalid or disposable-domain value is rejected via
     * {@link InvalidEmailException}, which the exception handler translates to a 400.
     */
    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidEmailException(email);
        }
        return normalized;
    }

    /**
     * The single derived identity of an owner, {@code normalizedTelephone + '|' + (email or empty)
     * + '|' + householdId}, into which all duplicate detection is consolidated. Two owners are the
     * same identity only when this whole key matches: because the normalized telephone is part of
     * the key, two members of the same household (same {@code householdId}) with different
     * telephones have different identity keys. A {@code null} email contributes an empty middle
     * segment. This is the value returned to callers as {@code identityKey}.
     */
    private static String identityKeyFor(String normalizedTelephone, String normalizedEmail, String householdId) {
        return normalizedTelephone + "|" + (normalizedEmail == null ? "" : normalizedEmail) + "|" + householdId;
    }

    /**
     * Rejects a create whose derived identity collides with an existing owner's, reported via one
     * of the {@code Duplicate*} exceptions the handler translates to a 409. The former separate
     * telephone, email and household checks are now expressed through the components of the single
     * identity key:
     * <ul>
     *   <li>the normalized telephone matches an existing owner's canonical E.164 telephone (an
     *       existing value that cannot form a valid E.164 number is skipped rather than colliding);</li>
     *   <li>a present, lower-cased email matches an existing owner's lower-cased email (a {@code null}
     *       candidate email, being optional, is not compared, and existing owners without an email
     *       are skipped);</li>
     *   <li>the {@code householdId} matches an existing owner's - unless the caller opted into sharing
     *       a household via {@code sharesHousehold}, in which case the household component is not
     *       compared.</li>
     * </ul>
     */
    private void rejectDuplicateIdentity(OwnerFieldsDto ownerFieldsDto, String normalizedTelephone,
                                         String normalizedEmail, String householdId) {
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        for (Owner existing : this.clinicService.findAllOwners()) {
            String existingTelephone;
            try {
                existingTelephone = normalizeTelephone(existing.getTelephone());
            } catch (InvalidTelephoneException ex) {
                existingTelephone = null;
            }
            if (normalizedTelephone.equals(existingTelephone)) {
                throw new DuplicateTelephoneException(normalizedTelephone);
            }
            if (normalizedEmail != null) {
                String existingEmail = existing.getEmail();
                if (existingEmail != null && normalizedEmail.equals(existingEmail.toLowerCase(Locale.ROOT))) {
                    throw new DuplicateEmailException(normalizedEmail);
                }
            }
            if (!sharesHousehold
                && householdId.equals(householdIdFor(existing.getLastName(), existing.getPostcode()))) {
                throw new DuplicateHouseholdException(ownerFieldsDto.getLastName(), ownerFieldsDto.getPostcode());
            }
        }
    }

    /**
     * Collapses a value for household comparison: leading and trailing whitespace is trimmed, any
     * internal run of whitespace is reduced to a single space, and the result is lower-cased. This
     * is how both {@code lastName} and {@code address} are compared so that differences of case or
     * spacing do not defeat the duplicate-household check.
     */
    private static String collapse(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner's address into a canonical form applied on every create: leading and
     * trailing whitespace is trimmed, any internal run of whitespace is reduced to a single space,
     * the value is upper-cased, and common street-type abbreviations are expanded on a per-word
     * basis ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The result is stored
     * and returned, and is the form every address comparison (duplicate-household detection and the
     * shared {@code householdId}) uses. A {@code null} or whitespace-only input normalizes to the
     * empty string, which the required-field guard then rejects. The transformation is idempotent,
     * so normalizing an already-normalized address is a no-op.
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
                default -> {
                }
            }
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Derives an owner's region code from its postcode, falling back to its city. A present postcode
     * that falls in a known region's inclusive 4-digit range wins (NSW 2000-2099, VIC 3000-3099, QLD
     * 4000-4099); otherwise the fixed city-to-region table is consulted (Sydney -> NSW, Melbourne ->
     * VIC, Brisbane -> QLD); a value resolved by neither is {@code "UNKNOWN"}. This is the single
     * region derivation shared by the {@code customerCode} and the owner's locality.
     */
    private static String deriveRegion(String city, String postcode) {
        if (postcode != null) {
            try {
                int pc = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> range : REGION_POSTCODES.entrySet()) {
                    int[] bounds = range.getValue();
                    if (pc >= bounds[0] && pc <= bounds[1]) {
                        return range.getKey();
                    }
                }
            } catch (NumberFormatException ignored) {
                // not a numeric postcode; fall back to the city-to-region table
            }
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Builds the {@code customerCode} for a newly created owner, formatted {@code '<REGION>-<HASH8>'}
     * where {@code REGION} is the region code derived from the owner's postcode (falling back to its
     * city) and {@code HASH8} is the first 8 upper-case hex characters of SHA-256 over
     * {@code normalizedTelephone + lastName} (e.g. {@code 'NSW-1A2B3C4D'}). This is a stable identity:
     * it carries no sequence number and depends only on the owner's own region, telephone and surname.
     */
    private static String customerCodeFor(String region, String normalizedTelephone, String lastName) {
        return region + "-" + sha256HexPrefix(normalizedTelephone + lastName, 8);
    }

    /**
     * De-duplicates a freshly computed {@code customerCode} against the codes already carried by
     * existing owners. When the base code is unused it is returned unchanged; otherwise
     * {@code '-<n>'} is appended with the smallest {@code n} of 2 or more that yields a code no
     * existing owner already holds. The count reflects the state prior to this create.
     */
    private String deduplicateCustomerCode(String baseCode) {
        Set<String> existingCodes = new HashSet<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            String code = existing.getCustomerCode();
            if (code != null) {
                existingCodes.add(code);
            }
        }
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
     * Returns the first {@code hexChars} upper-case hex characters of the SHA-256 digest of the UTF-8
     * bytes of {@code value}.
     */
    private static String sha256HexPrefix(String value, int hexChars) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hexChars);
            for (byte b : digest) {
                if (sb.length() >= hexChars) {
                    break;
                }
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, hexChars);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Builds the {@code membershipNumber} for a newly created owner, formatted
     * {@code '<customerCode>-M<YY>'} where {@code YY} is the last two digits of the
     * {@code registrationDate} year (e.g. {@code 'SMI-0007-M26'}).
     */
    private static String membershipNumberFor(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /**
     * Derives the stable {@code householdId} for an owner from the collapsed {@code lastName} and
     * its {@code postcode} - the same pair used to detect a shared household. It is the first 12
     * lower-case hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. Because
     * it is a pure function of that pair, every owner in the same household - whether created first
     * or joining later via {@code sharesHousehold} - receives the identical, non-blank identifier
     * without needing to read any other owner's value. A {@code null} postcode contributes an empty
     * segment.
     */
    private static String householdIdFor(String lastName, String postcode) {
        String key = collapse(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (byte b : digest) {
                if (sb.length() >= 12) {
                    break;
                }
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Counts the existing owners who already share the given {@code firstName} and {@code lastName},
     * compared case-insensitively. This is evaluated before the new owner is saved, so it reflects
     * the state prior to this create and is stored on the owner as its {@code namesakeCount}.
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
     * Counts the members of the household the new owner will belong to, as it stands after this
     * create. Every existing owner carrying the same {@code householdId} is counted, and the new
     * owner itself is added, so a value of 3 or more means the owner joins a household of at least
     * three members. This is evaluated with the new owner's {@code householdId} already set and is
     * stored on the owner as its {@code householdMemberCount}.
     */
    private int countHouseholdMembers(String householdId) {
        int count = 1;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Finds an existing owner that makes the new owner a <em>possible</em> (soft) duplicate: one
     * that is not a hard identity collision but shares the new owner's {@code lastName} (compared
     * case-insensitively) and {@code postcode} while carrying a different normalized telephone. The
     * new owner's postcode must be present for a soft match to be possible. When several existing
     * owners qualify the one with the lowest id is chosen. Returns the matching owner's id, or
     * {@code null} when there is no soft match. This is evaluated after the hard-duplicate check has
     * already passed, so any owner sharing the lastName and postcode necessarily differs in telephone.
     *
     * <p>Because the household is now keyed on {@code (lastName, postcode)} - exactly the soft-match
     * key - a co-member reached here can only be one the caller declared via {@code sharesHousehold}
     * (otherwise the hard household-duplicate check would already have rejected it). A declared
     * household member is not a suspected duplicate, so no soft match is reported in that case.
     */
    private Integer findPossibleDuplicateOf(OwnerFieldsDto ownerFieldsDto, String normalizedTelephone) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return null;
        }
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null) {
            return null;
        }
        String lastName = ownerFieldsDto.getLastName();
        Integer matchId = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (lastName == null || !lastName.equalsIgnoreCase(existing.getLastName())) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            String existingTelephone;
            try {
                existingTelephone = normalizeTelephone(existing.getTelephone());
            } catch (InvalidTelephoneException ex) {
                existingTelephone = null;
            }
            if (normalizedTelephone.equals(existingTelephone)) {
                continue;
            }
            if (existing.getId() != null && (matchId == null || existing.getId() < matchId)) {
                matchId = existing.getId();
            }
        }
        return matchId;
    }

    /**
     * The tenure, in days, that an owner must exceed before its tenure contributes membership
     * points. Tenure is measured from the owner's {@code registrationDate}, so a newly created owner
     * (whose tenure is zero) never satisfies this and thus never earns the tenure points on create.
     */
    private static final int TENURE_POINTS_THRESHOLD_DAYS = 365;

    /**
     * The household size (number of members) at or above which an owner earns the household points.
     */
    private static final int HOUSEHOLD_POINTS_THRESHOLD = 3;

    /**
     * Computes the {@code membershipPoints} for an owner. Points start at 0, gain 2 when an email is
     * present, gain 1 when the owner's {@code namesakeCount} is 0, gain 2 when the owner's household
     * has {@value #HOUSEHOLD_POINTS_THRESHOLD} or more members, and gain 3 when the owner's tenure
     * (days elapsed since its {@code registrationDate}) exceeds {@value #TENURE_POINTS_THRESHOLD_DAYS}
     * days. Because a newly created owner has zero tenure, the tenure points never apply on create.
     * Evaluated after the owner's email, namesakeCount, householdMemberCount and registrationDate
     * have been set.
     */
    private static int membershipPointsFor(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null
            && owner.getHouseholdMemberCount() >= HOUSEHOLD_POINTS_THRESHOLD) {
            points += 2;
        }
        if (tenureInDays(owner) > TENURE_POINTS_THRESHOLD_DAYS) {
            points += 3;
        }
        return points;
    }

    /**
     * Maps {@code membershipPoints} to the numeric {@code membershipLevel}: level 1 for 0-1 points,
     * 2 for 2-3, 3 for 4-5, and 4 for 6 or more points.
     */
    private static int membershipLevelForPoints(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The owner's tenure in whole days: the number of days elapsed from its {@code registrationDate}
     * up to the current server date. An owner with no registration date, or one dated in the future,
     * has a tenure of zero.
     */
    private static long tenureInDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(registrationDate, LocalDate.now()));
    }

    /**
     * The maximum number of owners any single city may contain. A create whose city already
     * holds this many owners is rejected.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Rejects a create whose city already contains {@value #CITY_CAPACITY} or more owners,
     * compared case-insensitively - the same way the city is matched when building the
     * {@code customerCode} sequence. The count reflects the state prior to this create. A city
     * at capacity is reported via {@link CityAtCapacityException}, which the exception handler
     * translates to a 409.
     */
    private void rejectCityAtCapacity(String city) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * The maximum number of owners that may be created on any single day (by
     * {@code registrationDate}). A create attempted once this many owners already carry the
     * current day's registration date is rejected.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Rejects a create once {@value #DAILY_OWNER_LIMIT} or more owners already carry the given
     * business-day-adjusted {@code registrationDate} (the date the new owner will itself receive).
     * The count reflects the state prior to this create. When the limit is reached the create is
     * reported via {@link DailyOwnerLimitExceededException}, which the exception handler translates
     * to a 429 Too Many Requests.
     */
    private void rejectDailyOwnerLimit(LocalDate registrationDate) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException();
        }
    }

    /**
     * The number of owners that must already carry a given {@code registrationDate} before a
     * create on that date is flagged with a bulk-signup warning. Once <em>more than</em> this
     * many owners already exist for the day, the new owner's {@code bulkSignupWarning} is true.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Computes the {@code bulkSignupWarning} for a create on the given business-day-adjusted
     * {@code registrationDate}: true when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners
     * have already been created for that date, otherwise false. The count reflects the state prior
     * to this create, matching how the daily-limit rule accumulates.
     */
    private boolean bulkSignupWarningFor(LocalDate registrationDate) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Rolls an effective registration date forward onto a business day. A Saturday or Sunday is
     * advanced to the following Monday; a weekday is returned unchanged. This is applied to the
     * effective registration date - whether supplied in the request or defaulted to the server
     * date - so the stored {@code registrationDate}, and everything derived from it, always falls
     * on a business day.
     */
    /**
     * Rejects a supplied {@code registrationDate} that lies in the future. A registration date is
     * only ever explicitly supplied on the request; when present it must not be later than the
     * current server date. A future date raises a {@link FutureRegistrationDateException}, which the
     * exception handler translates to a 400 Bad Request. A {@code null} date (the default, filled in
     * later from the server date) is accepted.
     */
    private static void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }

    private static LocalDate toBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String normalizedAddress = normalizeAddress(ownerFieldsDto.getAddress());
        rejectMissingOrBlankFields(ownerFieldsDto, normalizedAddress);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizedAddress);
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        owner.setTelephone(normalizedTelephone);
        String normalizedEmail = normalizeEmail(ownerFieldsDto.getEmail());
        owner.setEmail(normalizedEmail);
        validatePostcode(owner.getCity(), owner.getPostcode());
        String householdId = householdIdFor(owner.getLastName(), owner.getPostcode());
        rejectDuplicateIdentity(ownerFieldsDto, normalizedTelephone, normalizedEmail, householdId);
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        LocalDate effectiveDate = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        rejectDailyOwnerLimit(registrationDate);
        owner.setBulkSignupWarning(bulkSignupWarningFor(registrationDate));
        owner.setRegistrationDate(registrationDate);
        String region = deriveRegion(owner.getCity(), owner.getPostcode());
        owner.setCustomerCode(deduplicateCustomerCode(customerCodeFor(region, normalizedTelephone, owner.getLastName())));
        owner.setMembershipNumber(membershipNumberFor(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setHouseholdId(householdId);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdMemberCount(countHouseholdMembers(owner.getHouseholdId()));
        int membershipPoints = membershipPointsFor(owner);
        owner.setMembershipPoints(membershipPoints);
        owner.setMembershipLevel(membershipLevelForPoints(membershipPoints));
        Integer possibleDuplicateOf = findPossibleDuplicateOf(ownerFieldsDto, normalizedTelephone);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(), owner.getMembershipNumber());
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
