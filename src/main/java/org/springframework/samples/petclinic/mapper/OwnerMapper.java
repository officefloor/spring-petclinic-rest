package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipTier", expression = "java(membershipTier(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Canonical region -> city table, used to derive an owner's locality.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Derives the owner's locality from the city using the fixed city-to-region table
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), returning the canonical region string
     * or {@code 'UNKNOWN'} when the city is not in the table.
     */
    default @Nullable String locality(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Returns the owner's membership tier: {@code 'SILVER'} when namesakeCount is 0 and an
     * email is present, otherwise {@code 'BRONZE'}.
     */
    default @Nullable String membershipTier(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return (noNamesakes && hasEmail) ? "SILVER" : "BRONZE";
    }

    /**
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where YY is the
     * last two digits of the registrationDate year, e.g. {@code 'MEL-SMI-0007-M26'}.
     */
    default @Nullable String membershipNumber(@Nullable Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default @Nullable String displayName(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default @Nullable String initials(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String firstName = owner.getFirstName();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(Character.toUpperCase(firstName.charAt(0))).append('.');
        }
        String lastName = owner.getLastName();
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(Character.toUpperCase(lastName.charAt(0))).append('.');
        }
        return sb.toString();
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
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
