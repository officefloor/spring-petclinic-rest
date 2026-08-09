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
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's canonical self link, derived at read time as {@code '/api/owners/'} followed by
     * the owner's id. Null when the owner or its id is absent, so the field is simply omitted then.
     */
    default String selfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * The owner's fiscal year, derived at read time from the (business-day-adjusted)
     * {@code registrationDate} and formatted {@code FY<YY>}, where YY is the last two digits of the
     * fiscal year. The fiscal year starts on 1 July, so a date on or after 1 July belongs to the
     * fiscal year labelled with the next calendar year (2026-07-01 -> {@code FY27}) and an earlier
     * date to the current calendar year (2026-06-30 -> {@code FY26}). Null when the owner or its
     * {@code registrationDate} is absent, so the field is simply omitted then.
     */
    default String fiscalYear(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(owner.getRegistrationDate()) % 100);
    }

    /**
     * The fiscal year a date falls in, as a full calendar year. The fiscal year starts on 1 July, so
     * a date in July or later belongs to the next calendar year and an earlier date to the current
     * calendar year. Kept {@code private static} so MapStruct does not treat it as a property mapping.
     */
    private static int fiscalYearOf(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's age band, derived at read time from {@code birthDate} relative to the
     * {@code registrationDate}: {@code MINOR} under 18, {@code ADULT} from 18 to 64 and
     * {@code SENIOR} at 65 or older. Null when the owner or its {@code birthDate} (or the
     * {@code registrationDate} it is measured against) is absent, so the field is simply
     * omitted for owners without a birth date.
     */
    default String ageBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int years = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * A single Luhn check digit (0-9) computed at read time over the digits contained in the
     * owner's customerCode. Non-digit characters (the hyphen and letters in '<REGION>-<HASH8>')
     * are skipped. Null when the owner or its customerCode is absent.
     */
    default Integer checkDigit(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null) {
            return null;
        }
        String code = owner.getCustomerCode();
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
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
     * The owner's preferred contact channel, derived at read time: {@code EMAIL}
     * when an email address is present, otherwise {@code PHONE}.
     */
    default String contactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        String email = owner.getEmail();
        return (email != null && !email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /** Fixed city-to-region table used to derive an owner's locality at read time. */
    Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to its inclusive 4-digit postcode range {low, high}. */
    Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Fixed region-to-timezone table, mapping each region to its IANA timezone name. */
    Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * The owner's locality, derived at read time from the region-and-hash identity: the REGION
     * prefix of the {@code customerCode} (everything before the first hyphen). That region was
     * itself derived at write time from the postcode (preferred) or city, defaulting to
     * {@code UNKNOWN}, so reading it back keeps locality and the identity in lock-step.
     *
     * <p>Owners predating the region-and-hash identity (e.g. seed data with no customerCode) fall
     * back to the historical read-time derivation: postcode preferred, then city, then
     * {@code UNKNOWN}.
     */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            return dash > 0 ? code.substring(0, dash) : code;
        }
        String byPostcode = regionFromPostcode(owner.getPostcode());
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * The owner's IANA timezone, derived at read time from the owner's locality/region via the fixed
     * region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane).
     * Null when the owner is absent or the region has no known timezone (e.g. {@code UNKNOWN}), so the
     * field is simply omitted then.
     */
    default String timezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * The region whose postcode range contains {@code postcode}, or {@code null} when the postcode is
     * absent, not four digits, or in no known range. Kept {@code private static} so MapStruct does not
     * treat it as a {@code String}-to-{@code String} property mapping method.
     */
    private static String regionFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Known E.164 country codes, longest first so '61' matches before the shorter '1', mirroring
     *  the write-time normalization in NormalizeOwnerTelephone. */
    List<String> COUNTRY_CODES = List.of("61", "1");

    /**
     * The stored E.164 {@code telephone} formatted for humans at read time: the country code, a single
     * space, then the national digits grouped in threes from the left, e.g. {@code '+61412345678'} ->
     * {@code '+61 412 345 678'}. The raw {@code telephone} is unchanged. Null when the owner is absent;
     * a value that is not a recognizable E.164 string (no leading {@code '+'}, non-digits, or no known
     * country code) is returned unchanged.
     */
    default String telephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("[0-9]+")) {
            return telephone;
        }
        String countryCode = null;
        for (String code : COUNTRY_CODES) {
            if (digits.startsWith(code)) {
                countryCode = code;
                break;
            }
        }
        if (countryCode == null) {
            return telephone;
        }
        String national = digits.substring(countryCode.length());
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + (grouped.length() == 0 ? "" : " " + grouped);
    }

    /**
     * The owner's salutation, derived at read time as the stored title and last name
     * separated by a single space (e.g. 'DR Franklin'), or just the last name when no
     * title was supplied (null or blank).
     */
    default String salutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /** Formats the owner's stored names as 'LastName, FirstName'. */
    default String displayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * The owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot, e.g. 'J.S.'.
     */
    default String initials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "membershipPoints", ignore = true)
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
