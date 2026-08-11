package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.OwnerIdentities;

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
            expression = "java(org.springframework.samples.petclinic.util.Salutations.salutationFor(owner))")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
                    + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.util.TelephoneDisplays.displayFor(owner))")
    @Mapping(target = "membershipPoints",
            expression = "java(org.springframework.samples.petclinic.util.MembershipLevels.pointsFor(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.util.MembershipLevels.cappedLevelFor(owner))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.util.Localities.localityFor(owner))")
    @Mapping(target = "timezone",
            expression = "java(org.springframework.samples.petclinic.util.Localities.timezoneFor(owner))")
    @Mapping(target = "contactPreference",
            expression = "java(org.springframework.samples.petclinic.util.ContactPreferences.preferenceFor(owner))")
    @Mapping(target = "ageBand",
            expression = "java(org.springframework.samples.petclinic.util.AgeBands.bandFor(owner))")
    @Mapping(target = "apiVersion", expression = "java(Integer.valueOf(2))")
    @Mapping(target = "identity", expression = "java(toOwnerIdentity(owner))")
    @Mapping(target = "fiscalYear",
            expression = "java(org.springframework.samples.petclinic.util.FiscalYears.labelFor(owner))")
    @Mapping(target = "ownerSegment",
            expression = "java(org.springframework.samples.petclinic.util.OwnerSegments.segmentFor(owner))")
    @Mapping(target = "selfLink",
            expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "riskFlag",
            expression = "java(org.springframework.samples.petclinic.util.RiskFlags.flagFor(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /**
     * Groups the owner's version-2 identifiers into the nested {@code identity} object of the
     * response. The {@code memberId} and {@code householdId} are read from the persisted owner; the
     * {@code identityKey} is derived on the fly (see {@link OwnerIdentities#identityKey(Owner)}).
     */
    default OwnerIdentityDto toOwnerIdentity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(OwnerIdentities.identityKey(owner));
        return identity;
    }

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
