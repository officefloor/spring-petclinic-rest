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

    @Mapping(target = "selfLink",
        expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.of(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone.toDisplay(owner.getTelephone()))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's salutation at read time: the stored {@code title} followed by a single
     * space and the {@code lastName} when a title was supplied, otherwise just the {@code lastName}.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Derives the owner's preferred contact channel at read time: {@code EMAIL} when an email is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        return hasEmail(owner) ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's age band at read time from {@code birthDate}, computed against
     * {@code registrationDate} (falling back to the current date when none is stored):
     * {@code MINOR} when under 18, {@code ADULT} when 18-64, {@code SENIOR} when 65 or older.
     * Returns {@code null} when no birth date was supplied, so the field is absent for owners
     * created without one.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        java.time.LocalDate asOf = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(birthDate, asOf).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /** Whether the owner has a non-blank email address. */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    /**
     * Reports the owner's bulk-signup warning: {@code true} when more than 80 owners had already
     * been created on this owner's registration business day at the time it was created. Treats a
     * missing value (owners created before this rule existed) as {@code false}.
     */
    default boolean bulkSignupWarning(Owner owner) {
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Computes the owner's check digit: a single Luhn check digit (0-9) over the digits contained
     * in {@code customerCode}, derived at read time. Returns {@code null} when no customer code has
     * been assigned.
     */
    default Integer checkDigit(Owner owner) {
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

    /**
     * Derives the owner's membership points: starts at 0, adds 2 when an email is present, adds 1
     * when the owner had no namesakes at creation ({@code namesakeCount == 0}), adds 2 when the
     * owner belongs to a household of 3 or more members ({@code householdSize >= 3}, counted at
     * creation by {@code AssignHouseholdSize}), and adds 3 when the owner's tenure is at least one
     * elapsed fiscal year (fiscal years start 1 July) since its {@code registrationDate}.
     */
    default int membershipPoints(Owner owner) {
        int points = 0;
        if (hasEmail(owner)) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (tenureFiscalYears(owner) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Maps the owner's {@link #membershipPoints(Owner) membership points} to a numeric level:
     * level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more, then applies the
     * household level ceiling (see {@link #uncappedMembershipLevel(Owner)}).
     *
     * <p>When a {@code membershipLevelCap} was recorded at creation time (by
     * {@code CapMembershipLevel}, as one above the maximum level among the owner's existing
     * household members), the level is clamped down to that cap. With no cap stored (no existing
     * household member when the owner was created) the uncapped level is returned unchanged.
     */
    default int membershipLevel(Owner owner) {
        int level = uncappedMembershipLevel(owner);
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /**
     * The owner's membership level derived purely from its {@link #membershipPoints(Owner) points},
     * before the household level ceiling is applied: level 1 for 0-1 points, 2 for 2-3, 3 for 4-5,
     * and 4 for 6 or more.
     */
    default int uncappedMembershipLevel(Owner owner) {
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
     * The owner's tenure in whole elapsed fiscal years (fiscal years start 1 July): the number of
     * 1-July boundaries between its {@code registrationDate} and today, derived via
     * {@code FiscalYear} so it matches the fiscal year the membership number and {@code fiscalYear}
     * label use. Returns 0 when no registration date is stored (or it lies in the future), so an
     * owner with no recorded tenure never qualifies for the tenure-gated points.
     */
    default long tenureFiscalYears(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return org.springframework.samples.petclinic.rest.function.owner.FiscalYear
            .elapsed(registrationDate, java.time.LocalDate.now());
    }

    /**
     * The owner's {@code fiscalYear} label, {@code FY<YY>} for the fiscal year (starting 1 July)
     * that contains its business-day-adjusted {@code registrationDate}, derived via
     * {@code FiscalYear}. Returns {@code null} when no registration date is stored, so the field is
     * absent for such owners.
     */
    default String fiscalYear(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return org.springframework.samples.petclinic.rest.function.owner.FiscalYear.label(registrationDate);
    }

    /**
     * Derives the owner's locality (region) at read time from the region-and-hash
     * {@code customerCode}: its {@code REGION} prefix (e.g. {@code NSW} for {@code NSW-1A2B3C4D}),
     * so the region cannot drift from the identity every other derived value flows through. Owners
     * with no {@code customerCode} fall back to resolving the region directly. Both the parse and
     * the fallback (including the {@code UNKNOWN} sentinel) live in {@code OwnerCustomerCode}.
     */
    default String locality(Owner owner) {
        String region = org.springframework.samples.petclinic.rest.function.owner.OwnerCustomerCode
            .regionOf(owner.getCustomerCode());
        return region != null ? region
            : org.springframework.samples.petclinic.rest.function.owner.OwnerCustomerCode
                .regionFor(owner);
    }

    /**
     * Derives the owner's timezone at read time: the IANA name for the owner's
     * {@link #locality(Owner) locality} (region) from the fixed region-to-timezone table
     * (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane). Returns
     * {@code null} when the region has no known timezone (e.g. {@code UNKNOWN}), so the field is
     * absent for such owners.
     */
    default String timezone(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerLocality
            .timezoneForRegion(locality(owner));
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
