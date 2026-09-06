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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
import org.springframework.samples.petclinic.model.Region;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.IdempotencyKeyStore;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.rest.advice.DailyOwnerRegistrationLimitException;
import org.springframework.samples.petclinic.rest.advice.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.advice.DuplicateOwnerIdentityException;
import org.springframework.samples.petclinic.rest.advice.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.rest.advice.InvalidOwnerPostcodeException;
import org.springframework.samples.petclinic.rest.advice.OwnerCityAtCapacityException;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailDomains;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

    private final TelephoneNormalizer telephoneNormalizer;

    private final AddressNormalizer addressNormalizer;

    private final EmailNormalizer emailNormalizer;

    private final DisposableEmailDomains disposableEmailDomains;

    private final IdempotencyKeyStore idempotencyKeyStore;

    public OwnerRestControllerV1(ClinicService clinicService,
                                 OwnerMapper ownerMapper,
                                 PetMapper petMapper,
                                 VisitMapper visitMapper,
                                 TelephoneNormalizer telephoneNormalizer,
                                 AddressNormalizer addressNormalizer,
                                 EmailNormalizer emailNormalizer,
                                 DisposableEmailDomains disposableEmailDomains,
                                 IdempotencyKeyStore idempotencyKeyStore) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.telephoneNormalizer = telephoneNormalizer;
        this.addressNormalizer = addressNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.disposableEmailDomains = disposableEmailDomains;
        this.idempotencyKeyStore = idempotencyKeyStore;
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
        return withOwner(ownerId, owner ->
            new ResponseEntity<>(toOwnerDtoWithDerivedFields(owner), HttpStatus.OK));
    }

    /**
     * The request header carrying the caller's idempotency key. When a create is repeated with a key
     * already seen, the originally created owner is returned with 200 instead of a duplicate being
     * created (see {@link #addOwner(OwnerFieldsDto)} and {@link IdempotencyKeyStore}).
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = currentIdempotencyKey();
        Optional<Owner> existing = idempotencyKeyStore.find(idempotencyKey)
            .map(this.clinicService::findOwnerById);
        if (existing.isPresent()) {
            return ownerResponse(existing.get(), HttpStatus.OK);
        }
        Owner owner = createOwner(ownerFieldsDto);
        idempotencyKeyStore.record(idempotencyKey, owner.getId());
        return ownerResponse(owner, HttpStatus.CREATED);
    }

    /**
     * Reads the {@code Idempotency-Key} header from the current request, or {@code null} when no
     * request is bound or the header is absent. Held in one place so the create endpoint can decide,
     * from the key alone, whether a create is a first occurrence or a repeat to be served from the
     * originally created owner.
     *
     * @return the request's idempotency key, or {@code null} when none was supplied
     */
    private String currentIdempotencyKey() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getHeader(IDEMPOTENCY_KEY_HEADER);
        }
        return null;
    }

    /**
     * Validates and normalizes the submitted owner fields, builds the owner, enforces the create-time
     * duplicate rules and persists it, returning the saved owner. This holds the whole "make and store
     * a new owner" flow in one place, separate from turning that owner into an HTTP response
     * ({@link #ownerResponse(Owner, HttpStatus)}), so the create endpoint reads as "create the owner,
     * then respond" and the persisted owner is available to callers that need it beyond the response.
     *
     * @param ownerFieldsDto the submitted owner fields, normalized in place as part of the create
     * @return the persisted owner, with its derived fields populated
     */
    private Owner createOwner(OwnerFieldsDto ownerFieldsDto) {
        normalizeAddress(ownerFieldsDto);
        rejectBlankOwnerFields(ownerFieldsDto);
        rejectDisposableEmailDomain(ownerFieldsDto.getEmail());
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        rejectInvalidPostcode(ownerFieldsDto.getCity(), ownerFieldsDto.getPostcode());
        rejectCityAtCapacity(ownerFieldsDto.getCity());
        rejectFutureRegistrationDate(ownerFieldsDto.getRegistrationDate());
        LocalDate registrationDate = businessDay(effectiveRegistrationDate(ownerFieldsDto.getRegistrationDate()));
        rejectDailyRegistrationLimit(registrationDate);
        String normalizedTelephone = telephoneNormalizer.normalize(ownerFieldsDto.getTelephone());
        ownerFieldsDto.setTelephone(normalizedTelephone);
        String normalizedEmail = emailNormalizer.normalize(ownerFieldsDto.getEmail());
        ownerFieldsDto.setEmail(normalizedEmail);
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        owner.setRegistrationDate(registrationDate);
        owner.setCustomerCode(customerCode(owner));
        owner.setNamesakeCount(namesakeCount(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdId(householdId(owner));
        rejectDuplicateIdentity(owner);
        rejectHouseholdDuplicate(owner, sharesHousehold);
        owner.setPossibleDuplicateOf(sharesHousehold ? null : possibleDuplicateOf(owner));
        this.clinicService.saveOwner(owner);
        owner.setHouseholdSize(householdSize(owner));
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            membershipLevel(owner), ownerMapper.membershipNumber(owner));
        return owner;
    }

    /**
     * Builds the response for an owner: its derived-field DTO (see
     * {@link #toOwnerDtoWithDerivedFields(Owner)}) as the body and a {@code Location} header pointing at
     * the owner's canonical URL, returned with the given {@code status}. Assembling the response in one
     * place keeps "what to return for an owner" separate from the create flow that produced it, so any
     * path that returns an owner does so identically (differing only in the status it carries).
     *
     * @param owner the persisted owner to represent
     * @param status the HTTP status to return
     * @return the owner response with its body, {@code Location} header and status
     */
    private ResponseEntity<OwnerDto> ownerResponse(Owner owner, HttpStatus status) {
        OwnerDto ownerDto = toOwnerDtoWithDerivedFields(owner);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<>(ownerDto, headers, status);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        return withOwner(ownerId, currentOwner -> {
            currentOwner.setAddress(ownerFieldsDto.getAddress());
            currentOwner.setCity(ownerFieldsDto.getCity());
            currentOwner.setFirstName(ownerFieldsDto.getFirstName());
            currentOwner.setLastName(ownerFieldsDto.getLastName());
            currentOwner.setTelephone(ownerFieldsDto.getTelephone());
            currentOwner.setEmail(emailNormalizer.normalize(ownerFieldsDto.getEmail()));
            this.clinicService.saveOwner(currentOwner);
            return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<OwnerDto> deleteOwner(Integer ownerId) {
        return withOwner(ownerId, owner -> {
            owner.setDeleted(true);
            this.clinicService.saveOwner(owner);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<PetDto> addPetToOwner(Integer ownerId, PetFieldsDto petFieldsDto) {
        return withOwner(ownerId, owner -> {
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
        });
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
     * Loads the owner with the given id and, when it exists, produces the response through
     * {@code action}; when no such owner exists a {@code 404 Not Found} is returned instead. The
     * single-owner endpoints share this "find the owner or report 404" preamble, so the lookup and
     * the not-found response are written in exactly one place rather than copied into each method.
     *
     * @param ownerId the id of the owner to load
     * @param action builds the response for an existing owner
     * @param <T> the response body type
     * @return the result of {@code action} for an existing owner, otherwise a 404 response
     */
    private <T> ResponseEntity<T> withOwner(Integer ownerId, Function<Owner, ResponseEntity<T>> action) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return action.apply(owner);
    }

    /**
     * Maps a persisted owner to its {@link OwnerDto} and populates the read-only fields that are
     * derived from server-side state rather than stored columns (the {@code bulkSignupWarning} flag,
     * the {@code identityKey} and the {@code possibleDuplicate} pair), and re-applies the owner's
     * {@code membershipLevel} through the controller ({@link #membershipLevel(Owner)}). Both the
     * create endpoint and the single-owner read endpoint return their owner through this method, so
     * an owner is decorated identically on either path and each derived field is computed in exactly
     * one place.
     *
     * @param owner the persisted owner to represent
     * @return the owner DTO with its derived read-only fields populated
     */
    private OwnerDto toOwnerDtoWithDerivedFields(Owner owner) {
        owner.setHouseholdSize(householdSize(owner));
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        ownerDto.setMembershipLevel(membershipLevel(owner));
        ownerDto.setBulkSignupWarning(bulkSignupWarning(owner.getRegistrationDate()));
        ownerDto.setIdentityKey(identityKey(owner));
        Integer possibleDuplicateOf = owner.getPossibleDuplicateOf();
        ownerDto.setPossibleDuplicate(possibleDuplicateOf != null);
        ownerDto.setPossibleDuplicateOf(possibleDuplicateOf);
        return ownerDto;
    }

    /**
     * The {@code membershipLevel} reported for a single owner. Starts from the owner-local derivation
     * in {@link OwnerMapper#membershipLevel(Owner)} (the level from the owner's own membership points),
     * routed through the controller so the create audit log and the single-owner response agree on one
     * value. Unlike the mapper - which sees only the owner - the controller can reach the rest of the
     * clinic, so this is the single place a reported membership level is shaped by cross-owner context.
     *
     * <p>Household ceiling: the reported level is capped at one above the current maximum
     * {@linkplain #householdMemberLevel(Owner) level} among the owner's <em>existing</em>
     * {@link #householdMembers(Owner) household members} (those sharing its {@code householdId}, other
     * than the owner itself). With no existing household member no cap applies and the owner-local level
     * is reported unchanged. The cap uses each member's own owner-local level (not their reported,
     * capped level), so it depends only on the members' own points and cannot recurse.
     *
     * @param owner the owner whose reported membership level is wanted
     * @return the owner's membership level, capped at one above its household maximum
     */
    private Integer membershipLevel(Owner owner) {
        int ownLevel = ownerMapper.membershipLevel(owner);
        java.util.OptionalInt maxHouseholdLevel = householdMembers(owner).stream()
            .filter(member -> !java.util.Objects.equals(member.getId(), owner.getId()))
            .mapToInt(this::householdMemberLevel)
            .max();
        if (maxHouseholdLevel.isEmpty()) {
            return ownLevel;
        }
        return Math.min(ownLevel, maxHouseholdLevel.getAsInt() + 1);
    }

    /**
     * The owner-local membership level of a household member, used only as an input to the household
     * ceiling in {@link #membershipLevel(Owner)}. The member's {@link Owner#getHouseholdSize()
     * household size} is set from its household before the level is derived, so the member's household
     * factor is accounted for exactly as it is when the member is reported on its own; the level itself
     * comes from the owner-local {@link OwnerMapper#membershipLevel(Owner)}.
     *
     * @param member an existing member of an owner's household
     * @return the member's owner-local membership level
     */
    private int householdMemberLevel(Owner member) {
        member.setHouseholdSize(householdSize(member));
        return ownerMapper.membershipLevel(member);
    }

    /**
     * Canonicalizes the submitted address in place before it is validated, stored and returned, so
     * every later step - the required-field check, the persisted value and the response - sees the
     * single normalized form produced by {@link AddressNormalizer#normalize(String)} (see that method
     * for the trim/collapse/upper-case/abbreviation rules). Normalizing once here, at the head of the
     * create flow, keeps address canonicalization in exactly one place rather than spread across the
     * steps that read the address.
     *
     * <p>An address may be supplied either as the structured {@code addressLine1} (with an optional
     * {@code addressLine2}) or as the single flat {@code address}, the latter kept for backward
     * compatibility. Whichever address fields are supplied are normalized, and the structured fields
     * are preferred when present: the flat {@code address} returned is composed as the normalized
     * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when an
     * {@code addressLine2} is present; only when no {@code addressLine1} is supplied does the flat
     * {@code address} retain its own normalized value.
     *
     * @param ownerFieldsDto the submitted owner fields, whose address fields are replaced with their normalized form
     */
    private void normalizeAddress(OwnerFieldsDto ownerFieldsDto) {
        String addressLine1 = addressNormalizer.normalize(ownerFieldsDto.getAddressLine1());
        String addressLine2 = addressNormalizer.normalize(ownerFieldsDto.getAddressLine2());
        String flatAddress = addressNormalizer.normalize(ownerFieldsDto.getAddress());
        ownerFieldsDto.setAddressLine1(addressLine1);
        ownerFieldsDto.setAddressLine2(addressLine2);
        if (addressLine1 != null && !addressLine1.isBlank()) {
            String composed = addressLine2 != null && !addressLine2.isBlank()
                ? addressLine1 + " " + addressLine2
                : addressLine1;
            ownerFieldsDto.setAddress(composed);
        } else {
            ownerFieldsDto.setAddress(flatAddress);
        }
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
        addRequiredAddress(blankFields, ownerFieldsDto);
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
     * Records the address requirement's offending field name(s) into {@code blankFields}. An owner
     * must supply an address in EITHER accepted form: the structured {@code addressLine1} or the flat
     * {@code address} (kept for backward compatibility). The requirement is satisfied when at least one
     * of those is present and non-blank; only when both are missing or blank is {@code address} reported
     * as the offending field. Holding the "an owner must have an address" rule in its own method - rather
     * than a bare entry in the field list - gives it one place to live as the accepted address forms grow.
     *
     * @param blankFields the accumulating list of blank required-field names
     * @param ownerFieldsDto the submitted owner fields
     */
    private void addRequiredAddress(List<String> blankFields, OwnerFieldsDto ownerFieldsDto) {
        if (isBlank(ownerFieldsDto.getAddressLine1()) && isBlank(ownerFieldsDto.getAddress())) {
            blankFields.add("address");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Rejects an owner whose {@code email} domain is on the disposable-domain blocklist
     * ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}). Email is validated
     * only when present, so an owner created without one is accepted unchanged; syntactic validity is
     * already enforced by Bean Validation's {@code @Email} constraint on {@link OwnerFieldsDto}, so
     * this only rejects an otherwise well-formed address that uses a throwaway provider (see
     * {@link DisposableEmailDomains#isDisposable(String)} for the case-insensitive domain match). A
     * blocklisted address is reported through a {@link DisposableEmailDomainException}, which the
     * {@code ExceptionControllerAdvice} renders as a 400 response naming the {@code email} field.
     *
     * @param email the supplied email, or {@code null} when omitted
     * @throws DisposableEmailDomainException if the email's domain is blocklisted
     */
    private void rejectDisposableEmailDomain(String email) {
        if (disposableEmailDomains.isDisposable(email)) {
            throw new DisposableEmailDomainException(email);
        }
    }

    /**
     * Validates the owner's optional {@code postcode} against its {@code city} region. Postcode is
     * validated only when present, so an owner created without one is accepted unchanged. A supplied
     * postcode is already constrained to four digits by Bean Validation on {@link OwnerFieldsDto}; this
     * additionally rejects a well-formed postcode that is out of range for the city's region using the
     * shared {@link Region#acceptsPostcode(int) region-to-postcode} table ({@code NSW 2000-2099},
     * {@code VIC 3000-3099}, {@code QLD 4000-4099}). A city with no known region accepts any 4-digit
     * postcode. An out-of-range postcode is reported through an {@link InvalidOwnerPostcodeException},
     * which the {@code ExceptionControllerAdvice} renders as a 400 response.
     *
     * @param city the city of the owner being created
     * @param postcode the supplied postcode, or {@code null} when omitted
     * @throws InvalidOwnerPostcodeException if the postcode is out of range for the city's region
     */
    private void rejectInvalidPostcode(String city, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        Region.forCity(city).ifPresent(region -> {
            if (!region.acceptsPostcode(Integer.parseInt(postcode))) {
                throw new InvalidOwnerPostcodeException(postcode, city);
            }
        });
    }

    /**
     * Number of leading hex characters of the identity hash retained as the {@code HASH8} segment of
     * the {@code customerCode}.
     */
    private static final int CUSTOMER_CODE_HASH_LENGTH = 8;

    /**
     * Builds the {@code customerCode} assigned to a newly created owner, formatted
     * {@code '<REGION>-<HASH8>'}: {@code REGION} is the owner's canonical region code resolved from
     * its {@code postcode} (falling back to its {@code city}) through the shared
     * {@link Region#code(String, String)}, and {@code HASH8} is the first 8 upper-case hex characters
     * of the SHA-256 digest of the owner's normalized telephone followed by its last name
     * (e.g. {@code 'NSW-1A2B3C4D'}). When this base code collides with an existing owner's
     * {@code customerCode}, {@code '-<n>'} is appended with the smallest {@code n} of 2 or more that
     * makes the result unique (e.g. {@code 'NSW-1A2B3C4D-2'}), so distinct owners always receive
     * distinct customer codes.
     *
     * @param owner the owner being created, with its telephone already normalized
     * @return the formatted, de-duplicated customer code
     */
    private String customerCode(Owner owner) {
        String region = Region.code(owner.getPostcode(), owner.getCity());
        String hash8 = hashPrefix(owner.getTelephone() + owner.getLastName(), CUSTOMER_CODE_HASH_LENGTH);
        String base = region + "-" + hash8;
        return deduplicateCustomerCode(base);
    }

    /**
     * Ensures the given base {@code customerCode} does not collide with an existing owner's
     * {@code customerCode}. When no existing owner carries the base code it is returned unchanged;
     * otherwise {@code '-<n>'} is appended with the smallest {@code n} of 2 or more that yields a code
     * held by no existing owner.
     *
     * @param base the computed base customer code, formatted {@code '<REGION>-<HASH8>'}
     * @return the base code, or a {@code '<base>-<n>'} variant that is unique among existing owners
     */
    private String deduplicateCustomerCode(String base) {
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
        return (int) countOwners(owner -> equalsIgnoreCase(owner.getFirstName(), firstName)
            && equalsIgnoreCase(owner.getLastName(), lastName));
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
        if (ownersInCity(city) >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }

    /**
     * Counts the existing owners whose {@code city} matches the given city, compared
     * case-insensitively (see {@link #equalsIgnoreCase(String, String)}). Used by the per-city
     * capacity check ({@link #rejectCityAtCapacity(String)}) to count a city's owners.
     *
     * @param city the city to count owners for
     * @return the number of existing owners in that city
     */
    private long ownersInCity(String city) {
        return countOwners(owner -> equalsIgnoreCase(owner.getCity(), city));
    }

    /**
     * The maximum number of owners that may be registered on a single day. Once this many owners
     * already carry a given {@code registrationDate}, further owners for that date are rejected by
     * {@link #rejectDailyRegistrationLimit(LocalDate)}.
     */
    private static final long MAX_OWNERS_PER_DAY = 100L;

    /**
     * Resolves the effective registration date for a create request: the value supplied in the
     * request when present, otherwise the server's current date. The result is the raw effective
     * date before the business-day adjustment applied by {@link #businessDay(LocalDate)}.
     *
     * @param suppliedDate the registration date from the request, or {@code null} when omitted
     * @return the supplied date, or today when none was supplied
     */
    private LocalDate effectiveRegistrationDate(LocalDate suppliedDate) {
        return suppliedDate != null ? suppliedDate : LocalDate.now();
    }

    /**
     * Rejects a create request whose supplied {@code registrationDate} lies after the server's
     * current date. A registration date may be supplied to backdate a create - and is defaulted to
     * today when omitted - but it may never be in the future. The check is applied to the raw
     * supplied value before the business-day adjustment ({@link #businessDay(LocalDate)}), so a
     * future date is rejected regardless of how that adjustment would move it. A future date is
     * reported through a {@link FutureRegistrationDateException}, which the
     * {@code ExceptionControllerAdvice} renders as a 400 Bad Request response naming the
     * {@code registrationDate} field.
     *
     * @param suppliedDate the registration date from the request, or {@code null} when omitted
     * @throws FutureRegistrationDateException if the supplied date is later than the server date
     */
    private void rejectFutureRegistrationDate(LocalDate suppliedDate) {
        if (suppliedDate != null && suppliedDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(suppliedDate);
        }
    }

    /**
     * The fixed list of public holidays that are not business days. A registration date that lands
     * on one of these is rolled forward by {@link #businessDay(LocalDate)} to the next non-holiday
     * business day, exactly as it is rolled forward off a weekend.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Adjusts a date so it falls on a business day, rolling forward off weekends and public
     * holidays. A Saturday, Sunday or {@link #PUBLIC_HOLIDAYS listed public holiday} is rolled
     * forward one day at a time until the first non-holiday weekday is reached; any other weekday is
     * returned unchanged. Applied to the effective registration date so that a weekend or holiday
     * value - whether supplied in the request or defaulted to the server date - is stored as the
     * next business day, and every value derived from the registration date (such as the membership
     * number's year segment and the daily create-limit bucket) uses the adjusted business day.
     *
     * @param date the effective registration date
     * @return the same date when it is a non-holiday weekday, otherwise the next business day
     */
    private LocalDate businessDay(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Whether the given date is a weekend day or a listed public holiday, and so not a business day.
     *
     * @param date the date to test
     * @return {@code true} for a Saturday, Sunday or {@link #PUBLIC_HOLIDAYS listed public holiday}
     */
    private boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY
            || dayOfWeek == DayOfWeek.SUNDAY
            || PUBLIC_HOLIDAYS.contains(date);
    }

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
        if (ownersRegisteredOn(date) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerRegistrationLimitException(date);
        }
    }

    /**
     * Counts the existing owners whose {@code registrationDate} equals the given day. Shared by the
     * hard daily create limit ({@link #rejectDailyRegistrationLimit(LocalDate)}) and the soft
     * {@code bulkSignupWarning} flag ({@link #bulkSignupWarning(LocalDate)}) so both accumulate a
     * day's owners identically.
     *
     * @param date the registration date to count owners for
     * @return the number of existing owners registered on that day
     */
    private long ownersRegisteredOn(LocalDate date) {
        return countOwners(owner -> date.equals(owner.getRegistrationDate()));
    }

    /**
     * The soft daily create threshold above which the read-only {@code bulkSignupWarning} flag is
     * raised. Once more than this many owners share a {@code registrationDate}, that day is flagged
     * as unusually high volume - a warning well below the hard {@link #MAX_OWNERS_PER_DAY} cap.
     */
    private static final long BULK_SIGNUP_WARNING_THRESHOLD = 80L;

    /**
     * Derives the read-only {@code bulkSignupWarning} flag for an owner: {@code true} when more than
     * {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners have been created (counted by
     * {@code registrationDate}) on the given day, otherwise {@code false}. Uses the same per-day
     * accumulation as {@link #rejectDailyRegistrationLimit(LocalDate)}. A {@code null} date (an owner
     * without a registration date, such as seed data) is never flagged.
     *
     * @param date the owner's registration date (the day it was created), or {@code null}
     * @return {@code true} if the day already holds more than the warning threshold of owners
     */
    private boolean bulkSignupWarning(LocalDate date) {
        if (date == null) {
            return false;
        }
        return ownersRegisteredOn(date) > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Rejects a create request whose derived {@code identityKey} exactly equals an existing owner's.
     * The identity key consolidates the former separate telephone, email and household duplicate
     * checks into a single value: {@code normalizedTelephone + '|' + (email or empty) + '|' +
     * (householdId or empty)} (see {@link #identityKey(Owner)}). Because the telephone is part of the
     * key, two members of the same household (same {@code householdId}) with different telephones have
     * different identity keys and are both allowed; only an exact full-key match is a duplicate. A
     * collision is reported through a {@link DuplicateOwnerIdentityException}, which the
     * {@code ExceptionControllerAdvice} renders as a 409 Conflict response.
     *
     * @param owner the fully-populated owner being created (telephone, email and household id set)
     * @throws DuplicateOwnerIdentityException if an existing owner has the same identity key
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String key = identityKey(owner);
        boolean duplicate = existingOwnerMatches(this::identityKey, key);
        if (duplicate) {
            throw new DuplicateOwnerIdentityException(key);
        }
    }

    /**
     * Rejects a create request that would re-register an existing household member unless it declares
     * the shared household with {@code sharesHousehold}. The household is keyed on
     * {@code (lastName, postcode)} through the deterministic {@link #householdId(Owner)}, so an existing
     * owner with the same last name and postcode is a member of the same household. A new owner that
     * matches an existing member of that household on its <em>canonical telephone</em> (see
     * {@link TelephoneNormalizer#canonicalize(String)}) is a household duplicate and is rejected with a
     * 409 (via {@link DuplicateOwnerIdentityException}). A member carrying a <em>different</em> telephone
     * is a distinct person in the same household: it is allowed and surfaced instead as a soft
     * {@code possibleDuplicate} (see {@link #possibleDuplicateOf(Owner)}), which is what lets a household
     * hold more than one owner. Setting {@code sharesHousehold} bypasses this block, so the owner is
     * created as a declared household member. Owners without a {@code householdId} (e.g. no postcode)
     * have no household to collide with and are always allowed.
     *
     * @param owner the fully-populated owner being created (household id set, telephone normalized)
     * @param sharesHousehold whether the request declared it shares an existing household
     * @throws DuplicateOwnerIdentityException if a household member with the same telephone exists and it was not declared
     */
    private void rejectHouseholdDuplicate(Owner owner, boolean sharesHousehold) {
        if (sharesHousehold) {
            return;
        }
        String household = owner.getHouseholdId();
        if (household == null) {
            return;
        }
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        boolean sameTelephoneMember = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> household.equals(existing.getHouseholdId()))
            .anyMatch(existing -> java.util.Objects.equals(telephone,
                telephoneNormalizer.canonicalize(existing.getTelephone())));
        if (sameTelephoneMember) {
            throw new DuplicateOwnerIdentityException(identityKey(owner));
        }
    }

    /**
     * Computes the soft-match {@code possibleDuplicateOf} for a newly created owner: the id of the
     * first existing owner that shares this owner's {@code lastName} (compared case-insensitively) and
     * {@code postcode} while carrying a <em>different</em> telephone (compared by canonical E.164 form,
     * see {@link TelephoneNormalizer#canonicalize(String)}), or {@code null} when no such owner exists.
     * The check runs only after the exact-identity duplicate check ({@link #rejectDuplicateIdentity(Owner)})
     * has passed, so a shared last-name-and-postcode with the same telephone has already been rejected as
     * a hard duplicate and can never surface here. A blank {@code postcode} never matches, since two
     * owners without a postcode do not share one. The captured id is stored on the new owner and surfaced
     * as the read-only {@code possibleDuplicate} / {@code possibleDuplicateOf} fields.
     *
     * @param owner the fully-populated owner being created (telephone normalized)
     * @return the id of the first soft-matching existing owner, or {@code null} when none matches
     */
    private Integer possibleDuplicateOf(Owner owner) {
        if (owner.getPostcode() == null || owner.getPostcode().isBlank()) {
            return null;
        }
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getLastName(), owner.getLastName())
                && owner.getPostcode().equals(existing.getPostcode())
                && !java.util.Objects.equals(telephone, telephoneNormalizer.canonicalize(existing.getTelephone())))
            .map(Owner::getId)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Reduces an owner to its derived {@code identityKey}, formatted
     * {@code '<normalizedTelephone>|<email>|<householdId>'}: the owner's telephone reduced to its
     * canonical E.164 form (see {@link TelephoneNormalizer#canonicalize(String)}), the owner's
     * normalized (lower-cased) email or the empty string when absent (see
     * {@link EmailNormalizer#normalize(String)}), and the owner's {@code householdId} or the empty
     * string when absent, joined by {@code '|'}. Two owners are duplicates exactly when their whole
     * identity keys are equal. The key never contains {@code null} segments, so the create endpoint's
     * single duplicate check compares this one value across owners.
     *
     * @param owner the owner to derive the identity key for
     * @return the owner's identity key
     */
    private String identityKey(Owner owner) {
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        String normalizedEmail = emailNormalizer.normalize(owner.getEmail());
        String email = normalizedEmail == null ? "" : normalizedEmail;
        String household = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + household;
    }

    /**
     * Finds the first existing owner that already carries the given comparison {@code key}, derived
     * from each owner by {@code keyExtractor}. Owners for which the extractor yields {@code null} are
     * ignored (a {@code null} key never equals the sought key). The create endpoint's duplicate
     * checks reduce both an existing owner and the owner being created to the same canonical key;
     * this returns the matched existing owner itself, so a caller can either reject the collision or
     * record which owner was matched. Owners flagged deleted (soft-deleted) are excluded, so a
     * collision with only a deleted owner is not reported and the create is allowed.
     *
     * @param keyExtractor derives an owner's canonical comparison key (may return {@code null})
     * @param key the canonical key of the owner being created
     * @return the first existing owner sharing the key, or empty when none does
     */
    private Optional<Owner> findExistingOwner(Function<Owner, String> keyExtractor, String key) {
        return this.clinicService.findAllOwners().stream()
            .filter(owner -> !owner.isDeleted())
            .filter(owner -> key.equals(keyExtractor.apply(owner)))
            .findFirst();
    }

    /**
     * Reports whether any existing owner already carries the given comparison {@code key}, derived
     * from each owner by {@code keyExtractor} (see {@link #findExistingOwner(Function, String)}).
     * The create endpoint's duplicate checks use this to reduce both an existing owner and the owner
     * being created to the same canonical key and reject a match.
     *
     * @param keyExtractor derives an owner's canonical comparison key (may return {@code null})
     * @param key the canonical key of the owner being created
     * @return {@code true} if some existing owner shares the key
     */
    private boolean existingOwnerMatches(Function<Owner, String> keyExtractor, String key) {
        return findExistingOwner(keyExtractor, key).isPresent();
    }

    /**
     * Counts the existing owners that satisfy the given {@code predicate}. The create endpoint's
     * count-based rules - the per-city capacity, the per-day create limit and the derived
     * {@code namesakeCount} - all accumulate owners over {@link ClinicService#findAllOwners()} and
     * differ only in the predicate they count by, so the scan-and-count is written in exactly one
     * place and each rule supplies just its own predicate.
     *
     * @param predicate the condition an owner must satisfy to be counted
     * @return the number of existing owners that satisfy the predicate
     */
    private long countOwners(Predicate<Owner> predicate) {
        return this.clinicService.findAllOwners().stream()
            .filter(predicate)
            .count();
    }

    /**
     * Delimiter joining the canonical {@code lastName} and the {@code postcode} when deriving the
     * shared {@code householdId}.
     */
    private static final String HOUSEHOLD_KEY_DELIMITER = "|";

    /**
     * Reduces the household {@code lastName} to a canonical form for case-insensitive,
     * whitespace-insensitive comparison: leading and trailing whitespace is trimmed, every run of
     * internal whitespace is collapsed to a single space, and the result is lower-cased using
     * {@link java.util.Locale#ROOT} so comparison is locale-independent.
     *
     * @param value the raw field value, or {@code null}
     * @return the canonical value, or {@code null} when {@code value} is {@code null}
     */
    private String canonicalizeHousehold(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Derives the deterministic, shared {@code householdId} for an owner from its {@code lastName}
     * and {@code postcode}. The identifier is the first 12 upper-case hex characters of the SHA-256
     * hash of {@code normalizedLastName + '|' + postcode} (see {@link #canonicalizeHousehold(String)}
     * for the last-name normalization), so every owner with the same last name and postcode -
     * regardless of casing or spacing - is automatically assigned the identical value. Owners without
     * a postcode have no household basis and are given no household id ({@code null}).
     *
     * @param owner the owner being created
     * @return the shared household identifier, or {@code null} when the owner has no postcode
     */
    /**
     * Counts the owners in the given owner's household: the existing owners that share its
     * {@code householdId} (see {@link #householdId(Owner)}), which - because the owner being counted is
     * itself persisted before it is decorated - includes the owner. An owner with no {@code householdId}
     * (e.g. no postcode) has no shared household and counts as a household of one. The value feeds the
     * household factor of the owner's membership points.
     *
     * @param owner the owner whose household size is being derived
     * @return the number of owners in the owner's household (at least {@code 1})
     */
    private int householdSize(Owner owner) {
        if (owner.getHouseholdId() == null) {
            return 1;
        }
        return householdMembers(owner).size();
    }

    /**
     * The existing owners that make up the given owner's household: those sharing its
     * {@code householdId} (see {@link #householdId(Owner)}). An owner with no {@code householdId}
     * (e.g. no postcode) belongs to no shared household, so the result is empty. Whether the owner
     * itself appears depends on when the scan runs relative to the create's save: a scan taken after
     * the owner is persisted (as the {@link #householdSize(Owner) household size} derivation is)
     * counts it among its household, while one taken before the save sees only the owners already
     * present. Holding the household lookup in one place lets every household-derived value read the
     * same set of members.
     *
     * @param owner the owner whose household members are wanted
     * @return the owners sharing the owner's household, or an empty list when it has no household
     */
    private List<Owner> householdMembers(Owner owner) {
        String household = owner.getHouseholdId();
        if (household == null) {
            return List.of();
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> household.equals(existing.getHouseholdId()))
            .toList();
    }

    private String householdId(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String key = canonicalizeHousehold(owner.getLastName()) + HOUSEHOLD_KEY_DELIMITER + postcode;
        return hashPrefix(key, HOUSEHOLD_ID_LENGTH);
    }

    /**
     * Number of leading hex characters of the household hash retained as the shared
     * {@code householdId}.
     */
    private static final int HOUSEHOLD_ID_LENGTH = 12;

    /**
     * Computes the leading {@code length} upper-case hex characters of the SHA-256 digest of the
     * UTF-8 bytes of {@code input}. This is the shared hashing primitive behind the derived
     * identifiers that fingerprint owner fields - such as the household-scoped {@code householdId} -
     * so every such identifier is produced by the same algorithm and differs only in the value it
     * hashes and the number of hex characters it keeps.
     *
     * @param input the value to hash
     * @param length the number of leading hex characters to keep
     * @return the leading {@code length} upper-case hex characters of the SHA-256 digest
     */
    private String hashPrefix(String input, int length) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, length);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
