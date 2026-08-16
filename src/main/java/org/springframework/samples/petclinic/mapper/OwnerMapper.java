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
    imports = {LocalityDeriver.class, IdentityKeyDeriver.class, CheckDigitDeriver.class,
        AgeBandDeriver.class, TelephoneDisplayDeriver.class})
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "checkDigit",
        expression = "java(CheckDigitDeriver.checkDigit(owner.getCustomerCode()))")
    @Mapping(target = "membershipNumber",
        expression = "java(owner.getCustomerCode() + \"-M\" "
            + "+ String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipLevel",
        expression = "java(Math.min(4, 1 "
            + "+ ((owner.getEmail() != null && !owner.getEmail().isBlank()) ? 1 : 0) "
            + "+ ((owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) ? 1 : 0) "
            + "+ ((owner.getRegistrationDate() != null "
            + "&& java.time.temporal.ChronoUnit.DAYS.between(owner.getRegistrationDate(), "
            + "java.time.LocalDate.now()) > 365) ? 1 : 0)))")
    @Mapping(target = "locality",
        expression = "java(LocalityDeriver.locality(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference",
        expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) "
            + "? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand",
        expression = "java(AgeBandDeriver.ageBand(owner.getBirthDate(), "
            + "owner.getRegistrationDate()))")
    @Mapping(target = "identityKey",
        expression = "java(IdentityKeyDeriver.identityKey(owner.getTelephone(), "
            + "owner.getEmail(), owner.getHouseholdId()))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(TelephoneDisplayDeriver.telephoneDisplay(owner.getTelephone()))")
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
