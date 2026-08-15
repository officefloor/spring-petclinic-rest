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
    imports = {org.springframework.samples.petclinic.util.LocalityResolver.class,
        org.springframework.samples.petclinic.util.MembershipLevel.class,
        org.springframework.samples.petclinic.util.FiscalYear.class,
        org.springframework.samples.petclinic.util.TelephoneDisplay.class,
        org.springframework.samples.petclinic.util.AgeBand.class,
        org.springframework.samples.petclinic.util.ContactPreference.class,
        org.springframework.samples.petclinic.util.OwnerSegment.class,
        org.springframework.samples.petclinic.util.CheckDigit.class,
        org.springframework.samples.petclinic.util.Salutation.class,
        org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey.class})
public interface OwnerMapper {

    @Mapping(target = "salutation",
        expression = "java(Salutation.of(owner.getTitle(), owner.getLastName()))")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(TelephoneDisplay.format(owner.getTelephone()))")
    @Mapping(target = "membershipPoints",
        expression = "java(MembershipLevel.pointsOf(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(MembershipLevel.levelOf(MembershipLevel.pointsOf(owner)))")
    @Mapping(target = "fiscalYear",
        expression = "java(owner.getRegistrationDate() == null ? null "
            + ": FiscalYear.labelOf(owner.getRegistrationDate()))")
    @Mapping(target = "locality",
        expression = "java(LocalityResolver.regionOfCustomerCode(owner.getCustomerCode()))")
    @Mapping(target = "timezone",
        expression = "java(LocalityResolver.timezoneOfRegion("
            + "LocalityResolver.regionOfCustomerCode(owner.getCustomerCode())))")
    @Mapping(target = "contactPreference",
        expression = "java(ContactPreference.preferenceOf(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(OwnerSegment.of("
            + "MembershipLevel.levelOf(MembershipLevel.pointsOf(owner)), "
            + "LocalityResolver.regionOfCustomerCode(owner.getCustomerCode())))")
    @Mapping(target = "ageBand",
        expression = "java(AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "identityKey",
        expression = "java(OwnerIdentityKey.forOwner(owner))")
    @Mapping(target = "checkDigit",
        expression = "java(CheckDigit.luhnOf(owner.getCustomerCode()))")
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
