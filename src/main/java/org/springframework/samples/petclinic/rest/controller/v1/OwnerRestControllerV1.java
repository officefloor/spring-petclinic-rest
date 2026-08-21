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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidRequestException;
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
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Syntactic email validation: a non-empty local part and a dotted domain, with no whitespace.
     * Matches addresses such as {@code test.user@example.com} and rejects strings without an '@'
     * and a dotted domain (e.g. {@code not-an-email}).
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
            + "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$");

    /** Default country code assumed for national numbers that carry no explicit '+' prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Separators (spaces, dashes and brackets) stripped from a telephone before parsing. */
    private static final Pattern TELEPHONE_SEPARATORS = Pattern.compile("[\\s()\\[\\]-]");

    /** A valid E.164 body: a leading '+' followed by 8 to 15 digits. */
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[0-9]{8,15}$");

    /** Common street-type abbreviations expanded (on upper-cased tokens) during address normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

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
        List<String> missingFields = new ArrayList<>();
        if (!StringUtils.hasText(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        // Normalize the address up front; the required-field check rejects it when it is blank
        // after normalization (e.g. a whitespace-only value collapses to the empty string).
        String normalizedAddress = normalizeAddress(ownerFieldsDto.getAddress());
        if (!StringUtils.hasText(normalizedAddress)) {
            missingFields.add("address");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (!StringUtils.hasText(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new InvalidRequestException(missingFields);
        }
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        // Store (and later return) the normalized address computed above.
        owner.setAddress(normalizedAddress);
        // Normalize the telephone into E.164 form (reject with 400 when it cannot form a valid one).
        String normalizedTelephone = normalizeTelephone(owner.getTelephone());
        owner.setTelephone(normalizedTelephone);
        // Normalize the (optional) email: reject a syntactically invalid address, otherwise store it lower-cased.
        owner.setEmail(normalizeEmail(owner.getEmail()));
        // Default the (optional) registration date to the server's current date when none was supplied.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        // Reject the request if the E.164 telephone is already used by another owner.
        if (!this.clinicService.findOwnerByTelephone(normalizedTelephone).isEmpty()) {
            throw new DuplicateTelephoneException(normalizedTelephone);
        }
        // Reject the request if another owner already shares the same last name and address
        // (compared case-insensitively with collapsed whitespace), unless the caller opts in
        // by setting 'sharesHousehold' true. When they do opt in and an existing household is
        // matched, assign a stable shared 'householdId' to the new owner and backfill it onto
        // every existing member that does not yet carry it.
        List<Owner> householdMembers = findHouseholdMembers(owner.getLastName(), owner.getAddress());
        if (!householdMembers.isEmpty()) {
            if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
                throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
            }
            String householdId = generateHouseholdId(owner.getLastName(), owner.getAddress());
            owner.setHouseholdId(householdId);
            for (Owner member : householdMembers) {
                if (!householdId.equals(member.getHouseholdId())) {
                    member.setHouseholdId(householdId);
                    this.clinicService.saveOwner(member);
                }
            }
        }
        // Record how many existing owners already share this first name and last name
        // (compared case-insensitively) before this owner is created.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        // Assign the customer code '<LAST3>-<NNNN>' before persisting.
        owner.setCustomerCode(generateCustomerCode(owner.getLastName()));
        // Assign the membership number '<customerCode>-M<YY>' where YY is the last two
        // digits of the registration date year (e.g. 'SMI-0007-M26').
        owner.setMembershipNumber(generateMembershipNumber(owner.getCustomerCode(), owner.getRegistrationDate()));
        this.clinicService.saveOwner(owner);
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
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        currentOwner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        this.clinicService.saveOwner(currentOwner);
        return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
    }

    /**
     * Generate a customer code formatted {@code <LAST3>-<NNNN>}, where {@code LAST3} is the
     * upper-cased first three letters of {@code lastName} and {@code NNNN} is a global 4-digit
     * zero-padded sequence equal to one more than the current number of owners (e.g. {@code SMI-0007}).
     *
     * @param lastName the owner's last name
     * @return the generated customer code
     */
    private String generateCustomerCode(String lastName) {
        String prefix = lastName.length() >= 3 ? lastName.substring(0, 3) : lastName;
        int sequence = this.clinicService.findAllOwners().size() + 1;
        return String.format("%s-%04d", prefix.toUpperCase(Locale.ROOT), sequence);
    }

    /**
     * Generate a membership number formatted {@code <customerCode>-M<YY>}, where {@code YY} is the
     * last two digits of the {@code registrationDate} year (e.g. {@code SMI-0007-M26}).
     *
     * @param customerCode     the owner's already-assigned customer code
     * @param registrationDate the owner's registration date
     * @return the generated membership number
     */
    private String generateMembershipNumber(String customerCode, LocalDate registrationDate) {
        String yy = String.format("%02d", registrationDate.getYear() % 100);
        return String.format("%s-M%s", customerCode, yy);
    }

    /**
     * Count the existing owners that share the given first name and last name, comparing each
     * case-insensitively (trimmed, with internal whitespace collapsed). The incoming owner is not
     * yet persisted, so it is not included in the count.
     *
     * @param firstName the incoming owner's first name
     * @param lastName  the incoming owner's last name
     * @return the number of existing namesakes
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeForComparison(firstName);
        String normalizedLastName = normalizeForComparison(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeForComparison(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    /**
     * Find the existing owners that live in the same household as the given values, i.e. those that
     * share both the last name and the address when each is compared case-insensitively with
     * collapsed whitespace.
     *
     * @param lastName the incoming owner's last name
     * @param address  the incoming owner's address
     * @return the matching existing owners (possibly empty)
     */
    private List<Owner> findHouseholdMembers(String lastName, String address) {
        String normalizedLastName = normalizeForComparison(lastName);
        String normalizedAddress = normalizeForComparison(address);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getLastName()).equals(normalizedLastName)
                && normalizeForComparison(existing.getAddress()).equals(normalizedAddress))
            .toList();
    }

    /**
     * Generate a stable shared household identifier for the given last name and address. The value is
     * derived deterministically from the normalized (case-insensitive, whitespace-collapsed) last name
     * and address, so every owner joining the same household resolves to the same identifier. It is the
     * first 12 upper-case hex characters of the SHA-256 digest of {@code <lastName>|<address>}.
     *
     * @param lastName the household's last name
     * @param address  the household's address
     * @return the stable household identifier
     */
    private String generateHouseholdId(String lastName, String address) {
        String key = normalizeForComparison(lastName) + "|" + normalizeForComparison(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalize an owner address for storage: trim and collapse every internal run of whitespace to a
     * single space, upper-case the result and expand common street-type abbreviations token by token
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} value normalizes
     * to the empty string. The result is the canonical form that is stored, returned and used for every
     * address comparison (household duplicate detection and the shared household id).
     *
     * @param address the raw address, may be {@code null}
     * @return the normalized address
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
     * Normalize a value for identity comparison: trim, collapse every internal run of whitespace to a
     * single space and lower-case the result. A {@code null} value normalizes to the empty string.
     *
     * @param value the raw value, may be {@code null}
     * @return the normalized value
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalize an optional owner email address. A blank/absent value is treated as "not provided"
     * and returns {@code null}. When present, the value must be a syntactically valid address; if it
     * is not, an {@link InvalidRequestException} is thrown (mapped to 400 Bad Request). A valid value
     * is returned lower-cased.
     *
     * @param email the raw email from the request payload, may be {@code null}
     * @return the lower-cased email, or {@code null} when none was provided
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidRequestException(List.of("email"));
        }
        return email.toLowerCase(Locale.ROOT);
    }

    /**
     * Normalize a telephone into E.164 form. Spaces, dashes and brackets are stripped. A value that
     * already carries a leading '+' keeps its country code; otherwise the default country code
     * ({@code +61}) is assumed and a single leading '0' is dropped from the national digits. The
     * result must be a '+' followed by 8 to 15 digits, otherwise an {@link InvalidRequestException}
     * is thrown (mapped to 400 Bad Request).
     *
     * @param telephone the raw telephone from the request payload
     * @return the E.164 string (e.g. {@code +61412345678})
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = TELEPHONE_SEPARATORS.matcher(telephone).replaceAll("");
        String candidate;
        if (cleaned.startsWith("+")) {
            candidate = cleaned;
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            candidate = "+" + DEFAULT_COUNTRY_CODE + national;
        }
        if (!E164_PATTERN.matcher(candidate).matches()) {
            throw new InvalidRequestException(List.of("telephone"));
        }
        return candidate;
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
