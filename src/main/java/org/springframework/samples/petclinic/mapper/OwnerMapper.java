package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.IdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.AgeBand;
import org.springframework.samples.petclinic.util.DisposableEmailDomains;
import org.springframework.samples.petclinic.util.FiscalYears;
import org.springframework.samples.petclinic.util.IdentityVersion;
import org.springframework.samples.petclinic.util.MembershipLevels;
import org.springframework.samples.petclinic.util.OwnerSegment;
import org.springframework.samples.petclinic.util.Postcodes;
import org.springframework.samples.petclinic.util.Telephones;
import org.springframework.samples.petclinic.util.Timezones;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "apiVersion", expression = "java(apiVersion())")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's canonical API path, '/api/owners/' followed by its id, or null before an id is assigned. */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /** Format an owner's name as 'LastName, FirstName' for display. */
    default String displayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Compose the owner's salutation as the title, a space and the last name (e.g. 'DR Who'),
     * or just the last name when no title was supplied.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        return (title == null || title.isBlank()) ? owner.getLastName() : title + " " + owner.getLastName();
    }

    /** Upper-cased first letters of first and last name, dot-separated with a trailing dot (e.g. 'J.S.'). */
    default String initials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private static String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /** The owner-identity payload version (always {@link IdentityVersion#VERSION}). */
    default Integer apiVersion() {
        return IdentityVersion.VERSION;
    }

    /**
     * Group the owner's server-derived identifiers — member id, identity key and household id —
     * under the nested identity object of the response.
     */
    default IdentityDto identity(Owner owner) {
        IdentityDto identity = new IdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(owner.getIdentityKey());
        identity.setHouseholdId(owner.getHouseholdId());
        return identity;
    }

    /**
     * Derive the owner's fiscal year 'FY&lt;YY&gt;' from the registration date (see
     * {@link FiscalYears}). Returns null when no registration date has been resolved.
     */
    default String fiscalYear(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? null
                : FiscalYears.labelFor(FiscalYears.yearOfCentury(registrationDate));
    }

    /**
     * Derive the owner's locality (canonical region) as the plain region code of the postcode, or
     * 'UNKNOWN' when the postcode is absent, malformed or in no known range. This is a user-facing
     * field, not an identifier, so it never carries the identity version tag.
     */
    default String locality(Owner owner) {
        return Postcodes.regionCode(owner.getPostcode());
    }

    /**
     * Derive the IANA timezone name for the owner's locality (canonical region) from the fixed
     * region-to-timezone table, or null when the locality has no known timezone.
     */
    default String timezone(Owner owner) {
        return Timezones.of(locality(owner));
    }

    /** Derive the owner's membership points as at the current date. */
    default Integer membershipPoints(Owner owner) {
        return MembershipLevels.pointsOf(owner, LocalDate.now());
    }

    /**
     * Derive the owner's numeric membership level (1 to 4) from the current membership points,
     * capped at the owner's household membership-level cap when one applies.
     */
    default Integer membershipLevel(Owner owner) {
        return MembershipLevels.levelOf(owner, LocalDate.now());
    }

    /** Derive the owner's preferred contact method: EMAIL when an email is present, otherwise PHONE. */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        return owner.hasEmail() ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derive the owner's age band from the birth date as at the registration date, or null
     * when no birth date was supplied.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        AgeBand band = AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate());
        return band == null ? null : OwnerDto.AgeBandEnum.valueOf(band.name());
    }

    /**
     * Derive the owner's market segment '&lt;TIER&gt;_&lt;AREA&gt;' from the derived membership
     * level and locality.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        return OwnerDto.OwnerSegmentEnum.valueOf(OwnerSegment.of(membershipLevel(owner), locality(owner)).name());
    }

    /** Format the owner's stored E.164 telephone for humans (e.g. '+61 412 345 678'). */
    default String telephoneDisplay(Owner owner) {
        return Telephones.forDisplay(owner.getTelephone());
    }

    /**
     * Derive the aggregate risk flag: true when the owner is a possible duplicate, its email
     * domain is disposable-adjacent, or its city is over the soft capacity warning threshold;
     * otherwise false.
     */
    default boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || DisposableEmailDomains.isDisposableAdjacent(DisposableEmailDomains.domainOf(owner.getEmail()));
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
