package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Region;
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

    @Mapping(target = "identity", ignore = true)
    @Mapping(target = "apiVersion", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /** The owner-identity contract version carried by every owner response. */
    int API_VERSION = 2;

    /**
     * Groups the owner's version-2 identifiers under the nested {@code identity} object
     * ({@code memberId}, {@code householdId}, {@code identityKey}) and stamps the top-level
     * {@code apiVersion} ({@value #API_VERSION}). These three identifiers are no longer exposed at
     * the top level; the {@code identityKey} is the owner's domain-computed
     * {@link Owner#getIdentityKey()}.
     */
    @AfterMapping
    default void addIdentity(Owner owner, @MappingTarget OwnerDto ownerDto) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(owner.getIdentityKey());
        ownerDto.setIdentity(identity);
        ownerDto.setApiVersion(API_VERSION);
    }

    /**
     * Fills the presentation-only {@code displayName} as {@code "LastName, FirstName"}
     * from the stored names, once MapStruct has mapped the remaining fields.
     */
    @AfterMapping
    default void addDisplayName(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setDisplayName(owner.getLastName() + ", " + owner.getFirstName());
    }

    /**
     * Fills the presentation-only {@code salutation} from the owner's domain-computed
     * {@link Owner#getSalutation()} — the title and lastName separated by a space, or just the
     * lastName when the owner has no title.
     */
    @AfterMapping
    default void addSalutation(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setSalutation(owner.getSalutation());
    }

    /**
     * Fills the presentation-only {@code initials} as the upper-cased first letters of
     * firstName and lastName, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    @AfterMapping
    default void addInitials(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setInitials(
            Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".");
    }

    /**
     * Fills the presentation-only {@code fiscalYear} from the owner's domain-computed
     * {@link Owner#getFiscalYear()} — {@code 'FY<YY>'} derived from the business-day-adjusted
     * registrationDate — leaving it null when no registration date is available.
     */
    @AfterMapping
    default void addFiscalYear(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setFiscalYear(owner.getFiscalYear());
    }

    /**
     * Fills the {@code membershipPoints} and {@code membershipLevel} from the owner's
     * domain-computed {@link Owner#getMembershipPoints()} and {@link Owner#getMembershipLevel()}.
     */
    @AfterMapping
    default void addMembershipLevel(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setMembershipPoints(owner.getMembershipPoints());
        ownerDto.setMembershipLevel(owner.getMembershipLevel());
    }

    /**
     * Fills the {@code contactPreference} as {@code EMAIL} when the owner has an email
     * address, otherwise {@code PHONE}.
     */
    @AfterMapping
    default void addContactPreference(Owner owner, @MappingTarget OwnerDto ownerDto) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        ownerDto.setContactPreference(
            hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE);
    }

    /**
     * Fills the presentation-only {@code locality} as the {@code <REGION>} component of the owner's
     * {@code memberId} (see {@link Region#regionOfMemberId(String)}) — the region-and-hash
     * identity is the single source of truth — or {@code 'UNKNOWN'} when no memberId is assigned.
     */
    @AfterMapping
    default void addLocality(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setLocality(Region.regionOfMemberId(owner.getMemberId()));
    }

    /**
     * Fills the presentation-only {@code timezone} as the IANA timezone name derived from the
     * owner's locality/region (the {@code <REGION>} component of its {@code memberId}) via the
     * fixed region-to-timezone table (see {@link Region#timezoneForRegion(String)}), or {@code null}
     * when the region has no mapped timezone.
     */
    @AfterMapping
    default void addTimezone(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setTimezone(Region.timezoneForRegion(Region.regionOfMemberId(owner.getMemberId())));
    }

    /**
     * Fills the presentation-only {@code ownerSegment} from the owner's domain-computed
     * {@link Owner#getOwnerSegment()} — {@code '<TIER>_<AREA>'} where TIER reflects the
     * membershipLevel and AREA reflects whether the locality is a known region.
     */
    @AfterMapping
    default void addOwnerSegment(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setOwnerSegment(OwnerDto.OwnerSegmentEnum.fromValue(owner.getOwnerSegment()));
    }

    /**
     * Fills the {@code ageBand} from the owner's domain-computed {@link Owner#getAgeBand()}
     * (derived from birthDate measured against registrationDate), leaving it null when no
     * birth date is available.
     */
    @AfterMapping
    default void addAgeBand(Owner owner, @MappingTarget OwnerDto ownerDto) {
        String ageBand = owner.getAgeBand();
        if (ageBand != null) {
            ownerDto.setAgeBand(OwnerDto.AgeBandEnum.fromValue(ageBand));
        }
    }

    /**
     * Fills the presentation-only {@code selfLink} as {@code '/api/owners/'} followed by the
     * owner's id, leaving it null when the owner has no id assigned yet.
     */
    @AfterMapping
    default void addSelfLink(Owner owner, @MappingTarget OwnerDto ownerDto) {
        if (owner.getId() != null) {
            ownerDto.setSelfLink("/api/owners/" + owner.getId());
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
