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
@Mapper(uses = PetMapper.class,
    imports = {OwnerLocality.class, CustomerCodeCheckDigit.class, AgeBand.class, TelephoneDisplay.class})
public interface OwnerMapper {

    @Mapping(target = "checkDigit",
        expression = "java(CustomerCodeCheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "telephoneDisplay",
        expression = "java(TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "locality",
        expression = "java(OwnerLocality.fromCustomerCode(owner.getCustomerCode()))")
    @Mapping(target = "timezone",
        expression = "java(OwnerLocality.timezoneForRegion(OwnerLocality.fromCustomerCode(owner.getCustomerCode())))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand",
        expression = "java(AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "identityKey",
        expression = "java((owner.getTelephone() == null ? \"\" : owner.getTelephone()) + \"|\""
            + " + (owner.getEmail() == null ? \"\" : owner.getEmail()) + \"|\""
            + " + (owner.getHouseholdId() == null ? \"\" : owner.getHouseholdId()))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
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
