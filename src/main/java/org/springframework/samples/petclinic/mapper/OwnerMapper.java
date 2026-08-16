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

    @Mapping(target = "selfLink",
        expression = "java(owner == null || owner.getId() == null ? null : "
            + "\"/api/owners/\" + owner.getId())")
    @Mapping(target = "salutation",
        expression = "java(owner == null ? null : "
            + "(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() "
            + ": owner.getTitle() + \" \" + owner.getLastName()))")
    @Mapping(target = "displayName",
        expression = "java(owner == null ? null : owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner == null ? null : "
            + "Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.TelephoneFormats.telephoneDisplay("
            + "owner.getTelephone()))")
    @Mapping(target = "checkDigit",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.CheckDigits.luhn("
            + "owner.getCustomerCode()))")
    @Mapping(target = "householdId",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.Households.householdId("
            + "owner.getLastName(), owner.getPostcode()))")
    @Mapping(target = "identityKey",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.Households.identityKey("
            + "owner.getTelephone(), owner.getEmail(), "
            + "org.springframework.samples.petclinic.util.Households.householdId("
            + "owner.getLastName(), owner.getPostcode())))")
    @Mapping(target = "fiscalYear",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.FiscalYears.fiscalYearLabel("
            + "owner.getRegistrationDate()))")
    @Mapping(target = "membershipNumber",
        expression = "java(owner == null || owner.getCustomerCode() == null "
            + "|| owner.getRegistrationDate() == null ? null : "
            + "owner.getCustomerCode() + \"-M\" "
            + "+ String.format(\"%02d\", "
            + "org.springframework.samples.petclinic.util.FiscalYears.fiscalYear("
            + "owner.getRegistrationDate()) % 100))")
    @Mapping(target = "membershipPoints",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.Memberships.membershipPoints("
            + "owner.getEmail(), owner.getNamesakeCount(), owner.getHouseholdSize(), "
            + "owner.getRegistrationDate()))")
    @Mapping(target = "membershipLevel",
        expression = "java(owner == null ? null : "
            + "(owner.getMembershipLevel() != null ? owner.getMembershipLevel() : "
            + "org.springframework.samples.petclinic.util.Memberships.membershipLevel("
            + "owner.getEmail(), owner.getNamesakeCount(), owner.getHouseholdSize(), "
            + "owner.getRegistrationDate())))")
    @Mapping(target = "locality",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.Localities.region("
            + "owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "timezone",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.Localities.timezone("
            + "owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner == null ? null : "
            + "(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\"))")
    @Mapping(target = "ageBand",
        expression = "java(owner == null ? null : "
            + "org.springframework.samples.petclinic.util.AgeBands.ageBand("
            + "owner.getBirthDate(), owner.getRegistrationDate()))")
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
