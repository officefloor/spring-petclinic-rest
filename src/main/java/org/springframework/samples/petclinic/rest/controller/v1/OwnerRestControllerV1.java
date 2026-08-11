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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.springframework.samples.petclinic.rest.advice.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.advice.DailyRegistrationLimitException;
import org.springframework.samples.petclinic.rest.advice.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.advice.MissingOwnerFieldsException;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
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

    /**
     * The request header carrying a client-supplied idempotency token for owner creation. When a
     * create repeats with a token already seen, the originally created owner is returned with 200
     * instead of creating a duplicate.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per idempotency key, the id of the owner created by the first request that carried
     * that key, so a repeat with the same key resolves back to the original owner instead of
     * creating a duplicate.
     */
    private final java.util.Map<String, Integer> idempotentCreates = new java.util.concurrent.ConcurrentHashMap<>();

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
        boolean bulkSignupWarning = isBulkSignupWarning();
        List<OwnerDto> ownerDtos = ownerMapper.toOwnerDtoCollection(owners);
        ownerDtos.forEach(ownerDto -> ownerDto.setBulkSignupWarning(bulkSignupWarning));
        return new ResponseEntity<>(ownerDtos, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> getOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarning());
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        // Idempotent create: when the request carries an 'Idempotency-Key' already seen, return the
        // owner originally created for that key with 200, instead of creating a duplicate.
        String idempotencyKey = currentIdempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    OwnerDto existingDto = ownerMapper.toOwnerDto(existing);
                    existingDto.setBulkSignupWarning(isBulkSignupWarning());
                    return new ResponseEntity<>(existingDto, HttpStatus.OK);
                }
            }
        }
        // Prefer the structured address fields when present; the flat 'address' input remains accepted
        // for backward compatibility. When structured, the stored/returned address is the composed
        // normalized addressLine1 (with a single space and the normalized addressLine2 appended when
        // addressLine2 is present); otherwise it is the normalized flat 'address'.
        boolean structured = !isBlank(ownerFieldsDto.getAddressLine1());
        String addressLine1 = normalizeAddress(ownerFieldsDto.getAddressLine1());
        String addressLine2 = normalizeAddress(ownerFieldsDto.getAddressLine2());
        String address;
        if (structured) {
            address = addressLine2.isEmpty() ? addressLine1 : addressLine1 + " " + addressLine2;
        } else {
            address = normalizeAddress(ownerFieldsDto.getAddress());
        }
        List<String> missingFields = new ArrayList<>();
        if (isBlank(ownerFieldsDto.getFirstName())) {
            missingFields.add("firstName");
        }
        if (isBlank(ownerFieldsDto.getLastName())) {
            missingFields.add("lastName");
        }
        if (isBlank(address)) {
            missingFields.add("address");
        }
        if (isBlank(ownerFieldsDto.getCity())) {
            missingFields.add("city");
        }
        if (isBlank(ownerFieldsDto.getTelephone())) {
            missingFields.add("telephone");
        }
        if (!missingFields.isEmpty()) {
            throw new MissingOwnerFieldsException(missingFields);
        }
        String telephone = normalizeTelephone(ownerFieldsDto.getTelephone());
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setAddress(address);
        owner.setAddressLine1(structured ? addressLine1 : null);
        owner.setAddressLine2(structured && !addressLine2.isEmpty() ? addressLine2 : null);
        owner.setTelephone(telephone);
        validateEmailDomain(owner.getEmail());
        validatePostcode(owner.getPostcode(), owner.getRegion());
        // The household is keyed deterministically on (lastName, postcode): every owner receives the
        // same stable householdId as anyone sharing its normalized last name and postcode, without any
        // explicit link. This computed value drives duplicate detection and the household size.
        owner.setHouseholdId(householdIdFor(owner.getLastName(), owner.getPostcode()));
        // A new owner may join an existing household (same computed householdId); rather than being
        // rejected, the joiner is admitted with a capped membership level (see below).
        // Remaining duplicate detection is consolidated into the single derived identity key: a create
        // is rejected when the new owner's whole identity key equals an existing owner's.
        if (isIdentityKeyInUse(owner.getIdentityKey())) {
            throw new DuplicateIdentityException(owner.getIdentityKey());
        }
        if (isCityAtCapacity(owner.getCity())) {
            throw new CityAtCapacityException(owner.getCity());
        }
        if (owner.getRegistrationDate() != null && owner.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(owner.getRegistrationDate());
        }
        LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        LocalDate registrationDate = toBusinessDay(effectiveDate);
        if (isDailyRegistrationLimitReached(registrationDate)) {
            throw new DailyRegistrationLimitException(registrationDate);
        }
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(deduplicateCustomerCode(customerCodeFor(owner)));
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(householdSizeAfterCreate(owner.getHouseholdId()));
        owner.setMembershipLevel(cappedMembershipLevel(owner));
        // A declared household member (one that set 'sharesHousehold' to join an existing household) is
        // not a suspected duplicate; otherwise fall back to the soft last-name + postcode match.
        Integer possibleDuplicateOf = sharesHousehold ? null : possibleDuplicateOf(owner);
        owner.setPossibleDuplicate(possibleDuplicateOf != null);
        owner.setPossibleDuplicateOf(possibleDuplicateOf);
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            idempotentCreates.put(idempotencyKey, owner.getId());
        }
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(),
            owner.getMembershipNumber());
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarning());
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
        currentOwner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
        this.clinicService.saveOwner(currentOwner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(currentOwner);
        ownerDto.setBulkSignupWarning(isBulkSignupWarning());
        return new ResponseEntity<>(ownerDto, HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> deleteOwner(Integer ownerId) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Soft delete: retain the row, flag it deleted. The owner remains readable via GET, but
        // duplicate/identity detection on create ignores owners flagged deleted.
        owner.setDeleted(true);
        this.clinicService.saveOwner(owner);
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Reads the {@code Idempotency-Key} header from the current request, or {@code null} when the
     * header is absent, blank, or there is no bound request. A present, non-blank value is trimmed.
     *
     * @return the trimmed idempotency key, or {@code null} when none was supplied
     */
    private static String currentIdempotencyKey() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttributes)) {
            return null;
        }
        String key = servletAttributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        return isBlank(key) ? null : key.trim();
    }

    /**
     * The set of disposable email domains that owners may not register with. Compared
     * case-insensitively against the domain part of the supplied email.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS = java.util.Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Rejects an owner whose email domain is on the disposable-domain blocklist
     * ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}). Email is optional:
     * a {@code null} or blank value, or one without an {@code '@'} domain part, is accepted here (the
     * {@code @Email} Bean Validation constraint governs format). The domain is compared
     * case-insensitively; the stored email is already lower-cased.
     *
     * @param email the owner's email (may be {@code null})
     * @throws DisposableEmailDomainException if the email's domain is on the blocklist
     */
    private static void validateEmailDomain(String email) {
        if (isBlank(email)) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(java.util.Locale.ROOT);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }

    /**
     * The fixed, region-keyed inclusive 4-digit postcode ranges. A region absent from this table
     * (locality {@code UNKNOWN}) imposes no range, so any 4-digit postcode is accepted there.
     */
    private static final java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates a supplied {@code postcode} against the owner's city region. Postcode is optional:
     * a {@code null} or blank value is accepted (validation only applies WHEN PRESENT). When
     * present, the value has already passed the 4-digit format check (Bean Validation on the
     * request body); this enforces that it falls within the inclusive range for the city's region
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region imposes no range.
     *
     * @param postcode the supplied postcode (may be {@code null})
     * @param region the owner's locality/region derived from its city
     * @throws InvalidPostcodeException if the postcode is out of range for the region
     */
    private static void validatePostcode(String postcode, String region) {
        if (isBlank(postcode)) {
            return;
        }
        int[] range = REGION_POSTCODES.get(region);
        if (range == null) {
            return;
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
        }
        catch (NumberFormatException e) {
            throw new InvalidPostcodeException(postcode);
        }
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }

    /**
     * Normalizes a street address into the canonical form stored on and returned for a newly
     * created owner. A {@code null} value becomes the empty string; otherwise surrounding
     * whitespace is trimmed, internal runs of whitespace are collapsed to a single space, the
     * text is upper-cased, and common street-type abbreviations are expanded on a whole-word
     * basis ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The normalized
     * value is what gets persisted, echoed back, and used for every address comparison (household
     * duplicate detection and the shared household identifier).
     *
     * @param rawAddress the address value as supplied by the client
     * @return the normalized address (possibly empty, never {@code null})
     */
    private static String normalizeAddress(String rawAddress) {
        if (rawAddress == null) {
            return "";
        }
        String collapsed = rawAddress.trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            switch (token) {
                case "ST" -> token = "STREET";
                case "RD" -> token = "ROAD";
                case "AVE" -> token = "AVENUE";
                default -> { }
            }
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Builds the customer code assigned to a newly created owner. The code is formatted
     * {@code <REGION>-<HASH8>} where {@code REGION} is the region derived from the owner's postcode
     * (see {@link Owner#getRegion()}) and {@code HASH8} is the first 8 upper-case hex characters of
     * the SHA-256 digest of the owner's normalized (E.164) telephone concatenated with its last
     * name (e.g. {@code NSW-1A2B3C4D}). No sequence number is involved, so the code depends only on
     * the owner's own region and identity.
     *
     * @param owner the owner being created, with telephone already normalized and postcode/city set
     * @return the customer code to assign
     */
    private static String customerCodeFor(Owner owner) {
        return owner.getRegion() + "-" + telephoneNameHash(owner.getTelephone(), owner.getLastName());
    }

    /**
     * De-duplicates a freshly computed customer code against the codes already assigned to existing
     * owners. When no existing owner carries the given code it is returned unchanged; otherwise the
     * smallest suffix {@code -<n>} with {@code n >= 2} that yields a code no existing owner holds is
     * appended, guaranteeing distinct owners always receive distinct customer codes.
     *
     * @param customerCode the customer code computed for the owner being created
     * @return the de-duplicated customer code, unique among existing owners
     */
    private String deduplicateCustomerCode(String customerCode) {
        java.util.Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        if (!existing.contains(customerCode)) {
            return customerCode;
        }
        int n = 2;
        while (existing.contains(customerCode + "-" + n)) {
            n++;
        }
        return customerCode + "-" + n;
    }

    /**
     * Computes the {@code HASH8} segment of the customer code: the first 8 upper-case hex
     * characters of the SHA-256 digest of the normalized (E.164) telephone concatenated with the
     * last name.
     *
     * @param normalizedTelephone the owner's telephone in E.164 form
     * @param lastName the owner's last name
     * @return the 8-character upper-case hex hash
     */
    private static String telephoneNameHash(String normalizedTelephone, String lastName) {
        String key = (normalizedTelephone == null ? "" : normalizedTelephone)
            + (lastName == null ? "" : lastName);
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 8).toUpperCase(java.util.Locale.ROOT);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a telephone into E.164 form. Spaces, dashes and brackets are stripped. A value
     * already carrying a leading {@code '+'} keeps its country code; otherwise country code
     * {@code +61} is assumed and a single leading {@code '0'} is dropped from the national digits.
     * The result must carry a leading {@code '+'} followed by 8 to 15 digits, and the length of
     * the national number must match the country code: {@code +61} requires 9 national digits and
     * {@code +1} requires 10.
     *
     * @param rawTelephone the telephone value as supplied by the client
     * @return the telephone in E.164 form (e.g. {@code +61412345678})
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private static String normalizeTelephone(String rawTelephone) {
        if (rawTelephone == null) {
            throw new InvalidTelephoneException(rawTelephone);
        }
        String cleaned = rawTelephone.replaceAll("[\\s\\-()]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            String digits = cleaned.substring(1);
            if (!digits.matches("[0-9]+")) {
                throw new InvalidTelephoneException(rawTelephone);
            }
            e164 = "+" + digits;
        } else {
            if (!cleaned.matches("[0-9]+")) {
                throw new InvalidTelephoneException(rawTelephone);
            }
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = "+61" + national;
        }
        int digitCount = e164.length() - 1;
        if (digitCount < 8 || digitCount > 15) {
            throw new InvalidTelephoneException(rawTelephone);
        }
        if (e164.startsWith("+61")) {
            if (e164.length() - "+61".length() != 9) {
                throw new InvalidTelephoneException(rawTelephone);
            }
        }
        else if (e164.startsWith("+1")) {
            if (e164.length() - "+1".length() != 10) {
                throw new InvalidTelephoneException(rawTelephone);
            }
        }
        return e164;
    }

    /**
     * Determines whether any existing owner already carries the given identity key. The identity key
     * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) is the single value
     * all duplicate detection is based on, so two owners collide only when their whole identity keys
     * are equal.
     *
     * @param identityKey the identity key of the owner being created
     * @return {@code true} if another owner already has the same identity key
     */
    private boolean isIdentityKeyInUse(String identityKey) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .map(Owner::getIdentityKey)
            .anyMatch(identityKey::equals);
    }

    /**
     * Finds an existing owner that the owner being created is a possible (soft) duplicate of: one
     * that shares the same last name (compared case-insensitively, whitespace-collapsed) and the
     * same postcode but carries a <em>different</em> telephone. Such a create is not rejected as a
     * hard duplicate, but is flagged. An owner with no postcode can never be a soft duplicate. When
     * several owners match, the earliest (lowest id) is returned so the result is deterministic.
     *
     * @param owner the owner being created (telephone already normalized, postcode set)
     * @return the id of the matching existing owner, or {@code null} when there is no soft match
     */
    private Integer possibleDuplicateOf(Owner owner) {
        if (isBlank(owner.getPostcode())) {
            return null;
        }
        String normalizedLastName = normalizeIdentity(owner.getLastName());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getId() != null
                && !existing.isDeleted()
                && normalizeIdentity(existing.getLastName()).equals(normalizedLastName)
                && owner.getPostcode().equals(existing.getPostcode())
                && !java.util.Objects.equals(owner.getTelephone(), existing.getTelephone()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
    }

    /**
     * Counts how many existing owners already share the given first name and last name with the
     * owner being created. Both names are compared case-insensitively (after trimming and
     * collapsing runs of whitespace). The count reflects the state before the new owner is
     * persisted, so a first, otherwise-unique owner yields {@code 0}.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners sharing the same first and last name
     */
    private int namesakeCount(String firstName, String lastName) {
        String normalizedFirstName = normalizeIdentity(firstName);
        String normalizedLastName = normalizeIdentity(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeIdentity(existing.getFirstName()).equals(normalizedFirstName)
                && normalizeIdentity(existing.getLastName()).equals(normalizedLastName))
            .count();
    }

    /**
     * Counts how many members the owner being created belongs to in its household, including
     * itself, as of this create. Members are the owners sharing the given {@code householdId};
     * the new owner is not yet persisted, so its own membership is added to the existing count.
     * An owner not assigned to a shared household ({@code householdId} is {@code null}) is a
     * household of one.
     *
     * @param householdId the shared household identifier assigned to the owner being created, or
     *                    {@code null} if the owner does not share a household
     * @return the number of household members after this create (at least 1)
     */
    private int householdSizeAfterCreate(String householdId) {
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> householdId.equals(owner.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * The maximum number of owners permitted in a single city. Once a city already contains this
     * many owners it is considered full and no further owners may be created there.
     */
    private static final int CITY_CAPACITY = 50;

    /**
     * Determines whether the given city has already reached its capacity, i.e. already contains
     * {@value #CITY_CAPACITY} or more existing owners. Cities are compared case-insensitively after
     * collapsing runs of whitespace to a single space and trimming.
     *
     * @param city the city of the owner being created
     * @return {@code true} if the city already contains {@value #CITY_CAPACITY} or more owners
     */
    private boolean isCityAtCapacity(String city) {
        String normalizedCity = normalizeIdentity(city);
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeIdentity(existing.getCity()).equals(normalizedCity))
            .count();
        return count >= CITY_CAPACITY;
    }

    /**
     * The maximum number of owners that may be created on any single day. Once this many owners
     * already carry a {@code registrationDate} of the current day, no further owners may be created
     * that day.
     */
    private static final int DAILY_REGISTRATION_LIMIT = 100;

    /**
     * Determines whether the daily registration limit has already been reached, i.e. whether
     * {@value #DAILY_REGISTRATION_LIMIT} or more existing owners already carry the given date as
     * their {@code registrationDate}.
     *
     * @param date the current day
     * @return {@code true} if {@value #DAILY_REGISTRATION_LIMIT} or more owners were already
     *         registered on the given date
     */
    /**
     * The fixed public holidays that the business-day roll skips. A registration date landing on
     * any of these is rolled forward, exactly as it is for a weekend.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Rolls a registration date forward onto a business day. A date that already falls on a
     * weekday which is not a public holiday is returned unchanged; a Saturday, Sunday or listed
     * public holiday is rolled forward, one day at a time, until the next non-weekend,
     * non-holiday day is reached. This adjusted date is what gets stored as the
     * {@code registrationDate} and drives everything derived from it (the fiscal year, the
     * membership number's fiscal-year segment, the daily create-limit).
     *
     * @param date the effective registration date (supplied by the client or defaulted to the
     *             server's current date)
     * @return the same date if it is a business day, otherwise the next business day that is
     *         neither a weekend nor a public holiday
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(date);
    }

    private boolean isDailyRegistrationLimitReached(LocalDate date) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> date.equals(existing.getRegistrationDate()))
            .count();
        return count >= DAILY_REGISTRATION_LIMIT;
    }

    /**
     * The number of owners that may be created on a single day before the bulk-signup warning is
     * raised. Once <em>more than</em> this many owners already carry the current business day as
     * their {@code registrationDate}, responses flag {@code bulkSignupWarning} true.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been
     * created today, i.e. already carry the current business day as their {@code registrationDate}.
     * This shares the daily create-limit's accumulation path (owners counted by registration date).
     *
     * @return {@code true} if more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners are already
     *         registered on today's business day
     */
    private boolean isBulkSignupWarning() {
        LocalDate today = toBusinessDay(LocalDate.now());
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Computes the membership level to assign to the owner being created, applying the household
     * level ceiling: a new owner's level may not exceed one above the current maximum membership
     * level among their existing household members. The owner's own naturally derived level (see
     * {@link Owner#getComputedMembershipLevel()}) is returned unchanged when there is no existing
     * household member; otherwise it is capped at {@code maxHouseholdLevel + 1}. Existing members are
     * the non-deleted owners already sharing the owner's {@code householdId}; the owner being created
     * is not yet persisted, so it is naturally excluded.
     *
     * @param owner the owner being created, with {@code householdId} and the fields driving its
     *              membership level already set
     * @return the (possibly capped) membership level to store on the owner
     */
    private int cappedMembershipLevel(Owner owner) {
        int naturalLevel = owner.getComputedMembershipLevel();
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return naturalLevel;
        }
        java.util.OptionalInt maxHouseholdLevel = this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getId() != null
                && !existing.isDeleted()
                && householdId.equals(existing.getHouseholdId()))
            .mapToInt(Owner::getMembershipLevel)
            .max();
        if (maxHouseholdLevel.isEmpty()) {
            return naturalLevel;
        }
        return Math.min(naturalLevel, maxHouseholdLevel.getAsInt() + 1);
    }

    /**
     * Derives the stable household identifier for a given last name and postcode. The value is the
     * first 12 upper-case hex characters of the SHA-256 digest of {@code normalizedLastName + '|' +
     * postcode}, so it is deterministic across calls and identical for every owner sharing the same
     * normalized last name and postcode — they belong to the same household automatically.
     *
     * @param lastName the last name of the household
     * @param postcode the postcode of the household (may be {@code null})
     * @return the deterministic household identifier
     */
    private static String householdIdFor(String lastName, String postcode) {
        String key = normalizeIdentity(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
     * Normalizes a text field for household-identity comparison: {@code null} becomes the empty
     * string, surrounding whitespace is trimmed, internal runs of whitespace are collapsed to a
     * single space, and the result is lower-cased.
     *
     * @param value the raw field value
     * @return the normalized value used for case-insensitive, whitespace-insensitive comparison
     */
    private static String normalizeIdentity(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }
}
