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
    @Mapping(target = "householdId", expression = "java(java.util.UUID.nameUUIDFromBytes((owner.getLastName() + \"|\" + owner.getAddress()).trim().replaceAll(\"\\\\s+\", \" \").toLowerCase(java.util.Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString())")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.rest.advice.OwnerIdentityKey.of(owner))")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() == null || owner.getRegistrationDate() == null ? null : owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipLevel", expression = "java(Math.min(3, 1 + (owner.getEmail() != null && !owner.getEmail().isBlank() ? 1 : 0) + (Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0)))")
    @Mapping(target = "locality", expression = "java(java.util.Map.of(20, \"NSW\", 30, \"VIC\", 40, \"QLD\").getOrDefault(owner.getPostcode() != null && owner.getPostcode().matches(\"[0-9]{4}\") ? Integer.parseInt(owner.getPostcode()) / 100 : -1, java.util.Map.of(\"Sydney\", \"NSW\", \"Melbourne\", \"VIC\", \"Brisbane\", \"QLD\").getOrDefault(owner.getCity(), \"UNKNOWN\")))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
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
