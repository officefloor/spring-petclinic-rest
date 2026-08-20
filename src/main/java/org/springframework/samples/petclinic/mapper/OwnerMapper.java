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
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink",
            expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
            expression = "java((owner.getTitle() != null && !owner.getTitle().isBlank()) ? owner.getTitle() + \" \" + owner.getLastName() : owner.getLastName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipPoints",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.MembershipLevels.points(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.MembershipLevels.of(owner))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.Localities.region(owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "timezone",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.Localities.timezone(owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "ownerSegment",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.OwnerSegments.of(owner))")
    @Mapping(target = "contactPreference",
            expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) ? org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.EMAIL : org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneE164.toDisplay(owner.getTelephone()))")
    @Mapping(target = "apiVersion", expression = "java(2)")
    @Mapping(target = "identity",
            expression = "java(org.springframework.samples.petclinic.mapper.OwnerMapper.identityOf(owner))")
    @Mapping(target = "ageBand",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.AgeBands.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "riskFlag",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.RiskFlags.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Builds the version-2 {@code identity} object: the owner's derived memberId, householdId and
     * (freshly computed) identityKey, grouped together in the response.
     */
    static OwnerIdentityDto identityOf(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(OwnerIdentityKey.of(owner));
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
