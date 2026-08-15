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

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    @Mapping(target = "apiVersion", expression = "java(2)")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's version-2 identifiers — the memberId, identityKey and householdId — under a
     * single nested {@code identity} object in the owner response. Each value is carried through
     * unchanged; the version-2 derivation (mixing a fixed {@code 'V2'} tag into the region code used
     * inside the identifiers) happens where each identifier is assigned.
     */
    default OwnerIdentityDto identity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(owner.getIdentityKey());
        identity.setHouseholdId(owner.getHouseholdId());
        return identity;
    }

    /**
     * Registrable labels (the second-level domain, e.g. {@code mailinator} in
     * {@code mailinator.com}) of known disposable email providers. An owner's email domain is
     * considered <em>disposable-adjacent</em> when its registrable label matches one of these,
     * regardless of the top-level domain — so a look-alike such as {@code mailinator.net} is
     * flagged even though the exact {@code mailinator.com} would already have been rejected at
     * create time.
     */
    java.util.Set<String> DISPOSABLE_ADJACENT_LABELS = java.util.Set.of(
        "mailinator", "tempmail", "guerrillamail", "throwaway", "trashmail",
        "yopmail", "sharklasers", "getnada");

    /**
     * Whether the owner's email domain is disposable-adjacent: its registrable label (the
     * second-level domain) matches a known disposable provider (see {@link #DISPOSABLE_ADJACENT_LABELS}),
     * regardless of the top-level domain. Returns {@code false} when the owner has no email or a
     * domain with no discernible registrable label.
     */
    default boolean disposableAdjacentEmail(Owner owner) {
        String email = owner.getEmail();
        if (email == null || email.isEmpty()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(java.util.Locale.ROOT);
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        String registrableLabel = labels[labels.length - 2];
        return DISPOSABLE_ADJACENT_LABELS.contains(registrableLabel);
    }

    /**
     * The owner's derived overall risk flag, computed on read. {@code true} when any of these hold:
     * the owner is a possible duplicate ({@link Owner#getPossibleDuplicate()}), its email domain is
     * {@link #disposableAdjacentEmail(Owner) disposable-adjacent}, or its city is over its soft
     * capacity ({@link Owner#getCapacityWarning()}, set when the city was at or beyond the per-city
     * soft-capacity threshold at create time); otherwise {@code false}.
     */
    default Boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || disposableAdjacentEmail(owner)
            || Boolean.TRUE.equals(owner.getCapacityWarning());
    }

    /**
     * Derives the owner's salutation: the {@code title} followed by a single space and the last
     * name (e.g. {@code DR who}), or just the last name when no title is given (absent, empty or
     * blank).
     */
    default String salutation(Owner owner) {
        String lastName = owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Formats the stored E.164 {@code telephone} for humans: the {@code '+'} and country code, a
     * space, then the national digits grouped in threes (e.g. {@code +61412345678} -&gt;
     * {@code +61 412 345 678}). The country code is taken as {@code 61} (Australia) or {@code 1}
     * (NANP) when recognised, otherwise a single leading digit. Returns {@code null} when the
     * telephone is absent, and the value unchanged when it is not a {@code '+'}-prefixed E.164
     * number.
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || telephone.isEmpty()) {
            return null;
        }
        if (!telephone.startsWith("+") || !telephone.substring(1).matches("\\d+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength;
        if (digits.startsWith("61")) {
            countryCodeLength = 2;
        }
        else if (digits.startsWith("1")) {
            countryCodeLength = 1;
        }
        else {
            countryCodeLength = 1;
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
        return "+" + countryCode + (national.isEmpty() ? "" : " " + grouped);
    }

    /**
     * Derives the owner's age band from {@code birthDate} measured against {@code registrationDate}:
     * {@code MINOR} when under 18, {@code ADULT} from 18 to 64, and {@code SENIOR} at 65 or older.
     * Returns {@code null} when no birthDate is recorded. When the registrationDate is absent (e.g.
     * legacy records) the current date is used as the reference point.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            return OwnerDto.ContactPreferenceEnum.EMAIL;
        }
        return OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's locality (region) as the plain region code. This is <em>not</em> an
     * identifier: it never carries the version-2 {@code 'V2'} tag that is mixed into the region code
     * used inside the identifiers. It is resolved directly from the owner's postcode range and then
     * the fixed city-to-region table, returning {@code UNKNOWN} when neither source resolves — never
     * from the memberId, whose region segment now carries the {@code 'V2'} tag.
     */
    default String locality(Owner owner) {
        String region = regionFromPostcode(owner);
        if (region != null) {
            return region;
        }
        return cityRegion(owner);
    }

    /**
     * Derives the owner's marketing segment, formatted {@code <TIER>_<AREA>}. TIER is
     * {@code PREMIUM} when {@link #membershipLevel(Owner) membershipLevel} is 3 or more, otherwise
     * {@code STANDARD}. AREA is {@code METRO} when the {@link #locality(Owner) locality} is a known
     * region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String region = locality(owner);
        boolean metro = "NSW".equals(region) || "VIC".equals(region) || "QLD".equals(region);
        String area = metro ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * Derives the owner's IANA timezone from the {@link #locality(Owner) locality/region}
     * via the fixed region-to-timezone table (NSW-&gt;Australia/Sydney,
     * VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane). Returns {@code null}
     * when the region is not in the table.
     */
    default String timezone(Owner owner) {
        String region = locality(owner);
        switch (region) {
            case "NSW":
                return "Australia/Sydney";
            case "VIC":
                return "Australia/Melbourne";
            case "QLD":
                return "Australia/Brisbane";
            default:
                return null;
        }
    }

    /**
     * Maps the owner's city to its region via the fixed city-to-region table
     * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD). Returns {@code UNKNOWN}
     * when the city is absent or not in the table.
     */
    default String cityRegion(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        switch (city) {
            case "Sydney":
                return "NSW";
            case "Melbourne":
                return "VIC";
            case "Brisbane":
                return "QLD";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Resolves the region from the owner's 4-digit postcode range, or {@code null}
     * when the postcode is absent, malformed, or in no known range.
     */
    default String regionFromPostcode(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return null;
        }
        int code;
        try {
            code = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        if (code >= 2000 && code <= 2099) {
            return "NSW";
        }
        if (code >= 3000 && code <= 3099) {
            return "VIC";
        }
        if (code >= 4000 && code <= 4099) {
            return "QLD";
        }
        return null;
    }

    /**
     * Returns the owner's membership points. The score starts at 0 and gains 2 when an email is
     * present, 1 when {@code namesakeCount} is 0, 2 when the household has 3 or more members, and 3
     * when tenure exceeds one elapsed fiscal year.
     */
    default Integer membershipPoints(Owner owner) {
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            points += 2;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Returns the owner's numeric membership level, derived from {@link #membershipPoints(Owner)}:
     * level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5 points, and level 4 for 6 or
     * more points. The derived level is then capped by {@link Owner#getMembershipLevelCap()} when
     * present, so a new owner's level cannot exceed one above the highest level in their household.
     */
    default Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        int level;
        if (points <= 1) {
            level = 1;
        }
        else if (points <= 3) {
            level = 2;
        }
        else if (points <= 5) {
            level = 3;
        }
        else {
            level = 4;
        }
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            level = cap;
        }
        return level;
    }

    /**
     * The owner's tenure in whole elapsed fiscal years: the number of fiscal-year boundaries
     * (1 July) crossed between {@code registrationDate} and the current date. A newly created
     * owner (registered in the current fiscal year) has zero tenure, and a missing
     * registrationDate (e.g. legacy records) is likewise treated as zero.
     */
    default long tenureFiscalYears(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long years = fiscalYearOf(java.time.LocalDate.now()) - fiscalYearOf(registrationDate);
        return Math.max(years, 0);
    }

    /**
     * The fiscal year (a four-digit calendar year) that the given date falls in. The fiscal year
     * starts on 1 July and is identified by the calendar year in which it ends, so a date on or
     * after 1 July belongs to the fiscal year of the following calendar year.
     */
    default int fiscalYearOf(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, formatted 'FY&lt;YY&gt;' where YY is the last two digits of the fiscal
     * year of the (business-day-adjusted) registrationDate captured at create time (the fiscal year
     * starts on 1 July). Derived directly from the stored registrationDate rather than parsed out of
     * the memberId, whose region segment now carries the version-2 {@code 'V2'} tag. Returns
     * {@code null} when no registrationDate is recorded.
     */
    default String fiscalYear(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(registrationDate) % 100);
    }

    /**
     * The canonical link to this owner resource: {@code /api/owners/} followed by the
     * owner's id. Returns {@code null} when the owner has no id yet.
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
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
