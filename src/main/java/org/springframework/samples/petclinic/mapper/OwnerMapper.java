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

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "apiVersion", expression = "java(apiVersion())")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The version of the owner identity contract every rendered owner reports as {@code apiVersion}:
     * always {@code 2}, the version under which the derived identifiers are grouped in the nested
     * {@code identity} object rather than carried at the top level.
     */
    default Integer apiVersion() {
        return 2;
    }

    /**
     * Groups the owner's version-2 derived identifiers into the nested {@code identity} object of the
     * response: its unified {@code memberId}, its shared {@code householdId} and its derived
     * {@code identityKey} (see {@link Owner#getIdentityKey()}). These three are carried here rather
     * than at the top level under the version-2 contract. Returns {@code null} for a {@code null}
     * owner, leaving {@code identity} absent from the response.
     */
    default OwnerIdentityDto identity(Owner owner) {
        if (owner == null) {
            return null;
        }
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(owner.getIdentityKey());
        return identity;
    }

    /**
     * Derives the owner's {@code selfLink}: the canonical URI path of the owner, formed as
     * {@code '/api/owners/'} followed by its id. Returns {@code null} when the owner has no id
     * yet, leaving {@code selfLink} absent from the response.
     */
    default String selfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Derives the owner's age band as the DTO enum, rendering the owner's derived age band (see
     * {@link Owner#getAgeBand()}). Returns {@code null} when the owner has no derivable age band
     * (no birthDate or no registrationDate), leaving {@code ageBand} absent from the response.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        if (owner == null || owner.getAgeBand() == null) {
            return null;
        }
        return OwnerDto.AgeBandEnum.valueOf(owner.getAgeBand());
    }

    /**
     * Derives the owner's preferred contact channel: {@code 'EMAIL'} when an email address
     * is present (non-null and non-blank), otherwise {@code 'PHONE'}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.hasEmail(owner.getEmail())
            ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's segment as {@code '<TIER>_<AREA>'}. TIER is {@code 'PREMIUM'} when the
     * owner's derived {@code membershipLevel} (see {@link Owner#getMembershipLevel()}) is 3 or more,
     * otherwise {@code 'STANDARD'}. AREA is {@code 'METRO'} when the owner's locality is a known
     * region ({@code NSW}, {@code VIC} or {@code QLD}; see {@link #localityRegion(Owner)}), otherwise
     * {@code 'REGIONAL'}.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        Integer level = owner.getMembershipLevel();
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        String region = localityRegion(owner);
        String area = (region != null && REGION_TIMEZONES.containsKey(region)) ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.valueOf(tier + "_" + area);
    }

    /**
     * Derives the owner's locality from the region its identity carries (see
     * {@link #localityRegion(Owner)}). Returns that region string, or {@code 'UNKNOWN'} when the
     * owner has no known region.
     */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = localityRegion(owner);
        return region != null ? region : "UNKNOWN";
    }

    /**
     * Resolves the region an owner's {@link #locality(Owner)} is derived from: the owner's own region
     * (see {@link Owner#getRegion()}), reported only once the owner has been assigned its identity
     * (its {@code memberId} is present). Only the known regions ({@code NSW}, {@code VIC},
     * {@code QLD}) — the ones carrying a timezone entry — are returned; the sentinel {@code 'UNKNOWN'}
     * region (used when the owner has no known region) and a missing member id both resolve to
     * {@code null}. Reading the region straight from the owner keeps the user-facing locality
     * independent of how the owner's identifiers encode their region, so the identifier encoding can
     * change without disturbing this plain region. Keeping the region source in its own method leaves
     * {@link #locality(Owner)} owning only the {@code null -> 'UNKNOWN'} rendering, so where the
     * region itself comes from can change without disturbing that rendering.
     *
     * @param owner the owner whose locality region is resolved, never {@code null}
     * @return the region string, or {@code null} when the owner has no known region
     */
    default String localityRegion(Owner owner) {
        if (owner.getMemberId() == null) {
            return null;
        }
        String region = owner.getRegion();
        return region != null && REGION_TIMEZONES.containsKey(region) ? region : null;
    }

    /**
     * Fixed region-to-timezone table: the IANA timezone name each known region maps to
     * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}). A region absent from this table has no known timezone.
     */
    java.util.Map<String, String> REGION_TIMEZONES = java.util.Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's IANA timezone from the region its identity carries (see
     * {@link #localityRegion(Owner)}) using the fixed region-to-timezone table
     * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}). Returns {@code null} when the owner has no known region
     * or the region has no timezone entry, leaving {@code timezone} absent from the response.
     */
    default String timezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = localityRegion(owner);
        return region == null ? null : REGION_TIMEZONES.get(region);
    }

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default String displayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Returns the owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default String initials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
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
