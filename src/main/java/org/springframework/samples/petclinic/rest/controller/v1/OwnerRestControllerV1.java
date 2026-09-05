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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
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

    /** Dedicated audit logger; a successful create emits one line here. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Syntactic email check: a non-empty local part, an '@', and a dotted domain, none containing spaces. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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
        return withOwner(ownerId, owner ->
            new ResponseEntity<>(buildOwnerDto(owner), HttpStatus.OK));
    }

    /**
     * Look up the owner identified by {@code ownerId} and, when it exists, produce the response with
     * {@code onFound}; when no such owner exists, short-circuit with {@code 404 NOT_FOUND}. Centralizes
     * the fetch-and-guard shared by the single-owner endpoints so it is written once, not copied.
     */
    private <T> ResponseEntity<T> withOwner(Integer ownerId, Function<Owner, ResponseEntity<T>> onFound) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return onFound.apply(owner);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (!normalizeAndValidateFields(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate suppliedRegistrationDate = owner.getRegistrationDate();
        if (suppliedRegistrationDate != null && suppliedRegistrationDate.isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate registrationDate = resolveRegistrationDate(suppliedRegistrationDate);
        owner.setRegistrationDate(registrationDate);
        if (countOwnersRegisteredOn(registrationDate) >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        owner.setIdentityKey(identityKey(owner));
        if (isDuplicate(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (countOwnersInCity(owner.getCity()) >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        assignRegistrationAttributes(owner);
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(buildOwnerDto(owner), headers, HttpStatus.CREATED);
    }

    /**
     * Canonicalize an incoming owner's submitted contact and address fields into their stored forms
     * and validate them, returning whether the owner may proceed to creation. On success the owner's
     * address, email (when supplied) and telephone are replaced with their normalized values. Returns
     * {@code false} - so the caller can reject the request with {@code 400 BAD_REQUEST} - when the
     * address is blank after normalization, a supplied email is not syntactically valid, the postcode
     * is not acceptable for the owner's city, or the telephone cannot form a valid E.164 number.
     */
    private boolean normalizeAndValidateFields(Owner owner) {
        String address = normalizeAddress(owner.getAddress());
        if (address.isEmpty()) {
            return false;
        }
        owner.setAddress(address);
        if (owner.getEmail() != null) {
            String email = owner.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return false;
            }
            owner.setEmail(email);
        }
        if (!isPostcodeValidForCity(owner)) {
            return false;
        }
        String telephone = normalizeTelephone(owner.getTelephone());
        if (telephone == null) {
            return false;
        }
        owner.setTelephone(telephone);
        return true;
    }

    /**
     * Build the response DTO for a single owner, attaching the per-response derived flags that are
     * computed against the owner population rather than carried on the owner itself (currently the
     * {@code bulkSignupWarning}). Used by the single-owner read and create endpoints; the collection
     * listing intentionally omits these flags.
     */
    private OwnerDto buildOwnerDto(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarning());
        return ownerDto;
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
        String email = ownerFieldsDto.getEmail();
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        currentOwner.setEmail(email);
        this.clinicService.saveOwner(currentOwner);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> deleteOwner(Integer ownerId) {
        return withOwner(ownerId, owner -> {
            this.clinicService.deleteOwner(owner);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        });
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

    /** Matches a well-formed postcode: exactly 4 digits. */
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("^[0-9]{4}$");

    /**
     * Whether {@code owner}'s postcode is acceptable. Postcode is optional: a null postcode is always
     * accepted. When present it must be a 4-digit value and, when the owner's city maps to a known
     * region, must fall within that region's inclusive range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099); a city with no known region accepts any 4-digit postcode. The region-to-range
     * table lives on {@link Owner#postcodeRangeForRegion(String)} and is shared, not duplicated here.
     */
    private static boolean isPostcodeValidForCity(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return true;
        }
        if (!POSTCODE_PATTERN.matcher(postcode).matches()) {
            return false;
        }
        int[] range = Owner.postcodeRangeForRegion(owner.getRegion());
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /** Matches a well-formed E.164 number: a '+' followed by 8 to 15 digits. */
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[0-9]{8,15}$");

    /**
     * Required national-number length per country code: '+61' (Australia) expects 9 national
     * digits and '+1' (NANP) expects 10. A number whose country code is listed here but whose
     * national part is not the required length is rejected.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of(
        "+61", 9, "+1", 10);

    /**
     * Canonical stored form of a telephone number in E.164: a leading '+' and country
     * code are kept when present; otherwise country code '+61' is assumed and a single
     * leading '0' is dropped from the national digits. Spaces, dashes and brackets are
     * stripped. The result must be a '+' followed by 8 to 15 digits, and its national number
     * must have the length required for its country code ('+61' => 9 digits, '+1' => 10).
     * Returns {@code null} when the number cannot form a valid E.164 value.
     */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()\\[\\]-]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = cleaned;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = "+61" + national;
        }
        if (!E164_PATTERN.matcher(e164).matches()) {
            return null;
        }
        return hasValidNationalNumberLength(e164) ? e164 : null;
    }

    /**
     * Whether the national number of a well-formed E.164 value has the length required for its
     * country code. Country codes not listed in {@link #NATIONAL_NUMBER_LENGTHS} carry no
     * per-country length rule and are accepted.
     */
    private static boolean hasValidNationalNumberLength(String e164) {
        for (Map.Entry<String, Integer> entry : NATIONAL_NUMBER_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (e164.startsWith(countryCode)) {
                return e164.length() - countryCode.length() == entry.getValue();
            }
        }
        return true;
    }

    /**
     * Compute and assign the derived attributes carried by a newly registered owner: the customer
     * code, membership number, household id, namesake count and household member count. Each is
     * derived from the owner's already-normalized fields and the existing owner population, so this
     * must run before the owner is saved, while the counts still exclude the owner being created.
     */
    private void assignRegistrationAttributes(Owner owner) {
        owner.setCustomerCode(generateCustomerCode(owner));
        owner.setMembershipNumber(generateMembershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setHouseholdId(householdId(owner));
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setHouseholdMemberCount(countHouseholdMembers(owner));
        assignPossibleDuplicate(owner);
    }

    /**
     * Flag {@code owner} as a possible duplicate when, though not a hard duplicate (its create having
     * already cleared the {@link #isDuplicate} identity gate), it shares an existing owner's last name
     * (compared case-insensitively with collapsed whitespace) and postcode but carries a different
     * telephone. Sets {@code possibleDuplicate} accordingly and, when true, {@code possibleDuplicateOf}
     * to the matching owner's id. Runs before save, against the existing owner population.
     */
    private void assignPossibleDuplicate(Owner owner) {
        Integer matchId = findPossibleDuplicateOf(owner);
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    /**
     * The id of an existing owner that shares {@code owner}'s postcode and last name (compared
     * case-insensitively with collapsed whitespace) but has a different telephone, or {@code null}
     * when there is none. When several match, the lowest id is returned. An owner without a postcode
     * never matches.
     */
    private Integer findPossibleDuplicateOf(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return null;
        }
        String lastName = normalizeHouseholdField(owner.getLastName());
        String telephone = owner.getTelephone();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getId() != null
                && postcode.equals(existing.getPostcode())
                && normalizeHouseholdField(existing.getLastName()).equals(lastName)
                && !telephone.equals(existing.getTelephone()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
    }

    /**
     * Build {@code owner}'s customer code '<REGION>-<HASH8>': REGION is the region code derived from
     * the owner's postcode ({@link Owner#getRegion()}) and HASH8 the first 8 upper-case hex
     * characters of SHA-256 over the owner's already-normalized telephone concatenated with the
     * owner's last name (e.g. 'NSW-1A2B3C4D'). There are no sequence numbers: the identity is a pure
     * function of the region and the (telephone, last name) pair.
     */
    private String generateCustomerCode(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String hash8 = sha256Hex(telephone + lastName).substring(0, 8);
        return owner.getRegion() + "-" + hash8;
    }

    /**
     * The number of existing owners registered in {@code city}, compared case-insensitively
     * with collapsed whitespace.
     */
    private int countOwnersInCity(String city) {
        String normalized = normalizeHouseholdField(city);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHouseholdField(existing.getCity()).equals(normalized))
            .count();
    }

    /**
     * The effective registration date to store for a new owner: the date supplied on the request
     * when present, otherwise the current server date, in either case rolled forward to a business
     * day so it never falls on a weekend (a Saturday or Sunday is advanced to the following Monday).
     * This single point of resolution is what every registration-date-derived value (the stored
     * date, the membership number's year segment, the per-day create-limit) is computed from.
     */
    private static LocalDate resolveRegistrationDate(LocalDate suppliedDate) {
        LocalDate date = suppliedDate != null ? suppliedDate : LocalDate.now();
        return toBusinessDay(date);
    }

    /**
     * Roll {@code date} forward to the next business day: a Saturday or Sunday is advanced to the
     * following Monday; a weekday is returned unchanged.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Build the membership number '<customerCode>-M<YY>' where YY is the last two digits of the
     * {@code registrationDate} year (e.g. 'NSW-1A2B3C4D-M26').
     */
    private static String generateMembershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", registrationDate.getYear() % 100);
        return customerCode + "-M" + yy;
    }

    /**
     * The number of existing owners (before this create) sharing {@code owner}'s first name and
     * last name, compared case-insensitively.
     */
    private int countNamesakes(Owner owner) {
        String firstName = normalizeHouseholdField(owner.getFirstName());
        String lastName = normalizeHouseholdField(owner.getLastName());
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHouseholdField(existing.getFirstName()).equals(firstName)
                && normalizeHouseholdField(existing.getLastName()).equals(lastName))
            .count();
    }

    /**
     * Whether more than 80 owners have already been created today, i.e. carry today's (business-day
     * resolved) registration date. Surfaced on responses as {@code bulkSignupWarning} to flag an
     * unusually high daily signup volume.
     */
    private boolean isBulkSignupWarning() {
        return countOwnersRegisteredOn(toBusinessDay(LocalDate.now())) > 80;
    }

    /** The number of existing owners whose registration date is {@code date}. */
    private int countOwnersRegisteredOn(LocalDate date) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * The single derived key all duplicate detection flows through, formed from the owner's
     * already-normalized fields as {@code normalizedTelephone + '|' + (email or empty) + '|' +
     * (householdId or empty)}. It is computed at the create-time identity gate, before the household
     * id is derived, so the household segment is empty at that point. Because the telephone is part
     * of the key, two owners in the same household with different telephones resolve to different
     * keys and are both allowed.
     */
    private static String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String household = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + household;
    }

    /**
     * Whether {@code owner}'s whole {@link #identityKey} equals an existing owner's, which is the sole
     * duplicate condition: the previously separate telephone, email and household checks are now all
     * expressed through this one key, so only an exact full-key match is a duplicate and a create that
     * trips it is rejected with {@code 409 CONFLICT}.
     */
    private boolean isDuplicate(Owner owner) {
        String key = owner.getIdentityKey();
        return this.clinicService.findAllOwners().stream()
            .map(Owner::getIdentityKey)
            .filter(existing -> existing != null)
            .anyMatch(key::equals);
    }

    /** Common street-type abbreviations expanded during address normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Canonical stored form of an owner's address: leading/trailing whitespace trimmed, inner
     * whitespace collapsed to single spaces, upper-cased, with common street-type abbreviations
     * expanded per word ('ST' -> 'STREET', 'RD' -> 'ROAD', 'AVE' -> 'AVENUE'). Returns the empty
     * string when {@code address} is null or blank after normalization.
     */
    private static String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }

    /**
     * Canonical form for comparing a household field: trimmed, inner whitespace collapsed
     * to single spaces and lower-cased, so the match is case-insensitive and whitespace-insensitive.
     */
    private static String normalizeHouseholdField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Existing owners that share {@code owner}'s household, i.e. resolve to the same
     * {@link #householdId(Owner) householdId}. Household membership is defined by that single derived
     * key rather than re-derived from the underlying fields here, so the household is described in one
     * place and every household-keyed value stays consistent with it.
     */
    private List<Owner> findHousemates(Owner owner) {
        String householdId = householdId(owner);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId(existing).equals(householdId))
            .toList();
    }

    /**
     * The number of owners in {@code owner}'s household once this owner is created, i.e. the existing
     * housemates (owners sharing the same {@code householdId}) plus {@code owner} itself.
     */
    private int countHouseholdMembers(Owner owner) {
        return findHousemates(owner).size() + 1;
    }

    /**
     * A stable identifier shared by every owner in the same household, i.e. every owner with the
     * same last name and address (compared case-insensitively with collapsed whitespace). Derived
     * deterministically from those normalized fields so housemates always resolve to the same value,
     * regardless of registration order.
     */
    private static String householdId(Owner owner) {
        String key = normalizeHouseholdField(owner.getLastName()) + "\n"
            + normalizeHouseholdField(owner.getAddress());
        return sha256Hex(key).substring(0, 24);
    }

    /** The upper-case hex SHA-256 of the UTF-8 bytes of {@code input}, as 64 hex characters. */
    private static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
