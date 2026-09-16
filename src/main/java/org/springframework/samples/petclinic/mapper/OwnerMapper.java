package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.CityRegion;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipTier", expression = "java(membershipTier(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    // Not derivable from a single owner; the responder sets it from the daily registration count.
    @Mapping(target = "bulkSignupWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's locality: the canonical region derived from its city via the pinned
     * city-to-region table, or "UNKNOWN" for any city not in the table.
     */
    default String locality(Owner owner) {
        return CityRegion.localityOf(owner.getCity());
    }

    /**
     * The owner's membership tier: GOLD when their household had 3 or more members after
     * they were created; otherwise SILVER when they have no namesakes and a stored email
     * address, and BRONZE in every other case.
     */
    default OwnerDto.MembershipTierEnum membershipTier(Owner owner) {
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            return OwnerDto.MembershipTierEnum.GOLD;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return noNamesakes && hasEmail ? OwnerDto.MembershipTierEnum.SILVER
            : OwnerDto.MembershipTierEnum.BRONZE;
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot (e.g. "J.S.").
     */
    default String initials(Owner owner) {
        return firstInitial(owner.getFirstName()) + firstInitial(owner.getLastName());
    }

    private static String firstInitial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
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
