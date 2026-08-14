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
            expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
                    + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipTier", expression = "java(membershipTier(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Fixed city-to-region table backing the owner's {@code locality}: Sydney maps to {@code 'NSW'},
     * Melbourne to {@code 'VIC'} and Brisbane to {@code 'QLD'}.
     */
    java.util.Map<String, String> CITY_REGIONS = java.util.Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Derives the owner's locality (region) from city using the fixed {@link #CITY_REGIONS} table,
     * returning the canonical region string or {@code 'UNKNOWN'} when the city is not in the table.
     */
    default String locality(Owner owner) {
        return CITY_REGIONS.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Derives the owner's membership tier: {@code 'GOLD'} when the owner's household (owners sharing
     * the same householdId) has 3 or more members after this create; otherwise {@code 'SILVER'} when
     * namesakeCount is 0 and an email is present, otherwise {@code 'BRONZE'}.
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerDto.MembershipTierEnum membershipTier(Owner owner) {
        boolean goldHousehold = owner.getHouseholdMemberCount() != null
                && owner.getHouseholdMemberCount() >= 3;
        if (goldHousehold) {
            return org.springframework.samples.petclinic.rest.dto.OwnerDto.MembershipTierEnum.GOLD;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        return noNamesakes && hasEmail
                ? org.springframework.samples.petclinic.rest.dto.OwnerDto.MembershipTierEnum.SILVER
                : org.springframework.samples.petclinic.rest.dto.OwnerDto.MembershipTierEnum.BRONZE;
    }

    /**
     * Derives the owner's membership number, formatted {@code '<customerCode>-M<YY>'} where YY is the
     * last two digits of the registrationDate year (e.g. {@code 'SYD-SMI-0007-M26'}). Returns {@code null}
     * when either source field is absent, so owners without a customerCode or registrationDate map cleanly.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdMemberCount", ignore = true)
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
