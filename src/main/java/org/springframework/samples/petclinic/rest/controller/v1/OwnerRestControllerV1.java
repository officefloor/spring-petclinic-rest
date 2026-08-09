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
     * Syntactic check for an email address: a non-empty local part, an '@', a domain with at least
     * one dot and a two-or-more letter top-level label. Case-insensitive; the accepted value is
     * stored lower-cased.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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
        validateRequiredOwnerFields(ownerFieldsDto);
        LocalDate suppliedOrDefaultDate =
            ownerFieldsDto.getRegistrationDate() != null ? ownerFieldsDto.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(suppliedOrDefaultDate);
        rejectDailyLimitExceeded(registrationDate);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(normalizeAddress(owner.getAddress()));
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        applyHousehold(owner, ownerFieldsDto.getSharesHousehold());
        owner.setEmail(normalizeEmail(owner.getEmail()));
        rejectDuplicateIdentity(owner);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(generateCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setMembershipNumber(generateMembershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel());
        OwnerDto ownerDto = toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarningActive());
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
        if (isBlank(normalizeAddress(ownerFieldsDto.getAddress()))) {
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
     * forward to the following Monday; a weekday is returned unchanged. This applies whether the date
     * was supplied on the request or defaulted to the server's current date.
     *
     * @param date the effective registration date (supplied or defaulted)
     * @return the same date when it is a weekday, otherwise the following Monday
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
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
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
     * Builds an owner's customer code, formatted '&lt;CITY3&gt;-&lt;LAST3&gt;-&lt;NNNN&gt;'. CITY3 is
     * the upper-cased first three letters of the city; LAST3 is the upper-cased first three letters of
     * the last name; NNNN is a per-city 4-digit zero-padded sequence equal to one more than the number
     * of owners already in that city (e.g. 'SYD-SMI-0007').
     *
     * @param city the owner's city
     * @param lastName the owner's last name
     * @return the formatted customer code
     */
    private String generateCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        String normalizedCity = normalizeHouseholdField(city);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHouseholdField(existing.getCity()).equals(normalizedCity))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Builds an owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where
     * customerCode is the owner's customer code and YY is the last two digits of the
     * registrationDate year (e.g. 'SMI-0007-M26').
     *
     * @param customerCode the owner's customer code
     * @param registrationDate the owner's registration date
     * @return the formatted membership number
     */
    private String generateMembershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", registrationDate.getYear() % 100);
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
     * left unset. When a value is present it must be a syntactically valid address; the normalized
     * form stored and returned is the value lower-cased.
     *
     * @param email the raw email value from the request, or {@code null} when none was supplied
     * @return the lower-cased email, or {@code null} when no email was supplied
     * @throws InvalidOwnerFieldsException if a non-blank value is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return email.toLowerCase(Locale.ROOT);
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
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (inUse) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }

    /**
     * Applies the household rule to an owner being created. A household is the set of owners sharing a
     * last name and a normalized address: last names are compared case-insensitively after whitespace
     * is trimmed and collapsed, and addresses are compared in their normalized form (see
     * {@link #normalizeAddress(String)}), so purely cosmetic differences in spacing, letter case or
     * abbreviation still count as the same household.
     * <p>
     * When the owner's last name and address already belong to at least one other owner and
     * {@code sharesHousehold} is true, every member of the household - the existing owners and this
     * joiner - is assigned the same stable {@code householdId}, which is returned on read and forms
     * the third component of the {@code identityKey}. When {@code sharesHousehold} is not true, or no
     * other owner shares the last name and address, no identifier is assigned. Household membership no
     * longer rejects a create on its own; duplicates are detected solely through the identity key.
     *
     * @param owner the owner being created
     * @param sharesHousehold the request's shared-household acknowledgement, or {@code null} when absent
     */
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

    private void applyHousehold(Owner owner, Boolean sharesHousehold) {
        String lastName = normalizeHouseholdField(owner.getLastName());
        String address = normalizeAddress(owner.getAddress());
        List<Owner> housemates = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeHouseholdField(existing.getLastName()).equals(lastName)
                && normalizeAddress(existing.getAddress()).equals(address))
            .toList();
        if (housemates.isEmpty()) {
            return;
        }
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String householdId = generateHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner housemate : housemates) {
            if (!householdId.equals(housemate.getHouseholdId())) {
                housemate.setHouseholdId(householdId);
                this.clinicService.saveOwner(housemate);
            }
        }
    }

    /**
     * Builds the stable identifier shared by the members of one household. It is derived purely from
     * the normalized last name and address, so every owner of a given household deterministically
     * resolves to the same value - the first 12 upper-case hex characters of the SHA-256 digest of the
     * two normalized fields joined by a single space.
     *
     * @param normalizedLastName the household's last name, already normalized for comparison
     * @param normalizedAddress the household's address, already normalized for comparison
     * @return the shared household identifier
     */
    private String generateHouseholdId(String normalizedLastName, String normalizedAddress) {
        String key = normalizedLastName + ' ' + normalizedAddress;
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
