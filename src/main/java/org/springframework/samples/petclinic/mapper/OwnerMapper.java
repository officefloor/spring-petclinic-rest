package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "riskFlag", ignore = true)
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "ageBand", expression = "java(owner.getAgeBand() == null ? null : OwnerDto.AgeBandEnum.fromValue(owner.getAgeBand()))")
    @Mapping(target = "apiVersion", expression = "java(Integer.valueOf(2))")
    @Mapping(target = "identity", expression = "java(toOwnerIdentity(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's three version-2 identifiers (memberId, identityKey and householdId) into the
     * nested {@code identity} object of the owner response.
     */
    default OwnerIdentityDto toOwnerIdentity(Owner owner) {
        if (owner == null) {
            return null;
        }
        OwnerIdentityDto identity = new OwnerIdentityDto();
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
