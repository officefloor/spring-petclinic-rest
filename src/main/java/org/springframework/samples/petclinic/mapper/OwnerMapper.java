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
        expression = "java(org.springframework.samples.petclinic.rest.function.common.MemberId.fiscalYearLabel(owner.getMemberId()))")
    @Mapping(target = "membershipPoints",
        expression = "java(org.springframework.samples.petclinic.rest.function.common.MembershipLevels.points(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(owner.getMembershipLevel() != null ? owner.getMembershipLevel() : org.springframework.samples.petclinic.rest.function.common.MembershipLevels.of(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.rest.function.common.Localities.localityOf(owner.getMemberId(), owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.rest.function.common.Localities.timezoneOf(owner.getMemberId(), owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() == null || owner.getEmail().isBlank() ? org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.PHONE : org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.EMAIL)")
    @Mapping(target = "apiVersion",
        expression = "java(org.springframework.samples.petclinic.rest.function.common.IdentityVersion.API_VERSION)")
    @Mapping(target = "identity", expression = "java(toOwnerIdentity(owner))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.AgeBand.of(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone.toDisplay(owner.getTelephone()))")
    @Mapping(target = "selfLink",
        expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "ownerSegment",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerSegment.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's version-2 identifiers (memberId, householdId and the derived identityKey)
     * into the nested {@code identity} object of the response.
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto toOwnerIdentity(Owner owner) {
        org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity =
            new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(
            org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey.of(owner));
        return identity;
    }

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
