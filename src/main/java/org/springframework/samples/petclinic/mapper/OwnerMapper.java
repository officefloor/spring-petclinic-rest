package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.E164PhoneNumbers;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
            + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "membershipPoints",
        expression = "java(toMembershipPoints(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(toLocality(owner))")
    @Mapping(target = "timezone",
        expression = "java(toTimezone(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(toContactPreference(owner))")
    @Mapping(target = "identityKey",
        expression = "java(toIdentityKey(owner))")
    @Mapping(target = "checkDigit",
        expression = "java(toCheckDigit(owner))")
    @Mapping(target = "ageBand",
        expression = "java(toAgeBand(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(toTelephoneDisplay(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derive an owner's age band from the {@code birthDate} relative to the {@code registrationDate}:
     * 'MINOR' when under 18, 'ADULT' when 18 to 64, and 'SENIOR' when 65 or older. Returns
     * {@code null} when no birth date was supplied (the band is then absent from the DTO). When the
     * registration date is missing (e.g. legacy records) the current date is used as the reference.
     */
    default OwnerDto.AgeBandEnum toAgeBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Format an owner's stored E.164 {@code telephone} for humans: the country code, a space, then the
     * national digits grouped in threes (e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}).
     * The raw {@code telephone} property is left untouched in E.164 form. Returns the value unchanged
     * when it is {@code null} or not in {@code '+'}-prefixed E.164 form.
     *
     * <p>The recognized dialling codes come from {@link E164PhoneNumbers}, the same table the create
     * endpoint validates against, so the two paths cannot diverge. When a number carries no recognized
     * code, a two-digit country code is assumed (the most common length).
     */
    default String toTelephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String code = E164PhoneNumbers.countryCode(digits)
            .filter(c -> digits.length() > c.length())
            .orElse(digits.length() > 2 ? digits.substring(0, 2) : digits);
        String national = digits.substring(code.length());
        StringBuilder sb = new StringBuilder("+").append(code);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /**
     * Derive an owner's check digit: a single Luhn check digit (0-9) computed over the digits
     * contained in the {@code customerCode}. Non-digit characters (the separators) are ignored.
     */
    default Integer toCheckDigit(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return null;
        }
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
     * Derive an owner's duplicate-detection identity key: the normalized E.164 telephone, the
     * lower-cased email (empty when none), and the household id (empty when none), joined with
     * {@code '|'}. Two owners are duplicates only when their whole identity keys are equal.
     *
     * <p>This is the single source of truth for the key: it is both returned on the DTO and used by
     * the create endpoint's 409 duplicate check, so the value clients see is exactly the value that
     * governs rejection. The "email present" rule reuses {@link #hasEmail(Owner)}.
     */
    default String toIdentityKey(Owner owner) {
        String email = hasEmail(owner) ? owner.getEmail() : "";
        String household = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return owner.getTelephone() + "|" + email + "|" + household;
    }

    /**
     * Whether the owner has an email address present (non-null and non-empty). Single source of truth
     * for the "is an email present" rule shared by the derived fields below.
     */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty();
    }

    /**
     * Derive an owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String toContactPreference(Owner owner) {
        return hasEmail(owner) ? "EMAIL" : "PHONE";
    }

    /**
     * Derive an owner's membership points: start at 0; add 2 when an email address is present; add 1
     * when the owner has no namesakes (namesakeCount is 0); add 2 when the owner belongs to a
     * household of 3 or more (see {@link #hasHouseholdBonus(Owner)}); add 3 when the owner's tenure
     * exceeds 365 days (see {@link #hasTenureBonus(Owner)}). Because a newly created owner has zero
     * tenure it never earns the tenure points, so a new owner with an email and no namesakes in a
     * single-member household scores 3 points.
     */
    default Integer toMembershipPoints(Owner owner) {
        int points = 0;
        if (hasEmail(owner)) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (hasHouseholdBonus(owner)) {
            points += 2;
        }
        if (hasTenureBonus(owner)) {
            points += 3;
        }
        return points;
    }

    /**
     * Whether the owner belongs to a household of 3 or more members. The household size is the count
     * of owners sharing the owner's household id, frozen at creation (see the create endpoint).
     * Returns {@code false} when no household size is present (e.g. legacy records).
     */
    default boolean hasHouseholdBonus(Owner owner) {
        return owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3;
    }

    /**
     * Derive an owner's numeric membership level (1 to 4) from its membership points (see
     * {@link #toMembershipPoints(Owner)}): level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6
     * or more.
     */
    default Integer toMembershipLevel(Owner owner) {
        int points = toMembershipPoints(owner);
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
     * Whether the owner's tenure exceeds 365 days, i.e. more than a year has elapsed between the
     * {@code registrationDate} and the current date. A brand-new owner (registered today) has zero
     * tenure and so never qualifies. Returns {@code false} when no registration date is present.
     */
    default boolean hasTenureBonus(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > 365;
    }

    /**
     * Fixed city-to-region table used as the fall-back when deriving an owner's locality.
     */
    Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Fixed region-to-inclusive-postcode-range table used to derive an owner's locality from the
     * postcode: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. Each entry is {@code {low, high}}
     * inclusive.
     */
    Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derive an owner's locality (region) from the region-and-hash customer code: the {@code <REGION>}
     * segment of the {@code customerCode}, i.e. everything before the first {@code '-'}. Since the
     * region is baked into the customer code at creation (see the create endpoint), the locality now
     * moves with that identity rather than being recomputed from the postcode and city. When an owner
     * has no customer code yet (e.g. legacy records), fall back to deriving the region directly via
     * {@link #regionOf(Owner)}.
     */
    default String toLocality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return regionOf(owner);
    }

    /**
     * Fixed region-to-IANA-timezone table used to derive an owner's timezone: NSW -> Australia/Sydney,
     * VIC -> Australia/Melbourne, QLD -> Australia/Brisbane.
     */
    Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derive an owner's IANA timezone name from its locality/region (see {@link #toLocality(Owner)})
     * via the fixed {@link #REGION_TIMEZONE} table. Returns {@code null} when the region resolves to no
     * entry in the table (e.g. 'UNKNOWN'), so the timezone is then absent from the DTO.
     */
    default String toTimezone(Owner owner) {
        return REGION_TIMEZONE.get(toLocality(owner));
    }

    /**
     * Derive an owner's region, preferring the postcode: when the postcode is present and falls within
     * a known region's range (see {@link #REGION_POSTCODE_RANGES}) that region wins. When the postcode
     * is absent or in no known range, fall back to the fixed {@link #CITY_REGION} table. Returns
     * 'UNKNOWN' when neither the postcode nor the city resolves a region. This is the region baked into
     * the {@code <REGION>} segment of a new owner's customer code and, through it, the owner's locality.
     */
    default String regionOf(Owner owner) {
        // Prefer the postcode: the region whose range contains it wins. The logic is kept here (rather
        // than in a String -> String helper) because MapStruct would treat such a helper as an implicit
        // type conversion and wrongly apply it to other String properties (e.g. telephone).
        String postcode = owner.getPostcode();
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ignored) {
                // Non-numeric postcode: fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
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
