package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerRegion;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;
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

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "displayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "initials")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "locality")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "contactPreference")
    @Mapping(target = "identityKey", source = "owner", qualifiedByName = "identityKey")
    OwnerDto toOwnerDto(Owner owner);

    @Named("identityKey")
    default String identityKey(Owner owner) {
        return OwnerIdentity.identityKey(owner);
    }

    @Named("displayName")
    default String displayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    @Named("initials")
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    @Named("locality")
    default String locality(Owner owner) {
        return OwnerRegion.localityOf(owner.getCity(), owner.getPostcode());
    }

    @Named("contactPreference")
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isBlank())
            ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
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
