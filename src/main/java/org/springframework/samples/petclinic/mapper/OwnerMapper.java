package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.controller.CityRegionResolver;
import org.springframework.samples.petclinic.rest.controller.TelephoneNormalizer;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.HashUtils;
import org.springframework.samples.petclinic.util.IdentityUtils;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /** Used to count the members of an owner's household when deriving derived fields. */
    @Autowired
    protected ClinicService clinicService;

    /** Single source of truth for the city-to-region mapping behind an owner's locality. */
    @Autowired
    protected CityRegionResolver cityRegionResolver;

    /** Formats the stored E.164 telephone into its human-readable display form. */
    @Autowired
    protected TelephoneNormalizer telephoneNormalizer;

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneNormalizer.toDisplayForm(owner.getTelephone()))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's self link: '/api/owners/' followed by the owner's id, or null when the
     * owner has no id assigned yet.
     */
    String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * The fiscal year (starting 1 July) that {@code date} falls in, identified by the
     * calendar year in which the fiscal year ends: a date in July–December belongs to the
     * fiscal year ending the following calendar year, while January–June belongs to the one
     * ending that same calendar year.
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year formatted 'FY<YY>', where YY is the two-digit FY segment carried by
     * the owner's assigned memberId (see {@link #memberIdCore}). When no memberId has been assigned
     * this falls back to the fiscal year of the (business-day-adjusted) {@code registrationDate}
     * (the fiscal year starts on 1 July), and returns null when there is no registrationDate either.
     */
    String fiscalYear(Owner owner) {
        String core = memberIdCore(owner.getMemberId());
        if (core != null && core.length() > 11) {
            return "FY" + core.substring(core.length() - 11, core.length() - 9);
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYear(registrationDate) % 100);
    }

    /**
     * The owner's age band derived from {@code birthDate} relative to the
     * {@code registrationDate}: 'MINOR' when under 18, 'ADULT' from 18 to 64
     * inclusive and 'SENIOR' at 65 or older. Returns null when either date is
     * absent so no band can be computed.
     */
    OwnerDto.AgeBandEnum ageBand(Owner owner) {
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
     * The Luhn check digit (0-9) over the digits contained in {@code s}: from the rightmost
     * digit leftward, every second digit is doubled (the rightmost first) with digits over 9
     * reduced by 9, and the check digit makes the total a multiple of ten.
     *
     * <p>This is the single Luhn derivation behind the application's identity values; like
     * {@link #fiscalYear(LocalDate)} and {@link #identityKey(Owner)} it is exposed so the check
     * digit is computed the one way wherever an identifier is assembled, including outside this
     * package.
     */
    public static int luhn(String s) {
        int sum = 0;
        boolean doubling = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubling) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * The owner's derived identity key: the SHA-256 hex digest of the normalized telephone,
     * the lower-cased email (or an empty string when absent) and the {@link IdentityUtils#soundex
     * soundex} of the last name, joined by '|'. This single key is the sole basis for duplicate
     * detection on create — two owners are duplicates only when their whole identityKey matches,
     * so two people sharing a household (a like-sounding last name and postcode) but carrying
     * different telephones derive different keys and are all permitted.
     */
    public static String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        String lastNameSoundex = IdentityUtils.soundex(owner.getLastName());
        return HashUtils.sha256Hex(telephone + "|" + email + "|" + lastNameSoundex);
    }

    /**
     * The owner's preferred contact channel: 'EMAIL' when an email address is
     * present, otherwise 'PHONE'.
     */
    String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The memberId's core: the {@code '<REGION><FY><HASH8><CHK>'} carried by the owner's assigned
     * memberId, with any de-duplication {@code '-<n>'} suffix stripped, or null when no memberId has
     * been assigned. This is the single place the stored identifier is split back into its fixed
     * segments (a trailing FY of 2, HASH8 of 8 and CHK of 1 — eleven characters — following the
     * variable-length region), so every rule keyed on a segment reads it the one way.
     */
    private static String memberIdCore(String memberId) {
        if (memberId == null) {
            return null;
        }
        int dash = memberId.indexOf('-');
        return dash >= 0 ? memberId.substring(0, dash) : memberId;
    }

    /**
     * The REGION segment carried by the owner's assigned memberId: the part of its
     * {@link #memberIdCore core} before the fixed trailing FY(2) + HASH8(8) + CHK(1). Returns null
     * when no memberId has been assigned, or when it carries no region segment.
     */
    private String identityRegion(Owner owner) {
        String core = memberIdCore(owner.getMemberId());
        return core != null && core.length() > 11 ? core.substring(0, core.length() - 11) : null;
    }

    /**
     * The owner's locality: the REGION segment of its assigned identity code (see
     * {@link #identityRegion}). When no code has been assigned this falls back to the canonical
     * region derived from the owner via {@link CityRegionResolver}, preferring the postcode over the
     * city, or 'UNKNOWN' when neither yields a known region.
     */
    String locality(Owner owner) {
        String region = identityRegion(owner);
        return region != null ? region : cityRegionResolver.regionFor(owner.getCity(), owner.getPostcode());
    }

    /**
     * The owner's segment, formatted '&lt;TIER&gt;_&lt;AREA&gt;'. TIER is 'PREMIUM' when the owner's
     * {@link #membershipLevel(Owner) membershipLevel} is 3 or more, otherwise 'STANDARD'. AREA is
     * 'METRO' when the owner's {@link #locality(Owner) locality} is a known region (NSW, VIC or QLD),
     * otherwise 'REGIONAL'.
     */
    OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        boolean knownRegion = cityRegionResolver.timezoneForRegion(locality(owner)) != null;
        String area = knownRegion ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * The owner's IANA timezone name derived from the owner's {@link #locality(Owner) locality}
     * via the fixed region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
     * QLD -> Australia/Brisbane). Returns null when the locality is not a region in the table.
     */
    String timezone(Owner owner) {
        return cityRegionResolver.timezoneForRegion(locality(owner));
    }

    /**
     * The owner's membership points: starting at 0, plus 2 when an email address is present,
     * plus 1 when namesakeCount is 0, plus 2 for a household of 3 or more members, plus 3 when
     * the owner's tenure is more than one fiscal year.
     */
    public int membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (uniqueName) {
            points += 1;
        }
        if (householdSize(owner) >= 3) {
            points += 2;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        boolean tenured = registrationDate != null
            && fiscalYear(LocalDate.now()) - fiscalYear(registrationDate) > 1;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level from 1 to 4, derived from {@link #membershipPoints}
     * via {@link #levelForPoints} and then held to the household level-ceiling by
     * {@link #cappedByHousehold}.
     */
    public int membershipLevel(Owner owner) {
        return cappedByHousehold(owner, levelForPoints(membershipPoints(owner)));
    }

    /**
     * Hold {@code level} to the household level-ceiling: an owner's membershipLevel may not exceed
     * one above the current maximum membershipLevel among the household members that already
     * existed when this owner registered — the other, non-deleted owners sharing its householdId
     * that were created earlier (a strictly lower id, ids being monotonic). When no such earlier
     * household member exists no ceiling applies and {@code level} is returned unchanged.
     *
     * @param owner the owner whose own level is being capped
     * @param level the owner's uncapped membership level
     * @return the level, capped at one above the earlier household members' maximum
     */
    private int cappedByHousehold(Owner owner, int level) {
        Integer id = owner.getId();
        String householdId = owner.getHouseholdId();
        if (id == null || householdId == null) {
            return level;
        }
        OptionalInt maxExisting = clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> existing.getId() != null && existing.getId() < id)
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .mapToInt(this::membershipLevel)
            .max();
        return maxExisting.isEmpty() ? level : Math.min(level, maxExisting.getAsInt() + 1);
    }

    /**
     * The numeric membership level from 1 to 4 that {@code points} maps to: level 1 for 0-1
     * points, level 2 for 2-3 points, level 3 for 4-5 points and level 4 for 6 or more points.
     */
    int levelForPoints(int points) {
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

    /**
     * The owners belonging to {@code owner}'s household, i.e. those sharing its householdId
     * (including the owner itself once persisted). Returns an empty list when the owner has no
     * household id assigned.
     */
    List<Owner> householdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return List.of();
        }
        return clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .toList();
    }

    /**
     * The number of owners belonging to {@code owner}'s household, i.e. those sharing its
     * householdId (including the owner itself once persisted). Returns 0 when the owner has no
     * household id assigned.
     */
    int householdSize(Owner owner) {
        return householdMembers(owner).size();
    }

    /**
     * The owner's salutation: the {@code title} and last name separated by a single space
     * (e.g. 'DR who'), or just the last name when no title has been supplied.
     */
    String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot, e.g. 'J.S.'.
     */
    String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
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
