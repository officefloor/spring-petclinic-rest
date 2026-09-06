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
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "checkDigit",
        expression = "java(org.springframework.samples.petclinic.mapper.LuhnCheckDigit.forDigits(owner.getCustomerCode()))")
    @Mapping(target = "membershipNumber",
        expression = "java(owner.getCustomerCode() + \"-M\" "
            + "+ String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipPoints",
        expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevel.pointsForOwner(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevel.forOwner(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.forCustomerCode(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.mapper.TelephoneDisplay.forE164(owner.getTelephone()))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.mapper.AgeBand.forOwner(owner))")
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
