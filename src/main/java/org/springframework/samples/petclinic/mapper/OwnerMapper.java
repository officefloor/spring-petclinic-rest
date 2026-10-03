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
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
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

    /** The version of the owner API representation emitted by this mapper. */
    int API_VERSION = 2;

    @Mapping(target = "apiVersion", ignore = true)
    @Mapping(target = "identity", source = "owner")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Group the owner's version-2 identifiers (memberId, identityKey and householdId) into
     * the nested {@link OwnerIdentityDto}. Each is a straight copy of the corresponding
     * {@link Owner} property, so MapStruct generates the field copies; referenced from
     * {@link #toOwnerDto} for the {@code identity} target.
     */
    OwnerIdentityDto toOwnerIdentity(Owner owner);

    /**
     * Populate the genuinely derived web-layer fields that are not a straight copy of an
     * entity property: the {@code selfLink} (keeping the owner URL scheme out of the domain
     * entity) and the {@code apiVersion}. The nested {@code identity} object is mapped
     * declaratively by {@link #toOwnerIdentity(Owner)}. Fires for every {@link OwnerDto}
     * mapping, including the collection and page variants that delegate to
     * {@link #toOwnerDto}.
     */
    @AfterMapping
    default void fillDerivedFields(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setSelfLink(selfLink(ownerDto.getId()));
        ownerDto.setApiVersion(API_VERSION);
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
