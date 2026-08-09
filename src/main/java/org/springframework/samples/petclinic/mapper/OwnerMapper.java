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

    @Mapping(target = "salutation",
        expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() "
            + ": owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    // membershipPoints & membershipLevel are populated by the controller, which can size the
    // owner's household (the household-of-three-or-more factor needs the other owners).
    @Mapping(target = "membershipPoints", ignore = true)
    @Mapping(target = "membershipLevel", ignore = true)
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.regionFromCustomerCode(owner.getCustomerCode()))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.timezoneFromCustomerCode(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.mapper.TelephoneFormat.display(owner.getTelephone()))")
    @Mapping(target = "identityKey",
        expression = "java(owner.getTelephone() + \"|\" + (owner.getEmail() == null ? \"\" : owner.getEmail()) "
            + "+ \"|\" + (owner.getHouseholdId() == null ? \"\" : owner.getHouseholdId()))")
    @Mapping(target = "checkDigit",
        expression = "java(owner.getCustomerCode() == null ? null "
            + ": org.springframework.samples.petclinic.mapper.OwnerLocality.luhn(owner.getCustomerCode()))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.mapper.AgeBand.forBirthDate("
            + "owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear",
        expression = "java(org.springframework.samples.petclinic.mapper.FiscalYear.label("
            + "owner.getRegistrationDate()))")
    @Mapping(target = "selfLink",
        expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "sharesHousehold", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
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
