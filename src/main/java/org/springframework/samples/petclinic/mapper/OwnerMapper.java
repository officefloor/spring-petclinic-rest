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
    @Mapping(target = "membershipTier", expression = "java(membershipTier(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Reports the owner's bulk-signup warning: {@code true} when more than 80 owners had already
     * been created on this owner's registration business day at the time it was created. Treats a
     * missing value (owners created before this rule existed) as {@code false}.
     */
    default boolean bulkSignupWarning(Owner owner) {
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Derives the owner's membership tier at read time from fields captured at creation:
     * {@code GOLD} when the owner's household (owners sharing the same {@code householdId}) had 3
     * or more members after this owner was created; otherwise {@code SILVER} when the owner had no
     * namesakes at creation and has an email on file, else {@code BRONZE}.
     */
    default String membershipTier(Owner owner) {
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            return "GOLD";
        }
        boolean silver = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0
            && owner.getEmail() != null && !owner.getEmail().isBlank();
        return silver ? "SILVER" : "BRONZE";
    }

    /**
     * Derives the owner's locality (region) at read time from the city via a fixed
     * city-to-region table: Sydney->NSW, Melbourne->VIC, Brisbane->QLD. Returns the
     * canonical region string, or {@code UNKNOWN} when the city is not in the table.
     */
    default String locality(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        switch (city) {
            case "Sydney":
                return "NSW";
            case "Melbourne":
                return "VIC";
            case "Brisbane":
                return "QLD";
            default:
                return "UNKNOWN";
        }
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
