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

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipTier", expression = "java(membershipTier(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** Fixed city-to-region table used to derive an owner's locality. */
    Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * The canonical region derived from the owner's city using the fixed
     * {@link #CITY_REGION} table, or 'UNKNOWN' when the city is not listed.
     */
    default String locality(Owner owner) {
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * The owner's membership tier: 'SILVER' when namesakeCount is 0 and an email
     * address is present, otherwise 'BRONZE'.
     */
    default String membershipTier(Owner owner) {
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return uniqueName && hasEmail ? "SILVER" : "BRONZE";
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot, e.g. 'J.S.'.
     */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
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
