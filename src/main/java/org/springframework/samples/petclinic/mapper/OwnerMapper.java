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
    @Mapping(target = "salutation",
            expression = "java((owner.getTitle() == null || owner.getTitle().isBlank()) ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
            expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.mapper.TelephoneDisplays.of(owner.getTelephone()))")
    @Mapping(target = "checkDigit",
            expression = "java(org.springframework.samples.petclinic.mapper.CheckDigits.luhn(owner.getCustomerCode()))")
    @Mapping(target = "fiscalYear",
            expression = "java(owner.getRegistrationDate() == null ? null : org.springframework.samples.petclinic.model.FiscalYears.label(owner.getRegistrationDate()))")
    @Mapping(target = "membershipNumber",
            expression = "java((owner.getCustomerCode() == null || owner.getRegistrationDate() == null) ? null : owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", org.springframework.samples.petclinic.model.FiscalYears.startYear(owner.getRegistrationDate()) % 100))")
    @Mapping(target = "membershipPoints",
            expression = "java(org.springframework.samples.petclinic.model.MembershipLevels.points(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(owner.getMembershipLevel() != null ? owner.getMembershipLevel() : org.springframework.samples.petclinic.model.MembershipLevels.of(owner))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.mapper.Localities.region(owner))")
    @Mapping(target = "timezone",
            expression = "java(org.springframework.samples.petclinic.mapper.Localities.timezone(owner))")
    @Mapping(target = "contactPreference",
            expression = "java((owner.getEmail() == null || owner.getEmail().isBlank()) ? org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.PHONE : org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.EMAIL)")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.of(owner))")
    @Mapping(target = "ageBand",
            expression = "java(org.springframework.samples.petclinic.mapper.AgeBands.of(owner))")
    @Mapping(target = "selfLink",
            expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
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
