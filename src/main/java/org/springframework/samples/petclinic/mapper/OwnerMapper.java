package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
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

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "householdId", expression = "java(householdId(owner))")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), \"\"))")
    @Mapping(target = "checkDigit", expression = "java(owner.getCustomerCode() == null ? null : org.springframework.samples.petclinic.rest.function.owner.CheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.rest.function.owner.MembershipPoints.of(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.rest.function.owner.CityLocality.of(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.rest.function.owner.RegionTimezone.of(org.springframework.samples.petclinic.rest.function.owner.CityLocality.of(owner.getCity(), owner.getPostcode())))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.rest.function.owner.AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear", expression = "java(owner.getRegistrationDate() == null ? null : org.springframework.samples.petclinic.rest.function.owner.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "selfLink", expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "addressLine1", source = "address")
    @Mapping(target = "salutation", expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    OwnerDto toOwnerDto(Owner owner);

    /** Membership level, derived from {@link org.springframework.samples.petclinic.rest.function.owner.MembershipPoints
     *  membershipPoints}: 1 (0-1 points), 2 (2-3), 3 (4-5), 4 (6 or more). */
    default Integer membershipLevel(Owner owner) {
        if (owner.getMembershipLevel() != null) {
            return owner.getMembershipLevel();
        }
        return org.springframework.samples.petclinic.rest.function.owner.MembershipPoints.level(
                org.springframework.samples.petclinic.rest.function.owner.MembershipPoints.of(owner));
    }

    /** Membership number: '<customerCode>-M<YY>', YY being the last two digits of the
     *  registrationDate year. Null until both source fields are assigned. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
                org.springframework.samples.petclinic.rest.function.owner.FiscalYear.shortYear(owner.getRegistrationDate()));
    }

    /** Stable identifier for the household an owner belongs to: the first 12 hex characters of SHA-256
     *  over (normalizedLastName + '|' + postcode), so owners with the same last name and postcode
     *  share it. */
    default String householdId(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.HouseholdId.of(
                owner.getLastName(), owner.getPostcode());
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
