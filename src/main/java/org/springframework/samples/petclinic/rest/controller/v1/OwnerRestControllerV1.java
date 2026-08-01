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
import java.util.Collections;
import java.util.List;
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

    /** Dedicated audit logger recording successful owner creations. */
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
        owner.setCity(resolveCity(owner.getCity()));
        if (isDuplicateOwner(owner) || isDuplicateEmail(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (isDailyRegistrationLimitReached()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (isCityCapacityReached(owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setMembershipNumber(this.clinicService.findAllOwners().size() + 1);
        owner.setMembershipTier(this.clinicService.findAllOwners().size() < 100 ? "FOUNDING" : "STANDARD");
        owner.setCustomerCode(nextCustomerCode(owner.getCity()));
        owner.setNamesakeCount(countNamesakes(owner.getLastName()));
        owner.setSharesHousehold(sharesHousehold(owner));
        owner.setLocality(determineLocality(owner.getCity()));
        int sharedAreaCodeCount = countOwnersSharingAreaCode(owner.getTelephone());
        this.clinicService.saveOwner(owner);
        AUDIT.info("Owner created by user={} ownerId={} membershipNumber={}",
            currentUsername(), owner.getId(), owner.getMembershipNumber());
        if (sharedAreaCodeCount >= 5) {
            AUDIT.warn("Possible bulk signup: owner ownerId={} telephone area code {} shared by {} existing owners",
                owner.getId(), areaCode(owner.getTelephone()), sharedAreaCodeCount);
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
     * Returns the name of the currently authenticated user, or {@code "anonymous"} if there is
     * no authentication in the current security context.
     *
     * @return the authenticated user name for audit logging
     */
    private static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "anonymous";
        }
        return authentication.getName();
    }

    /**
     * Resolves the city name to use for a newly created owner. The supplied city is
     * title-cased (each whitespace-separated word starts with an upper-case letter and the
     * remaining letters are lower-cased), for example {@code "new york"} becomes
     * {@code "New York"}. However, if an owner already exists in that city (matched ignoring
     * letter case), the existing owner's exact spelling of the city name is reused instead, so
     * that all owners in a city share a single canonical spelling.
     *
     * @param city the city supplied for the owner being created, may be {@code null}
     * @return the city name to store and return for the owner
     */
    private String resolveCity(String city) {
        if (city == null) {
            return null;
        }
        String existingCity = this.clinicService.findAllOwners().stream()
            .map(Owner::getCity)
            .filter(existing -> existing != null && existing.equalsIgnoreCase(city))
            .findFirst()
            .orElse(null);
        if (existingCity != null) {
            return existingCity;
        }
        return toTitleCase(city);
    }

    /**
     * Title-cases the given text so that each whitespace-separated word starts with an
     * upper-case letter and the remaining letters are lower-cased, for example
     * {@code "new york"} and {@code "NEW YORK"} both become {@code "New York"}. The original
     * spacing between words is preserved.
     *
     * @param value the text to title-case
     * @return the title-cased text
     */
    private static String toTitleCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean startOfWord = true;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isWhitespace(ch)) {
                startOfWord = true;
                result.append(ch);
            } else if (startOfWord) {
                result.append(Character.toUpperCase(ch));
                startOfWord = false;
            } else {
                result.append(Character.toLowerCase(ch));
            }
        }
        return result.toString();
    }

    /**
     * Builds the customer code for a newly created owner, formatted as
     * {@code '<UPPERCASE_CITY>-<NNNN>'} where {@code NNNN} is one more than the number of
     * owners already registered in that city, zero-padded to four digits (e.g.
     * {@code 'LONDON-0007'}). Cities are matched ignoring letter case so the sequence is
     * consistent with the upper-cased prefix.
     *
     * @param city the city of the owner being created
     * @return the assigned customer code
     */
    private String nextCustomerCode(String city) {
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        return city.toUpperCase() + "-" + String.format("%04d", ownersInCity + 1);
    }

    /**
     * Counts how many existing owners share the given last name at this moment. The count is taken
     * over the owners already registered (excluding the owner currently being created, which has not
     * yet been persisted); last names are compared for exact equality.
     *
     * @param lastName the last name of the owner being created
     * @return the number of other owners that currently share the given last name
     */
    private int countNamesakes(String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> Objects.equals(existing.getLastName(), lastName))
            .count();
    }

    /**
     * Determines whether, at the moment the owner is being created, another owner already shares
     * the same household &mdash; that is, an existing owner has both the same address and the same
     * city. Address and city are compared for exact equality, using the values resolved for the
     * owner being created (which has not yet been persisted).
     *
     * @param owner the candidate owner to check
     * @return {@code true} if an existing owner already has the same address and city
     */
    private boolean sharesHousehold(Owner owner) {
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(existing.getAddress(), owner.getAddress())
                && Objects.equals(existing.getCity(), owner.getCity()));
    }

    /**
     * Determines the locality of the owner being created, based on the given city. The city is
     * {@code "local"} if it is the single most common city among the existing owners at this moment
     * (before the owner being created is persisted); otherwise it is {@code "remote"}. Cities are
     * compared ignoring letter case. If there are no existing owners, or if two or more cities are
     * tied for the most common, there is no single most common city and the result is
     * {@code "remote"}.
     *
     * @param city the city of the owner being created, may be {@code null}
     * @return {@code "local"} or {@code "remote"}
     */
    private String determineLocality(String city) {
        if (city == null) {
            return "remote";
        }
        Map<String, Long> countsByCity = this.clinicService.findAllOwners().stream()
            .map(Owner::getCity)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(existing -> existing.toLowerCase(), Collectors.counting()));
        if (countsByCity.isEmpty()) {
            return "remote";
        }
        long max = Collections.max(countsByCity.values());
        List<String> mostCommon = countsByCity.entrySet().stream()
            .filter(entry -> entry.getValue() == max)
            .map(Map.Entry::getKey)
            .toList();
        if (mostCommon.size() == 1 && mostCommon.get(0).equals(city.toLowerCase())) {
            return "local";
        }
        return "remote";
    }

    /**
     * Determines whether the maximum number of owners that may be registered on a single day has
     * already been reached. At most 20 owners may be created per day; the count is taken over the
     * owners whose registration date is today.
     *
     * @return {@code true} if 20 or more owners have already been registered today
     */
    private boolean isDailyRegistrationLimitReached() {
        LocalDate today = LocalDate.now();
        long ownersToday = this.clinicService.findAllOwners().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        return ownersToday >= 20;
    }

    /**
     * Determines whether the city of the owner being created has already reached its capacity of
     * owners. At most 8 owners may be registered in a single city; the count is taken over the
     * existing owners whose city matches (ignoring letter case) the given city.
     *
     * @param city the city of the owner being created, may be {@code null}
     * @return {@code true} if the city already contains 8 or more owners
     */
    private boolean isCityCapacityReached(String city) {
        if (city == null) {
            return false;
        }
        long ownersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        return ownersInCity >= 8;
    }

    /**
     * Counts how many existing owners share the area code (the first three digits of the
     * digits-only telephone number) with the given telephone. The count is taken over the owners
     * already registered (excluding the owner currently being created, which has not yet been
     * persisted). Owners without an area code &mdash; that is, whose telephone has fewer than three
     * digits &mdash; are never counted, and a candidate telephone without an area code matches
     * nobody.
     *
     * @param telephone the digits-only telephone of the owner being created, may be {@code null}
     * @return the number of existing owners sharing the same three-digit area code
     */
    private int countOwnersSharingAreaCode(String telephone) {
        String areaCode = areaCode(telephone);
        if (areaCode == null) {
            return 0;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .map(existing -> areaCode(normalizeTelephone(existing.getTelephone())))
            .filter(areaCode::equals)
            .count();
    }

    /**
     * Extracts the area code &mdash; the first three digits &mdash; from a digits-only telephone
     * number. Returns {@code null} when the telephone is {@code null} or has fewer than three
     * digits.
     *
     * @param telephone the digits-only telephone number, may be {@code null}
     * @return the three-digit area code, or {@code null} if unavailable
     */
    private static String areaCode(String telephone) {
        if (telephone == null || telephone.length() < 3) {
            return null;
        }
        return telephone.substring(0, 3);
    }

    /**
     * Determines whether an owner already exists that uses the same telephone number as the
     * given candidate. Telephone numbers are compared digits-only, so formatting characters
     * such as spaces, dashes and parentheses are ignored.
     *
     * @param owner the candidate owner to check
     * @return {@code true} if an owner with the same telephone number already exists
     */
    private boolean isDuplicateOwner(Owner owner) {
        String telephone = normalizeTelephone(owner.getTelephone());
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(normalizeTelephone(existing.getTelephone()), telephone));
    }

    /**
     * Determines whether an owner already exists that uses the same email address as the
     * given candidate. Owners without an email address are never considered duplicates.
     * The comparison ignores letter case and surrounding or repeated whitespace.
     *
     * @param owner the candidate owner to check
     * @return {@code true} if an owner with the same email address already exists
     */
    private boolean isDuplicateEmail(Owner owner) {
        String email = normalize(owner.getEmail());
        if (email == null || email.isEmpty()) {
            return false;
        }
        return this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> Objects.equals(normalize(existing.getEmail()), email));
    }

    /**
     * Normalizes a value for duplicate detection so that comparisons ignore letter case and
     * surrounding or repeated whitespace. Leading and trailing whitespace is stripped, any run
     * of internal whitespace collapses to a single space, and the result is lower-cased. For
     * example {@code "  John   Smith "} and {@code "John Smith"} both normalize to
     * {@code "john smith"}.
     *
     * @param value the value to normalize, may be {@code null}
     * @return the normalized value, or {@code null} if {@code value} is {@code null}
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Normalizes a telephone number to digits only by stripping every non-digit character
     * (such as spaces, dashes and parentheses). This is used both to store telephone numbers
     * in a canonical form and to compare them for uniqueness, so that {@code "(613) 555-0100"}
     * and {@code "6135550100"} are treated as the same number.
     *
     * @param value the telephone number to normalize, may be {@code null}
     * @return the digits-only telephone number, or {@code null} if {@code value} is {@code null}
     */
    private static String normalizeTelephone(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }
}
