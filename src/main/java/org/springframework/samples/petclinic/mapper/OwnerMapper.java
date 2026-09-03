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

    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipPoints",
            expression = "java(org.springframework.samples.petclinic.mapper.OwnerMapper.membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.mapper.OwnerMapper.membershipLevel(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's stored E.164 {@code telephone} formatted for humans: the '+' country code, a space,
     * then the national digits grouped in threes (e.g. {@code '+61 412 345 678'}). Null when no
     * telephone is recorded.
     */
    default String telephoneDisplay(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone.toDisplay(
                owner.getTelephone());
    }

    /**
     * The owner's age band, computed from {@code birthDate} against {@code registrationDate}:
     * {@code MINOR} (under 18), {@code ADULT} (18-64) or {@code SENIOR} (65+). Null when no
     * birth date is recorded.
     */
    default String ageBand(Owner owner) {
        if (owner.getBirthDate() == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
                ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(owner.getBirthDate(), reference).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The owner's derived {@code identityKey} = normalizedTelephone + '|' + (email or empty) + '|' +
     * householdId, the single key all duplicate detection is expressed through.
     */
    default String identityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(
                owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * The owner's preferred contact channel: {@code EMAIL} when an email is present, otherwise
     * {@code PHONE}.
     */
    default String contactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's salutation: the honorific {@code title}, a single space, then the {@code lastName}
     * when a title is present, or just the {@code lastName} when no title is given.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's locality (region), read from the REGION component of the customerCode
     * ({@code <REGION>-<HASH8>}); see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerRegion#regionOf}.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerRegion.regionOf(
                owner.getCustomerCode());
    }

    /**
     * The IANA timezone name for the owner's locality (region), from the fixed region-to-timezone
     * table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane); see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerRegion#timezoneOf}.
     * Null when the region is absent or unknown.
     */
    default String timezone(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerRegion.timezoneOf(
                owner.getCustomerCode());
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits of the owner's {@code customerCode}.
     * Null when the customer code is absent.
     */
    default Integer checkDigit(Owner owner) {
        if (owner.getCustomerCode() == null) {
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
     * The fiscal year the {@code date} falls in, as an integer. The fiscal year starts on 1 July and is
     * named by the calendar year in which it ends, so 1 July 2026 - 30 June 2027 is fiscal year 2027: a
     * date in July or later belongs to the next calendar year's fiscal year, an earlier date to the
     * current calendar year's.
     */
    static int fiscalYearOf(java.time.LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, formatted {@code FY<YY>} where YY is the last two digits of the fiscal
     * year the business-day-adjusted registrationDate falls in (fiscal year starting 1 July), e.g.
     * {@code FY27}. Null when the registration date is absent.
     */
    default String fiscalYear(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(owner.getRegistrationDate()) % 100);
    }

    /**
     * The owner's membership number, formatted {@code <customerCode>-M<YY>} where YY is the last two
     * digits of the fiscal year the registrationDate falls in (fiscal year starting 1 July), e.g.
     * {@code NSW-1A2B3C4D-M27}. Null when either the customer code or the registration date is absent.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
                fiscalYearOf(owner.getRegistrationDate()) % 100);
    }

    /**
     * The owner's membership points. Starts at 0; add 2 when an email is present; add 1 when the owner's
     * name was unique on creation (namesakeCount is 0); add 2 for a household of 3 or more members; add 3
     * for tenure of one or more elapsed fiscal years (the current fiscal year differs from the fiscal
     * year the registrationDate falls in; fiscal year starting 1 July).
     */
    static int membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
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
        if (owner.getRegistrationDate() != null
                && fiscalYearOf(java.time.LocalDate.now())
                        - fiscalYearOf(owner.getRegistrationDate()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, derived by banding {@link #membershipPoints(Owner)}: level 1 for
     * 0-1 points, level 2 for 2-3, level 3 for 4-5, level 4 for 6 or more.
     */
    static int membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
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
