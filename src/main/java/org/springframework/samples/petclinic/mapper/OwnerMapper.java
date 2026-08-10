package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    /** Fixed city-to-region table used to derive an owner's region. */
    java.util.Map<String, String> CITY_REGION =
        java.util.Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Fixed region-to-timezone table (IANA names) used to derive an owner's timezone. */
    java.util.Map<String, String> REGION_TIMEZONE =
        java.util.Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "householdId", expression = "java(householdId(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's salutation: the supplied title, a single space and the last name when a
     * title was supplied (e.g. 'DR Who'), or just the last name when no title (null or blank)
     * was given.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * The owner's age band, derived from the supplied birthDate as at the
     * registrationDate: 'MINOR' when under 18, 'ADULT' when 18-64, and 'SENIOR' when
     * 65 or older. Returns {@code null} when no birthDate (or registrationDate) is
     * available, so the field is omitted for owners without a supplied birth date.
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
     * The owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's region, read from the {@code <REGION>-<HASH8>} customer code assigned on
     * creation: the segment before the first '-'. Returns 'UNKNOWN' when no customer code has
     * been assigned (e.g. legacy owners predating the region-and-hash identity).
     */
    default String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return "UNKNOWN";
    }

    /**
     * The owner's IANA timezone, derived from its locality/region via the fixed
     * region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
     * QLD -> Australia/Brisbane). Returns {@code null} when the region is not one of
     * these, so the field is omitted for owners whose region is unknown.
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * The owner's membership points. Starts at 0; gains 2 when an email address is present,
     * 1 when the owner has no namesakes (namesakeCount is 0), 2 for a household of 3 or more
     * (householdSize is 3 or greater), and 3 when the owner's tenure exceeds 365 days (more
     * than 365 whole days between the registrationDate and today). Because a newly created
     * owner has zero tenure, the tenure points are only earned once more than a year has
     * passed since registration.
     */
    default Integer membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
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
            && java.time.temporal.ChronoUnit.DAYS.between(owner.getRegistrationDate(),
                java.time.LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, derived from {@link #membershipPoints(Owner)}:
     * level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5 points, and level 4
     * for 6 or more points.
     */
    default Integer membershipLevel(Owner owner) {
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
     * A single Luhn check digit (0-9) computed over the digits contained in the owner's
     * customerCode. Returns {@code null} when the customerCode has not been assigned.
     */
    default Integer checkDigit(Owner owner) {
        if (owner.getCustomerCode() == null) {
            return null;
        }
        String s = owner.getCustomerCode();
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
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
     * The owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY
     * is the last two digits of the registrationDate year (e.g. 'NSW-1A2B3C4D-M26').
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Known E.164 country calling codes used to split the stored telephone into its
     * country code and national number for display. Longer codes are matched first so a
     * '+61' number is not mistaken for a '+6...' one.
     */
    java.util.List<String> COUNTRY_CODES = java.util.List.of("61", "1");

    /**
     * The stored E.164 telephone formatted for humans: the country code, a space, and the
     * national digits grouped in threes (e.g. '+61412345678' becomes '+61 412 345 678').
     * Returns the stored value unchanged when it is {@code null} or not in the expected
     * '+'-prefixed all-digits E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            return telephone;
        }
        String countryCode = COUNTRY_CODES.stream()
            .filter(digits::startsWith)
            .findFirst()
            .orElse(digits.substring(0, 1));
        String national = digits.substring(countryCode.length());
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            if (i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(national.charAt(i));
        }
        return sb.toString();
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot, e.g. 'J.S.'.
     */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * A stable identifier for the owner's household, derived deterministically from the
     * normalized last name and the postcode. Every owner sharing the same last name
     * (trimmed, lower-cased, whitespace collapsed) and postcode therefore receives the
     * same identifier: the first twelve upper-case hex characters of the SHA-256 of
     * '&lt;normalizedLastName&gt;|&lt;postcode&gt;'. A {@code null} postcode contributes
     * the empty string.
     */
    default String householdId(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = normalizeHouseholdKey(owner.getLastName()) + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The owner's derived identity key: the single value onto which all duplicate
     * detection is consolidated, formatted
     * '&lt;normalizedTelephone&gt;|&lt;email or empty&gt;|&lt;householdId&gt;'.
     * The telephone and email are the owner's stored (already normalized) values;
     * a {@code null} email contributes the empty string. Two owners are duplicates
     * only when their whole identity keys are equal.
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        return telephone + "|" + email + "|" + householdId(owner);
    }

    private static String normalizeHouseholdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
