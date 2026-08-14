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
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitReachedException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityAtCapacityException;
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
     * Dedicated audit logger. On a successful create an audit line carrying the new owner's id,
     * {@code customerCode} and {@code registrationDate} is emitted to the logger named {@code AUDIT}.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Syntactic check for an email address: a non-empty local part, an {@code @}, and a domain with
     * at least one dot and no whitespace on either side.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Common street-type abbreviations expanded during address normalization. Keys are the
     * upper-cased abbreviation as it appears as a whole whitespace-delimited token; values are the
     * expanded form.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * The maximum number of owners a single city may contain. A create request for a city that
     * already holds this many owners is rejected with a 409 response.
     */
    private static final int MAX_OWNERS_PER_CITY = 50;

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
        rejectDuplicateTelephone(ownerFieldsDto);
        rejectDuplicateHousehold(ownerFieldsDto);
        rejectCityAtCapacity(ownerFieldsDto);
        resolveRegistrationDate(ownerFieldsDto);
        rejectDailyLimitReached(ownerFieldsDto.getRegistrationDate());
        normalizeEmail(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        assignCustomerCode(owner);
        assignMembershipNumber(owner);
        assignNamesakeCount(owner);
        assignBulkSignupWarning(owner);
        assignHousehold(owner, ownerFieldsDto);
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
        currentOwner.setAddress(ownerFieldsDto.getAddress());
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
     * Rejects an owner whose {@code firstName}, {@code lastName}, {@code address}, {@code city} or
     * {@code telephone} is missing or blank (including whitespace-only values that Bean Validation
     * does not catch). The names of all offending fields are collected so the caller receives a
     * 400 response whose {@code errors} array lists each one.
     */
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
     * Normalizes an owner's {@code address} on create and writes the canonical form back onto the
     * request so it is what gets validated, compared, stored and returned. The value is trimmed,
     * runs of internal whitespace are collapsed to a single space, the result is upper-cased and
     * common street-type abbreviations are expanded as whole tokens ({@code ST}->{@code STREET},
     * {@code RD}->{@code ROAD}, {@code AVE}->{@code AVENUE}). Running before the required-field check
     * means an address that is blank after normalization is rejected like a missing one.
     */
    private void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        ownerFieldsDto.setAddress(canonicalAddress(ownerFieldsDto.getAddress()));
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
     * brackets are stripped. The result must have 8 to 15 digits after the {@code '+'}. The E.164
     * string is written back onto the request so it is what gets stored and returned. Any value that
     * cannot form a valid E.164 number is rejected with a 400 response whose {@code errors} array
     * names {@code telephone}.
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
        String national;
        if (hasCountryCode) {
            national = digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            national = "61" + digits;
        }
        if (!national.matches("[0-9]{8,15}")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return "+" + national;
    }

    /**
     * Rejects creating an owner whose E.164 telephone is already used by any other owner. The
     * incoming value has already been converted to its E.164 form by {@link #normalizeTelephone};
     * each existing owner's stored telephone is converted the same way before comparison so numbers
     * that only differ in formatting still collide. A match results in a 409 response naming
     * {@code telephone}.
     */
    private void rejectDuplicateTelephone(OwnerFieldsDto ownerFieldsDto) {
        String telephone = ownerFieldsDto.getTelephone();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (telephone.equals(normalizeExisting(existing.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(telephone);
            }
        }
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
     * Rejects creating an owner whose {@code lastName} and {@code address} already belong to another
     * owner. Both fields are compared case-insensitively and with runs of whitespace collapsed to a
     * single space, so values that differ only in letter case or spacing still collide. When the
     * request opts in with {@code sharesHousehold} set to {@code true} the check is skipped, allowing
     * household members to share a name and address. A match results in a 409 response naming
     * {@code lastName} and {@code address}.
     */
    private void rejectDuplicateHousehold(OwnerFieldsDto ownerFieldsDto) {
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String lastName = normalizeForComparison(ownerFieldsDto.getLastName());
        String address = canonicalAddress(ownerFieldsDto.getAddress());
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (lastName.equals(normalizeForComparison(existing.getLastName()))
                && address.equals(canonicalAddress(existing.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(
                    ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
            }
        }
    }

    /**
     * Rejects creating an owner in a city that has reached capacity: a city that already contains
     * {@value #MAX_OWNERS_PER_CITY} or more owners cannot take another one. Existing owners are
     * counted with a case-insensitive match on {@code city}, the same way {@link #assignCustomerCode}
     * groups a city. A city at or over the limit results in a 409 response naming {@code city}.
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
        ownerFieldsDto.setEmail(trimmed.toLowerCase(Locale.ROOT));
    }

    /**
     * Resolves an owner's effective {@code registrationDate} on create and rolls it onto a business
     * day. When the caller supplies no value it defaults to the server's current date; a value
     * provided in the request is kept. The effective date, whether supplied or defaulted, must fall
     * on a business day: a Saturday or Sunday is rolled forward to the following Monday. The adjusted
     * date is written back onto the request so it is what gets stored and returned (in ISO
     * {@code YYYY-MM-DD} format) and what every value derived from the registration date is based on.
     */
    private void resolveRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate effective = ownerFieldsDto.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        ownerFieldsDto.setRegistrationDate(toBusinessDay(effective));
    }

    /**
     * Rolls a date forward onto a business day: a Saturday or Sunday is advanced to the following
     * Monday; a weekday is returned unchanged.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    /**
     * Assigns the owner's {@code customerCode} on create, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}
     * where {@code CITY3} is the upper-cased first three letters of {@code city}, {@code LAST3} is the
     * upper-cased first three letters of {@code lastName} and {@code NNNN} is a per-city 4-digit
     * zero-padded sequence equal to one more than the number of owners already in that city
     * (e.g. {@code 'SYD-SMI-0007'}).
     */
    private void assignCustomerCode(Owner owner) {
        String city = owner.getCity();
        String lastName = owner.getLastName();
        String cityPrefix = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String lastPrefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = 1;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence));
    }

    /**
     * Assigns the owner's {@code membershipNumber} on create, formatted
     * {@code '<customerCode>-M<YY>'} where {@code YY} is the last two digits of the
     * {@code registrationDate} year (e.g. {@code 'SMI-0007-M26'}). Assigned after
     * {@link #assignCustomerCode} and once {@code registrationDate} has been defaulted, so both
     * inputs are present.
     */
    private void assignMembershipNumber(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
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
     * Assigns the new owner's {@code householdId} when the request opts in with
     * {@code sharesHousehold} set to {@code true} and at least one existing owner already shares the
     * same last name and address (compared case-insensitively with runs of whitespace collapsed, the
     * same way {@link #rejectDuplicateHousehold} matches). The household's members and the new owner
     * all end up carrying one stable shared identifier: if any existing member already has a
     * {@code householdId} it is reused, otherwise a new one is derived from the normalized last name
     * and address. Any existing member that is missing the identifier is updated so the whole
     * household stays consistent. When the flag is absent, or set but no matching owner exists, no
     * identifier is assigned.
     */
    private void assignHousehold(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return;
        }
        String lastName = normalizeForComparison(owner.getLastName());
        String address = canonicalAddress(owner.getAddress());
        List<Owner> members = new ArrayList<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (lastName.equals(normalizeForComparison(existing.getLastName()))
                && address.equals(canonicalAddress(existing.getAddress()))) {
                members.add(existing);
            }
        }
        if (members.isEmpty()) {
            return;
        }
        String householdId = members.stream()
            .map(Owner::getHouseholdId)
            .filter(id -> id != null && !id.isBlank())
            .findFirst()
            .orElseGet(() -> newHouseholdId(lastName, address));
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
        }
    }

    /**
     * Derives a stable household identifier from the normalized last name and address: the first
     * twelve upper-case hex characters of the SHA-256 digest of {@code "<lastName>|<address>"}. Two
     * owners in the same household therefore derive the same value regardless of creation order.
     */
    private static String newHouseholdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((normalizedLastName + "|" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
