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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
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
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.mapper.IdentityKey;
import org.springframework.samples.petclinic.mapper.MemberIdCheckDigit;
import org.springframework.samples.petclinic.mapper.OwnerLocality;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitReachedException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityAtCapacityException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.audit.OwnerCreatedEvent;
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

    /**
     * Dedicated audit logger. On a successful create an audit line carrying the new owner's id,
     * {@code memberId}, {@code registrationDate} and {@code membershipLevel} is emitted to the
     * logger named {@code AUDIT}.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Monotonically increasing sequence number stamped onto each structured {@link OwnerCreatedEvent}.
     * Shared across all creates so the events carry a total order independent of the owner id.
     */
    private static final AtomicLong AUDIT_SEQUENCE = new AtomicLong();

    /**
     * Syntactic check for an email address: a non-empty local part, an {@code @}, and a domain with
     * at least one dot and no whitespace on either side.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Disposable email domains that an owner's email may not use. An email whose domain (the part
     * after the {@code @}, compared case-insensitively) is on this list is rejected with a 400
     * response naming {@code email}.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Common street-type abbreviations expanded during address normalization. Keys are the
     * upper-cased abbreviation as it appears as a whole whitespace-delimited token; values are the
     * expanded form.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Required national-number length per E.164 country code (the digits that follow the country
     * code). {@code '+61'} (Australia) requires 9 national digits; {@code '+1'} (NANP) requires 10.
     * A country code that is not listed here is not length-checked, keeping other numbers accepted.
     */
    private static final Map<String, Integer> COUNTRY_NATIONAL_LENGTH =
        Map.of("1", 10, "61", 9);

    /**
     * Inclusive 4-digit postcode range allowed for each known region, keyed by the region an owner's
     * city derives to (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD): NSW 2000-2099, VIC
     * 3000-3099, QLD 4000-4099. A city that derives to no known region ({@code "UNKNOWN"}) is not in
     * this table and accepts any 4-digit postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * The maximum number of owners a single city may contain. A create request for a city that
     * already holds this many owners is rejected with a 409 response.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * The number of owners a city must already contain for a create in that city to be flagged as
     * approaching capacity. When an existing city holds at least this many owners but fewer than
     * {@value #MAX_OWNERS_PER_CITY} (so the create is still accepted), the new owner is created with
     * {@code capacityWarning} set to {@code true}.
     */
    private static final int CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * The maximum number of owners that may be registered in a single day. A create request made
     * once this many owners already carry today's {@code registrationDate} is rejected with a 429
     * response.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * The number of owners that must already carry a day's {@code registrationDate} before a create
     * for that day is flagged as a bulk signup. Once more than this many owners already exist for the
     * day, the new owner is created with {@code bulkSignupWarning} set to {@code true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * The tenure, in elapsed fiscal years, an owner must exceed to reach {@code membershipLevel} 4.
     * Tenure accrues from membership, so a newly created owner (zero tenure) never clears this
     * threshold and is therefore capped at level 3 on create.
     */
    private static final int TENURE_LEVEL_THRESHOLD_FISCAL_YEARS = 1;

    /**
     * The HTTP header carrying a client-supplied idempotency token for owner creation. When a create
     * request repeats with a token already seen, the originally created owner is returned instead of
     * a second owner being created.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers the owner id created for each seen {@code Idempotency-Key}, so a repeated create with
     * an already-seen key returns the originally created owner (200) instead of creating a duplicate.
     */
    private final Map<String, Integer> idempotentCreates = new ConcurrentHashMap<>();

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
        String idempotencyKey = idempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = this.idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        normalizeAddress(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        normalizeTelephone(ownerFieldsDto);
        normalizeEmail(ownerFieldsDto);
        validatePostcode(ownerFieldsDto);
        String householdId = computeHouseholdId(ownerFieldsDto);
        rejectDuplicate(ownerFieldsDto);
        rejectCityAtCapacity(ownerFieldsDto);
        resolveRegistrationDate(ownerFieldsDto);
        rejectDailyLimitReached(ownerFieldsDto.getRegistrationDate());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setHouseholdId(householdId);
        assignMemberId(owner);
        assignNamesakeCount(owner);
        assignBulkSignupWarning(owner);
        assignCapacityWarning(owner);
        assignPossibleDuplicate(owner, ownerFieldsDto);
        assignHouseholdMembership(owner);
        assignMembership(owner);
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            this.idempotentCreates.put(idempotencyKey, owner.getId());
        }
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), owner.getMembershipLevel());
        emitOwnerCreatedEvent(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Emits the immutable structured {@link OwnerCreatedEvent} for a just-persisted owner to the
     * {@code AUDIT} logger as a JSON object, alongside the human-readable audit line. Each event is
     * stamped with the next value of {@link #AUDIT_SEQUENCE} (monotonically increasing across creates)
     * and carries the owner's current primary identifier via {@link #primaryIdentifier}.
     */
    private void emitOwnerCreatedEvent(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(AUDIT_SEQUENCE.incrementAndGet(), owner.getId(),
            primaryIdentifier(owner), owner.getMembershipLevel());
        AUDIT.info(event.toJson());
    }

    /**
     * The owner's primary identifier, as carried by the structured {@link OwnerCreatedEvent}: the
     * unified {@code memberId} assigned at creation.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        normalizeAddress(ownerFieldsDto);
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setAddressLine1(ownerFieldsDto.getAddressLine1());
        currentOwner.setAddressLine2(ownerFieldsDto.getAddressLine2());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(toE164(ownerFieldsDto.getTelephone()));
        normalizeEmail(ownerFieldsDto);
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

    /**
     * Returns the trimmed {@code Idempotency-Key} header of the current request, or {@code null} when
     * the header is absent, blank or there is no active request. A present key opts a create into the
     * idempotent-repeat behaviour tracked by {@link #idempotentCreates}.
     */
    private String idempotencyKey() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        String key = attributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        return key.trim();
    }

    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (isBlank(ownerFieldsDto.getAddress())) {
            missingFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(missingFields);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Normalizes an owner's address on create, supporting both the structured form
     * ({@code addressLine1} plus optional {@code addressLine2}) and the flat {@code address} form kept
     * for backward compatibility. Whichever fields are supplied are canonicalized in place - trimmed,
     * internal whitespace runs collapsed to a single space, upper-cased and common street-type
     * abbreviations expanded as whole tokens ({@code ST}->{@code STREET}, {@code RD}->{@code ROAD},
     * {@code AVE}->{@code AVENUE}). The canonical {@code addressLine1}/{@code addressLine2} are written
     * back, and the composed {@code address} - the normalized {@code addressLine1} with a single space
     * and the normalized {@code addressLine2} appended when present, falling back to the normalized
     * flat {@code address} when no structured line is given - is written onto the request so it is what
     * gets validated, compared, stored and returned. Running before the required-field check means an
     * address that is blank in both forms after normalization is rejected like a missing one.
     */
    private void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        String line1 = canonicalAddress(ownerFieldsDto.getAddressLine1());
        String line2 = canonicalAddress(ownerFieldsDto.getAddressLine2());
        String flat = canonicalAddress(ownerFieldsDto.getAddress());
        ownerFieldsDto.setAddressLine1(line1);
        ownerFieldsDto.setAddressLine2(line2);
        ownerFieldsDto.setAddress(composeAddress(line1, line2, flat));
    }

    /**
     * Composes the effective address, preferring the structured form: when {@code line1} is present
     * (non-blank) the result is {@code line1}, with a single space and {@code line2} appended when
     * {@code line2} is present; otherwise the flat {@code address} is used. All three inputs are
     * expected to be already canonicalized. Returns the flat value (which may be {@code null} or blank,
     * left for the required-field check to reject) when no structured line is supplied.
     */
    private static String composeAddress(String line1, String line2, String flat) {
        if (line1 != null && !line1.isBlank()) {
            if (line2 != null && !line2.isBlank()) {
                return line1 + " " + line2;
            }
            return line1;
        }
        return flat;
    }

    /**
     * Returns the canonical form of an address: trimmed, internal whitespace runs collapsed to a
     * single space, upper-cased, with common street-type abbreviations expanded as whole tokens. A
     * {@code null} value returns {@code null}. The transform is idempotent, so an already-canonical
     * address is returned unchanged, making it safe to apply to stored values during comparison.
     */
    private static String canonicalAddress(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
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

    /**
     * Normalizes an owner's {@code telephone} on create into E.164 form. A leading {@code '+'} and
     * the country code that follows it are kept; otherwise the country code {@code '+61'} is assumed
     * and a single leading {@code '0'} is dropped from the national digits. Spaces, dashes and
     * brackets are stripped. The result must have 8 to 15 digits after the {@code '+'}, and when the
     * country code is one whose national-number length is known ({@code '+61'} => 9 national digits,
     * {@code '+1'} => 10) the national number must have exactly that many digits. The E.164 string is
     * written back onto the request so it is what gets stored and returned. Any value that cannot
     * form a valid E.164 number is rejected with a 400 response whose {@code errors} array names
     * {@code telephone}.
     */
    private void normalizeTelephone(OwnerFieldsDto ownerFieldsDto) {
        ownerFieldsDto.setTelephone(toE164(ownerFieldsDto.getTelephone()));
    }

    /**
     * Converts a raw telephone value to E.164 form, or throws {@link InvalidOwnerFieldsException}
     * (400) naming {@code telephone} when it cannot form a valid number.
     */
    private static String toE164(String telephone) {
        if (telephone == null) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        String countryCode;
        String national;
        if (hasCountryCode) {
            countryCode = matchCountryCode(digits);
            national = digits;
        } else {
            countryCode = "61";
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            national = "61" + digits;
        }
        if (!national.matches("[0-9]{8,15}")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        if (countryCode != null) {
            Integer requiredLength = COUNTRY_NATIONAL_LENGTH.get(countryCode);
            if (requiredLength != null
                && national.length() - countryCode.length() != requiredLength) {
                throw new InvalidOwnerFieldsException(List.of("telephone"));
            }
        }
        return "+" + national;
    }

    /**
     * Returns the longest known E.164 country code (a key of {@link #COUNTRY_NATIONAL_LENGTH}) that
     * the given country-code-prefixed digit string starts with, or {@code null} when none is known so
     * the number is not length-checked against a country.
     */
    private static String matchCountryCode(String digits) {
        return COUNTRY_NATIONAL_LENGTH.keySet().stream()
            .filter(digits::startsWith)
            .max(Comparator.comparingInt(String::length))
            .orElse(null);
    }

    /**
     * Rejects creating an owner that duplicates an existing one on the derived {@code identityKey} -
     * the SHA-256 hex digest of {@code normalizedTelephone + '|' + lowerEmail + '|' +
     * soundex(lastName)} (see {@link IdentityKey}). An exact key match means the same telephone, email
     * and same-sounding surname, i.e. the same person re-registering; because the telephone is part of
     * the key, two people who merely share a surname (and postcode) but carry different telephones no
     * longer collide here - they are surfaced as a soft match instead (see
     * {@link #assignPossibleDuplicate}). This single identity key is the only hard-duplicate rule; the
     * computed {@code householdId} no longer contributes a separate 409. A match is a 409.
     *
     * <p>Owners flagged {@code deleted} (soft-deleted via {@code DELETE /api/owners/{id}}) are skipped
     * entirely, so a normally-blocking duplicate is allowed when the only matching owner has been
     * deleted.
     *
     * <p>The {@code sharesHousehold} flag bypasses the entire duplicate block. The incoming telephone
     * and email have already been normalized by {@link #normalizeTelephone} and {@link #normalizeEmail}
     * (the email-domain blocklist applies first, in {@link #normalizeEmail}); each existing owner's key
     * is derived the same way before comparison. A match results in a 409 response naming
     * {@code identityKey}.
     */
    private void rejectDuplicate(OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String identityKey = IdentityKey.of(ownerFieldsDto.getTelephone(), ownerFieldsDto.getEmail(),
            ownerFieldsDto.getLastName());
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (identityKey.equals(existingIdentityKey(existing))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }

    /**
     * Derives an already-stored owner's {@code identityKey} for duplicate comparison: the stored
     * telephone is converted to E.164 form (or the empty string when it cannot form a valid number),
     * the stored email is lower-cased and the stored last name is reduced to its Soundex code, exactly
     * as {@link IdentityKey#of} does for an incoming request.
     */
    private static String existingIdentityKey(Owner existing) {
        String telephone = orEmpty(normalizeExisting(existing.getTelephone()));
        return IdentityKey.of(telephone, existing.getEmail(), existing.getLastName());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns the E.164 form of an already-stored telephone for duplicate comparison, or {@code null}
     * when the stored value cannot form a valid E.164 number (so it never matches an incoming one).
     */
    private static String normalizeExisting(String telephone) {
        try {
            return toE164(telephone);
        } catch (InvalidOwnerFieldsException ex) {
            return null;
        }
    }

    /**
     * Rejects creating an owner in a city that has reached capacity: a city that already contains
     * {@value #MAX_OWNERS_PER_CITY} or more owners cannot take another one. Existing owners are
     * counted with a case-insensitive match on {@code city}. A city at or over the limit results in a
     * 409 response naming {@code city}.
     */
    private void rejectCityAtCapacity(OwnerFieldsDto ownerFieldsDto) {
        String city = ownerFieldsDto.getCity();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }

    /**
     * Rejects creating an owner once {@value #MAX_OWNERS_PER_DAY} or more owners already carry the
     * given adjusted business-day {@code registrationDate}, counted by an exact match. The date has
     * already been resolved and rolled forward off a weekend by {@link #resolveRegistrationDate}, so
     * the limit is enforced per business day. A day at or over the limit results in a 429 response.
     */
    private void rejectDailyLimitReached(LocalDate registrationDate) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitReachedException(registrationDate);
        }
    }

    /**
     * Normalizes a value for case-insensitive, whitespace-insensitive comparison: leading and
     * trailing whitespace is trimmed, internal runs of whitespace are collapsed to a single space and
     * the result is lower-cased. A {@code null} value normalizes to the empty string.
     */
    private static String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner's optional {@code email}. The field may be omitted entirely, but when a
     * value is present it must be a syntactically valid address; the trimmed value is lower-cased and
     * written back onto the request so that is what gets stored and returned. A present but invalid
     * address is rejected with a 400 response whose {@code errors} array names {@code email}.
     */
    private void normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null) {
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        ownerFieldsDto.setEmail(normalized);
    }

    /**
     * Validates an owner's optional {@code postcode} on create. The field may be omitted entirely
     * (so an owner created without a postcode stays accepted), but when a value is present it must be
     * exactly 4 digits and valid for the region the owner's {@code city} derives to: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099. A city with no known region accepts any 4-digit postcode. A
     * present postcode that is malformed or out of range for the city's region is rejected with a 400
     * response whose {@code errors} array names {@code postcode}. A valid value is left on the request
     * as-is so it is stored and returned.
     */
    private void validatePostcode(OwnerFieldsDto ownerFieldsDto) {
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
        int[] range = REGION_POSTCODE_RANGE.get(OwnerLocality.derive(ownerFieldsDto.getCity()));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidOwnerFieldsException(List.of("postcode"));
            }
        }
    }

    /**
     * Resolves an owner's effective {@code registrationDate} on create and rolls it onto a business
     * day. When the caller supplies no value it defaults to the server's current date; a value
     * provided in the request is kept. A supplied value later than the server's current date is
     * rejected with a 400 response whose {@code errors} array names {@code registrationDate}. The
     * effective date, whether supplied or defaulted, must fall on a business day: a Saturday or
     * Sunday is rolled forward to the following Monday. The adjusted date is written back onto the
     * request so it is what gets stored and returned (in ISO {@code YYYY-MM-DD} format) and what
     * every value derived from the registration date is based on.
     */
    private void resolveRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate effective = ownerFieldsDto.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        else if (effective.isAfter(LocalDate.now())) {
            throw new InvalidOwnerFieldsException(List.of("registrationDate"));
        }
        ownerFieldsDto.setRegistrationDate(toBusinessDay(effective));
    }

    /**
     * The fixed public-holiday calendar. A registration date landing on one of these days is rolled
     * forward, just like a weekend, until it reaches a non-holiday business day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Rolls a date forward onto a business day: a Saturday, Sunday or listed public holiday is
     * advanced one day at a time until it reaches the next non-holiday business day; a weekday that
     * is not a holiday is returned unchanged.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Returns whether the given date falls on a Saturday or Sunday.
     */
    private static boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    /**
     * Assigns the owner's unified {@code memberId} on create, formatted
     * {@code '<REGION><FY><HASH8><CHK>'} where {@code REGION} is the region the owner's identity
     * derives to from its postcode (falling back to the city table when the postcode is absent or in
     * no known range), {@code FY} is the two-digit fiscal year of the business-day-adjusted
     * {@code registrationDate} (the fiscal year starts on 1 July, e.g. a registration on 2026-08-03
     * falls in fiscal year 2027 and yields {@code '27'}), {@code HASH8} is the first eight upper-case
     * hex characters of the SHA-256 digest of the normalized telephone concatenated with the last
     * name, and {@code CHK} is a single Luhn check digit over the digits of the
     * {@code '<REGION><FY><HASH8>'} body (e.g. {@code 'NSW271A2B3C4D4'}). The telephone has already
     * been normalized to E.164 form by {@link #normalizeTelephone} and {@code registrationDate} has
     * been defaulted, so both inputs are present. The identity is a pure function of region, fiscal
     * year, telephone and last name; the check digit is computed over the body before any
     * collision-handling suffix is applied.
     */
    private void assignMemberId(Owner owner) {
        String region = OwnerLocality.derive(owner.getCity(), owner.getPostcode());
        String fy = String.format("%02d", FiscalYear.yearOf(owner.getRegistrationDate()) % 100);
        String hash8 = shaHexUpper(owner.getTelephone() + owner.getLastName(), 8);
        String body = region + fy + hash8;
        int chk = MemberIdCheckDigit.of(body);
        owner.setMemberId(deduplicateMemberId(body + chk));
    }

    /**
     * Returns {@code memberId} unchanged when no existing owner already carries it; otherwise appends
     * {@code '-<n>'} with the smallest {@code n} of 2 or more that yields a value not held by any
     * existing owner. Compared against the pre-existing owners, so the returned member id is unique
     * among them.
     */
    private String deduplicateMemberId(String memberId) {
        Set<String> existing = new HashSet<>();
        for (Owner owner : this.clinicService.findAllOwners()) {
            if (owner.getMemberId() != null) {
                existing.add(owner.getMemberId());
            }
        }
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
     * Assigns the new owner's {@code namesakeCount} on create: the number of owners that already
     * exist sharing the same {@code firstName} and {@code lastName}, compared case-insensitively.
     * Computed before the new owner is persisted, so it counts only the pre-existing owners.
     */
    private void assignNamesakeCount(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    /**
     * Assigns the new owner's {@code membershipPoints} and derived {@code membershipLevel} on create.
     * Points start at 0 and accrue: plus 2 when an email is present, plus 1 when {@code namesakeCount}
     * is 0, plus 2 for a household of 3 or more members, and plus 3 for tenure exceeding
     * {@value #TENURE_LEVEL_THRESHOLD_FISCAL_YEARS} elapsed fiscal year. The points are then mapped to a level of 1 (0-1
     * points), 2 (2-3), 3 (4-5), or 4 (6 or more). Runs after {@link #assignNamesakeCount} and
     * {@link #assignHouseholdMembership} so those counts are settled. A newly created owner has zero
     * tenure, so the tenure bonus never applies on create.
     */
    private void assignMembership(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > TENURE_LEVEL_THRESHOLD_FISCAL_YEARS) {
            points += 3;
        }
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(capMembershipLevelByHousehold(owner, membershipLevelForPoints(points)));
    }

    /**
     * Caps a newly derived {@code membershipLevel} so it cannot exceed one above the current maximum
     * {@code membershipLevel} among the new owner's existing household members - the pre-existing owners
     * already carrying the same {@code householdId}. When the owner belongs to no household, or has no
     * existing household member, no cap applies and the derived level is returned unchanged. Runs after
     * {@link #assignHouseholdMembership} so the owner's {@code householdId} is settled.
     */
    private int capMembershipLevelByHousehold(Owner owner, int derivedLevel) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return derivedLevel;
        }
        Integer maxHouseholdLevel = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId()) && existing.getMembershipLevel() != null
                && (maxHouseholdLevel == null || existing.getMembershipLevel() > maxHouseholdLevel)) {
                maxHouseholdLevel = existing.getMembershipLevel();
            }
        }
        if (maxHouseholdLevel == null) {
            return derivedLevel;
        }
        return Math.min(derivedLevel, maxHouseholdLevel + 1);
    }

    /**
     * Maps {@code membershipPoints} to a {@code membershipLevel}: 1 for 0-1 points, 2 for 2-3, 3 for
     * 4-5, and 4 for 6 or more points.
     */
    private int membershipLevelForPoints(int points) {
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
     * The tenure, in elapsed fiscal years, of a newly created owner. Tenure accrues from membership
     * over time, and a new owner has not been a member for any elapsed fiscal year yet, so this is
     * always {@code 0} on create (regardless of any backdated {@code registrationDate}).
     */
    private long tenureFiscalYears(Owner owner) {
        return 0L;
    }

    /**
     * Assigns the new owner's {@code bulkSignupWarning} on create: {@code true} when more than
     * {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry the new owner's adjusted
     * business-day {@code registrationDate}, otherwise {@code false}. Counted the same way as the
     * daily-limit rule and computed before the new owner is persisted, so it counts only the
     * pre-existing owners for that day.
     */
    private void assignBulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_SIGNUP_WARNING_THRESHOLD);
    }

    /**
     * Assigns the new owner's {@code capacityWarning} on create: {@code true} when the owner's city
     * already holds between {@value #CAPACITY_WARNING_THRESHOLD} and {@value #MAX_OWNERS_PER_CITY}
     * minus one owners (approaching the {@value #MAX_OWNERS_PER_CITY} capacity limit), otherwise
     * {@code false}. Existing owners are counted case-insensitively on {@code city}, the same way
     * {@link #rejectCityAtCapacity} counts them, and computed before the new owner is persisted so it
     * counts only the pre-existing owners. A city at or over the hard limit has already been rejected
     * by {@link #rejectCityAtCapacity}, so this only ever sees counts below the limit.
     */
    private void assignCapacityWarning(Owner owner) {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= CAPACITY_WARNING_THRESHOLD && count < MAX_OWNERS_PER_CITY);
    }

    /**
     * Assigns the new owner's {@code possibleDuplicate} / {@code possibleDuplicateOf} on create. The
     * owner has already cleared the duplicate block, so it is not a hard duplicate. It is flagged as a
     * possible duplicate when an existing owner has the same {@code soundex(lastName)} and the same
     * {@code postcode} (a present, exact match) yet a <em>different</em> {@code identityKey} - the
     * soft-match now keys on the surname sound and postcode rather than an exact last name, and fires
     * precisely for the near-miss the identity key lets through (same household surname and postcode
     * but a different telephone, so a different key). When such an owner exists,
     * {@code possibleDuplicate} is set to {@code true} and {@code possibleDuplicateOf} to that owner's
     * id (the lowest id when several match); otherwise {@code possibleDuplicate} is {@code false} and
     * {@code possibleDuplicateOf} is left {@code null}. Computed before the new owner is persisted, so
     * it considers only the pre-existing owners.
     *
     * <p>A declared household member ({@code sharesHousehold} set to {@code true}) is never flagged: it
     * has deliberately opted into an existing household, so its shared last name and postcode are
     * expected rather than a suspected duplicate.
     */
    private void assignPossibleDuplicate(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String postcode = owner.getPostcode();
        String soundex = IdentityKey.soundex(owner.getLastName());
        String identityKey = IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : this.clinicService.findAllOwners()) {
                if (soundex.equals(IdentityKey.soundex(existing.getLastName()))
                    && postcode.equals(existing.getPostcode())
                    && !identityKey.equals(existingIdentityKey(existing))
                    && existing.getId() != null
                    && (matchId == null || existing.getId() < matchId)) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    /**
     * Computes the new owner's deterministic {@code householdId}: the first twelve upper-case hex
     * characters of the SHA-256 digest of {@code "<normalizedLastName>|<postcode>"}, where the last
     * name is normalized for comparison (trimmed, internal whitespace collapsed, lower-cased) and the
     * postcode is the already-validated 4-digit value. Because it is a pure function of the last name
     * and postcode, any two owners sharing those - regardless of creation order or whether they opted
     * into {@code sharesHousehold} - land in the same household automatically. Returns {@code null}
     * when the owner has no postcode, so such owners belong to no household. This single value feeds
     * both the duplicate detection ({@link #rejectDuplicate}) and the household-size count
     * ({@link #assignHouseholdMembership}).
     */
    private String computeHouseholdId(OwnerFieldsDto ownerFieldsDto) {
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String lastName = normalizeForComparison(ownerFieldsDto.getLastName());
        return shaHexUpper(lastName + "|" + postcode, 12);
    }

    /**
     * Assigns the new owner's {@code householdMemberCount} on create: the number of owners sharing
     * the new owner's {@code householdId} once this create is applied — that is, the new owner plus
     * every existing owner already carrying the same identifier. Runs after the owner's
     * {@code householdId} has been set from {@link #computeHouseholdId} so the identifier is settled.
     * Left {@code null} when the owner belongs to no household.
     */
    private void assignHouseholdMembership(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setHouseholdMemberCount(null);
            return;
        }
        int count = 1;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdMemberCount(count);
    }

    /**
     * Returns the first {@code length} characters of the SHA-256 digest of {@code input}, rendered as
     * upper-case hexadecimal. Used to derive the {@code memberId} HASH8 segment and the household
     * identifier, both stable functions of their inputs.
     */
    private static String shaHexUpper(String input, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, length).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
