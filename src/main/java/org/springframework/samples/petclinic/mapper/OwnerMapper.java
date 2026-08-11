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
public abstract class OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.mapper.TelephoneDisplay.of(owner))")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipPoints",
        expression = "java(org.springframework.samples.petclinic.mapper.Membership.pointsOf(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(org.springframework.samples.petclinic.mapper.Membership.levelOf(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.mapper.Locality.of(owner))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.mapper.Timezone.of(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(org.springframework.samples.petclinic.mapper.ContactPreference.of(owner))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.mapper.IdentityKey.of(owner))")
    @Mapping(target = "checkDigit",
        expression = "java(org.springframework.samples.petclinic.mapper.CheckDigit.of(owner))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.mapper.AgeBand.of(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
