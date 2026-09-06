package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * More than this many owners already sharing a {@code registrationDate} raises the
     * {@code bulkSignupWarning} on responses for that day.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    /**
     * The hard per-city capacity limit: a city already holding this many owners rejects further
     * creates (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.EnsureCityCapacity}).
     */
    private static final int CITY_CAPACITY_LIMIT = 50;

    /**
     * A city already holding at least this many (but fewer than {@link #CITY_CAPACITY_LIMIT})
     * owners raises the {@code capacityWarning} on responses, signalling it is approaching the limit.
     */
    private static final int CITY_CAPACITY_WARNING_THRESHOLD = 40;

    @Autowired
    protected OwnerRepository ownerRepository;

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "capacityWarning", expression = "java(capacityWarning(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner))")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.Telephones.toDisplay(owner.getTelephone()))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The canonical API path to this owner: {@code /api/owners/} followed by the owner's id.
     * Returns {@code null} when the owner has no id (e.g. before it has been persisted).
     */
    protected String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * The owner's {@code membershipPoints}: the raw loyalty score (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.Memberships}), built from a
     * present email, no namesakes, a household of 3 or more and tenure over one elapsed fiscal year.
     */
    protected int membershipPoints(Owner owner) {
        long householdSize = org.springframework.samples.petclinic.rest.function.owner.Households
                .size(owner, ownerRepository);
        return org.springframework.samples.petclinic.rest.function.owner.Memberships
                .membershipPoints(owner, householdSize);
    }

    /**
     * The owner's {@code membershipLevel}: the grade {@code membershipPoints} buckets into — 1 for
     * 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more — after the household level ceiling, which
     * caps a member at one above the current maximum level among the household members that already
     * existed when it joined (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.Memberships#cappedLevel}).
     */
    protected int membershipLevel(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.Memberships
                .cappedLevel(owner, ownerRepository);
    }

    /**
     * The owner's preferred contact channel: {@code EMAIL} when an email address is
     * present (non-null and non-blank), otherwise {@code PHONE}.
     */
    protected OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /** The age (in years) at which an owner stops being a minor and becomes an adult. */
    private static final int ADULT_AGE = 18;

    /** The age (in years) at which an owner becomes a senior. */
    private static final int SENIOR_AGE = 65;

    /**
     * Derives the owner's {@code ageBand} from {@code birthDate} as of the
     * {@code registrationDate}: {@code MINOR} when under {@value #ADULT_AGE},
     * {@code ADULT} when {@value #ADULT_AGE} to {@value #SENIOR_AGE}-1, and
     * {@code SENIOR} when {@value #SENIOR_AGE} or older. Returns {@code null} when
     * either the birth date or the registration date is absent, so an owner created
     * without a birth date has no age band.
     */
    protected OwnerDto.AgeBandEnum ageBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < ADULT_AGE) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < SENIOR_AGE) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code locality} (canonical region string): the plain, user-facing region
     * derived from the owner's postcode (falling back to the city, then {@code "UNKNOWN"}). This is
     * the region as shown to callers, never the identifier-dressed form baked into the
     * {@code memberId}.
     */
    protected String locality(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.MemberIds.plainRegionOf(owner);
    }

    /** The lowest {@code membershipLevel} that grades an owner's segment TIER as {@code PREMIUM}. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    /**
     * The owner's {@code ownerSegment} formatted '&lt;TIER&gt;_&lt;AREA&gt;'. TIER is
     * {@code PREMIUM} when {@link #membershipLevel(Owner) membershipLevel} is
     * {@value #PREMIUM_MIN_LEVEL} or more, otherwise {@code STANDARD}. AREA is {@code METRO} when
     * the {@link #locality(Owner) locality} is a known region (NSW, VIC or QLD), otherwise
     * {@code REGIONAL}.
     */
    protected OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(locality(owner)) ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /** Fixed region -> IANA timezone table. */
    private static final java.util.Map<String, String> REGION_TIMEZONE = java.util.Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's {@code timezone} (IANA name) from the {@link #locality(Owner)
     * locality}/region via the fixed region-to-timezone table (NSW->Australia/Sydney,
     * VIC->Australia/Melbourne, QLD->Australia/Brisbane). Returns {@code null} when the
     * region is not in the table (e.g. an {@code UNKNOWN} locality), so such owners have
     * no timezone.
     */
    protected String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * The owner's {@code fiscalYear}: the {@code FY<YY>} label for the fiscal year (starting 1 July)
     * containing the business-day-adjusted {@code registrationDate} (e.g. 'FY27'). Returns
     * {@code null} when the registration date is absent (e.g. legacy seed owners).
     */
    protected String fiscalYear(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return org.springframework.samples.petclinic.rest.function.owner.FiscalYears.label(registrationDate);
    }

    /**
     * True when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} other owners have already
     * been created for this owner's {@code registrationDate}, using the same per-day
     * accumulation the daily create limit enforces (the owner itself is excluded). Returns
     * {@code false} when the owner has no registration date (e.g. legacy seed owners).
     */
    protected boolean bulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long count = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * True when the owner's city already holds between {@value #CITY_CAPACITY_WARNING_THRESHOLD}
     * and {@code CITY_CAPACITY_LIMIT - 1} owners (the owner itself excluded), signalling the city
     * is approaching the hard capacity limit of {@value #CITY_CAPACITY_LIMIT}; otherwise false. The
     * city is compared case-insensitively with runs of whitespace collapsed to a single space,
     * mirroring {@link org.springframework.samples.petclinic.rest.function.owner.EnsureCityCapacity}.
     */
    protected boolean capacityWarning(Owner owner) {
        String city = normalizeCity(owner.getCity());
        long count = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> normalizeCity(existing.getCity()).equals(city))
                .count();
        return count >= CITY_CAPACITY_WARNING_THRESHOLD && count < CITY_CAPACITY_LIMIT;
    }

    private static String normalizeCity(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * The owner's {@code riskFlag}: true when the owner warrants a manual review because any one of
     * these holds — the owner is a possible duplicate ({@code possibleDuplicate} is true), the email
     * domain is disposable-adjacent (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerEmails#isDisposableAdjacent}),
     * or the city is over its soft capacity (the same 40-and-over threshold that raises
     * {@link #capacityWarning(Owner) capacityWarning}); otherwise false.
     */
    protected boolean riskFlag(Owner owner) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean disposableAdjacent = org.springframework.samples.petclinic.rest.function.owner.OwnerEmails
                .isDisposableAdjacent(owner.getEmail());
        return possibleDuplicate || disposableAdjacent || capacityWarning(owner);
    }

    /**
     * The owner's {@code salutation}: the {@code title} followed by a single space and the last
     * name when a title is supplied (non-null and non-blank), otherwise just the last name.
     */
    protected String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
