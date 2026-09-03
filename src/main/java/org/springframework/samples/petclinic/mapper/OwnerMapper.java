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
        expression = "java((owner.getFirstName().substring(0, 1) + \".\" + owner.getLastName().substring(0, 1) + \".\").toUpperCase())")
    @Mapping(target = "membershipNumber",
        expression = "java(owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", org.springframework.samples.petclinic.rest.controller.v1.FiscalYear.of(owner.getRegistrationDate()) % 100))")
    @Mapping(target = "fiscalYear",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.FiscalYear.label(owner))")
    @Mapping(target = "membershipPoints",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.MembershipLevel.points(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.MembershipCap.level(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.CustomerCodes.region(owner))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.RegionTimezone.of(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.OwnerSegment.of(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "checkDigit",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.CheckDigit.of(owner))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.IdentityKey.of(owner))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.AgeBand.of(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.controller.v1.TelephoneDisplay.of(owner))")
    @Mapping(target = "salutation",
        expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "addressLine1", source = "address")
    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
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
