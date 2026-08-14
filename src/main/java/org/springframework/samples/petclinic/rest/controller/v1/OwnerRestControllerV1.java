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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    /** Dedicated audit trail for owner-lifecycle side effects. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Email domains from disposable/throwaway providers, rejected on create. */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

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
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (isDisposableEmailDomain(ownerFieldsDto.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        // The owner must supply an address in EITHER form: a non-blank structured
        // 'addressLine1' or the flat 'address'. getAddress() prefers the structured
        // fields (composing line 1 with an optional line 2) and falls back to the flat
        // value; a blank result means neither form was provided. The composed value is
        // written back so the persisted flat 'address' stays consistent with it.
        String composedAddress = owner.getAddress();
        if (composedAddress == null || composedAddress.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(composedAddress);
        owner.setTelephone(normalizedTelephone);
        if (!owner.isPostcodeValidForCity()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // The household id is deterministic: it is derived purely from the owner's
        // (last name, postcode), so every owner at the same last name and postcode resolves
        // to the same value. It is assigned up front, before any duplicate detection, so the
        // identity key and the household check below both key off it.
        owner.setHouseholdId(householdId(owner.getLastName(), owner.getPostcode()));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        // Consolidated hard-duplicate detection: reject only when the new owner's WHOLE
        // identityKey (telephone|email|householdId) equals an existing owner's. With the
        // household id now computed up front, two members of one household still differ here
        // whenever their telephone or email differs, and so are both allowed.
        String identityKey = owner.getIdentityKey();
        if (this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()))) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Household duplicate: because the household is keyed on (last name, postcode), any
        // existing owner sharing this computed household id is the same household. A second
        // such owner is rejected as a household duplicate (409) unless it explicitly declares
        // 'sharesHousehold', which now only bypasses this block (the link is no longer created
        // here — the id is computed).
        List<Owner> householdMembers = findHouseholdMembers(owner.getHouseholdId());
        if (!householdMembers.isEmpty() && !sharesHousehold) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (countOwnersInCity(ownerFieldsDto.getCity()) >= 50) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (owner.getRegistrationDate() != null && owner.getRegistrationDate().isAfter(LocalDate.now())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveDate =
            owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        long ownersRegisteredToday = countOwnersRegisteredOn(registrationDate);
        if (ownersRegisteredToday >= 100) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        boolean bulkSignupWarning = ownersRegisteredToday > 80;
        HttpHeaders headers = new HttpHeaders();
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(customerCode(owner));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(bulkSignupWarning);
        // The household size (an input to the membership computation) keys off the computed
        // household id: this owner plus everyone already sharing it.
        owner.setHouseholdSize(householdMembers.size() + 1);
        // Soft-match ("possible duplicate"): a declared household member is not a suspected
        // duplicate, so only a non-declared create is scored. In practice a non-declared owner
        // sharing an existing owner's last name and postcode is already rejected above as a
        // household duplicate, so this now only ever confirms the owner is not a duplicate.
        Owner possibleDuplicate = sharesHousehold ? null : findPossibleDuplicate(owner);
        owner.setPossibleDuplicate(possibleDuplicate != null);
        owner.setPossibleDuplicateOf(possibleDuplicate == null ? null : possibleDuplicate.getId());
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(),
            owner.getMembershipNumber());
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
        String normalizedTelephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        if (normalizedTelephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(normalizedTelephone);
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
     * Normalize a telephone number to E.164 form. Spaces, dashes and brackets are
     * stripped. When a leading {@code '+'} and country code are present they are kept;
     * otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
     * is dropped from the national digits. The result must be a {@code '+'} followed
     * by 8 to 15 digits, and is returned as {@code +<digits>}; otherwise {@code null}
     * is returned to signal a bad request.
     */
    /**
     * Build the customer code for a newly created owner, formatted
     * {@code '<REGION>-<HASH8>'} where {@code REGION} is the region derived from the
     * owner's postcode (falling back to the city, or {@code 'UNKNOWN'}, via the same
     * derivation the locality uses), and {@code HASH8} is the first eight upper-case
     * hex characters of the SHA-256 of the owner's normalized telephone concatenated
     * with its last name. This identity carries no sequence number.
     */
    private String customerCode(Owner owner) {
        String region = owner.getLocality();
        String hash8 = customerCodeHash(owner.getTelephone(), owner.getLastName());
        String base = region + "-" + hash8;
        java.util.Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(java.util.Objects::nonNull)
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
     * Compute the {@code HASH8} portion of a customer code: the first eight upper-case
     * hex characters of the SHA-256 digest of {@code telephone + lastName} (each
     * treated as empty when {@code null}), already in their normalized forms.
     */
    private String customerCodeHash(String telephone, String lastName) {
        String key = (telephone == null ? "" : telephone) + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Count the existing owners whose city matches the given one, compared after
     * collapsing whitespace and lower-casing. Used to enforce the per-city capacity
     * cap: a city that already holds this many owners is at capacity.
     */
    private long countOwnersInCity(String city) {
        String normalizedCity = normalizeForHousehold(city);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getCity()).equals(normalizedCity))
            .count();
    }

    /**
     * Count the existing owners whose registration date falls on the given day. Used to
     * enforce the per-day create limit: once this many owners have been registered today,
     * the day is at capacity and further creates are rejected.
     */
    private long countOwnersRegisteredOn(LocalDate day) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * Roll a registration date forward to the next business day: a date falling on a
     * Saturday or Sunday is advanced to the following Monday, while a weekday is
     * returned unchanged.
     */
    private LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(8 - dayOfWeek.getValue());
        }
        return date;
    }

    /**
     * Find the existing owners that already belong to the same household as the given
     * computed household id. Because the household id is derived deterministically from
     * the owner's (last name, postcode), this is exactly the set of other owners sharing
     * that last name and postcode.
     */
    private List<Owner> findHouseholdMembers(String householdId) {
        if (householdId == null) {
            return List.of();
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
    }

    /**
     * Find an existing owner that this new owner softly duplicates: one sharing the same
     * last name (compared case-insensitively after whitespace normalization) and the same
     * postcode, but with a different telephone. Returns the lowest-id such owner, or
     * {@code null} when the new owner has no postcode or no soft match exists. Owners
     * whose telephone equals the new owner's are excluded, as those are governed by the
     * hard-duplicate (identityKey) check rather than this soft match.
     */
    private Owner findPossibleDuplicate(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return null;
        }
        String normalizedLastName = normalizeForHousehold(owner.getLastName());
        String telephone = owner.getTelephone();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeForHousehold(existing.getLastName()).equals(normalizedLastName)
                && postcode.equals(existing.getPostcode())
                && !java.util.Objects.equals(telephone, existing.getTelephone()))
            .min(java.util.Comparator.comparing(Owner::getId))
            .orElse(null);
    }

    /**
     * Count the existing owners that already share both the given first and last
     * name, compared case-insensitively. Used to populate an owner's namesake count
     * at creation time, reflecting how many owners with the same name existed before
     * this one was created.
     */
    private int countNamesakes(String firstName, String lastName) {
        String normalizedFirstName = normalizeName(firstName);
        String normalizedLastName = normalizeName(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeName(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    /**
     * Build the deterministic identifier shared by all owners of one household: the first
     * 12 hex characters of the SHA-256 digest of {@code normalizedLastName + '|' + postcode}.
     * Because it depends only on the (last name, postcode) pair, every owner at the same last
     * name and postcode resolves to the same value regardless of creation order.
     */
    private String householdId(String lastName, String postcode) {
        String key = normalizeForHousehold(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String normalizeForHousehold(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** True when the email's domain is on the disposable-provider blocklist. */
    private boolean isDisposableEmailDomain(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
    }

    private String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
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
        if (!hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Validate the national-number length of an E.164 number (the digits, without the
     * leading {@code '+'}) against its country code. Australia ({@code '+61'}) requires
     * exactly 9 national digits and North America ({@code '+1'}) exactly 10; country
     * codes without a known rule are accepted as-is.
     */
    private boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
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
