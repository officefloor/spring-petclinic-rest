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
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Dedicated audit logger. On a successful owner create an audit line carrying the owner id,
     * the {@code memberId} and the {@code registrationDate} is emitted to this named logger.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Dedicated notification logger. On a successful owner create a welcome-notification line
     * carrying the owner id and the {@code memberId} is enqueued by emitting it to this named logger.
     */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Monotonically increasing sequence number stamped onto every {@code OWNER_CREATED} structured
     * event, so events can be totally ordered across creates. Shared across all creates for the life
     * of the application; a plain counter is enough since it only ever grows.
     */
    private static final AtomicLong OWNER_EVENT_SEQ = new AtomicLong();

    /**
     * Serializes the immutable {@code OWNER_CREATED} structured event to its canonical JSON form.
     */
    private static final ObjectMapper EVENT_MAPPER = JsonMapper.builder().build();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final HttpServletRequest request;

    /**
     * The HTTP header carrying a client-supplied idempotency token for owner creation.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers the owner originally created under each seen {@code Idempotency-Key}. A create that
     * repeats a key already present here is replayed - the stored owner is returned with 200 instead
     * of creating a duplicate.
     */
    private final Map<String, OwnerDto> idempotentCreates = new ConcurrentHashMap<>();

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 HttpServletRequest request) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.request = request;
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
     * The single derived identity of an owner: the 64-character lower-case SHA-256 hex digest over
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, into which all
     * duplicate detection is consolidated. Two owners are the same identity only when this whole key
     * matches: because the normalized telephone is part of the pre-image, two owners who share a
     * surname (same soundex) and postcode but carry different telephones have different identity
     * keys. A {@code null} email contributes an empty middle segment. Under version 2 the owner's
     * {@link #regionCodeV2 version-2 region code} is mixed into the pre-image as a trailing segment,
     * so the key differs from its version-1 value. This is the value returned to callers as the
     * {@code identityKey} inside the {@code identity} object.
     */
    private static String identityKeyFor(String normalizedTelephone, String normalizedEmail, String lastName,
                                         String regionV2) {
        String preimage = normalizedTelephone + "|" + (normalizedEmail == null ? "" : normalizedEmail)
            + "|" + soundex(lastName) + "|" + regionV2;
        return sha256Hex(preimage);
    }

    /**
     * The American Soundex code of a name: its first letter followed by up to three digits encoding
     * the remaining consonants (b/f/p/v->1, c/g/j/k/q/s/x/z->2, d/t->3, l->4, m/n->5, r->6), with
     * adjacent duplicate codes collapsed, vowels (and y) resetting the run, and h/w transparent so
     * consonants they separate are still treated as adjacent. The result is padded with zeros to
     * exactly four characters. A {@code null} or letterless name yields the empty string.
     */
    private static String soundex(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char prev = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexCode(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            prev = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit for a single upper-case letter, or {@code '0'} for a vowel, y, h or w (which
     * carry no code).
     */
    private static char soundexCode(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    /**
     * The full lower-case SHA-256 hex digest (64 characters) of the UTF-8 bytes of {@code value}.
     */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Rejects a create whose derived identity collides with an existing owner's, reported via
     * {@link DuplicateTelephoneException} which the handler translates to a 409. Duplicate detection
     * is now the single identity key: the new owner's {@link #identityKeyFor identityKey} (the
     * SHA-256 over {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}) is
     * compared against each existing owner's, ignoring owners flagged deleted. The email-domain
     * block list has already been applied while normalizing the email, so it takes effect first. An
     * existing telephone that cannot form a valid E.164 number is skipped rather than colliding.
     * Because the telephone is part of the key, two owners with the same surname and postcode but
     * different telephones no longer collide here - they are surfaced as a soft match instead. The
     * former separate telephone, email and household 409 blocks are all subsumed by this key.
     */
    private void rejectDuplicateIdentity(String normalizedTelephone, String normalizedEmail, String lastName,
                                         String regionV2) {
        String identityKey = identityKeyFor(normalizedTelephone, normalizedEmail, lastName, regionV2);
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.isDeleted()) {
                continue;
            }
            String existingTelephone;
            try {
                existingTelephone = normalizeTelephone(existing.getTelephone());
            } catch (InvalidTelephoneException ex) {
                continue;
            }
            String existingEmail = existing.getEmail() == null ? null : existing.getEmail().toLowerCase(Locale.ROOT);
            String existingRegion = regionCodeV2(existing.getCity(), existing.getPostcode());
            String existingKey = identityKeyFor(existingTelephone, existingEmail, existing.getLastName(),
                existingRegion);
            if (identityKey.equals(existingKey)) {
                throw new DuplicateTelephoneException(normalizedTelephone);
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
     * region derivation shared by the {@code memberId} and the owner's locality.
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
     * The fixed version tag mixed into the region code of every version-2 identifier
     * ({@code memberId}, {@code householdId} and {@code identityKey}), so no value produced under
     * version 1 is produced again. It appears only inside those identifiers; the user-facing
     * {@code locality}, {@code timezone} and owner-segment region remain the plain region code.
     */
    private static final String IDENTITY_VERSION_TAG = "V2";

    /**
     * The version-2 region code folded into the owner's identifiers: the plain region from
     * {@link #deriveRegion} mixed with the fixed {@value #IDENTITY_VERSION_TAG} tag (e.g. the plain
     * region {@code "NSW"} becomes {@code "V2NSW"}). This tagged value is only ever embedded inside
     * an identifier; the plain region is what the owner's locality and timezone report.
     */
    private static String regionCodeV2(String city, String postcode) {
        return IDENTITY_VERSION_TAG + deriveRegion(city, postcode);
    }

    /**
     * Builds the {@code memberId} for a newly created owner, formatted {@code '<REGION><FY><HASH8><CHK>'}
     * where {@code REGION} is the region code derived from the owner's postcode (falling back to its
     * city), {@code FY} is the 2-digit fiscal year of the {@code registrationDate} (matching the owner's
     * {@code fiscalYear}), {@code HASH8} is the first 8 upper-case hex characters of SHA-256 over
     * {@code normalizedTelephone + lastName} (the same HASH8 used by the region-and-hash identity), and
     * {@code CHK} is a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}
     * (e.g. {@code 'NSW271A2B3C4D5'}). This is a stable identity: it carries no sequence number and
     * depends only on the owner's own region, fiscal year, telephone and surname.
     */
    private static String memberIdFor(String region, LocalDate registrationDate,
                                      String normalizedTelephone, String lastName) {
        String fy = String.format("%02d", fiscalYearOf(registrationDate) % 100);
        String hash8 = sha256HexPrefix(normalizedTelephone + lastName, 8);
        String base = region + fy + hash8;
        return base + luhnCheckDigit(base);
    }

    /**
     * The single Luhn check digit (0-9) computed over the digits contained in {@code value};
     * non-digit characters are ignored.
     */
    private static int luhnCheckDigit(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * De-duplicates a freshly computed {@code memberId} against the ids already carried by existing
     * owners. When the base id is unused it is returned unchanged; otherwise {@code '-<n>'} is appended
     * with the smallest {@code n} of 2 or more that yields an id no existing owner already holds. The
     * count reflects the state prior to this create.
     */
    private String deduplicateMemberId(String baseId) {
        Set<String> existingIds = new HashSet<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            String id = existing.getMemberId();
            if (id != null) {
                existingIds.add(id);
            }
        }
        if (!existingIds.contains(baseId)) {
            return baseId;
        }
        int n = 2;
        while (existingIds.contains(baseId + "-" + n)) {
            n++;
        }
        return baseId + "-" + n;
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
     * The fiscal year, as a four-digit year, of a business-day-adjusted date. The fiscal year
     * starts on 1 July and is labelled by the calendar year in which it ends: a date on or after
     * 1 July belongs to the fiscal year ending the following calendar year, while an earlier date
     * belongs to the fiscal year ending in its own year.
     */
    private static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derives the stable {@code householdId} for an owner from the collapsed {@code lastName} and
     * its {@code postcode} - the same pair used to detect a shared household. It is the first 12
     * lower-case hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. Because
     * it is a pure function of that pair, every owner in the same household - whether created first
     * or joining later via {@code sharesHousehold} - receives the identical, non-blank identifier
     * without needing to read any other owner's value. A {@code null} postcode contributes an empty
     * segment. Under version 2 the {@link #regionCodeV2 version-2 region code} is mixed in as a
     * trailing segment, so the value differs from its version-1 form. Because that region is derived
     * from the same postcode (falling back to the city), household members still share it.
     */
    private static String householdIdFor(String lastName, String postcode, String regionV2) {
        String key = collapse(lastName) + "|" + (postcode == null ? "" : postcode) + "|" + regionV2;
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
     * that is not a hard identity collision but whose {@code lastName} has the same
     * {@link #soundex soundex} code as the new owner's and whose {@code postcode} matches, while its
     * {@link #identityKeyFor identityKey} differs. The new owner's postcode must be present for a
     * soft match to be possible. When several existing owners qualify the one with the lowest id is
     * chosen. Returns the matching owner's id, or {@code null} when there is no soft match. This is
     * evaluated after the hard-duplicate check has already passed, so any surviving owner sharing the
     * surname's soundex and postcode necessarily has a different identity key (typically a different
     * telephone). A caller that declares {@code sharesHousehold} is deliberately joining an existing
     * household, so it is not flagged as a suspected duplicate.
     */
    private Integer findPossibleDuplicateOf(OwnerFieldsDto ownerFieldsDto, String normalizedTelephone,
                                            String normalizedEmail) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return null;
        }
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null) {
            return null;
        }
        String lastName = ownerFieldsDto.getLastName();
        if (lastName == null) {
            return null;
        }
        String regionV2 = regionCodeV2(ownerFieldsDto.getCity(), postcode);
        String identityKey = identityKeyFor(normalizedTelephone, normalizedEmail, lastName, regionV2);
        String soundex = soundex(lastName);
        Integer matchId = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (!soundex.equals(soundex(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            String existingTelephone;
            try {
                existingTelephone = normalizeTelephone(existing.getTelephone());
            } catch (InvalidTelephoneException ex) {
                continue;
            }
            String existingEmail = existing.getEmail() == null ? null : existing.getEmail().toLowerCase(Locale.ROOT);
            String existingRegion = regionCodeV2(existing.getCity(), existing.getPostcode());
            String existingKey = identityKeyFor(existingTelephone, existingEmail, existing.getLastName(),
                existingRegion);
            if (identityKey.equals(existingKey)) {
                continue;
            }
            if (existing.getId() != null && (matchId == null || existing.getId() < matchId)) {
                matchId = existing.getId();
            }
        }
        return matchId;
    }

    /**
     * The tenure, in elapsed fiscal years, that an owner must exceed before its tenure contributes
     * membership points. Tenure is measured from the owner's {@code registrationDate}, so a newly
     * created owner (whose tenure is zero) never satisfies this and thus never earns the tenure
     * points on create.
     */
    private static final int TENURE_POINTS_THRESHOLD_FISCAL_YEARS = 1;

    /**
     * The household size (number of members) at or above which an owner earns the household points.
     */
    private static final int HOUSEHOLD_POINTS_THRESHOLD = 3;

    /**
     * Computes the {@code membershipPoints} for an owner. Points start at 0, gain 2 when an email is
     * present, gain 1 when the owner's {@code namesakeCount} is 0, gain 2 when the owner's household
     * has {@value #HOUSEHOLD_POINTS_THRESHOLD} or more members, and gain 3 when the owner's tenure
     * (elapsed fiscal years since its {@code registrationDate}) exceeds
     * {@value #TENURE_POINTS_THRESHOLD_FISCAL_YEARS}. Because a newly created owner has zero tenure,
     * the tenure points never apply on create.
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
        if (tenureInFiscalYears(owner) > TENURE_POINTS_THRESHOLD_FISCAL_YEARS) {
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
     * Caps a newly computed {@code membershipLevel} so it never exceeds one above the current maximum
     * {@code membershipLevel} among the new owner's household members - the existing, non-deleted
     * owners already carrying the same {@code householdId}. When the household has no existing member
     * (or none with a recorded level) no cap applies and the level is returned unchanged; otherwise the
     * level is limited to {@code max(member level) + 1}. The count reflects the state prior to this
     * create, as the new owner has not yet been saved.
     */
    private int capMembershipLevelByHousehold(int membershipLevel, String householdId) {
        Integer maxMemberLevel = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            Integer existingLevel = existing.getMembershipLevel();
            if (existingLevel == null) {
                continue;
            }
            if (maxMemberLevel == null || existingLevel > maxMemberLevel) {
                maxMemberLevel = existingLevel;
            }
        }
        if (maxMemberLevel == null) {
            return membershipLevel;
        }
        return Math.min(membershipLevel, maxMemberLevel + 1);
    }

    /**
     * The owner's tenure in whole elapsed fiscal years: the number of 1 July fiscal-year boundaries
     * crossed between its {@code registrationDate} and the current server date. An owner with no
     * registration date, or one dated in the future, has a tenure of zero.
     */
    private static long tenureInFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return Math.max(0, fiscalYearOf(LocalDate.now()) - fiscalYearOf(registrationDate));
    }

    /**
     * The maximum number of owners any single city may contain. A create whose city already
     * holds this many owners is rejected.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Rejects a create whose city already contains {@value #CITY_CAPACITY} or more owners,
     * compared case-insensitively - the same way the city is matched when deriving the owner's
     * region. The count reflects the state prior to this create. A city
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
     * The number of owners a city must already hold before a create in it is flagged with a
     * capacity warning. Once a city holds at least this many owners - but is still below the
     * {@value #CITY_CAPACITY} hard limit - it is approaching capacity and the new owner's
     * {@code capacityWarning} is true.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Computes the {@code capacityWarning} for a create in the given city: true when the city
     * already holds between {@value #CITY_CAPACITY_WARNING_THRESHOLD} and
     * {@value #CITY_CAPACITY} (exclusive) owners - approaching the hard capacity limit - otherwise
     * false. The city is matched case-insensitively and the count reflects the state prior to this
     * create, exactly as {@link #rejectCityAtCapacity(String)} accumulates it.
     */
    private boolean capacityWarningFor(String city) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count >= CITY_CAPACITY_WARNING_THRESHOLD && count < CITY_CAPACITY;
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
     * Rolls an effective registration date forward onto a business day. A Saturday, Sunday or listed
     * public holiday is advanced to the next non-holiday business day; a plain weekday is returned
     * unchanged. This is applied to the effective registration date - whether supplied in the request
     * or defaulted to the server date - so the stored {@code registrationDate}, and everything derived
     * from it, always falls on a business day.
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

    /**
     * The fixed list of public holidays. An effective registration date that lands on any of these
     * is rolled forward, together with weekends, to the next non-holiday business day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    private static LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            OwnerDto replay = idempotentCreates.get(idempotencyKey);
            if (replay != null) {
                return new ResponseEntity<>(replay, HttpStatus.OK);
            }
        }
        String normalizedLine1 = null;
        String normalizedLine2 = null;
        String composedAddress;
        if (!isBlank(ownerFieldsDto.getAddressLine1())) {
            normalizedLine1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
            if (!isBlank(ownerFieldsDto.getAddressLine2())) {
                normalizedLine2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
            }
            composedAddress = normalizedLine2 == null
                ? normalizedLine1
                : normalizedLine1 + " " + normalizedLine2;
        } else {
            composedAddress = normalizeAddress(ownerFieldsDto.getAddress());
        }
        rejectMissingOrBlankFields(ownerFieldsDto, composedAddress);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(composedAddress);
        owner.setAddressLine1(normalizedLine1);
        owner.setAddressLine2(normalizedLine2);
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        owner.setTelephone(normalizedTelephone);
        String normalizedEmail = normalizeEmail(ownerFieldsDto.getEmail());
        owner.setEmail(normalizedEmail);
        validatePostcode(owner.getCity(), owner.getPostcode());
        String regionV2 = regionCodeV2(owner.getCity(), owner.getPostcode());
        String householdId = householdIdFor(owner.getLastName(), owner.getPostcode(), regionV2);
        rejectDuplicateIdentity(normalizedTelephone, normalizedEmail, owner.getLastName(), regionV2);
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        LocalDate effectiveDate = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        rejectDailyOwnerLimit(registrationDate);
        owner.setBulkSignupWarning(bulkSignupWarningFor(registrationDate));
        owner.setCapacityWarning(capacityWarningFor(owner.getCity()));
        owner.setRegistrationDate(registrationDate);
        owner.setMemberId(deduplicateMemberId(
            memberIdFor(regionV2, owner.getRegistrationDate(), normalizedTelephone, owner.getLastName())));
        owner.setHouseholdId(householdId);
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdMemberCount(countHouseholdMembers(owner.getHouseholdId()));
        int membershipPoints = membershipPointsFor(owner);
        owner.setMembershipPoints(membershipPoints);
        int membershipLevel = membershipLevelForPoints(membershipPoints);
        // A declared household member (sharesHousehold) keeps its point-derived level; an owner
        // admitted into an existing household implicitly has its level capped one above the
        // household's current maximum.
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            membershipLevel = capMembershipLevelByHousehold(membershipLevel, owner.getHouseholdId());
        }
        owner.setMembershipLevel(membershipLevel);
        Integer possibleDuplicateOf = findPossibleDuplicateOf(ownerFieldsDto, normalizedTelephone, normalizedEmail);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), owner.getMembershipLevel());
        emitOwnerCreatedEvent(owner);
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotentCreates.put(idempotencyKey, ownerDto);
        }
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * The schema version of the {@code OWNER_CREATED} structured event. Version 2 groups the owner
     * identity under an {@code identity} object in the API response, so the event records this
     * version alongside its payload and recomputes the owner segment from the version-2 identity.
     */
    private static final int OWNER_EVENT_SCHEMA_VERSION = 2;

    /**
     * Emits the immutable {@code OWNER_CREATED} structured event to the {@code AUDIT} logger as a
     * JSON object {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment,
     * event}}. The {@code schemaVersion} is {@value #OWNER_EVENT_SCHEMA_VERSION}; the {@code seq} is
     * a per-application monotonically increasing integer that totally orders creates; {@code memberId}
     * carries the owner's current primary identifier ({@link #primaryIdentifierOf}), now the version-2
     * memberId; and {@code ownerSegment} is recomputed from the version-2 identity. This is a distinct
     * line from the human-readable audit line.
     */
    private void emitOwnerCreatedEvent(Owner owner) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("schemaVersion", OWNER_EVENT_SCHEMA_VERSION);
        event.put("seq", OWNER_EVENT_SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", primaryIdentifierOf(owner));
        event.put("membershipLevel", owner.getMembershipLevel());
        event.put("ownerSegment", ownerMapper.deriveOwnerSegment(owner));
        event.put("event", "OWNER_CREATED");
        AUDIT.info(EVENT_MAPPER.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier carried by the {@code OWNER_CREATED} event: the unified
     * {@code memberId} into which the former {@code customerCode} has been consolidated.
     */
    private static String primaryIdentifierOf(Owner owner) {
        return owner.getMemberId();
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
