package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.function.owner.IdentityKey;
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
    @Mapping(target = "apiVersion",
            expression = "java(org.springframework.samples.petclinic.rest.function.common"
                    + ".IdentityVersion.VERSION)")
    @Mapping(target = "identity",
            expression = "java(identity(owner))")
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
    @Mapping(target = "riskFlag",
            expression = "java(riskFlag(owner))")
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
     * {@code METRO} when the owner's plain {@link #locality(Owner)} region is a known region (NSW, VIC
     * or QLD), otherwise {@code REGIONAL}. Recomputed from the version-2 identity via the plain region,
     * so the {@code V2} tag mixed into the memberId never flips a known region to {@code REGIONAL}.
     */
    default String ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = org.springframework.samples.petclinic.rest.function.common.Localities
                .isKnownRegion(locality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The owner's canonical region ("locality"), the <em>plain</em> region code derived from their
     * postcode via {@link org.springframework.samples.petclinic.rest.function.common.Localities
     * #ofPostcode(String)}, or {@code "UNKNOWN"} when the postcode maps to no known region. This is a
     * user-facing field, not an identifier, so the identity version-2 tag mixed into the memberId's
     * region segment never appears here. Shared by the {@code locality} and {@code timezone} mappings
     * and by {@link #ownerSegment(Owner)} so the plain region derivation lives in one place.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.common.Localities
                .ofPostcode(owner.getPostcode());
    }

    /**
     * The owner's identity-version-2 identifier group — the {@code memberId}, {@code householdId} and
     * derived {@code identityKey}, gathered under the nested {@code identity} object. Each carries the
     * fixed {@code V2} version tag mixed into its derivation, so none repeats a value produced under
     * version 1.
     */
    default OwnerIdentityDto identity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(IdentityKey.ofOwner(owner));
        return identity;
    }

    /**
     * The owner's read-model risk flag: {@code true} when any soft-risk signal holds — the owner is a
     * possible duplicate ({@link Owner#getPossibleDuplicate()}), its email domain is disposable-adjacent
     * (see {@link org.springframework.samples.petclinic.rest.function.owner.DisposableEmailDomains
     * #isDisposableAdjacent(String)}), or its city is over its soft capacity
     * ({@link Owner#getCapacityWarning()}) — otherwise {@code false}. Derived here rather than persisted
     * so the create response and later reads always agree.
     */
    default boolean riskFlag(Owner owner) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean overSoftCapacity = Boolean.TRUE.equals(owner.getCapacityWarning());
        boolean disposableAdjacent = org.springframework.samples.petclinic.rest.function.owner
                .DisposableEmailDomains.isDisposableAdjacent(owner.getEmail());
        return possibleDuplicate || disposableAdjacent || overSoftCapacity;
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
