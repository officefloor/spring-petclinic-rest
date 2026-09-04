package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.owner.AgeBand;
import org.springframework.samples.petclinic.rest.function.owner.OwnerRegion;
import org.springframework.samples.petclinic.rest.function.owner.FiscalYear;
import org.springframework.samples.petclinic.rest.function.owner.LocalityTimezone;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey;
import org.springframework.samples.petclinic.rest.function.owner.OwnerSegment;
import org.springframework.samples.petclinic.rest.function.owner.TelephoneDisplay;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class, imports = {OwnerDto.class, OwnerIdentityKey.class, OwnerRegion.class, LocalityTimezone.class, AgeBand.class, FiscalYear.class, TelephoneDisplay.class, OwnerSegment.class})
public interface OwnerMapper {

    @Mapping(target = "selfLink",
            expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "salutation",
            expression = "java(owner.getTitle() != null && !owner.getTitle().isBlank() ? owner.getTitle() + \" \" + owner.getLastName() : owner.getLastName())")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "locality",
            expression = "java(OwnerRegion.of(owner))")
    @Mapping(target = "timezone",
            expression = "java(LocalityTimezone.of(owner))")
    @Mapping(target = "contactPreference",
            expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "identityKey",
            expression = "java(OwnerIdentityKey.of(owner))")
    @Mapping(target = "ageBand",
            expression = "java(AgeBand.of(owner))")
    @Mapping(target = "ownerSegment",
            expression = "java(OwnerSegment.of(owner))")
    @Mapping(target = "fiscalYear",
            expression = "java(FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "telephoneDisplay",
            expression = "java(TelephoneDisplay.of(owner))")
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
