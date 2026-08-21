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
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipLevel",
        expression = "java(Math.min(3, 1 "
            + "+ (owner.getEmail() != null && !owner.getEmail().isEmpty() ? 1 : 0) "
            + "+ (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0 ? 1 : 0)))")
    @Mapping(target = "locality", expression = "java(deriveLocality(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isEmpty() "
            + "? org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.EMAIL "
            + ": org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.PHONE)")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Derive the locality region for an owner. The postcode is preferred: when present and it falls
     * within a known region's inclusive 4-digit range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099)
     * that region is returned. Otherwise fall back to the pinned city-to-region table (Sydney->NSW,
     * Melbourne->VIC, Brisbane->QLD), and finally "UNKNOWN" for a city with no known region.
     */
    default String deriveLocality(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("[0-9]{4}")) {
            int value = Integer.parseInt(postcode);
            if (value >= 2000 && value <= 2099) {
                return "NSW";
            }
            if (value >= 3000 && value <= 3099) {
                return "VIC";
            }
            if (value >= 4000 && value <= 4099) {
                return "QLD";
            }
        }
        String city = owner.getCity();
        if ("Sydney".equals(city)) {
            return "NSW";
        }
        if ("Melbourne".equals(city)) {
            return "VIC";
        }
        if ("Brisbane".equals(city)) {
            return "QLD";
        }
        return "UNKNOWN";
    }

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
