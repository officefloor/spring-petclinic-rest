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
import org.springframework.samples.petclinic.rest.function.owner.HouseholdNormalizer;
import org.springframework.samples.petclinic.rest.function.owner.IdentityKey;
import org.springframework.samples.petclinic.rest.function.owner.TelephoneNormalizer;
import org.springframework.samples.petclinic.util.AgeBand;
import org.springframework.samples.petclinic.util.ContactPreference;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.IdentityVersion;
import org.springframework.samples.petclinic.util.Locality;
import org.springframework.samples.petclinic.util.MemberId;
import org.springframework.samples.petclinic.util.MembershipLevel;
import org.springframework.samples.petclinic.util.MembershipPoints;
import org.springframework.samples.petclinic.util.OwnerSegment;
import org.springframework.samples.petclinic.util.RegionTimezone;
import org.springframework.samples.petclinic.util.RiskFlag;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "apiVersion", expression = "java(apiVersion())")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The canonical API path of the owner: '/api/owners/' followed by the owner's id. */
    default String selfLink(Owner owner) {
        return owner == null || owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /** The owner's name formatted as 'LastName, FirstName'. */
    default String displayName(Owner owner) {
        return owner == null ? null : owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * The owner's salutation: the honorific title followed by a space and the last name (e.g.
     * 'DR who'), or just the last name when no title is given.
     */
    default String salutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        return title == null || title.isBlank() ? owner.getLastName() : title + " " + owner.getLastName();
    }

    /**
     * The canonical region taken from the owner's identity: the REGION portion of the memberId (see
     * {@link MemberId}) with the {@link IdentityVersion version tag} stripped, so the user-facing
     * locality stays the plain region code (e.g. 'NSW') even though the identifier embeds the tagged
     * form. Falls back to deriving the region directly from the city and postcode (see
     * {@link Locality}) for an owner that has no memberId yet.
     */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = IdentityVersion.plainRegion(MemberId.regionOf(owner.getMemberId()));
        return region != null ? region : Locality.of(owner.getCity(), owner.getPostcode());
    }

    /**
     * The owner's IANA timezone (see {@link RegionTimezone}), derived from the owner's
     * {@link #locality(Owner) locality} (region): NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane. {@code null} when the region has no timezone.
     */
    default String timezone(Owner owner) {
        return owner == null ? null : RegionTimezone.of(locality(owner));
    }

    /** The upper-cased first letters of the first and last name, e.g. 'J.S.'. */
    default String initials(Owner owner) {
        return owner == null ? null
            : Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's fiscal year (see {@link FiscalYear}) as 'FY&lt;YY&gt;', where YY is the last two
     * digits of the fiscal year (starting 1 July) of the business-day-adjusted registration date.
     * {@code null} when the owner or its registration date is {@code null}.
     */
    default String fiscalYear(Owner owner) {
        return owner == null || owner.getRegistrationDate() == null ? null
            : FiscalYear.label(owner.getRegistrationDate());
    }

    /**
     * The owner's membership points (see {@link MembershipPoints}): a score that starts at 0 and
     * gains 2 for a present email address, 1 for a unique name (namesakeCount is zero), 2 for a
     * household of 3 or more and 3 for tenure over a year.
     */
    default Integer membershipPoints(Owner owner) {
        return MembershipPoints.of(owner);
    }

    /**
     * The owner's membership level (see {@link MembershipLevel}): the {@link #membershipPoints(Owner)
     * membership points} banded into a number from 1 to 4 (1 for 0-1 points, 2 for 2-3, 3 for 4-5,
     * 4 for 6 or more), then held down to the owner's household ceiling
     * ({@link Owner#getMembershipLevelCap()}) captured at creation. With no ceiling the banded level
     * stands.
     */
    default Integer membershipLevel(Owner owner) {
        Integer level = MembershipLevel.forPoints(MembershipPoints.of(owner));
        Integer cap = owner == null ? null : owner.getMembershipLevelCap();
        return level != null && cap != null ? Math.min(level, cap) : level;
    }

    /**
     * The owner's segment (see {@link OwnerSegment}), formatted '&lt;TIER&gt;_&lt;AREA&gt;': TIER is
     * 'PREMIUM' when the owner's {@link #membershipLevel(Owner) membership level} is 3 or more, else
     * 'STANDARD'; AREA is 'METRO' when the owner's {@link #locality(Owner) locality} is a known region
     * (NSW, VIC or QLD), else 'REGIONAL'.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        OwnerSegment segment = OwnerSegment.of(membershipLevel(owner), locality(owner));
        return OwnerDto.OwnerSegmentEnum.valueOf(segment.name());
    }

    /**
     * The owner's preferred contact channel (see {@link ContactPreference}): 'EMAIL' when an email
     * address is present, otherwise 'PHONE'.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        ContactPreference preference = ContactPreference.of(owner);
        return preference == null ? null : OwnerDto.ContactPreferenceEnum.valueOf(preference.name());
    }

    /**
     * The owner's identity block, grouping the three version-2 identifiers derived for the owner: the
     * unified {@link MemberId memberId}, the {@link HouseholdNormalizer household id} and the derived
     * {@link IdentityKey identity key}. Grouped under a nested object so the response separates the
     * owner's stable identifiers from its descriptive fields.
     */
    default OwnerIdentityDto identity(Owner owner) {
        if (owner == null) {
            return null;
        }
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(IdentityKey.forOwner(owner));
        return identity;
    }

    /** The owner-representation version reported at the top level of the response (see
     * {@link IdentityVersion}). */
    default Integer apiVersion() {
        return IdentityVersion.VERSION;
    }

    /**
     * The owner's age band (see {@link AgeBand}): 'MINOR' under 18, 'ADULT' from 18 to 64 and
     * 'SENIOR' at 65 or older, measured from the birth date against the registration date, or
     * {@code null} when no birth date was given.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        AgeBand band = AgeBand.of(owner);
        return band == null ? null : OwnerDto.AgeBandEnum.valueOf(band.name());
    }

    /**
     * The stored E.164 telephone formatted for humans (see
     * {@link TelephoneNormalizer#toDisplay(String)}): the country code, a space, then the national
     * digits grouped in threes, e.g. '+61 412 345 678'. The raw telephone stays in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        return owner == null ? null : TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    /**
     * The owner's risk follow-up signal (see {@link RiskFlag}): true when the owner is a possible
     * duplicate, its city was over its soft capacity at creation, or its email domain is
     * disposable-adjacent; false otherwise.
     */
    default Boolean riskFlag(Owner owner) {
        return RiskFlag.of(owner);
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
