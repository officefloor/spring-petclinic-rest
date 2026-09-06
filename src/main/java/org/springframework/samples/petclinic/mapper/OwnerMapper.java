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

    @Mapping(target = "selfLink",
        expression = "java(owner.getId() != null ? \"/api/owners/\" + owner.getId() : null)")
    @Mapping(target = "salutation",
        expression = "java(owner.getTitle() != null && !owner.getTitle().isBlank() "
            + "? owner.getTitle() + \" \" + owner.getLastName() : owner.getLastName())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "fiscalYear",
        expression = "java(org.springframework.samples.petclinic.mapper.FiscalYear.labelForOwner(owner))")
    @Mapping(target = "membershipPoints",
        expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevel.pointsForOwner(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(org.springframework.samples.petclinic.mapper.MembershipLevel.forOwner(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.forPostcodeAndCity("
            + "owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.timezoneForRegion("
            + "org.springframework.samples.petclinic.mapper.OwnerLocality.forPostcodeAndCity("
            + "owner.getPostcode(), owner.getCity())))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.mapper.TelephoneDisplay.forE164(owner.getTelephone()))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.mapper.AgeBand.forOwner(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(org.springframework.samples.petclinic.mapper.OwnerSegment.forOwner(owner))")
    @Mapping(target = "apiVersion", expression = "java(Integer.valueOf(2))")
    @Mapping(target = "identity", expression = "java(toOwnerIdentity(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's version-2 derived identifiers into the nested identity object of the
     * response: the memberId, identityKey and householdId that are no longer returned at the top
     * level. Returns {@code null} when the owner carries none of them (e.g. seed data created outside
     * the create endpoint), so the identity object is omitted rather than emitted empty.
     *
     * @param owner the owner being mapped
     * @return the populated identity object, or {@code null} when the owner has no derived identifiers
     */
    default OwnerIdentityDto toOwnerIdentity(Owner owner) {
        if (owner.getMemberId() == null && owner.getIdentityKey() == null
            && owner.getHouseholdId() == null) {
            return null;
        }
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(owner.getIdentityKey());
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
