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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

    /**
     * The HTTP request header a client may send to make a create idempotent. When a create repeats
     * with a key already seen, the originally created owner is returned instead of a duplicate.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per {@link #IDEMPOTENCY_KEY_HEADER Idempotency-Key}, the id of the owner that key's
     * first successful create produced, so a repeat of that create can return the same owner rather
     * than creating a duplicate.
     */
    private final Map<String, Integer> idempotentCreates = new ConcurrentHashMap<>();

    /**
     * Fixed public-holiday calendar. A registration date that lands on one of these dates is rolled
     * forward to the next non-holiday business day, in the same way weekends are.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final AddressNormalizer addressNormalizer;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 AddressNormalizer addressNormalizer) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.addressNormalizer = addressNormalizer;
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
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }

        ResponseEntity<OwnerDto> response = createOwner(ownerFieldsDto);

        if (idempotencyKey != null && response.getStatusCode() == HttpStatus.CREATED
            && response.getBody() != null && response.getBody().getId() != null) {
            idempotentCreates.put(idempotencyKey, response.getBody().getId());
        }
        return response;
    }

    /**
     * The non-blank {@link #IDEMPOTENCY_KEY_HEADER Idempotency-Key} header of the request currently
     * being handled, or {@code null} when the request carries no such header (or no request is bound
     * to the current thread). Used to make a repeated create return the originally created owner.
     */
    private String currentIdempotencyKey() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        String key = attributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        return (key == null || key.isBlank()) ? null : key;
    }

    /**
     * Run the full owner-creation pipeline for a submitted {@link OwnerFieldsDto} and build its
     * response. The submitted fields are mapped to an {@link Owner}, which is
     * {@link #normalizeAndValidate(Owner) normalized and validated}, checked against the
     * {@link #isDailyLimitReached(LocalDate) daily creation limit}, {@link #assignHousehold(Owner)
     * assigned its household} and screened for {@link #checkForConflicts(Owner, boolean) conflicts};
     * a surviving owner then has its {@link #assignDerivedAttributes(Owner, boolean) derived
     * attributes} assigned, is persisted and audited. Returns the {@link #created(Owner) 201 Created}
     * response for the persisted owner, or the {@link HttpStatus} the create must fail with
     * ({@code 400 Bad Request}, {@code 429 Too Many Requests} or {@code 409 Conflict}) when the owner
     * cannot be created.
     */
    private ResponseEntity<OwnerDto> createOwner(OwnerFieldsDto ownerFieldsDto) {
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);

        HttpStatus invalid = normalizeAndValidate(owner);
        if (invalid != null) {
            return new ResponseEntity<>(invalid);
        }
        if (isDailyLimitReached(owner.getRegistrationDate())) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }

        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        assignHousehold(owner);

        HttpStatus conflict = checkForConflicts(owner, sharesHousehold);
        if (conflict != null) {
            return new ResponseEntity<>(conflict);
        }

        assignDerivedAttributes(owner, sharesHousehold);
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            owner.getMembershipLevel(), owner.getMembershipNumber());
        return created(owner);
    }

    /**
     * Bring a freshly mapped owner into its canonical, valid form, or report the reason it must be
     * rejected. The owner's {@link #normalizeAddress(Owner) address is normalized} (a missing address
     * is rejected), its telephone is converted to E.164 (an unconvertible number is rejected), its
     * postcode is checked against its city's region, a registration date in the future is rejected,
     * and the effective registration date (the supplied one, or today when none was given) is rolled
     * onto a business day and stored back on the owner. Returns {@code null} once the owner is valid
     * and normalized, or the {@link HttpStatus} the create must fail with ({@code 400 Bad Request})
     * otherwise.
     */
    private HttpStatus normalizeAndValidate(Owner owner) {
        if (!normalizeAddress(owner)) {
            return HttpStatus.BAD_REQUEST;
        }
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return HttpStatus.BAD_REQUEST;
        }
        owner.setTelephone(telephone);
        if (isDisposableEmail(owner.getEmail())) {
            return HttpStatus.BAD_REQUEST;
        }
        if (!owner.isPostcodeValid()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (owner.getRegistrationDate() != null
            && owner.getRegistrationDate().isAfter(LocalDate.now())) {
            return HttpStatus.BAD_REQUEST;
        }
        LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(toBusinessDay(effectiveDate));
        return null;
    }

    /**
     * Normalize the owner's supplied address into its canonical stored form and report whether an
     * address was supplied at all. The owner may supply its address either as the structured
     * {@code addressLine1}/{@code addressLine2} fields or as the flat {@code address} field, which is
     * kept for backward compatibility; the structured form is preferred whenever a non-blank
     * {@code addressLine1} is present. Each supplied field is run through the {@link AddressNormalizer}
     * (trimmed, whitespace-collapsed, upper-cased, common street-type abbreviations expanded). When the
     * structured form is used, the normalized {@code addressLine1} and {@code addressLine2} are stored
     * back and the composed {@link Owner#getAddress() address} becomes the normalized
     * {@code addressLine1} with a single space and the normalized {@code addressLine2} appended when an
     * {@code addressLine2} is present; when the flat form is used, the normalized flat address is
     * stored and no structured lines are kept. Returns {@code true} once a non-blank address has been
     * normalized and stored, or {@code false} when neither form supplied an address, in which case the
     * create must be rejected with {@code 400 Bad Request}.
     */
    private boolean normalizeAddress(Owner owner) {
        String line1 = addressNormalizer.normalize(owner.getAddressLine1());
        if (!line1.isEmpty()) {
            String line2 = addressNormalizer.normalize(owner.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2.isEmpty() ? null : line2);
            owner.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
            return true;
        }
        String address = addressNormalizer.normalize(owner.getAddress());
        if (address.isEmpty()) {
            return false;
        }
        owner.setAddressLine1(null);
        owner.setAddressLine2(null);
        owner.setAddress(address);
        return true;
    }

    /**
     * The set of disposable (throwaway) email domains that an owner's email may not use. An owner
     * whose email is hosted on one of these domains is rejected with {@code 400 Bad Request}.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Whether the given email is hosted on a {@linkplain #DISPOSABLE_EMAIL_DOMAINS disposable}
     * domain. The email is optional, so a {@code null} or empty email is not disposable; otherwise
     * the domain (the part after the last {@code '@'}, compared case-insensitively) is matched
     * against the blocklist.
     */
    private boolean isDisposableEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
    }

    /**
     * Assign the owner its household link. The {@link #householdId(Owner) householdId} is computed
     * deterministically from the owner's lastName and postcode, so every owner sharing that
     * lastName and postcode is assigned the same value automatically, independent of whether the
     * owner declares it shares a household. The link is assigned before the
     * {@link #checkForConflicts(Owner, boolean) conflict checks} because it is part of the owner's
     * {@link Owner#getIdentityKey() identity} and feeds its {@link #householdSize(String) household
     * size}.
     */
    private void assignHousehold(Owner owner) {
        owner.setHouseholdId(householdId(owner));
    }

    /**
     * Whether the owner clashes with an owner that already exists, and the {@link HttpStatus} the
     * create must fail with when it does. An owner is rejected with {@code 409 Conflict} when it
     * {@link #isHouseholdDuplicate(Owner) duplicates an existing household} or when its
     * {@link #isCityAtCapacity(String) city is already at capacity}. Declaring {@code
     * sharesHousehold} bypasses only the household-duplicate block (the owner is then created as a
     * declared household member); it does not lift the city-capacity limit. Returns {@code null}
     * when the owner clashes with nothing and may be created.
     */
    private HttpStatus checkForConflicts(Owner owner, boolean sharesHousehold) {
        if (!sharesHousehold && isHouseholdDuplicate(owner)) {
            return HttpStatus.CONFLICT;
        }
        if (isCityAtCapacity(owner.getCity())) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    /**
     * Build the {@code 201 Created} response for a persisted owner: its {@link OwnerDto} body and a
     * {@code Location} header pointing at {@code /api/owners/{id}}.
     */
    private ResponseEntity<OwnerDto> created(Owner owner) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), headers, HttpStatus.CREATED);
    }

    /**
     * Assign the attributes that are derived once, at creation time, and stored on the owner: its
     * {@link #customerCode(Owner) customerCode}, {@link #namesakeCount(String, String) namesakeCount},
     * {@link #isBulkSignup(LocalDate) bulk-signup warning}, {@link #householdSize(String)
     * householdSize} and {@link #membershipLevel(Owner) membershipLevel}. Each is computed from the
     * owner's own fields together with the owners already present, so this runs after the owner has
     * been validated and has passed the duplicate and capacity checks, and immediately before it is
     * persisted.
     */
    private void assignDerivedAttributes(Owner owner, boolean sharesHousehold) {
        owner.setCustomerCode(customerCode(owner));
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(isBulkSignup(owner.getRegistrationDate()));
        owner.setHouseholdSize(householdSize(owner.getHouseholdId()));
        owner.setMembershipLevel(membershipLevel(owner));
        assignPossibleDuplicate(owner, sharesHousehold);
    }

    /**
     * The membership level to assign a newly created owner and store on it as its
     * {@link Owner#getMembershipLevel() membershipLevel}: the owner's own
     * {@linkplain Owner#getBaseMembershipLevel() base level} derived from its membershipPoints. This
     * is the single place the stored membership level is derived; it runs during
     * {@link #assignDerivedAttributes(Owner, boolean)}, after the owner has passed the duplicate and
     * capacity checks and so has its {@linkplain #householdMembers(String) household members}
     * available to it.
     */
    private Integer membershipLevel(Owner owner) {
        return owner.getBaseMembershipLevel();
    }

    /**
     * Flag a newly created owner as a possible (soft) duplicate. An owner that declares it shares a
     * household ({@code sharesHousehold}) is a declared household member, not a suspected duplicate,
     * so it is never flagged. Otherwise, because this runs after the hard
     * {@link #isHouseholdDuplicate(Owner) household-duplicate} check, the owner is not a household
     * match; it is flagged only when it is a soft match of an existing owner as determined by
     * {@link #findSoftDuplicate(Owner)}. When such an owner exists the new owner is flagged with
     * {@code possibleDuplicate = true} and {@code possibleDuplicateOf} set to that owner's id;
     * otherwise {@code possibleDuplicate = false} and no matching id is recorded.
     */
    private void assignPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        Owner match = sharesHousehold ? null : findSoftDuplicate(owner);
        owner.setPossibleDuplicate(match != null);
        owner.setPossibleDuplicateOf(match == null ? null : match.getId());
    }

    /**
     * The owners a newly created owner is checked against by the duplicate and identity rules:
     * the {@link #isHouseholdDuplicate(Owner) household-duplicate} and {@link #findSoftDuplicate(Owner)
     * soft-duplicate} checks, the {@link #customerCode(Owner) customerCode} de-duplication and the
     * {@link #namesakeCount(String, String) namesake} count. This is the single place that scope is
     * defined, so every one of those rules considers exactly the same set of owners; it spans every
     * owner the clinic holds except those flagged {@linkplain Owner#isDeleted() deleted}, which a
     * soft delete retains but excludes from duplicate and identity detection.
     */
    private Collection<Owner> activeOwners() {
        return this.clinicService.findAllOwners().stream()
            .filter(owner -> !owner.isDeleted())
            .collect(Collectors.toList());
    }

    /**
     * The existing owner this owner softly duplicates, or {@code null} when there is none. A soft
     * match is an existing owner that shares this owner's lastName (compared case-insensitively,
     * with collapsed whitespace) and its postcode but carries a different telephone; the earliest
     * such owner (by id) is returned. A postcode is required for a soft match, so an owner without a
     * postcode never matches.
     */
    private Owner findSoftDuplicate(Owner owner) {
        if (owner.getPostcode() == null) {
            return null;
        }
        String candidateLastName = normalizeName(owner.getLastName());
        return activeOwners().stream()
            .filter(existing -> owner.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> normalizeName(existing.getLastName()).equals(candidateLastName))
            .filter(existing -> !owner.getTelephone().equals(existing.getTelephone()))
            .min(Comparator.comparingInt(Owner::getId))
            .orElse(null);
    }

    /**
     * Build the customer code for a newly created owner, formatted {@code '<REGION>-<HASH8>'}
     * where REGION is the region derived from the owner's postcode (or {@link Owner#UNKNOWN_REGION}
     * when the postcode is absent or maps to no known region) and HASH8 is the first 8 upper-case
     * hex characters of the SHA-256 of the normalized (E.164) telephone concatenated with the
     * lastName (e.g. {@code 'NSW-3C1A9F2B'}). This is the single source of the owner's identity:
     * every value built from the customer code (the membership number and its check digit) and the
     * owner's locality are derived from it.
     *
     * <p>When the computed code collides with an existing owner's {@code customerCode}, a
     * {@code '-<n>'} suffix is appended with the smallest {@code n} of 2 or more that makes the code
     * unique, and the de-duplicated code is returned.
     */
    private String customerCode(Owner owner) {
        String region = owner.getRegion() != null ? owner.getRegion() : Owner.UNKNOWN_REGION;
        String hash8 = shaHexUpper(owner.getTelephone() + owner.getLastName()).substring(0, 8);
        String base = region + "-" + hash8;

        Set<String> existingCodes = activeOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (!existingCodes.contains(base)) {
            return base;
        }
        int n = 2;
        while (existingCodes.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /**
     * Whether the per-day owner creation limit has been reached for the given business day, i.e.
     * 100 or more owners already carry that {@code registrationDate}. The day is the adjusted
     * (business-day) registration date of the owner being created, so owners are counted per
     * business day. When true, a new owner must be rejected with {@code 429 Too Many Requests}.
     */
    private boolean isDailyLimitReached(LocalDate day) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
        return count >= 100;
    }

    /**
     * Whether creating this owner pushes the day's total past the bulk-signup threshold, i.e.
     * more than 80 owners have already been created for the given business day. Counts the owners
     * already carrying that {@code registrationDate} (the same accumulation as the daily-limit
     * rule); when that count exceeds 80 the newly created owner is flagged with a bulk-signup
     * warning.
     */
    private boolean isBulkSignup(LocalDate day) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
        return count > 80;
    }

    /**
     * Roll a registration date forward onto a business day: when it falls on a Saturday, Sunday or a
     * listed public holiday, advance it to the next non-holiday weekday; an ordinary weekday is
     * returned unchanged.
     */
    private LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Whether the given city has reached its owner capacity, i.e. it already contains 50 or
     * more owners. Cities are compared case-insensitively.
     */
    private boolean isCityAtCapacity(String city) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
        return count >= 50;
    }

    /**
     * The number of existing owners that already share the given firstName and lastName,
     * compared case-insensitively (and with surrounding whitespace trimmed and internal
     * whitespace runs collapsed). Counted over the owners present before this create.
     */
    private int namesakeCount(String firstName, String lastName) {
        String candidateFirstName = normalizeName(firstName);
        String candidateLastName = normalizeName(lastName);
        return (int) activeOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(candidateFirstName)
                && normalizeName(existing.getLastName()).equals(candidateLastName))
            .count();
    }

    /**
     * The number of members in this owner's household after this create: the count of existing
     * owners already carrying the same non-null {@code householdId} plus one for the owner being
     * created. An owner with no household ({@code householdId == null}) is a household of one.
     * Owners sharing a household are matched by their assigned {@code householdId}, so the count
     * does not depend on the order in which household members were created.
     */
    private int householdSize(String householdId) {
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(other -> householdId.equals(other.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * Convert a raw telephone input to its E.164 representation, or {@code null} if it
     * cannot form a valid E.164 number.
     *
     * <p>A leading '+' and country code are kept when present; otherwise country code
     * '+61' is assumed and a single leading '0' is dropped from the national digits.
     * Spaces, dashes and brackets (indeed any non-digit) are stripped. The result must
     * carry 8 to 15 digits after the '+', and — for a recognised country calling code —
     * exactly the national-digit count that code requires (see
     * {@link Owner#hasValidNationalLength}); otherwise it is rejected as invalid.
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        String e164;
        if (hasCountryCode) {
            e164 = digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            e164 = "61" + digits;
        }
        if (e164.length() < 8 || e164.length() > 15) {
            return null;
        }
        if (!Owner.hasValidNationalLength(e164)) {
            return null;
        }
        return "+" + e164;
    }

    /**
     * Whether the candidate owner belongs to a household that already exists, and so must be
     * rejected with {@code 409 Conflict} unless it declares {@code sharesHousehold}. The household
     * is keyed by the deterministic {@link #householdId(Owner) householdId} (derived from lastName
     * and postcode), so any existing owner carrying the same householdId means this owner is a
     * second member of an already-registered household. Two owners sharing a lastName and postcode
     * are therefore the same household: the second is a household duplicate.
     */
    private boolean isHouseholdDuplicate(Owner owner) {
        return !householdMembers(owner.getHouseholdId()).isEmpty();
    }

    /**
     * The existing owners already registered in the household identified by {@code householdId}: the
     * {@linkplain #activeOwners() active owners} carrying that same non-null householdId. The owner
     * being created is not yet persisted, so it is never among them, and a {@code null} householdId
     * (an owner with no household) yields an empty collection. This is the single source of a
     * household's existing membership, shared by every rule keyed by an owner's household (the
     * {@link #isHouseholdDuplicate(Owner) household-duplicate} check and the
     * {@link #membershipLevel(Owner) membership-level} derivation) so the set is never re-derived
     * elsewhere; like the other identity rules it ignores {@linkplain Owner#isDeleted() deleted}
     * owners.
     */
    private Collection<Owner> householdMembers(String householdId) {
        if (householdId == null) {
            return List.of();
        }
        return activeOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .collect(Collectors.toList());
    }

    /**
     * Normalize an owner's name for household comparison: trim, collapse internal whitespace runs
     * to a single space and lower-case. A null value normalizes to the empty string.
     */
    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * A stable shared identifier for the household an owner belongs to, derived deterministically
     * from its lastName and postcode. The lastName is normalized (case-insensitive,
     * whitespace-collapsed) before hashing, so every owner sharing the same lastName and postcode is
     * assigned the same value regardless of the order in which they are created. Formatted as the
     * first 12 upper-case hex characters of the SHA-256 of {@code '<normalizedLastName>|<postcode>'}
     * (an absent postcode contributes the empty string).
     */
    private String householdId(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = normalizeName(owner.getLastName()) + "|" + postcode;
        return shaHexUpper(key).substring(0, 12);
    }

    /**
     * The SHA-256 digest of {@code input}'s UTF-8 bytes, rendered as an upper-case hexadecimal
     * string (two hex characters per digest byte, so 64 characters in all). This is the single
     * place the hashing-and-hex-encoding is performed; callers that need a shorter opaque token
     * take a prefix of the result (e.g. {@link #householdId} keeps the first 12 characters and
     * {@link #customerCode} keeps the first 8).
     */
    private String shaHexUpper(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
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
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
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
