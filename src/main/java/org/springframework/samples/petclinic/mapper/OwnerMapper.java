package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "householdId", expression = "java(org.springframework.samples.petclinic.util.Household.idFor(owner))")
    @Mapping(target = "fiscalYear", expression = "java(org.springframework.samples.petclinic.util.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "memberId", expression = "java(owner.getMemberId())")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.util.Membership.points(owner))")
    @Mapping(target = "membershipLevel", expression = "java(org.springframework.samples.petclinic.util.Membership.level(org.springframework.samples.petclinic.util.Membership.points(owner)))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.util.Locality.of(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.util.Timezone.of(org.springframework.samples.petclinic.util.Locality.of(owner.getCity(), owner.getPostcode())))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.util.AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.util.IdentityKey.of(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.util.TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "ownerSegment", expression = "java(org.springframework.samples.petclinic.util.OwnerSegment.of(org.springframework.samples.petclinic.util.Membership.level(org.springframework.samples.petclinic.util.Membership.points(owner)), org.springframework.samples.petclinic.util.Locality.of(owner.getCity(), owner.getPostcode())))")
    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "registrationDate", source = "registrationDate", qualifiedByName = "defaultRegistrationDate")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /** Default a missing registration date to the server's current date, then roll
     *  the effective date forward onto a business day. */
    @Named("defaultRegistrationDate")
    default LocalDate defaultRegistrationDate(LocalDate registrationDate) {
        return org.springframework.samples.petclinic.util.BusinessDay.rollForward(
            registrationDate == null ? LocalDate.now() : registrationDate);
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
