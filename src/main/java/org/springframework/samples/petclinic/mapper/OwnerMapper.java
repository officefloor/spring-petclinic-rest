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

    @Mapping(target = "displayName", expression = "java(toDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Maps the owner's locality, held as the string {@code 'local'} or {@code 'remote'}, to the
     * corresponding {@link OwnerDto.LocalityEnum} value. Unlike the default MapStruct string-to-enum
     * conversion (which matches on the enum constant name), this resolves by the enum's JSON value,
     * so the lower-cased locality values map correctly.
     *
     * @param owner the owner, may be {@code null}
     * @return the matching locality enum value, or {@code null} if {@code owner} or its locality is
     *         {@code null}
     */
    default OwnerDto.LocalityEnum toLocality(Owner owner) {
        if (owner == null || owner.getLocality() == null) {
            return null;
        }
        return OwnerDto.LocalityEnum.fromValue(owner.getLocality());
    }

    /**
     * Computes the display name of an owner formatted as {@code 'LastName, FirstName'}.
     *
     * @param owner the owner, may be {@code null}
     * @return the formatted display name, or {@code null} if {@code owner} is {@code null}
     */
    default String toDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Computes the initials of an owner formatted as the upper-cased first letters of the
     * first and last name, dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     *
     * @param owner the owner, may be {@code null}
     * @return the formatted initials, or {@code null} if {@code owner} is {@code null}
     */
    default String toInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private static String initial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + ".";
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
