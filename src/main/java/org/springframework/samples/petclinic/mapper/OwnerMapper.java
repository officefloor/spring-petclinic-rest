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

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java((owner.getFirstName().charAt(0) + \".\" + owner.getLastName().charAt(0) + \".\").toUpperCase())")
    @Mapping(target = "householdId", expression = "java(org.springframework.samples.petclinic.service.HouseholdMatcher.householdId(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "address", expression = "java(org.springframework.samples.petclinic.service.AddressNormalizer.normalize(ownerDto.getAddress()))")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalise a telephone to E.164 for storage: keep an explicit leading '+' and country
     * code when present, otherwise assume '+61' and drop a single leading national '0'.
     * Spaces, dashes and brackets are stripped; the result must have 8 to 15 digits after
     * the '+', else the number is rejected as invalid.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean international = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        if (!international) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new IllegalArgumentException("Telephone cannot form a valid E.164 number: " + telephone);
        }
        return "+" + digits;
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
