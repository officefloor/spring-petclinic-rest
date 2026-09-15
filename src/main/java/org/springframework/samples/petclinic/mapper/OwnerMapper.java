package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.CustomerCodeGenerator;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.samples.petclinic.util.LuhnCheckDigit;
import org.springframework.samples.petclinic.util.MembershipLevelCalculator;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "checkDigit", expression = "java(computeCheckDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(computeMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(formatIdentityKey(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Builds an owner's display name from the stored names, formatted as
     * {@code "LastName, FirstName"}.
     */
    default String formatDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Builds an owner's initials from the stored names as the upper-cased first
     * letters of the first and last name, dot-separated with a trailing dot,
     * e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Builds an owner's membership number from its stored fields, formatted
     * {@code '<customerCode>-M<YY>'} where {@code YY} is the last two digits of
     * the registration date's year, e.g. {@code "NSW-3F2A9C1E-M26"}. Returns
     * {@code null} when either the customer code or registration date is absent.
     */
    default String formatMembershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Computes an owner's check digit from its stored customer code, delegating to
     * {@link LuhnCheckDigit}. Returns {@code null} when the customer code is absent.
     */
    default Integer computeCheckDigit(Owner owner) {
        if (owner.getCustomerCode() == null) {
            return null;
        }
        return LuhnCheckDigit.of(owner.getCustomerCode());
    }

    /**
     * Computes an owner's numeric membership level from its stored fields, delegating to
     * {@link MembershipLevelCalculator}.
     */
    default int computeMembershipLevel(Owner owner) {
        return MembershipLevelCalculator.levelOf(owner);
    }

    /**
     * Derives an owner's locality (canonical region) from the region segment embedded in its
     * customer code, yielding {@code "UNKNOWN"} when the code is absent or carries no region.
     */
    default String formatLocality(Owner owner) {
        return CustomerCodeGenerator.regionOf(owner.getCustomerCode());
    }

    /**
     * Derives an owner's preferred contact channel from its stored fields:
     * {@code "EMAIL"} when an email is present, otherwise {@code "PHONE"}.
     */
    default String formatContactPreference(Owner owner) {
        return owner.hasEmail() ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's identity key from its stored fields, delegating to {@link IdentityKey}.
     * This is the same key used to detect duplicate owners on create.
     */
    default String formatIdentityKey(Owner owner) {
        return IdentityKey.of(owner);
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "householdId", ignore = true)
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
