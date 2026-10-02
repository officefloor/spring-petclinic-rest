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
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
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
     * Syntactic check for an email address: a non-empty local part, a single {@code @}, and a
     * domain containing at least one dot, with no whitespace in any part.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Common street-type abbreviations expanded during address normalization. Keys are the
     * upper-cased abbreviation as it appears as a whole whitespace-delimited token; values are the
     * expanded form it is replaced with.
     */
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
        validateRequiredFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        String address = normalizeAddress(ownerFieldsDto.getAddress());
        owner.setAddress(address);
        if (Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            assignHousehold(owner, ownerFieldsDto.getLastName(), address);
        } else {
            rejectDuplicateHousehold(ownerFieldsDto.getLastName(), address);
        }
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        rejectDuplicateTelephone(telephone);
        owner.setTelephone(telephone);
        owner.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCustomerCode(generateCustomerCode(owner.getLastName()));
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
     * Rejects an owner payload that is missing or blank in any required field. Bean Validation
     * already rejects {@code null} values and some formats, but a whitespace-only value can still
     * satisfy a {@code @Size(min = 1)} constraint; this guard catches those remaining cases.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if any required field is missing or blank, carrying the
     *                                     name of every offending field
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
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
            throw new InvalidOwnerFieldsException(missingFields);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Normalizes a telephone number for owner creation into E.164 form. Spaces, dashes and brackets
     * are stripped. When the value carries a leading {@code '+'} the country code it supplies is kept;
     * otherwise the number is treated as a national Australian number, so a single leading {@code '0'}
     * is dropped and the {@code '+61'} country code is prepended. The result must be a {@code '+'}
     * followed by 8 to 15 digits. This E.164 string is what gets stored and returned.
     *
     * @param telephone the raw telephone value submitted by the client
     * @return the telephone in E.164 form, e.g. {@code +61412345678}
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number, yielding a
     *                                     400 response
     */
    private String normalizeTelephone(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Converts a raw telephone value to E.164 form, or returns {@code null} if it cannot form a valid
     * E.164 number. Spaces, dashes and brackets are removed; a leading {@code '+'} keeps the supplied
     * country code, while a bare national number has a single leading {@code '0'} dropped and the
     * {@code '+61'} country code prepended. A valid result is a {@code '+'} followed by 8 to 15 digits.
     *
     * @param telephone the raw telephone value, possibly {@code null}
     * @return the E.164 string, or {@code null} if the value is not a valid E.164 number
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String digits = (hasPlus ? cleaned.substring(1) : cleaned).replaceAll("\\D", "");
        String nationalSignificant = hasPlus
            ? digits
            : "61" + (digits.startsWith("0") ? digits.substring(1) : digits);
        if (!nationalSignificant.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + nationalSignificant;
    }

    /**
     * Normalizes an optional owner email address. A {@code null} value means the field was omitted and
     * is left unset. When a value is present it must be a syntactically valid address; the stored and
     * returned value is that address lower-cased.
     *
     * @param email the raw email value submitted by the client, or {@code null} if omitted
     * @return the lower-cased email, or {@code null} if none was supplied
     * @throws InvalidOwnerFieldsException if a value is present but not a syntactically valid address,
     *                                     yielding a 400 response
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        return email.toLowerCase(Locale.ROOT);
    }

    /**
     * Builds the owner's {@code customerCode}, formatted {@code '<LAST3>-<NNNN>'}. {@code LAST3} is
     * the upper-cased first three letters of the last name, and {@code NNNN} is a global 4-digit
     * zero-padded sequence equal to one more than the current number of owners (e.g. {@code 'SMI-0007'}).
     *
     * @param lastName the owner's last name
     * @return the formatted customer code
     */
    private String generateCustomerCode(String lastName) {
        String letters = lastName.replaceAll("[^\\p{L}]", "");
        String last3 = letters.substring(0, Math.min(3, letters.length())).toUpperCase(Locale.ROOT);
        int sequence = this.clinicService.findAllOwners().size() + 1;
        return String.format("%s-%04d", last3, sequence);
    }

    /**
     * Rejects creation of an owner whose E.164 telephone is already used by any other owner. Stored
     * telephones are converted to E.164 here as well so the comparison is independent of the format
     * each owner was originally saved with; a stored value that cannot form a valid E.164 number is
     * ignored rather than matched.
     *
     * @param telephone the E.164 telephone of the owner being created
     * @throws DuplicateOwnerTelephoneException if another owner already uses this telephone,
     *                                          yielding a 409 Conflict response
     */
    private void rejectDuplicateTelephone(String telephone) {
        boolean inUse = this.clinicService.findAllOwners().stream()
            .map(Owner::getTelephone)
            .map(this::toE164)
            .anyMatch(telephone::equals);
        if (inUse) {
            throw new DuplicateOwnerTelephoneException(telephone);
        }
    }

    /**
     * Rejects creation of an owner whose last name and address both match an existing owner's,
     * which marks them as belonging to the same household. Both the last name and the address are
     * compared case-insensitively with surrounding and internal whitespace collapsed, so values
     * that differ only in letter case or spacing are still treated as a match.
     *
     * @param lastName the last name of the owner being created
     * @param address  the address of the owner being created
     * @throws DuplicateOwnerHouseholdException if another owner already shares this last name and
     *                                          address, yielding a 409 Conflict response
     */
    private void rejectDuplicateHousehold(String lastName, String address) {
        if (!findHouseholdMembers(lastName, address).isEmpty()) {
            throw new DuplicateOwnerHouseholdException(lastName, address);
        }
    }

    /**
     * Finds every existing owner belonging to the household identified by the given last name and
     * address — that is, the owners the duplicate-household rule rejects and the {@code sharesHousehold}
     * opt-in joins. Both the last name and the address are compared case-insensitively with surrounding
     * and internal whitespace collapsed, so values that differ only in letter case or spacing still
     * match. This single definition of household membership is shared by {@link #rejectDuplicateHousehold}
     * and {@link #assignHousehold}.
     *
     * @param lastName the last name of the owner being created
     * @param address  the address of the owner being created
     * @return the existing owners in that household, or an empty list if there are none
     */
    private List<Owner> findHouseholdMembers(String lastName, String address) {
        String normalizedLastName = normalizeForComparison(lastName);
        String normalizedAddress = normalizeAddress(address);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForComparison(existing.getLastName()).equals(normalizedLastName)
                && normalizeAddress(existing.getAddress()).equals(normalizedAddress))
            .toList();
    }

    /**
     * Enrolls a newly created owner into the household of any existing owner that shares its last
     * name and address, so an owner that opts in via {@code sharesHousehold} is accepted instead of
     * rejected as a duplicate. Last name and address are compared case-insensitively with collapsed
     * whitespace. When at least one such owner already exists, every member of the household — the
     * existing owners and the new one — is assigned the same stable {@code householdId}; if a member
     * already carries one it is reused, otherwise a new identifier derived from the shared last name
     * and address is created and back-filled onto the existing members. When no owner yet lives at
     * that address under the same last name no identifier is assigned and the owner is created alone.
     *
     * @param newOwner the owner being created, whose {@code householdId} will be set on a match
     * @param lastName the last name of the owner being created
     * @param address  the address of the owner being created
     * @return the assigned {@code householdId}, or {@code null} if there was no household to join
     */
    private String assignHousehold(Owner newOwner, String lastName, String address) {
        List<Owner> members = findHouseholdMembers(lastName, address);
        if (members.isEmpty()) {
            return null;
        }
        String householdId = members.stream()
            .map(Owner::getHouseholdId)
            .filter(existing -> existing != null && !existing.isBlank())
            .findFirst()
            .orElseGet(() -> generateHouseholdId(normalizeForComparison(lastName), normalizeAddress(address)));
        for (Owner member : members) {
            if (member.getHouseholdId() == null || member.getHouseholdId().isBlank()) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
        }
        newOwner.setHouseholdId(householdId);
        return householdId;
    }

    /**
     * Derives a stable household identifier, formatted {@code 'HH-<HEX12>'}, where {@code HEX12} is
     * the first twelve upper-cased hex characters of the SHA-256 digest of the normalized last name
     * and address joined by a {@code '|'}. The same last name and address therefore always yield the
     * same identifier, so members of one household share a single value.
     *
     * @param normalizedLastName the household's last name, already normalized for comparison
     * @param normalizedAddress  the household's address, already normalized for comparison
     * @return the derived household identifier
     */
    private String generateHouseholdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((normalizedLastName + "|" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return "HH-" + hex.substring(0, 12).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Normalizes a text value for case- and whitespace-insensitive comparison: surrounding
     * whitespace is trimmed, every internal run of whitespace is collapsed to a single space, and
     * the result is lower-cased.
     *
     * @param value the value to normalize, possibly {@code null}
     * @return the normalized value, or an empty string if {@code value} is {@code null}
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes an owner address for storage, display and comparison. Surrounding whitespace is
     * trimmed, every internal run of whitespace is collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations appearing as whole tokens are expanded
     * ({@code ST} to {@code STREET}, {@code RD} to {@code ROAD}, {@code AVE} to {@code AVENUE}).
     * This normalized value is what gets stored and returned, and it is the form every address
     * comparison (household duplicate detection and the shared household id) uses.
     *
     * @param address the raw address value submitted by the client, possibly {@code null}
     * @return the normalized address, or an empty string if {@code address} is {@code null}
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
