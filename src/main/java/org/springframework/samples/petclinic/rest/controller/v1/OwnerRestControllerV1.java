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

import java.util.Collection;
import java.util.List;

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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("AUDIT");

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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String normalizedAddress = normalizeAddress(owner.getAddress());
        if (normalizedAddress.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Address must not be blank after normalization");
        }
        owner.setAddress(normalizedAddress);
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        owner.setEmail(normalizeEmail(owner.getEmail()));
        validatePostcode(owner.getPostcode(), owner.getCity());
        java.time.LocalDate serverDate = java.time.LocalDate.now();
        if (owner.getRegistrationDate() != null && owner.getRegistrationDate().isAfter(serverDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Registration date must not be later than the current server date");
        }
        java.time.LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : serverDate;
        java.time.LocalDate businessDate = toBusinessDay(effectiveDate);
        owner.setRegistrationDate(businessDate);
        long createdToday = this.clinicService.findAllOwners().stream()
            .map(Owner::getRegistrationDate)
            .filter(businessDate::equals)
            .count();
        if (createdToday >= 100) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "The maximum number of owners for today has already been reached");
        }
        owner.setBulkSignupWarning(createdToday > 80);
        // The household is keyed deterministically on (normalized last name, postcode): every owner
        // computes the same householdId from those two fields, so owners sharing a last name and
        // postcode belong to the same household automatically, without any explicit linking.
        String lastNameKey = normalizeForHousehold(owner.getLastName());
        String householdId = householdId(lastNameKey, owner.getPostcode());
        owner.setHouseholdId(householdId);
        List<Owner> householdMembers = this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(
                householdId(normalizeForHousehold(existing.getLastName()), existing.getPostcode())))
            .toList();
        // All duplicate detection is expressed through the single derived identityKey
        // (telephone|email|householdId); a new owner is rejected only when its whole key matches.
        String identityKey = owner.getIdentityKey();
        boolean identityInUse = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (identityInUse) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "An owner with this identity already exists");
        }
        // Because the household is keyed on (last name, postcode), a second owner sharing a last name
        // and postcode with an existing owner is the same household and is rejected as a household
        // duplicate (409). Declaring 'sharesHousehold' only bypasses this block, creating the owner as
        // a declared household member.
        boolean declaredHouseholdMember = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (!householdMembers.isEmpty() && !declaredHouseholdMember) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "An owner in this household already exists");
        }
        String cityKey = normalizeForHousehold(owner.getCity());
        long cityCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(cityKey))
            .count();
        if (cityCount >= 50) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This city already has the maximum number of owners");
        }
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(householdMembers.size() + 1);
        // The former soft-match (same last name and postcode, different telephone) now coincides
        // exactly with the household key: such an owner is either rejected as a household duplicate
        // above, or, when it declares 'sharesHousehold', created as a declared household member. A
        // declared member is not a suspected duplicate, so no owner is ever flagged here.
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        owner.setCustomerCode(customerCode(owner.getCity(), owner.getPostcode(),
            owner.getTelephone(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), ownerDto.getMembershipLevel());
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Rolls a registration date forward to a business day. When {@code date} falls on a Saturday or
     * Sunday it is advanced to the following Monday; a weekday is returned unchanged. This is applied
     * to the effective registration date (whether supplied in the request or defaulted to the server
     * date) so that every stored {@code registrationDate}, and any value derived from it, lands on a
     * business day.
     *
     * @param date the effective registration date
     * @return the same date if it is a weekday, otherwise the following Monday
     */
    private java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    /**
     * Counts the owners that already exist (before the current create) whose first and last names
     * both match the supplied names, compared case-insensitively. The returned value is captured on
     * the new owner at creation time and does not change as further owners are added later.
     *
     * @param firstName the new owner's first name
     * @param lastName  the new owner's last name
     * @return the number of pre-existing namesake owners
     */
    private int namesakeCount(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getFirstName() != null
                && existing.getFirstName().equalsIgnoreCase(firstName)
                && existing.getLastName() != null
                && existing.getLastName().equalsIgnoreCase(lastName))
            .count();
    }

    /**
     * Builds the customer code for a new owner, formatted {@code '<REGION>-<HASH8>'} where
     * {@code REGION} is the canonical region derived from the owner's postcode (falling back to
     * the city when the postcode is absent or in no known range, see
     * {@link org.springframework.samples.petclinic.mapper.Localities#forCityAndPostcode}), and
     * {@code HASH8} is the first 8 upper-case hex characters of the SHA-256 digest of the
     * normalized telephone concatenated with the last name (e.g. {@code 'NSW-1A2B3C4D'}). Unlike
     * the previous city-prefixed scheme it carries no per-city sequence number, so the identity is
     * a pure function of the owner's region, telephone and last name.
     *
     * @param city                the owner's city
     * @param postcode            the owner's postcode, may be {@code null}
     * @param normalizedTelephone the owner's telephone, already normalized to E.164
     * @param lastName            the owner's last name
     * @return the assigned customer code
     */
    private String customerCode(String city, String postcode, String normalizedTelephone, String lastName) {
        String region = org.springframework.samples.petclinic.mapper.Localities
            .forCityAndPostcode(city, postcode);
        String hash8 = sha256UpperHex((normalizedTelephone == null ? "" : normalizedTelephone)
            + (lastName == null ? "" : lastName), 8);
        return region + "-" + hash8;
    }

    /**
     * Returns the first {@code length} upper-case hex characters of the SHA-256 digest of the
     * UTF-8 bytes of {@code value}.
     *
     * @param value  the source value to hash
     * @param length the number of leading hex characters to return
     * @return the leading upper-case hex characters of the digest
     */
    private String sha256UpperHex(String value, int length) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase(java.util.Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a street address applied whenever an owner is created: leading and trailing
     * whitespace is trimmed, any internal run of whitespace is collapsed to a single space, the
     * value is upper-cased, and common street-type abbreviations are expanded to their full form
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The expansion is applied
     * per whitespace-delimited token, so only standalone abbreviations are expanded. A {@code null}
     * value normalizes to the empty string. So {@code '  12  main  st '} becomes
     * {@code '12 MAIN STREET'}.
     *
     * @param address the raw address as submitted, or {@code null} when absent
     * @return the normalized address, or the empty string when the value is blank
     */
    private String normalizeAddress(String address) {
        String collapsed = (address == null ? "" : address)
            .trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
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
     * Normalizes a value for household-duplicate comparison: leading and trailing whitespace is
     * trimmed, any internal run of whitespace is collapsed to a single space, and the result is
     * lower-cased so the comparison is case-insensitive. A {@code null} value normalizes to the
     * empty string.
     *
     * @param value the raw value (last name or address) as submitted
     * @return the normalized comparison key
     */
    private String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Derives the stable shared household identifier for a household, keyed on the normalized last
     * name and address. Because it is a pure function of those normalized keys, every owner in the
     * same household deterministically computes the same value. The identifier is the first 12
     * upper-case hex characters of the SHA-256 digest of the two keys joined with a NUL separator
     * (the separator prevents distinct name/address pairs from colliding).
     *
     * @param lastNameKey the normalized last-name comparison key
     * @param addressKey  the normalized address comparison key
     * @return the stable household identifier
     */
    private String householdId(String lastNameKey, String postcode) {
        String seed = lastNameKey + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase(java.util.Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * The exact number of national (subscriber) digits required for a given country calling code,
     * i.e. the digits that follow the country code in an E.164 number. A country code that is not
     * listed here has no fixed-length requirement beyond the general 8-to-15-digit E.164 bound.
     * Australia ({@code '+61'}) requires 9 national digits and the NANP ({@code '+1'}) requires 10.
     */
    private static final java.util.Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        java.util.Map.of("61", 9, "1", 10);

    /**
     * Normalizes a telephone number to E.164 form. Spaces, dashes and brackets (indeed any
     * non-digit character) are stripped. If the submitted value carries a leading {@code '+'}
     * its country code is kept as-is; otherwise the default country code {@code '+61'} is
     * assumed and a single leading {@code '0'} is dropped from the national digits. The
     * resulting value must be a {@code '+'} followed by 8 to 15 digits. So {@code '0412 345 678'}
     * is stored as {@code '+61412345678'}.
     *
     * <p>In addition, when the number's country code has a fixed national-number length (see
     * {@link #NATIONAL_NUMBER_LENGTHS}), the national digits that follow the country code must
     * match that length exactly: {@code '+61'} requires 9 national digits and {@code '+1'}
     * requires 10. So {@code '+61 123'} is rejected because its 3 national digits are not 9.
     *
     * @param telephone the raw telephone number as submitted
     * @return the normalized E.164 telephone number
     * @throws ResponseStatusException with a 400 status if the value cannot form a valid E.164
     *         number (8 to 15 digits after the '+'), or if its national-number length is wrong
     *         for its country code
     */
    private String normalizeTelephone(String telephone) {
        String raw = telephone == null ? "" : telephone.trim();
        String digits = raw.replaceAll("\\D", "");
        String e164;
        if (raw.startsWith("+")) {
            e164 = "+" + digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            e164 = "+61" + digits;
        }
        int digitCount = e164.length() - 1;
        if (digitCount < 8 || digitCount > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        validateNationalNumberLength(e164);
        return e164;
    }

    /**
     * Validates that an E.164 number's national-number length is correct for its country code.
     * The country code is matched by longest known prefix (so {@code '+61'} is preferred over
     * {@code '+1'} would-be matches); when the code has a fixed national-number length the digits
     * following it must match exactly. Country codes without a fixed length are left unchecked.
     *
     * @param e164 the normalized E.164 number (a {@code '+'} followed by digits)
     * @throws ResponseStatusException with a 400 status if the national-number length is wrong
     *         for the country code
     */
    private void validateNationalNumberLength(String e164) {
        String allDigits = e164.substring(1);
        String bestCode = null;
        for (String code : NATIONAL_NUMBER_LENGTHS.keySet()) {
            if (allDigits.startsWith(code)
                    && (bestCode == null || code.length() > bestCode.length())) {
                bestCode = code;
            }
        }
        if (bestCode == null) {
            return;
        }
        int nationalLength = allDigits.length() - bestCode.length();
        int required = NATIONAL_NUMBER_LENGTHS.get(bestCode);
        if (nationalLength != required) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Telephone for country code '+" + bestCode + "' must have " + required
                    + " national digits");
        }
    }

    /**
     * The inclusive 4-digit postcode range permitted for each canonical region, keyed by the
     * region derived from the owner's city ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
     * {@code Brisbane -> QLD}): NSW {@code 2000-2099}, VIC {@code 3000-3099}, QLD
     * {@code 4000-4099}. A city whose region is not listed here accepts any 4-digit postcode.
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODE_RANGES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's optional postcode against their city's region. The postcode is only
     * checked when present (a {@code null} postcode is accepted, keeping the request contract
     * backward-compatible). Its four-digit shape is already enforced by Bean Validation on the
     * request DTO. When the city maps to a known region (see {@link #REGION_POSTCODE_RANGES}) the
     * postcode must fall within that region's inclusive range; a city with no known region accepts
     * any 4-digit postcode.
     *
     * @param postcode the owner's postcode as submitted, or {@code null} when absent
     * @param city     the owner's city, used to derive the region whose range applies
     * @throws ResponseStatusException with a 400 status when a supplied postcode is out of range
     *         for the city's region
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        String region = org.springframework.samples.petclinic.mapper.Localities.forCity(city);
        int[] range = REGION_POSTCODE_RANGES.get(region);
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Postcode " + postcode + " is not valid for region " + region);
        }
    }

    /**
     * Normalizes an optional email address by lower-casing it. Syntactic validity is enforced
     * by Bean Validation on the request DTO, so a value reaching this point is either {@code null}
     * (absent) or already valid.
     *
     * @param email the email address as submitted, or {@code null} when absent
     * @return the lower-cased email, or {@code null} when absent
     */
    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(java.util.Locale.ROOT);
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
