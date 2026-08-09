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
import java.util.List;
import java.util.Locale;
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
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityCapacityExceededException;
import org.springframework.samples.petclinic.rest.advice.OwnerDailyLimitExceededException;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.LocalityLookup;
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
     * Syntactic check for an email address: a non-empty local part, an '@', a domain with at least
     * one dot and a two-or-more letter top-level label. Case-insensitive; the accepted value is
     * stored lower-cased.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** A well-formed postcode: exactly four digits. */
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("^[0-9]{4}$");

    /**
     * Email domains belonging to disposable-address providers. An owner whose email domain (the part
     * after the '@', compared case-insensitively) is one of these is rejected: such addresses are
     * throwaway and unsuitable for a durable owner record.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Region -&gt; inclusive 4-digit postcode range {@code {low, high}}. A postcode supplied for an
     * owner whose city maps to one of these regions (see {@link LocalityLookup}) must fall within the
     * region's range; a city with no known region ({@code "UNKNOWN"}) accepts any 4-digit postcode.
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODE_RANGES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Dedicated audit logger; one line is emitted per successful owner create. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * The maximum number of owners a single city may contain. Once a city already holds this many
     * owners, any further create naming that city is rejected as a conflict.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * The maximum number of owners that may be registered in a single day. Once this many owners
     * already carry today's registration date, any further create is rejected as too many requests.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * The number of owners that must already be registered on today's business day before a response
     * carries a bulk-signup warning. Once more than this many owners already carry today's
     * registration date, responses set {@code bulkSignupWarning} true.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * The fixed list of public holidays. A registration date that lands on one of these dates is
     * rolled forward to the next non-holiday business day, just as a weekend date is.
     */
    private static final java.util.Set<LocalDate> PUBLIC_HOLIDAYS = java.util.Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

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
        owners.forEach(owner -> owner.setHouseholdMemberCount(countHouseholdMembers(owner)));
        return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OwnerDto ownerDto = toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarningActive());
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Owner alreadyCreated = findOwnerByIdempotencyKey(idempotencyKey);
            if (alreadyCreated != null) {
                OwnerDto existingDto = toOwnerDto(alreadyCreated);
                existingDto.setBulkSignupWarning(isBulkSignupWarningActive());
                return new ResponseEntity<>(existingDto, HttpStatus.OK);
            }
        }
        validateRequiredOwnerFields(ownerFieldsDto);
        rejectFutureRegistrationDate(ownerFieldsDto.getRegistrationDate());
        LocalDate suppliedOrDefaultDate =
            ownerFieldsDto.getRegistrationDate() != null ? ownerFieldsDto.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(suppliedOrDefaultDate);
        rejectDailyLimitExceeded(registrationDate);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        validatePostcode(owner.getCity(), owner.getPostcode());
        applyAddress(owner);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        boolean declaredHouseholdMember = applyHousehold(owner, ownerFieldsDto.getSharesHousehold());
        owner.setEmail(normalizeEmail(owner.getEmail()));
        rejectDuplicateIdentity(owner);
        applyPossibleDuplicate(owner, declaredHouseholdMember);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(
            generateCustomerCode(owner.getPostcode(), owner.getCity(), normalizedTelephone, owner.getLastName()));
        owner.setMembershipNumber(generateMembershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setIdempotencyKey(idempotencyKey);
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(),
            owner.getMembershipNumber());
        OwnerDto ownerDto = toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarningActive());
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * The idempotency key supplied on the current create request, read from its {@code Idempotency-Key}
     * header. A blank value is treated as absent.
     *
     * @return the trimmed {@code Idempotency-Key} header, or {@code null} when it is absent or blank
     */
    private String currentIdempotencyKey() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String header = attributes.getRequest().getHeader("Idempotency-Key");
            if (header != null && !header.isBlank()) {
                return header.trim();
            }
        }
        return null;
    }

    /**
     * Finds the owner originally created under the given idempotency key, so a repeated create carrying
     * an already-seen key returns that owner instead of creating a duplicate. The lowest-id match is
     * returned when more than one owner somehow carries the key.
     *
     * @param idempotencyKey the create request's idempotency key
     * @return the owner first created under the key, or {@code null} when the key has not been seen
     */
    private Owner findOwnerByIdempotencyKey(String idempotencyKey) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> idempotencyKey.equals(existing.getIdempotencyKey()))
            .min(java.util.Comparator.comparing(Owner::getId))
            .orElse(null);
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
        this.clinicService.saveOwner(currentOwner);
        return new ResponseEntity<>(toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
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
     * Rejects an owner whose mandatory fields are missing (null) or blank. The name of every
     * offending field is collected so the caller learns exactly which values must be supplied.
     *
     * @param ownerFieldsDto the submitted owner payload
     * @throws MissingOwnerFieldsException if any of firstName, lastName, address, city or
     *         telephone is missing or blank
     */
    private void validateRequiredOwnerFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (isBlank(ownerFieldsDto.getAddressLine1())
            && isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
            missingFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Rejects an owner create whose supplied {@code registrationDate} is later than the server's
     * current date. A registration date may not lie in the future; a value equal to today or in the
     * past is accepted, and an absent date is left to default to the server date.
     *
     * @param registrationDate the supplied registration date, or {@code null} when none was supplied
     * @throws InvalidOwnerFieldsException if the supplied date is after the server's current date
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidOwnerFieldsException(List.of("registrationDate"));
        }
    }

    /**
     * Counts the existing owners who, before this create, already share the given first and last name.
     * Both names are compared case-insensitively (after trimming surrounding whitespace and collapsing
     * internal runs of whitespace, mirroring the other name comparisons), so cosmetic differences in
     * case or spacing still count as the same name.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners sharing both names (zero when none)
     */
    private int countNamesakes(String firstName, String lastName) {
        String first = normalizeHouseholdField(firstName);
        String last = normalizeHouseholdField(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHouseholdField(existing.getFirstName()).equals(first)
                && normalizeHouseholdField(existing.getLastName()).equals(last))
            .count();
    }

    /**
     * Rejects an owner create whose city already holds the maximum number of owners. Owners are
     * grouped into a city case-insensitively after surrounding whitespace is trimmed and internal
     * runs are collapsed (mirroring the other city comparisons), so cosmetic differences in case or
     * spacing count as the same city. When the city already contains {@link #MAX_OWNERS_PER_CITY} or
     * more owners the create is rejected as a conflict.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityCapacityExceededException if the city already contains the maximum number of owners
     */
    /**
     * Rejects an owner create once the maximum number of owners have already been registered on the
     * business day this owner would land on. Owners are grouped by their {@code registrationDate},
     * which is always a business day (a weekend effective date is rolled forward to the following
     * Monday); when {@link #MAX_OWNERS_PER_DAY} or more existing owners already carry the given
     * business day the create is rejected as too many requests. Because a weekend effective date is
     * rolled forward, this quota bounds the number of owners that may be registered per business day.
     *
     * @param businessDay the adjusted business day the owner being created would be registered on
     * @throws OwnerDailyLimitExceededException if the business day already holds the maximum number of owners
     */
    private void rejectDailyLimitExceeded(LocalDate businessDay) {
        long ownersOnDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
            .count();
        if (ownersOnDay >= MAX_OWNERS_PER_DAY) {
            throw new OwnerDailyLimitExceededException(businessDay, MAX_OWNERS_PER_DAY);
        }
    }

    /**
     * Rolls an effective registration date forward onto a business day. A Saturday or Sunday is moved
     * forward, and a date that lands on a listed public holiday is likewise rolled forward, until the
     * result is a weekday that is not a public holiday; a weekday that is not a holiday is returned
     * unchanged. This applies whether the date was supplied on the request or defaulted to the
     * server's current date.
     *
     * @param date the effective registration date (supplied or defaulted)
     * @return the first non-holiday weekday on or after the supplied date
     */
    /**
     * Reports whether responses should carry a bulk-signup warning. The warning is active once more
     * than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry today's business-day
     * registration date (the same accumulation basis as the per-day create limit), signalling that the
     * daily create volume is approaching {@link #MAX_OWNERS_PER_DAY}.
     *
     * @return {@code true} when more than the threshold number of owners are already registered on
     *         today's business day, otherwise {@code false}
     */
    private boolean isBulkSignupWarningActive() {
        LocalDate today = toBusinessDay(LocalDate.now());
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        return ownersToday > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    private LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
        while (isWeekend(adjusted) || PUBLIC_HOLIDAYS.contains(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    private void rejectCityAtCapacity(String city) {
        String normalizedCity = normalizeHouseholdField(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHouseholdField(existing.getCity()).equals(normalizedCity))
            .count();
        if (ownersInCity >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityCapacityExceededException(city, MAX_OWNERS_PER_CITY);
        }
    }

    /**
     * Builds an owner's customer code, formatted '&lt;REGION&gt;-&lt;HASH8&gt;'. REGION is the region
     * code derived from the owner's postcode (falling back to their city; see {@link LocalityLookup}),
     * and HASH8 is the first 8 upper-case hex characters of the SHA-256 digest over the owner's
     * normalized telephone concatenated with their last name (e.g. 'NSW-1A2B3C4D'). Two owners resolve
     * to the same base code only when they share a region and produce the same telephone-and-last-name
     * hash; when the computed code collides with an existing owner's customer code it is de-duplicated
     * by appending '-&lt;n&gt;' with the smallest {@code n} of 2 or more that makes it unique.
     *
     * @param postcode the owner's postcode, used first to derive the region
     * @param city the owner's city, used to derive the region when the postcode maps to none
     * @param normalizedTelephone the owner's normalized (E.164) telephone
     * @param lastName the owner's last name
     * @return the formatted, de-duplicated customer code
     */
    private String generateCustomerCode(String postcode, String city, String normalizedTelephone, String lastName) {
        String region = LocalityLookup.forPostcodeAndCity(postcode, city);
        String hash8 = sha256UpperHex(normalizedTelephone + lastName, 8);
        String baseCode = region + "-" + hash8;
        return deduplicateCustomerCode(baseCode);
    }

    /**
     * De-duplicates a computed customer code against every existing owner's customer code. When no
     * existing owner already carries the base code it is returned unchanged; otherwise '-&lt;n&gt;' is
     * appended with the smallest {@code n} of 2 or more that yields a code no existing owner holds.
     *
     * @param baseCode the computed, formatted customer code before de-duplication
     * @return the base code when unique, otherwise the base code suffixed with '-&lt;n&gt;'
     */
    private String deduplicateCustomerCode(String baseCode) {
        java.util.Set<String> existingCodes = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        if (!existingCodes.contains(baseCode)) {
            return baseCode;
        }
        int n = 2;
        while (existingCodes.contains(baseCode + "-" + n)) {
            n++;
        }
        return baseCode + "-" + n;
    }

    /**
     * Builds an owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where
     * customerCode is the owner's customer code and YY is the last two digits of the fiscal year (the
     * fiscal year starts on 1 July) that the business-day-adjusted registrationDate falls in
     * (e.g. 'NSW-1A2B3C4D-M27').
     *
     * @param customerCode the owner's customer code
     * @param registrationDate the owner's business-day-adjusted registration date
     * @return the formatted membership number
     */
    private String generateMembershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", Owner.fiscalYear(registrationDate) % 100);
        return customerCode + "-M" + yy;
    }

    /**
     * The exact number of national (subscriber) digits an E.164 telephone must carry for a given
     * country calling code. Australia ('+61') uses 9 national digits and the North American
     * Numbering Plan ('+1') uses 10. Country codes not listed here are only bound by the generic
     * E.164 length (8 to 15 digits overall).
     */
    private static final java.util.Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        java.util.Map.of("61", 9, "1", 10);

    /**
     * Normalizes a telephone number supplied on create into E.164 form. Spaces, dashes and brackets
     * are stripped. When the value carries an explicit leading '+' its country code is kept as given;
     * otherwise country code '+61' is assumed and a single leading '0' is dropped from the national
     * digits. The result must be a '+' followed by 8 to 15 digits, and its national number must have
     * the exact length its country code requires ('+61' needs 9 national digits, '+1' needs 10). This
     * E.164 string is what gets stored and returned.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized E.164 telephone (a '+' followed by 8 to 15 digits)
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number or its
     *         national number has the wrong length for its country code
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.trim().replaceAll("[\\s()\\-]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = cleaned;
        } else {
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            e164 = "+61" + national;
        }
        if (!e164.matches("^\\+[0-9]{8,15}$")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        validateNationalNumberLength(e164);
        return e164;
    }

    /**
     * Rejects an E.164 telephone whose national number does not have the exact length its country
     * calling code requires. The digits after the leading '+' are split into the longest matching
     * country code from {@link #NATIONAL_NUMBER_LENGTHS} and the remaining national number; when the
     * code is known and the national number's length differs from the required count the telephone is
     * rejected. Numbers whose country code is not listed are left to the generic E.164 length rule.
     *
     * @param e164 the normalized E.164 telephone (a '+' followed by 8 to 15 digits)
     * @throws InvalidOwnerFieldsException if the national number has the wrong length for its country code
     */
    private void validateNationalNumberLength(String e164) {
        String digits = e164.substring(1);
        for (int codeLength = Math.min(3, digits.length()); codeLength >= 1; codeLength--) {
            String countryCode = digits.substring(0, codeLength);
            Integer requiredNationalLength = NATIONAL_NUMBER_LENGTHS.get(countryCode);
            if (requiredNationalLength != null) {
                int nationalLength = digits.length() - codeLength;
                if (nationalLength != requiredNationalLength) {
                    throw new InvalidOwnerFieldsException(List.of("telephone"));
                }
                return;
            }
        }
    }

    /**
     * Normalizes an optional email address supplied on create. An absent email (null or blank) is
     * left unset. When a value is present it must be a syntactically valid address whose domain is not
     * on the disposable-domain blocklist ({@link #DISPOSABLE_EMAIL_DOMAINS}); the normalized form
     * stored and returned is the value lower-cased.
     *
     * @param email the raw email value from the request, or {@code null} when none was supplied
     * @return the lower-cased email, or {@code null} when no email was supplied
     * @throws InvalidOwnerFieldsException if a non-blank value is not a syntactically valid address or
     *         its domain is on the disposable-domain blocklist
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return normalized;
    }

    /**
     * Validates an optional postcode supplied on create. A postcode is validated only when present:
     * an absent (null or blank) postcode is accepted, keeping the create request backward-compatible.
     * When present it must be exactly four digits and, when the owner's city maps to a known region
     * (see {@link LocalityLookup}), must fall within that region's inclusive range
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). A city with no known
     * region accepts any four-digit postcode.
     *
     * @param city the owner's city, used to derive the region whose range the postcode must satisfy
     * @param postcode the raw postcode value from the request, or {@code null} when none was supplied
     * @throws InvalidOwnerFieldsException if a supplied postcode is not four digits or is out of range
     *         for the city's region
     */
    private void validatePostcode(String city, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!POSTCODE_PATTERN.matcher(postcode).matches()) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
        int[] range = REGION_POSTCODE_RANGES.get(LocalityLookup.forCity(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }

    /**
     * Resolves an owner's address from the supplied structured and flat fields, preferring the
     * structured form. Whichever address fields were supplied are normalized (see
     * {@link #normalizeAddress(String)}). When a non-blank {@code addressLine1} is supplied the
     * structured form wins: {@code addressLine1} and {@code addressLine2} are stored normalized (an
     * absent or blank {@code addressLine2} is cleared) and the composed {@code address} is the
     * normalized {@code addressLine1}, with a single space and the normalized {@code addressLine2}
     * appended when the latter is present. When no {@code addressLine1} is supplied the flat
     * {@code address} is used unchanged for backward compatibility and the structured lines are
     * cleared. The composed {@code address} is what every later step reads.
     *
     * @param owner the owner being created, mapped from the request but not yet normalized
     */
    private void applyAddress(Owner owner) {
        String line1 = normalizeAddress(owner.getAddressLine1());
        String line2 = normalizeAddress(owner.getAddressLine2());
        String flat = normalizeAddress(owner.getAddress());
        if (!line1.isEmpty()) {
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2.isEmpty() ? null : line2);
            owner.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(flat);
        }
    }

    /**
     * Normalizes an address supplied on create. Surrounding whitespace is trimmed, each internal run
     * of whitespace is collapsed to a single space, the value is upper-cased and common abbreviations
     * are expanded to their full words (ST -&gt; STREET, RD -&gt; ROAD, AVE -&gt; AVENUE). This
     * normalized form is what gets stored, returned and used for every address comparison (the
     * required-field check, household duplicate detection and the shared household id).
     *
     * @param address the raw address value from the request, or {@code null}
     * @return the normalized address (empty string when {@code address} is {@code null} or blank)
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
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "ST" -> tokens[i] = "STREET";
                case "RD" -> tokens[i] = "ROAD";
                case "AVE" -> tokens[i] = "AVENUE";
                default -> { }
            }
        }
        return String.join(" ", tokens);
    }

    /**
     * Rejects an owner create whose {@code identityKey} exactly matches an existing owner's. The
     * identity key - the owner's normalized telephone, email (empty when none) and {@code householdId}
     * (empty when none) joined by {@code '|'} - is the single derived value that subsumes the former
     * separate telephone, email and household duplicate checks. Only a whole-key match is a conflict,
     * so two members of one household with different telephones have different keys and are both
     * allowed; only an owner whose entire key is identical is a duplicate.
     *
     * @param owner the owner being created, with its telephone, email and householdId already resolved
     * @throws DuplicateOwnerIdentityException if another owner already has this exact identity key
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean inUse = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (inUse) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * Flags an owner being created as a possible (soft) duplicate. A soft match is an existing owner
     * that shares this owner's last name (compared case-insensitively with collapsed whitespace) and
     * postcode but carries a different (normalized) telephone; such an owner is not a hard duplicate
     * (its identity key differs) and is still created. When at least one soft match exists,
     * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the lowest-id match;
     * otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is left unset.
     * <p>
     * A declared household member - an owner that acknowledged sharing an existing household via
     * {@code sharesHousehold} - is never flagged: having deliberately joined the household it is not a
     * suspected duplicate, so {@code possibleDuplicate} is set false without inspecting other owners.
     *
     * @param owner the owner being created, with its last name, postcode and normalized telephone resolved
     * @param declaredHouseholdMember whether the owner joined an existing household by acknowledging it
     */
    private void applyPossibleDuplicate(Owner owner, boolean declaredHouseholdMember) {
        if (declaredHouseholdMember) {
            owner.setPossibleDuplicate(false);
            return;
        }
        String lastName = normalizeHouseholdField(owner.getLastName());
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Owner match = null;
        if (postcode != null && !postcode.isBlank()) {
            match = this.clinicService.findAllOwners().stream()
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> normalizeHouseholdField(existing.getLastName()).equals(lastName))
                .filter(existing -> postcode.equals(existing.getPostcode()))
                .filter(existing -> !java.util.Objects.equals(telephone, existing.getTelephone()))
                .min(java.util.Comparator.comparing(Owner::getId))
                .orElse(null);
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        } else {
            owner.setPossibleDuplicate(false);
        }
    }

    /**
     * Maps an owner to its DTO, first populating the owner's {@link Owner#getHouseholdMemberCount()
     * household member count} for the response.
     *
     * @param owner the owner to map
     * @return the mapped owner DTO
     */
    private OwnerDto toOwnerDto(Owner owner) {
        owner.setHouseholdMemberCount(countHouseholdMembers(owner));
        return ownerMapper.toOwnerDto(owner);
    }

    /**
     * Counts the members of the given owner's household - the owners that share this owner's
     * {@code householdId}, including the owner itself. An owner with no household ({@code householdId}
     * is {@code null}) is its own sole member, so the count is one.
     *
     * @param owner the owner whose household is being sized
     * @return the number of owners sharing this owner's household (one when it has no household)
     */
    private int countHouseholdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * Applies the household rule to an owner being created. A household is the set of owners sharing a
     * last name and a postcode: the {@code householdId} is derived deterministically from the
     * normalized last name and the postcode (see {@link #generateHouseholdId(String, String)}), so two
     * owners with the same last name and postcode always resolve to the same identifier and belong to
     * the same household automatically - no acknowledgement is needed to create the link.
     * <p>
     * When the owner supplies a postcode its {@code householdId} is always assigned. If that household
     * already has at least one member, the create is a household duplicate: it is rejected with a 409
     * unless {@code sharesHousehold} is true, in which case the owner is created as a declared member
     * of that household. An owner with no postcode belongs to no household and is left without an
     * identifier.
     *
     * @param owner the owner being created
     * @param sharesHousehold the request's shared-household acknowledgement, or {@code null} when absent
     * @return {@code true} when the owner joined an existing household as a declared member, otherwise
     *         {@code false}
     * @throws DuplicateOwnerHouseholdException when the household already exists and the create did not
     *         acknowledge it via {@code sharesHousehold}
     */
    private boolean applyHousehold(Owner owner, Boolean sharesHousehold) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return false;
        }
        String lastName = normalizeHouseholdField(owner.getLastName());
        String householdId = generateHouseholdId(lastName, postcode);
        owner.setHouseholdId(householdId);
        boolean hasExistingMember = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (!hasExistingMember) {
            return false;
        }
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            throw new DuplicateOwnerHouseholdException(owner.getLastName(), postcode);
        }
        return true;
    }

    /**
     * Builds the stable identifier shared by the members of one household. It is derived purely from
     * the normalized last name and the postcode, so every owner sharing a last name and postcode
     * resolves to the same value - the first 12 upper-case hex characters of the SHA-256 digest of the
     * two fields joined by a single '|'.
     *
     * @param normalizedLastName the household's last name, already normalized for comparison
     * @param postcode the household's postcode
     * @return the shared household identifier
     */
    private String generateHouseholdId(String normalizedLastName, String postcode) {
        String key = normalizedLastName + '|' + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02X", b));
                if (hex.length() >= 12) {
                    break;
                }
            }
            return hex.substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    /**
     * Returns the first {@code length} upper-case hex characters of the SHA-256 digest of the given
     * input's UTF-8 bytes. Used to derive the customer code's stable, deterministic hash component from
     * the owner's normalized telephone and last name.
     *
     * @param input the string to digest
     * @param length the number of leading upper-case hex characters to return
     * @return the first {@code length} upper-case hex characters of the SHA-256 digest
     */
    private String sha256UpperHex(String input, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02X", b));
                if (hex.length() >= length) {
                    break;
                }
            }
            return hex.substring(0, length);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    /**
     * Normalizes a last name for household comparison: surrounding whitespace is trimmed, each
     * internal run of whitespace is collapsed to a single space and the result is lower-cased.
     * Addresses use {@link #normalizeAddress(String)} instead, which additionally upper-cases and
     * expands common abbreviations.
     *
     * @param value the raw last name, or {@code null}
     * @return the normalized comparison key (empty string when {@code value} is {@code null})
     */
    private String normalizeHouseholdField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
