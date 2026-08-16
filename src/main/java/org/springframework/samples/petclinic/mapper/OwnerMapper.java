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
        AgeBandDeriver.class, TelephoneDisplayDeriver.class, MembershipPointsDeriver.class,
        FiscalYearDeriver.class, OwnerSegmentDeriver.class})
public interface OwnerMapper {

    @Mapping(target = "selfLink",
        expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
        expression = "java((owner.getTitle() != null && !owner.getTitle().isBlank()) "
            + "? owner.getTitle() + \" \" + owner.getLastName() : owner.getLastName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "checkDigit",
        expression = "java(CheckDigitDeriver.checkDigit(owner.getCustomerCode()))")
    @Mapping(target = "membershipNumber",
        expression = "java(owner.getCustomerCode() + \"-M\" "
            + "+ String.format(\"%02d\", FiscalYearDeriver.fiscalYear("
            + "owner.getRegistrationDate()) % 100))")
    @Mapping(target = "fiscalYear",
        expression = "java(FiscalYearDeriver.fiscalYearLabel(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints",
        expression = "java(MembershipPointsDeriver.membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(MembershipPointsDeriver.effectiveMembershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(LocalityDeriver.locality(owner.getCustomerCode()))")
    @Mapping(target = "timezone",
        expression = "java(LocalityDeriver.timezone("
            + "LocalityDeriver.locality(owner.getCustomerCode())))")
    @Mapping(target = "ownerSegment",
        expression = "java(OwnerSegmentDeriver.ownerSegment("
            + "MembershipPointsDeriver.effectiveMembershipLevel(owner), "
            + "LocalityDeriver.locality(owner.getCustomerCode())))")
    @Mapping(target = "contactPreference",
        expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) "
            + "? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand",
        expression = "java(AgeBandDeriver.ageBand(owner.getBirthDate(), "
            + "owner.getRegistrationDate()))")
    @Mapping(target = "identityKey",
        expression = "java(IdentityKeyDeriver.identityKey(owner.getTelephone(), "
            + "owner.getEmail(), owner.getLastName()))")
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
