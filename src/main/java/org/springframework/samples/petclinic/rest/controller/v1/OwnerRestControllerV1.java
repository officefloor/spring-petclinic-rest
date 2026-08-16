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
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.controller.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.controller.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.controller.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.controller.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.controller.InvalidEmailException;
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
     * Dedicated audit logger. On successful owner create an audit line is emitted here carrying
     * the newly assigned owner id, customer code and registration date.
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
        normalizeAddress(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        normalizeTelephone(ownerFieldsDto);
        rejectDuplicateTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateHousehold(ownerFieldsDto);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        defaultRegistrationDate(ownerFieldsDto);
        rejectDailyLimit(ownerFieldsDto.getRegistrationDate());
        normalizeEmail(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        assignCustomerCode(owner);
        assignHouseholdId(owner);
        assignHouseholdMemberCount(owner);
        assignNamesakeCount(owner);
        assignMembershipNumber(owner);
        assignBulkSignupWarning(owner);
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
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
        normalizeEmail(ownerFieldsDto);
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
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


    /**
     * Rejects an owner whose mandatory fields ({@code firstName}, {@code lastName},
     * {@code address}, {@code city}, {@code telephone}) are missing or blank. Bean
     * Validation already rejects {@code null} values and the pattern-constrained fields
     * when blank, but {@code address} and {@code city} have no pattern, so a
     * whitespace-only value would otherwise slip through.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws RequiredFieldsMissingException if any required field is missing or blank
     */
    /**
     * Common street-type abbreviations expanded during address normalization. Keys are the
     * upper-cased abbreviation tokens; values are their canonical expansions.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes the submitted address on create and writes the canonical value back onto the
     * request so it is persisted, echoed back, and used for every downstream address comparison
     * (household duplicate detection and the household id). The value is trimmed, internal
     * whitespace runs are collapsed to a single space, it is upper-cased, and common street-type
     * abbreviations are expanded token-by-token (see {@link #ADDRESS_ABBREVIATIONS}). A
     * {@code null} address is left untouched so the required-field check can report it as missing.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private static void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        ownerFieldsDto.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
    }

    /**
     * Canonicalizes an address value: trimmed, internal whitespace collapsed to single spaces,
     * upper-cased, and each whitespace-delimited token that is a known street-type abbreviation
     * expanded to its full form. A {@code null} value is returned unchanged.
     *
     * @param raw the address to canonicalize, possibly {@code null}
     * @return the normalized address, or {@code null} if {@code raw} was {@code null}
     */
    private static String normalizeAddress(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
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

    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> errors = new ArrayList<>();
        requireText("firstName", ownerFieldsDto.getFirstName(), errors);
        requireText("lastName", ownerFieldsDto.getLastName(), errors);
        requireText("address", ownerFieldsDto.getAddress(), errors);
        requireText("city", ownerFieldsDto.getCity(), errors);
        requireText("telephone", ownerFieldsDto.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new RequiredFieldsMissingException(errors);
        }
    }

    private static void requireText(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }

    /**
     * Normalizes the submitted telephone on create to E.164 form (see {@link #toE164}) and
     * writes the canonical {@code '+'}-prefixed value back onto the request so it is persisted
     * and echoed back in that form.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws RequiredFieldsMissingException with a 400 status if the telephone cannot form a
     *                                        valid E.164 number
     */
    private static void normalizeTelephone(OwnerFieldsDto ownerFieldsDto) {
        String e164 = toE164(ownerFieldsDto.getTelephone());
        if (e164 == null) {
            throw new RequiredFieldsMissingException(List.of("telephone"));
        }
        ownerFieldsDto.setTelephone(e164);
    }

    /**
     * Converts a raw telephone to E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. Spaces, dashes and brackets are stripped. When a leading {@code '+'}
     * and country code are present they are kept; otherwise country code {@code '+61'} is
     * assumed and a single leading {@code '0'} is dropped from the national digits. The result
     * must contain 8 to 15 digits after the {@code '+'}.
     *
     * @param raw the submitted telephone, possibly {@code null}
     * @return the canonical E.164 string, or {@code null} if it is not a valid E.164 number
     */
    private static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
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
        return "+" + digits;
    }

    /**
     * Rejects a create whose E.164 telephone is already used by any existing owner. The
     * submitted telephone has already been normalized to E.164 by {@link #normalizeTelephone};
     * each stored owner's telephone is normalized the same way before comparison so differing
     * input formats collapse to the same value.
     *
     * @param normalizedTelephone the submitted, already-normalized E.164 telephone
     * @throws DuplicateTelephoneException with a 409 status if another owner already uses the
     *                                     same E.164 telephone
     */
    /**
     * Matches a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain of at least two dot-separated labels ending in an alphabetic TLD. None of
     * the parts may contain whitespace or a second {@code @}.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$");

    /**
     * Normalizes the submitted email when present: the value is trimmed, required to be a
     * syntactically valid address, and written back lower-cased so it is persisted and echoed
     * back in canonical form. A {@code null} email is left untouched — the field is optional.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidEmailException with a 400 status if the email is present but not syntactically valid
     */
    private static void normalizeEmail(OwnerFieldsDto ownerFieldsDto) {
        String email = ownerFieldsDto.getEmail();
        if (email == null) {
            return;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        ownerFieldsDto.setEmail(trimmed.toLowerCase(Locale.ROOT));
    }

    /**
     * Establishes the effective registration date on create and rolls it onto a business day.
     * When the client does not supply a {@code registrationDate} it defaults to the server's
     * current date; the resulting date (supplied or defaulted) is then rolled forward to the
     * next Monday when it falls on a Saturday or Sunday (see {@link #toBusinessDay}). The
     * adjusted value is written back onto the request so it is persisted, echoed back in ISO
     * 'YYYY-MM-DD' format, and used by every value derived from the registration date (such as
     * the membership number's year segment and the daily create-limit).
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private static void defaultRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate effective = ownerFieldsDto.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        ownerFieldsDto.setRegistrationDate(toBusinessDay(effective));
    }

    /**
     * Rolls a date forward to the next business day: a Saturday or Sunday is advanced to the
     * following Monday; any weekday is returned unchanged.
     *
     * @param date the date to adjust
     * @return the same date when it is a weekday, otherwise the next Monday
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    /**
     * Assigns the owner's {@code customerCode} on create, formatted
     * {@code '<CITY3>-<LAST3>-<NNNN>'} where CITY3 is the upper-cased first three letters of
     * {@code city}, LAST3 the upper-cased first three letters of {@code lastName}, and NNNN a
     * per-city 4-digit zero-padded sequence equal to one more than the number of owners already
     * in that city (compared case-insensitively) — e.g. {@code 'SYD-SMI-0007'}.
     *
     * @param owner the owner being created (with its {@code city} and {@code lastName} already set)
     */
    private void assignCustomerCode(Owner owner) {
        String city = owner.getCity();
        String lastName = owner.getLastName();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        long sequence = this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    /**
     * Assigns the owner's {@code householdId} on create: a stable identifier for the household,
     * formatted {@code 'HH-<HEX12>'} where HEX12 is the first 12 upper-cased hex characters of the
     * SHA-256 digest of the owner's collapsed {@code lastName} and {@code address} (see
     * {@link #collapse}) joined by a {@code '\n'}. Because it is derived purely from those two
     * canonicalized fields, every owner in the same household — including one joining an existing
     * household via {@code sharesHousehold} — is assigned the same value, independent of create
     * order.
     *
     * @param owner the owner being created (with its {@code lastName} and {@code address} already set)
     */
    private void assignHouseholdId(Owner owner) {
        String key = collapse(owner.getLastName()) + "\n" + collapse(owner.getAddress());
        owner.setHouseholdId("HH-" + sha256Hex(key).substring(0, 12).toUpperCase(Locale.ROOT));
    }

    /**
     * Assigns the owner's {@code householdMemberCount} on create: the number of owners in this
     * owner's household — those already sharing its {@code householdId} plus this owner itself —
     * after this create. Because {@code householdId} is derived from the owner's collapsed
     * {@code lastName} and {@code address} (see {@link #assignHouseholdId}), the count reflects every
     * owner registered to the same household, including those who joined via {@code sharesHousehold}.
     * The count is taken before this owner is persisted, so it is one more than the number of
     * existing household members. It underpins the {@code GOLD} membership tier, which applies once
     * the household reaches three or more members.
     *
     * @param owner the owner being created (with its {@code householdId} already assigned)
     */
    private void assignHouseholdMemberCount(Owner owner) {
        String householdId = owner.getHouseholdId();
        long existing = this.clinicService.findAllOwners().stream()
            .filter(other -> householdId != null && householdId.equals(other.getHouseholdId()))
            .count();
        owner.setHouseholdMemberCount((int) (existing + 1));
    }

    /**
     * Assigns the owner's {@code namesakeCount} on create: the number of existing owners that
     * already share this owner's {@code firstName} and {@code lastName}, compared
     * case-insensitively. The count is taken before this owner is persisted, so it reflects only
     * pre-existing namesakes.
     *
     * @param owner the owner being created (with its {@code firstName} and {@code lastName} already set)
     */
    private void assignNamesakeCount(Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName != null && firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName != null && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
        owner.setNamesakeCount((int) count);
    }

    /**
     * Assigns the owner's {@code membershipNumber} on create, formatted
     * {@code '<customerCode>-M<YY>'} where customerCode is the owner's already-assigned customer
     * code and YY is the last two digits of the {@code registrationDate} year, zero-padded
     * (e.g. {@code 'SMI-0007-M26'}). Derived purely from the owner's own fields, so both
     * {@link #assignCustomerCode} and {@link #defaultRegistrationDate} must run first.
     *
     * @param owner the owner being created (with its {@code customerCode} and
     *              {@code registrationDate} already set)
     */
    private void assignMembershipNumber(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }

    /**
     * The number of owners that must already carry a registration date before a subsequent
     * create for that same day is flagged with a bulk-signup warning. The warning is raised
     * once the count of owners already registered on that day <em>exceeds</em> this value.
     */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Assigns the owner's {@code bulkSignupWarning} on create: {@code true} when more than
     * {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry this owner's (business-day
     * adjusted) {@code registrationDate}, otherwise {@code false}. The count is taken over the
     * existing owners before this one is persisted, so it reflects only the owners already
     * created for that day (matching the accumulation used by {@link #rejectDailyLimit}).
     *
     * @param owner the owner being created (with its {@code registrationDate} already set)
     */
    private void assignBulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        owner.setBulkSignupWarning(count > BULK_SIGNUP_WARNING_THRESHOLD);
    }

    /**
     * Returns the lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value}.
     *
     * @param value the string to hash
     * @return the 64-character lower-case hex digest
     */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void rejectDuplicateTelephone(String normalizedTelephone) {
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .map(OwnerRestControllerV1::toE164)
            .filter(telephone -> telephone != null)
            .anyMatch(normalizedTelephone::equals);
        if (duplicate) {
            throw new DuplicateTelephoneException(normalizedTelephone);
        }
    }

    /**
     * Rejects a create whose {@code lastName} and {@code address} match those of an existing
     * owner. Both fields are compared with {@link #collapse} — trimmed, internal whitespace
     * runs collapsed to a single space, and case-insensitive — so differing spacing or casing
     * still counts as the same household. The check is skipped when the request opts in via
     * {@code sharesHousehold == true}, acknowledging that the new owner shares a household with
     * the existing one.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws DuplicateHouseholdException with a 409 status if another owner already has the same
     *                                     last name and address and the request did not opt in
     */
    private void rejectDuplicateHousehold(OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String lastName = collapse(ownerFieldsDto.getLastName());
        String address = collapse(ownerFieldsDto.getAddress());
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(owner -> collapse(owner.getLastName()).equals(lastName)
                && collapse(owner.getAddress()).equals(address));
        if (duplicate) {
            throw new DuplicateHouseholdException(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        }
    }

    /**
     * The maximum number of owners permitted in a single city. A create is rejected once the
     * owner's city already contains this many owners.
     */
    private static final long CITY_CAPACITY = 50;

    /**
     * Rejects a create whose {@code city} already contains {@link #CITY_CAPACITY} or more owners,
     * compared case-insensitively (matching {@link #assignCustomerCode}). The count is taken
     * before this owner is persisted.
     *
     * @param city the submitted city
     * @throws CityAtCapacityException with a 409 status if the city is already at capacity
     */
    private void rejectCityAtCapacity(String city) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }

    /**
     * The maximum number of owners permitted to be created in a single day. A create is
     * rejected once this many owners already carry today's {@code registrationDate}.
     */
    private static final long DAILY_LIMIT = 100;

    /**
     * Rejects a create once {@link #DAILY_LIMIT} or more owners already carry the given adjusted
     * business day as their {@code registrationDate}. The day counted against is this create's
     * effective registration date after the business-day roll (see {@link #defaultRegistrationDate}),
     * so owners are counted per adjusted business day. The count is taken before this owner is
     * persisted.
     *
     * @param businessDay the effective, business-day-adjusted registration date of this create
     * @throws DailyOwnerLimitExceededException with a 429 status if the day is already at the limit
     */
    private void rejectDailyLimit(LocalDate businessDay) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(businessDay);
        }
    }

    /**
     * Canonicalizes a value for household comparison: leading/trailing whitespace is trimmed,
     * every internal run of whitespace is collapsed to a single space, and the result is
     * lower-cased. A {@code null} value collapses to the empty string.
     *
     * @param value the value to canonicalize, possibly {@code null}
     * @return the collapsed, lower-cased value
     */
    private static String collapse(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
