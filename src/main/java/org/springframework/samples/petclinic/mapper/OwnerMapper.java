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

    @Mapping(target = "addressLine1", source = "address")
    @Mapping(target = "salutation", expression = "java(org.springframework.samples.petclinic.rest.function.owner.Salutation.of(owner))")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java((owner.getFirstName().charAt(0) + \".\" + owner.getLastName().charAt(0) + \".\").toUpperCase())")
    @Mapping(target = "apiVersion", expression = "java(2)")
    @Mapping(target = "identity", expression = "java(new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto()"
            + ".memberId(owner.getCustomerCode())"
            + ".identityKey(org.springframework.samples.petclinic.rest.function.owner.IdentityKey.of(owner))"
            + ".householdId(org.springframework.samples.petclinic.rest.function.owner.HouseholdId.of(owner)))")
    @Mapping(target = "fiscalYear", expression = "java(org.springframework.samples.petclinic.rest.function.owner.FiscalYear.label(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.rest.function.owner.Locality.of(owner))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.rest.function.owner.Timezone.of(owner))")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.rest.function.owner.AgeBand.of(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneDisplay.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

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
