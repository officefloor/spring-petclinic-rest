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
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.of(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone.toDisplay(owner.getTelephone()))")
    OwnerDto toOwnerDto(Owner owner);

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
     * Derives the owner's numeric membership level: starts at 1, adds 1 when an email is present,
     * adds 1 when the owner had no namesakes at creation ({@code namesakeCount == 0}), and adds 1
     * when the owner belongs to a household (has a {@code householdId}).
     *
     * <p>Level 4 requires tenure of more than 365 days: the level is capped at 3 unless the owner's
     * tenure (days since {@code registrationDate}) exceeds 365, in which case it may reach 4. Because
     * a newly created owner has zero tenure, a new owner never exceeds level 3 — even one with an
     * email, a {@code namesakeCount} of 0 and a household (which otherwise sum to 4) lands at 3.
     */
    default int membershipLevel(Owner owner) {
        int level = 1;
        if (hasEmail(owner)) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        if (owner.getHouseholdId() != null && !owner.getHouseholdId().isBlank()) {
            level++;
        }
        int cap = tenureDays(owner) > 365 ? 4 : 3;
        return Math.min(level, cap);
    }

    /**
     * The owner's tenure in whole days: the number of days from its {@code registrationDate} to
     * today. Returns 0 when no registration date is stored (or it lies in the future), so an owner
     * with no recorded tenure never qualifies for the tenure-gated level 4.
     */
    default long tenureDays(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(registrationDate, java.time.LocalDate.now());
        return Math.max(days, 0);
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
