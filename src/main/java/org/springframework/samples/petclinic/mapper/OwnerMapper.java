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

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "householdId",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.HouseholdId.of(owner.getLastName(), owner.getAddress()))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName(), owner.getAddress()))")
    @Mapping(target = "membershipNumber",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.MembershipNumber.of(owner.getCustomerCode(), owner.getRegistrationDate()))")
    @Mapping(target = "checkDigit",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.CheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.MembershipTier.of(owner.getNamesakeCount(), owner.getEmail()))")
    @Mapping(target = "locality",
            expression = "java(owner.getCustomerCode().substring(0, owner.getCustomerCode().indexOf('-')))")
    @Mapping(target = "contactPreference",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.ContactPreference.of(owner.getEmail()))")
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
