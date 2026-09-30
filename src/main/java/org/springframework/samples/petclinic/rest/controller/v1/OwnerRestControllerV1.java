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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.E164PhoneNumbers;
import org.springframework.samples.petclinic.util.IdentityKeyUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
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
     * Assigns the monotonically increasing sequence number carried by each structured
     * {@link OwnerCreatedEvent}. Static so the order spans every create handled by this endpoint,
     * regardless of the controller instance, giving the audit event stream a total order.
     */
    private static final AtomicLong AUDIT_EVENT_SEQUENCE = new AtomicLong();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final IdempotencyKeyRegistry idempotencyKeyRegistry;

    private final HttpServletRequest request;

    /** HTTP header carrying the idempotency key for the create-owner endpoint. */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 IdempotencyKeyRegistry idempotencyKeyRegistry,
                                 HttpServletRequest request) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.idempotencyKeyRegistry = idempotencyKeyRegistry;
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

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        String idempotencyKey = this.request.getHeader(IDEMPOTENCY_KEY_HEADER);
        Integer existingId = this.idempotencyKeyRegistry.findOwnerId(idempotencyKey);
        if (existingId != null) {
            Owner existing = this.clinicService.findOwnerById(existingId);
            if (existing != null) {
                return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
            }
        }
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        applyAddress(owner);
        validatePostcode(owner.getCity(), owner.getPostcode());
        LocalDate effectiveRegistrationDate = owner.getRegistrationDate();
        if (effectiveRegistrationDate == null) {
            effectiveRegistrationDate = LocalDate.now();
        } else if (effectiveRegistrationDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "registrationDate must not be later than the current server date");
        }
        effectiveRegistrationDate = rollToBusinessDay(effectiveRegistrationDate);
        owner.setRegistrationDate(effectiveRegistrationDate);
        if (countOwnersRegisteredOn(effectiveRegistrationDate) >= DAILY_CREATE_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "the maximum number of owners that may be created today has been reached");
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // The household id is deterministic (derived from the last name and postcode), so every
        // owner is keyed to its household automatically, regardless of creation order.
        this.clinicService.assignHousehold(owner);
        // An owner joining an existing household is now admitted rather than rejected; their
        // membership level is instead capped relative to the household (see the level-ceiling rule
        // applied via the membershipLevelCap below).
        boolean householdInUse = this.clinicService.isHouseholdInUse(owner.getHouseholdId(), null);
        if (isIdentityKeyInUse(ownerMapper.toIdentityKey(owner), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "an owner with the same identity already exists");
        }
        if (countOwnersInCity(owner.getCity()) >= CITY_CAPACITY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "city has reached its maximum number of owners");
        }
        owner.setCustomerCode(customerCodeFor(owner));
        owner.setMembershipNumber(membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(countHouseholdMembers(owner.getHouseholdId()) + 1);
        owner.setMembershipLevelCap(householdMembershipLevelCap(owner.getHouseholdId()));
        owner.setBulkSignupWarning(
            countOwnersRegisteredOn(effectiveRegistrationDate) > BULK_SIGNUP_WARNING_THRESHOLD);
        // A declared household member (one created with 'sharesHousehold' that joins an existing
        // household) is not a suspected duplicate, so it is never flagged as a possible duplicate.
        boolean declaredHouseholdMember = sharesHousehold && householdInUse;
        Owner softDuplicate = declaredHouseholdMember ? null : findPossibleDuplicate(owner);
        owner.setPossibleDuplicate(softDuplicate != null);
        owner.setPossibleDuplicateOf(softDuplicate == null ? null : softDuplicate.getId());
        this.clinicService.saveOwner(owner);
        this.idempotencyKeyRegistry.record(idempotencyKey, owner.getId());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerDto.getMembershipLevel(), ownerDto.getMembershipNumber());
        OwnerCreatedEvent event = OwnerCreatedEvent.of(AUDIT_EVENT_SEQUENCE.incrementAndGet(),
            owner.getId(), primaryIdentifier(owner), ownerDto.getMembershipLevel());
        AUDIT.info(event.toJson());
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
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setAddressLine1(ownerFieldsDto.getAddressLine1());
        currentOwner.setAddressLine2(ownerFieldsDto.getAddressLine2());
        applyAddress(currentOwner);
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

    /**
     * The owner's current primary identifier, carried by the structured {@link OwnerCreatedEvent}.
     * Today an owner's primary identifier is its {@code customerCode}; this single accessor is the one
     * place that decides which field is primary, so if a later change unifies the customer code into a
     * member id, redirecting this method to the member id makes the audit event carry it instead.
     *
     * @param owner the created owner
     * @return the owner's current primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * Builds the customer code for a new owner, formatted {@code '<REGION>-<HASH8>'} where REGION is
     * the region derived from the owner's postcode (preferring the postcode, falling back to the
     * city table, else {@code 'UNKNOWN'}; see {@link OwnerMapper#regionOf(Owner)}) and HASH8 is the
     * first 8 upper-case hex characters of the SHA-256 digest of the owner's normalized E.164
     * telephone concatenated with the last name (e.g. {@code 'NSW-1A2B3C4D'}). The code carries no
     * sequence number, so its base form is a stable function of the owner's region and
     * telephone/last-name identity rather than of creation order.
     *
     * <p>When that computed code already belongs to an existing owner it is de-duplicated by
     * appending {@code '-<n>'} with the smallest {@code n} of 2 or more that yields a code no
     * existing owner already has; the de-duplicated code is returned.
     *
     * @param owner the owner being created, with its telephone already normalized to E.164 form
     * @return the assigned, collision-free customer code
     */
    private String customerCodeFor(Owner owner) {
        String region = ownerMapper.regionOf(owner);
        String hash8 = IdentityKeyUtils.sha256UpperHex(owner.getTelephone() + owner.getLastName())
            .substring(0, 8);
        String baseCode = region + "-" + hash8;
        if (!isCustomerCodeInUse(baseCode)) {
            return baseCode;
        }
        int n = 2;
        String candidate = baseCode + "-" + n;
        while (isCustomerCodeInUse(candidate)) {
            n++;
            candidate = baseCode + "-" + n;
        }
        return candidate;
    }

    /**
     * Determines whether any existing owner already carries the given {@code customerCode}, compared
     * exactly. Called while assigning a new owner's code so collisions can be de-duplicated.
     *
     * @param customerCode the candidate customer code to look for
     * @return {@code true} if an existing owner already has that customer code
     */
    private boolean isCustomerCodeInUse(String customerCode) {
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (customerCode.equals(existing.getCustomerCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The maximum number of owners allowed to live in a single city. A create is rejected once the
     * owner's city already contains this many owners.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * The maximum number of owners that may be created on a single day. A create is rejected with
     * {@link HttpStatus#TOO_MANY_REQUESTS} once this many owners already have today's
     * {@code registrationDate}.
     */
    private static final int DAILY_CREATE_LIMIT = 100;

    /**
     * The number of owners that must already have been created for the current day before a new
     * create is flagged with {@code bulkSignupWarning}. The warning is set once more than this many
     * owners already carry today's {@code registrationDate}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * The fixed public holidays that the business-day roll skips. A registration date landing on one
     * of these dates is rolled forward, exactly as it is for a weekend day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"),
        LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Rolls a registration date forward onto a business day: a date that falls on a Saturday, a
     * Sunday or a listed public holiday is advanced one day at a time until it lands on a weekday
     * that is not a public holiday, while a date already on a non-holiday weekday is returned
     * unchanged. Applied to the effective registration date (supplied or defaulted to the server
     * date) so that the stored {@code registrationDate}, and every value derived from it, always
     * falls on a business day.
     *
     * @param date the effective registration date
     * @return the same date when it is a non-holiday weekday, otherwise the next such business day
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
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
     * Counts the existing owners whose {@code registrationDate} equals the given (already
     * business-day-adjusted) date, i.e. the number of owners that have already been created for that
     * business day. Called before a new owner is saved, so the returned value reflects the number of
     * owners created for that day prior to this create.
     *
     * @param date the adjusted business-day registration date to count against
     * @return the number of existing owners registered on that date
     */
    private int countOwnersRegisteredOn(LocalDate date) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Counts the existing owners that live in the given city, comparing city names case-insensitively.
     *
     * @param city the city to count owners for
     * @return the number of existing owners in that city
     */
    private int countOwnersInCity(String city) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
    }

    /**
     * Builds the membership number for a new owner, formatted {@code '<customerCode>-M<YY>'} where YY is
     * the last two digits of the fiscal year (which starts on 1 July) of the business-day-adjusted
     * registration date (e.g. {@code 'NSW-1A2B3C4D-M27'}). The fiscal year is derived via
     * {@link OwnerMapper#fiscalYear(LocalDate)}, the single source of truth also used by the DTO's
     * {@code fiscalYear} field.
     *
     * @param customerCode     the owner's assigned customer code
     * @param registrationDate the owner's business-day-adjusted registration date
     * @return the assigned membership number
     */
    private String membershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", OwnerMapper.fiscalYear(registrationDate) % 100);
        return String.format("%s-M%s", customerCode, yy);
    }

    /**
     * Counts the existing owners that already share the given first name and last name, comparing
     * both case-insensitively. Called before a new owner is saved, so the returned value reflects
     * the number of namesakes that existed prior to this create.
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
     * Counts the existing owners that already belong to the given household (i.e. that share the
     * given {@code householdId}). Called before a new owner is saved, so adding one for the owner
     * being created yields the household's total size at the moment of creation. Owners without a
     * household id are ignored.
     *
     * @param householdId the new owner's assigned household id
     * @return the number of existing owners already in that household
     */
    private int countHouseholdMembers(String householdId) {
        if (householdId == null) {
            return 0;
        }
        int count = 0;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes the membership-level ceiling for a new owner joining the given household: one above the
     * current maximum effective membership level among the existing owners in that household. The
     * ceiling is frozen on the owner at creation and later applied by
     * {@link OwnerMapper#toMembershipLevel(Owner)}. Returns {@code null} when the household has no
     * existing member (or the owner has no household id), meaning no cap applies.
     *
     * @param householdId the new owner's assigned household id
     * @return one above the household's current maximum membership level, or {@code null} when the
     *         household has no existing member
     */
    private Integer householdMembershipLevelCap(String householdId) {
        if (householdId == null) {
            return null;
        }
        Integer maxLevel = null;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId())) {
                int level = ownerMapper.toMembershipLevel(existing);
                if (maxLevel == null || level > maxLevel) {
                    maxLevel = level;
                }
            }
        }
        return maxLevel == null ? null : maxLevel + 1;
    }

    /**
     * Normalizes a telephone number into E.164 form. Spaces, dashes and brackets are stripped. When
     * the value carries a leading {@code '+'} the following country code and digits are kept as-is;
     * otherwise country code {@code +61} is assumed and a single leading {@code '0'} is dropped from
     * the national digits. The result must be a {@code '+'} followed by 8 to 15 digits. This E.164
     * string is what gets stored and returned.
     *
     * <p>For example {@code "0412 345 678"} is normalized to {@code "+61412345678"}.
     *
     * <p>Once in E.164 form the national-number length is validated against the country code: a
     * {@code +61} (Australia) number must carry exactly 9 national digits and a {@code +1} (NANP)
     * number exactly 10 (see {@link E164PhoneNumbers#NATIONAL_LENGTHS}). Country codes without a configured
     * rule are accepted on the general 8-to-15 digit length alone.
     *
     * @param telephone the raw telephone value supplied by the client
     * @return the normalized E.164 telephone number
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if the value cannot form a
     *                                 valid E.164 number (unexpected characters, not 8 to 15 digits
     *                                 after the {@code '+'}, or the wrong national-number length for
     *                                 its country code)
     */
    private static String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "telephone could not be normalized to a valid E.164 number");
        }
        validateNationalNumberLength(digits);
        return "+" + digits;
    }

    /**
     * Validates the national-number length of an E.164 number (its digits, without the leading
     * {@code '+'}) against its country code. The longest dialling code that prefixes the number (see
     * {@link E164PhoneNumbers#countryCode}) selects the rule; if none is configured the number passes
     * (only the general 8-to-15 digit rule applies).
     *
     * @param digits the E.164 digits without the leading {@code '+'}
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if the number of national
     *                                 digits does not match the country code's required length
     */
    private static void validateNationalNumberLength(String digits) {
        E164PhoneNumbers.countryCode(digits)
            .ifPresent(code -> {
                int nationalLength = digits.length() - code.length();
                int required = E164PhoneNumbers.NATIONAL_LENGTHS.get(code);
                if (nationalLength != required) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "telephone must have " + required
                            + " national digits for country code +" + code);
                }
            });
    }

    /**
     * Basic syntactic email validation: a non-empty local part, an {@code @}, and a domain
     * containing at least one dot, none of the three sections containing whitespace or an
     * additional {@code @}.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Disposable email domains that are not accepted for an owner. An email whose domain is on this
     * blocklist is rejected with {@link HttpStatus#BAD_REQUEST}. Entries are lower-case and matched
     * case-insensitively against the domain (the part after the {@code @}).
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Validates and normalizes an optional owner email address. A {@code null} value is left as is
     * (email is optional). When a value is supplied it must be a syntactically valid address whose
     * domain is not on the disposable-domain blocklist; the value is then stored and returned
     * lower-cased.
     *
     * @param email the raw email value supplied by the client, possibly {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if a value is present but
     *                                 is not a syntactically valid address, or its domain is on the
     *                                 disposable-domain blocklist
     */
    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "email must be a syntactically valid address");
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "email domain " + domain + " is not allowed");
        }
        return normalized;
    }

    /**
     * Fixed region-to-inclusive-postcode-range table. The postcode of an owner whose city maps to a
     * known region (via {@link OwnerMapper#CITY_REGION}) must fall within its region's range: NSW
     * 2000-2099, VIC 3000-3099, QLD 4000-4099. Each entry is {@code {low, high}} inclusive.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's optional postcode against the city's region. Postcode is validated only
     * when present ({@code null} is accepted, keeping the request contract backward-compatible). The
     * four-digit format is enforced by request validation; here the value is range-checked: when the
     * city maps to a known region (via {@link OwnerMapper#CITY_REGION}) the postcode must fall within
     * that region's inclusive range (see {@link #REGION_POSTCODE_RANGES}). A city with no known region
     * accepts any four-digit postcode.
     *
     * @param city     the owner's city
     * @param postcode the owner's postcode, possibly {@code null}
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if a supplied postcode is
     *                                 out of range for the city's region
     */
    private static void validatePostcode(String city, String postcode) {
        if (postcode == null) {
            return;
        }
        String region = OwnerMapper.CITY_REGION.get(city);
        int[] range = region == null ? null : REGION_POSTCODE_RANGES.get(region);
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "postcode " + postcode + " is not valid for region " + region);
        }
    }

    /**
     * Common street-type abbreviations expanded to their full word during address normalization.
     * Keys and values are upper-case; matching is done per whitespace-delimited token.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Applies an owner's address, accepting either the structured form (a non-blank
     * {@code addressLine1} with an optional {@code addressLine2}) or the flat {@code address} form,
     * preferring the structured fields when present. Whichever fields are supplied are normalized
     * (see {@link #normalizeAddress(String)}), and the stored flat {@code address} is set to the
     * composed value: the normalized {@code addressLine1}, with a single space and the normalized
     * {@code addressLine2} appended when an {@code addressLine2} was supplied. When only the flat
     * form is supplied, the normalized flat address is stored and both structured lines are cleared.
     * This keeps the request contract backward-compatible: an owner that supplied just the flat
     * {@code address} (plus city) is still accepted.
     *
     * @param owner the owner being created or updated, carrying the raw address fields
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if neither a non-blank
     *                                 {@code addressLine1} nor a non-blank flat {@code address} was
     *                                 supplied
     */
    private static void applyAddress(Owner owner) {
        boolean hasStructured = owner.getAddressLine1() != null && !owner.getAddressLine1().isBlank();
        boolean hasFlat = owner.getAddress() != null && !owner.getAddress().isBlank();
        if (!hasStructured && !hasFlat) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "an address must be supplied, either 'addressLine1' or 'address'");
        }
        if (hasStructured) {
            String line1 = normalizeAddress(owner.getAddressLine1());
            owner.setAddressLine1(line1);
            String composed = line1;
            if (owner.getAddressLine2() != null && !owner.getAddressLine2().isBlank()) {
                String line2 = normalizeAddress(owner.getAddressLine2());
                owner.setAddressLine2(line2);
                composed = line1 + " " + line2;
            } else {
                owner.setAddressLine2(null);
            }
            owner.setAddress(composed);
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(normalizeAddress(owner.getAddress()));
        }
    }

    /**
     * Normalizes an owner address for storage. Leading/trailing whitespace is trimmed and internal
     * runs of whitespace are collapsed to a single space, the value is upper-cased, and common
     * street-type abbreviations are expanded per token ({@code ST -> STREET}, {@code RD -> ROAD},
     * {@code AVE -> AVENUE}). This normalized value is what gets stored and returned.
     *
     * <p>For example {@code "  12  main  st "} is normalized to {@code "12 MAIN STREET"}.
     *
     * @param address the raw address supplied by the client, possibly {@code null}
     * @return the normalized address
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if the address is blank
     *                                 after normalization (required field)
     */
    private static String normalizeAddress(String address) {
        String collapsed = address == null ? "" : address.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "address must not be blank");
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
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
     * Determines whether the given whole identity key already belongs to any owner other than the
     * one identified by {@code excludeId} (pass {@code null} to consider every owner, e.g. on
     * create). The key is derived in one place — {@link OwnerMapper#toIdentityKey(Owner)}, the same
     * method that builds the {@code identityKey} returned on the DTO — so the value governing the
     * 409 is exactly the value clients see. Only an exact full-key match counts as a duplicate.
     *
     * @param identityKey the identity key to look for
     * @param excludeId   id of an owner to ignore during the check, or {@code null} for none
     * @return {@code true} if another owner already has the same identity key
     */
    private boolean isIdentityKeyInUse(String identityKey, Integer excludeId) {
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue;
            }
            if (identityKey.equals(ownerMapper.toIdentityKey(existing))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the existing owner that the given (not-a-hard-duplicate) new owner is a possible
     * (soft) duplicate of: one whose identity key <em>differs</em> from the new owner's while sharing
     * the same last-name Soundex code (see {@link IdentityKeyUtils#soundex(String)}) and postcode. A
     * hard duplicate (identical identity key) has already been rejected with a 409 before this runs, so
     * this flags only the softer same-household-name overlap — in particular two owners with the same
     * last name and postcode but different telephones (whose identity keys therefore differ) are a soft
     * match rather than a rejected duplicate. The match with the lowest id is returned so the result is
     * deterministic when several owners qualify.
     *
     * @param owner the new owner being created, with its telephone already normalized
     * @return the matching existing owner, or {@code null} when none qualifies
     */
    private Owner findPossibleDuplicate(Owner owner) {
        if (owner.getLastName() == null || owner.getPostcode() == null) {
            return null;
        }
        String identityKey = ownerMapper.toIdentityKey(owner);
        String soundex = IdentityKeyUtils.soundex(owner.getLastName());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> !identityKey.equals(ownerMapper.toIdentityKey(existing)))
            .filter(existing -> soundex.equals(IdentityKeyUtils.soundex(existing.getLastName())))
            .filter(existing -> owner.getPostcode().equals(existing.getPostcode()))
            .min(Comparator.comparingInt(Owner::getId))
            .orElse(null);
    }

}
