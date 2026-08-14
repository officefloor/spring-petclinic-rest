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
import org.springframework.samples.petclinic.rest.function.owner.IdentityKey;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink",
            expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
            expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "fiscalYear",
            expression = "java(org.springframework.samples.petclinic.mapper.FiscalYear.label(owner))")
    @Mapping(target = "membershipPoints",
            expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevel.points(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevel.effective(owner))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.mapper.MemberId.locality(owner))")
    @Mapping(target = "timezone",
            expression = "java(org.springframework.samples.petclinic.mapper.CityRegion.timezone(org.springframework.samples.petclinic.mapper.MemberId.locality(owner)))")
    @Mapping(target = "contactPreference",
            expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.EMAIL : org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "apiVersion", constant = "2")
    @Mapping(target = "identity", expression = "java(ownerIdentity(owner))")
    @Mapping(target = "ageBand",
            expression = "java(org.springframework.samples.petclinic.mapper.AgeBand.of(owner))")
    @Mapping(target = "ownerSegment",
            expression = "java(org.springframework.samples.petclinic.mapper.OwnerSegment.of(owner))")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.E164Telephone.display(owner.getTelephone()))")
    @Mapping(target = "riskFlag",
            expression = "java(org.springframework.samples.petclinic.mapper.RiskFlag.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /**
     * Builds the nested version-2 {@code identity} object grouping the owner's memberId, identityKey
     * and householdId. The identityKey is derived on read (see {@link IdentityKey#of(Owner)}); the
     * memberId and householdId were assigned and stored during creation.
     */
    default OwnerIdentityDto ownerIdentity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(IdentityKey.of(owner));
        identity.setHouseholdId(owner.getHouseholdId());
        return identity;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "memberId", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "membershipLevel", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "deleted", ignore = true)
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
