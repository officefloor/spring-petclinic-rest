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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final HttpServletRequest request;

    /**
     * HTTP header carrying the client-supplied idempotency key for the create endpoint. When a
     * create repeats with a key already seen, the originally created owner is returned instead of
     * creating a duplicate.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers, per seen {@code Idempotency-Key}, the id of the owner that key's create produced,
     * so a repeat create with the same key can return the originally created owner rather than
     * persisting a duplicate. Shared across requests, hence concurrency-safe.
     */
    private static final Map<String, Integer> IDEMPOTENT_CREATES = new ConcurrentHashMap<>();

    /**
     * Source of the {@code seq} carried by each structured {@code OWNER_CREATED} audit event: a
     * monotonically increasing integer stamped once per successful create, in creation order.
     * Shared across requests, hence concurrency-safe.
     */
    private static final AtomicLong OWNER_CREATED_SEQ = new AtomicLong(0);

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 HttpServletRequest request) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.request = request;
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
        return okOwnerResponse(owner);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = idempotencyKey();
        if (idempotencyKey != null) {
            ResponseEntity<OwnerDto> replay = replayIdempotentCreate(idempotencyKey);
            if (replay != null) {
                return replay;
            }
        }
        Owner owner = createOwner(ownerFieldsDto);
        if (idempotencyKey != null) {
            IDEMPOTENT_CREATES.put(idempotencyKey, owner.getId());
        }
        return createdOwnerResponse(owner);
    }

    /**
     * Reads the request's {@code Idempotency-Key} header, returning the trimmed key or {@code null}
     * when the header is absent or blank. A blank key carries no intent to deduplicate, so the
     * create proceeds normally.
     *
     * @return the non-blank idempotency key, or {@code null} when none was supplied
     */
    private String idempotencyKey() {
        String key = this.request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (key == null || key.isBlank()) {
            return null;
        }
        return key.trim();
    }

    /**
     * Replays the create originally produced by the given {@code Idempotency-Key}, if any: when the
     * key has already created an owner that still exists, that owner is rendered with {@code 200 OK}
     * (rather than creating a duplicate with {@code 201 Created}). When the key has not been seen, or
     * the owner it created no longer exists, {@code null} is returned so the caller creates the owner
     * normally.
     *
     * @param idempotencyKey the non-blank idempotency key supplied on the request
     * @return the {@code 200 OK} response carrying the originally created owner, or {@code null}
     */
    private ResponseEntity<OwnerDto> replayIdempotentCreate(String idempotencyKey) {
        Integer existingId = IDEMPOTENT_CREATES.get(idempotencyKey);
        if (existingId == null) {
            return null;
        }
        Owner existing = this.clinicService.findOwnerById(existingId);
        if (existing == null) {
            return null;
        }
        return okOwnerResponse(existing);
    }

    /**
     * Runs the full intake pipeline for a newly submitted owner and persists the result, returning
     * the saved {@link Owner}. The submitted fields are normalized and validated
     * ({@link #prepareNewOwner(OwnerFieldsDto, LocalDate)}) against the effective business-day
     * {@code registrationDate} ({@link #effectiveRegistrationDate(OwnerFieldsDto)}); the owner is
     * then mapped, stamped with that registration date and every derived value (member id,
     * namesake count, household), flagged as a possible duplicate where applicable, saved, and
     * finally stamped with its household member count. A rule that rejects the owner signals it by
     * throwing, which the exception advice renders as the matching 4xx response, so this method
     * returns only for an owner that was accepted and persisted. The create is audited once the
     * owner has its identity.
     *
     * <p>The returned owner carries its generated id and every stored field, ready to be rendered by
     * {@link #createdOwnerResponse(Owner)}.
     *
     * @param ownerFieldsDto the submitted owner fields, normalized in place
     * @return the persisted owner
     */
    private Owner createOwner(OwnerFieldsDto ownerFieldsDto) {
        LocalDate registrationDate = effectiveRegistrationDate(ownerFieldsDto);
        prepareNewOwner(ownerFieldsDto, registrationDate);
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setMemberId(memberIdFor(owner));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        assignHousehold(owner);
        applyPossibleDuplicate(owner, ownerFieldsDto);
        owner.setMembershipLevelCap(householdMembershipLevelCap(owner.getHouseholdId()));
        this.clinicService.saveOwner(owner);
        applyHouseholdMemberCount(owner);
        auditOwnerCreated(owner);
        emitOwnerCreatedEvent(owner);
        enqueueWelcomeNotification(owner);
        return owner;
    }

    /**
     * Enqueues a welcome notification for a freshly persisted owner by emitting a line to the
     * dedicated {@code NOTIFY} logger carrying the owner's {@code id} and its unified
     * {@code memberId}. This runs only on a successful create — once the owner has its generated
     * identity and derived member id — so downstream consumers watching the {@code NOTIFY} logger
     * can react to new members structurally.
     *
     * @param owner the persisted owner, already carrying its generated id and derived member id
     */
    private void enqueueWelcomeNotification(Owner owner) {
        NOTIFY.info("Welcome notification queued: id={} memberId={}",
            owner.getId(), owner.getMemberId());
    }

    /**
     * Writes the human-readable {@code Owner created} audit line for a freshly persisted owner,
     * carrying its {@code id}, its unified {@code memberId}, its stored {@code registrationDate} and
     * its derived {@code membershipLevel}. This is the free-form companion to the structured
     * {@code OWNER_CREATED} event (see {@link #emitOwnerCreatedEvent(Owner)}); isolating it here
     * keeps {@link #createOwner(OwnerFieldsDto)} a thin orchestration and gives the audit line one
     * place to grow.
     *
     * @param owner the persisted owner, already carrying its generated id and derived values
     */
    private void auditOwnerCreated(Owner owner) {
        AUDIT.info("Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            owner.getMembershipLevel());
    }

    /**
     * Emits the immutable, structured {@code OWNER_CREATED} audit event for a freshly persisted owner,
     * alongside the human-readable audit line. The event is a JSON object carrying, in order, a
     * {@code seq} (a monotonically increasing integer across creates, see {@link #OWNER_CREATED_SEQ}),
     * the owner's {@code ownerId}, the owner's current primary identifier (see
     * {@link #primaryIdentifier(Owner)}) under the {@code memberId} key, the owner's derived
     * {@code membershipLevel}, and the {@code event} marker {@code 'OWNER_CREATED'}. Published to the
     * dedicated {@code AUDIT} logger so downstream consumers can react to owner creation structurally
     * rather than by parsing the free-form line.
     *
     * @param owner the persisted owner, already carrying its generated id and derived values
     */
    private void emitOwnerCreatedEvent(Owner owner) {
        long seq = OWNER_CREATED_SEQ.incrementAndGet();
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"memberId\":{},\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            seq, owner.getId(), jsonString(primaryIdentifier(owner)), owner.getMembershipLevel());
    }

    /**
     * Returns the owner's current primary identifier, the single identity the structured
     * {@code OWNER_CREATED} event carries: the owner's unified {@code memberId}. This is the one seam
     * that decides which field is primary, so the event carries the member id under its
     * {@code memberId} key.
     *
     * @param owner the persisted owner whose primary identifier the event should carry
     * @return the owner's current primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Renders a value as a JSON string literal for the structured audit event: {@code null} becomes
     * the JSON {@code null} keyword, otherwise the value is quoted with the JSON-significant
     * characters ({@code "} and {@code \}) escaped. The derived identifiers this carries never contain
     * such characters, but escaping keeps the emitted event valid JSON regardless.
     *
     * @param value the value to render, or {@code null}
     * @return the JSON representation of the value
     */
    private String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /**
     * Renders a persisted owner as the {@code 201 Created} response of the create endpoint. The owner
     * is mapped to an {@link OwnerDto}, its {@code bulkSignupWarning} is derived from the owner's
     * stored {@code registrationDate} (mirroring {@link #getOwner(Integer)}), and a {@code Location}
     * header pointing at {@code /api/owners/{id}} is attached. Isolating the response assembly here
     * keeps {@link #addOwner(OwnerFieldsDto)} a thin orchestration of "create the owner, then render
     * it" and gives the created representation one place to grow.
     *
     * @param owner the persisted owner to render, carrying its generated id and stored fields
     * @return the {@code 201 Created} response carrying the owner representation and {@code Location} header
     */
    private ResponseEntity<OwnerDto> createdOwnerResponse(Owner owner) {
        OwnerDto ownerDto = toOwnerDtoWithWarning(owner);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
    }

    /**
     * Renders a persisted owner as the {@code 200 OK} response shared by the read path
     * ({@link #getOwner(Integer)}) and the idempotent-replay path
     * ({@link #replayIdempotentCreate(String)}): the owner is stamped with its household member
     * count and then mapped to an {@link OwnerDto} carrying its {@code bulkSignupWarning} (see
     * {@link #toOwnerDtoWithWarning(Owner)}). Isolating the assembly here keeps both paths rendering
     * an existing owner identically rather than each copying the same steps.
     *
     * @param owner the persisted owner to render
     * @return the {@code 200 OK} response carrying the owner representation
     */
    private ResponseEntity<OwnerDto> okOwnerResponse(Owner owner) {
        applyHouseholdMemberCount(owner);
        return new ResponseEntity<>(toOwnerDtoWithWarning(owner), HttpStatus.OK);
    }

    /**
     * Maps a persisted owner to the {@link OwnerDto} returned by every rendered response, stamping
     * its {@code bulkSignupWarning} from the owner's stored {@code registrationDate} (see
     * {@link #bulkSignupWarning(LocalDate)}) and its {@code capacityWarning} from the owner's
     * {@code city} (see {@link #capacityWarning(String)}). This is the single definition of how an
     * owner becomes its response representation, shared by the read, idempotent-replay and create
     * responses, so all three derive the rendered fields identically rather than each copying the
     * mapping.
     *
     * @param owner the persisted owner to map
     * @return the owner representation with its {@code bulkSignupWarning} and {@code capacityWarning} set
     */
    private OwnerDto toOwnerDtoWithWarning(Owner owner) {
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(bulkSignupWarning(owner.getRegistrationDate()));
        ownerDto.setCapacityWarning(capacityWarning(owner.getCity()));
        ownerDto.setRiskFlag(riskFlag(owner));
        return ownerDto;
    }

    /**
     * Reports whether an owner should be flagged for review. The flag is true when any of three
     * independent signals hold and false only when none does: the owner is a possible (soft)
     * duplicate (its {@code possibleDuplicate} flag is set, see
     * {@link #applyPossibleDuplicate(Owner, OwnerFieldsDto)}), the owner's email domain is
     * disposable-adjacent (see {@link #isDisposableAdjacentEmail(String)}) or the owner's city is
     * over its soft capacity (see {@link #capacityWarning(String)}). This is the single definition
     * of an owner's risk flag, shared by every rendered response.
     *
     * @param owner the persisted owner to assess
     * @return {@code true} when any risk signal holds, otherwise {@code false}
     */
    private boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || isDisposableAdjacentEmail(owner.getEmail())
            || capacityWarning(owner.getCity());
    }

    /**
     * Reports whether an email's domain is disposable-adjacent: its domain (see
     * {@link #emailDomain(String)}) equals one of the known disposable domains (see
     * {@link #DISPOSABLE_EMAIL_DOMAINS}) or is a subdomain of one (it ends with {@code '.'} followed
     * by a disposable domain), compared case-insensitively. An exact disposable domain is already
     * rejected at intake by {@link #normalizeEmail(String)}, so a persisted owner reaches this rule
     * only through a subdomain of a disposable domain. An owner without an email has no domain and
     * is never disposable-adjacent.
     *
     * @param email the owner's stored email, or {@code null} when none was supplied
     * @return {@code true} when the email's domain is a disposable domain or a subdomain of one
     */
    private boolean isDisposableAdjacentEmail(String email) {
        String domain = emailDomain(email);
        if (domain == null) {
            return false;
        }
        return DISPOSABLE_EMAIL_DOMAINS.stream()
            .anyMatch(disposable -> domain.equals(disposable) || domain.endsWith("." + disposable));
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
        currentOwner.setTitle(ownerFieldsDto.getTitle());
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
        applyAddress(ownerFieldsDto);
        validateRequiredFields(ownerFieldsDto);
        ownerFieldsDto.setTelephone(normalizeTelephone(ownerFieldsDto.getTelephone()));
        ownerFieldsDto.setEmail(normalizeEmail(ownerFieldsDto.getEmail()));
        validatePostcode(ownerFieldsDto.getPostcode(), ownerFieldsDto.getCity());
        rejectDuplicateIdentity(ownerFieldsDto);
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        rejectDailyLimitExceeded(registrationDate);
    }

    /**
     * Applies an owner's address to the submitted fields, normalizing it in place into the
     * canonical form stored and returned for the owner. This is the single intake step that owns
     * how a submitted address becomes an owner's stored address: it delegates every transform to
     * {@link #normalizeAddress(String)}, the single definition of address normalization, and writes
     * the results back onto {@code ownerFieldsDto}.
     *
     * <p>The structured fields are preferred when supplied: when a non-blank {@code addressLine1}
     * is present the two structured lines are normalized in place and the flat {@code address} is
     * (re)composed from them — the normalized {@code addressLine1}, with a single space and the
     * normalized {@code addressLine2} appended only when an {@code addressLine2} was supplied — so
     * every rule that later reads {@code address} sees the structured value. When no structured
     * {@code addressLine1} is supplied the flat {@code address} is normalized in place, preserving
     * backward compatibility.
     *
     * <p>It runs ahead of {@link #validateRequiredFields(OwnerFieldsDto)} so an address that is
     * blank after normalization in either form is rejected by the required-field check that
     * follows. Isolating the step here keeps {@link #prepareNewOwner(OwnerFieldsDto, LocalDate)} a
     * list of named intake steps and gives address handling one place to grow.
     *
     * @param ownerFieldsDto the submitted owner fields, whose address is normalized in place
     */
    private void applyAddress(OwnerFieldsDto ownerFieldsDto) {
        String line1 = ownerFieldsDto.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String normalizedLine1 = normalizeAddress(line1);
            ownerFieldsDto.setAddressLine1(normalizedLine1);
            String line2 = ownerFieldsDto.getAddressLine2();
            if (line2 != null && !line2.isBlank()) {
                String normalizedLine2 = normalizeAddress(line2);
                ownerFieldsDto.setAddressLine2(normalizedLine2);
                ownerFieldsDto.setAddress(normalizedLine1 + " " + normalizedLine2);
            } else {
                ownerFieldsDto.setAddressLine2(null);
                ownerFieldsDto.setAddress(normalizedLine1);
            }
        } else {
            ownerFieldsDto.setAddress(normalizeAddress(ownerFieldsDto.getAddress()));
        }
    }

    /**
     * Determines the effective registration date for a newly submitted owner, adjusted so it always
     * falls on a business day. The date supplied on the request is used when present, otherwise the
     * server's current date is used. When that effective date lands on a Saturday, Sunday or listed
     * public holiday it is rolled forward to the next non-holiday business day; a non-holiday weekday
     * is left unchanged. This single adjusted date
     * is what gets stored as {@code registrationDate} and is what every value derived from it (the
     * membership number's year segment, the per-day create limit) is computed against.
     *
     * A supplied registration date later than the server's current date is rejected with an
     * {@link InvalidFieldsException}, which the exception advice renders as a 400 Bad Request.
     *
     * @param ownerFieldsDto the submitted owner fields, which may carry a registration date
     * @return the effective registration date rolled forward to the next business day
     */
    private LocalDate effectiveRegistrationDate(OwnerFieldsDto ownerFieldsDto) {
        LocalDate supplied = ownerFieldsDto.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new InvalidFieldsException(List.of("registrationDate"));
        }
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        return toBusinessDay(effective);
    }

    /**
     * The fixed public holidays the business-day roll skips. A date landing on any of these is
     * advanced, along with weekends, until it reaches the next non-holiday business day.
     */
    private static final java.util.Set<LocalDate> PUBLIC_HOLIDAYS = java.util.Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Rolls a date forward to the next business day: a Saturday, Sunday or listed public holiday is
     * advanced one day at a time until it reaches a weekday that is not a public holiday, while a
     * date already on such a business day is returned unchanged.
     *
     * @param date the date to adjust
     * @return the same date when it is a non-holiday weekday, otherwise the next such business day
     */
    private LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
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
        if (countOwnersRegisteredOn(registrationDate) >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
    }

    /**
     * Counts the existing owners whose adjusted business-day {@code registrationDate} equals the
     * given date. This is the single definition of the per-day owner count, shared by the per-day
     * create limit ({@link #rejectDailyLimitExceeded(LocalDate)}) and the bulk-signup warning
     * ({@link #bulkSignupWarning(LocalDate)}), so both accumulate owners against a day identically.
     *
     * @param registrationDate the adjusted business-day registration date to count owners against
     * @return the number of existing owners carrying that registration date
     */
    private long countOwnersRegisteredOn(LocalDate registrationDate) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
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
        return countOwnersRegisteredOn(registrationDate) > BULK_SIGNUP_WARNING_THRESHOLD;
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
        if (countOwnersInCity(city) >= CITY_OWNER_CAPACITY) {
            throw new CityCapacityExceededException(city);
        }
    }

    /**
     * Counts the existing owners whose {@code city} equals the given city, compared
     * case-insensitively. This is the single definition of the per-city owner count, shared by the
     * hard capacity rejection ({@link #rejectCityAtCapacity(String)}) and the approaching-capacity
     * warning ({@link #capacityWarning(String)}), so both measure a city's occupancy identically.
     *
     * @param city the city to count owners against
     * @return the number of existing owners whose city matches
     */
    private long countOwnersInCity(String city) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
            .count();
    }

    /**
     * The number of owners a city must already contain before it is flagged as approaching its
     * capacity limit. Once a city holds at least this many owners (but is not yet at
     * {@link #CITY_OWNER_CAPACITY}), the create and read responses report {@code capacityWarning}
     * true for owners in that city.
     */
    private static final long CITY_CAPACITY_WARNING_THRESHOLD = 40L;

    /**
     * Reports whether the given city is approaching its capacity limit, i.e. whether it already
     * contains between {@link #CITY_CAPACITY_WARNING_THRESHOLD} and {@link #CITY_OWNER_CAPACITY}
     * minus one owners (40 to 49) inclusive. The count is taken the same way the hard rejection
     * counts a city's owners ({@link #countOwnersInCity(String)}). A {@code null} city never warns.
     *
     * @param city the city to test, or {@code null}
     * @return {@code true} when the city holds 40 to 49 owners, otherwise {@code false}
     */
    private boolean capacityWarning(String city) {
        if (city == null) {
            return false;
        }
        long count = countOwnersInCity(city);
        return count >= CITY_CAPACITY_WARNING_THRESHOLD && count < CITY_OWNER_CAPACITY;
    }

    /**
     * Builds the unified {@code memberId} assigned to a newly created owner. The id is formatted as
     * {@code '<REGION><FY><HASH8><CHK>'}, concatenating (with no separators) the owner's region code
     * (see {@link Owner#getRegionCode()}, rendered {@code 'UNKNOWN'} when the owner has no known
     * region), the two-digit fiscal-year segment its business-day-adjusted {@code registrationDate}
     * falls in (see {@link Owner#fiscalYearSegment(LocalDate)}), its identity hash (see
     * {@link Owner#getIdentityHash()}, the first 8 upper-case hex characters of the SHA-256 digest
     * over the owner's normalized E.164 {@code telephone} concatenated with its {@code lastName}) and
     * a single Luhn check digit (see {@link Owner#luhnCheckDigit(String)}) computed over the digits of
     * {@code <REGION><FY><HASH8>}. The composed base is then de-duplicated against existing owners
     * (see {@link #deduplicateMemberId(String)}). The base carries no sequence number, so two owners
     * sharing a region, fiscal year, telephone and last name resolve to the same base and are
     * separated only by de-duplication.
     *
     * @param owner the owner being created, whose fields the id is derived from
     * @return the formatted member id
     */
    private String memberIdFor(Owner owner) {
        String prefix = owner.getRegionCode()
            + Owner.fiscalYearSegment(owner.getRegistrationDate())
            + owner.getIdentityHash();
        return deduplicateMemberId(prefix + Owner.luhnCheckDigit(prefix));
    }

    /**
     * Ensures the computed {@code memberId} is unique across existing owners. When the given base id
     * collides with an existing owner's {@code memberId}, {@code '-<n>'} is appended with the smallest
     * {@code n} of 2 or more that yields an id no existing owner already carries.
     *
     * @param baseCode the member id computed for the owner being created
     * @return the base id when it is already unique, otherwise the de-duplicated id
     */
    private String deduplicateMemberId(String baseCode) {
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (!existing.contains(baseCode)) {
            return baseCode;
        }
        int n = 2;
        while (existing.contains(baseCode + "-" + n)) {
            n++;
        }
        return baseCode + "-" + n;
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
     * Flags a newly created owner as a possible (soft) duplicate. Reaching this point means the
     * owner already cleared {@link #rejectDuplicateIdentity(OwnerFieldsDto)}, so it does not share
     * an existing owner's identity key. A member that declared its household via
     * {@code sharesHousehold} is a deliberate addition to a known household, not a suspected
     * duplicate, so it is never flagged. Otherwise it is a possible duplicate when an existing owner
     * shares the Soundex code of its {@code lastName} and its {@code postcode} while carrying a
     * different identity key (see {@link #findPossibleDuplicate(Owner)}). When such an owner is
     * found, {@code possibleDuplicate} is set {@code true} and {@code possibleDuplicateOf} to the
     * matching owner's id; otherwise {@code possibleDuplicate} is set {@code false} and no matching
     * id is recorded. An owner without a postcode can share no postcode and is never a possible
     * duplicate.
     *
     * @param owner the owner being created, already carrying its normalized telephone and postcode
     * @param ownerFieldsDto the submitted owner fields carrying the {@code sharesHousehold} flag
     */
    private void applyPossibleDuplicate(Owner owner, OwnerFieldsDto ownerFieldsDto) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        if (sharesHousehold(ownerFieldsDto)) {
            return;
        }
        findPossibleDuplicate(owner).ifPresent(existing -> {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(existing.getId());
        });
    }

    /**
     * Finds the existing owner, if any, that makes the given newly created owner a possible (soft)
     * duplicate: a non-deleted owner whose {@code lastName} shares the new owner's Soundex code (see
     * {@link Owner#soundex(String)}) and whose {@code postcode} matches, while carrying a different
     * identity key (see {@link Owner#identityKey(String, String, String)}). Requiring a differing
     * identity key keeps a hard duplicate — already rejected with 409 by
     * {@link #rejectDuplicateIdentity(OwnerFieldsDto)} — from also being flagged as a soft match, so
     * the two household members whose telephones differ (hence differing identity keys, but matching
     * Soundex and postcode) are exactly the soft matches surfaced here. This is the single definition
     * of the soft-duplicate match; {@link #applyPossibleDuplicate(Owner, OwnerFieldsDto)} only records
     * the outcome on the owner. An owner without a postcode can share no postcode and matches nothing,
     * so {@link java.util.Optional#empty()} is returned.
     *
     * @param owner the owner being created, already carrying its normalized telephone and postcode
     * @return the matching existing owner, or {@link java.util.Optional#empty()} when none matches
     */
    private java.util.Optional<Owner> findPossibleDuplicate(Owner owner) {
        if (owner.getPostcode() == null) {
            return java.util.Optional.empty();
        }
        String soundex = Owner.soundex(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !Owner.isDeleted(existing.getDeleted()))
            .filter(existing -> !identityKey.equals(existing.getIdentityKey())
                && soundex.equals(Owner.soundex(existing.getLastName()))
                && owner.getPostcode().equals(existing.getPostcode()))
            .findFirst();
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
     * Validates the national-number length of an already syntactically valid E.164 telephone
     * against its country code. When the number carries a country code this application recognizes
     * (see {@link Owner#countryCodeOf(String)}) the digits following that code must have exactly the
     * length that code admits ({@code '+61'} => 9 national digits, {@code '+1'} => 10, see
     * {@link Owner#nationalNumberLength(String)}). When the country code is recognized but the
     * national number is the wrong length an {@link InvalidFieldsException} is thrown, which the
     * exception advice renders as a 400 response naming the {@code telephone} field. A country code
     * that is not recognized carries no per-country length rule and is left to the general
     * 8-to-15-digit E.164 check.
     *
     * @param e164 the normalized E.164 telephone (a {@code '+'} followed by digits)
     */
    private void validateNationalNumberLength(String e164) {
        String countryCode = Owner.countryCodeOf(e164);
        if (countryCode == null) {
            return;
        }
        int nationalLength = e164.length() - 1 - countryCode.length();
        if (nationalLength != Owner.nationalNumberLength(countryCode)) {
            throw new InvalidFieldsException(List.of("telephone"));
        }
    }

    /**
     * Pattern for a syntactically valid email address: a non-empty local part, an {@code @},
     * and a domain containing at least one dot, none of the parts holding whitespace or a
     * second {@code @}.
     */
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /**
     * Disposable email domains an owner may not register with. An email whose domain (compared
     * case-insensitively) is one of these is rejected, since it identifies a throwaway mailbox.
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Normalizes an optional owner email. An email is optional, so a {@code null} value is
     * accepted and returned unchanged. When present it must be a syntactically valid address
     * whose domain is not on the disposable-domain blocklist (see
     * {@link #DISPOSABLE_EMAIL_DOMAINS}); the value is stored and returned lower-cased. When
     * present but syntactically invalid or on a disposable domain an
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
        String domain = emailDomain(trimmed);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
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
     * Extracts the lower-cased domain of an email — everything after its final {@code '@'}. This is
     * the single definition of an email's domain, shared wherever an owner's email is matched by its
     * domain (for example the disposable-domain blocklist applied in {@link #normalizeEmail(String)}),
     * so every such rule reads the domain identically rather than re-deriving it. A {@code null} email
     * (none was supplied) has no domain and yields {@code null}. Like {@link #canonicalEmail(String)}
     * no validation is performed, so this can be applied to a value read back off an existing owner as
     * readily as to one being normalized; a caller that needs a syntactically valid address validates
     * it first. The domain is lower-cased so it compares case-insensitively, matching how
     * {@link #DISPOSABLE_EMAIL_DOMAINS} is compared. For example {@code "jane@Example.COM"} yields
     * {@code "example.com"}.
     *
     * @param email the email whose domain to extract, or {@code null} when none was supplied
     * @return the lower-cased domain, or {@code null} when {@code email} is {@code null}
     */
    private String emailDomain(String email) {
        if (email == null) {
            return null;
        }
        return email.substring(email.lastIndexOf('@') + 1).toLowerCase(java.util.Locale.ROOT);
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
     * Rejects a new owner that is an exact identity duplicate of an existing owner. An owner's
     * identity is its single derived identity key (see
     * {@link Owner#identityKey(String, String, String)}) — the SHA-256 hex digest over its
     * normalized {@code telephone}, canonical {@code email} and the Soundex code of its
     * {@code lastName} — so a new owner is a hard duplicate only when some existing, non-deleted
     * owner carries the same identity key. The email-domain blocklist has already been applied when
     * the email was normalized ahead of this check, so a disposable-domain email is rejected before
     * duplicate detection runs. Two owners that merely belong to the same household (the same last
     * name and postcode) but carry different telephones now have distinct identity keys and are not
     * rejected here; such a near-match is instead recorded as a soft {@code possibleDuplicate} (see
     * {@link #applyPossibleDuplicate(Owner, OwnerFieldsDto)}). When an existing owner shares the new
     * owner's identity key a {@link DuplicateIdentityException} is thrown, which the exception advice
     * renders as a 409 Conflict response. Setting {@code sharesHousehold} declares the owner a
     * deliberate household member and bypasses this block.
     *
     * @param ownerFieldsDto the submitted owner fields, already normalized in place
     */
    private void rejectDuplicateIdentity(OwnerFieldsDto ownerFieldsDto) {
        if (sharesHousehold(ownerFieldsDto)) {
            return;
        }
        String identityKey = Owner.identityKey(ownerFieldsDto.getTelephone(),
            ownerFieldsDto.getEmail(), ownerFieldsDto.getLastName());
        boolean taken = this.clinicService.findAllOwners().stream()
            .filter(existing -> !Owner.isDeleted(existing.getDeleted()))
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (taken) {
            throw new DuplicateIdentityException(identityKey);
        }
    }

    /**
     * Assigns the deterministic shared {@code householdId} to a newly created owner. The identifier
     * is derived from the owner's household identity — its {@code lastName} and {@code postcode} (see
     * {@link #householdIdFor(String, String)}) — so every owner sharing a last name and postcode
     * resolves to the same stable value automatically, without any explicit linking. An owner without
     * a postcode belongs to no shared household and is left without a household identifier.
     *
     * @param owner the newly created owner, mutated in place with its household identifier
     */
    private void assignHousehold(Owner owner) {
        owner.setHouseholdId(householdIdFor(owner.getLastName(), owner.getPostcode()));
    }

    /**
     * Reports whether a submitted owner opted into sharing a household, i.e. whether its request
     * carried {@code sharesHousehold} set to {@code true}. A {@code null} or {@code false} flag is
     * not opting in. This is the single reading of the {@code sharesHousehold} intent, shared by
     * every rule that treats a declared household member differently from an independent owner.
     *
     * @param ownerFieldsDto the submitted owner fields carrying the {@code sharesHousehold} flag
     * @return {@code true} when the request opted into sharing a household
     */
    private boolean sharesHousehold(OwnerFieldsDto ownerFieldsDto) {
        return Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
    }

    /**
     * Derives the stable {@code householdId} shared by every owner in the household identified by
     * the given {@code lastName} and {@code postcode}. The household is keyed on {@code (lastName,
     * postcode)}: the normalized last name and the postcode are hashed with SHA-256 and the leading
     * 12 hex characters of the digest form the identifier, so any two owners sharing a last name and
     * postcode — regardless of creation order or address — resolve to the same identifier
     * automatically. An owner without a postcode belongs to no shared household, so {@code null} is
     * returned.
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode, or {@code null} when the owner has none
     * @return the deterministic household identifier, or {@code null} when {@code postcode} is {@code null}
     */
    private String householdIdFor(String lastName, String postcode) {
        if (postcode == null) {
            return null;
        }
        return shaHex(householdKey(lastName, postcode), 12);
    }

    /**
     * Builds the canonical household key that identifies the household a {@code lastName} and
     * {@code postcode} belong to: the normalized last name (case-insensitive, with collapsed
     * whitespace) joined to the postcode by {@code '|'}. This is the single definition of which
     * owner fields make up a household and how they are normalized;
     * {@link #householdIdFor(String, String)} hashes this key into the stored identifier, so any two
     * owners in the same household resolve to the same key and therefore the same identifier.
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode
     * @return the normalized household key
     */
    private String householdKey(String lastName, String postcode) {
        return collapseWhitespace(lastName).toLowerCase(java.util.Locale.ROOT) + "|" + postcode;
    }

    /**
     * Returns the leading {@code hexChars} upper-case hex characters of the SHA-256 digest of a
     * string, the form used by the derived owner codes (for example the customer code and household
     * identifier). Delegates to {@link Owner#shaHexPrefix(String, int)}, the single definition of the
     * truncated upper-case hex derivation, so {@code hexChars} characters cover the leading
     * {@code ceil(hexChars / 2)} bytes of the digest.
     *
     * @param input the string to hash
     * @param hexChars the number of leading upper-case hex characters to return
     * @return the first {@code hexChars} upper-case hex characters of the SHA-256 digest
     */
    private String shaHex(String input, int hexChars) {
        return Owner.shaHexPrefix(input, hexChars);
    }

    /**
     * Returns the owners that belong to the household identified by the given {@code householdId},
     * i.e. every owner carrying that exact identifier. Soft-deleted owners are included, so a caller
     * that must ignore deleted owners (as {@link #rejectDuplicateIdentity(OwnerFieldsDto)} does)
     * filters them out itself. The owners are taken from the current set, so after a create the
     * result includes the newly persisted owner. An owner without a household (a {@code null}
     * identifier) belongs to no shared household, so an empty list is returned. This is the single
     * definition of a household's members, shared by every rule that reasons about the owners
     * sharing a household (the household-duplicate block and the household member count).
     *
     * @param householdId the shared household identifier, or {@code null} when the owner shares no household
     * @return the owners in the household, or an empty list when {@code householdId} is {@code null}
     */
    private List<Owner> householdMembers(String householdId) {
        if (householdId == null) {
            return List.of();
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .collect(Collectors.toList());
    }

    /**
     * Counts the owners that belong to the household identified by the given {@code householdId} (see
     * {@link #householdMembers(String)}), i.e. every owner carrying that exact identifier. The count
     * is taken against the current set of owners, so after a create it reflects the household's size
     * including the newly persisted owner. An owner without a household (a {@code null} identifier)
     * belongs to no shared household and yields {@code 0}.
     *
     * @param householdId the shared household identifier, or {@code null} when the owner shares no household
     * @return the number of owners in the household, or {@code 0} when {@code householdId} is {@code null}
     */
    private int countHouseholdMembers(String householdId) {
        return householdMembers(householdId).size();
    }

    /**
     * Stamps an owner with its household member count, the number of owners sharing its household
     * (see {@link #countHouseholdMembers(String)}), so every value the owner derives from its
     * household size (its membership factor and level) reads a populated count rather than the
     * unstamped default. This is the single definition of hydrating an owner with its household
     * member count, shared by the create and read paths that render an owner's derived values.
     *
     * @param owner the owner to stamp, mutated in place with its household member count
     */
    private void applyHouseholdMemberCount(Owner owner) {
        owner.setHouseholdMemberCount(countHouseholdMembers(owner.getHouseholdId()));
    }

    /**
     * Resolves the household level ceiling a newly created owner is subject to: one above the
     * current maximum {@code membershipLevel} among the existing, non-deleted owners already
     * sharing its household (see {@link #householdMembers(String)}). Each existing member is
     * stamped with its household member count first so its {@link Owner#getMembershipLevel()}
     * reads a populated count, mirroring how the create and read paths render a member's derived
     * level. The ceiling is taken before the new owner is persisted, so it reflects only the
     * members already in the household. When the owner joins an empty household — no household
     * identifier, or no existing member — no ceiling applies and {@code null} is returned, letting
     * the owner earn its uncapped level (see {@link Owner#cappedMembershipLevel(int, Integer)}).
     *
     * @param householdId the shared household identifier of the owner being created, or {@code null}
     * @return one above the household's current maximum membership level, or {@code null} when no ceiling applies
     */
    private Integer householdMembershipLevelCap(String householdId) {
        java.util.OptionalInt max = householdMembers(householdId).stream()
            .filter(existing -> !Owner.isDeleted(existing.getDeleted()))
            .peek(this::applyHouseholdMemberCount)
            .mapToInt(Owner::getMembershipLevel)
            .max();
        return max.isPresent() ? max.getAsInt() + 1 : null;
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
