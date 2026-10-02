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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    /**
     * URI template for an owner resource. Single source of the owner path, shared by the
     * {@code selfLink} this mapper derives and the {@code Location} header the controller sets, so the
     * two can never drift.
     */
    String OWNER_PATH_TEMPLATE = "/api/owners/{id}";

    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the DTO's {@code selfLink} from the owner's id — the API path to the owner, kept in the
     * web layer rather than the persistence entity. Runs after every {@link Owner} to {@link OwnerDto}
     * mapping, including collection and page mappings. Left null until the owner has an id.
     */
    @AfterMapping
    default void setSelfLink(Owner owner, @MappingTarget OwnerDto ownerDto) {
        if (owner.getId() != null) {
            ownerDto.setSelfLink(UriComponentsBuilder.newInstance()
                .path(OWNER_PATH_TEMPLATE).buildAndExpand(owner.getId()).toString());
        }
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "postcode", ignore = true)
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
