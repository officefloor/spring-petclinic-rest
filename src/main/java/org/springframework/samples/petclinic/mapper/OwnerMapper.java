package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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

    /** Base path of the owner resource; the self link is this plus the owner id. */
    String OWNER_RESOURCE_PATH = "/api/owners/";

    /** Build an owner's self link from its id, e.g. {@code "/api/owners/1"}. */
    static String selfLink(Integer ownerId) {
        return ownerId == null ? null : OWNER_RESOURCE_PATH + ownerId;
    }

    OwnerDto toOwnerDto(Owner owner);

    /**
     * Populate the derived {@code selfLink} in the DTO (web) layer, keeping the owner
     * URL scheme out of the domain entity. Fires for every {@link OwnerDto} mapping,
     * including the collection and page variants that delegate to {@link #toOwnerDto}.
     */
    @AfterMapping
    default void fillSelfLink(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setSelfLink(selfLink(ownerDto.getId()));
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
