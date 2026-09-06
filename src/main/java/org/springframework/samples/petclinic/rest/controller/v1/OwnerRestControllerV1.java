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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
import org.springframework.samples.petclinic.rest.advice.DailyOwnerRegistrationLimitException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerHouseholdException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityAtCapacityException;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
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

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final TelephoneNormalizer telephoneNormalizer;

    private final AddressNormalizer addressNormalizer;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 TelephoneNormalizer telephoneNormalizer,
                                 AddressNormalizer addressNormalizer) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.telephoneNormalizer = telephoneNormalizer;
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
        ownerFieldsDto.setAddress(addressNormalizer.normalize(ownerFieldsDto.getAddress()));
        rejectBlankOwnerFields(ownerFieldsDto);
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        if (!sharesHousehold) {
            rejectDuplicateHousehold(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
        }
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        rejectDailyRegistrationLimit(LocalDate.now());
        String normalizedTelephone = telephoneNormalizer.normalize(ownerFieldsDto.getTelephone());
        rejectDuplicateTelephone(normalizedTelephone);
        ownerFieldsDto.setTelephone(normalizedTelephone);
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName()));
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        if (sharesHousehold) {
            owner.setHouseholdId(householdId(owner.getLastName(), owner.getAddress()));
        }
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
     * Rejects an owner whose required text fields are blank (whitespace-only). Missing (null)
     * and empty fields are already rejected by Bean Validation on {@link OwnerFieldsDto}; this
     * closes the gap for {@code address} and {@code city}, whose values may be non-empty yet
     * blank (e.g. {@code "   "}). The offending field names are reported through an
     * {@link InvalidOwnerFieldsException}, which the {@code ExceptionControllerAdvice} renders
     * as a 400 response carrying an {@code errors} array of field names.
     *
     * @param ownerFieldsDto the submitted owner fields
     * @throws InvalidOwnerFieldsException if one or more required fields are blank
     */
    private void rejectBlankOwnerFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> blankFields = new ArrayList<>();
        addIfBlank(blankFields, "firstName", ownerFieldsDto.getFirstName());
        addIfBlank(blankFields, "lastName", ownerFieldsDto.getLastName());
        addIfBlank(blankFields, "address", ownerFieldsDto.getAddress());
        addIfBlank(blankFields, "city", ownerFieldsDto.getCity());
        addIfBlank(blankFields, "telephone", ownerFieldsDto.getTelephone());
        if (!blankFields.isEmpty()) {
            throw new InvalidOwnerFieldsException(blankFields);
        }
    }

    private void addIfBlank(List<String> blankFields, String field, String value) {
        if (value != null && value.isBlank()) {
            blankFields.add(field);
        }
    }

    /**
     * Normalizes an owner's optional email address. When present it is stored and returned
     * lower-cased (using {@link Locale#ROOT} so normalization is locale-independent). A missing
     * (null) email is left as-is. Syntactic validity is enforced by Bean Validation ({@code @Email}
     * on {@link OwnerFieldsDto}), so an invalid address is already rejected as a 400 before this
     * method runs.
     *
     * @param email the raw email value from the request, or {@code null} when omitted
     * @return the lower-cased email, or {@code null} when none was supplied
     */
    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Builds the {@code customerCode} assigned to a newly created owner, formatted
     * {@code '<CITY3>-<LAST3>-<NNNN>'}: {@code CITY3} is the upper-cased first three letters of the
     * owner's {@code city}, {@code LAST3} is the upper-cased first three letters of the owner's
     * {@code lastName}, and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one
     * more than the number of owners already in that city (e.g. {@code 'MEL-SMI-0007'}).
     *
     * @param city the city of the owner being created
     * @param lastName the last name of the owner being created
     * @return the formatted customer code
     */
    private String nextCustomerCode(String city, String lastName) {
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(java.util.Locale.ROOT);
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(java.util.Locale.ROOT);
        long sequence = this.clinicService.findAllOwners().stream()
            .filter(owner -> equalsIgnoreCase(owner.getCity(), city))
            .count() + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Counts how many existing owners already share the given {@code firstName} and {@code lastName},
     * compared case-insensitively (using {@link java.util.Locale#ROOT}-independent
     * {@link String#equalsIgnoreCase(String)}). The count reflects the owners present before the
     * current create, so a name that is unique on create yields {@code 0}. The value is stored on the
     * new owner and surfaced as the read-only {@code namesakeCount} field.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners with the same first and last name (case-insensitive)
     */
    private int namesakeCount(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(owner -> equalsIgnoreCase(owner.getFirstName(), firstName)
                && equalsIgnoreCase(owner.getLastName(), lastName))
            .count();
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    /**
     * The maximum number of owners a single city may contain. Once a city already holds this many
     * owners, further owners in that city are rejected by {@link #rejectCityAtCapacity(String)}.
     */
    private static final long MAX_OWNERS_PER_CITY = 50L;

    /**
     * Rejects a create request whose city already contains {@link #MAX_OWNERS_PER_CITY} or more
     * owners. Existing owners are matched to the requested city case-insensitively (using
     * {@link java.util.Locale#ROOT}-independent {@link String#equalsIgnoreCase(String)}), mirroring
     * the per-city counting used for the customer code. A city at capacity is reported through an
     * {@link OwnerCityAtCapacityException}, which the {@code ExceptionControllerAdvice} renders as a
     * 409 Conflict response.
     *
     * @param city the city of the owner being created
     * @throws OwnerCityAtCapacityException if the city already holds the maximum number of owners
     */
    private void rejectCityAtCapacity(String city) {
        long owners = this.clinicService.findAllOwners().stream()
            .filter(owner -> equalsIgnoreCase(owner.getCity(), city))
            .count();
        if (owners >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }

    /**
     * The maximum number of owners that may be registered on a single day. Once this many owners
     * already carry a given {@code registrationDate}, further owners for that date are rejected by
     * {@link #rejectDailyRegistrationLimit(LocalDate)}.
     */
    private static final long MAX_OWNERS_PER_DAY = 100L;

    /**
     * Rejects a create request once {@link #MAX_OWNERS_PER_DAY} or more owners have already been
     * created on the given day, counted by {@code registrationDate}. A day at capacity is reported
     * through a {@link DailyOwnerRegistrationLimitException}, which the {@code ExceptionControllerAdvice}
     * renders as a 429 Too Many Requests response.
     *
     * @param date the registration date of the owner being created (today)
     * @throws DailyOwnerRegistrationLimitException if the day already holds the maximum number of owners
     */
    private void rejectDailyRegistrationLimit(LocalDate date) {
        long owners = this.clinicService.findAllOwners().stream()
            .filter(owner -> date.equals(owner.getRegistrationDate()))
            .count();
        if (owners >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerRegistrationLimitException(date);
        }
    }

    /**
     * Rejects a create request whose normalized telephone is already used by any existing owner.
     * Existing owners' telephones are reduced to the same canonical form (via
     * {@link TelephoneNormalizer#canonicalize(String)}) before comparison, so numbers that differ
     * only in formatting still collide. A collision is reported through a
     * {@link DuplicateOwnerTelephoneException}, which the {@code ExceptionControllerAdvice} renders
     * as a 409 Conflict response.
     *
     * @param normalizedTelephone the canonical telephone of the owner being created
     * @throws DuplicateOwnerTelephoneException if another owner already uses this telephone
     */
    private void rejectDuplicateTelephone(String normalizedTelephone) {
        boolean duplicate = existingOwnerMatches(
            owner -> owner.getTelephone() == null ? null : telephoneNormalizer.canonicalize(owner.getTelephone()),
            normalizedTelephone);
        if (duplicate) {
            throw new DuplicateOwnerTelephoneException(normalizedTelephone);
        }
    }

    /**
     * Reports whether any existing owner already carries the given comparison {@code key}, derived
     * from each owner by {@code keyExtractor}. Owners for which the extractor yields {@code null}
     * are ignored. The create endpoint's duplicate checks use this to reduce both an existing owner
     * and the owner being created to the same canonical key and reject a match.
     *
     * @param keyExtractor derives an owner's canonical comparison key (may return {@code null})
     * @param key the canonical key of the owner being created
     * @return {@code true} if some existing owner shares the key
     */
    private boolean existingOwnerMatches(Function<Owner, String> keyExtractor, String key) {
        return this.clinicService.findAllOwners().stream()
            .map(keyExtractor)
            .filter(Objects::nonNull)
            .anyMatch(key::equals);
    }

    /**
     * Rejects a create request that shares a household with an existing owner: another owner already
     * has the same {@code lastName} and the same {@code address}. Both values are compared in
     * canonical form (case-insensitive with surrounding and internal whitespace collapsed to a single
     * space, see {@link #canonicalizeHousehold(String)}), so values that differ only in casing or
     * spacing still collide. A collision is reported through a {@link DuplicateOwnerHouseholdException},
     * which the {@code ExceptionControllerAdvice} renders as a 409 Conflict response. Callers skip this
     * check when the request opts in via {@code sharesHousehold}.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @throws DuplicateOwnerHouseholdException if another owner already shares the household
     */
    private void rejectDuplicateHousehold(String lastName, String address) {
        String canonicalLastName = canonicalizeHousehold(lastName);
        String canonicalAddress = canonicalizeHousehold(address);
        if (canonicalLastName == null || canonicalAddress == null) {
            return;
        }
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(owner -> canonicalLastName.equals(canonicalizeHousehold(owner.getLastName()))
                && canonicalAddress.equals(canonicalizeHousehold(owner.getAddress())));
        if (duplicate) {
            throw new DuplicateOwnerHouseholdException(canonicalLastName, canonicalAddress);
        }
    }

    /**
     * Reduces a household field ({@code lastName} or {@code address}) to a canonical form for
     * case-insensitive, whitespace-insensitive comparison: leading and trailing whitespace is
     * trimmed, every run of internal whitespace is collapsed to a single space, and the result is
     * lower-cased using {@link java.util.Locale#ROOT} so comparison is locale-independent.
     *
     * @param value the raw field value, or {@code null}
     * @return the canonical value, or {@code null} when {@code value} is {@code null}
     */
    private String canonicalizeHousehold(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Derives the stable, shared {@code householdId} for an owner joining a household via
     * {@code sharesHousehold}. The identifier is a deterministic function of the canonical
     * {@code lastName} and {@code address} (see {@link #canonicalizeHousehold(String)}), so every
     * owner with the same last name at the same address - regardless of casing or spacing - is
     * assigned the identical value. It is the first 16 upper-case hex characters of the SHA-256 hash
     * of the two canonical fields joined by a delimiter that cannot occur in the input.
     *
     * @param lastName the last name of the owner being created
     * @param address the address of the owner being created
     * @return the shared household identifier
     */
    private String householdId(String lastName, String address) {
        String key = canonicalizeHousehold(lastName) + "\n" + canonicalizeHousehold(address);
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, 16);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
