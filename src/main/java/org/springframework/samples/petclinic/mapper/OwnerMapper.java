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
    // ownerSegment depends on the membershipLevel, so the controller populates it once the
    // level is known.
    @Mapping(target = "ownerSegment", ignore = true)
    // locality and timezone are the user-facing plain region derived directly from the postcode;
    // they must never carry the version-2 'V2' tag that appears inside the identity's region code.
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.forPostcodeOrUnknown(owner.getPostcode()))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.timezoneFromPostcode(owner.getPostcode()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.mapper.TelephoneFormat.display(owner.getTelephone()))")
    // The version-2 owner identity: memberId, householdId and identityKey grouped under a nested
    // 'identity' object, no longer exposed at the top level. apiVersion is the fixed schema version.
    @Mapping(target = "apiVersion", constant = "2")
    @Mapping(target = "identity",
        expression = "java(new org.springframework.samples.petclinic.rest.dto.IdentityDto()"
            + ".memberId(owner.getMemberId()).householdId(owner.getHouseholdId())"
            + ".identityKey(org.springframework.samples.petclinic.mapper.OwnerIdentity.identityKey("
            + "owner.getTelephone(), owner.getEmail(), owner.getLastName())))")
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
    @Mapping(target = "capacityWarning", ignore = true)
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
