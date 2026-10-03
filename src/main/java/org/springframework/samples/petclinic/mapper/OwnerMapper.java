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
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class, imports = OwnerIdentity.class)
public interface OwnerMapper {

    // apiVersion is the fixed representation version; the version-2 identifiers are grouped under
    // the nested identity object (see #toIdentityDto), built here at the single Owner-to-DTO
    // boundary so every response path carries them without any step mutating the entity.
    @Mapping(target = "apiVersion", constant = "2")
    @Mapping(target = "identity", expression = "java(toIdentityDto(owner))")
    // selfLink is the canonical path to the owner, the single definition of '/api/owners/' + id
    // (RespondWithOwnerCreated sources the created Location from it). Absent (null) until the
    // owner is persisted and has an id.
    @Mapping(target = "selfLink", expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Build the nested identity object carrying the owner's version-2 identifiers. The identityKey
     * is derived here (not persisted); the memberId and householdId are read from the stored owner.
     */
    default IdentityDto toIdentityDto(Owner owner) {
        IdentityDto identity = new IdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(OwnerIdentity.of(owner));
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
