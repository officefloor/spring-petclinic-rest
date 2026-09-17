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
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.LocalityResolver;
import org.springframework.samples.petclinic.rest.validation.TelephoneFormatter;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.samples.petclinic.rest.validation.TimezoneResolver;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(formatTelephoneDisplay(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "locality", expression = "java(resolveLocality(owner))")
    @Mapping(target = "timezone", expression = "java(resolveTimezone(owner))")
    @Mapping(target = "fiscalYear", expression = "java(owner.getFiscalYear())")
    @Mapping(target = "membershipPoints", expression = "java(resolveMembershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(resolveMembershipLevel(owner))")
    @Mapping(target = "contactPreference", expression = "java(resolveContactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(resolveAgeBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(resolveOwnerSegment(owner))")
    @Mapping(target = "identityKey", expression = "java(owner.getIdentityKey())")
    @Mapping(target = "salutation", expression = "java(owner.getSalutation())")
    @Mapping(target = "selfLink", expression = "java(formatSelfLink(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /**
     * Formats the owner's canonical API self link as {@code "/api/owners/<id>"}.
     */
    default String formatSelfLink(Owner owner) {
        return "/api/owners/" + owner.getId();
    }

    /**
     * Formats the owner's stored names for display as {@code "LastName, FirstName"}.
     */
    default String formatDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats the owner's stored E.164 telephone for human display (country code, a space and the
     * national digits grouped in threes), leaving the raw {@code telephone} in E.164 form.
     */
    default String formatTelephoneDisplay(Owner owner) {
        return TelephoneFormatter.toDisplay(owner.getTelephone());
    }

    /**
     * Formats the owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    /** The upper-cased first letter of {@code name} followed by a dot. */
    private String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /**
     * Derives the owner's locality (canonical region) from its postcode via the same resolver the
     * {@code memberId} uses for its leading {@code <REGION>} segment, so the locality always agrees
     * with the region encoded in the owner's identity.
     */
    default String resolveLocality(Owner owner) {
        return LocalityResolver.resolveFromPostcode(owner.getPostcode());
    }

    /**
     * Derives the owner's IANA timezone from its {@linkplain #resolveLocality(Owner) locality}
     * (canonical region) via the fixed region-to-timezone table, or {@code null} when the locality
     * has no known timezone.
     */
    default String resolveTimezone(Owner owner) {
        return TimezoneResolver.resolve(resolveLocality(owner));
    }

    /** The minimum tenure, in fiscal years, an owner must exceed to earn the tenure membership points. */
    int TENURE_POINTS_THRESHOLD_FISCAL_YEARS = 1;

    /** The minimum household size that earns the household membership points. */
    int HOUSEHOLD_POINTS_THRESHOLD = 3;

    /**
     * Resolves the owner's membership points: starts at {@code 0}, plus {@code 2} when an email is
     * present, plus {@code 1} when the owner has no namesakes (namesakeCount is 0), plus {@code 2}
     * when the owner belongs to a household of {@value #HOUSEHOLD_POINTS_THRESHOLD} or more, plus
     * {@code 3} when the owner's tenure exceeds {@value #TENURE_POINTS_THRESHOLD_FISCAL_YEARS} fiscal year.
     */
    default Integer resolveMembershipPoints(Owner owner) {
        int points = 0;
        if (hasEmail(owner)) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= HOUSEHOLD_POINTS_THRESHOLD) {
            points += 2;
        }
        if (owner.getTenureFiscalYears() > TENURE_POINTS_THRESHOLD_FISCAL_YEARS) {
            points += 3;
        }
        return points;
    }

    /**
     * Resolves the owner's effective membership level: the level persisted (capped) on creation when
     * present, otherwise the level {@linkplain #deriveMembershipLevel derived} from its membership
     * points.
     */
    default Integer resolveMembershipLevel(Owner owner) {
        Integer stored = owner.getMembershipLevel();
        return stored != null ? stored : deriveMembershipLevel(owner);
    }

    /**
     * Derives the owner's numeric membership level from its {@linkplain #resolveMembershipPoints
     * membership points}: level {@code 1} for {@code 0-1} points, {@code 2} for {@code 2-3},
     * {@code 3} for {@code 4-5} and {@code 4} for {@code 6} or more.
     */
    default Integer deriveMembershipLevel(Owner owner) {
        int points = resolveMembershipPoints(owner);
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

    /** The minimum membership level that qualifies an owner for the {@code PREMIUM} tier. */
    int PREMIUM_TIER_MIN_LEVEL = 3;

    /**
     * Resolves the owner's segment, formatted {@code "<TIER>_<AREA>"}. TIER is {@code "PREMIUM"} when
     * the owner's {@linkplain #resolveMembershipLevel membership level} is at least
     * {@value #PREMIUM_TIER_MIN_LEVEL}, otherwise {@code "STANDARD"}. AREA is {@code "METRO"} when the
     * owner's {@linkplain #resolveLocality locality} is a known region, otherwise {@code "REGIONAL"}.
     */
    default String resolveOwnerSegment(Owner owner) {
        String tier = resolveMembershipLevel(owner) >= PREMIUM_TIER_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = LocalityResolver.isKnownRegion(resolveLocality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Resolves the owner's preferred contact channel: {@code "EMAIL"} when an email is
     * present, otherwise {@code "PHONE"}.
     */
    default String resolveContactPreference(Owner owner) {
        return hasEmail(owner) ? "EMAIL" : "PHONE";
    }

    /** Whether the owner has a non-blank email address. */
    private boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    /**
     * Resolves the owner's age band (as a string) from its birth date against its registration
     * date, or {@code null} when the owner has no birth date.
     */
    default String resolveAgeBand(Owner owner) {
        return owner.getAgeBand() == null ? null : owner.getAgeBand().name();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "normalizeAddress")
    @Mapping(target = "addressLine2", source = "addressLine2", qualifiedByName = "normalizeAddress")
    @Mapping(target = "address", expression = "java(composeAddress(ownerDto))")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Store the address in its canonical form (trimmed, whitespace-collapsed, upper-cased and with
     * common abbreviations expanded), so it is always persisted, returned and compared normalized.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Compose the owner's canonical {@code address} from the supplied fields, preferring the
     * structured form: when {@code addressLine1} is present it is the normalized {@code addressLine1}
     * with a single space and the normalized {@code addressLine2} appended when that line is present;
     * otherwise it falls back to the normalized flat {@code address}. Composing into the single
     * {@code address} field keeps everything that reads the address (household hash, postcode
     * validation, locality) consistent whichever form was supplied.
     */
    default String composeAddress(OwnerFieldsDto ownerDto) {
        String line1 = normalizeAddress(ownerDto.getAddressLine1());
        if (line1 == null || line1.isBlank()) {
            return normalizeAddress(ownerDto.getAddress());
        }
        String line2 = normalizeAddress(ownerDto.getAddressLine2());
        return (line2 == null || line2.isBlank()) ? line1 : line1 + " " + line2;
    }

    /**
     * Store the telephone in E.164 form. Validation ({@code @Telephone}) has already guaranteed
     * the raw value normalizes to a valid E.164 number.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
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
