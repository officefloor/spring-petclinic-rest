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

    @Mapping(target = "selfLink", expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "salutation", expression = "java(org.springframework.samples.petclinic.model.Salutation.of(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.model.TelephoneDisplay.of(owner))")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "fiscalYear", expression = "java(org.springframework.samples.petclinic.model.FiscalYear.of(owner))")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.model.MembershipPoints.of(owner))")
    @Mapping(target = "membershipLevel", expression = "java(org.springframework.samples.petclinic.model.MembershipLevel.of(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.model.Locality.of(owner))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.model.Timezone.of(owner))")
    @Mapping(target = "contactPreference", expression = "java(org.springframework.samples.petclinic.model.ContactPreference.of(owner))")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.model.AgeBand.of(owner))")
    @Mapping(target = "apiVersion", expression = "java(org.springframework.samples.petclinic.model.IdentityVersion.API_VERSION)")
    @Mapping(target = "identity", expression = "java(toOwnerIdentityDto(owner))")
    @Mapping(target = "ownerSegment", expression = "java(org.springframework.samples.petclinic.model.OwnerSegment.of(owner))")
    @Mapping(target = "riskFlag", expression = "java(org.springframework.samples.petclinic.model.RiskFlag.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's version-2 identifiers into the nested {@code identity} object: the
     * stored {@code memberId} and {@code householdId}, plus the derived {@code identityKey}.
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto toOwnerIdentityDto(Owner owner) {
        if (owner == null) {
            return null;
        }
        org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity =
                new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(org.springframework.samples.petclinic.model.IdentityKey.of(owner));
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
