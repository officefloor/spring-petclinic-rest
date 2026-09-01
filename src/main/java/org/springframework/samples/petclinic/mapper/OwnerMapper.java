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

    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() == null || owner.getRegistrationDate() == null ? null : owner.getCustomerCode() + \"-M\" + org.springframework.samples.petclinic.rest.function.owner.FiscalYear.twoDigit(owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear", expression = "java(owner.getRegistrationDate() == null ? null : org.springframework.samples.petclinic.rest.function.owner.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerMembershipLevel.points(owner))")
    @Mapping(target = "membershipLevel", expression = "java(owner.getMembershipLevel() != null ? owner.getMembershipLevel() : org.springframework.samples.petclinic.rest.function.owner.OwnerMembershipLevel.of(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerLocality.region(owner))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerTimezone.of(owner))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner))")
    @Mapping(target = "checkDigit", expression = "java(org.springframework.samples.petclinic.rest.function.owner.CustomerCodeCheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerAgeBand.of(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneDisplay.of(owner.getTelephone()))")
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
