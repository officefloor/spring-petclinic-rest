package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.E164Telephone;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.BusinessDays;
import org.springframework.samples.petclinic.util.Regions;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(formatSelfLink(owner))")
    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "salutation", expression = "java(formatSalutation(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(formatTelephoneDisplay(owner))")
    @Mapping(target = "fiscalYear", expression = "java(formatFiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(formatMembershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(formatMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "timezone", expression = "java(formatTimezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(formatBulkSignupWarning(owner))")
    @Mapping(target = "capacityWarning", expression = "java(formatCapacityWarning(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(formatPossibleDuplicate(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(formatAgeBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(formatOwnerSegment(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derive an owner's marketing segment, formatted {@code "<TIER>_<AREA>"}. The tier is
     * {@code "PREMIUM"} when the owner's {@code membershipLevel} (see
     * {@link #formatMembershipLevel(Owner)}) is {@code 3} or more, otherwise {@code "STANDARD"}.
     * The area is {@code "METRO"} when the owner's locality (see {@link #formatLocality(Owner)}) is
     * a known region (NSW, VIC or QLD), otherwise {@code "REGIONAL"}. Returns one of
     * {@code "PREMIUM_METRO"}, {@code "PREMIUM_REGIONAL"}, {@code "STANDARD_METRO"} or
     * {@code "STANDARD_REGIONAL"}; {@code null} only when the owner is {@code null}.
     */
    default String formatOwnerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        Integer level = formatMembershipLevel(owner);
        String tier = level != null && level >= 3 ? "PREMIUM" : "STANDARD";
        String area = Regions.isKnownRegion(formatLocality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Resolve an owner's possible-duplicate flag, coalescing a {@code null} stored value
     * (e.g. for owners created before the flag existed) to {@code false} so the response
     * always carries an explicit boolean.
     */
    default Boolean formatPossibleDuplicate(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getPossibleDuplicate());
    }

    /**
     * Derive an owner's age band from its {@code birthDate}, measured as the owner's age in whole
     * years on its {@code registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"} from 18
     * through 64, and {@code "SENIOR"} at 65 and over. Returns {@code null} when the owner, its
     * birth date or its registration date is absent, since the band cannot be derived without both
     * dates.
     */
    default String formatAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * Derive an owner's preferred contact channel from its stored contact details:
     * {@code "EMAIL"} when a (non-blank) email address is present, otherwise {@code "PHONE"}.
     */
    default String formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Resolve an owner's bulk-signup warning flag, coalescing a {@code null} stored value
     * (e.g. for owners created before the flag existed) to {@code false} so the response
     * always carries an explicit boolean.
     */
    default Boolean formatBulkSignupWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Resolve an owner's capacity-warning flag, coalescing a {@code null} stored value
     * (e.g. for owners created before the flag existed) to {@code false} so the response
     * always carries an explicit boolean.
     */
    default Boolean formatCapacityWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getCapacityWarning());
    }

    /**
     * Derive an owner's locality from its {@code memberId}: the leading {@code <REGION>} segment
     * (the run of letters before the 2-digit fiscal year), which is the region code derived from
     * the owner's postcode. For an owner that has not yet been assigned a member id, the region is
     * derived directly from the postcode (see {@link Regions#forPostcode}), yielding the same value
     * the id would carry. Returns {@code null} only when the owner itself is {@code null}.
     */
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = memberIdRegion(owner.getMemberId());
        if (region != null) {
            return region;
        }
        return Regions.forPostcode(owner.getPostcode());
    }

    /**
     * The leading {@code <REGION>} segment of a {@code memberId}: its maximal prefix of letters,
     * which precedes the 2-digit fiscal year. Returns {@code null} when the member id is
     * {@code null} (so callers can fall back to deriving the region from the postcode).
     *
     * <p>Declared {@code private} so MapStruct does not pick it up as an automatic {@code String ->
     * String} conversion and apply it to every string property.
     */
    private String memberIdRegion(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return memberId.substring(0, i);
    }

    /**
     * Derive an owner's timezone as the IANA name mapped from its locality/region via a fixed
     * region-to-timezone table (see {@link Regions#timezoneForRegion}): NSW -> Australia/Sydney,
     * VIC -> Australia/Melbourne, QLD -> Australia/Brisbane. Returns {@code null} when the owner
     * is {@code null} or its region is not in the table.
     */
    default String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Regions.timezoneForRegion(formatLocality(owner));
    }

    /**
     * Compute an owner's membership points. Points start at {@code 0} and gain: {@code 2} when an
     * email address is present; {@code 1} when the owner has no namesakes (its
     * {@code namesakeCount} is {@code 0}); {@code 2} for a household of three or more members (the
     * owner plus its pets); and {@code 3} when the owner's tenure (the number of fiscal years
     * elapsed since its {@code registrationDate}'s fiscal year) exceeds {@code 1}. A null or absent
     * namesake count is treated as not being zero, so it contributes nothing.
     */
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        if (hasEmail) {
            points += 2;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (uniqueName) {
            points += 1;
        }
        int householdSize = 1 + (owner.getPets() == null ? 0 : owner.getPets().size());
        if (householdSize >= 3) {
            points += 2;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
            && BusinessDays.fiscalYearEndYear(LocalDate.now())
                - BusinessDays.fiscalYearEndYear(registrationDate) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Determine an owner's membership level, a number from {@code 1} to {@code 4} derived from its
     * membership points (see {@link #formatMembershipPoints(Owner)}): level {@code 1} for
     * {@code 0}-{@code 1} points, {@code 2} for {@code 2}-{@code 3}, {@code 3} for {@code 4}-{@code 5}
     * and {@code 4} for {@code 6} or more.
     *
     * <p>The level is then capped by the owner's {@code membershipLevelCap} when one is present (see
     * {@link Owner#getMembershipLevelCap()}): a new owner's level may not exceed one above the
     * current maximum level among its existing household members, so the returned value is the
     * smaller of the points-derived level and the cap. A {@code null} cap means no household ceiling
     * applies and the points-derived level is returned unchanged.
     */
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = formatMembershipPoints(owner);
        int level;
        if (points <= 1) {
            level = 1;
        }
        else if (points <= 3) {
            level = 2;
        }
        else if (points <= 5) {
            level = 3;
        }
        else {
            level = 4;
        }
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && cap < level) {
            return cap;
        }
        return level;
    }

    /**
     * Format an owner's fiscal year as {@code "FY<YY>"}, where {@code YY} is the 2-digit fiscal
     * year. The value is read from the {@code <FY>} segment of the owner's {@code memberId} (the
     * two digits immediately after the leading region segment), which is the fiscal year the
     * owner's (business-day-adjusted) {@code registrationDate} falls in. The fiscal year starts on
     * 1 July and is named by the calendar year in which it ends, so a registration date of
     * {@code 2026-07-01} is {@code "FY27"} while {@code 2026-06-30} is {@code "FY26"}. For an owner
     * with no member id yet, it falls back to deriving the fiscal year directly from the
     * registration date. Returns {@code null} when the owner, or both its member id and
     * registration date, are absent.
     */
    default String formatFiscalYear(Owner owner) {
        if (owner == null) {
            return null;
        }
        String memberId = owner.getMemberId();
        if (memberId != null) {
            int start = memberIdRegion(memberId).length();
            if (start + 2 <= memberId.length()) {
                return "FY" + memberId.substring(start, start + 2);
            }
        }
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d", BusinessDays.fiscalYearEndYear(owner.getRegistrationDate()) % 100);
    }

    /**
     * Format an owner's stored E.164 telephone for humans as the country code, a space, then the
     * national digits grouped in threes, e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"} (see {@link E164Telephone#toDisplay}). The raw {@code telephone}
     * is left in E.164 form. Returns {@code null} when the owner or its telephone is absent.
     */
    default String formatTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return E164Telephone.toDisplay(owner.getTelephone());
    }

    /**
     * Compose an owner's salutation as its {@code title} followed by a single space and the
     * last name (e.g. {@code "DR Franklin"}). When no title was supplied (null or blank) the
     * salutation is just the last name. Returns {@code null} when the owner is {@code null}.
     */
    default String formatSalutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        if (title == null || title.trim().isEmpty()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Build a link to an owner's own resource, formed as {@code "/api/owners/"} followed by the
     * owner's id. Returns {@code null} when the owner or its id is absent, since the link cannot
     * be formed without an id.
     */
    default String formatSelfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Format an owner's name as {@code "LastName, FirstName"} from the stored names.
     */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Format an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "membershipLevelCap", ignore = true)
    @Mapping(target = "address", expression = "java(composeAddress(ownerDto))")
    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "normalizeAddress")
    @Mapping(target = "addressLine2", source = "addressLine2", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalize a postal address for storage into its canonical form (see
     * {@link AddressNormalizer}), so it is persisted and returned the same way regardless of
     * the formatting supplied by the caller. {@code @Named} keeps this out of MapStruct's
     * automatic method selection, so it only runs where a mapping explicitly references it via
     * {@code qualifiedByName}.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Compose the single stored/returned {@code address} from whichever address fields the request
     * supplies, preferring the structured form. When a non-blank {@code addressLine1} is present the
     * composed value is the normalized {@code addressLine1} (see {@link AddressNormalizer}), with a
     * single space and the normalized {@code addressLine2} appended when {@code addressLine2} is
     * present; otherwise it falls back to the normalized flat {@code address}. This keeps everything
     * that reads the stored address consistent regardless of which form the caller used.
     */
    default String composeAddress(OwnerFieldsDto ownerDto) {
        if (ownerDto == null) {
            return null;
        }
        String line1 = ownerDto.getAddressLine1();
        if (line1 != null && !line1.trim().isEmpty()) {
            String composed = AddressNormalizer.normalize(line1);
            String line2 = ownerDto.getAddressLine2();
            if (line2 != null && !line2.trim().isEmpty()) {
                composed = composed + " " + AddressNormalizer.normalize(line2);
            }
            return composed;
        }
        return AddressNormalizer.normalize(ownerDto.getAddress());
    }

    /**
     * Normalize a telephone number for storage into E.164 form (see {@link E164Telephone}).
     * The requirement that a valid E.164 number can be formed is enforced separately by
     * {@code TelephoneValidation}, so for already-validated input this returns the E.164
     * string; it is idempotent, so re-normalizing a stored value yields the same value.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return E164Telephone.normalize(telephone);
    }

    /**
     * Normalize an email address for storage by lower-casing it, so it is persisted and
     * returned in a canonical form regardless of the casing supplied by the caller.
     * Syntactic validity is enforced separately by the {@code @Email} constraint on the DTO.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase();
    }

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
