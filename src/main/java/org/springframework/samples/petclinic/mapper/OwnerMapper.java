package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "householdId", expression = "java(org.springframework.samples.petclinic.util.Household.idFor(owner.getLastName(), owner.getAddress()))")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipLevel", expression = "java(Math.min(3, 1 + (owner.getEmail() != null ? 1 : 0) + (Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0)))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.util.CustomerCode.region(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.util.IdentityKey.of(owner))")
    @Mapping(target = "checkDigit", expression = "java(org.springframework.samples.petclinic.util.CheckDigit.luhn(owner.getCustomerCode()))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "registrationDate", source = "registrationDate", qualifiedByName = "defaultRegistrationDate")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /** Default a missing registration date to the server's current date, then roll
     *  the effective date forward onto a business day. */
    @Named("defaultRegistrationDate")
    default LocalDate defaultRegistrationDate(LocalDate registrationDate) {
        return org.springframework.samples.petclinic.util.BusinessDay.rollForward(
            registrationDate == null ? LocalDate.now() : registrationDate);
    }

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
