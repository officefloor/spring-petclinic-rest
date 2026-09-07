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
import org.springframework.samples.petclinic.rest.advice.CityCapacityExceededException;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.advice.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.advice.InvalidFieldsException;
import org.springframework.samples.petclinic.rest.advice.RequiredFieldsMissingException;
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
        owner.setHouseholdMemberCount(countHouseholdMembers(owner.getHouseholdId()));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(bulkSignupWarning(owner.getRegistrationDate()));
        return new ResponseEntity<>(ownerDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        LocalDate registrationDate = effectiveRegistrationDate(ownerFieldsDto);
        prepareNewOwner(ownerFieldsDto, registrationDate);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(nextCustomerCode(owner));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        assignHousehold(owner, ownerFieldsDto);
        this.clinicService.saveOwner(owner);
        owner.setHouseholdMemberCount(countHouseholdMembers(owner.getHouseholdId()));
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            ownerMapper.membershipLevel(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(bulkSignupWarning(registrationDate));
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
     * Applies the intake rules a newly submitted owner must satisfy before it is persisted.
     * The address is normalized in place (so a required-field check that follows rejects an
     * address blank after normalization), the required fields are checked, the telephone and
     * email are normalized in place, and the resulting owner is rejected when its derived
     * identity key already belongs to another owner. Each rule
     * signals a violation by throwing, which the exception advice renders as the matching 4xx
     * response; when the method returns normally {@code ownerFieldsDto} is normalized and ready
     * to be mapped and saved.
     *
     * @param ownerFieldsDto the submitted owner fields, normalized in place
     */
    private void prepareNewOwner(OwnerFieldsDto ownerFieldsDto, LocalDate registrationDate) {
        ownerFieldsDto.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        validateRequiredFields(ownerFieldsDto);
        ownerFieldsDto.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        validatePostcode(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        rejectDuplicateIdentity(ownerFieldsDto);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        rejectDailyLimitExceeded(registrationDate);
    }

    /**
     * Determines the effective registration date for a newly submitted owner, adjusted so it always
     * falls on a business day. The date supplied on the request is used when present, otherwise the
     * server's current date is used. When that effective date lands on a Saturday or Sunday it is
     * rolled forward to the following Monday; a weekday is left unchanged. This single adjusted date
     * is what gets stored as {@code registrationDate} and is what every value derived from it (the
     * membership number's year segment, the per-day create limit) is computed against.
     *
     * @param ownerFieldsDto the submitted owner fields, which may carry a registration date
     * @return the effective registration date rolled forward to the next business day
     */
    private LocalDate effectiveRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate supplied = ownerFieldsDto.getRegistrationDate();
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        return toBusinessDay(effective);
    }

    /**
     * Rolls a date forward to the next business day: a Saturday or Sunday is advanced to the
     * following Monday, while a weekday is returned unchanged.
     *
     * @param date the date to adjust
     * @return the same date when it is a weekday, otherwise the next Monday
     */
    private LocalDate toBusinessDay(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY) {
            return date.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
        }
        return date;
    }

    /**
     * The maximum number of owners that may be created on a single day. Once this many owners
     * already carry today's {@code registrationDate}, no further owner may be created today.
     */
    private static final long DAILY_OWNER_LIMIT = 100L;

    /**
     * Rejects a new owner when {@link #DAILY_OWNER_LIMIT} or more owners already carry the given
     * adjusted business-day {@code registrationDate}. The count is taken against the same adjusted
     * date the new owner would be stored with, so owners are capped per business day. When the limit
     * has been reached a {@link DailyOwnerLimitExceededException} is thrown, which the exception
     * advice renders as a 429 Too Many Requests response.
     *
     * @param registrationDate the adjusted business-day registration date of the owner being created
     */
    private void rejectDailyLimitExceeded(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
    }

    /**
     * The number of owners that may carry a single day's {@code registrationDate} before that day
     * is flagged as a bulk-signup day. Once more than this many owners share a registration date,
     * the create and read responses report {@code bulkSignupWarning} true for that date.
     */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80L;

    /**
     * Reports whether the given business-day {@code registrationDate} is a bulk-signup day, i.e.
     * whether more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already carry that date. The
     * count is taken against the same adjusted date owners are stored with, mirroring the
     * per-day create-limit accumulation. A {@code null} date is never a bulk-signup day.
     *
     * @param registrationDate the adjusted business-day registration date to test, or {@code null}
     * @return {@code true} when more than 80 owners share the date, otherwise {@code false}
     */
    private boolean bulkSignupWarning(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * The maximum number of owners a single city may hold. Once a city already contains this
     * many owners, no further owner may be created in it.
     */
    private static final long CITY_OWNER_CAPACITY = 50L;

    /**
     * Rejects a new owner whose city already contains {@link #CITY_OWNER_CAPACITY} or more owners.
     * Cities are compared case-insensitively, consistent with how per-city owners are counted
     * elsewhere. When the city is at capacity a {@link CityCapacityExceededException} is thrown,
     * which the exception advice renders as a 409 Conflict response.
     *
     * @param city the city of the owner being created
     */
    private void rejectCityAtCapacity(String city) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (count >= CITY_OWNER_CAPACITY) {
            throw new CityCapacityExceededException(city);
        }
    }

    /**
     * Builds the customer code assigned to a newly created owner. The code is formatted as
     * {@code '<CITY3>-<LAST3>-<NNNN>'}, where {@code CITY3} is the upper-cased first three letters
     * of the owner's city, {@code LAST3} is the upper-cased first three letters of the owner's last
     * name and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one more than the
     * number of owners already in that city. For example the seventh owner in {@code 'Springfield'}
     * named {@code 'Smith'} is assigned {@code 'SPR-SMI-0007'}. The whole owner is taken so the code
     * is derived from whichever of its fields the identity scheme needs.
     *
     * @param owner the owner being created, whose fields the code is derived from
     * @return the formatted customer code
     */
    private String nextCustomerCode(Owner owner) {
        String city = owner.getCity();
        String lastName = owner.getLastName();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        long sequence = this.clinicService.findAllOwners().stream()
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count() + 1L;
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /**
     * Counts the existing owners that share the given {@code firstName} and {@code lastName}
     * with the owner being created, compared case-insensitively. The count is taken before the
     * new owner is persisted, so it reflects only pre-existing owners and never includes the new
     * owner itself. The resulting value is stored on the owner and exposed as {@code namesakeCount}.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of pre-existing owners sharing the same first and last name
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Rejects an owner whose {@code firstName}, {@code lastName}, {@code address}, {@code city}
     * or {@code telephone} is missing or blank. A field that is only whitespace is treated as
     * blank. When any field fails, a {@link RequiredFieldsMissingException} is thrown carrying
     * the names of every offending field, which the exception advice renders as a 400 response
     * with an {@code errors} array.
     *
     * @param ownerFieldsDto the submitted owner fields
     */
    private void validateRequiredFields(OwnerFieldsDto ownerFieldsDto) {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", ownerFieldsDto.getFirstName());
        addIfBlank(missing, "lastName", ownerFieldsDto.getLastName());
        addIfBlank(missing, "address", ownerFieldsDto.getAddress());
        addIfBlank(missing, "city", ownerFieldsDto.getCity());
        addIfBlank(missing, "telephone", ownerFieldsDto.getTelephone());
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private void addIfBlank(List<String> missing, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missing.add(fieldName);
        }
    }

    /**
     * Normalizes a submitted telephone number into E.164 form. Spaces, dashes and brackets
     * are stripped. When a leading {@code '+'} and country code are present they are kept as
     * given; otherwise the country code {@code '+61'} is assumed and a single leading
     * {@code '0'} is dropped from the national digits. The result must be a {@code '+'}
     * followed by 8 to 15 digits. The normalized E.164 value is what gets stored and returned.
     * When the value cannot form a valid E.164 number an {@link InvalidFieldsException} is
     * thrown, which the exception advice renders as a 400 response naming the {@code telephone}
     * field. For example {@code "0412 345 678"} becomes {@code "+61412345678"}.
     *
     * @param telephone the submitted telephone value
     * @return the normalized E.164 telephone
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = "+" + cleaned.substring(1);
        } else {
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            e164 = "+61" + national;
        }
        if (!e164.matches("^\\+[0-9]{8,15}$")) {
            throw new InvalidFieldsException(List.of("telephone"));
        }
        validateNationalNumberLength(e164);
        return e164;
    }

    /**
     * The required national-number length (the digits after the country code) for each country
     * code we recognize: {@code '+61'} (Australia) requires 9 national digits and {@code '+1'}
     * (NANP) requires 10. Keyed by the country-code digits without the leading {@code '+'}.
     */
    private static final java.util.Map<String, Integer> NATIONAL_NUMBER_LENGTHS =
        java.util.Map.of("61", 9, "1", 10);

    /**
     * Validates the national-number length of an already syntactically valid E.164 telephone
     * against its country code. For each recognized country code the digits following the code
     * must have exactly the expected length ({@code '+61'} => 9 national digits, {@code '+1'} =>
     * 10). When the country code is recognized but the national number is the wrong length an
     * {@link InvalidFieldsException} is thrown, which the exception advice renders as a 400
     * response naming the {@code telephone} field. A country code that is not recognized carries
     * no per-country length rule and is left to the general 8-to-15-digit E.164 check.
     *
     * @param e164 the normalized E.164 telephone (a {@code '+'} followed by digits)
     */
    private void validateNationalNumberLength(String e164) {
        String digits = e164.substring(1);
        NATIONAL_NUMBER_LENGTHS.entrySet().stream()
            .filter(entry -> digits.startsWith(entry.getKey()))
            // longest matching country code wins, so a shorter code cannot shadow a longer one
            .max(java.util.Comparator.comparingInt(entry -> entry.getKey().length()))
            .ifPresent(entry -> {
                int nationalLength = digits.length() - entry.getKey().length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidFieldsException(List.of("telephone"));
                }
            });
    }

    /**
     * Pattern for a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain containing at least one dot, none of the parts holding whitespace or a
     * second {@code @}.
     */
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /**
     * Normalizes an optional owner email. An email is optional, so a {@code null} value is
     * accepted and returned unchanged. When present it must be a syntactically valid address;
     * the value is stored and returned lower-cased. When present but invalid an
     * {@link InvalidFieldsException} is thrown, which the exception advice renders as a 400
     * response naming the {@code email} field.
     *
     * @param email the submitted email value, or {@code null} when omitted
     * @return the lower-cased email, or {@code null} when none was supplied
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidFieldsException(List.of("email"));
        }
        return canonicalEmail(trimmed);
    }

    /**
     * Reduces an email to the canonical form used both to store it and to compare it against
     * other owners' emails: surrounding whitespace is trimmed and the value is lower-cased. A
     * {@code null} email (none was supplied) canonicalizes to {@code null}. Unlike
     * {@link #normalizeEmail(String)} no validation is performed, so this can be applied to a
     * value read back off an existing owner without risk of rejecting it. This is the single
     * definition of email identity: two emails denote the same address exactly when they
     * canonicalize to the same value, so an email stored via {@link #normalizeEmail(String)} and
     * an email already on an existing owner can be compared for equality after both pass through
     * here. For example {@code "  Jane@Example.COM "} canonicalizes to {@code "jane@example.com"}.
     *
     * @param email the email to canonicalize, or {@code null} when none was supplied
     * @return the trimmed, lower-cased email, or {@code null} when {@code email} is {@code null}
     */
    private String canonicalEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Validates an optional owner postcode. A postcode is optional, so a {@code null} value is
     * accepted and left unchanged. When present it must be exactly 4 digits, and when the owner's
     * city has a known region (see {@link Owner#regionForCity(String)}) the postcode must fall
     * within that region's inclusive range ({@code NSW 2000-2099}, {@code VIC 3000-3099},
     * {@code QLD 4000-4099}, see {@link Owner#postcodeRangeForRegion(String)}). A city with no known
     * region admits any 4-digit postcode. When present but malformed or out of range for the city's
     * region an {@link InvalidFieldsException} is thrown, which the exception advice renders as a
     * 400 response naming the {@code postcode} field.
     *
     * @param postcode the submitted postcode, or {@code null} when omitted
     * @param city the owner's city, used to resolve the region whose range the postcode must satisfy
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("^[0-9]{4}$")) {
            throw new InvalidFieldsException(List.of("postcode"));
        }
        int[] range = Owner.postcodeRangeForRegion(Owner.regionForCity(city));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidFieldsException(List.of("postcode"));
            }
        }
    }

    /**
     * Rejects a new owner whose derived identity key is already used by any existing owner. All
     * duplicate detection is consolidated into this single key (see
     * {@link Owner#identityKey(String, String, String)}): the normalized E.164 telephone, the
     * canonical (trimmed, lower-cased) email and the household identifier the owner would receive,
     * joined by {@code '|'}. The submitted telephone and email have already been normalized in
     * place, and the household identifier is the value {@link #assignHousehold} would later store
     * (see {@link #prospectiveHouseholdId(OwnerFieldsDto)}). An owner is a duplicate only when its
     * whole key equals an existing owner's, so a difference in any part — for example two members
     * of one household with different telephones — yields distinct keys and is allowed. When a
     * match is found a {@link DuplicateIdentityException} is thrown, which the exception advice
     * renders as a 409 Conflict response.
     *
     * @param ownerFieldsDto the submitted owner fields, already normalized in place
     */
    private void rejectDuplicateIdentity(OwnerFieldsDto ownerFieldsDto) {
        String identityKey = Owner.identityKey(ownerFieldsDto.getTelephone(),
            canonicalEmail(ownerFieldsDto.getEmail()), prospectiveHouseholdId(ownerFieldsDto));
        boolean taken = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> identityKey.equals(Owner.identityKey(existing.getTelephone(),
                canonicalEmail(existing.getEmail()), existing.getHouseholdId())));
        if (taken) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * Assigns the shared {@code householdId} for an owner created with {@code sharesHousehold}
     * set to {@code true}. The identifier is the one the owner would receive
     * ({@link #prospectiveHouseholdId(OwnerFieldsDto)}), derived deterministically from its
     * household identity so every owner in the same household resolves to the same stable value.
     * The new owner receives that value, and any existing household mates that do not yet carry it
     * are updated so the whole household shares one identifier. When the request did not opt in the
     * owner is left without a household identifier.
     *
     * @param owner the newly created owner, mutated in place with its household identifier
     * @param ownerFieldsDto the submitted owner fields carrying the {@code sharesHousehold} flag
     */
    private void assignHousehold(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        String householdId = prospectiveHouseholdId(ownerFieldsDto);
        if (householdId == null) {
            return;
        }
        owner.setHouseholdId(householdId);
        for (Owner mate : findHouseholdMates(owner.getLastName(), owner.getAddress())) {
            if (!householdId.equals(mate.getHouseholdId())) {
                mate.setHouseholdId(householdId);
                this.clinicService.saveOwner(mate);
            }
        }
    }

    /**
     * Derives the {@code householdId} a newly submitted owner would receive, without needing the
     * owner to have been mapped or persisted yet. An owner shares a household only when the request
     * opts in via {@code sharesHousehold}; when it does, the identifier is derived deterministically
     * from the owner's household identity (its {@code lastName} and {@code address}, see
     * {@link #householdIdFor(String, String)}), so it matches the value {@link #assignHousehold} will
     * later store and every household mate resolves to the same identifier. When the request did not
     * opt in the owner belongs to no shared household and {@code null} is returned.
     *
     * @param ownerFieldsDto the submitted owner fields carrying the {@code sharesHousehold} flag
     * @return the household identifier the owner would receive, or {@code null} when it shares none
     */
    private String prospectiveHouseholdId(OwnerFieldsDto ownerFieldsDto) {
        if (!Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold())) {
            return null;
        }
        return householdIdFor(ownerFieldsDto.getLastName(), ownerFieldsDto.getAddress());
    }

    /**
     * Derives the stable {@code householdId} shared by every owner in the household identified by
     * the given {@code lastName} and {@code address}. The two fields are normalized exactly as
     * household identity is compared elsewhere (case-insensitively, with collapsed whitespace) and
     * hashed with SHA-256, so any two owners in the same household — regardless of creation order —
     * resolve to the same identifier. The result is formatted as {@code 'HH-<16 hex chars>'}.
     *
     * @param lastName the household's last name
     * @param address the household's address
     * @return the deterministic household identifier
     */
    private String householdIdFor(String lastName, String address) {
        String key = collapseWhitespace(lastName).toLowerCase(java.util.Locale.ROOT) + "|"
            + normalizeAddress(address).toLowerCase(java.util.Locale.ROOT);
        return "HH-" + shaHex(key, 16);
    }

    /**
     * Computes the upper-case hex SHA-256 digest of a string, truncated to its first
     * {@code hexChars} characters. The input is hashed as UTF-8 bytes and each digest byte is
     * rendered as two upper-case hex digits, so {@code hexChars} characters cover the leading
     * {@code ceil(hexChars / 2)} bytes of the digest. This is the single definition of the
     * SHA-256 hex derivation shared by every rule that hashes owner fields (for example the
     * household identifier). SHA-256 is a required platform algorithm; were it ever unavailable
     * an {@link IllegalStateException} is thrown.
     *
     * @param input the string to hash
     * @param hexChars the number of leading upper-case hex characters to return
     * @return the first {@code hexChars} upper-case hex characters of the SHA-256 digest
     */
    private String shaHex(String input, int hexChars) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length && sb.length() < hexChars; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.substring(0, hexChars);
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    /**
     * Counts the owners that belong to the household identified by the given {@code householdId},
     * i.e. every owner carrying that exact identifier. The count is taken against the current set
     * of owners, so after a create it reflects the household's size including the newly persisted
     * owner. An owner without a household (a {@code null} identifier) belongs to no shared
     * household and yields {@code 0}.
     *
     * @param householdId the shared household identifier, or {@code null} when the owner shares no household
     * @return the number of owners in the household, or {@code 0} when {@code householdId} is {@code null}
     */
    private int countHouseholdMembers(String householdId) {
        if (householdId == null) {
            return 0;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * Finds every existing owner that shares a household with the given {@code lastName} and
     * {@code address}. Both fields are compared case-insensitively and with runs of whitespace
     * collapsed to a single space (and surrounding whitespace trimmed), so for example
     * {@code "Franklin"} at {@code "110  W. Liberty St."} matches {@code "franklin"} at
     * {@code "110 W. Liberty St."}. This is the single definition of household identity shared by
     * every rule that reasons about households.
     *
     * @param lastName the last name to match on
     * @param address the address to match on
     * @return the owners sharing the household, in encounter order (possibly empty)
     */
    private List<Owner> findHouseholdMates(String lastName, String address) {
        String normalizedLastName = collapseWhitespace(lastName);
        String normalizedAddress = normalizeAddress(address);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizedLastName.equalsIgnoreCase(collapseWhitespace(existing.getLastName()))
                && normalizedAddress.equalsIgnoreCase(normalizeAddress(existing.getAddress())))
            .toList();
    }

    /**
     * Collapses a field for household comparison: leading and trailing whitespace is trimmed and
     * every internal run of whitespace is reduced to a single space. A {@code null} value collapses
     * to the empty string. Case is left untouched so callers can compare case-insensitively.
     *
     * @param value the value to collapse, or {@code null}
     * @return the whitespace-collapsed value, never {@code null}
     */
    private String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Common street-type abbreviations expanded during address normalization, keyed by their
     * upper-cased token.
     */
    private static final java.util.Map<String, String> ADDRESS_ABBREVIATIONS =
        java.util.Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * Normalizes a submitted address into the canonical form stored and returned for an owner:
     * leading and trailing whitespace is trimmed, every internal run of whitespace is collapsed to
     * a single space, the value is upper-cased, and common street-type abbreviations are expanded
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} value
     * normalizes to the empty string, and an address that holds only whitespace normalizes to the
     * empty string so the required-field check rejects it. For example {@code "  12  main  st "}
     * becomes {@code "12 MAIN STREET"}. This is the single definition of address normalization used
     * everywhere an address is stored or compared.
     *
     * @param address the submitted address value, or {@code null}
     * @return the normalized address, never {@code null}
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
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
}
