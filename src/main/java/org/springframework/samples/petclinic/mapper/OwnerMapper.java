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
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey.of(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.E164Telephone.display(owner.getTelephone()))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's salutation: the {@code title} and {@code lastName} separated by a single
     * space (e.g. {@code "DR Franklin"}), or just the {@code lastName} when no title was supplied.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Derives the owner's age band from {@code birthDate}, computed against the
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} when 18-64,
     * {@code SENIOR} when 65 or older. Returns {@code null} when no birth date was supplied
     * (or the registration date is absent, e.g. seed data), so the field is simply absent.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int years = java.time.Period.between(birthDate, registrationDate).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * The fixed city-to-region table: any city not listed derives locality {@code UNKNOWN}.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * The fixed region-to-timezone table: maps a region code to its IANA timezone name. Any region
     * not listed (e.g. {@code UNKNOWN}) has no timezone.
     */
    java.util.Map<String, String> REGION_TIMEZONE = java.util.Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's IANA timezone from the {@link #locality(Owner) locality/region} via the
     * fixed {@link #REGION_TIMEZONE} table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane). Returns {@code null} when the region is not one of these, so the
     * field is simply absent.
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * Derives the owner's locality from the region-and-hash identity: the REGION prefix of the
     * {@code customerCode} (everything before the first {@code '-'}). This is the same region the
     * identity is built from ({@link org.springframework.samples.petclinic.rest.function.owner.OwnerRegion},
     * postcode range first — NSW 2000-2099, VIC 3000-3099, QLD 4000-4099 — then the city table, then
     * {@code UNKNOWN}), so the locality a client reads back always matches the code's region prefix.
     * Owners with no {@code customerCode} (e.g. seed data) fall back to deriving the region directly.
     */
    default String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return org.springframework.samples.petclinic.rest.function.owner.OwnerRegion.of(owner);
    }

    /**
     * Derives the owner's membership points. Starts at 0; adds 2 when an email address is present;
     * adds 1 when the owner has no namesakes ({@code namesakeCount} is 0); adds 2 for a household of
     * 3 or more members ({@code householdMemberCount} is 3 or greater); adds 3 when the owner's
     * tenure exceeds 365 days. A newly created owner has zero tenure, so a new owner never earns the
     * tenure points.
     */
    default int membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        boolean largeHousehold = owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3;
        if (largeHousehold) {
            points += 2;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && java.time.temporal.ChronoUnit.DAYS.between(registrationDate, java.time.LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * Maps the owner's {@link #membershipPoints(Owner) membershipPoints} to a numeric level: 1 for
     * 0-1 points, 2 for 2-3 points, 3 for 4-5 points, 4 for 6 or more points.
     */
    default int membershipLevel(Owner owner) {
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
     * Derives the owner's membership number, formatted {@code <customerCode>-M<YY>} where YY is the
     * last two digits of the registrationDate year (e.g. {@code LON-SMI-0007-M26}). Returns {@code null}
     * when either source field is absent, so owners without a customer code or registration date
     * (e.g. seed data) simply have no membership number.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int yy = Math.floorMod(owner.getRegistrationDate().getYear(), 100);
        return String.format("%s-M%02d", owner.getCustomerCode(), yy);
    }

    /**
     * Computes the owner's Luhn check digit (0-9) over the digits contained in the customerCode.
     * Returns {@code null} when the customer code is absent (e.g. seed data).
     */
    default Integer checkDigit(Owner owner) {
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
