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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final AddressNormalizer addressNormalizer;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 AddressNormalizer addressNormalizer) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.addressNormalizer = addressNormalizer;
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
        String address = addressNormalizer.normalize(owner.getAddress());
        if (address.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setAddress(address);
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        owner.setTelephone(telephone);
        if (!owner.isPostcodeValid()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(toBusinessDay(effectiveDate));
        if (isDailyLimitReached(owner.getRegistrationDate())) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (sharesHousehold) {
            owner.setHouseholdId(householdId(owner.getLastName(), owner.getAddress()));
        }
        if (isDuplicate(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (isCityAtCapacity(owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        owner.setCustomerCode(customerCode(owner));
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        owner.setBulkSignupWarning(isBulkSignup(owner.getRegistrationDate()));
        owner.setHouseholdSize(householdSize(owner.getHouseholdId()));
        this.clinicService.saveOwner(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Build the customer code for a newly created owner, formatted {@code '<REGION>-<HASH8>'}
     * where REGION is the region derived from the owner's postcode (or {@link Owner#UNKNOWN_REGION}
     * when the postcode is absent or maps to no known region) and HASH8 is the first 8 upper-case
     * hex characters of the SHA-256 of the normalized (E.164) telephone concatenated with the
     * lastName (e.g. {@code 'NSW-3C1A9F2B'}). This is the single source of the owner's identity:
     * every value built from the customer code (the membership number and its check digit) and the
     * owner's locality are derived from it.
     */
    private String customerCode(Owner owner) {
        String region = owner.getRegion() != null ? owner.getRegion() : Owner.UNKNOWN_REGION;
        String hash8 = shaHexUpper(owner.getTelephone() + owner.getLastName()).substring(0, 8);
        return region + "-" + hash8;
    }

    /**
     * Whether the per-day owner creation limit has been reached for the given business day, i.e.
     * 100 or more owners already carry that {@code registrationDate}. The day is the adjusted
     * (business-day) registration date of the owner being created, so owners are counted per
     * business day. When true, a new owner must be rejected with {@code 429 Too Many Requests}.
     */
    private boolean isDailyLimitReached(LocalDate day) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
        return count >= 100;
    }

    /**
     * Whether creating this owner pushes the day's total past the bulk-signup threshold, i.e.
     * more than 80 owners have already been created for the given business day. Counts the owners
     * already carrying that {@code registrationDate} (the same accumulation as the daily-limit
     * rule); when that count exceeds 80 the newly created owner is flagged with a bulk-signup
     * warning.
     */
    private boolean isBulkSignup(LocalDate day) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
        return count > 80;
    }

    /**
     * Roll a registration date forward onto a business day: when it falls on a Saturday or Sunday,
     * advance it to the following Monday; a weekday is returned unchanged.
     */
    private LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Whether the given city has reached its owner capacity, i.e. it already contains 50 or
     * more owners. Cities are compared case-insensitively.
     */
    private boolean isCityAtCapacity(String city) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
        return count >= 50;
    }

    /**
     * The number of existing owners that already share the given firstName and lastName,
     * compared case-insensitively (and with surrounding whitespace trimmed and internal
     * whitespace runs collapsed). Counted over the owners present before this create.
     */
    private int namesakeCount(String firstName, String lastName) {
        String candidateFirstName = normalizeName(firstName);
        String candidateLastName = normalizeName(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(candidateFirstName)
                && normalizeName(existing.getLastName()).equals(candidateLastName))
            .count();
    }

    /**
     * The number of members in this owner's household after this create: the count of existing
     * owners already carrying the same non-null {@code householdId} plus one for the owner being
     * created. An owner with no household ({@code householdId == null}) is a household of one.
     * Owners sharing a household are matched by their assigned {@code householdId}, so the count
     * does not depend on the order in which household members were created.
     */
    private int householdSize(String householdId) {
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(other -> householdId.equals(other.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * The number of national (subscriber) digits required for each known country calling
     * code, keyed by the code's digits (without the leading '+'). A number whose country
     * code appears here must carry exactly this many digits after the code, e.g. '+61'
     * requires 9 national digits and '+1' requires 10.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY = Map.of(
        "1", 10,
        "61", 9);

    /**
     * Convert a raw telephone input to its E.164 representation, or {@code null} if it
     * cannot form a valid E.164 number.
     *
     * <p>A leading '+' and country code are kept when present; otherwise country code
     * '+61' is assumed and a single leading '0' is dropped from the national digits.
     * Spaces, dashes and brackets (indeed any non-digit) are stripped. The result must
     * carry 8 to 15 digits after the '+', and — for a recognised country calling code —
     * exactly the national-digit count that code requires (see
     * {@link #NATIONAL_DIGITS_BY_COUNTRY}); otherwise it is rejected as invalid.
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        String e164;
        if (hasCountryCode) {
            e164 = digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            e164 = "61" + digits;
        }
        if (e164.length() < 8 || e164.length() > 15) {
            return null;
        }
        if (!hasValidNationalLength(e164)) {
            return null;
        }
        return "+" + e164;
    }

    /**
     * Whether the E.164 digit string (no leading '+') carries the national-number length
     * required by its country calling code. The longest matching known code wins; a number
     * whose country code is not recognised passes this check (its length is governed only by
     * the general 8-15 digit bound in {@link #toE164}).
     */
    private boolean hasValidNationalLength(String e164) {
        return NATIONAL_DIGITS_BY_COUNTRY.entrySet().stream()
            .filter(entry -> e164.startsWith(entry.getKey()))
            .max(Comparator.comparingInt(entry -> entry.getKey().length()))
            .map(entry -> e164.length() - entry.getKey().length() == entry.getValue())
            .orElse(true);
    }

    /**
     * Whether the candidate owner duplicates an owner that already exists, and so must be
     * rejected with {@code 409 Conflict}. All duplicate detection is consolidated into a single
     * derived {@link Owner#getIdentityKey() identityKey} — the normalized telephone, email (or
     * empty) and householdId (or empty) joined with {@code '|'} — and an owner is a duplicate only
     * when its WHOLE identity key equals that of an existing owner. Because the telephone is part
     * of the key, two members of the same household with different telephones have different
     * identity keys and are both allowed; only an exact full-key match is a duplicate.
     */
    private boolean isDuplicate(Owner owner) {
        String identityKey = owner.getIdentityKey();
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    /**
     * Normalize an owner's name for household comparison: trim, collapse internal whitespace runs
     * to a single space and lower-case. A null value normalizes to the empty string.
     */
    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * A stable shared identifier for the household formed by a given lastName and address.
     * Derived deterministically from the normalized (case-insensitive, whitespace-collapsed)
     * lastName and address, so every owner sharing the same household is assigned the same
     * value regardless of the order in which they are created. Formatted as the first 12
     * upper-case hex characters of the SHA-256 of {@code '<lastName>|<address>'}.
     */
    private String householdId(String lastName, String address) {
        String key = normalizeName(lastName) + "|" + addressNormalizer.normalize(address);
        return shaHexUpper(key).substring(0, 12);
    }

    /**
     * The SHA-256 digest of {@code input}'s UTF-8 bytes, rendered as an upper-case hexadecimal
     * string (two hex characters per digest byte, so 64 characters in all). This is the single
     * place the hashing-and-hex-encoding is performed; callers that need a shorter opaque token
     * take a prefix of the result (e.g. {@link #householdId} keeps the first 12 characters and
     * {@link #customerCode} keeps the first 8).
     */
    private String shaHexUpper(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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
