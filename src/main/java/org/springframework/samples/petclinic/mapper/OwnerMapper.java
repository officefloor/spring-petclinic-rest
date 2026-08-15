package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.function.common.AgeBands;
import org.springframework.samples.petclinic.rest.function.common.CheckDigits;
import org.springframework.samples.petclinic.rest.function.common.ContactPreferences;
import org.springframework.samples.petclinic.rest.function.common.CustomerCodes;
import org.springframework.samples.petclinic.rest.function.common.FiscalYears;
import org.springframework.samples.petclinic.rest.function.common.IdentityKeys;
import org.springframework.samples.petclinic.rest.function.common.Membership;
import org.springframework.samples.petclinic.rest.function.common.Telephones;
import org.springframework.samples.petclinic.rest.function.common.Timezones;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class,
        imports = { AgeBands.class, CheckDigits.class, ContactPreferences.class, CustomerCodes.class,
                FiscalYears.class, IdentityKeys.class, Membership.class, Telephones.class, Timezones.class })
public interface OwnerMapper {

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
            expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() "
                    + "? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
                    + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "checkDigit",
            expression = "java(CheckDigits.luhnOf(owner.getCustomerCode()))")
    @Mapping(target = "membershipPoints",
            expression = "java(Membership.pointsOf(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(owner.getMembershipLevel() != null "
                    + "? owner.getMembershipLevel() : Membership.levelOf(owner))")
    @Mapping(target = "fiscalYear",
            expression = "java(FiscalYears.labelOf(owner))")
    @Mapping(target = "locality",
            expression = "java(CustomerCodes.localityOf(owner))")
    @Mapping(target = "timezone",
            expression = "java(Timezones.ofOwner(owner))")
    @Mapping(target = "contactPreference",
            expression = "java(ContactPreferences.of(owner))")
    @Mapping(target = "identityKey",
            expression = "java(IdentityKeys.of(owner))")
    @Mapping(target = "ageBand",
            expression = "java(AgeBands.of(owner))")
    @Mapping(target = "telephoneDisplay",
            expression = "java(Telephones.displayOf(owner))")
    @Mapping(target = "selfLink",
            expression = "java(\"/api/owners/\" + owner.getId())")
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
