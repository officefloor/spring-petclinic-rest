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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerLocality;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.controller.CityOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.controller.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.controller.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.controller.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.controller.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.controller.InvalidEmailException;
import org.springframework.samples.petclinic.rest.controller.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.controller.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.controller.RequiredFieldsMissingException;
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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV1 implements OwnersApi {

    /**
     * Pragmatic syntactic check for an email address: a non-empty local part, a single {@code @}, and a
     * domain with at least one dot and a two-or-more-letter final label. Whitespace is not permitted.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@.]{2,}$");

    /**
     * Domains that supply disposable / throwaway mailboxes. An owner whose email domain matches one of
     * these (compared case-insensitively) is rejected. Values are stored lower-cased for comparison.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Dedicated audit logger. Successful side-effecting operations (such as creating an owner) emit a
     * line here so audit trails can be captured independently of application logging.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Dedicated notification logger. A newly created owner enqueues a welcome notification by emitting a
     * line here carrying the owner id and the assigned member id, so notification side-effects can be
     * captured independently of application and audit logging.
     */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Monotonically increasing sequence number stamped onto each structured {@code OWNER_CREATED} event.
     * Shared across all creates so the events can be totally ordered independently of timestamps.
     */
    private static final AtomicLong OWNER_CREATED_SEQUENCE = new AtomicLong();

    /**
     * Serializes structured audit events to compact JSON. Configured once and reused; it is thread-safe.
     */
    private static final ObjectMapper AUDIT_EVENT_MAPPER = JsonMapper.builder().build();

    /**
     * The request header carrying a client-supplied idempotency key for the create endpoint. When a create
     * repeats with a key already seen, the originally created owner is returned instead of a duplicate.
     */
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Remembers the id of the owner originally created for each seen {@code Idempotency-Key}. A repeated
     * create carrying an already-seen key is answered from this map with the original owner rather than
     * creating a duplicate.
     */
    private static final Map<String, Integer> IDEMPOTENT_CREATES = new ConcurrentHashMap<>();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final HttpServletRequest request;

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
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public ResponseEntity<OwnerDto> addOwner(OwnerFieldsDto ownerFieldsDto) {
        String idempotencyKey = idempotencyKey();
        if (idempotencyKey != null) {
            Integer existingId = IDEMPOTENT_CREATES.get(idempotencyKey);
            if (existingId != null) {
                Owner existing = this.clinicService.findOwnerById(existingId);
                if (existing != null) {
                    return new ResponseEntity<>(ownerMapper.toOwnerDto(existing), HttpStatus.OK);
                }
            }
        }
        rejectMissingOrBlankFields(ownerFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        applyAddress(owner);
        owner.setTelephone(normalizeTelephone(owner.getTelephone()));
        owner.setEmail(normalizeEmail(owner.getEmail()));
        validatePostcode(owner.getPostcode(), owner.getCity());
        rejectFutureRegistrationDate(owner.getRegistrationDate());
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setRegistrationDate(rollToBusinessDay(owner.getRegistrationDate()));
        rejectDailyLimitReached(owner.getRegistrationDate());
        rejectCityAtCapacity(owner.getCity());
        owner.setHouseholdId(computeHouseholdId(owner));
        boolean sharesHousehold = Boolean.TRUE.equals(ownerFieldsDto.getSharesHousehold());
        rejectDuplicateIdentity(owner);
        assignPossibleDuplicate(owner, sharesHousehold);
        owner.setMemberId(generateMemberId(owner));
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName()));
        owner.setHouseholdSize(countHouseholdSize(owner.getHouseholdId()));
        owner.setMembershipLevel(computeMembershipLevel(owner));
        owner.setBulkSignupWarning(isBulkSignupDay(owner.getRegistrationDate()));
        owner.setCapacityWarning(isCityApproachingCapacity(owner.getCity()));
        this.clinicService.saveOwner(owner);
        if (idempotencyKey != null) {
            IDEMPOTENT_CREATES.put(idempotencyKey, owner.getId());
        }
        OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), ownerDto.getMembershipLevel());
        emitOwnerCreatedEvent(owner, ownerDto);
        enqueueWelcomeNotification(owner);
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
     * Emits an immutable structured {@code OWNER_CREATED} event to the {@code AUDIT} logger, alongside the
     * human-readable audit line. The event is a JSON object
     * {@code {seq, ownerId, memberId, membershipLevel, event}} where {@code seq} is a process-wide,
     * monotonically increasing sequence number across all creates.
     *
     * <p>The {@code memberId} field carries the owner's <em>current primary identifier</em> — see
     * {@link #primaryIdentifier(Owner)} — which is now the unified member id.
     *
     * @param owner    the freshly persisted owner
     * @param ownerDto its DTO projection, used for the derived membership level
     */
    private void emitOwnerCreatedEvent(Owner owner, OwnerDto ownerDto) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("seq", OWNER_CREATED_SEQUENCE.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", primaryIdentifier(owner));
        event.put("membershipLevel", ownerDto.getMembershipLevel());
        event.put("event", "OWNER_CREATED");
        AUDIT.info(AUDIT_EVENT_MAPPER.writeValueAsString(event));
    }

    /**
     * Enqueues a welcome notification for a freshly created owner by emitting a line to the {@code NOTIFY}
     * logger carrying the owner id and the assigned member id. Downstream notification delivery consumes
     * this signal; capturing the logger lets the side-effect be asserted without an implementation hook.
     *
     * @param owner the freshly persisted owner
     */
    private void enqueueWelcomeNotification(Owner owner) {
        NOTIFY.info("welcome notification enqueued ownerId={} memberId={}",
            owner.getId(), owner.getMemberId());
    }

    /**
     * Returns the owner's current primary identifier, which structured audit events must carry. This is the
     * single point that decides which field is primary: the unified {@code memberId}.
     *
     * @param owner the owner whose primary identifier is required
     * @return the current primary identifier
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Rejects an owner payload that is missing or blank in any mandatory field. This complements Bean
     * Validation, which cannot reject whitespace-only values that still satisfy a minimum-length constraint.
     *
     * @param fields the submitted owner fields
     * @throws RequiredFieldsMissingException if any of firstName, lastName, city or telephone is
     *                                        {@code null} or blank, or if the owner supplies no address
     *                                        in either form (a blank structured {@code addressLine1} and a
     *                                        blank flat {@code address}, each measured after normalization)
     */
    /**
     * Reads the client-supplied {@code Idempotency-Key} header for the current create request, if any.
     *
     * @return the trimmed idempotency key, or {@code null} when the header is absent or blank
     */
    private String idempotencyKey() {
        String key = this.request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (key == null || key.isBlank()) {
            return null;
        }
        return key.strip();
    }

    private void rejectMissingOrBlankFields(OwnerFieldsDto fields) {
        List<String> missing = new ArrayList<>();
        if (isBlank(fields.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(fields.getLastName())) {
            missing.add("lastName");
        }
        boolean hasStructuredAddress = !isBlank(normalizeAddress(fields.getAddressLine1()));
        boolean hasFlatAddress = !isBlank(normalizeAddress(fields.getAddress()));
        if (!hasStructuredAddress && !hasFlatAddress) {
            missing.add("address");
        }
        if (isBlank(fields.getCity())) {
            missing.add("city");
        }
        if (isBlank(fields.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new RequiredFieldsMissingException(missing);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Inclusive 4-digit postcode ranges permitted per city region, keyed by the region derived from the
     * city via {@link OwnerLocality}: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A region absent from
     * this table (i.e. {@code "UNKNOWN"}) imposes no range constraint, so any 4-digit postcode is accepted.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Validates an owner's {@code postcode} when present. Postcode is optional: a {@code null} value is
     * accepted and left absent, keeping the create contract backward-compatible. When supplied it must be
     * a 4-digit value, and it must fall within the inclusive range for the city's region (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
     *
     * @param postcode the submitted postcode, or {@code null} when omitted
     * @param city the owner's city, used to derive the region whose range constrains the postcode
     * @throws InvalidPostcodeException if a supplied postcode is not 4 digits or is out of range for the
     *                                  city's region
     */
    private void validatePostcode(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_POSTCODE_RANGE.get(OwnerLocality.of(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }

    /**
     * Normalizes a submitted telephone number into E.164 form, which becomes the stored and returned
     * value. Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are
     * kept as supplied; otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
     * is dropped from the national digits. The result must be a {@code '+'} followed by 8 to 15 digits.
     * <p>
     * Where a per-country rule is defined the national-number length is validated against the country
     * code: {@code '+61'} (Australia) requires exactly 9 national digits and {@code '+1'} (NANP)
     * requires exactly 10.
     *
     * @param telephone the raw telephone value from the request
     * @return the E.164 telephone (a {@code '+'} followed by 8 to 15 digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    private String normalizeTelephone(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            // No country code supplied: assume Australia (+61) and drop a single leading '0'.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            throw new InvalidTelephoneException(cleaned);
        }
        // Validate the national-number length against the country code where a rule is defined.
        if (digits.startsWith("61") && digits.length() - 2 != 9) {
            throw new InvalidTelephoneException(cleaned);
        }
        if (digits.startsWith("1") && digits.length() - 1 != 10) {
            throw new InvalidTelephoneException(cleaned);
        }
        return "+" + digits;
    }

    /**
     * Normalizes a submitted email address. A {@code null} email is left absent (the field is optional).
     * When present, it must be a syntactically valid address; the stored and returned value is trimmed and
     * lower-cased.
     *
     * @param email the raw email value from the request, or {@code null} if omitted
     * @return the lower-cased email, or {@code null} if none was supplied
     * @throws InvalidEmailException if a non-null email is not a syntactically valid address
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
        return normalized;
    }

    /**
     * Applies the owner's address in whichever form the request supplied, preferring the structured
     * fields. When a non-blank {@code addressLine1} is present, {@code addressLine1} and (when present)
     * {@code addressLine2} are normalized in place and the flat {@code address} is set to the composed
     * value: the normalized {@code addressLine1}, with a single space and the normalized
     * {@code addressLine2} appended when an {@code addressLine2} is present. Otherwise the structured
     * lines are cleared and the flat {@code address} is normalized on its own, keeping the earlier
     * flat-address contract backward-compatible.
     *
     * @param owner the owner being created, mapped straight from the request
     */
    private void applyAddress(Owner owner) {
        if (!isBlank(owner.getAddressLine1())) {
            String line1 = normalizeAddress(owner.getAddressLine1());
            owner.setAddressLine1(line1);
            String composed = line1;
            if (!isBlank(owner.getAddressLine2())) {
                String line2 = normalizeAddress(owner.getAddressLine2());
                owner.setAddressLine2(line2);
                composed = line1 + " " + line2;
            } else {
                owner.setAddressLine2(null);
            }
            owner.setAddress(composed);
        } else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(normalizeAddress(owner.getAddress()));
        }
    }

    /**
     * Normalizes a submitted address into the form that is stored and returned. Leading and trailing
     * whitespace is removed, internal runs of whitespace are collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded ({@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}). Abbreviations are only expanded when they form a whole
     * word, so an address such as {@code '  12  main  st '} normalizes to {@code '12 MAIN STREET'}.
     *
     * @param address the raw address value from the request (may be {@code null})
     * @return the normalized address, or an empty string when {@code address} is {@code null} or blank
     */
    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expandAddressAbbreviation(tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Expands a single upper-cased address token from its common abbreviation to its full form, or
     * returns the token unchanged when it is not a recognised abbreviation.
     *
     * @param token an upper-cased whole-word address token
     * @return the expanded form ({@code STREET}, {@code ROAD}, {@code AVENUE}) or the original token
     */
    private String expandAddressAbbreviation(String token) {
        return switch (token) {
            case "ST" -> "STREET";
            case "RD" -> "ROAD";
            case "AVE" -> "AVENUE";
            default -> token;
        };
    }

    /**
     * Builds the unified member id assigned to a newly created owner, formatted
     * {@code '<REGION><FY><HASH8><CHK>'} where {@code REGION} is the region derived from the owner's
     * postcode (falling back to the city when the postcode resolves to no known region), the same
     * derivation that yields the owner's locality; {@code FY} is the two-digit fiscal year of the owner's
     * (business-day-adjusted) {@code registrationDate}; {@code HASH8} is the first 8 upper-cased hex
     * characters of the SHA-256 digest of the owner's normalized telephone concatenated with the owner's
     * last name; and {@code CHK} is a single Luhn check digit computed over the digits of
     * {@code '<REGION><FY><HASH8>'} (e.g. {@code 'NSW261A2B3C4D7'}).
     * <p>
     * Should the computed id collide with an existing owner's {@code memberId}, it is de-duplicated by
     * appending {@code '-<n>'} with the smallest {@code n} of 2 or more that yields a value not already
     * held by any existing owner; the de-duplicated id is returned.
     *
     * @param owner the owner being created, with normalized telephone, postcode and registration date set
     * @return the assigned, collision-free member id
     */
    private String generateMemberId(Owner owner) {
        String region = OwnerLocality.of(owner.getCity(), owner.getPostcode());
        String fiscalYear = fiscalYearSuffix(owner.getRegistrationDate());
        String hash8 = sha256Hex(owner.getTelephone() + owner.getLastName())
            .substring(0, 8).toUpperCase(Locale.ROOT);
        String core = region + fiscalYear + hash8;
        String base = core + luhnCheckDigit(core);
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(id -> id != null)
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
     * Returns the two-digit fiscal-year suffix ({@code '<YY>'}) for the given date: the last two digits of
     * the calendar year in which that date's fiscal year (starting 1 July) begins. This is the same value
     * carried by the DTO's {@code fiscalYear} and embedded as the {@code FY} segment of the member id.
     *
     * @param date the (business-day-adjusted) registration date
     * @return the zero-padded two-digit fiscal-year suffix
     */
    private String fiscalYearSuffix(LocalDate date) {
        int year = date.getYear();
        int start = date.getMonthValue() >= 7 ? year : year - 1;
        return String.format("%02d", start % 100);
    }

    /**
     * Computes a single Luhn check digit (0-9) over the digits contained in {@code value}. Non-digit
     * characters are skipped; the rightmost digit is doubled and every second digit thereafter, digits
     * exceeding 9 after doubling have 9 subtracted, and the check digit is {@code (10 - sum % 10) % 10}.
     *
     * @param value the value whose contained digits are summed
     * @return the Luhn check digit (between 0 and 9 inclusive)
     */
    private int luhnCheckDigit(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Computes the full lower-case hex SHA-256 digest of the UTF-8 bytes of the given value.
     *
     * @param value the value to hash
     * @return the 64-character lower-case hex SHA-256 digest
     */
    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    /**
     * Counts how many existing owners already share the given first name and last name, compared
     * case-insensitively. The count is taken before the new owner is persisted, so it excludes the
     * owner being created.
     *
     * @param firstName the first name of the owner being created
     * @param lastName the last name of the owner being created
     * @return the number of existing owners with the same first and last name (case-insensitively)
     */
    private int countNamesakes(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }

    /**
     * Computes the size of the owner's household: the number of existing owners already sharing the given
     * {@code householdId} plus the owner being created itself. The existing count is taken before the new
     * owner is persisted, so adding one for the owner yields the household's total membership including it
     * (e.g. two existing members give a household size of 3 for the third member). Households are keyed on
     * the deterministic {@code householdId} (derived from last name and postcode).
     *
     * @param householdId the computed household identifier of the owner being created
     * @return the household size including the owner being created (at least 1)
     */
    private int countHouseholdSize(String householdId) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> householdId.equals(owner.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }

    /**
     * Computes the membership level assigned to a newly created owner, applying the household level ceiling.
     * The owner's own (uncapped) level cannot exceed one above the current maximum membership level among
     * their existing household members, where every level is measured against the household's current size
     * (the owner's just-computed {@code householdSize}). When the owner has no existing household member the
     * ceiling does not apply and the uncapped level is returned. The count is taken before the new owner is
     * persisted, so it excludes the owner being created; the existing members share the deterministic
     * {@code householdId} and any soft-deleted owner is ignored.
     *
     * @param owner the owner being created, with its {@code householdId} and {@code householdSize} already set
     * @return the owner's membership level, capped at one above the existing household maximum
     */
    private Integer computeMembershipLevel(Owner owner) {
        Integer householdSize = owner.getHouseholdSize();
        Integer ownLevel = this.ownerMapper.membershipLevelForHouseholdSize(owner, householdSize);
        String householdId = owner.getHouseholdId();
        int maxExisting = this.clinicService.findAllOwners().stream()
            .filter(existing -> !isDeleted(existing))
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .mapToInt(existing -> this.ownerMapper.membershipLevelForHouseholdSize(existing, householdSize))
            .max()
            .orElse(-1);
        if (maxExisting < 0) {
            return ownLevel;
        }
        return Math.min(ownLevel, maxExisting + 1);
    }

    /**
     * The maximum number of owners permitted in a single city. A create request whose city already
     * holds this many owners is rejected.
     */
    private static final int CITY_OWNER_LIMIT = 50;

    /**
     * Rejects a create request whose city already contains {@link #CITY_OWNER_LIMIT} or more owners.
     * Cities are matched case-insensitively, mirroring the per-city customer-code sequence, so owners
     * differing only in the casing of their city share one capacity pool. The count is taken before the
     * new owner is persisted, so it excludes the owner being created.
     *
     * @param city the city of the owner being created
     * @throws CityOwnerLimitExceededException if the city already holds the maximum number of owners
     */
    private void rejectCityAtCapacity(String city) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> city.equalsIgnoreCase(owner.getCity()))
            .count();
        if (existing >= CITY_OWNER_LIMIT) {
            throw new CityOwnerLimitExceededException(city, CITY_OWNER_LIMIT);
        }
    }

    /**
     * The number of owners a city must already hold before a create request is flagged as approaching the
     * per-city capacity limit. Once the count reaches this threshold (but is still below
     * {@link #CITY_OWNER_LIMIT}) the created owner carries {@code capacityWarning = true}.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Reports whether the given city is approaching its per-city capacity limit, i.e. already holds between
     * {@link #CITY_CAPACITY_WARNING_THRESHOLD} and {@link #CITY_OWNER_LIMIT} (exclusive) owners. Cities are
     * matched case-insensitively, mirroring {@link #rejectCityAtCapacity(String)}. The count is taken before
     * the new owner is persisted, so it excludes the owner being created.
     *
     * @param city the city of the owner being created
     * @return {@code true} when the city already holds 40 to 49 owners inclusive, otherwise {@code false}
     */
    private boolean isCityApproachingCapacity(String city) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> city.equalsIgnoreCase(owner.getCity()))
            .count();
        return existing >= CITY_CAPACITY_WARNING_THRESHOLD && existing < CITY_OWNER_LIMIT;
    }

    /**
     * The maximum number of owners permitted to be created in a single day. A create request whose
     * registration date already holds this many owners is rejected.
     */
    private static final int DAILY_OWNER_LIMIT = 100;

    /**
     * Rejects a create request when {@link #DAILY_OWNER_LIMIT} or more owners have already been created
     * on the same day, compared by {@code registrationDate}. The count is taken before the new owner is
     * persisted, so it excludes the owner being created.
     *
     * @param registrationDate the registration date of the owner being created
     * @throws DailyOwnerLimitExceededException if the day already holds the maximum number of owners
     */
    /**
     * The fixed list of public holidays that are not treated as business days. A registration date that
     * lands on one of these dates is rolled forward, just as a weekend is.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Adjusts an effective registration date so that it always falls on a business day. When the given
     * date is a Saturday, a Sunday or a listed public holiday it is rolled forward one day at a time until
     * it reaches the next non-holiday business day; a weekday that is not a holiday is returned unchanged.
     * This applies equally to a date supplied in the request and to a date defaulted to the server date, and
     * the adjusted value is what becomes the owner's {@code registrationDate}, so every value derived from it
     * (the membership number's year segment, the daily create-limit count) uses the business-day-adjusted
     * date.
     *
     * @param date the effective registration date, supplied or defaulted
     * @return the same date when it is a business day, otherwise the next non-holiday business day
     */
    private LocalDate rollToBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * The number of owners that must already exist for a given day before a further create is flagged as a
     * bulk sign-up surge. When more than this many owners already share the new owner's registration date,
     * the created owner carries {@code bulkSignupWarning = true}.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * Determines whether the owner being created lands on a day that already holds a bulk sign-up surge,
     * i.e. more than {@link #BULK_SIGNUP_WARNING_THRESHOLD} owners already share its {@code registrationDate}.
     * The count is taken before the new owner is persisted, so it excludes the owner being created, mirroring
     * the daily create-limit rule's accumulation path.
     *
     * @param registrationDate the registration date of the owner being created
     * @return {@code true} when more than 80 owners were already created on that day, otherwise {@code false}
     */
    private boolean isBulkSignupDay(LocalDate registrationDate) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> registrationDate.equals(owner.getRegistrationDate()))
            .count();
        return existing > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * Rejects a create request whose supplied {@code registrationDate} is later than the server date. A
     * registration cannot be dated in the future. The check applies only to a date supplied in the request;
     * a {@code null} value (which is later defaulted to the server date) is accepted, and the comparison is
     * made against the supplied value before any business-day adjustment.
     *
     * @param registrationDate the registration date supplied in the request, or {@code null} when omitted
     * @throws FutureRegistrationDateException if a supplied registration date is later than the server date
     */
    private void rejectFutureRegistrationDate(LocalDate registrationDate) {
        if (registrationDate == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (registrationDate.isAfter(today)) {
            throw new FutureRegistrationDateException(registrationDate, today);
        }
    }

    private void rejectDailyLimitReached(LocalDate registrationDate) {
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> registrationDate.equals(owner.getRegistrationDate()))
            .count();
        if (existing >= DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitExceededException(registrationDate, DAILY_OWNER_LIMIT);
        }
    }

    /**
     * Rejects a create request whose owner collides with an existing owner on the single derived
     * {@code identityKey}. This one key consolidates the former separate telephone, email and household
     * duplicate checks: the create is rejected only when the new owner's WHOLE identity key exactly equals
     * an existing owner's. Because the (normalized) telephone is part of the key, two members of the same
     * household (same {@code householdId}) with different telephones have different keys and are both
     * allowed; only an exact full-key match is a duplicate. The owner's {@code householdId} has already
     * been computed (deterministically from the last name and postcode) before this check runs, so the
     * key compared here is the same one that is later returned.
     *
     * @param owner the owner being created, with normalized fields and its computed {@code householdId} set
     * @throws DuplicateIdentityException if an existing owner has the same identity key
     */
    private void rejectDuplicateIdentity(Owner owner) {
        String key = identityKey(owner);
        boolean taken = this.clinicService.findAllOwners().stream()
            .filter(existing -> !isDeleted(existing))
            .anyMatch(existing -> key.equals(identityKey(existing)));
        if (taken) {
            throw new DuplicateIdentityException(key);
        }
    }

    /**
     * Flags a soft (possible) duplicate on the owner being created. A soft match is an existing owner that,
     * while not a hard {@code identityKey} duplicate (that stronger check has already run, so its whole
     * identity key differs), shares this owner's last-name {@link #soundex(String) soundex} and exact
     * postcode. When at least one such owner exists, {@code possibleDuplicate} is set {@code true} and
     * {@code possibleDuplicateOf} to the matching owner's id (the lowest id when several match, for
     * determinism); otherwise {@code possibleDuplicate} is set {@code false} and no match id is recorded. A
     * new owner with no postcode has nothing to match on and is never a possible duplicate.
     * <p>
     * A declared household member ({@code sharesHousehold}) is never flagged: it is a known household member
     * rather than a suspected duplicate.
     *
     * @param owner the owner being created, with normalized telephone and validated postcode already set
     * @param sharesHousehold whether the create declared the owner a household member
     */
    private void assignPossibleDuplicate(Owner owner, boolean sharesHousehold) {
        Integer matchId = null;
        if (!sharesHousehold && owner.getPostcode() != null) {
            String key = identityKey(owner);
            String soundex = soundex(owner.getLastName());
            matchId = this.clinicService.findAllOwners().stream()
                .filter(existing -> !isDeleted(existing))
                .filter(existing -> soundex.equals(soundex(existing.getLastName()))
                    && owner.getPostcode().equals(existing.getPostcode())
                    && !key.equals(identityKey(existing)))
                .map(Owner::getId)
                .filter(id -> id != null)
                .min(Comparator.naturalOrder())
                .orElse(null);
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    /**
     * Whether an existing owner has been soft-deleted. A soft-deleted owner is retained and still
     * readable, but is ignored by the create endpoint's duplicate and identity checks.
     *
     * @param owner an existing owner
     * @return {@code true} when the owner is flagged deleted, otherwise {@code false}
     */
    private boolean isDeleted(Owner owner) {
        return Boolean.TRUE.equals(owner.getDeleted());
    }

    /**
     * Derives an owner's {@code identityKey}: the lower-case hex SHA-256 digest of the normalized
     * telephone, the lower-cased email (or an empty string when absent) and the {@link #soundex(String)
     * soundex} of the last name, joined by {@code '|'} in that order before hashing (e.g. the digest of
     * {@code '+61412345678||F650'}). Telephone and email are stored already normalized, so the stored
     * values are used directly. This single 64-hex key is what duplicate detection compares.
     *
     * @param owner the owner whose identity key is being derived
     * @return the owner's identity key, a 64-character lower-case hex SHA-256 digest
     */
    private String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String soundex = soundex(owner.getLastName());
        return sha256Hex(telephone + "|" + email + "|" + soundex);
    }

    /**
     * Computes the American Soundex code of the given value: the retained (upper-cased) first letter
     * followed by three digits derived from the remaining consonants, right-padded with zeros or
     * truncated to length four. Non-letters are ignored; adjacent letters mapping to the same digit are
     * coded once (bridged across {@code 'H'}/{@code 'W'}), and vowels reset the run so a repeated code
     * separated by a vowel is coded twice. A {@code null} or letter-free value yields an empty string.
     *
     * @param value the value (typically a last name) to encode
     * @return the four-character Soundex code, or an empty string when there are no letters
     */
    private String soundex(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prev = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexDigit(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            prev = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a single upper-case letter to its Soundex digit ({@code '1'}-{@code '6'}), or {@code '0'} for
     * vowels and the non-coding letters {@code H}/{@code W}/{@code Y}.
     *
     * @param c an upper-case letter
     * @return the letter's Soundex digit
     */
    private char soundexDigit(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    /**
     * Computes the owner's deterministic {@code householdId}: the first 12 hex characters of the SHA-256
     * digest of {@code '<normalizedLastName>|<postcode>'}, where the last name is normalized with
     * {@link #normalizeForComparison} and an absent postcode contributes an empty string. Because the
     * identifier is a pure function of last name and postcode, any two owners sharing those values share
     * the household automatically, with no cross-owner mutation. The value is assigned to every owner,
     * whether or not {@code sharesHousehold} was requested.
     *
     * @param owner the owner being created, with its validated postcode already set
     * @return the deterministic household identifier
     */
    private String computeHouseholdId(Owner owner) {
        String normalizedLastName = normalizeForComparison(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        return sha256Hex(normalizedLastName + "|" + postcode).substring(0, 12);
    }

    /**
     * Normalizes a value for household-identity comparison: leading and trailing whitespace is
     * removed, internal runs of whitespace are collapsed to a single space, and the result is
     * lower-cased.
     *
     * @param value the raw value to normalize (may be {@code null})
     * @return the normalized value, or an empty string when {@code value} is {@code null}
     */
    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
