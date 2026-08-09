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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.MembershipPoints;
import org.springframework.samples.petclinic.mapper.OwnerIdentity;
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

    /** Dedicated audit logger; each successful owner create emits one line here. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Monotonically increasing sequence stamped onto each structured {@code OWNER_CREATED}
     * event, shared across every controller instance so the {@code seq} strictly increases
     * across all owner creates for the life of the process.
     */
    private static final java.util.concurrent.atomic.AtomicLong AUDIT_EVENT_SEQ =
        new java.util.concurrent.atomic.AtomicLong();

    /**
     * The header carrying an idempotency key for owner creation. When a create repeats with a key
     * already seen, the originally created owner is returned with 200 instead of creating a duplicate.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per seen idempotency key, the id of the owner that create originally produced, so a
     * repeat of the same request returns that owner rather than creating a duplicate.
     */
    private final Map<String, Integer> idempotentCreates = new java.util.concurrent.ConcurrentHashMap<>();

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
        Map<String, Integer> householdSizes = householdSizes();
        List<OwnerDto> ownerDtos = new java.util.ArrayList<>();
        for (Owner owner : owners) {
            OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
            applyMembership(ownerDto, owner, householdSizes);
            ownerDtos.add(ownerDto);
        }
        return new ResponseEntity<>(ownerDtos, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        applyMembership(ownerDto, owner);
        ownerDto.setBulkSignupWarning(isBulkSignupDay());
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    OwnerDto existingDto = ownerMapper.toOwnerDto(existing);
                    applyMembership(existingDto, existing);
                    existingDto.setBulkSignupWarning(isBulkSignupDay());
                    return new ResponseEntity<>(existingDto, HttpStatus.OK);
                }
            }
        }
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String address = applyAddress(owner, ownerFieldsDto);
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(address);
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setTelephone(telephone);
        if (owner.getEmail() != null) {
            String email = normalizeEmail(owner.getEmail());
            if (email == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            owner.setEmail(email);
        }
        if (owner.getPostcode() != null && !isValidPostcode(owner.getPostcode(), owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveRegistrationDate = owner.getRegistrationDate();
        if (effectiveRegistrationDate == null) {
            effectiveRegistrationDate = LocalDate.now();
        }
        else if (effectiveRegistrationDate.isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate registrationDate = toBusinessDay(effectiveRegistrationDate);
        owner.setRegistrationDate(registrationDate);
        if (countOwnersInCity(owner.getCity()) >= MAX_OWNERS_PER_CITY) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (countOwnersRegisteredOn(registrationDate) >= MAX_OWNERS_PER_DAY) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        // The householdId is deterministic: the first twelve hex characters of SHA-256 over
        // '<normalizedLastName>|<postcode>'. Owners sharing a last name and postcode therefore
        // share the household automatically. An owner without a postcode has no household.
        boolean declaredHouseholdMember = false;
        if (owner.getPostcode() != null) {
            String householdId = householdId(householdKey(owner.getLastName()), owner.getPostcode());
            owner.setHouseholdId(householdId);
            // A second owner sharing a household (same lastName + postcode) but with a distinct
            // identity is a legitimate additional household member — true duplicates are still
            // caught by the identityKey check below. 'sharesHousehold' opts such an owner in as a
            // declared member so it is not additionally flagged as a possible duplicate.
            boolean householdOccupied = false;
            for (Owner existing : this.clinicService.findAllOwners()) {
                if (existing.isDeleted()) {
                    continue;
                }
                if (householdId.equals(computeHouseholdId(existing))) {
                    householdOccupied = true;
                    break;
                }
            }
            if (householdOccupied && Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
                declaredHouseholdMember = true;
            }
        }
        // Consolidated duplicate detection: reject only when the whole identityKey
        // (SHA-256 over normalizedTelephone | lowerEmail | soundex(lastName)) matches an
        // existing owner's. This is the single duplicate check; owners sharing a household
        // (same last name and postcode) but with different telephones produce different keys
        // and are no longer rejected here — they surface as soft matches below.
        String identityKey = OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.identityKey(toE164(existing.getTelephone()),
                existing.getEmail(), existing.getLastName()))) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        // Soft-match: the owner is not a hard (identityKey) duplicate, but if it shares an
        // existing owner's last name and postcode while having a different normalized
        // telephone it is still created and flagged as a possible duplicate of that owner. A
        // declared household member, however, is not a suspected duplicate, so it is never flagged.
        if (declaredHouseholdMember) {
            owner.setPossibleDuplicate(false);
        }
        else {
            applyPossibleDuplicate(owner);
        }
        owner.setCustomerCode(deduplicatedCustomerCode(
            customerCode(owner.getPostcode(), owner.getTelephone(), owner.getLastName())));
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotentCreates.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        Map<String, Integer> householdSizes = householdSizes();
        applyMembership(ownerDto, owner, householdSizes);
        capMembershipLevelToHousehold(ownerDto, owner, householdSizes);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel(), owner.getMembershipNumber());
        emitOwnerCreatedEvent(owner.getId(), primaryIdentifier(owner), ownerDto.getMembershipLevel());
        ownerDto.setBulkSignupWarning(isBulkSignupDay());
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * The owner's current primary identifier, as carried by the structured {@code OWNER_CREATED}
     * event. Today that is the {@link Owner#getCustomerCode() customerCode}; when the customer code
     * is later unified into the member id this is the single place that switches to the member id,
     * so the event automatically carries whatever the current primary identifier is.
     *
     * @param owner the newly created, saved owner
     * @return the owner's current primary identifier
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * Emits the immutable, structured {@code OWNER_CREATED} audit event via the {@code AUDIT}
     * logger, in addition to the human-readable audit line. The event is a JSON object
     * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}} where {@code seq}
     * is a process-wide monotonically increasing integer across creates, and the {@code customerCode}
     * slot carries the owner's {@link #primaryIdentifier current primary identifier}.
     *
     * @param ownerId         the newly created owner's id
     * @param primaryId       the owner's current primary identifier (customerCode today)
     * @param membershipLevel the owner's resolved membership level
     */
    private static void emitOwnerCreatedEvent(int ownerId, String primaryId, int membershipLevel) {
        long seq = AUDIT_EVENT_SEQ.incrementAndGet();
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"customerCode\":\"{}\",\"membershipLevel\":{},"
            + "\"event\":\"OWNER_CREATED\"}", seq, ownerId, primaryId, membershipLevel);
    }

    /**
     * Reads the {@code Idempotency-Key} header of the request currently being handled, used to make
     * owner creation idempotent. Returns the trimmed key when present and non-blank, otherwise
     * {@code null} (including when there is no active servlet request), so a missing or blank key
     * simply means "no idempotency" and the create proceeds normally.
     *
     * @return the current request's idempotency key, or {@code null} when none is supplied
     */
    private static String currentIdempotencyKey() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        String key = servletAttributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        if (key == null || key.isBlank()) {
            return null;
        }
        return key.trim();
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
        // Soft delete: the row is retained and flagged deleted rather than removed, so the
        // owner is still readable via GET but is ignored by the create duplicate/identity checks.
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
     * The maximum number of owners permitted per city. Owner creation is rejected once the
     * owner's city already contains this many owners.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Counts how many existing owners live in the given city, compared case-insensitively.
     * This is evaluated before the new owner is persisted, so the owner being created is not
     * included in the count.
     *
     * @param city the new owner's city
     * @return the number of existing owners in that city
     */
    private int countOwnersInCity(String city) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }

    /**
     * The maximum number of owners that may be created in a single business day. Owner
     * creation is rejected once this many owners already fall on the new owner's adjusted
     * business day {@code registrationDate}.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * The fixed list of public holidays that the business-day roll skips over. A date that
     * lands on one of these is rolled forward to the next non-holiday business day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Rolls a date forward to the next business day: a Saturday, Sunday or listed public
     * holiday is moved forward one day at a time until a non-holiday weekday is reached,
     * while a date that is already a non-holiday weekday is returned unchanged.
     *
     * @param date the date to adjust
     * @return the same date if it is a non-holiday weekday, otherwise the next such day
     */
    private LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Counts how many existing owners fall on the given business day, i.e. whose {@code
     * registrationDate} rolls forward to the same business day. This is evaluated before the
     * new owner is persisted, so the owner being created is not included in the count.
     *
     * @param businessDay the adjusted business day to count against
     * @return the number of existing owners registered on that business day
     */
    private int countOwnersRegisteredOn(LocalDate businessDay) {
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            LocalDate existingDate = existing.getRegistrationDate();
            if (existingDate != null && businessDay.equals(toBusinessDay(existingDate))) {
                count++;
            }
        }
        return count;
    }

    /**
     * The number of owners that may be created on a single business day before a bulk-signup
     * warning is raised. Once more than this many owners already fall on the current business
     * day, {@code bulkSignupWarning} is reported as {@code true} in the owner response.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Reports whether an unusually high number of owners have been created for today's business
     * day, i.e. whether more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already fall on
     * the business day that {@code LocalDate.now()} rolls forward to.
     *
     * @return {@code true} once more than 80 owners have been created today, otherwise {@code false}
     */
    private boolean isBulkSignupDay() {
        return countOwnersRegisteredOn(toBusinessDay(LocalDate.now())) > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Builds the customer code for a newly created owner, formatted
     * {@code '<REGION>-<HASH8>'} where {@code REGION} is the canonical region
     * {@link OwnerLocality#forPostcodeOrUnknown derived from the postcode} (or
     * {@code "UNKNOWN"} when the postcode resolves no region) and {@code HASH8} is the
     * first eight upper-case hex characters of the SHA-256 digest of
     * {@code normalizedTelephone + lastName}. The code carries no sequence number, so it is
     * stable for a given telephone and last name rather than dependent on creation order.
     *
     * @param postcode            the owner's postcode (may be {@code null})
     * @param normalizedTelephone the owner's normalized E.164 telephone
     * @param lastName            the owner's last name
     * @return the assigned customer code (e.g. {@code 'NSW-1A2B3C4D'})
     */
    private static String customerCode(String postcode, String normalizedTelephone, String lastName) {
        String region = org.springframework.samples.petclinic.mapper.OwnerLocality.forPostcodeOrUnknown(postcode);
        return region + "-" + sha256Hex8(normalizedTelephone + lastName);
    }

    /**
     * De-duplicates a freshly computed {@code base} customer code against the codes already
     * assigned to existing owners. When {@code base} is free it is returned unchanged;
     * otherwise {@code '-<n>'} is appended with the smallest {@code n} of two or more that
     * yields a code no existing owner holds.
     *
     * @param base the computed {@code '<REGION>-<HASH8>'} customer code
     * @return {@code base}, or {@code base + "-<n>"} de-duplicated to be unique
     */
    private String deduplicatedCustomerCode(String base) {
        Set<String> existing = new HashSet<>();
        for (Owner owner : this.clinicService.findAllOwners()) {
            if (owner.getCustomerCode() != null) {
                existing.add(owner.getCustomerCode());
            }
        }
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
     * Returns the first eight upper-case hex characters of the SHA-256 digest of the UTF-8
     * bytes of {@code source} — the {@code HASH8} component of an owner's customer code.
     *
     * @param source the string to hash
     * @return the first eight upper-case hex characters of {@code SHA-256(source)}
     */
    private static String sha256Hex8(String source) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 8).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Builds the membership number for a newly created owner, formatted
     * {@code '<customerCode>-M<YY>'} where {@code customerCode} is the owner's assigned
     * customer code and {@code YY} is the last two digits of the fiscal year of the owner's
     * business-day-adjusted registration date, zero-padded to two digits. The fiscal year
     * starts on 1 July and is named by the calendar year it ends in.
     *
     * @param customerCode     the owner's assigned customer code (e.g. {@code 'NSW-1A2B3C4D'})
     * @param registrationDate the owner's business-day-adjusted registration date
     * @return the assigned membership number (e.g. {@code 'NSW-1A2B3C4D-M27'})
     */
    private String membershipNumber(String customerCode, LocalDate registrationDate) {
        int fiscalYear = org.springframework.samples.petclinic.mapper.FiscalYear.yearValue(registrationDate);
        return String.format("%s-M%02d", customerCode, Math.floorMod(fiscalYear, 100));
    }

    /**
     * Counts how many existing owners share the given first and last name, compared
     * case-insensitively. This is evaluated before the new owner is persisted, so the
     * owner being created is not included in the count.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of existing owners with the same first and last name
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
     * Canonical expansions applied to common street-type abbreviations during address
     * normalization. Keys and values are upper-cased so the lookup runs after the address
     * has itself been upper-cased.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes an owner address for creation. Leading and trailing whitespace is trimmed,
     * every run of internal whitespace is collapsed to a single space, the result is
     * upper-cased and common street-type abbreviations are expanded ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). An abbreviation is recognized as a whole
     * token, optionally carrying a single trailing full stop (so both {@code 'St'} and
     * {@code 'St.'} become {@code 'STREET'}). The normalized value is what gets stored,
     * returned and compared for household duplicates; an address that is blank once
     * normalized fails the required-field check.
     *
     * @param address the raw address value supplied by the client
     * @return the normalized address (empty string when {@code address} is null or blank)
     */
    /**
     * Normalizes the address an owner supplies, preferring the structured form, and stores the
     * normalized structured lines on the owner. When a non-blank {@code addressLine1} is present
     * the structured fields are used: {@code addressLine1} (and {@code addressLine2}, when
     * present) are {@link #normalizeAddress normalized} and set on the owner, and the composed
     * address returned is the normalized {@code addressLine1} with a single space and the
     * normalized {@code addressLine2} appended when {@code addressLine2} is present. Otherwise
     * the flat {@code address} is normalized and returned, and the structured lines are left
     * unset. The returned value becomes the owner's stored {@code address}, read by everything
     * downstream (household hash, postcode validation and locality); an owner that supplies
     * neither form yields an empty string, failing the required-address check.
     *
     * @param owner          the owner being created, whose structured address lines are set here
     * @param ownerFieldsDto the submitted owner fields
     * @return the composed, normalized address (empty string when no address form is supplied)
     */
    private static String applyAddress(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        String addressLine1 = ownerFieldsDto.getAddressLine1();
        if (addressLine1 != null && !addressLine1.isBlank()) {
            String normalizedLine1 = normalizeAddress(addressLine1);
            owner.setAddressLine1(normalizedLine1);
            String addressLine2 = ownerFieldsDto.getAddressLine2();
            if (addressLine2 != null && !addressLine2.isBlank()) {
                String normalizedLine2 = normalizeAddress(addressLine2);
                owner.setAddressLine2(normalizedLine2);
                return normalizedLine1 + " " + normalizedLine2;
            }
            owner.setAddressLine2(null);
            return normalizedLine1;
        }
        owner.setAddressLine1(null);
        owner.setAddressLine2(null);
        return normalizeAddress(ownerFieldsDto.getAddress());
    }

    private static String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            String core = token.endsWith(".") ? token.substring(0, token.length() - 1) : token;
            String expanded = ADDRESS_ABBREVIATIONS.get(core);
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(expanded != null ? expanded : token);
        }
        return normalized.toString();
    }

    /**
     * Normalizes a last name or address for household-duplicate comparison: leading and
     * trailing whitespace is trimmed, every run of internal whitespace is collapsed to a
     * single space and the result is lower-cased. Two owners are considered to share a
     * household when both their last names and addresses produce the same key.
     *
     * @param value the raw last name or address value
     * @return the normalized comparison key (empty string when {@code value} is null)
     */
    private static String householdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Derives the stable shared household identifier for the given normalized last name key
     * and postcode. The identifier is deterministic — the same household (same
     * {@link #householdKey normalized} last name and postcode) always maps to the same
     * value — so every owner in a household is assigned an identical id. It is the
     * upper-cased first twelve hex characters of the SHA-256 digest of {@code
     * '<lastNameKey>|<postcode>'}.
     *
     * @param lastNameKey the normalized last name key
     * @param postcode    the owner's postcode
     * @return the shared household identifier
     */
    private static String householdId(String lastNameKey, String postcode) {
        String source = lastNameKey + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 12).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Computes the deterministic {@link #householdId household id} an existing owner belongs to,
     * derived from its {@link #householdKey normalized} last name and its postcode. Returns
     * {@code null} when the owner has no postcode (such an owner has no household), so it never
     * collides with a computed household id.
     *
     * @param owner the existing owner
     * @return the owner's household id, or {@code null} when it has no postcode
     */
    private static String computeHouseholdId(Owner owner) {
        if (owner.getPostcode() == null) {
            return null;
        }
        return householdId(householdKey(owner.getLastName()), owner.getPostcode());
    }

    /**
     * Populates an owner's {@code membershipPoints} and {@code membershipLevel} on the given
     * dto, sizing the owner's household from all owners so the household-of-three-or-more
     * factor is scored correctly. Use {@link #applyMembership(OwnerDto, Owner, Map)} instead
     * when mapping a collection to size every household from a single scan.
     *
     * @param ownerDto the dto to populate
     * @param owner    the owner being mapped
     */
    private void applyMembership(OwnerDto ownerDto, Owner owner) {
        applyMembership(ownerDto, owner, householdSizes());
    }

    /**
     * Populates an owner's {@code membershipPoints} and {@code membershipLevel} on the given
     * dto using a pre-computed map of household id to member count.
     *
     * @param ownerDto       the dto to populate
     * @param owner          the owner being mapped
     * @param householdSizes household id to member count, as returned by {@link #householdSizes()}
     */
    private void applyMembership(OwnerDto ownerDto, Owner owner, Map<String, Integer> householdSizes) {
        int householdSize = owner.getHouseholdId() == null ? 1
            : householdSizes.getOrDefault(owner.getHouseholdId(), 1);
        int points = MembershipPoints.points(owner, householdSize);
        ownerDto.setMembershipPoints(points);
        ownerDto.setMembershipLevel(MembershipPoints.level(points));
    }

    /**
     * Caps a newly created owner's {@code membershipLevel} at one above the highest
     * {@code membershipLevel} among the other current members of its household, writing the
     * capped value back onto the dto and returning it. A member's current level is scored the
     * same way as {@link #applyMembership}, using {@code householdSizes} so the
     * household-of-three-or-more factor reflects the household as it now stands. When the owner
     * has no household, or is the only member of its household, no cap applies and the level is
     * returned unchanged.
     *
     * @param ownerDto       the newly created owner's dto, with its uncapped membership already applied
     * @param owner          the newly created owner (already saved, so it has an id)
     * @param householdSizes household id to member count, as returned by {@link #householdSizes()}
     * @return the resulting (possibly capped) membership level
     */
    private int capMembershipLevelToHousehold(OwnerDto ownerDto, Owner owner,
        Map<String, Integer> householdSizes) {
        int level = ownerDto.getMembershipLevel();
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return level;
        }
        Integer maxMemberLevel = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.isDeleted() || existing.getId().equals(owner.getId())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                int size = householdSizes.getOrDefault(householdId, 1);
                int memberLevel = MembershipPoints.level(MembershipPoints.points(existing, size));
                maxMemberLevel = maxMemberLevel == null ? memberLevel
                    : Math.max(maxMemberLevel, memberLevel);
            }
        }
        if (maxMemberLevel != null && level > maxMemberLevel + 1) {
            level = maxMemberLevel + 1;
            ownerDto.setMembershipLevel(level);
        }
        return level;
    }

    /**
     * Counts how many owners belong to each household, keyed by household id. Owners without a
     * household id (no postcode) are excluded — such an owner is its own single-member household.
     *
     * @return a map from household id to the number of owners sharing it
     */
    private Map<String, Integer> householdSizes() {
        Map<String, Integer> sizes = new java.util.HashMap<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            String householdId = existing.getHouseholdId();
            if (householdId != null) {
                sizes.merge(householdId, 1, Integer::sum);
            }
        }
        return sizes;
    }

    /**
     * Applies soft-match (possible-duplicate) detection to a new owner that has already
     * cleared hard {@code identityKey} duplicate rejection. The owner is flagged as a
     * possible duplicate when a non-deleted existing owner shares its {@link
     * OwnerIdentity#soundex soundex(lastName)} and its postcode while producing a different
     * {@code identityKey}; {@code possibleDuplicateOf} is then set to that existing owner's id.
     * Because the telephone is part of the identity key, two owners with the same last name and
     * postcode but different telephones differ in their keys and so surface here as a soft match
     * rather than a hard duplicate. When several existing owners match, the one with the lowest
     * id is chosen so the result is deterministic. When no such owner exists — or the new owner
     * has no postcode to compare — {@code possibleDuplicate} is set to {@code false} and no
     * matching id is recorded.
     *
     * @param owner the new owner, already normalized (E.164 telephone, validated postcode)
     */
    private void applyPossibleDuplicate(Owner owner) {
        owner.setPossibleDuplicate(false);
        if (owner.getPostcode() == null) {
            return;
        }
        String soundex = OwnerIdentity.soundex(owner.getLastName());
        String identityKey = OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        Owner match = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (soundex.equals(OwnerIdentity.soundex(existing.getLastName()))
                && owner.getPostcode().equals(existing.getPostcode())
                && !identityKey.equals(OwnerIdentity.identityKey(toE164(existing.getTelephone()),
                    existing.getEmail(), existing.getLastName()))) {
                if (match == null || (existing.getId() != null
                    && existing.getId() < match.getId())) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    /**
     * Normalizes a telephone number for owner creation into E.164 form. Spaces, dashes
     * and brackets are stripped. When the number already carries a leading {@code '+'}
     * and country code these are kept; a number written in national trunk form (a leading
     * {@code '0'}) is treated as Australian, so the {@code '0'} is dropped and country code
     * {@code '+61'} prepended; any other bare number is treated as a NANP number and country
     * code {@code '+1'} is prepended. The resulting number must have between 8 and 15 digits
     * after the {@code '+'}, and its national-number length must be valid for the detected
     * country code (see {@link #hasValidNationalLength}).
     *
     * @param telephone the raw telephone value supplied by the client
     * @return the E.164 telephone (e.g. {@code +61412345678}), or {@code null} when it
     *         cannot form a valid E.164 number (signalling a 400 Bad Request)
     */
    private static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else if (cleaned.startsWith("0")) {
            digits = "61" + cleaned.substring(1);
        } else {
            digits = "1" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        if (!hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Validates the national-number length of an E.164 digit string against its country
     * code: Australia ({@code +61}) requires exactly 9 national digits and the NANP
     * ({@code +1}) requires exactly 10. Numbers under any other country code are not
     * length-checked here — only the general 8-to-15-digit E.164 bound applies to them.
     *
     * @param digits the E.164 digits (without the leading {@code '+'})
     * @return {@code true} when the national-number length is valid for the country code
     */
    private static boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }

    /**
     * Inclusive 4-digit postcode ranges permitted for each canonical region: NSW 2000-2099,
     * VIC 3000-3099 and QLD 4000-4099. A region absent from this table (e.g. the {@code
     * "UNKNOWN"} derived for a city with no known region) imposes no range restriction.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's postcode for creation. A postcode is only ever validated when
     * present (an absent postcode is optional and accepted). When present it must be exactly
     * four digits and, for a city whose {@link OwnerLocality#forCity region} has a known
     * postcode range, must fall inside that inclusive range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
     *
     * @param postcode the supplied postcode (never {@code null} here)
     * @param city     the owner's city, used to derive the region
     * @return {@code true} when the postcode is valid for the city, {@code false} otherwise
     *         (signalling a 400 Bad Request)
     */
    private static boolean isValidPostcode(String postcode, String city) {
        if (!postcode.matches("[0-9]{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(
            org.springframework.samples.petclinic.mapper.OwnerLocality.forCity(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * Disposable email domains that are not accepted for owner creation. An owner whose
     * email domain (compared case-insensitively) is in this set is rejected with a 400
     * Bad Request.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Normalizes an owner email for creation. A present email must be a syntactically
     * valid address whose domain is not on the disposable-domain blocklist ({@link
     * #DISPOSABLE_EMAIL_DOMAINS}); the returned value is lower-cased.
     *
     * @param email the raw email value supplied by the client (never {@code null} here)
     * @return the lower-cased email, or {@code null} when it is not a syntactically valid
     *         address or its domain is a disposable domain (signalling a 400 Bad Request)
     */
    private static String normalizeEmail(String email) {
        String trimmed = email.trim();
        if (!trimmed.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return null;
        }
        String normalized = trimmed.toLowerCase();
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            return null;
        }
        return normalized;
    }
}
