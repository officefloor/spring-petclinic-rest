package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.AgeBand;
import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.ContactPreference;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSegment;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "apiVersion",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.IdentityVersion.NUMBER)")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * How to address the owner: their {@link Owner#getTitle() title} followed by a space
     * and the last name (e.g. {@code DR Franklin}), or just the last name when no title
     * was supplied.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        return (title == null || title.isBlank())
            ? owner.getLastName()
            : title + " " + owner.getLastName();
    }

    /**
     * The upper-cased first letters of the first and last name, dot-separated with a
     * trailing dot, e.g. {@code J.S.}.
     */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's stored E.164 telephone number formatted for humans as the country code, a
     * space, then the national digits grouped in threes (e.g. {@code +61 412 345 678}). The
     * raw {@code telephone} field stays in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.Telephones
            .toDisplay(owner.getTelephone());
    }

    /**
     * The fiscal year of the owner's (business-day-adjusted) registration date, formatted as
     * {@code FY<YY>} (see {@link org.springframework.samples.petclinic.model.FiscalYear}).
     */
    default String fiscalYear(Owner owner) {
        return org.springframework.samples.petclinic.model.FiscalYear
            .label(owner.getRegistrationDate());
    }

    /**
     * The owner's membership points, as defined by {@link MembershipLevel}.
     */
    default int membershipPoints(Owner owner) {
        return MembershipLevel.points(owner);
    }

    /**
     * The owner's membership level from 1 to 4, as defined by {@link MembershipLevel}.
     */
    default int membershipLevel(Owner owner) {
        return MembershipLevel.of(owner);
    }

    /**
     * The owner's plain region, derived by postcode first and then city (see
     * {@link CityRegion#localityOf(String, String)}). This is the user-facing region code
     * (e.g. {@code NSW}); it deliberately never carries the version tag that the member id
     * embeds in its own region segment.
     */
    default String locality(Owner owner) {
        return CityRegion.localityOf(owner.getPostcode(), owner.getCity());
    }

    /**
     * The owner's IANA timezone, derived from their {@link #locality(Owner) locality} via
     * the pinned region-to-timezone table (NSW -> Australia/Sydney, VIC ->
     * Australia/Melbourne, QLD -> Australia/Brisbane), or {@code null} when the locality
     * resolves to no known region.
     */
    default String timezone(Owner owner) {
        return CityRegion.timezoneOf(locality(owner));
    }

    /**
     * The owner's marketing segment, formatted as {@code <TIER>_<AREA>} (see
     * {@link OwnerSegment}): the tier from the owner's {@link #membershipLevel(Owner)
     * membership level} and the area from their {@link #locality(Owner) locality}.
     */
    default String ownerSegment(Owner owner) {
        return OwnerSegment.of(owner);
    }

    /**
     * How the owner prefers to be contacted: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    default String contactPreference(Owner owner) {
        return ContactPreference.of(owner).name();
    }

    /**
     * The owner's age band derived from their birth date against their registration date
     * (see {@link AgeBand}), or {@code null} when no birth date was supplied.
     */
    default String ageBand(Owner owner) {
        AgeBand ageBand = AgeBand.of(owner);
        return ageBand == null ? null : ageBand.name();
    }

    /**
     * The owner's derived identity key used for duplicate detection (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.IdentityKeys}).
     */
    default String identityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.IdentityKeys.of(owner);
    }

    /**
     * The owner's grouped version-2 identifiers — its {@link Owner#getMemberId() member id},
     * {@link Owner#getHouseholdId() household id} and {@link #identityKey(Owner) identity key} —
     * assembled into the nested {@code identity} object of the response.
     */
    default OwnerIdentityDto identity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(identityKey(owner));
        return identity;
    }

    @Mapping(target = "memberId", source = "identity.memberId")
    @Mapping(target = "householdId", source = "identity.householdId")
    @Mapping(target = "householdMemberCount", ignore = true)
    @Mapping(target = "membershipLevelCap", ignore = true)
    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "memberId", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdMemberCount", ignore = true)
    @Mapping(target = "membershipLevelCap", ignore = true)
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
