package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
            expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
                    + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "membershipPoints", expression = "java(OwnerMapper.membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(OwnerMapper.membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "apiVersion", expression = "java(API_VERSION)")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "riskFlag", expression = "java(OwnerMapper.riskFlag(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The version of the owner API contract this mapper produces. */
    int API_VERSION = 2;

    /**
     * Builds the owner's version-2 {@code identity}: the nested object grouping the three derived
     * identifiers — {@code memberId} and {@code householdId} (assigned and stored at create time) and
     * the derived {@code identityKey} (see {@link #identityKey(Owner)}). Each is rederived under version
     * 2 with the fixed {@code 'V2'} version tag mixed in; the user-facing {@code locality},
     * {@code timezone} and {@code ownerSegment} stay the plain region code.
     */
    default OwnerIdentityDto identity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(identityKey(owner));
        return identity;
    }

    /**
     * Derives the owner's {@code riskFlag}: {@code true} when the owner warrants a manual risk
     * review because any of these hold — the owner is a possible duplicate
     * ({@code possibleDuplicate}), the email domain is disposable-adjacent (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerEmail#isDisposableAdjacent}),
     * or the city is over its soft capacity ({@code capacityWarning}, i.e. the city held 40-49 owners
     * when this one was created). Otherwise {@code false}.
     */
    static boolean riskFlag(Owner owner) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean overSoftCapacity = Boolean.TRUE.equals(owner.getCapacityWarning());
        boolean disposableAdjacent = org.springframework.samples.petclinic.rest.function.owner.OwnerEmail
                .isDisposableAdjacent(owner.getEmail());
        return possibleDuplicate || disposableAdjacent || overSoftCapacity;
    }

    /**
     * Derives the owner's {@code selfLink}: the canonical relative URL of this owner, formatted
     * {@code '/api/owners/<id>'}. Returns {@code null} when the id is absent, so owners without an
     * id map cleanly.
     */
    default String selfLink(Owner owner) {
        if (owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * The calendar year in which the fiscal year containing {@code date} ends. The fiscal year runs
     * from 1 July to 30 June, so a date on or after 1 July belongs to the fiscal year ending the
     * following calendar year (e.g. 15 Aug 2026 falls in the fiscal year ending 2027). All fiscal-year
     * derived values — {@link #fiscalYear(Owner)}, the FY segment of the
     * {@link org.springframework.samples.petclinic.rest.function.owner.MemberId} and the tenure bonus in
     * {@link #membershipPoints(Owner)} — go through this single definition.
     */
    static int fiscalYearEnding(java.time.LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? year + 1 : year;
    }

    /**
     * Derives the owner's {@code fiscalYear}, formatted {@code 'FY<YY>'} where YY is the last two
     * digits of the fiscal year (starting 1 July) that contains the business-day-adjusted
     * {@code registrationDate} — so 15 Aug 2026 yields {@code 'FY27'}. Returns {@code null} when the
     * registrationDate is absent, so owners without one map cleanly.
     */
    default String fiscalYear(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearEnding(owner.getRegistrationDate()) % 100);
    }

    /**
     * The fixed set of known regions (localities) treated as METRO; any other locality
     * (including 'UNKNOWN') is REGIONAL.
     */
    Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    /**
     * Derives the owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}. TIER is
     * {@code 'PREMIUM'} when {@link #membershipLevel(Owner)} is 3 or more, otherwise
     * {@code 'STANDARD'}. AREA is {@code 'METRO'} when the {@link #locality(Owner)} is a known
     * region (NSW, VIC or QLD), otherwise {@code 'REGIONAL'} — so the result is one of
     * {@code 'PREMIUM_METRO'}, {@code 'PREMIUM_REGIONAL'}, {@code 'STANDARD_METRO'} or
     * {@code 'STANDARD_REGIONAL'}.
     */
    default String ownerSegment(Owner owner) {
        Integer level = membershipLevel(owner);
        String tier = level != null && level >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Derives the owner's {@code salutation}: the {@code title} and {@code lastName} joined by a
     * single space ({@code '<title> <lastName>'}) when a title is present, otherwise just the
     * {@code lastName}. So an owner titled 'DR' named 'Who' yields {@code 'DR Who'}.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Derives the owner's {@code ageBand} from {@code birthDate}, computed against the
     * {@code registrationDate}: {@code 'MINOR'} when under 18, {@code 'ADULT'} from 18 to 64 and
     * {@code 'SENIOR'} at 65 or over. Returns {@code null} when either date is absent, so owners
     * without a birthDate map cleanly.
     */
    default String ageBand(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * Derives the owner's version-2 {@code identityKey}, the single key all duplicate detection is
     * expressed through: the SHA-256 hex of {@code 'V2'|normalizedTelephone|lowerEmail|soundex(lastName)}
     * (see {@link org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity}). Returned
     * inside the nested {@code identity} object, not at the top level.
     */
    default String identityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity
                .identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * Derives the owner's {@code telephoneDisplay}: the stored E.164 {@code telephone} formatted for
     * humans as the country code, a space, then the national digits grouped left-to-right in threes
     * (e.g. {@code '+61412345678'} becomes {@code '+61 412 345 678'}). The known country codes are
     * {@code +61} (2 digits, matching the normalization in
     * {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwnerFields}) and
     * {@code +1} (1 digit); any other leading digit is treated as a two-digit country code. Returns
     * {@code null} when the telephone is absent or not a {@code +}-prefixed run of digits, so the raw
     * {@code telephone} is left untouched in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return null;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("\\d+")) {
            return null;
        }
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return null;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
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
     * Fixed region-to-timezone table mapping each locality (region) to its IANA timezone name.
     */
    Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's {@code timezone}: the IANA timezone name for the owner's {@link #locality(Owner)}
     * (region) from the fixed region-to-timezone table (NSW→Australia/Sydney, VIC→Australia/Melbourne,
     * QLD→Australia/Brisbane). Returns {@code null} when the locality is not in the table (e.g. 'UNKNOWN'),
     * so owners outside the known regions map cleanly.
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * Derives the owner's preferred contact channel: {@code 'EMAIL'} when an email is present,
     * otherwise {@code 'PHONE'}.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Derives the owner's locality (region) from the postcode range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), falling back to the city table, yielding {@code 'UNKNOWN'} when neither resolves
     * (see {@link org.springframework.samples.petclinic.rest.function.owner.MemberId#region}). This is
     * the same region embedded as the {@code REGION} segment of the owner's {@code memberId}, so
     * locality and memberId stay consistent.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.MemberId
                .region(owner.getPostcode(), owner.getCity());
    }

    /**
     * Derives the owner's membership points: starting at 0, plus 2 when an email is present, plus 1
     * when {@code namesakeCount} is 0, plus 2 for a household of 3 or more (its
     * {@code householdMemberCount}), plus 3 when the owner's tenure — the number of fiscal years
     * (starting 1 July) elapsed between {@code registrationDate} and today — exceeds 1.
     */
    static Integer membershipPoints(Owner owner) {
        return membershipPoints(owner, owner.getHouseholdMemberCount());
    }

    /**
     * As {@link #membershipPoints(Owner)}, but scoring the "household of 3 or more" bonus against the
     * supplied {@code householdMemberCount} rather than the owner's stored one. Used when evaluating an
     * existing household member against the household as it stands <em>now</em> (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.AssignMembershipLevel}), since a
     * member's own count is frozen at its own create time.
     */
    static Integer membershipPoints(Owner owner, Integer householdMemberCount) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        boolean largeHousehold = householdMemberCount != null && householdMemberCount >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = owner.getRegistrationDate() != null
                && (fiscalYearEnding(java.time.LocalDate.now())
                        - fiscalYearEnding(owner.getRegistrationDate())) > 1;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's effective membership level. When a level has been stored on the entity (assigned at
     * create time by
     * {@link org.springframework.samples.petclinic.rest.function.owner.AssignMembershipLevel}, which
     * applies the household level ceiling) that value stands; otherwise it falls back to the level
     * derived directly from points (see {@link #derivedMembershipLevel(Owner)}), so seed owners
     * without a stored level still map cleanly.
     */
    static Integer membershipLevel(Owner owner) {
        if (owner.getMembershipLevel() != null) {
            return owner.getMembershipLevel();
        }
        return derivedMembershipLevel(owner);
    }

    /**
     * Derives the owner's membership level from 1 to 4 by mapping {@link #membershipPoints(Owner)}:
     * 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more. This is the uncapped level, before
     * the household ceiling applied at create time.
     */
    static Integer derivedMembershipLevel(Owner owner) {
        return levelForPoints(membershipPoints(owner));
    }

    /**
     * As {@link #derivedMembershipLevel(Owner)}, but scoring the household bonus against the supplied
     * {@code householdMemberCount} — the household's current size — rather than the owner's frozen one.
     */
    static Integer derivedMembershipLevel(Owner owner, Integer householdMemberCount) {
        return levelForPoints(membershipPoints(owner, householdMemberCount));
    }

    private static Integer levelForPoints(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "memberId", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdMemberCount", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "membershipLevel", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    default OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
