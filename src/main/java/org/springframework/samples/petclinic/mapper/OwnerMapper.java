package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class, imports = {MembershipLevel.class, IdentityVersion.class})
public interface OwnerMapper {

    @Mapping(target = "selfLink",
            expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
            expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
            expression = "java(TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "apiVersion",
            expression = "java(IdentityVersion.API_VERSION)")
    @Mapping(target = "identity",
            expression = "java(toOwnerIdentityDto(owner))")
    @Mapping(target = "fiscalYear",
            expression = "java(MemberId.fiscalYear(owner.getMemberId()))")
    @Mapping(target = "membershipPoints",
            expression = "java(MembershipLevel.points(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(MembershipLevel.of(owner))")
    @Mapping(target = "locality",
            expression = "java(Locality.of(owner.getMemberId()))")
    @Mapping(target = "timezone",
            expression = "java(Timezone.of(Locality.of(owner.getMemberId())))")
    @Mapping(target = "contactPreference",
            expression = "java(ContactPreference.of(owner.getEmail()))")
    @Mapping(target = "ageBand",
            expression = "java(AgeBand.of(owner))")
    @Mapping(target = "ownerSegment",
            expression = "java(OwnerSegment.of(owner))")
    @Mapping(target = "riskFlag",
            expression = "java(RiskFlag.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's three version-2 identifiers under the nested {@code identity} object of the
     * response. Each identifier is derived on the entity (mixing in the fixed 'V2' version tag).
     */
    default OwnerIdentityDto toOwnerIdentityDto(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(owner.getIdentityKey());
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
