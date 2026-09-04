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
@Mapper(uses = {PetMapper.class, MembershipDecorator.class})
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "telephoneDisplay", expression = "java(TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "householdId", expression = "java(HouseholdId.of(owner.getLastName(), owner.getPostcode()))")
    @Mapping(target = "identityKey", expression = "java(IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName()))")
    @Mapping(target = "membershipNumber", expression = "java(MembershipNumber.of(owner.getCustomerCode(), owner.getRegistrationDate()))")
    @Mapping(target = "checkDigit", expression = "java(CheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "membershipLevel", ignore = true)
    @Mapping(target = "membershipPoints", ignore = true)
    @Mapping(target = "locality", expression = "java(owner.getCustomerCode() != null ? owner.getCustomerCode().substring(0, owner.getCustomerCode().indexOf('-')) : Locality.of(owner.getPostcode(), owner.getCity()))")
    @Mapping(target = "timezone", expression = "java(Timezone.of(owner.getCustomerCode() != null ? owner.getCustomerCode().substring(0, owner.getCustomerCode().indexOf('-')) : Locality.of(owner.getPostcode(), owner.getCity())))")
    @Mapping(target = "contactPreference", expression = "java(ContactPreference.of(owner.getEmail()))")
    @Mapping(target = "ageBand", expression = "java(AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear", expression = "java(FiscalYear.label(owner.getRegistrationDate()))")
    OwnerDto toOwnerDto(Owner owner);

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
