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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidFieldsException;
import org.springframework.samples.petclinic.rest.advice.RequiredFieldsMissingException;
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
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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
     * Syntactic email check: a non-empty local part, an {@code @}, and a dotted domain, with no
     * whitespace or additional {@code @} characters. Deliberately permissive - it enforces basic
     * address shape rather than full RFC 5322 conformance.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Disposable email domains that are not accepted for an owner's email. An email whose domain
     * (the part after the {@code @}, compared case-insensitively) is on this blocklist is rejected.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS = java.util.Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /**
     * Dedicated audit logger. Successful owner creation emits a single line here carrying the new
     * owner's id, customer code and registration date so the side-effect can be observed
     * independently of the HTTP response.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Monotonically increasing sequence number stamped onto each structured {@code OWNER_CREATED}
     * audit event. Shared across every create handled by this singleton controller and incremented
     * once per successful owner creation, so the events form a strictly ordered, gap-free stream.
     */
    private static final java.util.concurrent.atomic.AtomicLong AUDIT_EVENT_SEQ =
        new java.util.concurrent.atomic.AtomicLong();

    /**
     * Common street-type abbreviations expanded during address normalization. Keys and values are
     * upper-cased so the mapping is applied after the address has been upper-cased.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Maximum number of owners permitted in a single city. Once a city already contains this many
     * owners, creating another owner in that city is rejected as a conflict.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Number of owners a city must already contain for a newly created owner in it to be flagged with
     * {@code capacityWarning=true}. Once a city holds at least this many owners (but fewer than the
     * {@value #CITY_CAPACITY} hard limit), the next owner in that city signals it is approaching
     * capacity; the create itself still succeeds.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Fixed list of public holidays. A registration date that, after the weekend adjustment, lands on
     * one of these dates is rolled forward to the next non-holiday business day.
     */
    private static final java.util.Set<LocalDate> PUBLIC_HOLIDAYS = java.util.Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Maximum number of owners permitted to be registered on a single day. Once this many owners
     * already share the current registration date, creating another owner today is rejected.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Number of owner registrations on a single business day above which a newly created owner is
     * flagged with {@code bulkSignupWarning=true}. Once more than this many owners already share the
     * registration date, the surge is signalled in the response; the create itself still succeeds.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Fixed city-to-region table used to validate an owner's postcode. Mirrors the mapping used to
     * derive an owner's locality ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD});
     * a city that is not listed has no known region and accepts any 4-digit postcode.
     */
    private static final Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Region to inclusive 4-digit postcode range {@code {low, high}}: NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099. A supplied postcode outside its city's region range is rejected.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Request header carrying the client-supplied idempotency key. When a create request repeats with
     * a key that has already produced an owner, the original owner is returned with {@code 200 OK}
     * instead of creating a duplicate.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Maps a previously seen {@code Idempotency-Key} to the id of the owner it created, so a repeated
     * create with the same key resolves to the originally created owner rather than creating a
     * duplicate. Kept in memory for the lifetime of this singleton controller and safe for concurrent
     * access across requests.
     */
    private final Map<String, Integer> idempotentCreates = new java.util.concurrent.ConcurrentHashMap<>();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final jakarta.servlet.http.HttpServletRequest request;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 jakarta.servlet.http.HttpServletRequest request) {
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
     * Normalizes an owner's address: trims and collapses runs of whitespace to single spaces,
     * upper-cases the value, and expands common street-type abbreviations word-by-word
     * ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}). A {@code null} or blank value
     * normalizes to the empty string. The normalized value is what the application stores, returns
     * and compares for household detection.
     *
     * @param address the raw address value from the request, may be {@code null}
     * @return the normalized address, or the empty string when none was supplied
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
     * Applies address normalization to whichever address form the request supplied, preferring the
     * structured fields. When a non-blank {@code addressLine1} is present the structured form wins:
     * {@code addressLine1} (and {@code addressLine2} when present) are normalized in place, and the
     * flat {@code address} is set to the composed value - the normalized {@code addressLine1} with,
     * when an {@code addressLine2} was supplied, a single space and the normalized {@code addressLine2}
     * appended - so everything downstream that reads {@code address} sees the structured value. When
     * no {@code addressLine1} is supplied the flat {@code address} is normalized as before, keeping
     * earlier minimal owners backward-compatible.
     *
     * @param ownerFieldsDto the owner payload whose address fields are normalized in place
     */
    private void applyAddressFields(OwnerFieldsDto ownerFieldsDto) {
        if (StringUtils.hasText(ownerFieldsDto.getAddressLine1())) {
            String line1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
            ownerFieldsDto.setAddressLine1(line1);
            if (StringUtils.hasText(ownerFieldsDto.getAddressLine2())) {
                String line2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
                ownerFieldsDto.setAddressLine2(line2);
                ownerFieldsDto.setAddress(line1 + " " + line2);
            } else {
                ownerFieldsDto.setAddressLine2(null);
                ownerFieldsDto.setAddress(line1);
            }
        } else {
            ownerFieldsDto.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        }
    }

    /**
     * Rejects an owner payload that is missing or blank in any required field. The response body's
     * {@code errors} array lists the name of each offending field.
     *
     * @param ownerFieldsDto the owner payload to validate
     * @throws RequiredFieldsMissingException if one or more required fields are missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getAddress())) {
            missingFields.add("address");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new RequiredFieldsMissingException(missingFields);
        }
    }

    /**
     * Validates an owner's optional postcode. Postcode is optional: a {@code null} value is accepted
     * and leaves the owner without a postcode. When present it must be exactly 4 digits, and it must
     * fall within the inclusive range of the owner's city region per {@link #REGION_POSTCODE_RANGES}
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). A city with no known
     * region accepts any 4-digit postcode.
     *
     * @param postcode the raw postcode value from the request, may be {@code null}
     * @param city the owner's city, used to look up the region whose range constrains the postcode
     * @throws InvalidFieldsException if a supplied postcode is malformed or out of range for the city
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidFieldsException(List.of("postcode"));
        }
        String region = CITY_REGIONS.get(city);
        int[] range = region == null ? null : REGION_POSTCODE_RANGES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidFieldsException(List.of("postcode"));
            }
        }
    }

    /**
     * Normalizes a telephone number into E.164 form. Spaces, dashes and brackets are stripped. A
     * leading '+' and its country code are kept as given; otherwise country code '+61' is assumed
     * and a single leading '0' is dropped from the national digits. The result must be a '+'
     * followed by 8 to 15 digits.
     *
     * @param telephone the raw telephone value from the request
     * @return the E.164 telephone (e.g. {@code +61412345678})
     * @throws InvalidFieldsException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        if (telephone == null) {
            throw new InvalidFieldsException(List.of("telephone"));
        }
        String trimmed = telephone.trim();
        boolean international = trimmed.startsWith("+");
        String cleaned = trimmed.replaceAll("[\\s\\-()]", "");
        String digits;
        if (international) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidFieldsException(List.of("telephone"));
        }
        requireValidNationalLength(digits);
        return "+" + digits;
    }

    /**
     * Country codes with a fixed national-number length, checked longest-prefix first so that
     * '+61' is matched before the '+1' prefix it starts with. Australia ('61') requires 9 national
     * digits; the North American Numbering Plan ('1') requires 10.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of("61", 9, "1", 10);

    /**
     * Validates that the national number (the E.164 digits after the country code) has the length
     * required for its country. Country codes without a known fixed length are left to the general
     * E.164 length check.
     *
     * @param digits the E.164 digits (country code plus national number, without the leading '+')
     * @throws InvalidFieldsException if the national number is the wrong length for its country
     */
    private void requireValidNationalLength(String digits) {
        NATIONAL_NUMBER_LENGTHS.entrySet().stream()
            .filter(entry -> digits.startsWith(entry.getKey()))
            .max(Comparator.comparingInt(entry -> entry.getKey().length()))
            .ifPresent(entry -> {
                int nationalLength = digits.length() - entry.getKey().length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidFieldsException(List.of("telephone"));
                }
            });
    }

    /**
     * Normalizes an optional email address. Email is not required, so a missing or blank value is
     * accepted and yields {@code null} (no email stored). When a non-blank value is supplied it must
     * be a syntactically valid address; the accepted value is returned lower-cased.
     *
     * @param email the raw email value from the request, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidFieldsException if a non-blank value is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidFieldsException(List.of("email"));
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidFieldsException(List.of("email"));
        }
        return normalized;
    }

    /**
     * Tests whether an existing owner is not soft-deleted, so that duplicate and identity detection
     * can ignore owners flagged {@code deleted}. An owner whose {@code deleted} flag is {@code null}
     * or {@code false} is considered active; only an explicit {@code true} excludes it.
     *
     * @param owner an existing owner from the data store
     * @return {@code true} when the owner is not flagged deleted, otherwise {@code false}
     */
    private static boolean isNotDeleted(Owner owner) {
        return !Boolean.TRUE.equals(owner.getDeleted());
    }

    private void requireUniqueIdentity(Owner owner) {
        String identityKey = ownerMapper.identityKey(owner);
        boolean taken = this.clinicService.findAllOwners().stream()
            .filter(OwnerRestControllerV1::isNotDeleted)
            .anyMatch(existing -> identityKey.equals(ownerMapper.identityKey(existing)));
        if (taken) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * Computes the membership level ceiling for the owner being created: one above the current
     * maximum membership level among the existing members of the owner's household (those sharing its
     * computed {@link OwnerMapper#householdId(Owner) household id}, ignoring soft-deleted owners). Each
     * existing member's level is evaluated for the household size that now includes the joining owner,
     * so the ceiling reflects the household's up-to-date standing. Returns {@code null} when the
     * household has no existing member, in which case no cap applies. Invoked before the new owner is
     * persisted, so it never counts the owner itself.
     *
     * @param owner the owner being created, with its normalized fields already applied
     * @return the capped maximum membership level, or {@code null} when no existing household member
     */
    private Integer householdMembershipLevelCap(Owner owner) {
        String householdId = ownerMapper.householdId(owner);
        List<Owner> members = this.clinicService.findAllOwners().stream()
            .filter(OwnerRestControllerV1::isNotDeleted)
            .filter(existing -> householdId.equals(ownerMapper.householdId(existing)))
            .toList();
        if (members.isEmpty()) {
            return null;
        }
        int householdSize = members.size() + 1;
        int maxLevel = members.stream()
            .mapToInt(existing -> ownerMapper.householdMemberLevel(existing, householdSize))
            .max()
            .getAsInt();
        return maxLevel + 1;
    }

    /**
     * Collapses surrounding and internal whitespace and lower-cases a value so that owner household
     * fields can be compared case-insensitively with collapsed whitespace. A {@code null} value
     * normalizes to the empty string.
     *
     * @param value the raw field value, may be {@code null}
     * @return the trimmed, whitespace-collapsed, lower-cased form
     */
    private String normalizeHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Finds an existing owner that the owner being created softly matches: one that is not a hard
     * duplicate (that case is already rejected by {@link #requireUniqueIdentity(Owner)}, so its
     * identity key differs from the new owner's) but shares the new owner's surname phonetically -
     * the same {@link Soundex Soundex} code for the last name - and the same postcode. Only owners
     * with a postcode participate: when the new owner has no postcode there is nothing to soft-match
     * on. When several existing owners match, the one with the lowest id is returned so the result is
     * deterministic.
     *
     * @param owner the owner being created, with its normalized telephone already applied
     * @return the lowest-id matching existing owner, or {@code null} when there is no soft match
     */
    private Owner findPossibleDuplicate(Owner owner) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        if (lastName == null || postcode == null) {
            return null;
        }
        String soundex = Soundex.encode(lastName);
        String identityKey = ownerMapper.identityKey(owner);
        return this.clinicService.findAllOwners().stream()
            .filter(OwnerRestControllerV1::isNotDeleted)
            .filter(existing -> postcode.equals(existing.getPostcode()))
            .filter(existing -> soundex.equals(Soundex.encode(existing.getLastName())))
            .filter(existing -> !identityKey.equals(ownerMapper.identityKey(existing)))
            .min(Comparator.comparingInt(Owner::getId))
            .orElse(null);
    }

    /**
     * Rejects an owner whose city already contains the maximum permitted number of owners
     * ({@value #CITY_CAPACITY}). Cities are compared case-insensitively with collapsed whitespace,
     * the same normalization used for the other owner household fields.
     *
     * @param city the city of the owner being created
     * @throws CityAtCapacityException if the city already contains {@value #CITY_CAPACITY} or more owners
     */
    private void requireCityHasCapacity(String city) {
        String normalizedCity = normalizeHousehold(city);
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHousehold(existing.getCity()).equals(normalizedCity))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * Determines whether creating an owner in the given city is approaching the per-city capacity
     * limit: {@code true} when the city already contains between {@value #CITY_CAPACITY_WARNING_THRESHOLD}
     * and {@value #CITY_CAPACITY} (exclusive) owners. Cities are compared case-insensitively with
     * collapsed whitespace, matching {@link #requireCityHasCapacity(String)}. Evaluated before the new
     * owner is persisted, so the count reflects the owners that existed at the time of creation; the
     * hard rejection at {@value #CITY_CAPACITY} has already run, so the count seen here is below it.
     *
     * @param city the city of the owner being created
     * @return {@code true} when the city already holds 40-49 owners, otherwise {@code false}
     */
    private boolean isApproachingCityCapacity(String city) {
        String normalizedCity = normalizeHousehold(city);
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHousehold(existing.getCity()).equals(normalizedCity))
            .count();
        return count >= CITY_CAPACITY_WARNING_THRESHOLD && count < CITY_CAPACITY;
    }

    /**
     * Rejects creating an owner once the given business day has already reached the maximum
     * permitted number of owner registrations ({@value #DAILY_OWNER_LIMIT}). Existing owners are
     * counted by their {@code registrationDate} against the supplied (already business-day-adjusted)
     * registration date.
     *
     * @param registrationDate the business-day-adjusted registration date of the owner being created
     * @throws DailyOwnerLimitException if {@value #DAILY_OWNER_LIMIT} or more owners are already
     *     registered on that business day
     */
    private void requireDailyLimitNotReached(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitException(registrationDate);
        }
    }

    /**
     * Determines whether creating an owner on the given business day constitutes a bulk-signup
     * surge: {@code true} when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already
     * been registered on that date. Evaluated before the new owner is persisted, so the count
     * reflects the owners that existed at the time of creation.
     *
     * @param registrationDate the business-day-adjusted registration date of the owner being created
     * @return {@code true} if more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners already share
     *     that registration date, otherwise {@code false}
     */
    private boolean isBulkSignup(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Rejects a supplied registration date that is later than the current server date. A {@code null}
     * date is permitted (it is subsequently defaulted to the server date); only an explicitly supplied
     * date in the future is invalid.
     *
     * @param registrationDate the registration date supplied on the request, may be {@code null}
     * @throws InvalidFieldsException if the supplied date is later than today's server date
     */
    private void requireRegistrationDateNotInFuture(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidFieldsException(List.of("registrationDate"));
        }
    }

    /**
     * Rolls a registration date forward onto a business day: any Saturday, Sunday or listed public
     * holiday is advanced day by day until the first weekday that is not a public holiday, while a
     * plain weekday is returned unchanged.
     *
     * @param date the effective registration date (supplied or defaulted to the server date)
     * @return the same date when it is a non-holiday weekday, otherwise the next non-holiday business day
     */
    private LocalDate toBusinessDay(LocalDate date) {
        LocalDate result = date;
        while (isWeekend(result) || PUBLIC_HOLIDAYS.contains(result)) {
            result = result.plusDays(1);
        }
        return result;
    }

    private boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (StringUtils.hasText(idempotencyKey)) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        applyAddressFields(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        validatePostcode(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        requireCityHasCapacity(ownerFieldsDto.getCity());
        String email = normalizeEmail(ownerFieldsDto.getEmail());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(telephone);
        owner.setEmail(email);
        owner.setDeleted(false);
        requireUniqueIdentity(owner);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        requireRegistrationDateNotInFuture(owner.getRegistrationDate());
        LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        owner.setRegistrationDate(registrationDate);
        requireDailyLimitNotReached(registrationDate);
        owner.setBulkSignupWarning(isBulkSignup(registrationDate));
        owner.setCapacityWarning(isApproachingCityCapacity(ownerFieldsDto.getCity()));
        Owner possibleDuplicate = sharesHousehold ? null : findPossibleDuplicate(owner);
        owner.setPossibleDuplicate(possibleDuplicate != null);
        owner.setPossibleDuplicateOf(possibleDuplicate == null ? null : possibleDuplicate.getId());
        owner.setMembershipLevelCap(householdMembershipLevelCap(owner));
        this.clinicService.saveOwner(owner);
        if (StringUtils.hasText(idempotencyKey)) {
            idempotentCreates.put(idempotencyKey, owner.getId());
        }
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            ownerMapper.membershipLevel(owner));
        AUDIT.info("{}", new OwnerCreatedEvent(AUDIT_EVENT_SEQ.incrementAndGet(), owner.getId(),
            primaryIdentifier(owner), ownerMapper.membershipLevel(owner)).toJson());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * The owner's primary identifier as carried by the structured {@code OWNER_CREATED} audit event:
     * the unified {@code memberId}, into which the former {@code customerCode} has been folded.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Immutable structured record of a successful owner creation, serialized as a single JSON object
     * to the {@code AUDIT} logger alongside the human-readable audit line. The {@code memberId}
     * field carries the owner's primary identifier (see {@link #primaryIdentifier(Owner)}).
     */
    private record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
                                     Integer membershipLevel) {

        String toJson() {
            return "{\"seq\":" + seq
                + ",\"ownerId\":" + ownerId
                + ",\"memberId\":" + jsonString(memberId)
                + ",\"membershipLevel\":" + membershipLevel
                + ",\"event\":\"OWNER_CREATED\"}";
        }

        private static String jsonString(String value) {
            if (value == null) {
                return "null";
            }
            StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> {
                        if (c < 0x20) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                    }
                }
            }
            return sb.append('"').toString();
        }
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
