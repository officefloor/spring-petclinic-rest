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

    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "apiVersion", expression = "java(2)")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.E164Telephone.display(owner.getTelephone()))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's salutation: the {@code title} and {@code lastName} separated by a single
     * space (e.g. {@code "DR Franklin"}), or just the {@code lastName} when no title was supplied.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Derives the owner's age band from {@code birthDate}, computed against the
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} when 18-64,
     * {@code SENIOR} when 65 or older. Returns {@code null} when no birth date was supplied
     * (or the registration date is absent, e.g. seed data), so the field is simply absent.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int years = java.time.Period.between(birthDate, registrationDate).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * The second-level labels of the known disposable email domains (the blocklist enforced by
     * {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwnerFields}:
     * {@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}). An email whose
     * domain carries one of these labels is <em>disposable-adjacent</em>.
     */
    java.util.Set<String> DISPOSABLE_ADJACENT_LABELS = java.util.Set.of(
        "mailinator", "tempmail", "guerrillamail");

    /**
     * Derives the owner's risk flag: {@code true} when any of these hold, otherwise {@code false}:
     * the owner is a possible duplicate ({@code possibleDuplicate}), its email domain is
     * {@link #disposableAdjacentEmail(Owner) disposable-adjacent}, or its city is over its soft
     * capacity (the approaching-capacity {@code capacityWarning}).
     */
    default boolean riskFlag(Owner owner) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean overSoftCapacity = Boolean.TRUE.equals(owner.getCapacityWarning());
        return possibleDuplicate || overSoftCapacity || disposableAdjacentEmail(owner);
    }

    /**
     * Whether the owner's email domain is disposable-adjacent: it carries one of the
     * {@link #DISPOSABLE_ADJACENT_LABELS known disposable labels} as one of its dot-separated
     * labels (e.g. {@code mailinator.net} or {@code sub.tempmail.co}). The exact blocklisted
     * domains never reach here (they are rejected with 400 at validation), so this catches the
     * look-alikes around them. Returns {@code false} when no email is present.
     */
    default boolean disposableAdjacentEmail(Owner owner) {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_ADJACENT_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The fixed city-to-region table: any city not listed derives locality {@code UNKNOWN}.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * The fixed region-to-timezone table: maps a region code to its IANA timezone name. Any region
     * not listed (e.g. {@code UNKNOWN}) has no timezone.
     */
    java.util.Map<String, String> REGION_TIMEZONE = java.util.Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's IANA timezone from the {@link #locality(Owner) locality/region} via the
     * fixed {@link #REGION_TIMEZONE} table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane). Returns {@code null} when the region is not one of these, so the
     * field is simply absent.
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * Derives the owner's user-facing locality: the plain region code
     * ({@link org.springframework.samples.petclinic.rest.function.owner.OwnerRegion}, postcode range
     * first — NSW 2000-2099, VIC 3000-3099, QLD 4000-4099 — then the city table, then {@code UNKNOWN}).
     * This is the same base region the identity is built from, but WITHOUT the identifiers' {@code 'V2'}
     * tag, so a Sydney owner reads back locality {@code "NSW"} even though the memberId's internal
     * region is {@code "NSWV2"}. Deriving it directly (rather than from the memberId prefix) keeps the
     * locality free of the identifier tag.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerRegion.of(owner);
    }

    /**
     * Groups the owner's version-2 identifiers under the response's nested {@code identity} object:
     * the {@code memberId} and {@code householdId} assigned during create and the {@code identityKey}
     * derived on read ({@link org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey}).
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity(Owner owner) {
        org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity =
            new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(
            org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey.of(owner));
        return identity;
    }

    /**
     * Derives the owner's segment, formatted {@code <TIER>_<AREA>}. TIER is {@code PREMIUM} when the
     * owner's {@link #membershipLevel(Owner) membershipLevel} is 3 or more, otherwise
     * {@code STANDARD}. AREA is {@code METRO} when the {@link #locality(Owner) locality} is a known
     * region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        return ownerSegment(membershipLevel(owner), locality(owner));
    }

    /**
     * As {@link #ownerSegment(Owner)}, but for a supplied membership level and locality — so a
     * responder can derive the segment from the household-capped level actually returned.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(locality) ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.valueOf(tier + "_" + area);
    }

    /**
     * Derives the owner's membership points. Starts at 0; adds 2 when an email address is present;
     * adds 1 when the owner has no namesakes ({@code namesakeCount} is 0); adds 2 for a household of
     * 3 or more members ({@code householdMemberCount} is 3 or greater); adds 3 when the owner's
     * tenure spans more than one elapsed fiscal year. A newly created owner has zero tenure, so a
     * new owner never earns the tenure points.
     */
    default int membershipPoints(Owner owner) {
        int householdMemberCount = owner.getHouseholdMemberCount() == null ? 0 : owner.getHouseholdMemberCount();
        return membershipPointsForHousehold(owner, householdMemberCount);
    }

    /**
     * As {@link #membershipPoints(Owner)}, but scores the household-of-3-or-more bonus against the
     * supplied {@code householdMemberCount} rather than the owner's stored one. Used to evaluate an
     * existing household member's points at the household's <em>current</em> size (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.HouseholdLevelCap}).
     */
    default int membershipPointsForHousehold(Owner owner, int householdMemberCount) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (householdMemberCount >= 3) {
            points += 2;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && (fiscalYearOf(java.time.LocalDate.now()) - fiscalYearOf(registrationDate)) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Maps the owner's {@link #membershipPoints(Owner) membershipPoints} to a numeric level: 1 for
     * 0-1 points, 2 for 2-3 points, 3 for 4-5 points, 4 for 6 or more points.
     */
    default int membershipLevel(Owner owner) {
        return levelForPoints(membershipPoints(owner));
    }

    /**
     * As {@link #membershipLevel(Owner)}, but evaluated with the supplied {@code householdMemberCount}
     * (see {@link #membershipPointsForHousehold(Owner, int)}).
     */
    default int membershipLevelForHousehold(Owner owner, int householdMemberCount) {
        return levelForPoints(membershipPointsForHousehold(owner, householdMemberCount));
    }

    /** Maps membership points to the numeric level band. */
    default int levelForPoints(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The fiscal year (1 July - 30 June) that {@code date} falls in, named by the calendar year in
     * which it ends: a date in July-December belongs to the fiscal year ending the following
     * calendar year, a date in January-June to the fiscal year ending the same calendar year. So
     * 2026-08-11 is fiscal year 2027 and 2026-03-11 is fiscal year 2026.
     */
    default int fiscalYearOf(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derives the owner's fiscal year, formatted {@code FY<YY>} where YY is the last two digits of
     * the {@link #fiscalYearOf(java.time.LocalDate) fiscal year} of the business-day-adjusted
     * registrationDate (e.g. {@code FY27}). Returns {@code null} when no registration date is present
     * (e.g. seed data), so the field is simply absent.
     */
    default String fiscalYear(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", Math.floorMod(fiscalYearOf(registrationDate), 100));
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
