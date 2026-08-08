package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(formatTelephoneDisplay(owner))")
    @Mapping(target = "checkDigit", expression = "java(formatCheckDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipPoints", expression = "java(formatMembershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(formatMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(formatIdentityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(formatAgeBand(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "possibleDuplicate", expression = "java(owner.getPossibleDuplicateOf() != null)")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives an owner's age band from the birth date, computed against the registration date
     * (falling back to the current date when the registration date is absent): {@code MINOR} when
     * under 18, {@code ADULT} from 18 to 64, and {@code SENIOR} at 65 or older. Returns
     * {@code null} when no birth date is present, so the field is omitted from the response.
     */
    default OwnerDto.AgeBandEnum formatAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(owner.getBirthDate(), asOf).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives an owner's duplicate-detection identity key as
     * {@code '<normalizedTelephone>|<email or empty>|<householdId or empty>'} from the stored
     * (already E.164-normalized) telephone, lower-cased email and household id. A {@code null}
     * telephone, email or household id contributes the empty string in its position.
     */
    default String formatIdentityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Derives an owner's preferred contact channel: {@code EMAIL} when an email address is present
     * (non-null and non-blank), otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives an owner's locality (region) from the customer-code identity: it is the {@code REGION}
     * segment of the {@code '<REGION>-<HASH8>'} customer code (the part before the first {@code '-'}).
     * When the customer code is absent it falls back to resolving the region directly, preferring the
     * postcode range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) and then the fixed city-to-region
     * table ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}), yielding
     * {@code "UNKNOWN"} when neither resolves.
     */
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        String fromPostcode = regionForPostcode(owner.getPostcode());
        if (fromPostcode != null) {
            return fromPostcode;
        }
        return switch (owner.getCity() == null ? "" : owner.getCity()) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Resolves a region from a 4-digit postcode by inclusive range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099). Returns {@code null} when the postcode is absent, not exactly 4 digits, or in
     * no known range, signalling that the caller should fall back to the city-to-region table.
     */
    private String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        if (value >= 2000 && value <= 2099) {
            return "NSW";
        }
        if (value >= 3000 && value <= 3099) {
            return "VIC";
        }
        if (value >= 4000 && value <= 4099) {
            return "QLD";
        }
        return null;
    }

    /**
     * Computes an owner's membership points, starting at 0: plus 2 when an email is present, plus 1
     * when the owner's namesake count is 0, plus 2 for a household of 3 or more, plus 3 when the
     * owner's tenure exceeds 365 days.
     */
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (unique) {
            points += 1;
        }
        boolean largeHousehold = owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = owner.getRegistrationDate() != null
            && owner.getRegistrationDate().plusDays(365).isBefore(LocalDate.now());
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives an owner's membership level, a number from 1 to 4, from the owner's membership points:
     * 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points.
     */
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = formatMembershipPoints(owner);
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
     * Computes an owner's check digit: a single Luhn check digit (0-9) over the digits contained in the
     * customer code. Returns {@code null} when the customer code is absent.
     */
    default Integer formatCheckDigit(Owner owner) {
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
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where YY is the last two
     * digits of the registration-date year (e.g. {@code "NSW-1A2B3C4D-M26"}). Returns {@code null} when the
     * customer code or registration date is absent.
     */
    default String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Formats an owner's stored E.164 telephone for humans as the country code, a space, and the
     * national digits grouped in threes (e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}).
     * The raw {@code telephone} value stays in E.164 form. Returns the stored value unchanged when it
     * is {@code null} or not a well-formed E.164 string (a {@code '+'} followed by digits).
     */
    default String formatTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.matches("\\+[0-9]+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength = countryCodeLength(digits);
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            grouped.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return grouped.toString();
    }

    /**
     * Determines the length of the country-code segment of an E.164 number's digits (its digits
     * without the leading {@code '+'}): 1 for the NANP ({@code '+1'}) and 2 otherwise (e.g. Australia's
     * {@code '+61'}), mirroring the country codes the application normalises to.
     */
    private int countryCodeLength(String digits) {
        return digits.startsWith("1") ? 1 : 2;
    }

    /**
     * Formats an owner's display name as {@code "LastName, FirstName"} from the stored names.
     */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private String initial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + ".";
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
