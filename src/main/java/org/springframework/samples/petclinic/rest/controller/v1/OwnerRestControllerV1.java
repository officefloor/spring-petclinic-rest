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
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.advice.CityOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
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
import org.springframework.util.StringUtils;
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
     * Dedicated audit logger. A successful owner create emits a single line here carrying the new
     * owner's id, customer code, registration date and membership level, so the create side-effect
     * can be audited independently of the application log.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    /**
     * Pattern for a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain containing at least one dot, with no whitespace in any segment.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Required national-number length per E.164 country code. A normalized telephone whose country
     * code appears here must carry exactly this many national digits after the code; Australia
     * ({@code +61}) requires 9 and the North American Numbering Plan ({@code +1}) requires 10.
     * Country codes not listed are only subject to the generic 8-to-15 total-digit rule.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTH = Map.of("61", 9, "1", 10);

    /**
     * Maximum number of owners permitted in a single city. A create whose city already holds this many
     * owners (compared case-insensitively) is rejected with a 409 Conflict.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Maximum number of owners that may be registered on a single day (by registration date). A create
     * attempted once this many owners already carry today's registration date is rejected with a
     * 429 Too Many Requests.
     */
    private static final int MAX_OWNERS_PER_DAY = 100;

    /**
     * Threshold above which a create is flagged with a bulk-signup warning. When more than this many
     * owners already carry the new owner's registration date, the created owner is flagged
     * ({@code bulkSignupWarning} true); otherwise the flag is false.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Expansions of the common street-type abbreviations recognised by {@link #normalizeAddress}.
     * Keys and values are upper-case, matching the casing the address is folded to before expansion.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Inclusive 4-digit postcode range permitted per region, keyed by the region derived from the
     * owner's city ({@link Owner#getLocality()}): NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A
     * region not listed (the {@code 'UNKNOWN'} locality of a city with no known region) accepts any
     * 4-digit postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

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
        validateRequiredFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        LocalDate registrationDate = Owner.resolveRegistrationDate(owner.getRegistrationDate());
        owner.setRegistrationDate(registrationDate);
        requireDailyCapacity(registrationDate);
        requireCityHasCapacity(owner.getCity());
        validatePostcode(owner);
        owner.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            joinHousehold(owner);
        }
        owner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        requireUniqueIdentity(owner);
        owner.setCustomerCode(owner.buildCustomerCode());
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(countOwnersRegisteredOn(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD);
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Rejects a create once {@value #MAX_OWNERS_PER_DAY} or more owners already carry the new owner's
     * adjusted business-day registration date. Existing owners are counted by {@code registrationDate}
     * equal to that date, before the new owner is saved, so the count reflects only the owners that
     * existed prior to this create.
     *
     * @param registrationDate the new owner's effective registration date, already rolled forward onto
     *     a business day
     * @throws DailyOwnerLimitException if {@value #MAX_OWNERS_PER_DAY} or more owners are already
     *     registered on that business day
     */
    private void requireDailyCapacity(LocalDate registrationDate) {
        if (countOwnersRegisteredOn(registrationDate) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(registrationDate);
        }
    }

    /**
     * Counts the existing owners whose registration date equals the given business-day date. This is
     * evaluated before the new owner is saved, so it reflects only the owners that existed prior to
     * this create. It is the single definition of the per-day count shared by the daily-limit rule and
     * the bulk-signup warning.
     *
     * @param registrationDate the effective business-day registration date to count against
     * @return the number of pre-existing owners registered on that date
     */
    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Rejects an owner whose city already contains the maximum allowed number of owners
     * ({@value #MAX_OWNERS_PER_CITY}). Existing owners are counted by city compared case-insensitively,
     * before the new owner is saved, so the count reflects only the owners that existed prior to this
     * create.
     *
     * @param city the city of the owner being created
     * @throws CityOwnerLimitException if the city already holds {@value #MAX_OWNERS_PER_CITY} or more owners
     */
    private void requireCityHasCapacity(String city) {
        long cityOwnerCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getCity(), city))
            .count();
        if (cityOwnerCount >= MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitException(city);
        }
    }

    /**
     * Validates a supplied postcode against the owner's region. Postcode is optional: an owner with no
     * postcode is accepted unchanged. When present it is a 4-digit string (enforced by the request
     * schema) that must fall within the inclusive range permitted for the region derived from the
     * owner's city ({@link #REGION_POSTCODE_RANGES}); a city whose region is unknown accepts any 4-digit
     * postcode. An out-of-range postcode is rejected with a 400 Bad Request.
     *
     * @param owner the owner being created, with its city and postcode set
     * @throws InvalidOwnerFieldsException if a supplied postcode is out of range for the city's region
     */
    private void validatePostcode(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        int[] range = REGION_POSTCODE_RANGES.get(owner.getLocality());
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }

    /**
     * Counts the existing owners that already share the given first name and last name, compared
     * case-insensitively. This is evaluated before the new owner is saved, so it reflects only the
     * owners that existed prior to this create.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of pre-existing namesakes
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getFirstName(), firstName)
                && equalsIgnoreCase(existing.getLastName(), lastName))
            .count();
    }

    /**
     * Compares two possibly-{@code null} strings for equality, ignoring case. Two {@code null} values
     * are considered equal and a {@code null} is never equal to a non-{@code null} value.
     *
     * @param a the first value, possibly {@code null}
     * @param b the second value, possibly {@code null}
     * @return whether the values are equal ignoring case
     */
    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    /**
     * Rejects an owner payload that is missing or blank in any required field. Bean Validation
     * (via {@code @Valid}) already rejects {@code null} values and fields that violate their size
     * or pattern constraints; this guard additionally rejects whitespace-only values for fields
     * (such as address and city) that have no pattern constraint, so that no required field is blank.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if one or more required fields are missing or blank
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> invalidFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            invalidFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            invalidFields.add("lastName");
        }
        if (!StringUtils.hasText(normalizeAddress(ownerFieldsDto.getAddress()))) {
            invalidFields.add("address");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            invalidFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            invalidFields.add("telephone");
        }
        if (!invalidFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(invalidFields);
        }
    }

    /**
     * Normalizes a submitted telephone number to E.164 form and requires the result to be valid. The
     * normalized E.164 string (a leading {@code '+'} followed by country code and national digits) is
     * what gets stored and returned.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized E.164 telephone number
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Converts a telephone value to E.164 form, giving the single authoritative definition of a
     * telephone's normalized form. Spaces, dashes and brackets are stripped. A leading {@code '+'}
     * and its country code are kept when present; otherwise country code {@code '+61'} is assumed and
     * a single leading {@code '0'} is dropped from the national digits. The result is valid only when
     * it contains 8 to 15 digits after the {@code '+'} and, for a recognized country code (see
     * {@link #NATIONAL_NUMBER_LENGTH}), carries exactly that country's required national-number length.
     *
     * <p>Returns {@code null} rather than throwing when the value cannot form a valid E.164 number, so
     * it is safe to apply to already-stored (possibly legacy) values when comparing them.
     *
     * @param telephone the raw telephone value, possibly {@code null}
     * @return the E.164 string, or {@code null} if the value cannot form a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[ \\-()]", "");
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
        if (!nationalNumberLengthValid(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks a normalized E.164 digit string (country code followed by national digits, without the
     * leading {@code '+'}) against the per-country national-number length rule. For a country code
     * listed in {@link #NATIONAL_NUMBER_LENGTH} the national part must have exactly the required length
     * (e.g. {@code '+61'} needs 9 national digits, {@code '+1'} needs 10); any other country code is
     * accepted, leaving only the generic total-length rule to apply.
     *
     * @param digits the E.164 digits without the leading {@code '+'}
     * @return {@code true} if the national-number length matches the country code's requirement
     */
    private boolean nationalNumberLengthValid(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_NUMBER_LENGTH.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code)) {
                return digits.length() - code.length() == entry.getValue();
            }
        }
        return true;
    }

    /**
     * Normalizes an optional owner email. An absent (null or blank) email is allowed and yields
     * {@code null}. When present, the value must be a syntactically valid address; it is then stored
     * and returned lower-cased.
     *
     * @param email the raw email value from the request, possibly {@code null}
     * @return the lower-cased email, or {@code null} when none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but not a valid email address
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects an owner whose whole identity key is already used by an existing owner. This is the
     * single consolidated duplicate check: the identity key ({@link Owner#getIdentityKey()}) is the
     * owner's normalized telephone, email (empty when absent) and household id joined with
     * {@code '|'}, and a create is a duplicate only when the new owner's whole key equals an existing
     * owner's. Because the telephone is part of the key, two members of the same household
     * (same {@code householdId}) with different telephones have different identity keys and are both
     * allowed.
     *
     * @param owner the owner being created, with its normalized telephone, email and household id set
     * @throws DuplicateIdentityException if another owner already has this identity key
     */
    private void requireUniqueIdentity(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean inUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (inUse) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * Returns the existing owners that belong to the same household as the given last name and
     * address, i.e. those whose last name and address match case-insensitively and with runs of
     * whitespace collapsed to a single space.
     *
     * @param lastName the last name to match
     * @param address the address to match
     * @return the matching owners (possibly empty, never {@code null})
     */
    private List<Owner> findHouseholdMembers(String lastName, String address) {
        String normalizedLastName = normalizeForHousehold(lastName);
        String normalizedAddress = normalizeAddress(address);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getLastName()).equals(normalizedLastName)
                && normalizeAddress(existing.getAddress()).equals(normalizedAddress))
            .toList();
    }

    /**
     * Joins the owner being created to the shared household of any existing owner(s) that have the
     * same last name and address (compared case-insensitively with collapsed whitespace). Every
     * member of the household - the existing owners and the new one - is assigned the same stable
     * {@code householdId}, so the shared identifier can be read back from any of them.
     *
     * <p>When no existing owner shares the household there is nobody to share an identifier with, so
     * the owner is left without a {@code householdId}.
     *
     * <p>Every member of the household - the existing owners and the new one - is also stamped with
     * the household's member count after this create ({@code householdSize}), so the shared size can
     * be read back from any of them.
     *
     * @param owner the owner being created, whose {@code householdId} is set when a household is joined
     */
    private void joinHousehold(Owner owner) {
        List<Owner> members = findHouseholdMembers(owner.getLastName(), owner.getAddress());
        if (members.isEmpty()) {
            return;
        }
        String householdId = computeHouseholdId(owner.getLastName(), owner.getAddress());
        int householdSize = members.size() + 1;
        owner.setHouseholdId(householdId);
        owner.setHouseholdSize(householdSize);
        for (Owner member : members) {
            member.setHouseholdId(householdId);
            member.setHouseholdSize(householdSize);
            this.clinicService.saveOwner(member);
        }
    }

    /**
     * Computes the stable, shared household identifier for a last name and address. The value is
     * derived deterministically from the normalized (case-insensitive, whitespace-collapsed) last
     * name and address, so every owner in the same household computes the same identifier.
     *
     * @param lastName the last name of the household
     * @param address the address of the household
     * @return the household identifier, formatted {@code 'HH-<12 upper-case hex digits>'}
     */
    private String computeHouseholdId(String lastName, String address) {
        String key = normalizeForHousehold(lastName) + "\n" + normalizeAddress(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return "HH-" + hex;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Normalizes a value for case-insensitive, whitespace-collapsed household comparison: leading and
     * trailing whitespace is trimmed, any internal run of whitespace is collapsed to a single space,
     * and the result is lower-cased. A {@code null} value normalizes to an empty string.
     *
     * @param value the raw value, possibly {@code null}
     * @return the normalized value
     */
    private String normalizeForHousehold(String value) {
        return collapseWhitespace(value).toLowerCase(Locale.ROOT);
    }

    /**
     * Trims leading and trailing whitespace and collapses any internal run of whitespace to a single
     * space, giving the single definition of that step shared by the owner normalizers. A {@code null}
     * value yields an empty string.
     *
     * @param value the raw value, possibly {@code null}
     * @return the trimmed, whitespace-collapsed value
     */
    private String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Normalizes an owner's address to its canonical, stored-and-returned form: leading and trailing
     * whitespace is trimmed, any internal run of whitespace is collapsed to a single space, the result
     * is upper-cased, and common street-type abbreviations are expanded as whole words
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} or
     * blank-after-collapsing value normalizes to an empty string. The transformation is idempotent, so
     * applying it to an already-normalized (or legacy) value is safe when comparing addresses.
     *
     * @param address the raw address value, possibly {@code null}
     * @return the normalized address
     */
    private String normalizeAddress(String address) {
        String collapsed = collapseWhitespace(address).toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
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
