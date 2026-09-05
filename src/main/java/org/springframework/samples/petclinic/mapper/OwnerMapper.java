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

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(toSalutation(owner))")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(toMembershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(toCheckDigit(owner))")
    @Mapping(target = "membershipPoints", expression = "java(toMembershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "timezone", expression = "java(toTimezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(toIdentityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(toAgeBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(toTelephoneDisplay(owner))")
    @Mapping(target = "fiscalYear", expression = "java(toFiscalYear(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code fiscalYear}: the fiscal year of the business-day-adjusted
     * {@code registrationDate}, formatted {@code FY<YY>} (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.FiscalYear}). Returns null
     * when no registration date is present (e.g. unmigrated seed data).
     */
    default String toFiscalYear(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return org.springframework.samples.petclinic.rest.function.owner.FiscalYear.label(registrationDate);
    }

    /**
     * Derives the owner's {@code salutation}: the {@code title} followed by a single space and the
     * {@code lastName} when a title is present, or just the {@code lastName} when no title was
     * supplied.
     */
    default String toSalutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Derives the owner's {@code telephoneDisplay}: the stored E.164 {@code telephone} formatted
     * for humans (see {@link org.springframework.samples.petclinic.rest.function.owner.E164Telephone#display}).
     * The raw {@code telephone} is left in E.164 form.
     */
    default String toTelephoneDisplay(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.E164Telephone
                .display(owner.getTelephone());
    }

    /**
     * Derives the owner's {@code ageBand} from its {@code birthDate} measured against its
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64,
     * {@code SENIOR} at 65 or older. Returns null when either date is absent (so no band is
     * reported for owners with no supplied birth date).
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum toAgeBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.ADULT;
        }
        return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code identityKey}: the single value that consolidates all
     * duplicate detection, {@code normalizedTelephone + '|' + (email or empty) + '|' +
     * (householdId or empty)}.
     */
    default String toIdentityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner);
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email is present,
     * otherwise {@code PHONE}.
     */
    default String toContactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Derives the owner's locality (region) from its region-and-hash identity: the
     * {@code customerCode} is {@code <REGION>-<HASH8>}, so the locality is the REGION segment
     * preceding the hash. Falls back to deriving the region directly from the postcode/city
     * (see {@link org.springframework.samples.petclinic.rest.function.owner.OwnerCustomerCode})
     * only for unmigrated owners that carry no customer code.
     */
    default String toLocality(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash > 0) {
                return customerCode.substring(0, dash);
            }
        }
        return org.springframework.samples.petclinic.rest.function.owner.OwnerCustomerCode
                .region(owner.getPostcode(), owner.getCity());
    }

    /** Region -> IANA timezone, the pinned region-to-timezone table. */
    java.util.Map<String, String> REGION_TIMEZONE = java.util.Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's {@code timezone}: the IANA name for the owner's locality/region via
     * the fixed region-to-timezone table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane). Returns null when the region yields no known timezone (e.g.
     * an {@code UNKNOWN} locality).
     */
    default String toTimezone(Owner owner) {
        return REGION_TIMEZONE.get(toLocality(owner));
    }

    /**
     * Derives the owner's membership points: start at 0; add 2 when an email is present; add 1
     * when the owner has no namesakes ({@code namesakeCount} is 0); add 2 for a household of 3 or
     * more ({@code householdSize} at least 3); add 3 for tenure of at least one elapsed fiscal
     * year (the {@code registrationDate}'s fiscal year is earlier than the current fiscal year,
     * fiscal years starting 1 July). A newly created owner has zero tenure.
     */
    default Integer toMembershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (unique) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null && org.springframework.samples.petclinic.rest.function.owner.FiscalYear
                .elapsed(registrationDate, java.time.LocalDate.now()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives the owner's numeric membership level (1 to 4) from its {@code membershipPoints}:
     * level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, 4 for 6 or more.
     */
    default Integer toMembershipLevel(Owner owner) {
        int points = toMembershipPoints(owner);
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
     * Derives the owner's membership number as {@code <customerCode>-M<YY>}, where YY is the
     * last two digits of the fiscal year of the registration date (fiscal years starting 1 July;
     * e.g. {@code NSW-1A2B3C4D-M26}). Returns null when either the customer code or registration
     * date is absent (e.g. unmigrated seed data).
     */
    default String toMembershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int fiscalYear = org.springframework.samples.petclinic.rest.function.owner.FiscalYear
                .of(owner.getRegistrationDate());
        return owner.getCustomerCode() + "-M" + String.format("%02d", fiscalYear % 100);
    }

    /**
     * Derives the owner's {@code checkDigit}: the Luhn check digit (0-9) computed over the
     * digits contained in the {@code customerCode}. Returns null when the customer code is
     * absent (e.g. unmigrated seed data).
     */
    default Integer toCheckDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
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
