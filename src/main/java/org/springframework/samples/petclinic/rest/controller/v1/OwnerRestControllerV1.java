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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * Dedicated audit logger recording every successful owner creation.
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
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        if (hasReachedDailyRegistrationLimit()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (hasReachedCityOwnerLimit(owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (isDuplicateOwner(owner) || isDuplicateEmail(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        owner.setNamesakeCount(countNamesakes(owner.getLastName()));
        owner.setSharesHousehold(sharesHousehold(owner.getAddress(), owner.getCity()));
        owner.setLocality(resolveLocality(owner.getCity()));
        owner.setCity(resolveCity(owner.getCity()));
        int membershipNumber = this.clinicService.findAllOwners().size() + 1;
        owner.setMembershipNumber(membershipNumber);
        owner.setCustomerCode(nextCustomerCode(owner.getCity()));
        owner.setMembershipTier(membershipNumber <= MAX_FOUNDING_OWNERS ? "FOUNDING" : "STANDARD");
        long sharedAreaCodeCount = countOwnersSharingAreaCode(owner.getTelephone());
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created by user={} ownerId={} membershipNumber={}",
            currentUsername(), owner.getId(), owner.getMembershipNumber());
        if (sharedAreaCodeCount >= MIN_AREA_CODE_BULK_SIGNUP) {
            AUDIT.warn("Possible bulk signup: owner ownerId={} telephone={} shares area code={} with {} existing owners",
                owner.getId(), owner.getTelephone(), areaCode(owner.getTelephone()), sharedAreaCodeCount);
        }
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
        currentOwner.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
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
     * Resolves the name of the currently authenticated user for audit logging, falling back to
     * {@code "anonymous"} when there is no authentication in the security context.
     *
     * @return the authenticated user name, never {@code null}
     */
    private static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "anonymous" : authentication.getName();
    }

    /**
     * The number of owners at the head of the registration sequence that receive the
     * {@code 'FOUNDING'} membership tier. The first {@value #MAX_FOUNDING_OWNERS} owners ever
     * created are {@code 'FOUNDING'}; all later owners are {@code 'STANDARD'}.
     */
    private static final int MAX_FOUNDING_OWNERS = 100;

    /**
     * The maximum number of owners that may be registered on any single calendar day.
     */
    private static final long MAX_DAILY_REGISTRATIONS = 20;

    /**
     * Determines whether the daily owner-registration limit has already been reached. Owners are
     * counted by their {@link Owner#getRegistrationDate() registration date}, so once
     * {@value #MAX_DAILY_REGISTRATIONS} owners have been registered with today's date, no further
     * owners may be created today.
     *
     * @return {@code true} if today's registration limit has been reached
     */
    private boolean hasReachedDailyRegistrationLimit() {
        LocalDate today = LocalDate.now();
        long registeredToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        return registeredToday >= MAX_DAILY_REGISTRATIONS;
    }

    /**
     * The maximum number of owners that may be registered in any single city.
     */
    private static final long MAX_OWNERS_PER_CITY = 8;

    /**
     * Determines whether the given city has already reached its owner limit. Owners are matched to
     * the city using {@link #normalize(String)}, so the count ignores letter case and any
     * surrounding or repeated whitespace. Once {@value #MAX_OWNERS_PER_CITY} owners live in a city,
     * no further owners may be created there.
     *
     * @param city the city of the new owner, may be {@code null}
     * @return {@code true} if the city already contains {@value #MAX_OWNERS_PER_CITY} owners
     */
    private boolean hasReachedCityOwnerLimit(String city) {
        String normalized = normalize(city);
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> Objects.equals(normalize(existing.getCity()), normalized))
            .count();
        return ownersInCity >= MAX_OWNERS_PER_CITY;
    }

    /**
     * Builds the customer code for a newly created owner, formatted as
     * {@code '<UPPERCASE_CITY>-<NNNN>'} where {@code NNNN} is one more than the number of owners
     * already registered in that city, zero-padded to four digits (e.g. {@code 'LONDON-0007'}).
     *
     * @param city the city of the new owner
     * @return the assigned customer code
     */
    private String nextCustomerCode(String city) {
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> Objects.equals(
                normalize(existing.getCity()), normalize(city)))
            .count();
        return String.format("%s-%04d", city.toUpperCase(Locale.ROOT), ownersInCity + 1);
    }

    /**
     * Resolves the city to store for a newly created owner. The city is title-cased (so
     * {@code "new york"} becomes {@code "New York"}), unless an owner already lives in that
     * city, in which case the existing owner's exact spelling of the city name is reused. Cities
     * are matched using {@link #normalize(String)}, so the match ignores letter case and any
     * surrounding or repeated whitespace.
     *
     * @param city the city supplied for the new owner, may be {@code null}
     * @return the city to store, or {@code null} if the input was {@code null}
     */
    private String resolveCity(String city) {
        if (city == null) {
            return null;
        }
        String normalized = normalize(city);
        if (!normalized.isEmpty()) {
            for (Owner existing : this.clinicService.findAllOwners()) {
                if (normalized.equals(normalize(existing.getCity()))) {
                    return existing.getCity();
                }
            }
        }
        return toTitleCase(city);
    }

    /**
     * Title-cases free-form text by upper-casing the first letter of each whitespace-separated word
     * and lower-casing the remaining letters, preserving the original whitespace. For example
     * {@code "new  YORK"} becomes {@code "New  York"}.
     *
     * @param value the raw value, never {@code null}
     * @return the title-cased value
     */
    private static String toTitleCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean startOfWord = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                startOfWord = true;
                result.append(c);
            } else if (startOfWord) {
                result.append(Character.toTitleCase(c));
                startOfWord = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    /**
     * Resolves the locality of a newly created owner relative to the owners present at the moment
     * it is created. The value is {@code "local"} when the new owner's city is the single most
     * common city among the existing owners, and {@code "remote"} otherwise. Cities are matched
     * using {@link #normalize(String)}, so the comparison ignores letter case and any surrounding
     * or repeated whitespace. When there are no existing owners, or when two or more cities tie for
     * the highest owner count, no city is the sole most common one and the result is
     * {@code "remote"}.
     *
     * @param city the city supplied for the new owner, may be {@code null}
     * @return {@code "local"} if the city is the single most common city, otherwise {@code "remote"}
     */
    private String resolveLocality(String city) {
        String normalized = normalize(city);
        Map<String, Long> ownersPerCity = this.clinicService.findAllOwners().stream()
            .map(existing -> normalize(existing.getCity()))
            .filter(normalizedCity -> !normalizedCity.isEmpty())
            .collect(Collectors.groupingBy(normalizedCity -> normalizedCity, Collectors.counting()));
        long highestCount = ownersPerCity.values().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);
        if (highestCount == 0L) {
            return "remote";
        }
        List<String> mostCommonCities = ownersPerCity.entrySet().stream()
            .filter(entry -> entry.getValue() == highestCount)
            .map(Map.Entry::getKey)
            .toList();
        boolean isSingleMostCommon = mostCommonCities.size() == 1
            && mostCommonCities.get(0).equals(normalized);
        return isSingleMostCommon ? "local" : "remote";
    }

    /**
     * Counts how many existing owners share the given last name. Last names are matched using
     * {@link #normalize(String)}, so the count ignores letter case and any surrounding or repeated
     * whitespace. This is evaluated against the owners present at the moment the new owner is
     * created, before the new owner is persisted, so the new owner does not count itself.
     *
     * @param lastName the last name of the new owner, may be {@code null}
     * @return the number of existing owners sharing the same last name
     */
    private int countNamesakes(String lastName) {
        String normalized = normalize(lastName);
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalized.equals(normalize(existing.getLastName())))
            .count();
    }

    /**
     * Determines whether the new owner shares a household with an existing owner, meaning another
     * owner already has the same address and city. Both the address and the city are matched using
     * {@link #normalize(String)}, so the comparison ignores letter case and any surrounding or
     * repeated whitespace. This is evaluated against the owners present at the moment the new owner
     * is created, before the new owner is persisted, so the new owner does not count itself.
     *
     * @param address the address of the new owner, may be {@code null}
     * @param city    the city of the new owner, may be {@code null}
     * @return {@code true} if an existing owner already has the same address and city
     */
    private boolean sharesHousehold(String address, String city) {
        String normalizedAddress = normalize(address);
        String normalizedCity = normalize(city);
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalizedAddress.equals(normalize(existing.getAddress()))
                && normalizedCity.equals(normalize(existing.getCity())));
    }

    /**
     * Determines whether the given telephone number is already used by any other owner.
     *
     * @param owner the candidate owner
     * @return {@code true} if an owner with the same telephone number already exists in the data store
     */
    private boolean isDuplicateOwner(Owner owner) {
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(existing.getTelephone(), owner.getTelephone()));
    }

    /**
     * Determines whether the given email address is already used by any other owner. An owner
     * without an email address is never considered a duplicate on this criterion. Email addresses
     * are compared using {@link #normalize(String)} so the check ignores letter case and any
     * surrounding or repeated whitespace.
     *
     * @param owner the candidate owner
     * @return {@code true} if the owner provides an email already used by an existing owner
     */
    private boolean isDuplicateEmail(Owner owner) {
        String email = normalize(owner.getEmail());
        if (email.isEmpty()) {
            return false;
        }
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> email.equals(normalize(existing.getEmail())));
    }

    /**
     * The minimum number of existing owners that must share a new owner's area code before the
     * creation is flagged as a possible bulk signup. When {@value #MIN_AREA_CODE_BULK_SIGNUP} or
     * more existing owners already share the area code, a WARN is emitted to the audit log.
     */
    private static final int MIN_AREA_CODE_BULK_SIGNUP = 5;

    /**
     * Extracts the area code (the first three digits) from an already
     * {@link #normalizeTelephone(String) normalized} telephone number. A telephone that is
     * {@code null} or has fewer than three digits has no area code.
     *
     * @param telephone the digits-only telephone number, may be {@code null}
     * @return the three-digit area code, or {@code null} if none can be determined
     */
    private static String areaCode(String telephone) {
        if (telephone == null || telephone.length() < 3) {
            return null;
        }
        return telephone.substring(0, 3);
    }

    /**
     * Counts how many existing owners share the given telephone's area code (its first three
     * digits). This is evaluated against the owners present at the moment the new owner is created,
     * before the new owner is persisted, so the new owner does not count itself. Owners whose
     * telephone has no determinable area code are ignored, and a candidate telephone without an area
     * code matches no one.
     *
     * @param telephone the normalized telephone number of the new owner, may be {@code null}
     * @return the number of existing owners sharing the same area code
     */
    private long countOwnersSharingAreaCode(String telephone) {
        String areaCode = areaCode(telephone);
        if (areaCode == null) {
            return 0L;
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> areaCode.equals(areaCode(normalizeTelephone(existing.getTelephone()))))
            .count();
    }

    /**
     * Normalizes a telephone number to digits only by stripping spaces, dashes and parentheses, so
     * that a value such as {@code "(613) 555-0100"} is stored and compared as {@code "6135550100"}.
     * This is applied before an owner is stored and before the telephone-uniqueness check, so two
     * differently formatted representations of the same number are treated as duplicates. A
     * {@code null} value is returned unchanged.
     *
     * @param telephone the raw telephone number, may be {@code null}
     * @return the digits-only telephone number, or {@code null} if the input was {@code null}
     */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("[\\s()-]", "");
    }

    /**
     * Normalizes free-form text for duplicate detection so that comparisons ignore letter case and
     * surrounding or repeated whitespace: the value is trimmed, runs of internal whitespace are
     * collapsed to a single space and the result is lower-cased. This is what makes
     * {@code "  john   smith "} and {@code "John Smith"} count as the same value when detecting
     * duplicates. A {@code null} value normalizes to the empty string.
     *
     * @param value the raw value, may be {@code null}
     * @return the normalized value, never {@code null}
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
