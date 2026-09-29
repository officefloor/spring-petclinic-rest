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

    @Mapping(target = "displayName", expression = "java(toDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "membershipTier", expression = "java(toMembershipTier(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** City -> canonical region; anything not listed derives locality 'UNKNOWN'. */
    Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * The owner's locality, derived from the city using a fixed city-to-region table
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), or 'UNKNOWN' when the city is not
     * in the table.
     */
    default String toLocality(Owner owner) {
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * The owner's membership tier: 'SILVER' when namesakeCount is 0 and an email is
     * present, otherwise 'BRONZE'.
     */
    default OwnerDto.MembershipTierEnum toMembershipTier(Owner owner) {
        Integer namesakeCount = owner.getNamesakeCount();
        String email = owner.getEmail();
        boolean hasEmail = email != null && !email.isBlank();
        return (namesakeCount != null && namesakeCount == 0 && hasEmail)
                ? OwnerDto.MembershipTierEnum.SILVER : OwnerDto.MembershipTierEnum.BRONZE;
    }

    /** The owner's name formatted as 'LastName, FirstName'. */
    default String toDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a trailing dot. */
    default String toInitials(Owner owner) {
        return owner.getFirstName().substring(0, 1).toUpperCase()
                + "." + owner.getLastName().substring(0, 1).toUpperCase() + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
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
