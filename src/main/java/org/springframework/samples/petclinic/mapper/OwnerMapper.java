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
        expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "fiscalYear",
        expression = "java(owner.getRegistrationDate() == null ? null : org.springframework.samples.petclinic.util.FiscalYears.label(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints",
        expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevels.pointsForOwner(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevels.forOwner(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.MemberIds.localityOf(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerSegments.forOwner(owner))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.MemberIds.timezoneOf(owner))")
    @Mapping(target = "bulkSignupWarning",
        expression = "java(owner.getBulkSignupWarning() != null && owner.getBulkSignupWarning())")
    @Mapping(target = "capacityWarning",
        expression = "java(owner.getCapacityWarning() != null && owner.getCapacityWarning())")
    @Mapping(target = "possibleDuplicate",
        expression = "java(owner.getPossibleDuplicate() != null && owner.getPossibleDuplicate())")
    @Mapping(target = "riskFlag",
        expression = "java((owner.getPossibleDuplicate() != null && owner.getPossibleDuplicate()) || (owner.getCapacityWarning() != null && owner.getCapacityWarning()) || org.springframework.samples.petclinic.rest.function.owner.DisposableDomains.emailIsDisposableAdjacent(owner.getEmail()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.IdentityKeys.forOwner(owner))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.mapper.AgeBands.forOwner(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.Telephones.toDisplay(owner.getTelephone()))")
    @Mapping(target = "deleted",
        expression = "java(owner.getDeleted() != null && owner.getDeleted())")
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
