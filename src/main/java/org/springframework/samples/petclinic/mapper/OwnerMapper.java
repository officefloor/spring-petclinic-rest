package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.MemberIdGenerator;
import org.springframework.samples.petclinic.rest.validation.OwnerRiskResolver;
import org.springframework.samples.petclinic.util.AgeBandResolver;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.samples.petclinic.util.MembershipLevelCalculator;
import org.springframework.samples.petclinic.util.OwnerSegmentResolver;
import org.springframework.samples.petclinic.util.TelephoneDisplayFormatter;
import org.springframework.samples.petclinic.util.TimezoneResolver;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(formatSelfLink(owner))")
    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "salutation", expression = "java(formatSalutation(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(formatTelephoneDisplay(owner))")
    @Mapping(target = "fiscalYear", expression = "java(formatFiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(computeMembershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(computeMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "timezone", expression = "java(formatTimezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(formatIdentityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(formatAgeBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(formatOwnerSegment(owner))")
    @Mapping(target = "riskFlag", expression = "java(computeRiskFlag(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Builds an owner's canonical self link from its id, formatted
     * {@code "/api/owners/<id>"}. Returns {@code null} when the owner has no id yet.
     */
    default String formatSelfLink(Owner owner) {
        if (owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Builds an owner's display name from the stored names, formatted as
     * {@code "LastName, FirstName"}.
     */
    default String formatDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Builds an owner's salutation from the stored title and last name: the title
     * followed by a single space and the last name when a title is present, or just
     * the last name when no title is given.
     */
    default String formatSalutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Builds an owner's initials from the stored names as the upper-cased first
     * letters of the first and last name, dot-separated with a trailing dot,
     * e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Formats an owner's stored E.164 telephone for humans, delegating to
     * {@link TelephoneDisplayFormatter}. The raw {@code telephone} stays in E.164 form.
     */
    default String formatTelephoneDisplay(Owner owner) {
        return TelephoneDisplayFormatter.display(owner.getTelephone());
    }

    /**
     * Derives an owner's fiscal year from the {@code FY} segment embedded in its member id,
     * formatted {@code 'FY<YY>'}, delegating to {@link MemberIdGenerator}. Returns {@code null}
     * when no member id is present.
     */
    default String formatFiscalYear(Owner owner) {
        return MemberIdGenerator.fiscalYearOf(owner.getMemberId());
    }

    /**
     * Computes an owner's membership points from its stored fields, delegating to
     * {@link MembershipLevelCalculator}.
     */
    default int computeMembershipPoints(Owner owner) {
        return MembershipLevelCalculator.membershipPoints(owner);
    }

    /**
     * Computes an owner's effective numeric membership level from its stored fields, delegating to
     * {@link MembershipLevelCalculator}. Any membership-level cap recorded at creation is applied.
     */
    default int computeMembershipLevel(Owner owner) {
        return MembershipLevelCalculator.effectiveMembershipLevel(owner);
    }

    /**
     * Derives an owner's locality (canonical region) from the region segment embedded in its
     * member id, yielding {@code "UNKNOWN"} when the id is absent or carries no region.
     */
    default String formatLocality(Owner owner) {
        return MemberIdGenerator.regionOf(owner.getMemberId());
    }

    /**
     * Derives an owner's timezone as an IANA name from its locality (canonical region),
     * delegating to {@link TimezoneResolver}. Returns {@code null} when the region has no
     * mapped timezone.
     */
    default String formatTimezone(Owner owner) {
        return TimezoneResolver.timezoneOf(formatLocality(owner));
    }

    /**
     * Derives an owner's preferred contact channel from its stored fields:
     * {@code "EMAIL"} when an email is present, otherwise {@code "PHONE"}.
     */
    default String formatContactPreference(Owner owner) {
        return owner.hasEmail() ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's identity key from its stored fields, delegating to {@link IdentityKey}.
     * This is the same key used to detect duplicate owners on create.
     */
    default String formatIdentityKey(Owner owner) {
        return IdentityKey.of(owner);
    }

    /**
     * Derives an owner's age band from its birth date measured against its registration
     * date, delegating to {@link AgeBandResolver}. Returns {@code null} when no birth date
     * is present.
     */
    default String formatAgeBand(Owner owner) {
        return AgeBandResolver.bandOf(owner.getBirthDate(), owner.getRegistrationDate());
    }

    /**
     * Derives an owner's marketing segment ({@code '<TIER>_<AREA>'}) from its effective
     * membership level and locality, delegating to {@link OwnerSegmentResolver}.
     */
    default String formatOwnerSegment(Owner owner) {
        return OwnerSegmentResolver.segmentOf(computeMembershipLevel(owner), formatLocality(owner));
    }

    /**
     * Derives an owner's risk flag from its stored fields, delegating to {@link OwnerRiskResolver}.
     */
    default boolean computeRiskFlag(Owner owner) {
        return OwnerRiskResolver.isAtRisk(owner);
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "householdId", ignore = true)
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
