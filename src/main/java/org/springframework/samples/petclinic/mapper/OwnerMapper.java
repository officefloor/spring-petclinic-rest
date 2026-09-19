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
    @Mapping(target = "checkDigit",
        expression = "java(org.springframework.samples.petclinic.util.LuhnCheckDigit.compute(owner.getCustomerCode()))")
    @Mapping(target = "membershipLevel",
        expression = "java(org.springframework.samples.petclinic.util.MembershipLevelFormatter.format(owner.getNamesakeCount(), owner.getEmail()))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.util.LocalityResolver.resolve(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "contactPreference",
        expression = "java(org.springframework.samples.petclinic.util.ContactPreferenceResolver.resolve(owner.getEmail()))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.util.OwnerIdentityKey.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "address",
        expression = "java(org.springframework.samples.petclinic.util.AddressNormalizer.normalize(ownerDto.getAddress()))")
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
