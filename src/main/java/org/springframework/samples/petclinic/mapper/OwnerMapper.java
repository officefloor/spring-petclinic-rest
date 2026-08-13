package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.IdentityKeys;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Maps Owner &amp; OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * A create is flagged with {@code bulkSignupWarning=true} once more than this many owners have
     * already been created on the same registration date.
     */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    /**
     * A create is flagged with {@code capacityWarning=true} when its city already holds at least
     * this many owners (excluding the owner itself), approaching the hard capacity limit.
     */
    private static final int CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * The hard per-city capacity limit; at or above it a create is rejected rather than warned, so
     * the warning window is {@code [CAPACITY_WARNING_THRESHOLD, CITY_CAPACITY)}.
     */
    private static final int CITY_CAPACITY = 50;

    @Autowired
    protected ClinicService clinicService;

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "capacityWarning", expression = "java(capacityWarning(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "apiVersion", expression = "java(apiVersion())")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner identity API version carried by every owner response. Version 2 groups the owner's
     * identifiers under the {@code identity} object and derives them with the version-2 identity
     * algorithm.
     */
    private static final int API_VERSION = 2;

    /**
     * Derives the response's top-level {@code apiVersion}: the fixed owner-identity API version
     * ({@value #API_VERSION}).
     */
    protected Integer apiVersion() {
        return API_VERSION;
    }

    /**
     * Builds the owner's nested {@code identity} object grouping the owner's version-2 identifiers:
     * its {@code memberId} and {@code householdId} (assigned on create) and its derived
     * {@code identityKey}. Under version 2 these are the only place the {@code 'V2'} version tag
     * appears; the user-facing {@code locality}, {@code timezone} and {@code ownerSegment} keep the
     * plain region code.
     */
    protected OwnerIdentityDto identity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(identityKey(owner));
        return identity;
    }

    /**
     * The month the fiscal year starts on (1 July).
     */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    /**
     * Derives the fiscal year (as an integer) of the given date. The fiscal year starts on 1 July,
     * so a date on or after 1 July belongs to the following calendar year's fiscal year (e.g.
     * 2026-07-01 is fiscal year 2027) while an earlier date takes the calendar year unchanged.
     */
    private int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derives the owner's {@code fiscalYear}, formatted {@code FY<YY>}. When a {@code memberId} has
     * been assigned the YY is the FY component embedded in it ({@code <REGION><FY><HASH8><CHK>}), so
     * the fiscal year moves in lockstep with the identity assigned on create. When no member id has
     * been assigned (e.g. seed data) it falls back to the last two digits of the fiscal year
     * (starting 1 July) of the owner's business-day-adjusted {@code registrationDate}, or
     * {@code null} when no registration date is present either.
     */
    protected String fiscalYear(Owner owner) {
        String fromMemberId = fiscalYearFromMemberId(owner.getMemberId());
        if (fromMemberId != null) {
            return "FY" + fromMemberId;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(registrationDate) % 100);
    }

    /**
     * The number of fixed-width trailing characters of a member id's core (before any collision
     * suffix): the 2-digit FY, the 8-character HASH8 and the single CHK digit.
     */
    private static final int MEMBER_ID_SUFFIX_LENGTH = 11;

    /**
     * Extracts the core of a {@code memberId} — the {@code <REGION><FY><HASH8><CHK>} value before any
     * {@code -<n>} collision suffix — or {@code null} when the member id is absent or too short to
     * carry a region ahead of its fixed-width FY, HASH8 and CHK segments.
     */
    private String memberIdCore(String memberId) {
        if (memberId == null) {
            return null;
        }
        int dash = memberId.indexOf('-');
        String core = dash >= 0 ? memberId.substring(0, dash) : memberId;
        return core.length() > MEMBER_ID_SUFFIX_LENGTH ? core : null;
    }

    /**
     * Derives the 2-digit FY component of an owner's {@code memberId} — the two digits following the
     * REGION prefix — or {@code null} when no member id has been assigned.
     */
    private String fiscalYearFromMemberId(String memberId) {
        String core = memberIdCore(memberId);
        if (core == null) {
            return null;
        }
        int fyStart = core.length() - MEMBER_ID_SUFFIX_LENGTH;
        return core.substring(fyStart, fyStart + 2);
    }

    /**
     * Derives the owner's {@code ageBand} from their {@code birthDate} relative to their
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64, and
     * {@code SENIOR} at 65 or older. Returns {@code null} when no birth date was supplied, or when
     * the registration date (against which the age is measured) is absent.
     */
    protected OwnerDto.AgeBandEnum ageBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * The country calling codes (without the leading {@code '+'}) the display formatter recognises,
     * longest first so the correct code is matched before a shorter prefix of it. Australia
     * ({@code +61}) and the North American Numbering Plan ({@code +1}) mirror the codes the create
     * endpoint normalizes against.
     */
    private static final List<String> KNOWN_COUNTRY_CODES = List.of("61", "1");

    /**
     * Derives the owner's {@code telephoneDisplay}: the stored E.164 {@code telephone} formatted for
     * humans as the country code, a space, then the national digits grouped in threes (e.g.
     * {@code +61412345678} becomes {@code +61 412 345 678}). Returns the raw value unchanged when it
     * is {@code null} or not an E.164 number ({@code '+'} followed by digits).
     */
    protected String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.matches("\\+[0-9]+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String countryCode = KNOWN_COUNTRY_CODES.stream()
            .filter(digits::startsWith)
            .findFirst()
            .orElse("");
        String national = digits.substring(countryCode.length());
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + " " + grouped;
    }

    /**
     * Derives the owner's {@code identityKey}, the single consolidated duplicate-detection key: the
     * lower-case hex SHA-256 of {@code "V2" + '|' + normalizedTelephone + '|' + lowerEmail + '|' +
     * soundex(lastName)}. A {@code null} telephone or email contributes an empty segment; the leading
     * {@code 'V2'} version tag is the version-2 identity marker.
     */
    protected String identityKey(Owner owner) {
        return IdentityKeys.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    protected OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * The inclusive 4-digit postcode range that identifies each region, keyed by region code
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derives the owner's plain locality (region) directly, preferring the postcode: a 4-digit
     * postcode falling in a known region's range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099)
     * yields that region, otherwise the city-to-region table (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD) is consulted, returning {@code UNKNOWN} when neither identifies a region.
     *
     * <p>The locality is a user-facing field, not an identifier, so it stays the plain region code
     * (e.g. {@code NSW}); the version-2 {@code 'V2'} tag mixed into the {@code identity} identifiers
     * (memberId, householdId, identityKey) never appears here.
     */
    protected String locality(Owner owner) {
        String fromPostcode = regionFromPostcode(owner.getPostcode());
        if (fromPostcode != null) {
            return fromPostcode;
        }
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        return switch (city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * The IANA timezone of each region, keyed by region code (NSW->Australia/Sydney,
     * VIC->Australia/Melbourne, QLD->Australia/Brisbane).
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's {@code timezone}: the IANA timezone name for the owner's
     * {@link #locality(Owner) locality/region}, looked up in the fixed region-to-timezone table
     * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane). Returns
     * {@code null} when the region has no known timezone (e.g. {@code UNKNOWN}).
     */
    protected String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * Resolves the region whose postcode range contains the given postcode, or {@code null} when
     * the postcode is absent, not 4 digits, or in no known range.
     */
    private String regionFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The minimum {@link #membershipLevel(Owner) membership level} at which an owner is a
     * {@code PREMIUM} tier for {@link #ownerSegment(Owner) segmentation}; below it the owner is
     * {@code STANDARD}.
     */
    private static final int PREMIUM_TIER_MIN_LEVEL = 3;

    /**
     * The regions treated as {@code METRO} for {@link #ownerSegment(Owner) segmentation}; any other
     * locality (e.g. {@code UNKNOWN}) is {@code REGIONAL}.
     */
    private static final java.util.Set<String> METRO_REGIONS = java.util.Set.of("NSW", "VIC", "QLD");

    /**
     * Derives the owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>} (one of
     * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
     * {@code STANDARD_REGIONAL}). TIER is {@code PREMIUM} when the owner's
     * {@link #membershipLevel(Owner) membership level} is {@value #PREMIUM_TIER_MIN_LEVEL} or more,
     * otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's
     * {@link #locality(Owner) locality} is a known region (NSW, VIC or QLD), otherwise
     * {@code REGIONAL}.
     */
    protected OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= PREMIUM_TIER_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality(owner)) ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * The minimum household size (counting the owner itself) that earns household membership
     * points.
     */
    private static final int HOUSEHOLD_POINTS_SIZE = 3;

    /**
     * An owner's tenure must span strictly more than this many elapsed fiscal years to earn the
     * tenure membership points. A newly created owner has zero elapsed fiscal years and so never
     * clears this threshold.
     */
    private static final int TENURE_POINTS_FISCAL_YEARS = 1;

    /**
     * Derives the owner's membership points: it starts at 0, gains 2 when an email is present,
     * gains 1 when the owner has no namesakes (namesakeCount is 0), gains 2 when the owner's
     * household has {@value #HOUSEHOLD_POINTS_SIZE} or more members, and gains 3 once the owner's
     * tenure exceeds {@value #TENURE_POINTS_FISCAL_YEARS} elapsed fiscal year(s).
     */
    protected Integer membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (householdSize(owner) >= HOUSEHOLD_POINTS_SIZE) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > TENURE_POINTS_FISCAL_YEARS) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives the owner's membership level, capped by their household. The level starts from the
     * owner's own {@link #uncappedMembershipLevel(Owner) points-based level} but may not exceed one
     * above the current maximum level among the owner's existing household members (other owners
     * sharing its {@code householdId}). When the owner has no existing household member no cap
     * applies and the points-based level is returned unchanged.
     */
    protected Integer membershipLevel(Owner owner) {
        int level = uncappedMembershipLevel(owner);
        OptionalInt maxHouseholdLevel = otherHouseholdMembers(owner)
            .mapToInt(this::uncappedMembershipLevel)
            .max();
        if (maxHouseholdLevel.isEmpty()) {
            return level;
        }
        return Math.min(level, maxHouseholdLevel.getAsInt() + 1);
    }

    /**
     * The owner's points-based membership level, before the household ceiling is applied: level 1
     * for 0-1 points, level 2 for 2-3, level 3 for 4-5, and level 4 for 6 or more (see
     * {@link #membershipPoints(Owner) membership points}). The household members' maximum against
     * which {@link #membershipLevel(Owner)} caps is measured with this uncapped value, so mutual
     * household members do not recurse into each other's caps.
     */
    private int uncappedMembershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The owner's existing household members: every other owner (excluding the owner itself) sharing
     * its {@code householdId}. Empty when the owner has no household identifier or is the sole member
     * of its household.
     */
    private Stream<Owner> otherHouseholdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return Stream.empty();
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()));
    }

    /**
     * The number of owners in this owner's household — the count of owners (including this one)
     * sharing its {@code householdId}. Returns 0 when the owner has no household identifier.
     */
    private long householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 0;
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * The owner's tenure in elapsed fiscal years: the number of fiscal years (each starting 1 July)
     * between their {@code registrationDate} and the current date, i.e. the current fiscal year less
     * the registration date's fiscal year. Returns 0 when no registration date is present, so an
     * owner without a registration date is treated as having no tenure.
     */
    private long tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return (long) fiscalYearOf(LocalDate.now()) - fiscalYearOf(registrationDate);
    }

    /**
     * Flags a bulk-signup day: {@code true} once more than {@link #BULK_SIGNUP_THRESHOLD} other
     * owners already carry this owner's {@code registrationDate}. The owner itself is excluded from
     * the count, so on a create the flag reflects the owners that pre-existed it that day, matching
     * the accumulation the per-day create limit is enforced against. Returns {@code false} when the
     * owner has no registration date.
     */
    protected Boolean bulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long othersOnDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return othersOnDay > BULK_SIGNUP_THRESHOLD;
    }

    /**
     * Flags a city approaching its hard capacity limit: {@code true} when this owner's city already
     * holds between {@link #CAPACITY_WARNING_THRESHOLD} and {@link #CITY_CAPACITY} minus one owners,
     * inclusive (excluding the owner itself), so on a create the flag reflects the owners that
     * pre-existed it in that city, matching the count the per-city capacity limit is enforced
     * against. Cities are compared case-insensitively, as they are for the capacity limit. Returns
     * {@code false} when the owner has no city.
     */
    protected Boolean capacityWarning(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return false;
        }
        long othersInCity = this.clinicService.findAllOwners().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
            .count();
        return othersInCity >= CAPACITY_WARNING_THRESHOLD && othersInCity < CITY_CAPACITY;
    }

    /**
     * The known disposable/throwaway email providers, mirroring the create endpoint's blocklist.
     * An email whose domain exactly matches one of these is rejected on create; the risk flag
     * instead catches domains that are merely <em>adjacent</em> to one of them (see
     * {@link #disposableAdjacentEmail(Owner)}).
     */
    private static final java.util.Set<String> DISPOSABLE_EMAIL_DOMAINS =
        java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The second-level labels of the known disposable providers (e.g. {@code mailinator} from
     * {@code mailinator.com}), used to recognise disposable-adjacent domains regardless of their
     * top-level domain or any subdomain in front.
     */
    private static final java.util.Set<String> DISPOSABLE_DOMAIN_LABELS = DISPOSABLE_EMAIL_DOMAINS.stream()
        .map(domain -> domain.substring(0, domain.lastIndexOf('.')))
        .collect(java.util.stream.Collectors.toUnmodifiableSet());

    /**
     * Derives the owner's {@code riskFlag}: {@code true} when any of these hold, otherwise
     * {@code false}. The owner is a possible duplicate ({@code possibleDuplicate} is true), the
     * email domain is {@link #disposableAdjacentEmail(Owner) disposable-adjacent}, or the city is
     * over its soft capacity (the {@link #capacityWarning(Owner) capacity warning} is raised).
     */
    protected Boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || disposableAdjacentEmail(owner)
            || Boolean.TRUE.equals(capacityWarning(owner));
    }

    /**
     * Whether the owner's email domain is <em>disposable-adjacent</em>: it shares a second-level
     * label with a known disposable provider (mailinator, tempmail, guerrillamail), so a subdomain
     * ({@code promo.mailinator.com}) or an alternate top-level domain ({@code tempmail.co}) that
     * slips past the create endpoint's exact-match blocklist is still recognised. Returns
     * {@code false} when the owner has no email or the domain shares no such label.
     */
    protected boolean disposableAdjacentEmail(Owner owner) {
        String email = owner.getEmail();
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(java.util.Locale.ROOT);
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_DOMAIN_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Derives the owner's {@code salutation}: the owner's {@code title} and {@code lastName}
     * separated by a single space (e.g. {@code DR Franklin}), or just the {@code lastName} when no
     * title was supplied (a {@code null} or blank title).
     */
    protected String salutation(Owner owner) {
        String title = owner.getTitle();
        String lastName = owner.getLastName();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Derives the owner's {@code selfLink}: the canonical link to this owner resource, formatted
     * {@code /api/owners/<id>} using the owner's id. Returns {@code null} when the owner has no id.
     */
    protected String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
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
