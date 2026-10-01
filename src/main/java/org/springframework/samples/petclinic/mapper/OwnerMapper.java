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

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "householdId", expression = "java(formatHouseholdId(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** Format an owner's name as 'LastName, FirstName' from the stored names. */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Format an owner's initials as the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot (e.g. 'J.S.').
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Compute the owner's household id, the stable identifier shared by all owners who live in the
     * same household (see {@link Owner#householdId(String, String)}).
     */
    default String formatHouseholdId(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.householdId(owner.getLastName(), owner.getAddress());
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "telephone", qualifiedByName = "normalizeTelephone")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /** Normalize a telephone to its canonical E.164 form, matching {@code TelephoneValidator}. */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return Owner.normalizeTelephone(telephone);
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
