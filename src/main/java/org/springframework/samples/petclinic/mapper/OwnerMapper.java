package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Addresses;
import org.springframework.samples.petclinic.util.E164;
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
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java((owner.getTitle() == null || owner.getTitle().isBlank()) ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials", source = "initials")
    @Mapping(target = "apiVersion", expression = "java(Integer.valueOf(2))")
    @Mapping(target = "identity", expression = "java(toOwnerIdentity(owner))")
    @Mapping(target = "fiscalYear", expression = "java(owner.getRegistrationDate() == null ? null : org.springframework.samples.petclinic.util.FiscalYears.label(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.util.Membership.points(owner, false, false))")
    @Mapping(target = "membershipLevel", expression = "java(org.springframework.samples.petclinic.util.Membership.level(org.springframework.samples.petclinic.util.Membership.points(owner, false, false)))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.util.CustomerCodes.regionOf(owner.getCustomerCode()))")
    @Mapping(target = "ownerSegment", expression = "java(org.springframework.samples.petclinic.util.Segments.of(org.springframework.samples.petclinic.util.Membership.level(org.springframework.samples.petclinic.util.Membership.points(owner, false, false)), org.springframework.samples.petclinic.util.CustomerCodes.regionOf(owner.getCustomerCode())))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.util.Timezones.of(org.springframework.samples.petclinic.util.CustomerCodes.regionOf(owner.getCustomerCode())))")
    @Mapping(target = "contactPreference", expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.util.TelephoneDisplay.format(owner.getTelephone()))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's version-2 identity block: the v2 memberId, identityKey and the household id
     *  already derived onto the owner. Grouped so the identifiers travel together in the response. */
    default org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto toOwnerIdentity(Owner owner) {
        org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity =
            new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto();
        identity.setMemberId(org.springframework.samples.petclinic.util.MemberIds.of(owner));
        identity.setIdentityKey(org.springframework.samples.petclinic.util.IdentityKeys.of(owner));
        identity.setHouseholdId(owner.getHouseholdId());
        return identity;
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "normalizeAddress")
    @Mapping(target = "addressLine2", source = "addressLine2", qualifiedByName = "normalizeAddress")
    @Mapping(target = "address", expression = "java(org.springframework.samples.petclinic.util.Addresses.compose(ownerDto.getAddressLine1(), ownerDto.getAddressLine2(), ownerDto.getAddress()))")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /** Store the telephone in E.164 form (see {@link E164#toE164}). */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return E164.toE164(telephone);
    }

    /** Store the address in normalized form (see {@link Addresses#normalize}). */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return Addresses.normalize(address);
    }

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
