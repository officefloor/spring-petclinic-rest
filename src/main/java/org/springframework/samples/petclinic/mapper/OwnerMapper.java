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
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(toMembershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(Boolean.TRUE.equals(owner.getBulkSignupWarning()))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The region of the owner's city from {@link #CITY_REGIONS}, or 'UNKNOWN' when the city is not listed.
     */
    default String toLocality(Owner owner) {
        return owner.getCity() == null ? "UNKNOWN" : CITY_REGIONS.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * 'EMAIL' when the owner has an email address, otherwise 'PHONE'.
     */
    default String toContactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    /** The highest membership level assigned on create; level 4 is reserved for tenure. */
    int MAX_CREATE_MEMBERSHIP_LEVEL = 3;

    /**
     * Starts at 1, adds 1 when an email is present and 1 when the owner has no namesakes, capped at
     * {@link #MAX_CREATE_MEMBERSHIP_LEVEL}.
     */
    default Integer toMembershipLevel(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX_CREATE_MEMBERSHIP_LEVEL);
    }

    /**
     * Builds the membership number '&lt;customerCode&gt;-M&lt;YY&gt;', where YY is the last two digits of
     * the registration date's year.
     */
    default String toMembershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            Math.floorMod(owner.getRegistrationDate().getYear(), 100));
    }

    default String toInitials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private static String initial(String name) {
        return name == null || name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
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
