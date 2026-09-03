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
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerMembership.points(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerMembership.level(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's canonical URL, formatted {@code '/api/owners/<id>'} where id is the owner's id.
     * Null when the owner has no id yet.
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

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
     * The owner's fiscal year, formatted {@code FY<YY>} where YY is the last two digits of the fiscal
     * year the business-day-adjusted registrationDate falls in (fiscal year starting 1 July), e.g.
     * {@code FY27}. Null when the registration date is absent.
     */
    default String fiscalYear(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d",
                org.springframework.samples.petclinic.rest.function.owner.OwnerMembership
                        .fiscalYearOf(owner.getRegistrationDate()) % 100);
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
                org.springframework.samples.petclinic.rest.function.owner.OwnerMembership
                        .fiscalYearOf(owner.getRegistrationDate()) % 100);
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
