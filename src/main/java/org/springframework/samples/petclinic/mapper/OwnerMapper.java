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

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "formatDisplayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "formatInitials")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'} for the read-only
     * {@code displayName} field.
     */
    @Named("formatDisplayName")
    default String formatDisplayName(Owner owner) {
        return owner == null ? null : owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot (e.g. {@code 'J.S.'}), for the
     * read-only {@code initials} field.
     */
    @Named("formatInitials")
    default String formatInitials(Owner owner) {
        return owner == null ? null
            : Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalizes a telephone number for persistence by removing every non-digit character.
     * The exactly-10-digits rule is enforced separately by {@code TelephoneValidation}.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return telephone == null ? null : telephone.replaceAll("\\D", "");
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
