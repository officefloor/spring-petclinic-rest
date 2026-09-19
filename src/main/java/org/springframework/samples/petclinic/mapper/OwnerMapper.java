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

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Maps an {@link Owner} to its DTO and attaches the response-only bulk-signup warning, which is
     * computed per request rather than stored on the owner.
     */
    default OwnerDto toOwnerDto(Owner owner, boolean bulkSignupWarning) {
        OwnerDto ownerDto = toOwnerDto(owner);
        ownerDto.setBulkSignupWarning(bulkSignupWarning);
        return ownerDto;
    }

    /**
     * Maps an {@link Owner} to its DTO and attaches both response-only warnings, which are computed
     * per request rather than stored on the owner.
     */
    default OwnerDto toOwnerDto(Owner owner, boolean bulkSignupWarning, boolean capacityWarning) {
        OwnerDto ownerDto = toOwnerDto(owner, bulkSignupWarning);
        ownerDto.setCapacityWarning(capacityWarning);
        return ownerDto;
    }

    /**
     * Maps an {@link Owner} to its DTO and attaches all response-only flags, which are computed per
     * request rather than stored on the owner.
     */
    default OwnerDto toOwnerDto(Owner owner, boolean bulkSignupWarning, boolean capacityWarning,
            boolean riskFlag) {
        OwnerDto ownerDto = toOwnerDto(owner, bulkSignupWarning, capacityWarning);
        ownerDto.setRiskFlag(riskFlag);
        return ownerDto;
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
