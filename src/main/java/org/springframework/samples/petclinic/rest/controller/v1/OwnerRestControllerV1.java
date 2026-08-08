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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.springframework.samples.petclinic.audit.OwnerCreatedEvent;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.OwnerIdentity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

    /**
     * Syntactic email validation: a non-empty local part, a single '@', and a
     * domain with at least one dot-separated label ending in a letter-only TLD.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /**
     * Disposable / throw-away email domains whose owners are rejected: an email on one of these
     * domains is treated as invalid input and rejected with 400. Compared case-insensitively.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Fixed public-holiday list. A registration date that lands on one of these is not a business
     * day and is rolled forward to the next non-holiday weekday.
     */
    private static final java.util.Set<LocalDate> PUBLIC_HOLIDAYS =
        java.util.Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    /** Dedicated audit logger for owner lifecycle side-effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Dedicated logger for outbound member notifications (e.g. the welcome notification). */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Monotonically increasing sequence number stamped onto the structured {@code OWNER_CREATED}
     * audit event, incremented once per successful create so events are strictly ordered.
     */
    private static final AtomicLong CREATE_SEQUENCE = new AtomicLong();

    /** Request header carrying the client-supplied idempotency key for owner creation. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers which owner was created for each already-seen idempotency key, so a repeated create
     * carrying a key seen before returns the originally created owner instead of creating a duplicate.
     */
    private final Map<String, Integer> idempotentCreations = new ConcurrentHashMap<>();

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
     * Derive the stable, shared household identifier for owners in the same household: the first 12
     * lower-case hex characters of the SHA-256 digest over
     * {@code '<VERSION_TAG>|<normalizedLastName>|<postcode>'}, so every owner with the same last name
     * and postcode resolves to the same value regardless of the order in which they were created. The
     * fixed {@code V2} version tag is mixed in so the value differs from its version-1 form.
     */
    private static String householdId(String lastNameKey, String postcode) {
        return hashHex(OwnerIdentity.VERSION_TAG + "|" + lastNameKey + "|" + (postcode == null ? "" : postcode),
            12, false);
    }

    /**
     * Count how many owners belong to the given owner's household (owners sharing its normalized last
     * name and postcode). An owner without a postcode has no household peers and counts as a household
     * of one.
     */
    private int householdSize(Owner owner) {
        if (owner.getPostcode() == null) {
            return 1;
        }
        String householdId = householdId(normaliseIdentity(owner.getLastName()), owner.getPostcode());
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getPostcode() != null
                && householdId.equals(
                    householdId(normaliseIdentity(existing.getLastName()), existing.getPostcode())))
            .count();
    }

    /**
     * The existing, non-deleted owners that belong to {@code owner}'s household (sharing its
     * normalized last name and postcode), excluding {@code owner} itself. An owner without a
     * postcode has no household peers, so this is empty.
     */
    private List<Owner> otherHouseholdMembers(Owner owner) {
        if (owner.getPostcode() == null) {
            return List.of();
        }
        String householdId = householdId(normaliseIdentity(owner.getLastName()), owner.getPostcode());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> existing.getPostcode() != null
                && householdId.equals(
                    householdId(normaliseIdentity(existing.getLastName()), existing.getPostcode())))
            .toList();
    }

    /**
     * The owner's membership level after applying the household ceiling: the natural level derived
     * from the owner's membership points, capped at one above the current maximum membership level
     * among the owner's existing household members. With no existing household member no cap applies.
     * Assumes the owner's household size has already been populated.
     */
    private Integer cappedMembershipLevel(Owner owner) {
        Integer natural = ownerMapper.formatMembershipLevel(owner);
        if (natural == null) {
            return null;
        }
        List<Owner> members = otherHouseholdMembers(owner);
        if (members.isEmpty()) {
            return natural;
        }
        int maxMemberLevel = 0;
        for (Owner member : members) {
            member.setHouseholdSize(householdSize(member));
            Integer memberLevel = ownerMapper.formatMembershipLevel(member);
            if (memberLevel != null) {
                maxMemberLevel = Math.max(maxMemberLevel, memberLevel);
            }
        }
        return Math.min(natural, maxMemberLevel + 1);
    }

    /**
     * Build the unified member id for a new owner as {@code '<REGION><FY><HASH8><CHK>'}, where REGION is
     * the version-2 region code — the fixed {@code V2} version tag prepended to the region derived from
     * the postcode (falling back to the city-to-region table, else {@code "UNKNOWN"}) — FY is the
     * two-digit fiscal year (starting 1 July) of the business-day-adjusted {@code registrationDate},
     * HASH8 is the first 8 upper-case hex characters of the SHA-256 digest over
     * {@code normalizedTelephone + lastName}, and CHK is a single Luhn check digit computed over the
     * digits of {@code '<REGION><FY><HASH8>'}. The version tag ensures the id differs from its
     * version-1 form; the id otherwise depends only on the owner's own region, fiscal year and
     * identity, not on how many owners already exist.
     */
    private String nextMemberId(String region, LocalDate registrationDate,
                                String normalizedTelephone, String lastName) {
        String fiscalYear = String.format("%02d", fiscalYearStart(registrationDate) % 100);
        String hash8 = hash8(normalizedTelephone + (lastName == null ? "" : lastName));
        String base = OwnerIdentity.regionCode(region) + fiscalYear + hash8;
        return base + luhnCheckDigit(base);
    }

    /**
     * De-duplicate a computed member id against the ids already assigned to existing owners: when
     * {@code memberId} is unused it is returned unchanged, otherwise {@code '-<n>'} is appended with
     * the smallest {@code n} of 2 or more that makes the result unique among existing owners.
     */
    private String deduplicateMemberId(String memberId) {
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (!existing.contains(memberId)) {
            return memberId;
        }
        int n = 2;
        while (existing.contains(memberId + "-" + n)) {
            n++;
        }
        return memberId + "-" + n;
    }

    /**
     * The starting calendar year of the fiscal year (which starts on 1 July) that contains the given
     * date: the date's own year when it falls on or after 1 July, otherwise the previous year.
     */
    private static int fiscalYearStart(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits contained in {@code input}
     * (non-digit characters are ignored).
     */
    private static int luhnCheckDigit(String input) {
        int sum = 0;
        boolean dbl = true;
        for (int i = input.length() - 1; i >= 0; i--) {
            char c = input.charAt(i);
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
     * First 8 upper-case hex characters of the SHA-256 digest of the UTF-8 bytes of {@code input}.
     */
    private static String hash8(String input) {
        return hashHex(input, 8, true);
    }

    /**
     * First {@code length} hex characters of the SHA-256 digest of the UTF-8 bytes of {@code input},
     * upper-cased when {@code upperCase} is true and lower-cased otherwise.
     */
    private static String hashHex(String input, int length, boolean upperCase) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hex = sb.substring(0, length);
            return upperCase ? hex.toUpperCase() : hex;
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Resolve an owner's region preferring the postcode: the region is taken from the postcode range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) when the postcode resolves, otherwise from the
     * fixed city-to-region table, else {@code "UNKNOWN"}.
     */
    private static String region(String postcode, String city) {
        String fromPostcode = regionForPostcode(postcode);
        return fromPostcode != null ? fromPostcode : regionForCity(city);
    }

    /**
     * Region for a 4-digit postcode by inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099),
     * or {@code null} when the postcode is absent, not exactly 4 digits, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Roll a registration date forward to the next business day: when it falls on a Saturday or
     * Sunday, or on a listed public holiday, it is advanced day by day to the next non-holiday
     * weekday; ordinary weekday dates are returned unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(date)) {
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

    /**
     * Region -> inclusive 4-digit postcode range {@code {low, high}} used to validate a supplied
     * postcode against the owner's city region.
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Resolve an owner's region from the city using the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}); {@code "UNKNOWN"}
     * when the city is not listed.
     */
    private static String regionForCity(String city) {
        return switch (city == null ? "" : city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Validate a supplied postcode: it must be exactly 4 digits, and when the owner's city maps to a
     * known region the numeric value must fall within that region's inclusive range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
     */
    private static boolean isPostcodeValid(String postcode, String city) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(regionForCity(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * Whether the email's domain (the part after the final '@') is on the disposable-domain
     * blocklist. A {@code null} email is not disposable; the comparison is case-insensitive.
     */
    private static boolean isDisposableEmailDomain(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
    }

    /**
     * Whether the owner's city is approaching the per-city capacity limit of 50 owners: true when the
     * city already holds between 40 and 49 owners (counted the same way as the hard-limit rule, over
     * all owners with a matching, normalized city), otherwise false.
     */
    private boolean capacityWarning(String city) {
        String cityKey = normaliseIdentity(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normaliseIdentity(existing.getCity()).equals(cityKey))
            .count();
        return ownersInCity >= 40 && ownersInCity <= 49;
    }

    /**
     * The soft, review-triggering per-city capacity threshold (as opposed to the hard 50-owner limit
     * that rejects a create): a city holding this many owners or more is considered over soft capacity.
     */
    private static final int SOFT_CITY_CAPACITY = 40;

    /**
     * Whether the owner's city is over its soft capacity: true when the city already holds
     * {@link #SOFT_CITY_CAPACITY} owners or more (counted the same way as the hard-limit rule, over all
     * owners with a matching, normalized city), otherwise false.
     */
    private boolean overSoftCityCapacity(String city) {
        String cityKey = normaliseIdentity(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normaliseIdentity(existing.getCity()).equals(cityKey))
            .count();
        return ownersInCity >= SOFT_CITY_CAPACITY;
    }

    /**
     * Whether the email's domain (the part after the final '@') is disposable-adjacent: it is one of
     * the disposable domains, or a subdomain of one (it ends with {@code '.<disposableDomain>'}). This
     * is broader than {@link #isDisposableEmailDomain(String)} (which rejects an exact disposable
     * domain at create time): an owner whose stored email sits just off the blocklist is flagged for
     * review rather than rejected. A {@code null} email is not disposable-adjacent; the comparison is
     * case-insensitive.
     */
    private static boolean isDisposableAdjacentEmailDomain(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        for (String disposable : DISPOSABLE_EMAIL_DOMAINS) {
            if (domain.equals(disposable) || domain.endsWith("." + disposable)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The owner's risk flag: true when any of these hold — the owner is a possible duplicate
     * (a {@code possibleDuplicateOf} target exists), the email domain is disposable-adjacent, or the
     * owner's city is over its soft capacity; otherwise false.
     */
    private boolean riskFlag(Owner owner) {
        return owner.getPossibleDuplicateOf() != null
            || isDisposableAdjacentEmailDomain(owner.getEmail())
            || overSoftCityCapacity(owner.getCity());
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
        owners.forEach(owner -> owner.setHouseholdSize(householdSize(owner)));
        return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        owner.setHouseholdSize(householdSize(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setMembershipLevel(cappedMembershipLevel(owner));
        ownerDto.setBulkSignupWarning(owner.getRegistrationDate() != null
            && ownersCreatedOn(owner.getRegistrationDate()) > 80);
        ownerDto.setCapacityWarning(capacityWarning(owner.getCity()));
        ownerDto.setRiskFlag(riskFlag(owner));
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    /**
     * The value of the {@code Idempotency-Key} header on the current request, or {@code null} when the
     * request carries no such header (or there is no active servlet request). A blank value is treated
     * as absent.
     */
    private static String currentIdempotencyKey() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        String key = servletAttributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        return (key == null || key.isBlank()) ? null : key;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = this.idempotentCreations.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    existing.setHouseholdSize(householdSize(existing));
                    OwnerDto existingDto = ownerMapper.toOwnerDto(existing);
                    existingDto.setMembershipLevel(cappedMembershipLevel(existing));
                    existingDto.setBulkSignupWarning(existing.getRegistrationDate() != null
                        && ownersCreatedOn(existing.getRegistrationDate()) > 80);
                    existingDto.setCapacityWarning(capacityWarning(existing.getCity()));
                    existingDto.setRiskFlag(riskFlag(existing));
                    return new ResponseEntity<>(existingDto, HttpStatus.OK);
                }
            }
        }
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The supplied telephone number is not valid");
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The supplied email address is not valid");
        }
        if (isDisposableEmailDomain(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Disposable email addresses are not accepted");
        }
        // Structured address is preferred when supplied (a non-blank 'addressLine1'); otherwise the
        // flat 'address' input is used for backward compatibility. Normalization applies to whichever
        // fields are supplied, and the composed 'address' is the normalized addressLine1 with the
        // normalized addressLine2 appended after a single space when present.
        String addressLine1 = normaliseAddress(ownerFieldsDto.getAddressLine1());
        String addressLine2 = normaliseAddress(ownerFieldsDto.getAddressLine2());
        boolean structuredAddress = !addressLine1.isEmpty();
        String address;
        if (structuredAddress) {
            address = addressLine2.isEmpty() ? addressLine1 : addressLine1 + " " + addressLine2;
        } else {
            address = normaliseAddress(ownerFieldsDto.getAddress());
        }
        if (address.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An address is required");
        }
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode != null && !isPostcodeValid(postcode, ownerFieldsDto.getCity())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The supplied postcode is not valid for the city");
        }
        if (ownerFieldsDto.getRegistrationDate() != null
            && ownerFieldsDto.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The registration date cannot be in the future");
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
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "The daily owner-registration limit has been reached");
        }
        String cityKey = normaliseIdentity(ownerFieldsDto.getCity());
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normaliseIdentity(existing.getCity()).equals(cityKey))
            .count();
        if (ownersInCity >= 50) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The city has reached its owner capacity");
        }
        String lastNameKey = normaliseIdentity(ownerFieldsDto.getLastName());
        // The household is deterministically keyed on (normalized last name, postcode): every owner
        // with a postcode belongs to the household identified by this computed id, and owners sharing
        // a last name and postcode share it automatically. A new owner may join an existing household;
        // the household ceiling below (rather than a hard rejection) governs its membership level.
        String householdId = (postcode == null) ? null : householdId(lastNameKey, postcode);
        // Duplicate detection is the single identity key (normalized telephone, lower-cased email and
        // Soundex of the last name). A candidate whose key collides with an existing, non-deleted
        // owner is a hard duplicate (409); the household id no longer participates in this decision.
        String identityKey = OwnerIdentity.identityKey(telephone, email, ownerFieldsDto.getLastName());
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> OwnerIdentity.identityKey(toE164(existing.getTelephone()),
                existing.getEmail(), existing.getLastName()).equals(identityKey));
        if (identityInUse) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An owner with the same identity already exists");
        }
        // Soft match: an existing, non-deleted owner sharing this owner's postcode and last-name
        // Soundex but with a different identity key (e.g. a different telephone) is not a hard
        // duplicate, but the new owner is flagged as a possible duplicate of it.
        String lastNameSoundex = OwnerIdentity.soundex(ownerFieldsDto.getLastName());
        Integer possibleDuplicateOf = (postcode == null) ? null
            : this.clinicService.findAllOwners().stream()
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> postcode.equals(existing.getPostcode()))
                .filter(existing -> lastNameSoundex.equals(OwnerIdentity.soundex(existing.getLastName())))
                .filter(existing -> !OwnerIdentity.identityKey(toE164(existing.getTelephone()),
                    existing.getEmail(), existing.getLastName()).equals(identityKey))
                .map(Owner::getId)
                .findFirst()
                .orElse(null);
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
        if (structuredAddress) {
            owner.setAddressLine1(addressLine1);
            owner.setAddressLine2(addressLine2.isEmpty() ? null : addressLine2);
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
        }
        owner.setTelephone(telephone);
        owner.setEmail(email == null ? null : email.toLowerCase());
        owner.setRegistrationDate(registrationDate);
        owner.setMemberId(deduplicateMemberId(nextMemberId(
            region(postcode, owner.getCity()), registrationDate, telephone, owner.getLastName())));
        owner.setHouseholdId(householdId);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            this.idempotentCreations.put(idempotencyKey, owner.getId());
        }
        owner.setHouseholdSize(householdSize(owner));
        Integer membershipLevel = cappedMembershipLevel(owner);
        AUDIT.info("owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
        // Immutable structured event carrying the owner's current primary identifier (the
        // unified memberId), with a monotonic sequence.
        AUDIT.info(new OwnerCreatedEvent(CREATE_SEQUENCE.incrementAndGet(), owner.getId(),
            owner.getMemberId(), membershipLevel).toJson());
        // Enqueue a welcome notification for the newly created owner, carrying its id and memberId.
        NOTIFY.info("welcome notification: ownerId={} memberId={}",
            owner.getId(), owner.getMemberId());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setMembershipLevel(membershipLevel);
        ownerDto.setBulkSignupWarning(ownersCreatedThatDay > 80);
        ownerDto.setCapacityWarning(capacityWarning(owner.getCity()));
        ownerDto.setRiskFlag(riskFlag(owner));
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        String telephone = toE164(ownerFieldsDto.getTelephone());
        if (telephone == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The supplied telephone number is not valid");
        }
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String email = ownerFieldsDto.getEmail();
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The supplied email address is not valid");
        }
        String addressLine1 = normaliseAddress(ownerFieldsDto.getAddressLine1());
        String addressLine2 = normaliseAddress(ownerFieldsDto.getAddressLine2());
        if (!addressLine1.isEmpty()) {
            currentOwner.setAddressLine1(addressLine1);
            currentOwner.setAddressLine2(addressLine2.isEmpty() ? null : addressLine2);
            currentOwner.setAddress(addressLine2.isEmpty() ? addressLine1 : addressLine1 + " " + addressLine2);
        } else {
            currentOwner.setAddressLine1(null);
            currentOwner.setAddressLine2(null);
            currentOwner.setAddress(normaliseAddress(ownerFieldsDto.getAddress()));
        }
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
        // Soft delete: flag the owner deleted and retain the record so it can still be read back.
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
