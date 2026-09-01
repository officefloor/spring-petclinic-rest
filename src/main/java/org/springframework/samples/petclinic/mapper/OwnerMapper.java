package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class, imports = {MembershipLevels.class, FiscalYear.class,
    org.springframework.samples.petclinic.model.CustomerCode.class,
    org.springframework.samples.petclinic.model.IdentityKeys.class})
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "memberId", expression = "java(owner.getCustomerCode())")
    @Mapping(target = "fiscalYear", expression = "java(FiscalYear.labelOf(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints", expression = "java(MembershipLevels.pointsOf(owner))")
    @Mapping(target = "membershipLevel", expression = "java(owner.getMembershipLevelCap() == null ? MembershipLevels.levelOf(owner) : Math.min(MembershipLevels.levelOf(owner), owner.getMembershipLevelCap()))")
    @Mapping(target = "locality", expression = "java(CustomerCode.region(owner))")
    @Mapping(target = "timezone", expression = "java(TimeZones.zoneOf(CustomerCode.region(owner)))")
    @Mapping(target = "contactPreference", expression = "java(ContactPreferences.preferenceOf(owner))")
    @Mapping(target = "ageBand", expression = "java(AgeBands.bandOf(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(TelephoneDisplays.format(owner.getTelephone()))")
    @Mapping(target = "identityKey", expression = "java(IdentityKeys.of(owner))")
    @Mapping(target = "ownerSegment", expression = "java(OwnerSegments.of(owner))")
    @Mapping(target = "riskFlag", expression = "java(RiskFlags.of(owner))")
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
