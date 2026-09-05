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
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(toMembershipNumber(owner))")
    @Mapping(target = "membershipTier", expression = "java(toMembershipTier(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Fixed city-to-region table used to derive the owner's locality.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Derives the owner's locality (region) from the city via the fixed city-to-region
     * table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), or {@code UNKNOWN} when the city
     * is not in the table.
     */
    default String toLocality(Owner owner) {
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Derives the owner's membership tier: {@code GOLD} when the owner's household has 3 or more
     * members ({@code householdSize} &ge; 3, as counted after the owner was created); otherwise
     * {@code SILVER} when the owner has no namesakes ({@code namesakeCount} is 0) and an email is
     * present, otherwise {@code BRONZE}.
     */
    default OwnerDto.MembershipTierEnum toMembershipTier(Owner owner) {
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            return OwnerDto.MembershipTierEnum.GOLD;
        }
        boolean unique = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return unique && hasEmail ? OwnerDto.MembershipTierEnum.SILVER : OwnerDto.MembershipTierEnum.BRONZE;
    }

    /**
     * Derives the owner's membership number as {@code <customerCode>-M<YY>}, where YY is the
     * last two digits of the registration-date year (e.g. {@code SMI-0007-M26}). Returns null
     * when either the customer code or registration date is absent (e.g. unmigrated seed data).
     */
    default String toMembershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M" + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
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
