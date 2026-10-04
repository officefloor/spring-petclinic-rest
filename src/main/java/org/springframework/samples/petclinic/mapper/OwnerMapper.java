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
import org.springframework.samples.petclinic.rest.validation.E164Telephone;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.Regions;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    @Mapping(target = "timezone", expression = "java(formatTimezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(formatBulkSignupWarning(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(formatPossibleDuplicate(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(formatAgeBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Resolve an owner's possible-duplicate flag, coalescing a {@code null} stored value
     * (e.g. for owners created before the flag existed) to {@code false} so the response
     * always carries an explicit boolean.
     */
    default Boolean formatPossibleDuplicate(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getPossibleDuplicate());
    }

    /**
     * Derive an owner's age band from its {@code birthDate}, measured as the owner's age in whole
     * years on its {@code registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"} from 18
     * through 64, and {@code "SENIOR"} at 65 and over. Returns {@code null} when the owner, its
     * birth date or its registration date is absent, since the band cannot be derived without both
     * dates.
     */
    default String formatAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
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
     * Derive an owner's preferred contact channel from its stored contact details:
     * {@code "EMAIL"} when a (non-blank) email address is present, otherwise {@code "PHONE"}.
     */
    default String formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Resolve an owner's bulk-signup warning flag, coalescing a {@code null} stored value
     * (e.g. for owners created before the flag existed) to {@code false} so the response
     * always carries an explicit boolean.
     */
    default Boolean formatBulkSignupWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Derive an owner's locality from its region-and-hash identity: the {@code <REGION>} segment
     * of the {@code customerCode} (the part before the first hyphen), which is the region code
     * derived from the owner's postcode. For an owner that has not yet been assigned a customer
     * code, the region is derived directly from the postcode (see {@link Regions#forPostcode}),
     * yielding the same value the code would carry. Returns {@code null} only when the owner
     * itself is {@code null}.
     */
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String code = owner.getCustomerCode();
        if (code != null) {
            int hyphen = code.indexOf('-');
            return hyphen >= 0 ? code.substring(0, hyphen) : code;
        }
        return Regions.forPostcode(owner.getPostcode());
    }

    /**
     * Derive an owner's timezone as the IANA name mapped from its locality/region via a fixed
     * region-to-timezone table (see {@link Regions#timezoneForRegion}): NSW -> Australia/Sydney,
     * VIC -> Australia/Melbourne, QLD -> Australia/Brisbane. Returns {@code null} when the owner
     * is {@code null} or its region is not in the table.
     */
    default String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Regions.timezoneForRegion(formatLocality(owner));
    }

    /**
     * Compute an owner's membership points. Points start at {@code 0} and gain: {@code 2} when an
     * email address is present; {@code 1} when the owner has no namesakes (its
     * {@code namesakeCount} is {@code 0}); {@code 2} for a household of three or more members (the
     * owner plus its pets); and {@code 3} when the owner's tenure (days elapsed since its
     * {@code registrationDate}) exceeds {@code 365}. A null or absent namesake count is treated as
     * not being zero, so it contributes nothing.
     */
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        if (hasEmail) {
            points += 2;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (uniqueName) {
            points += 1;
        }
        int householdSize = 1 + (owner.getPets() == null ? 0 : owner.getPets().size());
        if (householdSize >= 3) {
            points += 2;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * Determine an owner's membership level, a number from {@code 1} to {@code 4} derived from its
     * membership points (see {@link #formatMembershipPoints(Owner)}): level {@code 1} for
     * {@code 0}-{@code 1} points, {@code 2} for {@code 2}-{@code 3}, {@code 3} for {@code 4}-{@code 5}
     * and {@code 4} for {@code 6} or more.
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
     * Compute an owner's check digit: a single Luhn check digit (0-9) over the digits
     * contained in the owner's {@code customerCode}. Non-digit characters (the region segment
     * and any hex letters of the hash) are ignored. Returns {@code null} when the owner or its
     * customer code is absent, since the digit cannot be derived without the code.
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
     * Format an owner's membership number as {@code "<customerCode>-M<YY>"}, where {@code YY}
     * is the last two digits of the registration date year (e.g. {@code "NSW-3F5A9C2E-M26"}).
     * Returns {@code null} when either the customer code or registration date is absent, since
     * the number cannot be derived without both.
     */
    default String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Format an owner's stored E.164 telephone for humans as the country code, a space, then the
     * national digits grouped in threes, e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"} (see {@link E164Telephone#toDisplay}). The raw {@code telephone}
     * is left in E.164 form. Returns {@code null} when the owner or its telephone is absent.
     */
    default String formatTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return E164Telephone.toDisplay(owner.getTelephone());
    }

    /**
     * Format an owner's name as {@code "LastName, FirstName"} from the stored names.
     */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Format an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalize a postal address for storage into its canonical form (see
     * {@link AddressNormalizer}), so it is persisted and returned the same way regardless of
     * the formatting supplied by the caller. {@code @Named} keeps this out of MapStruct's
     * automatic method selection, so it only runs where a mapping explicitly references it via
     * {@code qualifiedByName}.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalize a telephone number for storage into E.164 form (see {@link E164Telephone}).
     * The requirement that a valid E.164 number can be formed is enforced separately by
     * {@code TelephoneValidation}, so for already-validated input this returns the E.164
     * string; it is idempotent, so re-normalizing a stored value yields the same value.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return E164Telephone.normalize(telephone);
    }

    /**
     * Normalize an email address for storage by lower-casing it, so it is persisted and
     * returned in a canonical form regardless of the casing supplied by the caller.
     * Syntactic validity is enforced separately by the {@code @Email} constraint on the DTO.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase();
    }

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
