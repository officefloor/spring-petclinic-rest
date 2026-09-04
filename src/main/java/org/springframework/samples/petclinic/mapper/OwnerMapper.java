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

    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerMembership.points(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerMembership.level(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "apiVersion", expression = "java(2)")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's {@code riskFlag}: true when any of these hold — the owner is a possible duplicate
     * ({@code possibleDuplicate} true), the email domain is disposable-adjacent (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerEmail#isDisposableAdjacent}),
     * or the city is over its soft capacity ({@code capacityWarning} true); otherwise false.
     */
    default boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || org.springframework.samples.petclinic.rest.function.owner.OwnerEmail
                        .isDisposableAdjacent(owner.getEmail());
    }

    /**
     * The owner's canonical URL, formatted {@code '/api/owners/<id>'} where id is the owner's id.
     * Null when the owner has no id yet.
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * The owner's stored E.164 {@code telephone} formatted for humans: the '+' country code, a space,
     * then the national digits grouped in threes (e.g. {@code '+61 412 345 678'}). Null when no
     * telephone is recorded.
     */
    default String telephoneDisplay(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone.toDisplay(
                owner.getTelephone());
    }

    /**
     * The owner's age band, computed from {@code birthDate} against {@code registrationDate}:
     * {@code MINOR} (under 18), {@code ADULT} (18-64) or {@code SENIOR} (65+). Null when no
     * birth date is recorded.
     */
    default String ageBand(Owner owner) {
        if (owner.getBirthDate() == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
                ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(owner.getBirthDate(), reference).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The owner's version-2 identity block, grouping the {@code memberId}, {@code householdId} and
     * {@code identityKey} under one nested object. The memberId and householdId are the stored
     * version-2 identifiers; the identityKey is derived here (see {@link #identityKey(Owner)}).
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity(Owner owner) {
        org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity =
                new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(identityKey(owner));
        return identity;
    }

    /**
     * The owner's derived {@code identityKey} = SHA-256 hex over
     * {@code 'V2' + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, the single
     * key all duplicate detection is expressed through (version-2 derived).
     */
    default String identityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(
                owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * The owner's segment, formatted {@code '<TIER>_<AREA>'}: one of {@code PREMIUM_METRO},
     * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}. TIER is
     * {@code PREMIUM} when membershipLevel is 3 or more, otherwise {@code STANDARD}. AREA is
     * {@code METRO} when the locality is a known region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
     */
    default String ownerSegment(Owner owner) {
        String tier = org.springframework.samples.petclinic.rest.function.owner.OwnerMembership
                .level(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String locality = locality(owner);
        boolean metro = "NSW".equals(locality) || "VIC".equals(locality) || "QLD".equals(locality);
        return tier + "_" + (metro ? "METRO" : "REGIONAL");
    }

    /**
     * The owner's preferred contact channel: {@code EMAIL} when an email is present, otherwise
     * {@code PHONE}.
     */
    default String contactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's salutation: the honorific {@code title}, a single space, then the {@code lastName}
     * when a title is present, or just the {@code lastName} when no title is given.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's locality (region), read from the REGION prefix of the memberId
     * ({@code <REGION><FY><HASH8><CHK>}); see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerRegion#regionOf}.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerRegion.regionOf(
                owner.getMemberId());
    }

    /**
     * The IANA timezone name for the owner's locality (region), from the fixed region-to-timezone
     * table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane); see
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerRegion#timezoneOf}.
     * Null when the region is absent or unknown.
     */
    default String timezone(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerRegion.timezoneOf(
                owner.getMemberId());
    }

    /**
     * The owner's fiscal year, formatted {@code FY<YY>} where YY is the two-digit FY segment of the
     * owner's {@code memberId} — the fiscal year the business-day-adjusted registrationDate falls in
     * (fiscal year starting 1 July), e.g. {@code FY27}. Null when the memberId is absent.
     */
    default String fiscalYear(Owner owner) {
        String fiscalYearCode = org.springframework.samples.petclinic.rest.function.owner.MemberId
                .fiscalYearCodeOf(owner.getMemberId());
        return fiscalYearCode == null ? null : "FY" + fiscalYearCode;
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
