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
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "salutation",
        expression = "java(owner == null ? null : owner.getSalutation())")
    @Mapping(target = "displayName",
        expression = "java(owner == null ? null : owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner == null ? null : Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(owner == null ? null : owner.getTelephoneDisplay())")
    @Mapping(target = "membershipPoints",
        expression = "java(owner == null ? null : owner.getMembershipPoints())")
    @Mapping(target = "membershipLevel",
        expression = "java(owner == null ? null : owner.getMembershipLevel())")
    @Mapping(target = "locality",
        expression = "java(owner == null ? null : org.springframework.samples.petclinic.mapper.LocalityLookup.regionFor(owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "timezone",
        expression = "java(owner == null ? null : org.springframework.samples.petclinic.mapper.LocalityLookup.timezoneFor(owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner == null ? null : owner.getContactPreference())")
    @Mapping(target = "ageBand",
        expression = "java(owner == null || owner.getAgeBand() == null ? null : org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.fromValue(owner.getAgeBand()))")
    @Mapping(target = "fiscalYear",
        expression = "java(owner == null ? null : owner.getFiscalYear())")
    @Mapping(target = "apiVersion",
        expression = "java(owner == null ? null : Integer.valueOf(2))")
    @Mapping(target = "identity",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerMapper.identityOf(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(owner == null ? null : org.springframework.samples.petclinic.rest.dto.OwnerDto.OwnerSegmentEnum.fromValue(org.springframework.samples.petclinic.mapper.LocalityLookup.segmentFor(owner.getMembershipLevel(), owner.getPostcode(), owner.getCity())))")
    @Mapping(target = "selfLink",
        expression = "java(owner == null ? null : owner.getSelfLink())")
    @Mapping(target = "riskFlag",
        expression = "java(owner == null ? null : owner.getRiskFlag())")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Builds the version-2 {@code identity} bundle for an owner: the memberId, identityKey and
     * householdId grouped under a single object. Declared {@code static} on purpose so MapStruct
     * does not treat this single-argument method as a candidate mapping method; it is referenced
     * only from the explicit {@code identity} mapping expression.
     *
     * @param owner the owner to read the identifiers from, possibly {@code null}
     * @return the identity bundle, or {@code null} when {@code owner} is {@code null}
     */
    static OwnerIdentityDto identityOf(Owner owner) {
        if (owner == null) {
            return null;
        }
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(owner.getIdentityKey());
        identity.setHouseholdId(owner.getHouseholdId());
        return identity;
    }

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
