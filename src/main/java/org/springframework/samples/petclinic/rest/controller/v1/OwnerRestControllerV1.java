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
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
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
     * owner's id, customer code and registration date, so the create side-effect can be audited
     * independently of the application log.
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
     * Expansions of the common street-type abbreviations recognised by {@link #normalizeAddress}.
     * Keys and values are upper-case, matching the casing the address is folded to before expansion.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

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
        owner.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            joinHousehold(owner);
        } else {
            requireUniqueHousehold(owner.getLastName(), owner.getAddress());
        }
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        requireUniqueTelephone(telephone);
        owner.setTelephone(telephone);
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        owner.setCustomerCode(generateCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
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
        long dayOwnerCount = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (dayOwnerCount >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(registrationDate);
        }
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
     * Generates the customer code for a newly created owner, formatted
     * {@code '<CITY3>-<LAST3>-<NNNN>'}. {@code CITY3} is the upper-cased first three letters of the
     * city, {@code LAST3} the upper-cased first three letters of the last name, and {@code NNNN} a
     * per-city, 4-digit zero-padded sequence equal to one more than the number of owners already in
     * that city (e.g. {@code 'LON-SMI-0007'}).
     *
     * @param city the owner's city
     * @param lastName the owner's last name
     * @return the generated customer code
     */
    private String generateCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getCity(), city))
            .count() + 1;
        return String.format("%s-%s-%04d", city3, last3, sequence);
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
     * it contains 8 to 15 digits after the {@code '+'}.
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
        return "+" + digits;
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
     * Rejects a telephone number whose E.164 form is already used by an existing owner. This enforces
     * that no two owners share the same E.164 telephone. Existing values are compared by their E.164
     * form; a stored value that cannot form a valid E.164 number is compared as-is.
     *
     * @param telephone the normalized (E.164) telephone of the owner being created
     * @throws DuplicateTelephoneException if another owner already has this E.164 telephone
     */
    private void requireUniqueTelephone(String telephone) {
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .filter(existing -> existing != null)
            .map(existing -> {
                String e164 = toE164(existing);
                return e164 != null ? e164 : existing;
            })
            .anyMatch(telephone::equals);
        if (inUse) {
            throw new DuplicateTelephoneException(telephone);
        }
    }

    /**
     * Rejects an owner whose last name and address match those of an existing owner, where both are
     * compared case-insensitively and with runs of whitespace collapsed to a single space (a shared
     * household). The caller skips this check when the request opts in via {@code sharesHousehold}.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @throws DuplicateHouseholdException if another owner shares the same last name and address
     */
    private void requireUniqueHousehold(String lastName, String address) {
        if (!findHouseholdMembers(lastName, address).isEmpty()) {
            throw new DuplicateHouseholdException(lastName, address);
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
     * @param owner the owner being created, whose {@code householdId} is set when a household is joined
     */
    private void joinHousehold(Owner owner) {
        List<Owner> members = findHouseholdMembers(owner.getLastName(), owner.getAddress());
        if (members.isEmpty()) {
            return;
        }
        String householdId = computeHouseholdId(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
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
