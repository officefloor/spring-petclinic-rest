package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.IdentityDto;
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

    @Mapping(target = "selfLink",
        expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "fiscalYear",
        expression = "java(owner.getFiscalYear())")
    @Mapping(target = "locality",
        expression = "java(owner.getRegion())")
    @Mapping(target = "timezone",
        expression = "java(owner.getTimezone())")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneE164.display(owner.getTelephone()))")
    @Mapping(target = "apiVersion", constant = "2")
    @Mapping(target = "identity", expression = "java(toIdentityDto(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "riskFlag", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the version-2 identifiers into the nested {@code identity} object of the
     * response: the {@code memberId}, {@code identityKey} and {@code householdId} derived
     * on the owner.
     */
    default IdentityDto toIdentityDto(Owner owner) {
        IdentityDto identity = new IdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(owner.getIdentityKey());
        identity.setHouseholdId(owner.getHouseholdId());
        return identity;
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
