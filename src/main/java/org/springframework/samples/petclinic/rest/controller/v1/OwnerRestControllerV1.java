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
import java.util.Objects;
import java.util.Set;
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
import org.springframework.samples.petclinic.rest.controller.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.controller.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.controller.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.controller.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.controller.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.controller.InvalidEmailException;
import org.springframework.samples.petclinic.rest.controller.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.controller.RequiredFieldsMissingException;
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
     * Dedicated audit logger. On successful owner create an audit line is emitted here carrying
     * the newly assigned owner id, customer code, registration date, membership level and
     * membership number.
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

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        normalizeAddress(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        validatePostcode(ownerFieldsDto);
        normalizeTelephone(ownerFieldsDto);
        normalizeEmail(ownerFieldsDto);
        rejectDuplicateIdentity(ownerFieldsDto);
        rejectDuplicateHousehold(ownerFieldsDto);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        rejectFutureRegistrationDate(ownerFieldsDto);
        defaultRegistrationDate(ownerFieldsDto);
        rejectDailyLimit(ownerFieldsDto.getRegistrationDate());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        assignCustomerCode(owner);
        assignHouseholdId(owner);
        assignHouseholdMemberCount(owner);
        assignNamesakeCount(owner);
        assignMembershipNumber(owner);
        assignBulkSignupWarning(owner);
        assignPossibleDuplicate(owner, Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(),
            owner.getMembershipNumber());
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
        normalizeEmail(ownerFieldsDto);
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        currentOwner.setEmail(ownerFieldsDto.getEmail());
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


    /**
     * Rejects an owner whose mandatory fields ({@code firstName}, {@code lastName},
     * {@code address}, {@code city}, {@code telephone}) are missing or blank. Bean
     * Validation already rejects {@code null} values and the pattern-constrained fields
     * when blank, but {@code address} and {@code city} have no pattern, so a
     * whitespace-only value would otherwise slip through.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws RequiredFieldsMissingException if any required field is missing or blank
     */
    /**
     * Common street-type abbreviations expanded during address normalization. Keys are the
     * upper-cased abbreviation tokens; values are their canonical expansions.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes the submitted address on create, accepting either the structured form
     * ({@code addressLine1} plus an optional {@code addressLine2}) or the flat {@code address}
     * form retained for backward compatibility. Each supplied field is canonicalized the same way
     * (trimmed, internal whitespace collapsed to a single space, upper-cased, and common
     * street-type abbreviations expanded — see {@link #ADDRESS_ABBREVIATIONS}), and the normalized
     * values are written back onto the request.
     *
     * <p>The structured fields are preferred when present: when a non-blank {@code addressLine1} is
     * supplied, the flat {@code address} is set to the composed value — the normalized
     * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended
     * when an {@code addressLine2} is present — otherwise it keeps the normalized flat value. The
     * composed {@code address} is therefore persisted, echoed back, and used by everything that
     * reads the flat address. A blank/absent value in every form leaves {@code address} blank so the
     * required-field check can report it as missing.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private static void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        String addressLine1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
        String addressLine2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
        String flat = normalizeAddress(ownerFieldsDto.getAddress());
        ownerFieldsDto.setAddressLine1(addressLine1);
        ownerFieldsDto.setAddressLine2(addressLine2);
        String composed;
        if (addressLine1 != null && !addressLine1.isBlank()) {
            composed = (addressLine2 != null && !addressLine2.isBlank())
                ? addressLine1 + " " + addressLine2
                : addressLine1;
        }
        else {
            composed = flat;
        }
        ownerFieldsDto.setAddress(composed);
    }

    /**
     * Canonicalizes an address value: trimmed, internal whitespace collapsed to single spaces,
     * upper-cased, and each whitespace-delimited token that is a known street-type abbreviation
     * expanded to its full form. A {@code null} value is returned unchanged.
     *
     * @param raw the address to canonicalize, possibly {@code null}
     * @return the normalized address, or {@code null} if {@code raw} was {@code null}
     */
    private static String normalizeAddress(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> errors = new ArrayList<>();
        requireText("firstName", ownerFieldsDto.getFirstName(), errors);
        requireText("lastName", ownerFieldsDto.getLastName(), errors);
        requireText("address", ownerFieldsDto.getAddress(), errors);
        requireText("city", ownerFieldsDto.getCity(), errors);
        requireText("telephone", ownerFieldsDto.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new RequiredFieldsMissingException(errors);
        }
    }

    private static void requireText(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }

    /**
     * Region -> inclusive 4-digit postcode range {@code {low, high}} used to validate a supplied
     * postcode against the owner's city. A region absent from this table (i.e. a city whose region
     * is {@code 'UNKNOWN'}) imposes no range, so any 4-digit postcode is accepted there.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates the submitted {@code postcode} only when present. The field is optional, so a
     * {@code null} value is accepted. When present it has already been constrained to four digits
     * by Bean Validation; here it is additionally checked against the owner's city: for a city
     * whose region is known (see {@link org.springframework.samples.petclinic.mapper.LocalityLookup}),
     * the postcode must fall within that region's inclusive range
     * ({@link #REGION_POSTCODE_RANGE}); a city with no known region accepts any 4-digit value.
     *
     * @param ownerFieldsDto the submitted owner fields (city already validated as present)
     * @throws InvalidPostcodeException with a 400 status if the postcode is out of range for the
     *                                  city's region
     */
    private static void validatePostcode(OwnerFieldsDto ownerFieldsDto) {
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null) {
            return;
        }
        String region = org.springframework.samples.petclinic.mapper.LocalityLookup
            .regionFor(ownerFieldsDto.getCity());
        int[] range = REGION_POSTCODE_RANGE.get(region);
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }

    /**
     * Normalizes the submitted telephone on create to E.164 form (see {@link #toE164}) and
     * writes the canonical {@code '+'}-prefixed value back onto the request so it is persisted
     * and echoed back in that form.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws RequiredFieldsMissingException with a 400 status if the telephone cannot form a
     *                                        valid E.164 number
     */
    private static void normalizeTelephone(OwnerFieldsDto ownerFieldsDto) {
        String e164 = toE164(ownerFieldsDto.getTelephone());
        if (e164 == null) {
            throw new RequiredFieldsMissingException(List.of("telephone"));
        }
        ownerFieldsDto.setTelephone(e164);
    }

    /**
     * Converts a raw telephone to E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. Spaces, dashes and brackets are stripped. When a leading {@code '+'}
     * and country code are present they are kept; otherwise country code {@code '+61'} is
     * assumed and a single leading {@code '0'} is dropped from the national digits. The result
     * must contain 8 to 15 digits after the {@code '+'}, and for the recognised country codes
     * the national-number length must match the country: {@code '+61'} (Australia) requires
     * exactly 9 national digits and {@code '+1'} (NANP) requires exactly 10.
     *
     * @param raw the submitted telephone, possibly {@code null}
     * @return the canonical E.164 string, or {@code null} if it is not a valid E.164 number
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        if (digits.startsWith("61")) {
            // Australia: exactly 9 national digits after the '+61' country code.
            if (digits.length() - 2 != 9) {
                return null;
            }
        } else if (digits.startsWith("1")) {
            // NANP: exactly 10 national digits after the '+1' country code.
            if (digits.length() - 1 != 10) {
                return null;
            }
        }
        return "+" + digits;
    }

    /**
     * Rejects a create whose E.164 telephone is already used by any existing owner. The
     * submitted telephone has already been normalized to E.164 by {@link #normalizeTelephone};
     * each stored owner's telephone is normalized the same way before comparison so differing
     * input formats collapse to the same value.
     *
     * @param normalizedTelephone the submitted, already-normalized E.164 telephone
     * @throws DuplicateTelephoneException with a 409 status if another owner already uses the
     *                                     same E.164 telephone
     */
    /**
     * Matches a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain of at least two dot-separated labels ending in an alphabetic TLD. None of
     * the parts may contain whitespace or a second {@code @}.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$");

    /**
     * Disposable email domains that are not accepted for an owner's email. A create whose email
     * domain (the part after the {@code '@'}, compared case-insensitively) is on this blocklist is
     * rejected. Kept lower-cased to match the normalized email.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /**
     * Normalizes the submitted email when present: the value is trimmed, required to be a
     * syntactically valid address, required not to use a disposable-email domain, and written back
     * lower-cased so it is persisted and echoed back in canonical form. A {@code null} email is
     * left untouched — the field is optional.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidEmailException with a 400 status if the email is present but not syntactically
     *                               valid, or its domain is on the disposable-domain blocklist
     */
    private static void normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null) {
            return;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidEmailException(email);
        }
        ownerFieldsDto.setEmail(normalized);
    }

    /**
     * Rejects a create whose supplied {@code registrationDate} is later than the server's current
     * date. A registration may be back-dated but never post-dated. The check is made against the
     * value as supplied, before any defaulting or business-day roll (see
     * {@link #defaultRegistrationDate}); a {@code null} registration date is accepted (it defaults
     * to the server date).
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws FutureRegistrationDateException with a 400 status if the supplied registration date is
     *                                         after the server's current date
     */
    private static void rejectFutureRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate supplied = ownerFieldsDto.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
    }

    /**
     * Establishes the effective registration date on create and rolls it onto a business day.
     * When the client does not supply a {@code registrationDate} it defaults to the server's
     * current date; the resulting date (supplied or defaulted) is then rolled forward to the
     * next Monday when it falls on a Saturday or Sunday (see {@link #toBusinessDay}). The
     * adjusted value is written back onto the request so it is persisted, echoed back in ISO
     * 'YYYY-MM-DD' format, and used by every value derived from the registration date (such as
     * the membership number's year segment and the daily create-limit).
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private static void defaultRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate effective = ownerFieldsDto.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        ownerFieldsDto.setRegistrationDate(toBusinessDay(effective));
    }

    /**
     * Fixed public holidays that the business-day roll skips. A registration date that lands on
     * one of these dates is advanced to the next non-holiday business day (see
     * {@link #toBusinessDay}).
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Rolls a date forward to the next business day: any date that falls on a Saturday, a Sunday
     * or a listed public holiday is advanced one day at a time until it lands on a weekday that is
     * not a public holiday; any other date is returned unchanged.
     *
     * @param date the date to adjust
     * @return the same date when it is a non-holiday weekday, otherwise the next non-holiday
     *         business day
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
     * Assigns the owner's {@code customerCode} on create, formatted
     * {@code '<REGION>-<HASH8>'} where REGION is the region derived from the owner's postcode
     * (preferring the postcode range, falling back to the city — the same derivation that yields
     * the owner's {@code locality}, see
     * {@link org.springframework.samples.petclinic.mapper.LocalityLookup#regionFor(String, String)}),
     * and HASH8 is the first 8 upper-cased hex characters of the SHA-256 digest of the owner's
     * already-normalized (E.164) {@code telephone} concatenated with its {@code lastName} — e.g.
     * {@code 'NSW-A1B2C3D4'}. There is no per-city sequence: two owners in the same region share a
     * customer code only when their normalized telephone and last name both match.
     *
     * <p>When the computed customer code collides with an existing owner's {@code customerCode},
     * it is de-duplicated by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that
     * makes it unique among the existing owners, so distinct owners always receive distinct codes.
     *
     * @param owner the owner being created (with its {@code postcode}, {@code city},
     *              {@code telephone} and {@code lastName} already set)
     */
    private void assignCustomerCode(Owner owner) {
        String region = org.springframework.samples.petclinic.mapper.LocalityLookup
            .regionFor(owner.getPostcode(), owner.getCity());
        String hash8 = sha256Hex(owner.getTelephone() + owner.getLastName())
            .substring(0, 8).toUpperCase(Locale.ROOT);
        String base = region + "-" + hash8;
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        String customerCode = base;
        for (int n = 2; existing.contains(customerCode); n++) {
            customerCode = base + "-" + n;
        }
        owner.setCustomerCode(customerCode);
    }

    /**
     * Assigns the owner's {@code householdId} on create: a stable, deterministic identifier for
     * the household, the first 12 hex characters of the SHA-256 digest of the owner's normalized
     * {@code lastName} and {@code postcode} (see {@link #householdIdFor}). Because it is derived
     * purely from those two fields, every owner sharing a last name and postcode is assigned the
     * same value automatically — including one joining an existing household via
     * {@code sharesHousehold} — independent of create order.
     *
     * @param owner the owner being created (with its {@code lastName} and {@code postcode} already set)
     */
    private void assignHouseholdId(Owner owner) {
        owner.setHouseholdId(householdIdFor(owner.getLastName(), owner.getPostcode()));
    }

    /**
     * Derives the deterministic {@code householdId} for the given {@code lastName} and
     * {@code postcode}: the first 12 hex characters of the SHA-256 digest of the normalized
     * {@code lastName} (see {@link #collapse}) concatenated with a {@code '|'} and the
     * {@code postcode} (empty when absent). Because it is derived purely from those two fields,
     * every owner sharing a last name and postcode receives the same value, independent of create
     * order. Shared by {@link #assignHouseholdId} (which persists it), {@link #identityKeyFor}
     * (which folds it into the identity key for duplicate detection) and
     * {@link #rejectDuplicateHousehold} (which blocks a second owner in the same household).
     *
     * @param lastName the owner's last name
     * @param postcode the owner's postcode, possibly {@code null}
     * @return the derived household id
     */
    private static String householdIdFor(String lastName, String postcode) {
        String key = collapse(lastName) + "|" + (postcode == null ? "" : postcode);
        return sha256Hex(key).substring(0, 12);
    }

    /**
     * Rejects a create whose computed {@code householdId} (derived from {@code lastName} and
     * {@code postcode}, see {@link #householdIdFor}) already belongs to an existing owner, unless
     * the request opts in via {@code sharesHousehold}. Because the household is keyed on
     * {@code (lastName, postcode)}, a second owner sharing both is the same household: it is
     * rejected with a 409 as a household duplicate unless it declares {@code sharesHousehold}, in
     * which case it is created as a declared household member. The scan is taken over the existing
     * owners before this one is persisted.
     *
     * @param ownerFieldsDto the submitted owner fields ({@code lastName} and {@code postcode} set)
     * @throws DuplicateHouseholdException with a 409 status if the household already exists and the
     *                                     request did not opt in via {@code sharesHousehold}
     */
    private void rejectDuplicateHousehold(OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String householdId = householdIdFor(ownerFieldsDto.getLastName(), ownerFieldsDto.getPostcode());
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (duplicate) {
            throw new DuplicateHouseholdException(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        }
    }

    /**
     * Assigns the owner's {@code householdMemberCount} on create: the number of owners in this
     * owner's household — those already sharing its {@code householdId} plus this owner itself —
     * after this create. Because {@code householdId} is derived from the owner's {@code lastName}
     * and {@code postcode} (see {@link #assignHouseholdId}), the count reflects every
     * owner registered to the same household, including those who joined via {@code sharesHousehold}.
     * The count is taken before this owner is persisted, so it is one more than the number of
     * existing household members.
     *
     * @param owner the owner being created (with its {@code householdId} already assigned)
     */
    private void assignHouseholdMemberCount(Owner owner) {
        String householdId = owner.getHouseholdId();
        long existing = this.clinicService.findAllOwners().stream()
            .filter(other -> householdId != null && householdId.equals(other.getHouseholdId()))
            .count();
        owner.setHouseholdMemberCount((int) (existing + 1));
    }

    /**
     * Assigns the owner's {@code namesakeCount} on create: the number of existing owners that
     * already share this owner's {@code firstName} and {@code lastName}, compared
     * case-insensitively. The count is taken before this owner is persisted, so it reflects only
     * pre-existing namesakes.
     *
     * @param owner the owner being created (with its {@code firstName} and {@code lastName} already set)
     */
    private void assignNamesakeCount(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName != null && firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName != null && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
        owner.setNamesakeCount((int) count);
    }

    /**
     * Assigns the owner's {@code membershipNumber} on create, formatted
     * {@code '<customerCode>-M<YY>'} where customerCode is the owner's already-assigned customer
     * code and YY is the last two digits of the {@code registrationDate} year, zero-padded
     * (e.g. {@code 'NSW-A1B2C3D4-M26'}). Derived purely from the owner's own fields, so both
     * {@link #assignCustomerCode} and {@link #defaultRegistrationDate} must run first.
     *
     * @param owner the owner being created (with its {@code customerCode} and
     *              {@code registrationDate} already set)
     */
    private void assignMembershipNumber(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }

    /**
     * The number of owners that must already carry a registration date before a subsequent
     * create for that same day is flagged with a bulk-signup warning. The warning is raised
     * once the count of owners already registered on that day <em>exceeds</em> this value.
     */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Assigns the owner's {@code bulkSignupWarning} on create: {@code true} when more than
     * {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry this owner's (business-day
     * adjusted) {@code registrationDate}, otherwise {@code false}. The count is taken over the
     * existing owners before this one is persisted, so it reflects only the owners already
     * created for that day (matching the accumulation used by {@link #rejectDailyLimit}).
     *
     * @param owner the owner being created (with its {@code registrationDate} already set)
     */
    private void assignBulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        owner.setBulkSignupWarning(count > BULK_SIGNUP_WARNING_THRESHOLD);
    }

    /**
     * Assigns the owner's soft-match duplicate flags on create. The create has already passed
     * {@link #rejectDuplicateIdentity} and {@link #rejectDuplicateHousehold}, so this owner is not
     * a hard duplicate. A declared household member (one that opted in via {@code sharesHousehold})
     * is never flagged: it is an acknowledged member of the household, not a suspected duplicate,
     * so {@code possibleDuplicate} is {@code false}. Otherwise it is a <em>possible</em> duplicate
     * when an existing owner shares its {@code lastName} (compared case-insensitively) and its
     * {@code postcode} but has a <em>different</em> normalized {@code telephone}:
     * {@code possibleDuplicate} is set {@code true} and {@code possibleDuplicateOf} to that owner's
     * id (the earliest such owner by id when several match). Otherwise {@code possibleDuplicate} is
     * {@code false} and {@code possibleDuplicateOf} is left unset. The scan is taken over the
     * existing owners before this one is persisted.
     *
     * @param owner the owner being created (with its {@code lastName}, {@code postcode} and
     *              already-normalized {@code telephone} set)
     * @param declaredHouseholdMember whether the request opted in via {@code sharesHousehold}
     */
    private void assignPossibleDuplicate(Owner owner, boolean declaredHouseholdMember) {
        if (declaredHouseholdMember) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Integer matchId = null;
        if (lastName != null && postcode != null && telephone != null) {
            matchId = this.clinicService.findAllOwners().stream()
                .filter(existing -> lastName.equalsIgnoreCase(existing.getLastName()))
                .filter(existing -> postcode.equals(existing.getPostcode()))
                .filter(existing -> !telephone.equals(existing.getTelephone()))
                .map(Owner::getId)
                .filter(id -> id != null)
                .min(Integer::compareTo)
                .orElse(null);
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    /**
     * Returns the lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value}.
     *
     * @param value the string to hash
     * @return the 64-character lower-case hex digest
     */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Rejects a create whose whole derived {@code identityKey} already equals that of an existing
     * owner. This single key consolidates what used to be three separate duplicate checks
     * (telephone, email and household): the key is
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId} (see
     * {@link #identityKeyFor}), and a create is a duplicate only when its <em>whole</em> key
     * matches an existing owner's. Because the telephone is part of the key, two members of the
     * same household (same {@code householdId}) with different telephones have different identity
     * keys and are both allowed; only an exact full-key match is rejected. The submitted telephone
     * and email have already been normalized by {@link #normalizeTelephone} and
     * {@link #normalizeEmail}, and each existing owner's key is computed the same way so differing
     * input formats collapse to the same value.
     *
     * @param ownerFieldsDto the submitted owner fields (telephone and email already normalized)
     * @throws DuplicateIdentityException with a 409 status if another owner already has the same
     *                                    whole identity key
     */
    private void rejectDuplicateIdentity(OwnerFieldsDto ownerFieldsDto) {
        String identityKey = identityKeyFor(ownerFieldsDto.getLastName(), ownerFieldsDto.getPostcode(),
            ownerFieldsDto.getTelephone(), ownerFieldsDto.getEmail());
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .filter(owner -> !owner.isDeleted())
            .map(owner -> identityKeyFor(owner.getLastName(), owner.getPostcode(),
                owner.getTelephone(), owner.getEmail()))
            .anyMatch(identityKey::equals);
        if (duplicate) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * Computes an owner's derived {@code identityKey} from its identity-bearing fields:
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. The telephone is
     * normalized to E.164 form (see {@link #toE164}), the email is lower-cased (empty when
     * absent), and the {@code householdId} is derived from the {@code lastName} and
     * {@code postcode} the same way {@link #assignHouseholdId} assigns it (see
     * {@link #householdIdFor}). Deriving every part canonically means two owners collide only when
     * their whole identities match, regardless of the exact input formats.
     *
     * @param lastName  the owner's last name
     * @param postcode  the owner's postcode
     * @param telephone the owner's telephone, in any format {@link #toE164} accepts
     * @param email     the owner's email, possibly {@code null}
     * @return the canonical identity key
     */
    private static String identityKeyFor(String lastName, String postcode, String telephone, String email) {
        String normalizedTelephone = toE164(telephone);
        String telephonePart = normalizedTelephone == null ? "" : normalizedTelephone;
        String emailPart = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        return telephonePart + "|" + emailPart + "|" + householdIdFor(lastName, postcode);
    }

    /**
     * The maximum number of owners permitted in a single city. A create is rejected once the
     * owner's city already contains this many owners.
     */
    private static final long CITY_CAPACITY = 50;

    /**
     * Rejects a create whose {@code city} already contains {@link #CITY_CAPACITY} or more owners,
     * compared case-insensitively (matching {@link #assignCustomerCode}). The count is taken
     * before this owner is persisted.
     *
     * @param city the submitted city
     * @throws CityAtCapacityException with a 409 status if the city is already at capacity
     */
    private void rejectCityAtCapacity(String city) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * The maximum number of owners permitted to be created in a single day. A create is
     * rejected once this many owners already carry today's {@code registrationDate}.
     */
    private static final long DAILY_LIMIT = 100;

    /**
     * Rejects a create once {@link #DAILY_LIMIT} or more owners already carry the given adjusted
     * business day as their {@code registrationDate}. The day counted against is this create's
     * effective registration date after the business-day roll (see {@link #defaultRegistrationDate}),
     * so owners are counted per adjusted business day. The count is taken before this owner is
     * persisted.
     *
     * @param businessDay the effective, business-day-adjusted registration date of this create
     * @throws DailyOwnerLimitExceededException with a 429 status if the day is already at the limit
     */
    private void rejectDailyLimit(LocalDate businessDay) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(businessDay);
        }
    }

    /**
     * Canonicalizes a value for household comparison: leading/trailing whitespace is trimmed,
     * every internal run of whitespace is collapsed to a single space, and the result is
     * lower-cased. A {@code null} value collapses to the empty string.
     *
     * @param value the value to canonicalize, possibly {@code null}
     * @return the collapsed, lower-cased value
     */
    private static String collapse(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
