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

package org.springframework.samples.petclinic.rest.controller;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.HashUtils;
import org.springframework.samples.petclinic.util.IdentityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Owns the owner-creation concern: the single place that turns a submitted
 * {@link OwnerFieldsDto} into a validated, normalized, enriched and persisted {@link Owner},
 * and reports the create outcome as the HTTP response the endpoint returns.
 *
 * <p>Keeping the whole create pipeline here means every rule that fires on create — the
 * registration-date and business-day handling, the per-day and per-city quotas, the
 * address/telephone/email/postcode normalization and validation, the household, duplicate
 * and soft-match identity rules, and the derived customer code, namesake count and
 * membership number — reads the one owner this class builds, in the one order it applies
 * them, so they can never drift apart. The controller is left holding only the endpoint's
 * HTTP boundary.
 */
@Component
public class OwnerRegistrar {

    /** Minimal syntactic check for an email address: a non-empty local part, a single
     *  '@', and a dotted domain, none containing whitespace or a second '@'. */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** A postcode, when supplied, must be exactly four digits before its region range is checked. */
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("^[0-9]{4}$");

    /** Disposable email domains that are rejected: an owner whose email domain matches one of
     *  these (case-insensitively) is refused with 400. */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final TelephoneNormalizer telephoneNormalizer;

    private final AddressNormalizer addressNormalizer;

    private final CityRegionResolver cityRegionResolver;

    private final HouseholdResolver householdResolver;

    private final OwnerAuditor ownerAuditor;

    /**
     * Remembers, per {@code Idempotency-Key}, the id of the owner that key's create originally
     * produced, so a later create carrying the same key returns that same owner instead of
     * creating a duplicate. The registrar is a singleton, so this store outlives a single
     * request; entries are keyed by the client-supplied key, which is expected to be unique
     * per logical create.
     */
    private final java.util.concurrent.ConcurrentHashMap<String, Integer> idempotencyKeys =
        new java.util.concurrent.ConcurrentHashMap<>();

