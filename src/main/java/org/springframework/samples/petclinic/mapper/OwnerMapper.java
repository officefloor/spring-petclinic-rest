package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", source = "owner", qualifiedByName = "formatSelfLink")
    @Mapping(target = "salutation", source = "owner", qualifiedByName = "formatSalutation")
    @Mapping(target = "displayName", source = "owner", qualifiedByName = "formatDisplayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "formatInitials")
    @Mapping(target = "telephoneDisplay", source = "owner", qualifiedByName = "formatTelephoneDisplay")
    @Mapping(target = "memberId", source = "owner", qualifiedByName = "formatMemberId")
    @Mapping(target = "fiscalYear", source = "owner", qualifiedByName = "formatFiscalYear")
    @Mapping(target = "membershipPoints", source = "owner", qualifiedByName = "formatMembershipPoints")
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "formatMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "formatLocality")
    @Mapping(target = "timezone", source = "owner", qualifiedByName = "formatTimezone")
    @Mapping(target = "ownerSegment", source = "owner", qualifiedByName = "formatOwnerSegment")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "formatContactPreference")
    @Mapping(target = "ageBand", source = "owner", qualifiedByName = "formatAgeBand")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "riskFlag", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Formats an owner's self-link, as required by the API's read-only {@code selfLink}
     * field: the canonical API path {@code '/api/owners/'} followed by the owner's id.
     * Returns {@code null} when the owner has no id yet.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatSelfLink")
    default String formatSelfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Formats an owner's salutation, as required by the API's read-only {@code salutation}
     * field: the owner's title and last name separated by a single space (e.g. {@code 'DR Who'})
     * when a title is present, or just the last name when no title was supplied.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatSalutation")
    default String formatSalutation(Owner owner) {
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
     * Formats an owner's name as {@code 'LastName, FirstName'} from the stored first and
     * last names, as required by the API's read-only {@code displayName} field.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatDisplayName")
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code 'J.S.'}), as required by the
     * API's read-only {@code initials} field.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatInitials")
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    /**
     * Formats an owner's stored E.164 telephone for human display, as required by the API's
     * read-only {@code telephoneDisplay} field: the country code (with its leading {@code '+'}),
     * a space, then the national digits grouped in threes (e.g. {@code '+61 412 345 678'}). The
     * raw {@code telephone} field keeps its canonical E.164 form. Returns {@code null} when no
     * telephone is present.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatTelephoneDisplay")
    default String formatTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    /**
     * Formats an owner's member id, as required by the API's read-only {@code memberId} field:
     * the stored {@code '<REGION><FY><HASH8><CHK>'} identifier (the region code, the 2-digit
     * fiscal year, the 8 upper-hex characters of the SHA-256 digest over the normalized telephone
     * concatenated with the last name, and a single Luhn check digit over the digits of
     * {@code <REGION><FY><HASH8>}), de-duplicated with a trailing {@code '-<n>'} on collision.
     * Returns {@code null} when the member id is absent.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatMemberId")
    default String formatMemberId(Owner owner) {
        return owner == null ? null : owner.getMemberId();
    }

    /**
     * Formats an owner's fiscal year as {@code 'FY<YY>'}, as required by the API's read-only
     * {@code fiscalYear} field: the {@code FY} segment carried by the owner's {@code memberId}
     * (the two digits following the region code), so the fiscal year is read from the same member
     * id that encodes it. The fiscal year starts on 1 July and {@code YY} is the last two digits
     * of the calendar year in which that fiscal year begins (so a registration date of
     * {@code 2026-10-02} yields {@code 'FY26'} and {@code 2026-03-02} yields {@code 'FY25'}). When
     * no member id is present (e.g. an owner that predates it), the fiscal year is computed
     * directly from the business-day-adjusted registration date, which yields the same value the
     * member id would have carried. Returns {@code null} when neither is available.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatFiscalYear")
    default String formatFiscalYear(Owner owner) {
        if (owner == null) {
            return null;
        }
        String fy = memberIdFiscalYear(owner.getMemberId());
        if (fy != null) {
            return "FY" + fy;
        }
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d", FiscalYear.startYear(owner.getRegistrationDate()) % 100);
    }

    /**
     * Computes an owner's membership points, as required by the API's read-only
     * {@code membershipPoints} field: starting at 0, plus 2 when an email address is present,
     * plus 1 when the owner has no namesakes ({@code namesakeCount} is 0), plus 2 for a household
     * of 3 or more members, plus 3 when the owner's tenure spans at least one elapsed fiscal year
     * (the registration date's fiscal year precedes the current fiscal year, the fiscal year
     * starting on 1 July).
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatMembershipPoints")
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
                && FiscalYear.startYear(LocalDate.now()) - FiscalYear.startYear(owner.getRegistrationDate()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives an owner's membership level, as required by the API's read-only
     * {@code membershipLevel} field: a value from 1 to 4 mapped from the owner's
     * {@link #formatMembershipPoints membership points} — level 1 for 0-1 points, level 2 for
     * 2-3 points, level 3 for 4-5 points, and level 4 for 6 or more points.
     *
     * <p>The result is then capped by the owner's {@link Owner#getMembershipLevelCap()
     * membershipLevelCap}, when present: a new owner's level cannot exceed one above the highest
     * membership level among the household members that already existed when it was created, so the
     * returned level is the lower of the derived level and that stored cap. When no cap applies (no
     * existing household member on creation) the derived level stands.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName} (and when called directly, e.g.
     * to record the membership level on the create audit line).
     */
    @Named("formatMembershipLevel")
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
        return (cap != null && cap < level) ? cap : level;
    }

    /**
     * Derives an owner's locality, as required by the API's read-only {@code locality} field:
     * the {@code REGION} segment of the owner's {@code '<REGION><FY><HASH8><CHK>'} member id, so
     * the locality is read from the same region-and-hash identity that the member id encodes.
     * When no member id is present (e.g. an owner that predates it), the region is resolved
     * directly from the postcode (falling back to the city-to-region table, else {@code 'UNKNOWN'}),
     * which yields the same region the member id would have carried.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatLocality")
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = memberIdRegion(owner.getMemberId());
        if (region != null) {
            return region;
        }
        return LocalityResolver.resolve(owner.getCity(), owner.getPostcode());
    }

    /**
     * Returns the fixed-length body of a {@code memberId} — the {@code '<REGION><FY><HASH8><CHK>'}
     * portion before any {@code '-<n>'} de-duplication suffix — or {@code null} when the member id
     * is absent or too short to contain the mandatory trailing {@code FY<2> + HASH8<8> + CHK<1>}
     * (11 characters) after a non-empty region. The region segment never contains a {@code '-'},
     * so the de-duplication suffix is split off at the first hyphen.
     */
    private static String memberIdBody(String memberId) {
        if (memberId == null) {
            return null;
        }
        int dash = memberId.indexOf('-');
        String body = dash >= 0 ? memberId.substring(0, dash) : memberId;
        return body.length() > 11 ? body : null;
    }

    /** The {@code REGION} segment of a member id (everything before the trailing 11 characters). */
    private static String memberIdRegion(String memberId) {
        String body = memberIdBody(memberId);
        return body == null ? null : body.substring(0, body.length() - 11);
    }

    /** The two-digit {@code FY} segment of a member id (the two characters following the region). */
    private static String memberIdFiscalYear(String memberId) {
        String body = memberIdBody(memberId);
        return body == null ? null : body.substring(body.length() - 11, body.length() - 9);
    }

    /**
     * Derives an owner's timezone, as required by the API's read-only {@code timezone} field:
     * the IANA timezone name for the owner's {@link #formatLocality locality}/region, via the
     * fixed region-to-timezone table ({@code NSW -> Australia/Sydney},
     * {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}). Returns
     * {@code null} when the region has no mapped timezone (e.g. {@code 'UNKNOWN'}).
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatTimezone")
    default String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return LocalityResolver.timezone(formatLocality(owner));
    }

    /**
     * Derives an owner's segment, as required by the API's read-only {@code ownerSegment} field:
     * {@code '<TIER>_<AREA>'}, one of {@code 'PREMIUM_METRO'}, {@code 'PREMIUM_REGIONAL'},
     * {@code 'STANDARD_METRO'} or {@code 'STANDARD_REGIONAL'}. {@code TIER} is {@code 'PREMIUM'}
     * when the owner's {@link #formatMembershipLevel membership level} is 3 or more, otherwise
     * {@code 'STANDARD'}. {@code AREA} is {@code 'METRO'} when the owner's {@link #formatLocality
     * locality} is a known region (NSW, VIC or QLD), otherwise {@code 'REGIONAL'} — a locality of
     * {@code 'UNKNOWN'} being exactly the case where no known region resolved.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatOwnerSegment")
    default String formatOwnerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        Integer level = formatMembershipLevel(owner);
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        String area = LocalityResolver.isKnownRegion(formatLocality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Derives an owner's preferred contact channel, as required by the API's read-only
     * {@code contactPreference} field: {@code 'EMAIL'} when an email address is present,
     * otherwise {@code 'PHONE'}.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatContactPreference")
    default String formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getEmail() != null && !owner.getEmail().isEmpty() ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's age band, as required by the API's read-only {@code ageBand} field:
     * the owner's age in whole years at the registration date, mapped to {@code 'MINOR'} (under
     * 18), {@code 'ADULT'} (18 to 64) or {@code 'SENIOR'} (65 or over). Returns {@code null} when
     * no birth date was supplied (so the field is absent), or when no registration date is yet
     * known against which to compute the age.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatAgeBand")
    default String formatAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    private static String initial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "addressLine1", ignore = true)
    @Mapping(target = "addressLine2", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    @Mapping(target = "title", source = "title", qualifiedByName = "mapTitle")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Unwraps the owner-fields' optional honorific {@code title} enum into the stored
     * {@code String} form (its canonical value, e.g. {@code 'DR'}), or {@code null} when no
     * title was supplied. This is the single place the inbound title conversion lives: it runs
     * on the create path via {@link #toOwner(OwnerFieldsDto)} and is called directly on the
     * update path, keeping both consistent.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("mapTitle")
    default String mapTitle(OwnerFieldsDto.TitleEnum title) {
        return title == null ? null : title.getValue();
    }

    /**
     * Sets an owner's structured and composed address fields from the supplied owner-fields,
     * normalizing each. This is the single place the structured-field normalization rule lives: it
     * runs automatically after {@link #toOwner(OwnerFieldsDto)} via {@code @AfterMapping}, and is
     * called directly on the update path to keep both create and update consistent.
     *
     * <p>{@code addressLine1} and {@code addressLine2} are stored in their normalized forms and
     * {@code address} is set to the {@link #composeAddress composed} effective address.
     */
    @AfterMapping
    default void applyAddressFields(OwnerFieldsDto ownerDto, @MappingTarget Owner owner) {
        if (ownerDto == null) {
            return;
        }
        owner.setAddressLine1(normalizeAddress(ownerDto.getAddressLine1()));
        owner.setAddressLine2(normalizeAddress(ownerDto.getAddressLine2()));
        owner.setAddress(composeAddress(ownerDto));
    }

    /**
     * Composes the effective, normalized postal address for an owner from the supplied fields,
     * preferring the structured form over the flat one. When a non-blank {@code addressLine1} is
     * present, the result is the normalized {@code addressLine1} with the normalized
     * {@code addressLine2} appended after a single space when an {@code addressLine2} is present;
     * otherwise it is the normalized flat {@code address}. Each part is normalized through
     * {@link #normalizeAddress} (trim, collapse whitespace, upper-case, expand {@code ST -> STREET},
     * {@code RD -> ROAD}, {@code AVE -> AVENUE}).
     */
    default String composeAddress(OwnerFieldsDto ownerDto) {
        if (ownerDto == null) {
            return null;
        }
        String line1 = normalizeAddress(ownerDto.getAddressLine1());
        if (line1 != null && !line1.isEmpty()) {
            String line2 = normalizeAddress(ownerDto.getAddressLine2());
            return (line2 != null && !line2.isEmpty()) ? line1 + " " + line2 : line1;
        }
        return normalizeAddress(ownerDto.getAddress());
    }

    /**
     * Normalizes a postal address into a canonical form: surrounding whitespace is trimmed and
     * internal runs of whitespace collapsed to a single space, the value is upper-cased and common
     * abbreviations are expanded as whole tokens ({@code ST -> STREET}, {@code RD -> ROAD},
     * {@code AVE -> AVENUE}). So {@code '  12  main  st '} is stored and returned as
     * {@code '12 MAIN STREET'}.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is applied
     * only where referenced via {@code qualifiedByName}.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalizes a telephone number into canonical E.164 form: a leading {@code '+'} and country
     * code are kept when present, otherwise country code {@code '+61'} is assumed and a single
     * leading {@code '0'} is dropped from the national digits; spaces, dashes and brackets are
     * stripped and the result must contain 8 to 15 digits after the {@code '+'}. So
     * {@code '0412 345 678'} is stored and returned as {@code '+61412345678'}.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is applied
     * only where referenced via {@code qualifiedByName} (and when called directly, e.g. on update).
     *
     * @throws org.springframework.samples.petclinic.util.InvalidTelephoneException if the number
     *         cannot form a valid E.164 value (handled as a 400 Bad Request).
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
    }

    /**
     * Normalizes an email address by lower-casing it, so it is stored and returned in a
     * canonical form. The schema-level {@code format: email} on {@code OwnerFieldsDto.email}
     * guarantees any value present is a syntactically valid address.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName} (and when called directly,
     * e.g. on update).
     */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
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
