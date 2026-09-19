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

    @Mapping(target = "displayName",
        expression = "java(org.springframework.samples.petclinic.util.PersonNameFormatter.displayName(owner.getFirstName(), owner.getLastName()))")
    @Mapping(target = "initials",
        expression = "java(org.springframework.samples.petclinic.util.PersonNameFormatter.initials(owner.getFirstName(), owner.getLastName()))")
    @Mapping(target = "membershipNumber",
        expression = "java(org.springframework.samples.petclinic.util.MembershipNumberFormatter.format(owner.getCustomerCode(), owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear",
        expression = "java(org.springframework.samples.petclinic.util.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "checkDigit",
        expression = "java(org.springframework.samples.petclinic.util.LuhnCheckDigit.compute(owner.getCustomerCode()))")
    @Mapping(target = "membershipPoints",
        expression = "java(org.springframework.samples.petclinic.util.MembershipPointsFormatter.format(owner.getNamesakeCount(), owner.getEmail(), owner.getHouseholdSize(), owner.getRegistrationDate()))")
    @Mapping(target = "membershipLevel",
        expression = "java(org.springframework.samples.petclinic.util.MembershipLevelFormatter.format(org.springframework.samples.petclinic.util.MembershipPointsFormatter.format(owner.getNamesakeCount(), owner.getEmail(), owner.getHouseholdSize(), owner.getRegistrationDate())))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.util.LocalityResolver.fromCustomerCode(owner.getCustomerCode(), owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.util.TimezoneResolver.forRegion(org.springframework.samples.petclinic.util.LocalityResolver.fromCustomerCode(owner.getCustomerCode(), owner.getCity(), owner.getPostcode())))")
    @Mapping(target = "contactPreference",
        expression = "java(org.springframework.samples.petclinic.util.ContactPreferenceResolver.resolve(owner.getEmail()))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.util.AgeBandResolver.resolve(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.util.OwnerIdentityKey.of(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.util.TelephoneDisplayFormatter.format(owner.getTelephone()))")
    @Mapping(target = "salutation",
        expression = "java(org.springframework.samples.petclinic.util.SalutationFormatter.format(owner.getTitle(), owner.getLastName()))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "addressLine1",
        expression = "java(org.springframework.samples.petclinic.util.AddressNormalizer.normalize(ownerDto.getAddressLine1()))")
    @Mapping(target = "addressLine2",
        expression = "java(org.springframework.samples.petclinic.util.AddressNormalizer.normalize(ownerDto.getAddressLine2()))")
    @Mapping(target = "address",
        expression = "java(org.springframework.samples.petclinic.util.AddressComposer.compose(ownerDto.getAddressLine1(), ownerDto.getAddressLine2(), ownerDto.getAddress()))")
    @Mapping(target = "telephone",
        expression = "java(org.springframework.samples.petclinic.util.TelephoneNormalizer.toE164(ownerDto.getTelephone()).orElse(null))")
    @Mapping(target = "email",
        expression = "java(org.springframework.samples.petclinic.util.EmailNormalizer.normalize(ownerDto.getEmail()))")
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
