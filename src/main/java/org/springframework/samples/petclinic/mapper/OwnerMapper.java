package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
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

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "toDisplayName")
    @Mapping(target = "telephoneDisplay", source = "owner", qualifiedByName = "toTelephoneDisplay")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "toInitials")
    @Mapping(target = "membershipNumber", source = "owner", qualifiedByName = "toMembershipNumber")
    @Mapping(target = "checkDigit", source = "owner", qualifiedByName = "toCheckDigit")
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "toMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "toLocality")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "toContactPreference")
    @Mapping(target = "identityKey", source = "owner", qualifiedByName = "toIdentityKey")
    @Mapping(target = "ageBand", source = "owner", qualifiedByName = "toAgeBand")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code ageBand} from its {@code birthDate}, computed against the
     * {@code registrationDate}: {@code 'MINOR'} when the owner is under 18 on the registration date,
     * {@code 'ADULT'} from 18 up to and including 64, and {@code 'SENIOR'} at 65 or over. Returns
     * {@code null} when either the birth date or the registration date is absent.
     */
    @Named("toAgeBand")
    default OwnerDto.AgeBandEnum toAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's duplicate-detection {@code identityKey}: the single key that consolidates
     * the former separate telephone, email and household checks. It is the owner's normalized
     * telephone, its email (lower-cased and trimmed, or the empty string when absent) and its
     * household identifier (or the empty string when absent), joined in that order by {@code '|'}.
     * Because the telephone is part of the key, two members of the same household with different
     * telephones have different identity keys; only an exact full-key match is a duplicate.
     */
    @Named("toIdentityKey")
    default String toIdentityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone().trim();
        String email = owner.getEmail() == null ? "" : owner.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Derives the owner's preferred contact channel: {@code 'EMAIL'} when the owner has a non-blank
     * email, otherwise {@code 'PHONE'}.
     */
    @Named("toContactPreference")
    default String toContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Fixed city-to-region table used as the fallback for deriving an owner's locality.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Fixed region-to-postcode range table: each region admits an inclusive 4-digit range
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). Used to derive an
     * owner's locality from its postcode in preference to its city.
     */
    java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derives the owner's canonical region, the {@code REGION} component of its {@code customerCode}.
     * The postcode takes precedence: when the owner has a well-formed 4-digit postcode that falls
     * within a known region's range ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD
     * 4000-4099}), that region is returned. Otherwise the region is looked up from the fixed
     * city-to-region table ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}),
     * yielding {@code 'UNKNOWN'} when the city is not in the table. Preferring the postcode returns
     * the same region for known cities but disambiguates cities that share a name.
     */
    default String toRegion(Owner owner) {
        if (owner == null) {
            return null;
        }
        String regionByPostcode = regionForPostcode(owner.getPostcode());
        if (regionByPostcode != null) {
            return regionByPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Derives the owner's locality as the {@code REGION} component of its region-and-hash
     * {@code customerCode} (the prefix before the first {@code '-'}). The locality therefore shares
     * the customer code's identity rather than being computed independently. When the customer code
     * is absent the region is derived directly from the owner's postcode and city via
     * {@link #toRegion(Owner)}.
     */
    @Named("toLocality")
    default String toLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash >= 0) {
                return customerCode.substring(0, dash);
            }
        }
        return toRegion(owner);
    }

    /**
     * Returns the region whose inclusive postcode range contains the given 4-digit postcode, or
     * {@code null} when the postcode is absent, malformed, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Formats the owner's stored names as {@code 'LastName, FirstName'}.
     */
    @Named("toDisplayName")
    default String toDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for humans: the country code, a space, then
     * the national digits grouped in threes, e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"}. The country code is a single {@code '1'} for NANP ({@code +1}) numbers
     * and two digits otherwise (covering the {@code +61} numbers the app stores). Returns the value
     * unchanged when it is absent or not a well-formed E.164 number, leaving raw {@code telephone}
     * untouched.
     */
    @Named("toTelephoneDisplay")
    default String toTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.matches("\\+[0-9]{8,15}")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
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
     * Returns the owner's initials as the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    @Named("toInitials")
    default String toInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    /**
     * Formats the owner's membership number as {@code '<customerCode>-M<YY>'}, where {@code YY} is
     * the last two digits of the {@code registrationDate} year, e.g. {@code 'NSW-1A2B3C4D-M26'}. Returns
     * {@code null} when either the customer code or the registration date is absent.
     */
    @Named("toMembershipNumber")
    default String toMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Returns the owner's numeric membership level, assigned on creation: starts at {@code 1}, plus
     * {@code 1} when a non-blank email is present, plus {@code 1} when the owner has no namesakes
     * ({@code namesakeCount} is 0), capped at {@code 3} (level 4 is reserved for tenure).
     */
    @Named("toMembershipLevel")
    default Integer toMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Computes the owner's {@code checkDigit}: a single Luhn check digit (0-9) over the digits
     * contained in the owner's {@code customerCode}. Returns {@code null} when the customer code is
     * absent.
     */
    @Named("toCheckDigit")
    default Integer toCheckDigit(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        String code = owner.getCustomerCode();
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

    private static String initial(String name) {
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
