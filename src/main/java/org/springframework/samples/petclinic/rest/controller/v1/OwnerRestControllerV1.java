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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerLocality;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.controller.CityOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.controller.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.controller.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.controller.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.controller.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.controller.HouseholdDuplicateException;
import org.springframework.samples.petclinic.rest.controller.InvalidEmailException;
import org.springframework.samples.petclinic.rest.controller.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.controller.InvalidTelephoneException;
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
     * Pragmatic syntactic check for an email address: a non-empty local part, a single {@code @}, and a
     * domain with at least one dot and a two-or-more-letter final label. Whitespace is not permitted.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@.]{2,}$");

    /**
     * Domains that supply disposable / throwaway mailboxes. An owner whose email domain matches one of
     * these (compared case-insensitively) is rejected. Values are stored lower-cased for comparison.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Dedicated audit logger. Successful side-effecting operations (such as creating an owner) emit a
     * line here so audit trails can be captured independently of application logging.
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
        rejectMissingOrBlankFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizeAddress(owner.getAddress()));
        owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        owner.setEmail(normalizeEmail(owner.getEmail()));
        validatePostcode(owner.getPostcode(), owner.getCity());
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setRegistrationDate(rollToBusinessDay(owner.getRegistrationDate()));
        rejectDailyLimitReached(owner.getRegistrationDate());
        rejectCityAtCapacity(owner.getCity());
        owner.setHouseholdId(computeHouseholdId(owner));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        rejectDuplicateIdentity(owner);
        if (!sharesHousehold) {
            rejectHouseholdDuplicate(owner);
        }
        assignPossibleDuplicate(owner, sharesHousehold);
        owner.setCustomerCode(generateCustomerCode(owner));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(countHouseholdSize(owner.getHouseholdId()));
        owner.setBulkSignupWarning(isBulkSignupDay(owner.getRegistrationDate()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), ownerDto.getMembershipLevel());
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
     * Rejects an owner payload that is missing or blank in any mandatory field. This complements Bean
     * Validation, which cannot reject whitespace-only values that still satisfy a minimum-length constraint.
     *
     * @param fields the submitted owner fields
     * @throws RequiredFieldsMissingException if any of firstName, lastName, city or telephone is
     *                                        {@code null} or blank, or if address is blank after
     *                                        normalization
     */
    private void rejectMissingOrBlankFields(OwnerFieldsDto fields) {
        List<String> missing = new ArrayList<>();
        if (isBlank(fields.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(fields.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(normalizeAddress(fields.getAddress()))) {
            missing.add("address");
        }
        if (isBlank(fields.getCity())) {
            missing.add("city");
        }
        if (isBlank(fields.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Inclusive 4-digit postcode ranges permitted per city region, keyed by the region derived from the
     * city via {@link OwnerLocality}: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A region absent from
     * this table (i.e. {@code "UNKNOWN"}) imposes no range constraint, so any 4-digit postcode is accepted.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's {@code postcode} when present. Postcode is optional: a {@code null} value is
     * accepted and left absent, keeping the create contract backward-compatible. When supplied it must be
     * a 4-digit value, and it must fall within the inclusive range for the city's region (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
     *
     * @param postcode the submitted postcode, or {@code null} when omitted
     * @param city the owner's city, used to derive the region whose range constrains the postcode
     * @throws InvalidPostcodeException if a supplied postcode is not 4 digits or is out of range for the
     *                                  city's region
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_POSTCODE_RANGE.get(OwnerLocality.of(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }

    /**
     * Normalizes a submitted telephone number into E.164 form, which becomes the stored and returned
     * value. Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are
     * kept as supplied; otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
     * is dropped from the national digits. The result must be a {@code '+'} followed by 8 to 15 digits.
     * <p>
     * Where a per-country rule is defined the national-number length is validated against the country
     * code: {@code '+61'} (Australia) requires exactly 9 national digits and {@code '+1'} (NANP)
     * requires exactly 10.
     *
     * @param telephone the raw telephone value from the request
     * @return the E.164 telephone (a {@code '+'} followed by 8 to 15 digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            // No country code supplied: assume Australia (+61) and drop a single leading '0'.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(cleaned);
        }
        // Validate the national-number length against the country code where a rule is defined.
        if (digits.startsWith("61") && digits.length() - 2 != 9) {
            throw new InvalidTelephoneException(cleaned);
        }
        if (digits.startsWith("1") && digits.length() - 1 != 10) {
            throw new InvalidTelephoneException(cleaned);
        }
        return "+" + digits;
    }

    /**
     * Normalizes a submitted email address. A {@code null} email is left absent (the field is optional).
     * When present, it must be a syntactically valid address; the stored and returned value is trimmed and
     * lower-cased.
     *
     * @param email the raw email value from the request, or {@code null} if omitted
     * @return the lower-cased email, or {@code null} if none was supplied
     * @throws InvalidEmailException if a non-null email is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
        return normalized;
    }

    /**
     * Normalizes a submitted address into the form that is stored and returned. Leading and trailing
     * whitespace is removed, internal runs of whitespace are collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). Abbreviations are only expanded when they form a whole
     * word, so an address such as {@code '  12  main  st '} normalizes to {@code '12 MAIN STREET'}.
     *
     * @param address the raw address value from the request (may be {@code null})
     * @return the normalized address, or an empty string when {@code address} is {@code null} or blank
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expandAddressAbbreviation(tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Expands a single upper-cased address token from its common abbreviation to its full form, or
     * returns the token unchanged when it is not a recognised abbreviation.
     *
     * @param token an upper-cased whole-word address token
     * @return the expanded form ({@code STREET}, {@code ROAD}, {@code AVENUE}) or the original token
     */
    private String expandAddressAbbreviation(String token) {
        return switch (token) {
            case "ST" -> "STREET";
            case "RD" -> "ROAD";
            case "AVE" -> "AVENUE";
            default -> token;
        };
    }

    /**
     * Builds the customer code assigned to a newly created owner, formatted {@code '<REGION>-<HASH8>'}
     * where {@code REGION} is the region derived from the owner's postcode (falling back to the city when
     * the postcode resolves to no known region), the same derivation that yields the owner's locality, and
     * {@code HASH8} is the first 8 upper-cased hex characters of the SHA-256 digest of the owner's
     * normalized telephone concatenated with the owner's last name (e.g. {@code 'NSW-1A2B3C4D'}).
     * <p>
     * Should the computed code collide with an existing owner's {@code customerCode}, it is de-duplicated
     * by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that yields a value not already
     * held by any existing owner; the de-duplicated code is returned.
     *
     * @param owner the owner being created, with normalized telephone and postcode already set
     * @return the assigned, collision-free customer code
     */
    private String generateCustomerCode(Owner owner) {
        String region = OwnerLocality.of(owner.getCity(), owner.getPostcode());
        String hash8 = sha256Hex(owner.getTelephone() + owner.getLastName())
            .substring(0, 8).toUpperCase(Locale.ROOT);
        String base = region + "-" + hash8;
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(code -> code != null)
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
     * Computes the full lower-case hex SHA-256 digest of the UTF-8 bytes of the given value.
     *
     * @param value the value to hash
     * @return the 64-character lower-case hex SHA-256 digest
     */
    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    /**
     * Counts how many existing owners already share the given first name and last name, compared
     * case-insensitively. The count is taken before the new owner is persisted, so it excludes the
     * owner being created.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners with the same first and last name (case-insensitively)
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Computes the size of the owner's household: the number of existing owners already sharing the given
     * {@code householdId} plus the owner being created itself. The existing count is taken before the new
     * owner is persisted, so adding one for the owner yields the household's total membership including it
     * (e.g. two existing members give a household size of 3 for the third member). Households are keyed on
     * the deterministic {@code householdId} (derived from last name and postcode).
     *
     * @param householdId the computed household identifier of the owner being created
     * @return the household size including the owner being created (at least 1)
     */
    private int countHouseholdSize(String householdId) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> householdId.equals(owner.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * The maximum number of owners permitted in a single city. A create request whose city already
     * holds this many owners is rejected.
     */
    private static final int CITY_OWNER_LIMIT = 50;

    /**
     * Rejects a create request whose city already contains {@link #CITY_OWNER_LIMIT} or more owners.
     * Cities are matched case-insensitively, mirroring the per-city customer-code sequence, so owners
     * differing only in the casing of their city share one capacity pool. The count is taken before the
     * new owner is persisted, so it excludes the owner being created.
     *
     * @param city the city of the owner being created
     * @throws CityOwnerLimitExceededException if the city already holds the maximum number of owners
     */
    private void rejectCityAtCapacity(String city) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> city.equalsIgnoreCase(owner.getCity()))
            .count();
        if (existing >= CITY_OWNER_LIMIT) {
            throw new CityOwnerLimitExceededException(city, CITY_OWNER_LIMIT);
        }
    }

    /**
     * The maximum number of owners permitted to be created in a single day. A create request whose
     * registration date already holds this many owners is rejected.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Rejects a create request when {@link #DAILY_OWNER_LIMIT} or more owners have already been created
     * on the same day, compared by {@code registrationDate}. The count is taken before the new owner is
     * persisted, so it excludes the owner being created.
     *
     * @param registrationDate the registration date of the owner being created
     * @throws DailyOwnerLimitExceededException if the day already holds the maximum number of owners
     */
    /**
     * Adjusts an effective registration date so that it always falls on a business day. When the given
     * date is a Saturday or Sunday it is rolled forward to the following Monday; a weekday is returned
     * unchanged. This applies equally to a date supplied in the request and to a date defaulted to the
     * server date, and the adjusted value is what becomes the owner's {@code registrationDate}, so every
     * value derived from it (the membership number's year segment, the daily create-limit count) uses the
     * business-day-adjusted date.
     *
     * @param date the effective registration date, supplied or defaulted
     * @return the same date when it is a weekday, otherwise the next Monday
     */
    private LocalDate rollToBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /**
     * The number of owners that must already exist for a given day before a further create is flagged as a
     * bulk sign-up surge. When more than this many owners already share the new owner's registration date,
     * the created owner carries {@code bulkSignupWarning = true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether the owner being created lands on a day that already holds a bulk sign-up surge,
     * i.e. more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already share its {@code registrationDate}.
     * The count is taken before the new owner is persisted, so it excludes the owner being created, mirroring
     * the daily create-limit rule's accumulation path.
     *
     * @param registrationDate the registration date of the owner being created
     * @return {@code true} when more than 80 owners were already created on that day, otherwise {@code false}
     */
    private boolean isBulkSignupDay(LocalDate registrationDate) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> registrationDate.equals(owner.getRegistrationDate()))
            .count();
        return existing > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Rejects a create request whose supplied {@code registrationDate} is later than the server date. A
     * registration cannot be dated in the future. The check applies only to a date supplied in the request;
     * a {@code null} value (which is later defaulted to the server date) is accepted, and the comparison is
     * made against the supplied value before any business-day adjustment.
     *
     * @param registrationDate the registration date supplied in the request, or {@code null} when omitted
     * @throws FutureRegistrationDateException if a supplied registration date is later than the server date
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (registrationDate.isAfter(today)) {
            throw new FutureRegistrationDateException(registrationDate, today);
        }
    }

    private void rejectDailyLimitReached(LocalDate registrationDate) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> registrationDate.equals(owner.getRegistrationDate()))
            .count();
        if (existing >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(registrationDate, DAILY_OWNER_LIMIT);
        }
    }

    /**
     * Rejects a create request whose owner collides with an existing owner on the single derived
     * {@code identityKey}. This one key consolidates the former separate telephone, email and household
     * duplicate checks: the create is rejected only when the new owner's WHOLE identity key exactly equals
     * an existing owner's. Because the (normalized) telephone is part of the key, two members of the same
     * household (same {@code householdId}) with different telephones have different keys and are both
     * allowed; only an exact full-key match is a duplicate. The owner's {@code householdId} has already
     * been computed (deterministically from the last name and postcode) before this check runs, so the
     * key compared here is the same one that is later returned.
     *
     * @param owner the owner being created, with normalized fields and its computed {@code householdId} set
     * @throws DuplicateIdentityException if an existing owner has the same identity key
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String key = identityKey(owner);
        boolean taken = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> key.equals(identityKey(existing)));
        if (taken) {
            throw new DuplicateIdentityException(key);
        }
    }

    /**
     * Flags a soft (possible) duplicate on the owner being created. A soft match is an existing owner that,
     * while not a hard {@code identityKey} duplicate (that stronger check has already run), shares this
     * owner's last name (compared case-insensitively) and exact postcode but carries a different normalized
     * telephone. When at least one such owner exists, {@code possibleDuplicate} is set {@code true} and
     * {@code possibleDuplicateOf} to the matching owner's id (the lowest id when several match, for
     * determinism); otherwise {@code possibleDuplicate} is set {@code false} and no match id is recorded. A
     * new owner with no postcode has nothing to match on and is never a possible duplicate.
     * <p>
     * A declared household member ({@code sharesHousehold}) is never flagged: sharing a last name and
     * postcode is now exactly the household key, so such an owner is a known household member rather than
     * a suspected duplicate.
     *
     * @param owner the owner being created, with normalized telephone and validated postcode already set
     * @param sharesHousehold whether the create declared the owner a household member
     */
    private void assignPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        Integer matchId = null;
        if (!sharesHousehold && owner.getPostcode() != null) {
            matchId = this.clinicService.findAllOwners().stream()
                .filter(existing -> owner.getLastName().equalsIgnoreCase(existing.getLastName())
                    && owner.getPostcode().equals(existing.getPostcode())
                    && !telephonesEqual(owner.getTelephone(), existing.getTelephone()))
                .map(Owner::getId)
                .filter(id -> id != null)
                .min(Comparator.naturalOrder())
                .orElse(null);
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private boolean telephonesEqual(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    /**
     * Derives an owner's {@code identityKey}: the normalized telephone, the email (or an empty string when
     * absent) and the household id (or an empty string when absent), joined by {@code '|'} in that order
     * (e.g. {@code '+61412345678||a1b2c3d4e5f6a7b8'} for an owner with no email but a shared household).
     * Telephone and email are stored already normalized, so the stored values are used directly.
     *
     * @param owner the owner whose identity key is being derived
     * @return the owner's identity key
     */
    private String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Computes the owner's deterministic {@code householdId}: the first 12 hex characters of the SHA-256
     * digest of {@code '<normalizedLastName>|<postcode>'}, where the last name is normalized with
     * {@link #normalizeForComparison} and an absent postcode contributes an empty string. Because the
     * identifier is a pure function of last name and postcode, any two owners sharing those values share
     * the household automatically, with no cross-owner mutation. The value is assigned to every owner,
     * whether or not {@code sharesHousehold} was requested.
     *
     * @param owner the owner being created, with its validated postcode already set
     * @return the deterministic household identifier
     */
    private String computeHouseholdId(Owner owner) {
        String normalizedLastName = normalizeForComparison(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        return sha256Hex(normalizedLastName + "|" + postcode).substring(0, 12);
    }

    /**
     * Rejects a create request whose owner would join an existing household without declaring it. The
     * household is keyed on the computed {@code householdId} (derived from last name and postcode), so any
     * existing owner sharing that identifier is already a member of the same household; a further owner is a
     * household duplicate. The check is bypassed when the request opted in via {@code sharesHousehold}, in
     * which case the owner is created as a declared household member. The (stronger) exact identity-key
     * check has already run, so a full duplicate is rejected regardless of {@code sharesHousehold}.
     *
     * @param owner the owner being created, with its computed {@code householdId} already assigned
     * @throws HouseholdDuplicateException if an existing owner already shares the household
     */
    private void rejectHouseholdDuplicate(Owner owner) {
        String householdId = owner.getHouseholdId();
        boolean shared = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (shared) {
            throw new HouseholdDuplicateException(householdId);
        }
    }

    /**
     * Normalizes a value for household-identity comparison: leading and trailing whitespace is
     * removed, internal runs of whitespace are collapsed to a single space, and the result is
     * lower-cased.
     *
     * @param value the raw value to normalize (may be {@code null})
     * @return the normalized value, or an empty string when {@code value} is {@code null}
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
