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

import java.util.Collection;
import java.util.List;

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
import org.springframework.samples.petclinic.rest.advice.InvalidAddressException;
import org.springframework.samples.petclinic.rest.advice.DisposableEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidEmailException;
import org.springframework.samples.petclinic.rest.advice.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.Households;
import org.springframework.samples.petclinic.util.Localities;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
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

    /**
     * Dedicated audit logger. On a successful owner create an audit line is emitted here carrying the
     * new owner's id, its assigned {@code memberId}, its {@code registrationDate} and its
     * numeric {@code membershipLevel}.
     */
    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("AUDIT");

    /**
     * Monotonically increasing sequence assigned to each structured {@code OWNER_CREATED} audit event,
     * so the immutable event stream carries a strictly increasing {@code seq} across creates.
     */
    private static final java.util.concurrent.atomic.AtomicLong AUDIT_EVENT_SEQ =
        new java.util.concurrent.atomic.AtomicLong();

    /**
     * Name of the optional request header carrying an idempotency token for owner creation.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Idempotency store mapping a previously seen {@code Idempotency-Key} to the id of the owner
     * originally created under it. When a create repeats with a key already present here, the
     * originally created owner is returned (200 OK) instead of creating a duplicate.
     */
    private final java.util.Map<String, Integer> idempotencyKeyToOwnerId =
        new java.util.concurrent.ConcurrentHashMap<>();

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
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotencyKeyToOwnerId.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        applyAddress(owner, ownerFieldsDto);
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        rejectFutureRegistrationDate(registrationDate);
        if (registrationDate == null) {
            registrationDate = java.time.LocalDate.now();
        }
        registrationDate = toBusinessDay(registrationDate);
        owner.setRegistrationDate(registrationDate);
        rejectDailyLimitReached(registrationDate);
        rejectCityAtCapacity(owner.getCity());
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        owner.setTelephone(telephone);
        String email = normalizeEmail(ownerFieldsDto.getEmail());
        owner.setEmail(email);
        owner.setPostcode(validatePostcode(owner.getCity(), ownerFieldsDto.getPostcode()));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        rejectDuplicateIdentity(owner);
        // Joining an existing household is now permitted (an exact-identity duplicate is still
        // rejected above); a joiner's membershipLevel is instead capped one above the household's
        // current maximum (see cappedMembershipLevel).
        String region = Localities.region(owner.getPostcode(), owner.getCity());
        owner.setMemberId(assignMemberId(region, owner.getTelephone(), owner.getLastName(), registrationDate));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(computeBulkSignupWarning(registrationDate));
        owner.setCapacityWarning(computeCapacityWarning(owner.getCity()));
        owner.setHouseholdSize(countHouseholdMembers(owner.getLastName(), owner.getPostcode()) + 1);
        owner.setMembershipLevel(cappedMembershipLevel(owner));
        // A declared household member (sharesHousehold) is created but is not a suspected duplicate.
        Integer possibleDuplicateOf = sharesHousehold ? null : findPossibleDuplicateOf(owner);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotencyKeyToOwnerId.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel());
        AUDIT.info(ownerCreatedEvent(owner, ownerDto));
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Builds the immutable structured {@code OWNER_CREATED} audit event emitted (in addition to the
     * human-readable audit line) on a successful create, as a JSON object
     * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}. The {@code seq} is
     * a fresh value from the monotonically increasing {@link #AUDIT_EVENT_SEQ} sequence, so the event
     * stream is strictly ordered across creates.
     * <p>
     * The {@code memberId} field carries the owner's current primary identifier (see
     * {@link #primaryIdentifier}), so the event always names the owner by its live primary identifier.
     *
     * @param owner the just-saved owner
     * @param ownerDto the mapped DTO, used for the (possibly derived) {@code membershipLevel}
     * @return the JSON-encoded structured event
     */
    private String ownerCreatedEvent(Owner owner, OwnerDto ownerDto) {
        StringBuilder json = new StringBuilder(128);
        json.append('{');
        json.append("\"seq\":").append(AUDIT_EVENT_SEQ.incrementAndGet());
        json.append(",\"ownerId\":").append(owner.getId());
        json.append(",\"memberId\":").append(jsonString(primaryIdentifier(owner)));
        json.append(",\"membershipLevel\":").append(ownerDto.getMembershipLevel());
        json.append(",\"event\":\"OWNER_CREATED\"");
        json.append('}');
        return json.toString();
    }

    /**
     * The owner's current primary identifier carried by the structured audit event: the assigned
     * {@code memberId}.
     *
     * @param owner the just-saved owner
     * @return the owner's live primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Encodes a value as a JSON string literal (quoted, with the JSON-significant characters escaped),
     * or the literal {@code null} when the value is absent.
     */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
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
        sb.append('"');
        return sb.toString();
    }

    /**
     * Reads the optional {@code Idempotency-Key} header from the current request, if any. A create
     * carrying this header is idempotent: the first create under a key is recorded, and a later create
     * with the same key returns the originally created owner instead of creating a duplicate.
     *
     * @return the trimmed idempotency key, or {@code null} when the header is absent, blank or there is
     *         no active servlet request
     */
    private String currentIdempotencyKey() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            String key = servletAttributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
            if (key != null && !key.isBlank()) {
                return key.trim();
            }
        }
        return null;
    }

    /**
     * Rejects creating an owner whose supplied {@code registrationDate} is later than the server
     * date (in the future). Only an explicitly supplied date is validated; a {@code null} date
     * (which later defaults to the server date) is accepted. The check is against the raw supplied
     * date, before any business-day adjustment.
     *
     * @param registrationDate the supplied registration date, or {@code null} when none was supplied
     * @throws FutureRegistrationDateException (400 Bad Request) if the supplied date is in the future
     */
    private void rejectFutureRegistrationDate(java.time.LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(java.time.LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }

    /**
     * The maximum number of owners that may be created in a single day. Creating an owner once this
     * many owners already carry today's {@code registrationDate} is rejected.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Rejects creating an owner once {@value #DAILY_OWNER_LIMIT} or more owners already carry the
     * given business day as their {@code registrationDate}. The count is keyed by the
     * business-day-adjusted registration date of the owner being created (see
     * {@link #toBusinessDay}), so all owners falling on the same business day count together. Owners
     * with no {@code registrationDate} are ignored.
     *
     * @param registrationDate the business-day-adjusted registration date of the owner being created
     * @throws DailyOwnerLimitException (429 Too Many Requests) if the daily limit is already reached
     */
    private void rejectDailyLimitReached(java.time.LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitException(registrationDate);
        }
    }

    /**
     * The threshold of owners already created on a business day beyond which a bulk-signup warning is
     * raised on a new owner. Once strictly more than this many owners already carry the day as their
     * {@code registrationDate}, the new owner's {@code bulkSignupWarning} is set to {@code true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Computes the {@code bulkSignupWarning} for an owner being created: {@code true} when more than
     * {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been created today, otherwise
     * {@code false}. Existing owners are counted the same way the daily-limit rule counts them (see
     * {@link #rejectDailyLimitReached}), keyed by the business-day-adjusted registration date, and the
     * owner being created is excluded (it has not yet been saved).
     *
     * @param registrationDate the business-day-adjusted registration date of the owner being created
     * @return {@code true} if more than the threshold of owners already carry this business day
     */
    private boolean computeBulkSignupWarning(java.time.LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * The fixed public holidays the business-day roll skips. A registration date landing on any of
     * these dates is rolled forward, just as it is for a weekend.
     */
    private static final java.util.Set<java.time.LocalDate> PUBLIC_HOLIDAYS = java.util.Set.of(
        java.time.LocalDate.parse("2026-01-01"),
        java.time.LocalDate.parse("2026-01-26"),
        java.time.LocalDate.parse("2026-04-25"),
        java.time.LocalDate.parse("2026-12-25"),
        java.time.LocalDate.parse("2026-12-28"));

    /**
     * Rolls a registration date forward onto a business day: a Saturday, Sunday or listed public
     * holiday is moved forward to the next non-holiday weekday; an ordinary weekday is returned
     * unchanged. This applies to the effective registration date whether it was supplied in the
     * request or defaulted to the server date, and every value derived from the registration date
     * uses the adjusted result.
     *
     * @param date the effective registration date
     * @return {@code date} itself when it is a non-holiday weekday, otherwise the next business day
     */
    private java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        while (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
            || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * The maximum number of owners a single city may contain. Creating an owner in a city that
     * already holds this many owners is rejected.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Rejects creating an owner whose city already contains {@value #CITY_CAPACITY} or more owners.
     * Owners are counted per city comparing the city name case-insensitively (a {@code null} city is
     * treated as empty).
     *
     * @param city the city of the owner being created
     * @throws CityAtCapacityException (409 Conflict) if the city is already at capacity
     */
    private void rejectCityAtCapacity(String city) {
        String cityValue = city == null ? "" : city;
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> cityValue.equalsIgnoreCase(
                existing.getCity() == null ? "" : existing.getCity()))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * The number of owners a city must already hold for a new owner in it to raise a
     * {@code capacityWarning}. Once a city holds at least this many owners (but fewer than the hard
     * {@link #CITY_CAPACITY} limit) the next owner created in it warns that the city is approaching
     * capacity.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Computes the {@code capacityWarning} for an owner being created: {@code true} when the owner's
     * city already holds between {@value #CITY_CAPACITY_WARNING_THRESHOLD} and
     * {@value #CITY_CAPACITY} minus one owners (approaching, but not yet at, the hard
     * {@link #CITY_CAPACITY} limit), otherwise {@code false}. Owners are counted per city the same way
     * the hard capacity rule counts them (see {@link #rejectCityAtCapacity}), comparing the city name
     * case-insensitively with a {@code null} city treated as empty, and the owner being created is
     * excluded (it has not yet been saved). A city already at or beyond the hard limit is rejected
     * before this runs, so it never reports a warning.
     *
     * @param city the city of the owner being created
     * @return {@code true} if the city is approaching its capacity limit
     */
    private boolean computeCapacityWarning(String city) {
        String cityValue = city == null ? "" : city;
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> cityValue.equalsIgnoreCase(
                existing.getCity() == null ? "" : existing.getCity()))
            .count();
        return count >= CITY_CAPACITY_WARNING_THRESHOLD && count < CITY_CAPACITY;
    }

    /**
     * Assigns an owner's {@code memberId} on create, formatted {@code '<REGION><FY><HASH8><CHK>'}: the
     * owner's canonical region REGION (derived by preferring the postcode, see
     * {@link Localities#region(String, String)} — the same region derivation shared with the
     * {@code locality}); the 2-digit fiscal year FY of the registration date (see
     * {@link org.springframework.samples.petclinic.util.FiscalYears#fiscalYear(java.time.LocalDate)});
     * the first 8 upper-case hex characters HASH8 of the SHA-256 digest of the normalized (E.164)
     * telephone concatenated with the last name (the same HASH8 used by the region-and-hash identity);
     * and a single Luhn check digit CHK computed over the decimal digits contained in
     * {@code <REGION><FY><HASH8>} (e.g. {@code 'NSW263A7F9C2E5'}). The id is derived from the owner's
     * own identity fields; it carries no sequence number.
     * <p>
     * When the computed id collides with an existing owner's {@code memberId}, it is de-duplicated by
     * appending {@code '-<n>'} with the smallest {@code n} of 2 or more that makes it unique (e.g.
     * {@code 'NSW263A7F9C2E5-2'}), and the de-duplicated id is returned.
     *
     * @param region the owner's canonical region (the identity's REGION, shared with the locality)
     * @param telephone the owner's normalized (E.164) telephone
     * @param lastName the owner's last name
     * @param registrationDate the owner's business-day-adjusted registration date (supplies FY)
     * @return the assigned member id, de-duplicated to be unique among existing owners
     */
    private String assignMemberId(String region, String telephone, String lastName,
            java.time.LocalDate registrationDate) {
        String basis = (telephone == null ? "" : telephone) + (lastName == null ? "" : lastName);
        String hash8 = sha256UpperHex(basis).substring(0, 8);
        String fy = String.format("%02d",
            org.springframework.samples.petclinic.util.FiscalYears.fiscalYear(registrationDate) % 100);
        String base = region + fy + hash8;
        String memberId = base
            + org.springframework.samples.petclinic.util.CheckDigits.luhn(base);
        java.util.Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        if (!existing.contains(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * Returns the full upper-case hex SHA-256 digest of the UTF-8 bytes of {@code value}.
     */
    private static String sha256UpperHex(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Counts how many existing owners share the given first and last name, compared
     * case-insensitively, at the time this owner is created. The value excludes the owner
     * being created (which has not yet been saved) and is stored on the new owner and returned
     * as its {@code namesakeCount}.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners with the same first and last name
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
     * Counts how many existing owners belong to the same household as the owner being created, i.e.
     * share its {@code householdId} (derived by {@link Households#householdId} from the normalized last
     * name and postcode). The value excludes the owner being created (which has not yet been saved), so
     * the household's size after this create is this count plus one. It is stored on the new owner.
     *
     * @param lastName the last name of the owner being created
     * @param postcode the postcode of the owner being created
     * @return the number of existing owners sharing this owner's household
     */
    private int countHouseholdMembers(String lastName, String postcode) {
        String householdId = Households.householdId(lastName, postcode);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(
                Households.householdId(existing.getLastName(), existing.getPostcode())))
            .count();
    }

    /**
     * Computes the {@code membershipLevel} to store for the owner being created, applying the household
     * ceiling. The owner's natural level is first derived from their own fields (see
     * {@link org.springframework.samples.petclinic.util.Memberships#membershipLevel(String, Integer, Integer, java.time.LocalDate)}).
     * It is then capped so it cannot exceed one above the current maximum {@code membershipLevel} among
     * the owner's existing household members (the non-deleted owners sharing its {@code householdId}, see
     * {@link Households#householdId}). When the owner has no existing household member no cap applies and
     * the natural level is returned.
     *
     * @param owner the owner being created, with its level-affecting fields already populated
     * @return the (possibly capped) membership level to store on the owner
     */
    private int cappedMembershipLevel(Owner owner) {
        int naturalLevel = org.springframework.samples.petclinic.util.Memberships.membershipLevel(
            owner.getEmail(), owner.getNamesakeCount(), owner.getHouseholdSize(), owner.getRegistrationDate());
        String householdId = Households.householdId(owner.getLastName(), owner.getPostcode());
        java.util.OptionalInt maxMemberLevel = this.clinicService.findAllOwners().stream()
            .filter(this::isNotDeleted)
            .filter(existing -> householdId.equals(
                Households.householdId(existing.getLastName(), existing.getPostcode())))
            .mapToInt(this::effectiveMembershipLevel)
            .max();
        if (maxMemberLevel.isEmpty()) {
            return naturalLevel;
        }
        return Math.min(naturalLevel, maxMemberLevel.getAsInt() + 1);
    }

    /**
     * Returns an existing owner's effective {@code membershipLevel}: the stored value when present (the
     * value assigned, and possibly capped, when that owner was created), otherwise the level derived
     * from its own fields for owners that predate the stored level (e.g. seed data).
     *
     * @param owner an existing owner
     * @return the owner's effective membership level
     */
    private int effectiveMembershipLevel(Owner owner) {
        if (owner.getMembershipLevel() != null) {
            return owner.getMembershipLevel();
        }
        return org.springframework.samples.petclinic.util.Memberships.membershipLevel(
            owner.getEmail(), owner.getNamesakeCount(), owner.getHouseholdSize(), owner.getRegistrationDate());
    }

    /**
     * Finds the existing owner, if any, that the owner being created is a soft duplicate of: an owner
     * sharing this owner's {@code soundex(lastName)} and postcode (an exact match of the stored 4-digit
     * value) while carrying a <em>different</em> {@code identityKey}. A soft match is distinct from the
     * hard duplicate rejected earlier by {@link #rejectDuplicateIdentity} (which requires an identical
     * identity key): the owner is still created, but flagged. Because the telephone is part of the
     * identity key, two owners with the same last name and postcode but different telephones are a soft
     * match rather than a hard duplicate. When more than one existing owner matches, the earliest
     * (lowest id) is returned so the reference is deterministic. An owner with no postcode can never
     * soft-match.
     *
     * @param owner the owner being created, with its telephone and postcode already normalized/validated
     * @return the id of the matching existing owner, or {@code null} when there is no soft match
     */
    private Integer findPossibleDuplicateOf(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return null;
        }
        String soundex = Households.soundex(owner.getLastName());
        String identity = Households.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        return this.clinicService.findAllOwners().stream()
            .filter(this::isNotDeleted)
            .filter(existing -> soundex.equals(Households.soundex(existing.getLastName())))
            .filter(existing -> postcode.equals(existing.getPostcode()))
            .filter(existing -> !identity.equals(Households.identityKey(
                toE164OrNull(existing.getTelephone()), existing.getEmail(), existing.getLastName())))
            .map(Owner::getId)
            .filter(java.util.Objects::nonNull)
            .min(java.util.Comparator.naturalOrder())
            .orElse(null);
    }

    /**
     * Normalizes an owner's telephone on create into E.164 form. Spaces, dashes and brackets are
     * stripped. A leading '+' and its country code are kept as given; otherwise country code '+61' is
     * assumed and a single leading '0' is dropped from the national digits. The resulting number must
     * have 8 to 15 digits after the '+'. For example {@code "0412 345 678"} becomes
     * {@code "+61412345678"}. The E.164 value is stored and returned.
     * <p>
     * The national-number length (the digits after the country code) is additionally validated for
     * country codes with a fixed length: '+61' requires exactly 9 national digits and '+1' requires
     * exactly 10. Numbers of other country codes are only bound by the overall 8-to-15-digit limit.
     *
     * @param telephone the raw telephone as submitted
     * @return the normalized E.164 telephone (a '+' followed by 8 to 15 digits)
     * @throws InvalidTelephoneException (400 Bad Request) if the value cannot form a valid E.164 number
     *         or its national-number length is wrong for its country code
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.trim();
        boolean hasCountryCode = raw.startsWith("+");
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        if (hasCountryCode) {
            cleaned = cleaned.substring(1);
        }
        String digits;
        if (hasCountryCode) {
            digits = cleaned;
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}") || !hasValidNationalLength(digits)) {
            throw new InvalidTelephoneException(telephone);
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length of an E.164 digit string against its country code for the
     * country codes whose national number has a fixed length: '+61' requires 9 national digits and
     * '+1' requires 10. Any other country code passes, leaving it bound only by the overall length.
     *
     * @param digits the E.164 digits (the country code followed by the national number, without '+')
     * @return {@code true} if the national-number length is acceptable for the country code
     */
    private boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }

    /**
     * Syntactic email pattern: a non-empty local part, an '@', a domain with at least one dot and a
     * multi-character top-level label. Deliberately lenient about the exact character set while still
     * rejecting values that are clearly not addresses (e.g. missing '@').
     */
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    /**
     * Domains of known disposable/throwaway email providers. An owner whose email domain is on this
     * blocklist is rejected with a 400 Bad Request. Compared case-insensitively against the
     * lower-cased email domain.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS = java.util.Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Normalizes an owner's email. Email is optional: a {@code null} or blank value is left as
     * {@code null} (no email). When present it must be a syntactically valid address; the value is
     * lower-cased before being stored and returned.
     *
     * @param email the raw email as submitted, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidEmailException (400 Bad Request) if a non-blank value is not a valid address
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        String normalized = trimmed.toLowerCase(java.util.Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new DisposableEmailException(email);
        }
        return normalized;
    }

    /**
     * Fixed region-to-postcode-range table: a region's postcode must fall within its inclusive range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city whose region is not listed here (see
     * {@link Localities#region}) accepts any 4-digit postcode.
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODE_RANGES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's postcode on create. The postcode is optional: a {@code null} or blank value
     * is left as {@code null} (no postcode) and accepted. When present it must be exactly 4 digits and,
     * for a city whose region is in the fixed {@link #REGION_POSTCODE_RANGES range table}, must fall
     * within that region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no
     * known region accepts any 4-digit postcode. The value is stored and returned exactly as supplied.
     *
     * @param city the owner's city, used to derive the region whose range constrains the postcode
     * @param postcode the raw postcode as submitted, may be {@code null}
     * @return the postcode when supplied, or {@code null} when none was supplied
     * @throws InvalidPostcodeException (400 Bad Request) if a supplied postcode is not 4 digits or is
     *         out of range for the city's region
     */
    private String validatePostcode(String city, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_POSTCODE_RANGES.get(Localities.region(city));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
        return postcode;
    }

    /**
     * Common street-type abbreviations expanded to their full word during address normalization.
     */
    private static final java.util.Map<String, String> ADDRESS_ABBREVIATIONS = java.util.Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Applies the owner's address on create from the structured fields when present, falling back to the
     * flat {@code address} for backward compatibility. An owner must supply an address in at least one
     * form: a non-blank {@code addressLine1} (the structured form, preferred) or a non-blank flat
     * {@code address}; when neither is present the create is rejected.
     * <p>
     * Whichever address fields are supplied are normalized (see {@link #normalizeAddress}). When the
     * structured form is used the normalized {@code addressLine1} and, when supplied, {@code addressLine2}
     * are stored on the owner and the composed {@code address} is the normalized {@code addressLine1} with
     * a single space and the normalized {@code addressLine2} appended when {@code addressLine2} is present.
     * When only the flat form is used the normalized flat value becomes the stored {@code address} and the
     * structured lines are left unset. The stored {@code address} is the value every later step reads.
     *
     * @param owner the owner being created
     * @param ownerFieldsDto the submitted owner fields carrying the raw address input
     * @throws InvalidAddressException (400 Bad Request) if no address is supplied in either form
     */
    private void applyAddress(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        String rawLine1 = ownerFieldsDto.getAddressLine1();
        String rawLine2 = ownerFieldsDto.getAddressLine2();
        String rawFlat = ownerFieldsDto.getAddress();
        boolean hasLine1 = rawLine1 != null && !rawLine1.isBlank();
        boolean hasFlat = rawFlat != null && !rawFlat.isBlank();
        if (!hasLine1 && !hasFlat) {
            throw new InvalidAddressException(rawLine1 != null ? rawLine1 : rawFlat);
        }
        if (hasLine1) {
            String line1 = normalizeAddress(rawLine1);
            String line2 = (rawLine2 != null && !rawLine2.isBlank()) ? normalizeAddress(rawLine2) : null;
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2);
            owner.setAddress(line2 != null ? line1 + " " + line2 : line1);
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(normalizeAddress(rawFlat));
        }
    }

    /**
     * Normalizes an owner's address on create. Surrounding whitespace is trimmed and internal runs of
     * whitespace collapse to a single space, the value is upper-cased and common street-type
     * abbreviations are expanded to their full word ({@code ST} to {@code STREET}, {@code RD} to
     * {@code ROAD}, {@code AVE} to {@code AVENUE}). For example {@code "  12  main  st "} becomes
     * {@code "12 MAIN STREET"}. The normalized value is stored and returned, and is also the form used
     * for every address comparison (household duplicate detection and the shared household id).
     *
     * @param address the raw address as submitted
     * @return the normalized address
     * @throws InvalidAddressException (400 Bad Request) if the address is blank after normalization
     */
    private String normalizeAddress(String address) {
        String collapsed = (address == null ? "" : address).trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            throw new InvalidAddressException(address);
        }
        String[] tokens = collapsed.toUpperCase(java.util.Locale.ROOT).split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }

    /**
     * Rejects creating an owner whose identity duplicates an existing owner's. All duplicate detection
     * is consolidated onto the single derived {@code identityKey} (see
     * {@link Households#identityKey(String, String, String)}): the SHA-256 hex over the normalized
     * telephone, the lower-cased email (a {@code null} email compared as an empty segment) and the
     * {@code soundex} of the last name. Two owners are duplicates when their identity keys are equal.
     * Existing telephones are re-normalized to E.164 before comparison, so equivalent values submitted
     * in different formats (e.g. national {@code "0412 345 678"} and international
     * {@code "+61 412 345 678"}) still match. The email-domain blocklist is applied first (during
     * email normalization), and soft-deleted owners are ignored.
     * <p>
     * Because the telephone is part of the key, two members of one household (same last name and
     * postcode) with different telephones have different identity keys and are both allowed; such a
     * near-match is instead surfaced as a soft duplicate (see {@link #findPossibleDuplicateOf}).
     *
     * @param owner the owner being created, with its telephone and email already normalized
     * @throws DuplicateIdentityException (409 Conflict) if another owner has the same identity key
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String identity = Households.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .filter(this::isNotDeleted)
            .anyMatch(existing -> identity.equals(Households.identityKey(
                toE164OrNull(existing.getTelephone()), existing.getEmail(), existing.getLastName())));
        if (duplicate) {
            throw new DuplicateIdentityException(identity);
        }
    }

    /**
     * Whether an existing owner is not soft-deleted. A soft-deleted owner (its {@code deleted} flag
     * set) is retained but ignored by the create endpoint's duplicate and identity checks, so a
     * normally-blocking duplicate is allowed when the only matching owner has been deleted.
     */
    private boolean isNotDeleted(Owner owner) {
        return !Boolean.TRUE.equals(owner.getDeleted());
    }

    /**
     * Normalizes an existing owner's telephone to E.164 for duplicate comparison, returning
     * {@code null} instead of throwing when the stored value cannot form a valid E.164 number, or
     * when its national-number length does not match its country code.
     */
    private String toE164OrNull(String telephone) {
        try {
            return normalizeTelephone(telephone);
        } catch (InvalidTelephoneException e) {
            return null;
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
