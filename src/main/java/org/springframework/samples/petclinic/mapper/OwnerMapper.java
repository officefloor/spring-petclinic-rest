package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Known disposable email domains whose registrable base name (the label immediately before the
     * public suffix) marks an owner's email domain as <em>disposable-adjacent</em>. These are the same
     * domains the create endpoint refuses outright; a domain that is not an exact match but shares one
     * of these base names — a subdomain of a disposable domain, or the same base under a different TLD —
     * is disposable-adjacent.
     */
    java.util.Set<String> DISPOSABLE_DOMAIN_BASES = java.util.Set.of("mailinator", "tempmail", "guerrillamail");

    /**
     * Derives the owner's {@code riskFlag}: {@code true} when any of these hold — the owner is a possible
     * duplicate ({@code possibleDuplicate} is true), the owner's email domain is disposable-adjacent (see
     * {@link #isDisposableAdjacent(String)}), or the owner's city is over its soft capacity
     * ({@code capacityWarning} is true, i.e. the city already held 40 or more owners at creation). Otherwise
     * {@code false}. Returns {@code null} when the owner is absent.
     */
    default @Nullable Boolean riskFlag(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean overSoftCapacity = Boolean.TRUE.equals(owner.getCapacityWarning());
        boolean disposableAdjacent = isDisposableAdjacent(owner.getEmail());
        return possibleDuplicate || overSoftCapacity || disposableAdjacent;
    }

    /**
     * Whether an email's domain is disposable-adjacent: its registrable base name (the label immediately
     * before the final TLD, compared case-insensitively) matches the base name of a known disposable
     * domain. This flags a subdomain of a disposable domain (e.g. {@code sub.mailinator.com}) and the same
     * base under a different TLD (e.g. {@code mailinator.net}), as well as an exact disposable domain.
     * Returns {@code false} when the email is absent or carries no dotted domain.
     */
    default boolean isDisposableAdjacent(@Nullable String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(java.util.Locale.ROOT);
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        String base = labels[labels.length - 2];
        return DISPOSABLE_DOMAIN_BASES.contains(base);
    }

    /**
     * Derives the owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}. TIER is
     * {@code PREMIUM} when membershipLevel is 3 or more, otherwise {@code STANDARD}. AREA is
     * {@code METRO} when the locality is a known region ({@code NSW}, {@code VIC} or {@code QLD}),
     * otherwise {@code REGIONAL}. Returns {@code null} when the owner is absent.
     */
    default OwnerDto.@Nullable OwnerSegmentEnum ownerSegment(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        Integer level = membershipLevel(owner);
        boolean premium = level != null && level >= 3;
        String region = locality(owner);
        boolean metro = "NSW".equals(region) || "VIC".equals(region) || "QLD".equals(region);
        String tier = premium ? "PREMIUM" : "STANDARD";
        String area = metro ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * Derives the owner's {@code selfLink}, the canonical relative URL of the owner formatted as
     * {@code '/api/owners/' + id}. Returns {@code null} when the owner or its id is absent.
     */
    default @Nullable String selfLink(@Nullable Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * The fiscal year in which a date falls, labelled by the calendar year in which the fiscal year
     * ends. The fiscal year starts on 1 July, so a date on or after 1 July belongs to the fiscal year
     * ending the following calendar year, and a date before 1 July belongs to the fiscal year ending
     * that calendar year.
     */
    private static int fiscalYearEnding(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derives the owner's {@code fiscalYear} from the {@code FY} segment of its memberId, formatted as
     * {@code 'FY<YY>'} where YY are the two fiscal-year digits carried in the memberId (immediately
     * after the region segment), i.e. the last two digits of the calendar year in which the fiscal
     * year ends (the fiscal year starts on 1 July). Returns {@code null} when the owner has no
     * memberId or the memberId carries no fiscal-year segment.
     */
    default @Nullable String fiscalYear(@Nullable Owner owner) {
        String region = memberIdRegion(owner);
        if (region == null) {
            return null;
        }
        String memberId = owner.getMemberId();
        if (memberId.length() < region.length() + 2) {
            return null;
        }
        return "FY" + memberId.substring(region.length(), region.length() + 2);
    }

    /**
     * Derives the owner's {@code ageBand} from its birthDate relative to the registrationDate:
     * {@code MINOR} when under 18, {@code ADULT} when 18 to 64, and {@code SENIOR} when 65 or older.
     * Returns {@code null} when the owner has no birthDate or no registrationDate.
     */
    default OwnerDto.@Nullable AgeBandEnum ageBand(@Nullable Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's duplicate-detection {@code identityKey}: the lower-case hex SHA-256 of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, with an empty segment
     * for an absent telephone or email. This is the same key the create endpoint uses to reject an
     * owner whose whole key equals an existing owner's.
     */
    default @Nullable String identityKey(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        return org.springframework.samples.petclinic.util.IdentityKeys.identityKey(
            owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email is present,
     * otherwise {@code PHONE}.
     */
    default OwnerDto.@Nullable ContactPreferenceEnum contactPreference(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's locality from its memberId: the leading {@code REGION} segment of the
     * {@code memberId} ({@code '<REGION><FY><HASH8><CHK>'}), which is the region code derived from
     * the postcode on create and is the run of non-digit characters before the fiscal-year digits.
     * Returns the canonical region string, or {@code 'UNKNOWN'} when the owner has no memberId.
     */
    default @Nullable String locality(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = memberIdRegion(owner);
        return region == null ? "UNKNOWN" : region;
    }

    /**
     * The leading {@code REGION} segment of the owner's {@code memberId}, i.e. the run of non-digit
     * characters before the 2-digit fiscal year. Returns {@code null} when the owner has no memberId
     * or the memberId has no leading region segment.
     */
    private static @Nullable String memberIdRegion(@Nullable Owner owner) {
        String memberId = owner == null ? null : owner.getMemberId();
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && !Character.isDigit(memberId.charAt(i))) {
            i++;
        }
        return i == 0 ? null : memberId.substring(0, i);
    }

    /**
     * Derives the owner's {@code timezone} as an IANA name from its locality/region using the fixed
     * region-to-timezone table: {@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * and {@code QLD -> Australia/Brisbane}. Returns {@code null} when the region is not in the table.
     */
    default @Nullable String timezone(@Nullable Owner owner) {
        String region = locality(owner);
        if (region == null) {
            return null;
        }
        return switch (region) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }

    /**
     * Returns the owner's membership points. Starting at 0, it adds 2 when an email is present, 1
     * when namesakeCount is 0, 2 when the owner belongs to a household of at least 3 members, and 3
     * when tenure spans at least one elapsed fiscal year since the registrationDate (the fiscal year
     * starts on 1 July). Because a newly created owner shares the current fiscal year it never earns
     * the tenure points.
     */
    default @Nullable Integer membershipPoints(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        boolean largeHousehold = owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3;
        if (largeHousehold) {
            points += 2;
        }
        if (hasQualifyingTenure(owner)) {
            points += 3;
        }
        return points;
    }

    /**
     * Returns the owner's numeric membership level. When a membership level has been persisted on the
     * owner (the household-capped value assigned on create) that stored value is returned; otherwise the
     * level is derived from membershipPoints via {@link #derivedMembershipLevel(Owner)}.
     */
    default @Nullable Integer membershipLevel(@Nullable Owner owner) {
        if (owner != null && owner.getMembershipLevel() != null) {
            return owner.getMembershipLevel();
        }
        return derivedMembershipLevel(owner);
    }

    /**
     * Derives the owner's numeric membership level from 1 to 4 from membershipPoints, ignoring any
     * stored value: level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more points.
     */
    default @Nullable Integer derivedMembershipLevel(@Nullable Owner owner) {
        Integer points = membershipPoints(owner);
        if (points == null) {
            return null;
        }
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
     * Whether the owner's tenure spans at least one elapsed fiscal year, i.e. the current fiscal year
     * is later than the fiscal year of its registrationDate (the fiscal year starts on 1 July).
     * Returns {@code false} when the registrationDate is absent (or in the future, which shares the
     * current fiscal year), so a newly created owner has no qualifying tenure.
     */
    default boolean hasQualifyingTenure(@Nullable Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return false;
        }
        int elapsedFiscalYears = fiscalYearEnding(java.time.LocalDate.now())
            - fiscalYearEnding(owner.getRegistrationDate());
        return elapsedFiscalYears >= 1;
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for humans: the country code, a space, then
     * the national digits grouped in threes separated by spaces, e.g. {@code '+61 412 345 678'} for a
     * stored {@code '+61412345678'}. The country code is the single digit for the {@code +1} and
     * {@code +7} plans and two digits otherwise. Returns the raw value unchanged when it is not a
     * {@code '+'}-prefixed digit string, or {@code null} when absent.
     */
    default @Nullable String telephoneDisplay(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.matches("\\+\\d+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength = (digits.startsWith("1") || digits.startsWith("7")) ? 1 : 2;
        countryCodeLength = Math.min(countryCodeLength, digits.length());
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /**
     * Composes the owner's salutation as {@code 'title lastName'} when a title is present,
     * or just the {@code lastName} when no title is given.
     */
    default @Nullable String salutation(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        if (title == null || title.isEmpty()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default @Nullable String displayName(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default @Nullable String initials(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String firstName = owner.getFirstName();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(Character.toUpperCase(firstName.charAt(0))).append('.');
        }
        String lastName = owner.getLastName();
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(Character.toUpperCase(lastName.charAt(0))).append('.');
        }
        return sb.toString();
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "memberId", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "membershipLevel", ignore = true)
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
