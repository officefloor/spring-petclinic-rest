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

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class,
    imports = {OwnerLocality.class, AgeBand.class, TelephoneDisplay.class,
        FiscalYear.class, IdentityKey.class, OwnerSegment.class, RiskFlag.class})
public interface OwnerMapper {

    @Mapping(target = "selfLink",
        expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
        expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName()"
            + " : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "telephoneDisplay",
        expression = "java(TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "locality",
        expression = "java(OwnerLocality.fromMemberId(owner.getMemberId()))")
    @Mapping(target = "timezone",
        expression = "java(OwnerLocality.timezoneForRegion(OwnerLocality.fromMemberId(owner.getMemberId())))")
    @Mapping(target = "ownerSegment",
        expression = "java(OwnerSegment.of(owner.getMembershipLevel(), "
            + "OwnerLocality.fromMemberId(owner.getMemberId())))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand",
        expression = "java(AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear",
        expression = "java(FiscalYear.of(owner.getRegistrationDate()))")
    @Mapping(target = "identity",
        expression = "java(toOwnerIdentity(owner))")
    @Mapping(target = "apiVersion",
        constant = "2")
    @Mapping(target = "riskFlag",
        expression = "java(RiskFlag.of(owner.getPossibleDuplicate(), owner.getEmail(), "
            + "owner.getCapacityWarning()))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's version-2 identifiers into the nested {@code identity} object of the owner
     * representation: the {@code memberId} and {@code householdId} assigned at creation and the
     * {@code identityKey} derived at read time via {@link IdentityKey#of}. Each identifier already
     * carries the fixed {@code 'V2'} version tag mixed into its derivation, so none reproduces a
     * version-1 value; the tag never leaks into the user-facing {@code locality}, {@code timezone} or
     * {@code ownerSegment}.
     */
    default OwnerIdentityDto toOwnerIdentity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(
            IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName()));
        return identity;
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "memberId", ignore = true)
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
