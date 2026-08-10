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

import java.time.DayOfWeek;
import java.time.LocalDate;
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

    /** Dedicated audit logger for owner lifecycle side-effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Pragmatic check for a syntactically valid email address: a non-empty local
     * part, a single '@', and a domain with at least one dot-separated label.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");

    /**
     * Common street-type abbreviations expanded during address normalization,
     * keyed by their upper-cased form.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Required national-number length keyed by E.164 country calling code: a
     * {@code '+61'} (Australia) number carries 9 national digits and a {@code '+1'}
     * (NANP) number carries 10. A number whose country code is listed here must have
     * exactly the mandated number of national digits.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        Map.of("61", 9, "1", 10);

    /**
     * Inclusive 4-digit postcode range {@code {low, high}} keyed by the region derived
     * from the owner's city (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city whose
     * region is not listed here (see {@link OwnerMapper#CITY_REGION}) accepts any 4-digit
     * postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

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
        // Validate & normalize the optional email; reject the create when it is present but invalid.
        if (!normalizeEmail(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Validate & normalize the optional postcode; reject the create when it is present but
        // not valid for the owner's city (see normalizePostcode).
        if (!normalizePostcode(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Normalize the required address (trim/collapse whitespace, upper-case, expand
        // abbreviations) and store the normalized form back on the DTO so it is what gets
        // persisted and compared. Reject the create when the address is blank once normalized.
        String canonicalAddress = normalizeAddress(ownerFieldsDto.getAddress());
        if (canonicalAddress.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ownerFieldsDto.setAddress(canonicalAddress);
        // Reject the create when the owner's city is already at capacity, i.e. it already
        // contains 50 or more owners (compared case-insensitively).
        if (countOwnersInCity(ownerFieldsDto.getCity()) >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Normalize the telephone to E.164; reject the create when it cannot form a valid number.
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ownerFieldsDto.setTelephone(normalizedTelephone);
        // Consolidated duplicate detection. All former separate telephone, email and household
        // duplicate checks are now expressed through the single derived identity key
        // '<normalizedTelephone>|<email or empty>|<householdId>' (see OwnerMapper#identityKey).
        // A create is rejected only when the new owner's identity collides with an existing owner's.
        // Because the telephone is part of the identity, two members of the same household with
        // different telephones no longer collide and are both allowed; only owners that share the
        // same normalized telephone and email are treated as the same identity.
        Owner candidate = ownerMapper.toOwner(ownerFieldsDto);
        String contactIdentity = identityContact(candidate);
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> contactIdentity.equals(identityContact(existing)));
        if (identityInUse) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Determine the effective registration date — the value supplied on the request, or the
        // server's current date when none was supplied — and roll it forward to the next business
        // day: a Saturday or Sunday moves to the following Monday. Everything derived from the
        // registration date (membership-number year segment, per-day create-limit) uses this
        // adjusted date.
        LocalDate registrationDate = ownerFieldsDto.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
        registrationDate = toBusinessDay(registrationDate);
        // Reject the create when the daily sign-up limit has been reached, i.e. 100 or more
        // owners have already been registered on this (adjusted) business day.
        if (countOwnersRegisteredOn(registrationDate) >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = candidate;
        owner.setRegistrationDate(registrationDate);
        // Assign the customer code as '<CITY3>-<LAST3>-<NNNN>': the upper-cased first three
        // letters of the city, the upper-cased first three letters of the last name, and a
        // per-city 4-digit sequence one greater than the number of owners already in that city.
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        // Record how many existing owners already share this owner's first and last name
        // (compared case-insensitively) at the moment before this owner is created.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Flag a bulk sign-up when more than 80 owners have already been created today, i.e.
        // registered on this (adjusted) business day before this owner is created.
        owner.setBulkSignupWarning(countOwnersRegisteredOn(registrationDate) > 80);
        // Record the size of this owner's household after this create: the number of existing
        // owners sharing the same household (matching last name and address) plus this owner.
        owner.setHouseholdSize(countHouseholdMembers(owner.getLastName(), owner.getAddress()) + 1);
        this.clinicService.saveOwner(owner);
        // Emit an audit line recording the new owner's id, customer code, registration date
        // and membership level.
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerMapper.membershipLevel(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Validate and normalize the optional owner email in place.
     *
     * <p>An absent (or blank) email is allowed and is normalized to {@code null}.
     * When present it must be a syntactically valid address; a valid address is
     * stored back on the DTO lower-cased. Returns {@code false} when an email is
     * present but syntactically invalid, so the caller can reject with 400.
     */
    private boolean normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null || email.isBlank()) {
            ownerFieldsDto.setEmail(null);
            return true;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        ownerFieldsDto.setEmail(normalized);
        return true;
    }

    /**
     * Validate the optional owner postcode in place.
     *
     * <p>An absent (or blank) postcode is allowed and is normalized to {@code null}.
     * When present it must be exactly 4 digits, and — when the owner's city maps to a
     * known region via {@link OwnerMapper#CITY_REGION} — it must fall within that
     * region's inclusive postcode range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     * A city with no known region accepts any 4-digit postcode. Returns {@code false}
     * when a postcode is present but malformed or out of range, so the caller can
     * reject with 400.
     */
    private boolean normalizePostcode(OwnerFieldsDto ownerFieldsDto) {
        String postcode = ownerFieldsDto.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            ownerFieldsDto.setPostcode(null);
            return true;
        }
        if (!postcode.matches("\\d{4}")) {
            return false;
        }
        String region = OwnerMapper.CITY_REGION.get(ownerFieldsDto.getCity());
        int[] range = region == null ? null : REGION_POSTCODE_RANGES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normalize an owner address: trim, collapse each run of whitespace to a single
     * space, upper-case, and expand common street-type abbreviations
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
     * A {@code null} value normalizes to the empty string.
     */
    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
     * The contact portion of an owner's derived identity key: its normalized telephone and
     * email ('&lt;normalizedTelephone&gt;|&lt;email or empty&gt;'), i.e. the identity key
     * without the household segment. Two owners share an identity when this value is equal;
     * because the telephone is part of it, members of one household with different telephones
     * do not collide. A {@code null} email contributes the empty string.
     */
    private static String identityContact(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        return telephone + "|" + email;
    }

    /**
     * Normalize an owner household field (last name or address) for duplicate detection:
     * trimmed, lower-cased and with all runs of whitespace collapsed to a single space.
     * A {@code null} value normalizes to the empty string.
     */
    private static String normalizeHouseholdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalize a telephone number to E.164 form.
     *
     * <p>Spaces, dashes and brackets are stripped. When the number carries an
     * explicit leading {@code '+'} and country code it is kept; otherwise the
     * country code {@code '+61'} is assumed and a single leading {@code '0'} is
     * dropped from the national digits. The result must contain 8 to 15 digits
     * after the {@code '+'}, and — for a recognised country calling code — exactly
     * the national-number length that code mandates ({@code '+61'} => 9 national
     * digits, {@code '+1'} => 10).
     *
     * @return the E.164 string (e.g. {@code +61412345678}), or {@code null} when
     *         the input is absent or cannot form a valid E.164 number.
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        // Strip spaces, dashes and brackets.
        String stripped = trimmed.replaceAll("[\\s()\\-]", "");
        String digits;
        if (hasCountryCode) {
            // Keep the explicit country code; drop the leading '+' for validation.
            digits = stripped.substring(1);
        } else {
            // No country code: assume '+61' and drop a single leading '0'.
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        // For a recognised country calling code, enforce the exact national-number length it
        // mandates (e.g. '+61' => 9 national digits, '+1' => 10). Longer codes win so a '+61'
        // number is not mistaken for a '+6...' one. Unknown codes keep the generic 8-15 rule.
        for (String countryCode : NATIONAL_NUMBER_LENGTHS.keySet().stream()
                .sorted((a, b) -> b.length() - a.length()).toList()) {
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != NATIONAL_NUMBER_LENGTHS.get(countryCode)) {
                    return null;
                }
                break;
            }
        }
        return "+" + digits;
    }

    /**
     * Build the next customer code, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}.
     *
     * <p>{@code CITY3} is the upper-cased first three letters of {@code city} and
     * {@code LAST3} the upper-cased first three letters of {@code lastName} (fewer if
     * the value is shorter). {@code NNNN} is a per-city 4-digit zero-padded sequence
     * equal to one more than the number of owners already in that city
     * (e.g. {@code 'SYD-SMI-0007'}).
     */
    /**
     * Count the existing owners whose first and last name match the given names,
     * compared case-insensitively. Used to record an owner's namesake count at the
     * moment before it is created.
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null
                && existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName() != null
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
    }

    /**
     * Count the existing owners in the same household as the given last name and address
     * (compared after household-key normalization). Used, together with the owner being
     * created, to derive the household size recorded on the owner.
     */
    private int countHouseholdMembers(String lastName, String address) {
        String normalizedLastName = normalizeHouseholdKey(lastName);
        String normalizedAddress = normalizeHouseholdKey(address);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing ->
                normalizeHouseholdKey(existing.getLastName()).equals(normalizedLastName)
                    && normalizeHouseholdKey(existing.getAddress()).equals(normalizedAddress))
            .count();
    }

    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = countOwnersInCity(city) + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Count the existing owners located in the given city, compared case-insensitively.
     * Used to derive the per-city 4-digit sequence in the customer code.
     */
    private int countOwnersInCity(String city) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null
                && existing.getCity().equalsIgnoreCase(city))
            .count();
    }

    /**
     * Count the existing owners whose registration date is the given (adjusted business) date.
     * Used to enforce the per-day sign-up limit.
     */
    private int countOwnersRegisteredOn(LocalDate date) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Roll a registration date forward onto a business day: a Saturday or Sunday is moved to
     * the following Monday; a weekday is returned unchanged.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        Owner currentOwner = this.clinicService.findOwnerById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!normalizeEmail(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!normalizePostcode(ownerFieldsDto)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String normalizedTelephone = toE164(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(normalizedTelephone);
        currentOwner.setEmail(ownerFieldsDto.getEmail());
        currentOwner.setPostcode(ownerFieldsDto.getPostcode());
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
