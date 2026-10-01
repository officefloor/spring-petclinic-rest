package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "salutation", expression = "java(Salutation.of(owner))")
    @Mapping(target = "fiscalYear", expression = "java(org.springframework.samples.petclinic.model.FiscalYear.of(owner))")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.model.MembershipLevel.points(owner))")
    @Mapping(target = "membershipLevel", expression = "java(org.springframework.samples.petclinic.model.MembershipLevel.of(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.mapper.Regions.regionForPostcode(owner.getPostcode()))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.mapper.Regions.timezoneOf(org.springframework.samples.petclinic.mapper.Regions.regionForPostcode(owner.getPostcode())))")
    @Mapping(target = "ownerSegment", expression = "java(OwnerSegment.of(owner))")
    @Mapping(target = "contactPreference", expression = "java(ContactPreference.of(owner))")
    @Mapping(target = "ageBand", expression = "java(AgeBand.of(owner))")
    @Mapping(target = "identity", expression = "java(toOwnerIdentityDto(owner))")
    @Mapping(target = "apiVersion", expression = "java(org.springframework.samples.petclinic.model.IdentityVersion.VERSION)")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.model.OwnerTelephone.toDisplay(owner.getTelephone()))")
    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "riskFlag", expression = "java(RiskFlag.of(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Build the nested {@code identity} object of the owner response, grouping the three owner
     * identifiers — the {@code memberId}, the derived {@code identityKey} and the {@code householdId}.
     */
    default OwnerIdentityDto toOwnerIdentityDto(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(OwnerIdentity.of(owner));
        identity.setHouseholdId(owner.getHouseholdId());
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
