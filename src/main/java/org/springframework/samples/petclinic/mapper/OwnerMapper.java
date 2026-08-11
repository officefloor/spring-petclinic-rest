package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipPoints",
        expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(locality(owner))")
    @Mapping(target = "timezone",
        expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey",
        expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand",
        expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear",
        expression = "java(fiscalYear(owner))")
    @Mapping(target = "salutation",
        expression = "java(salutation(owner))")
    @Mapping(target = "selfLink",
        expression = "java(selfLink(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(ownerSegment(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Build the owner's canonical self link: '/api/owners/' followed by the owner's id. Returns null
     * when the owner has no id so an unsaved owner serializes cleanly.
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * The set of known regions treated as METRO when deriving an owner's segment area.
     */
    java.util.Set<String> METRO_REGIONS = java.util.Set.of("NSW", "VIC", "QLD");

    /**
     * Derive the owner's segment, formatted '&lt;TIER&gt;_&lt;AREA&gt;'. TIER is 'PREMIUM' when
     * {@link #membershipLevel(Owner)} is 3 or more, otherwise 'STANDARD'. AREA is 'METRO' when the
     * {@link #locality(Owner)} is a known region (NSW, VIC or QLD), otherwise 'REGIONAL'.
     */
    default String ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Build the owner's salutation: the honorific title followed by a single space and the last name
     * (e.g. 'DR Who') when a title is present, or just the last name when no title was supplied. A
     * blank title is treated as absent.
     */
    default String salutation(Owner owner) {
        String lastName = owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.trim().isEmpty()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Derive the owner's age band from the supplied birthDate, evaluated against the owner's
     * registrationDate: 'MINOR' when under 18, 'ADULT' when 18 to 64 inclusive, and 'SENIOR' when
     * 65 or older. Returns null when no birthDate was supplied (or no registrationDate is available)
     * so owners without a birth date serialize cleanly.
     */
    default String ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The E.164 country calling codes whose split from the national number is recognised when
     * formatting {@code telephoneDisplay} (matching the codes validated on create). The longest
     * matching code wins so a shorter code that is a prefix of another cannot mask it.
     */
    java.util.Set<String> KNOWN_COUNTRY_CODES = java.util.Set.of("1", "61");

    /**
     * Format the stored E.164 telephone for humans: the '+' and country calling code, a space, then
     * the national digits grouped in threes (e.g. '+61412345678' -&gt; '+61 412 345 678'). The
     * country code is split off using the recognised calling codes; when none matches, a single-digit
     * code is assumed so the value is still rendered as '+&lt;code&gt; &lt;grouped national&gt;'.
     * Returns the stored value unchanged when it is null or not in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("\\d+")) {
            return telephone;
        }
        String countryCode = digits.substring(0, 1);
        String national = digits.substring(1);
        for (int codeLength = Math.min(3, digits.length() - 1); codeLength >= 1; codeLength--) {
            String candidate = digits.substring(0, codeLength);
            if (KNOWN_COUNTRY_CODES.contains(candidate)) {
                countryCode = candidate;
                national = digits.substring(codeLength);
                break;
            }
        }
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
     * Derive the owner's identity key, the single value into which all duplicate detection is
     * consolidated: the full lower-case hex SHA-256 over
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Two owners are
     * duplicates only when their whole identity keys are equal. A null email or last name contributes
     * an empty segment.
     */
    default String identityKey(Owner owner) {
        return IdentityKeys.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * Determine the owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The calendar year in which the fiscal year containing {@code date} starts. The fiscal year
     * starts on 1 July, so dates in July through December map to their own calendar year while
     * dates in January through June map to the previous calendar year.
     */
    static int fiscalYearStart(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue()
            ? date.getYear() : date.getYear() - 1;
    }

    /**
     * Derive the owner's fiscal year from the (business-day-adjusted) registrationDate, formatted
     * 'FY&lt;YY&gt;' where YY is the last two digits of the fiscal year's starting calendar year. The
     * fiscal year starts on 1 July. Returns null when no registrationDate is available so owners
     * without one serialize cleanly.
     */
    default String fiscalYear(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearStart(registrationDate) % 100);
    }

    /**
     * Compute the owner's membership points. Starts at 0; add 2 when an email address is present;
     * add 1 when the owner has no namesakes (namesakeCount is 0); add 2 for a household of 3 or more
     * (householdSize); add 3 for tenure of at least one elapsed fiscal year, measured as the number
     * of fiscal-year starts (1 July) between the registrationDate and the current date. Because a
     * newly created owner has zero elapsed fiscal years, a new owner never earns the tenure points.
     */
    default Integer membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
                && fiscalYearStart(java.time.LocalDate.now())
                    - fiscalYearStart(owner.getRegistrationDate()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Determine the owner's numeric membership level, derived from membershipPoints: 1 for 0-1
     * points, 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points. The points-derived
     * level is then capped by {@code membershipLevelCap} when one was assigned at creation: a new
     * owner's level may not exceed one above the highest level among their existing household
     * members. When no cap was assigned (no existing household member), the points-derived level is
     * returned unchanged.
     */
    default Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        int level;
        if (points <= 1) {
            level = 1;
        } else if (points <= 3) {
            level = 2;
        } else if (points <= 5) {
            level = 3;
        } else {
            level = 4;
        }
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            level = cap;
        }
        return level;
    }

    /**
     * The fixed city-to-region table used to derive an owner's locality when the postcode is absent
     * or falls in no known range.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -&gt; inclusive 4-digit postcode range {low, high}: NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099.
     */
    java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derive the owner's locality (canonical region), preferring the postcode: look up the region by
     * postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only fall back to the
     * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD) when the postcode is
     * absent or in no known range. Returns 'UNKNOWN' when neither source resolves a region.
     */
    default String locality(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("\\d{4}")) {
            int value = Integer.parseInt(postcode);
            for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The fixed region-to-timezone table used to derive an owner's timezone from its locality:
     * NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane.
     */
    java.util.Map<String, String> REGION_TIMEZONE = java.util.Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derive the owner's timezone as an IANA name from its locality (canonical region) using the
     * fixed region-to-timezone table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane). Returns null when the locality is not in the table so owners with
     * an unknown region serialize cleanly.
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
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
