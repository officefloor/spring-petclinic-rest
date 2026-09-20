package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.CityRegion;
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
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipTier", expression = "java(membershipTier(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The upper-cased first letters of the first and last name, dot-separated with a
     * trailing dot, e.g. {@code J.S.}.
     */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's membership number, formatted as {@code <customerCode>-M<YY>} where
     * {@code YY} is the last two digits of the registration date year, e.g.
     * {@code SMI-0007-M26}.
     */
    default String membershipNumber(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        return owner.getCustomerCode() + "-M" + yy;
    }

    /**
     * The owner's membership tier: {@code SILVER} when the owner has no namesakes
     * ({@code namesakeCount} is 0) and an email address is present, otherwise
     * {@code BRONZE}.
     */
    default String membershipTier(Owner owner) {
        boolean noNamesakes = Integer.valueOf(0).equals(owner.getNamesakeCount());
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return noNamesakes && hasEmail ? "SILVER" : "BRONZE";
    }

    /**
     * The canonical region derived from the owner's city via the pinned city-to-region
     * table, or {@code UNKNOWN} when the city is not in the table.
     */
    default String locality(Owner owner) {
        return CityRegion.localityOf(owner.getCity());
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
