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
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials",
        expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "bulkSignupWarning",
        expression = "java(Boolean.TRUE.equals(owner.getBulkSignupWarning()))")
    @Mapping(target = "capacityWarning",
        expression = "java(Boolean.TRUE.equals(owner.getCapacityWarning()))")
    @Mapping(target = "possibleDuplicate",
        expression = "java(Boolean.TRUE.equals(owner.getPossibleDuplicate()))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerTelephones.toDisplay(owner.getTelephone()))")
    @Mapping(target = "apiVersion", expression = "java(2)")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's version-2 identifiers into the nested {@code identity} object:
     * the stored {@code memberId} and {@code householdId}, and the derived
     * {@code identityKey} (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity}).
     * Each is derived with the version-2 algorithm that mixes in the fixed {@code V2} tag.
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity(Owner owner) {
        org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity =
            new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity
            .identityKey(owner));
        return identity;
    }

    /**
     * Derives the owner's segment, formatted {@code <TIER>_<AREA>}. TIER is
     * {@code PREMIUM} when the membershipLevel is 3 or more, otherwise
     * {@code STANDARD}. AREA is {@code METRO} when the owner's locality is a known
     * region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        Integer level = owner.getMembershipLevel();
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        boolean metro = org.springframework.samples.petclinic.rest.function.owner.OwnerRegion
            .isKnownRegion(locality(owner));
        String area = metro ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * Whether the owner should be flagged for risk review. True when any of these hold:
     * the owner is a possible duplicate ({@code possibleDuplicate}); the email domain is
     * disposable-adjacent (shares a known throwaway provider's base name under a different
     * TLD or as a subdomain, see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerEmails});
     * or the city is over its soft capacity (the capacity-warning threshold,
     * {@code capacityWarning}). False otherwise.
     */
    default boolean riskFlag(Owner owner) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean overSoftCapacity = Boolean.TRUE.equals(owner.getCapacityWarning());
        boolean disposableAdjacent = org.springframework.samples.petclinic.rest.function.owner.OwnerEmails
            .isDisposableAdjacent(org.springframework.samples.petclinic.rest.function.owner.OwnerEmails
                .domainOf(owner.getEmail()));
        return possibleDuplicate || overSoftCapacity || disposableAdjacent;
    }

    /**
     * The canonical API path to this owner, formatted {@code /api/owners/<id>}. Null
     * until the owner has been assigned an id.
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * Derives the owner's age band from the {@code birthDate} measured against the
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to
     * 64 inclusive and {@code SENIOR} at 65 or over. Null when no birth date is present.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int years = java.time.Period.between(birthDate, reference).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Composes the owner's salutation from the optional title and the last name:
     * {@code <title> <lastName>} when a title is present (non-null and not blank),
     * or just the last name when no title was supplied.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title != null && !title.isBlank()) {
            return title + " " + owner.getLastName();
        }
        return owner.getLastName();
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email
     * address is present (non-null and not blank), otherwise {@code PHONE}.
     */
    default String contactPreference(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * Derives the owner's locality (region) from the owner's {@code memberId}
     * identity ({@code <REGION><FY><HASH8><CHK>}): the locality is the {@code REGION}
     * segment of that identity. When the member id has not been assigned yet, it falls
     * back to deriving the region directly (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerRegion}).
     */
    default String locality(Owner owner) {
        String region = org.springframework.samples.petclinic.rest.function.owner.OwnerRegion
            .regionFromMemberId(owner.getMemberId());
        return region != null ? region
            : org.springframework.samples.petclinic.rest.function.owner.OwnerRegion.regionOf(owner);
    }

    /**
     * Derives the owner's IANA timezone name from the owner's locality/region via the
     * pinned region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane). Null when the region has no mapped timezone.
     */
    default String timezone(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerRegion
            .timezoneOfRegion(locality(owner));
    }

    /**
     * Derives the owner's fiscal year, formatted {@code FY<YY>}, from the
     * (business-day-adjusted) registration date. The fiscal year starts on 1 July and
     * is named by the calendar year it ends in. Null when no registration date is set.
     */
    default String fiscalYear(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        return org.springframework.samples.petclinic.rest.function.owner.OwnerFiscalYear
            .fiscalYearLabel(owner.getRegistrationDate());
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
