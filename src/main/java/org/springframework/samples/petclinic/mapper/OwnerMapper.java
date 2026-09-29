package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.function.owner.MembershipLevel;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink",
            expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
            expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() "
                    + "? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
            expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
                    + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".TelephoneNormalizer.toDisplay(owner.getTelephone()))")
    @Mapping(target = "locality",
            expression = "java(locality(owner))")
    @Mapping(target = "timezone",
            expression = "java(org.springframework.samples.petclinic.rest.function.common"
                    + ".Localities.timezoneOfRegion(locality(owner)))")
    @Mapping(target = "fiscalYear",
            expression = "java(org.springframework.samples.petclinic.rest.function.common"
                    + ".FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".IdentityKey.ofOwner(owner))")
    @Mapping(target = "contactPreference",
            expression = "java(hasEmail(owner) ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints",
            expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(membershipLevel(owner))")
    @Mapping(target = "ownerSegment",
            expression = "java(ownerSegment(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** Whether the owner has a usable email address. */
    default boolean hasEmail(Owner owner) {
        return MembershipLevel.hasEmail(owner);
    }

    /**
     * The owner's membership points (see {@link MembershipLevel#points(Owner, Integer)} for the rule).
     *
     * <p>The household size is not derived here: it depends on other owners, so it is computed by
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerHousehold} before the
     * owner reaches this mapper and read from the entity, keeping this a pure transformation.
     */
    default int membershipPoints(Owner owner) {
        return MembershipLevel.points(owner, owner.getHouseholdSize());
    }

    /**
     * The owner's raw numeric membership level, mapped from {@link #membershipPoints(Owner)}: level 1
     * for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more. This is the level before the
     * household ceiling in {@link #membershipLevel(Owner)} is applied.
     */
    default int rawMembershipLevel(Owner owner) {
        return MembershipLevel.level(membershipPoints(owner));
    }

    /**
     * The owner's numeric membership level: their {@link #rawMembershipLevel(Owner)} capped so it cannot
     * exceed one above the current maximum membershipLevel among their household members. With no
     * existing household member no cap applies.
     *
     * <p>The ceiling depends on other owners, so — like the household size — it is not derived here: the
     * maximum level among other household members is computed by
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerHousehold} before the owner
     * reaches this mapper and read from {@link Owner#getHouseholdMaxMembershipLevel()} (null when no
     * other member exists), keeping this a pure transformation.
     */
    default int membershipLevel(Owner owner) {
        int level = rawMembershipLevel(owner);
        Integer householdMax = owner.getHouseholdMaxMembershipLevel();
        if (householdMax != null && level > householdMax + 1) {
            level = householdMax + 1;
        }
        return level;
    }

    /**
     * The owner's marketing segment, formatted {@code <TIER>_<AREA>}. TIER is {@code PREMIUM} when the
     * owner's {@link #membershipLevel(Owner)} is 3 or more, otherwise {@code STANDARD}. AREA is
     * {@code METRO} when the owner's locality (the region segment of the memberId) is a known region
     * (NSW, VIC or QLD), otherwise {@code REGIONAL}.
     */
    default String ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = org.springframework.samples.petclinic.rest.function.common.Localities
                .isKnownRegion(locality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The owner's canonical region ("locality"), read from the region segment of their memberId via
     * {@link org.springframework.samples.petclinic.rest.function.owner.MemberId#regionOf(String)}, or
     * {@code "UNKNOWN"} when the id maps to no known region. Shared by the {@code locality} and
     * {@code timezone} mappings and by {@link #ownerSegment(Owner)} so the derivation lives in one place.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.MemberId
                .regionOf(owner.getMemberId());
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
