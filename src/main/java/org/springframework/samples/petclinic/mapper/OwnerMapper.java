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

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", source = "initials")
    @Mapping(target = "membershipNumber", expression = "java((owner.getCustomerCode() == null || owner.getRegistrationDate() == null) ? null : owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipLevel", expression = "java(1 + ((owner.getEmail() != null && !owner.getEmail().isBlank()) ? 1 : 0) + (java.lang.Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.util.Localities.regionOf(owner.getCity()))")
    @Mapping(target = "contactPreference", expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) ? \"EMAIL\" : \"PHONE\")")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
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
