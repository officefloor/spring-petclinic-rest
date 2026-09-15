package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.LocalityResolver;
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
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(computeMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
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
     * the registration date's year, e.g. {@code "SMI-0007-M26"}. Returns
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
     * Computes an owner's numeric membership level from its stored fields, delegating to
     * {@link MembershipLevelCalculator}.
     */
    default int computeMembershipLevel(Owner owner) {
        return MembershipLevelCalculator.levelOf(owner);
    }

    /**
     * Derives an owner's locality (canonical region) from its stored city using the
     * fixed city-to-region table, yielding {@code "UNKNOWN"} for any unlisted city.
     */
    default String formatLocality(Owner owner) {
        return LocalityResolver.localityOf(owner.getCity());
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
