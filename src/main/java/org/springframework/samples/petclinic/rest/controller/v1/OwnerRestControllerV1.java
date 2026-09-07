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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Telephones;
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

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final OwnerCreationAuditor ownerCreationAuditor;

    private final WelcomeNotifier welcomeNotifier;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 OwnerCreationAuditor ownerCreationAuditor,
                                 WelcomeNotifier welcomeNotifier) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.ownerCreationAuditor = ownerCreationAuditor;
        this.welcomeNotifier = welcomeNotifier;
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

    /**
     * Remembers, per already-seen {@code Idempotency-Key}, the id of the owner that key
     * originally created, so a retried create with the same key returns that owner instead
     * of creating a duplicate. Held in memory for the application's lifetime and keyed by the
     * raw header value.
     */
    private final Map<String, Integer> idempotentCreates = new ConcurrentHashMap<>();

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto, String idempotencyKey) {
        boolean hasKey = idempotencyKey != null && !idempotencyKey.isBlank();
        if (hasKey) {
            Integer existingId = idempotentCreates.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        ResponseEntity<OwnerDto> response = createNewOwner(ownerFieldsDto);
        if (hasKey && response.getStatusCode() == HttpStatus.CREATED
            && response.getBody() != null && response.getBody().getId() != null) {
            idempotentCreates.putIfAbsent(idempotencyKey, response.getBody().getId());
        }
        return response;
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
        String email = ownerFieldsDto.getEmail();
        currentOwner.setEmail(email == null ? null : email.toLowerCase());
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

    /**
     * Create and persist a brand-new owner from the supplied fields, returning the
     * {@code 201 Created} response for it. This gathers the whole create pipeline in one
     * place — map the fields, {@link #normalizeNewOwner normalise} them, apply the daily
     * and city gates (each with its own rejection status), resolve any household,
     * {@link #assignDerivedAttributes derive the create-time attributes} (including the
     * household level ceiling that caps the owner's membership level), save, audit and
     * build the response with its {@code Location} header — so {@link #addOwner} itself
     * stays a thin entry point. An owner joining an existing household is admitted (its
     * membership level is capped to one above the household maximum rather than rejected):
     * two owners sharing a last name and postcode but with different telephones are no
     * longer a hard duplicate but a soft match. Duplicate detection is the single
     * {@link Owner#getIdentityKey() identity key}: a create whose identity key matches an
     * existing, non-deleted owner is rejected with {@code 409 Conflict}.
     *
     * @param ownerFieldsDto the owner fields from the request body
     * @return {@code 201 Created} with the created owner and its {@code Location} header,
     *         or the appropriate rejection status: {@code 400 Bad Request} when the fields
     *         are invalid, {@code 429 Too Many Requests} when the day's create cap is
     *         reached, or {@code 409 Conflict} when the city is at capacity or the owner's
     *         identity key duplicates an existing owner
     */
    private ResponseEntity<OwnerDto> createNewOwner(OwnerFieldsDto ownerFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        if (!normalizeNewOwner(owner)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (dailyLimitReached(owner.getRegistrationDate())) {
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        }
        if (cityIsAtCapacity(owner.getCity())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        owner.setHouseholdId(owner.computeHouseholdId());
        if (isDuplicateIdentity(owner)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        List<Owner> householdMembers = findHouseholdMembers(owner);
        boolean joinsExistingHousehold = !householdMembers.isEmpty();
        boolean declaredHouseholdMember = joinsExistingHousehold && sharesHousehold;
        assignDerivedAttributes(owner, declaredHouseholdMember, householdMembers);
        this.clinicService.saveOwner(owner);
        ownerCreationAuditor.auditOwnerCreated(owner);
        welcomeNotifier.enqueueWelcome(owner);
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Normalise the incoming fields of a freshly-mapped owner in place, applying
     * the canonical form each stored value must take: the address is trimmed,
     * whitespace-collapsed, upper-cased and its common abbreviations expanded, the
     * telephone is converted to E.164, any email is lower-cased and a missing
     * registration date defaults to today. Field-level rejections are reported here
     * rather than inline in {@link #addOwner}, and normalisation runs before any
     * household comparison so those comparisons see the canonical values.
     *
     * @param owner the freshly-mapped owner to normalise
     * @return {@code true} if the fields are valid, or {@code false} if the address
     *         is blank after normalisation, the telephone cannot form a valid
     *         E.164 number, or a supplied registration date is later than the
     *         server date, in which case the request must be rejected with
     *         {@code 400 Bad Request}
     */
    private boolean normalizeNewOwner(Owner owner) {
        if (!normalizeAddressFields(owner)) {
            return false;
        }
        String telephone = Telephones.toE164(owner.getTelephone());
        if (telephone == null) {
            return false;
        }
        owner.setTelephone(telephone);
        if (owner.getEmail() != null) {
            String email = owner.getEmail().toLowerCase();
            owner.setEmail(email);
            if (emailDomainIsBlocked(email)) {
                return false;
            }
        }
        if (!owner.postcodeValidForLocality()) {
            return false;
        }
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        } else if (owner.getRegistrationDate().isAfter(LocalDate.now())) {
            return false;
        }
        owner.setRegistrationDate(rollToBusinessDay(owner.getRegistrationDate()));
        return true;
    }

    /**
     * Normalise the owner's address fields in place to their canonical stored form
     * (trimmed, whitespace-collapsed, upper-cased and with common abbreviations
     * expanded; see {@link Owner#normalizeAddress(String)}), gathering the whole address
     * concern of {@link #normalizeNewOwner} in one place. The structured form is
     * preferred: when a non-blank {@code addressLine1} is supplied it (and any
     * {@code addressLine2}) is normalised, and the flat {@code address} is composed from
     * them — the normalised {@code addressLine1}, with a single space and the normalised
     * {@code addressLine2} appended when an {@code addressLine2} is present. Otherwise the
     * flat {@code address} is normalised on its own, preserving backward compatibility.
     * Either way the canonicalised values are written back so every later household
     * comparison and read sees them.
     *
     * @param owner the freshly-mapped owner whose address fields are being normalised
     * @return {@code true} once an address is valid and stored, or {@code false} when
     *         neither a structured {@code addressLine1} nor a flat {@code address} is
     *         present after normalisation, in which case the request must be rejected
     *         with {@code 400 Bad Request}
     */
    private boolean normalizeAddressFields(Owner owner) {
        String addressLine1 = Owner.normalizeAddress(owner.getAddressLine1());
        if (!addressLine1.isEmpty()) {
            owner.setAddressLine1(addressLine1);
            String composed = addressLine1;
            String addressLine2 = Owner.normalizeAddress(owner.getAddressLine2());
            if (!addressLine2.isEmpty()) {
                owner.setAddressLine2(addressLine2);
                composed = addressLine1 + " " + addressLine2;
            }
            owner.setAddress(composed);
            return true;
        }
        String address = Owner.normalizeAddress(owner.getAddress());
        if (address.isEmpty()) {
            return false;
        }
        owner.setAddress(address);
        return true;
    }

    /**
     * Email domains that identify disposable, throw-away mailboxes. An owner whose
     * email address is on one of these domains is rejected with {@code 400 Bad
     * Request} rather than created.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Whether the given (already lower-cased) email address belongs to a disposable
     * mailbox domain on the blocklist. The domain is the part after the final
     * {@code '@'}; an address without one is treated as having no blocked domain.
     *
     * @param email the owner's normalised (lower-cased) email address
     * @return {@code true} if the email's domain is on the disposable-domain blocklist
     */
    private static boolean emailDomainIsBlocked(String email) {
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        return DISPOSABLE_EMAIL_DOMAINS.contains(email.substring(at + 1));
    }

    /**
     * The fixed list of public holidays on which the clinic is closed. A registration
     * date that lands on one of these dates is treated the same as a weekend and rolled
     * forward past it (see {@link #rollToBusinessDay(LocalDate)}).
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Roll the given date forward to the next business day when it falls on a weekend or
     * a listed public holiday: a Saturday, Sunday or public holiday is advanced one day
     * at a time until a non-holiday weekday is reached, while a plain business day is
     * returned unchanged. Because rolling off a weekend can land on a holiday (and vice
     * versa), the advance repeats until the date is neither. Applied to the effective
     * registration date (whether supplied in the request or defaulted to the server date)
     * so that every value derived from it uses the adjusted business day.
     *
     * @param date the effective registration date
     * @return the same date if it is a non-holiday weekday, otherwise the next such day
     */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Whether the given date falls on a weekend (Saturday or Sunday).
     */
    private static boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    /**
     * Find the existing owners that belong to the same household as the given owner,
     * i.e. share the same last name (compared case-insensitively with runs of
     * whitespace collapsed to a single space) and postcode, and so resolve to the same
     * deterministic {@code householdId}. Soft-deleted owners are excluded, so the result
     * holds only current household members. Evaluated before the new owner is saved, so
     * it reflects only owners that already existed at creation time; an owner joining no
     * existing household yields an empty list.
     *
     * @param owner the candidate owner being created
     * @return the current members of the owner's household, empty if none exists
     */
    private List<Owner> findHouseholdMembers(Owner owner) {
        return activeOwners()
            .filter(existing -> existing.sameHouseholdAs(owner))
            .toList();
    }

    /**
     * Whether the candidate owner is a hard duplicate of an existing owner, i.e. it shares
     * the {@link Owner#getIdentityKey() identity key} (the SHA-256 digest over its
     * normalised telephone, lower-cased email and the Soundex of its last name) of a
     * current owner. Soft-deleted owners are ignored, so only the current owner population
     * is considered. A create whose identity key collides with an existing owner is
     * rejected with {@code 409 Conflict}. Evaluated after normalisation (so the
     * email-domain blocklist has already been applied) and before the owner is saved.
     *
     * @param owner the candidate owner being created, after normalisation
     * @return {@code true} if an existing, non-deleted owner shares the candidate's
     *         identity key
     */
    private boolean isDuplicateIdentity(Owner owner) {
        String identityKey = owner.getIdentityKey();
        return activeOwners()
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
    }

    /**
     * The existing owners that are still active, i.e. those not flagged as soft-deleted.
     * Evaluated fresh against the saved owners each call, so it reflects only owners that
     * already existed at creation time; a soft-deleted owner is excluded and so takes no
     * part in the household resolution built on top of it. Isolating the exclusion here
     * keeps the "current owner population" one candidate is compared against in one place.
     *
     * @return a stream of the current, non-deleted owners
     */
    private Stream<Owner> activeOwners() {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !Boolean.TRUE.equals(existing.getDeleted()));
    }

    /**
     * Assign the owner's computed-at-creation attributes in place: its member id,
     * namesake count, bulk-signup warning and household size. Unlike
     * the {@code @Transient} attributes {@link Owner} recomputes on demand, these are
     * derived once here — from the owner's own (already normalised) fields and from the
     * wider owner population — and persisted with the owner. Run after normalisation,
     * household resolution and the identity-key check, so every value reflects the
     * owner's final field values and any household it has joined, and immediately before
     * the owner is saved.
     *
     * @param owner the candidate owner being created, after household resolution
     * @param declaredHouseholdMember whether this owner was created as a declared member
     *                                of an existing household (it shared an existing
     *                                owner's household and set {@code sharesHousehold}),
     *                                in which case it is not a suspected duplicate
     * @param householdMembers        the existing members of the owner's household at
     *                                creation time, used to derive its level ceiling
     */
    private void assignDerivedAttributes(Owner owner, boolean declaredHouseholdMember,
            List<Owner> householdMembers) {
        owner.setMemberId(deduplicatedMemberId(owner));
        owner.setNamesakeCount(countNamesakes(owner));
        owner.setBulkSignupWarning(bulkSignupWarning(owner.getRegistrationDate()));
        owner.setCapacityWarning(cityApproachingCapacity(owner.getCity()));
        owner.setHouseholdSize(householdSize(owner));
        owner.setMembershipLevelCap(membershipLevelCap(householdMembers));
        assignPossibleDuplicate(owner, declaredHouseholdMember);
    }

    /**
     * The household level ceiling to assign to the candidate owner: one above the highest
     * {@link Owner#getMembershipLevel() membership level} currently held by an existing
     * member of its household, or {@code null} when the household has no existing member
     * (so no ceiling applies). This bounds a new owner's reported membership level to at
     * most one above its household, keeping households from being outranked at the moment a
     * relative joins. Evaluated before the owner is saved, over the members that already
     * existed at creation time.
     *
     * @param householdMembers the existing members of the owner's household
     * @return the level ceiling to assign, or {@code null} when uncapped
     */
    private Integer membershipLevelCap(List<Owner> householdMembers) {
        return householdMembers.stream()
            .map(Owner::getMembershipLevel)
            .max(Comparator.naturalOrder())
            .map(max -> max + 1)
            .orElse(null);
    }

    /**
     * Compute the member id to assign to the candidate owner, de-duplicated against
     * every existing owner's member id. The base id is the owner's computed
     * {@code <REGION><FY><HASH8><CHK>} (see {@link Owner#computeMemberId()}); when it does
     * not already belong to an existing owner it is used unchanged. Otherwise it collides,
     * and {@code -<n>} is appended with the smallest integer {@code n} of 2 or more that
     * yields an id no existing owner holds, so the returned member id is unique. Evaluated
     * before the owner is saved, so only pre-existing owners are considered.
     *
     * @param owner the candidate owner being created, after normalisation
     * @return the unique (de-duplicated) member id to assign
     */
    private String deduplicatedMemberId(Owner owner) {
        Set<String> existingIds = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(id -> id != null)
            .collect(java.util.stream.Collectors.toSet());
        String base = owner.computeMemberId();
        if (!existingIds.contains(base)) {
            return base;
        }
        int n = 2;
        while (existingIds.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /**
     * Flag the candidate owner as a possible duplicate of an existing owner. A soft
     * match is an existing owner that shares this owner's last name (compared
     * case-insensitively) and postcode but carries a different (normalised) telephone.
     * When such a match exists the owner is still created, but with
     * {@code possibleDuplicate} true and {@code possibleDuplicateOf} set to the matching
     * owner's id; otherwise {@code possibleDuplicate} is false and no match id is
     * recorded. The earliest-created matching owner (lowest id) is chosen so the result
     * is deterministic. Evaluated before the owner is saved, so only pre-existing owners
     * are considered.
     *
     * <p>A declared household member — one that shared an existing owner's household
     * (same last name and postcode) and was admitted because it set
     * {@code sharesHousehold} — is never flagged: a declared member is not a suspected
     * duplicate, so its {@code possibleDuplicate} is false with no match id.
     *
     * @param owner the candidate owner being created, after household resolution
     * @param declaredHouseholdMember whether the owner is a declared household member
     */
    private void assignPossibleDuplicate(Owner owner, boolean declaredHouseholdMember) {
        if (declaredHouseholdMember) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        Optional<Owner> match = this.clinicService.findAllOwners().stream()
            .filter(existing -> isSoftMatch(owner, existing))
            .min(Comparator.comparing(Owner::getId));
        if (match.isPresent()) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.get().getId());
        } else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }

    /**
     * Whether an already-created {@code existing} owner is a soft-match for the candidate
     * {@code owner}: their {@link Owner#getIdentityKey() identity keys} differ (so they are
     * not a hard duplicate) yet the {@link Owner#soundex(String) Soundex} of their last
     * names and their postcodes match. This is the single soft-match test
     * {@link #assignPossibleDuplicate} applies across the owner population, isolated here so
     * the comparison that decides a suspected duplicate lives in one place. Two owners
     * sharing a last name and postcode but with different telephones therefore soft-match
     * rather than being rejected, since the telephone is part of the identity key.
     *
     * @param owner    the candidate owner being created
     * @param existing an already-created owner to test against it
     * @return {@code true} if {@code existing} is a soft-match for the candidate
     */
    private boolean isSoftMatch(Owner owner, Owner existing) {
        String postcode = owner.getPostcode();
        return postcode != null && postcode.equals(existing.getPostcode())
            && Owner.soundex(owner.getLastName()).equals(Owner.soundex(existing.getLastName()))
            && !owner.getIdentityKey().equals(existing.getIdentityKey());
    }

    /**
     * Count how many owners belong to the given owner's household once this create
     * completes: the owners that already share its deterministic {@code householdId}
     * (derived from last name and postcode) plus the owner being created. Evaluated
     * after the owner's {@code householdId} has been assigned. An owner whose
     * {@code householdId} matches no existing owner is a household of one.
     *
     * @param owner the candidate owner being created, after household resolution
     * @return the household member count after this create (at least {@code 1})
     */
    private int householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(other -> householdId.equals(other.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * Count the existing owners that share the given owner's first and last name,
     * compared case-insensitively (surrounding whitespace trimmed). Evaluated
     * before the new owner is saved, so the result reflects only owners that
     * already existed at creation time.
     *
     * @param owner the candidate owner being created
     * @return the number of existing namesakes
     */
    private int countNamesakes(Owner owner) {
        String firstName = normalizeName(owner.getFirstName());
        String lastName = normalizeName(owner.getLastName());
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getFirstName()).equals(firstName)
                && normalizeName(existing.getLastName()).equals(lastName))
            .count();
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    /**
     * Determine whether the given city has already reached its owner capacity, i.e.
     * it already contains 50 or more existing owners (compared case-insensitively
     * with surrounding whitespace trimmed). A new owner may not be created in a city
     * that is at capacity.
     *
     * @param city the candidate owner's city
     * @return {@code true} if the city already holds 50 or more owners
     */
    private boolean cityIsAtCapacity(String city) {
        return ownersInCity(city) >= 50;
    }

    /**
     * Determine whether the given city is approaching its owner capacity, i.e. it
     * already contains between 40 and 49 existing owners (compared case-insensitively
     * with surrounding whitespace trimmed). This warns that the capacity limit of 50
     * is near; the hard rejection at 50 is handled separately by
     * {@link #cityIsAtCapacity(String)}. Evaluated before the new owner is saved, so
     * it reflects only owners that already existed at creation time.
     *
     * @param city the candidate owner's city
     * @return {@code true} if the city already holds between 40 and 49 owners
     */
    private boolean cityApproachingCapacity(String city) {
        long count = ownersInCity(city);
        return count >= 40 && count < 50;
    }

    /**
     * Count the existing owners in the given city, compared case-insensitively with
     * surrounding whitespace trimmed.
     *
     * @param city the city to count owners for
     * @return the number of existing owners in that city
     */
    private long ownersInCity(String city) {
        String normalizedCity = normalizeName(city);
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> normalizeName(existing.getCity()).equals(normalizedCity))
            .count();
    }

    /**
     * Determine whether the daily owner-creation limit has already been reached, i.e.
     * 100 or more owners already carry the given registration date. A new owner may
     * not be created once that day's cap is reached.
     *
     * @param registrationDate the registration date the new owner would be created with
     * @return {@code true} if 100 or more existing owners share that registration date
     */
    private boolean dailyLimitReached(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count >= 100;
    }

    /**
     * Determine whether more than 80 owners have already been created for the given
     * registration date, i.e. strictly more than 80 existing owners carry that date.
     * Evaluated before the new owner is saved, so it reflects only owners that
     * already existed at creation time.
     *
     * @param registrationDate the registration date the new owner would be created with
     * @return {@code true} if more than 80 existing owners share that registration date
     */
    private boolean bulkSignupWarning(LocalDate registrationDate) {
        long count = this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return count > 80;
    }
}