    public OwnerRegistrar(ClinicService clinicService,
                          OwnerMapper ownerMapper,
                          TelephoneNormalizer telephoneNormalizer,
                          AddressNormalizer addressNormalizer,
                          CityRegionResolver cityRegionResolver,
                          HouseholdResolver householdResolver,
                          OwnerAuditor ownerAuditor) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.telephoneNormalizer = telephoneNormalizer;
        this.addressNormalizer = addressNormalizer;
        this.cityRegionResolver = cityRegionResolver;
        this.householdResolver = householdResolver;
        this.ownerAuditor = ownerAuditor;
    }

    /**
     * Validate, normalize, enrich and persist a new owner from the submitted fields, and
     * report the outcome as the response the create endpoint returns: {@code 201 Created}
     * (with a {@code Location} header and the created owner) on success, or the appropriate
     * rejection status ({@code 400}, {@code 409} or {@code 429}) when a create rule refuses
     * the request. No owner is persisted unless every rule passes.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @return the create outcome as an HTTP response
     */
    public ResponseEntity<OwnerDto> register(OwnerFieldsDto ownerFieldsDto) {
        return register(ownerFieldsDto, null);
    }

    /**
     * As {@link #register(OwnerFieldsDto)}, but honouring an optional {@code Idempotency-Key}.
     * When {@code idempotencyKey} is non-blank and a create with that key has already succeeded,
     * the owner it originally produced is returned with {@code 200 OK} instead of creating a
     * duplicate. Otherwise the normal create pipeline runs and, on success, the key is recorded
     * against the created owner so a later repeat resolves to the same owner.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @param idempotencyKey the request's {@code Idempotency-Key} header, or {@code null} when absent
     * @return the create outcome as an HTTP response
     */
    public ResponseEntity<OwnerDto> register(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        String key = (idempotencyKey == null || idempotencyKey.isBlank()) ? null : idempotencyKey.trim();
        if (key != null) {
            Integer existingId = idempotencyKeys.get(key);
            if (existingId != null) {
                Owner original = this.clinicService.findOwnerById(existingId);
                if (original != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(original), HttpStatus.OK);
                }
                // The originally created owner is gone; forget the stale key and create afresh.
                idempotencyKeys.remove(key);
            }
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        // A supplied registration date may not be later than the server's current date.
        if (owner.getRegistrationDate().isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setRegistrationDate(toBusinessDay(owner.getRegistrationDate()));
        LocalDate registrationDate = owner.getRegistrationDate();
        long ownersCreatedThatDay = this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(registrationDate::equals)
            .count();
        if (ownersCreatedThatDay >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        String address = normalizedAddress(owner);
        if (address == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(address);
        String telephone = telephoneNormalizer.normalize(owner.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!telephoneNormalizer.hasValidNationalNumberLength(telephone)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setTelephone(telephone);
        String email = owner.getEmail();
        if (email != null) {
            email = email.trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT);
            if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            owner.setEmail(email.toLowerCase(Locale.ROOT));
        }
        // A postcode is optional, but when supplied it must be four digits and valid for the
        // owner's city per the fixed region table; a city with no known region accepts any
        // 4-digit postcode. Reject an out-of-range or malformed postcode with 400.
        String postcode = owner.getPostcode();
        if (postcode != null) {
            if (!POSTCODE_PATTERN.matcher(postcode).matches()
                    || !cityRegionResolver.isPostcodeValidForCity(owner.getCity(), postcode)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        // The owner's telephone and email are now in their canonical, stored form.
        // The household id is deterministic: owners with the same normalized last name and
        // postcode share it automatically. Assign it unconditionally so the membership-level
        // ceiling can key off it; it no longer participates in duplicate detection, which now
        // rests solely on the identity key below.
        owner.setHouseholdId(householdResolver.householdId(owner));
        // Duplicate detection is the single identity key: a candidate is rejected only when its
        // whole identityKey (normalized telephone, lower-cased email and soundex of the last name)
        // collides with a non-deleted owner's. Two people sharing a household with different
        // telephones therefore derive different keys and are both admitted; the second is instead
        // flagged as a possible duplicate by the soft-match rule below.
        if (isDuplicateOwner(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        String cityKey = IdentityUtils.normalizeIdentity(owner.getCity());
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> IdentityUtils.normalizeIdentity(existing.getCity()).equals(cityKey))
            .count();
        if (ownersInCity >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        owner.setCustomerCode(customerCode(owner));
        owner.setNamesakeCount(namesakeCount(owner));
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setBulkSignupWarning(ownersCreatedThatDay > 80);
        // Soft-match: the candidate has already cleared the hard-duplicate check, but its
        // identityKey may still differ from an existing owner's while their last names sound alike
        // (equal soundex) and their postcodes match — a likely re-registration of the same person
        // under a different telephone or email. When it does, it is created but flagged as a
        // possible duplicate of that existing owner; otherwise the flag is false and no reference
        // is recorded.
        Integer possibleDuplicateOf = possibleDuplicateOf(owner);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        this.clinicService.saveOwner(owner);
        if (key != null) {
            idempotencyKeys.put(key, owner.getId());
        }
        ownerAuditor.ownerCreated(owner, ownerMapper.membershipLevel(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * The owner's postal address in the single canonical form under which it is stored, or
     * {@code null} when the owner supplies no usable address. An owner may supply its address
     * in EITHER form: the structured {@code addressLine1} (with an optional {@code addressLine2})
     * is preferred, and the flat {@code address} remains accepted for backward compatibility.
     *
     * <p>Normalization by {@link AddressNormalizer} is applied to whichever fields are supplied.
     * When a non-blank {@code addressLine1} is present the owner's structured fields are replaced
     * with their normalized values (a blank or absent {@code addressLine2} is cleared to
     * {@code null}) and the returned address is composed as the normalized {@code addressLine1},
     * with a single space and the normalized {@code addressLine2} appended when present. Otherwise
     * the flat {@code address} is normalized and the structured fields are cleared. A blank result
     * (no usable address in either form) has no canonical form and is reported as {@code null} so
     * the caller can reject the request with 400.
     *
     * <p>This is the single point at which an owner's address is resolved and normalized on
     * create, so every rule keyed on the stored address reads the one value produced here.
     *
     * @param owner the owner about to be created
     * @return the canonical stored address, or {@code null} when the owner supplies none
     */
    private String normalizedAddress(Owner owner) {
        String rawLine1 = owner.getAddressLine1();
        if (rawLine1 != null && !rawLine1.isBlank()) {
            String line1 = addressNormalizer.normalize(rawLine1);
            String line2 = addressNormalizer.normalize(owner.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2.isBlank() ? null : line2);
            return line2.isBlank() ? line1 : line1 + " " + line2;
        }
        owner.setAddressLine1(null);
        owner.setAddressLine2(null);
        String address = addressNormalizer.normalize(owner.getAddress());
        return address.isBlank() ? null : address;
    }

    /**
     * Whether {@code candidate} collides with an already-registered owner under the single
     * identity rule enforced on create. This is the one point at which a new owner is
     * rejected as a duplicate (HTTP 409): all duplicate detection is consolidated into the
     * derived {@link OwnerMapper#identityKey identityKey} — the SHA-256 hex over the normalized
     * telephone, the lower-cased email (or an empty string when absent) and the soundex of the
     * last name, joined by '|'. The candidate clashes only when its WHOLE identityKey equals an
     * existing owner's; because the telephone is part of the key, two people sharing a household
     * (a like-sounding last name and postcode) with different telephones have different
     * identityKeys and are both allowed.
     *
     * <p>The candidate arrives with its telephone and email already normalized, and each existing
     * owner is stored in the same canonical form, so an identityKey match means the two owners are
     * genuinely the same identity.
     *
     * @param candidate the fully-normalized owner about to be created
     * @return {@code true} if an existing owner already occupies the candidate's identityKey
     */
    private boolean isDuplicateOwner(Owner candidate) {
        String identityKey = OwnerMapper.identityKey(candidate);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .map(OwnerMapper::identityKey)
            .anyMatch(identityKey::equals);
    }

    /**
     * The id of the existing owner the fully-normalized {@code candidate} soft-matches, or
     * {@code null} when there is none. A soft match is an owner whose whole identityKey differs
     * from the candidate's (so it is not a hard duplicate) yet whose last name sounds alike (equal
     * {@link IdentityUtils#soundex soundex}) and whose postcode matches — a likely re-registration
     * of the same person under a different telephone or email. Both must supply a postcode for a
     * match to be possible; when several existing owners qualify the lowest id is returned so the
     * result is deterministic.
     *
     * @param candidate the fully-normalized owner about to be created
     * @return the matching existing owner's id, or {@code null} if the candidate is no soft match
     */
    private Integer possibleDuplicateOf(Owner candidate) {
        String postcode = candidate.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String identityKey = OwnerMapper.identityKey(candidate);
        String lastNameSoundex = IdentityUtils.soundex(candidate.getLastName());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted()
                && !identityKey.equals(OwnerMapper.identityKey(existing))
                && IdentityUtils.soundex(existing.getLastName()).equals(lastNameSoundex)
                && postcode.equals(existing.getPostcode()))
            .map(Owner::getId)
            .filter(java.util.Objects::nonNull)
            .min(Integer::compareTo)
            .orElse(null);
    }

    /**
     * Build the customer code assigned to {@code owner} on create, formatted
     * {@code '<REGION>-<HASH8>'}: REGION is the region code derived from the owner's postcode
     * (falling back to the city) via {@link CityRegionResolver}, and HASH8 is the first eight
     * upper-case hex characters of the SHA-256 digest of the normalized telephone concatenated
     * with the last name (e.g. 'NSW-1A2B3C4D'). There is no per-city sequence.
     *
     * <p>When the computed code collides with an already-registered owner's customerCode it is
     * de-duplicated by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that
     * makes it unique (e.g. 'NSW-1A2B3C4D-2'), and the unique code is returned.
     */
    private String customerCode(Owner owner) {
        String region = cityRegionResolver.regionFor(owner.getCity(), owner.getPostcode());
        String hash8 = HashUtils.sha256HexPrefix(owner.getTelephone() + owner.getLastName(), 8);
        String base = region + "-" + hash8;
        java.util.Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        if (!existing.contains(base)) {
            return base;
        }
        int n = 2;
        while (existing.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /**
     * The number of existing owners that share {@code owner}'s firstName and lastName,
     * compared case-insensitively. The candidate is not yet stored at the point this is
     * assigned, so it does not count itself. This is the value stored as the owner's
     * namesakeCount on create.
     */
    private int namesakeCount(Owner owner) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> owner.getFirstName().equalsIgnoreCase(existing.getFirstName())
                && owner.getLastName().equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /** Fixed list of public holidays that a registration date must roll past. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Roll a registration date forward to the next business day: a Saturday, Sunday
     * or listed public holiday advances one day at a time until it lands on a weekday
     * that is not a public holiday. A plain weekday is returned unchanged.
     */
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

    /**
     * Build the membership number assigned on create, formatted
     * {@code '<customerCode>-M<YY>'} where YY is the last two digits of the fiscal year of the
     * business-day-adjusted {@code registrationDate} (the fiscal year starts on 1 July), e.g.
     * 'NSW-1A2B3C4D-M26'.
     */
    private static String membershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", OwnerMapper.fiscalYear(registrationDate) % 100);
        return customerCode + "-M" + yy;
    }
}
