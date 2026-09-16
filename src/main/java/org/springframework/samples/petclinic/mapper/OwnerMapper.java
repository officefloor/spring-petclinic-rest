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
import org.springframework.samples.petclinic.util.ContactPreference;
import org.springframework.samples.petclinic.util.MembershipLevel;

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
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
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
     * The owner's preferred contact channel: "EMAIL" when an email address is present,
     * otherwise "PHONE".
     *
     * @see ContactPreference#of(Owner)
     */
    default String contactPreference(Owner owner) {
        return ContactPreference.of(owner);
    }

    /**
     * The owner's membership level from 1 to 3, assigned on creation.
     *
     * @see MembershipLevel#of(Owner)
     */
    default Integer membershipLevel(Owner owner) {
        return MembershipLevel.of(owner);
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
