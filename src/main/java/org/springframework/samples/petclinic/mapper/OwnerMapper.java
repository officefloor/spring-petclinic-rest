package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.E164PhoneNumbers;
import org.springframework.samples.petclinic.util.IdentityKeyUtils;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
            + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "membershipPoints",
        expression = "java(toMembershipPoints(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(toLocality(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(toOwnerSegment(owner))")
    @Mapping(target = "timezone",
        expression = "java(toTimezone(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(toContactPreference(owner))")
    @Mapping(target = "identityKey",
        expression = "java(toIdentityKey(owner))")
    @Mapping(target = "ageBand",
        expression = "java(toAgeBand(owner))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(toTelephoneDisplay(owner))")
    @Mapping(target = "salutation",
        expression = "java(toSalutation(owner))")
    @Mapping(target = "fiscalYear",
        expression = "java(toFiscalYear(owner))")
    @Mapping(target = "selfLink",
        expression = "java(toSelfLink(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derive an owner's canonical self link: the resource path {@code '/api/owners/<id>'} where
     * {@code <id>} is the owner's id. Returns {@code null} when the owner has no id yet (the field is
     * then absent from the DTO).
     */
    default String toSelfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * The fiscal year that the given date falls in, where the fiscal year starts on 1 July and is
     * identified by the calendar year in which it ends. A date on or after 1 July belongs to the
     * fiscal year ending in the following calendar year (e.g. 2026-08-01 -&gt; 2027), while a date
     * before 1 July belongs to the fiscal year ending in the same calendar year (e.g. 2026-03-01 -&gt;
     * 2026). This is the single source of truth for the fiscal year used both when the create endpoint
     * bakes the FY segment into the member id (see {@code OwnerRestControllerV1.memberIdFor}) and when
     * the DTO's {@code fiscalYear} falls back to the registration date for a legacy record.
     *
     * @param date the date to resolve, on a business day
     * @return the calendar year in which that date's fiscal year ends
     */
    static int fiscalYear(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= Month.JULY.getValue() ? year + 1 : year;
    }

    /**
     * Derive an owner's fiscal year, formatted {@code 'FY<YY>'} where YY is the two-digit fiscal-year
     * segment carried by the {@code memberId} (see {@link #memberIdBase(String)}), e.g. {@code 'FY27'}.
     * The fiscal year therefore moves with the owner's member id. When an owner has no (well-formed)
     * member id yet (e.g. legacy records) it falls back to deriving the fiscal year directly from the
     * business-day-adjusted {@code registrationDate} (see {@link #fiscalYear(LocalDate)}), and returns
     * {@code null} when neither is present (the field is then absent from the DTO).
     */
    default String toFiscalYear(Owner owner) {
        String base = memberIdBase(owner.getMemberId());
        if (base != null) {
            return "FY" + memberIdFyDigits(base);
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? null : String.format("FY%02d", fiscalYear(registrationDate) % 100);
    }

    /**
     * Derive an owner's salutation: the {@code title}, a single space, and the {@code lastName}
     * (e.g. {@code "DR Franklin"}). When no title was supplied (null or blank) the salutation is
     * just the last name.
     */
    default String toSalutation(Owner owner) {
        String lastName = owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Derive an owner's age band from the {@code birthDate} relative to the {@code registrationDate}:
     * 'MINOR' when under 18, 'ADULT' when 18 to 64, and 'SENIOR' when 65 or older. Returns
     * {@code null} when no birth date was supplied (the band is then absent from the DTO). When the
     * registration date is missing (e.g. legacy records) the current date is used as the reference.
     */
    default OwnerDto.AgeBandEnum toAgeBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Format an owner's stored E.164 {@code telephone} for humans: the country code, a space, then the
     * national digits grouped in threes (e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}).
     * The raw {@code telephone} property is left untouched in E.164 form. Returns the value unchanged
     * when it is {@code null} or not in {@code '+'}-prefixed E.164 form.
     *
     * <p>The recognized dialling codes come from {@link E164PhoneNumbers}, the same table the create
     * endpoint validates against, so the two paths cannot diverge. When a number carries no recognized
     * code, a two-digit country code is assumed (the most common length).
     */
    default String toTelephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String code = E164PhoneNumbers.countryCode(digits)
            .filter(c -> digits.length() > c.length())
            .orElse(digits.length() > 2 ? digits.substring(0, 2) : digits);
        String national = digits.substring(code.length());
        StringBuilder sb = new StringBuilder("+").append(code);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /**
     * The fixed trailing-segment widths of a member id base {@code '<REGION><FY><HASH8><CHK>'}: FY is 2
     * digits, HASH8 is 8 hex characters and CHK is 1 digit, so the last {@link #MEMBER_ID_SUFFIX_WIDTH}
     * characters are the {@code <FY><HASH8><CHK>} suffix and REGION is the variable-width remainder in
     * front of it. This is the single place the layout is encoded; the position arithmetic in
     * {@link #memberIdRegion(String)} and {@link #memberIdFyDigits(String)} derives from these, and it
     * must stay in step with how {@code OwnerRestControllerV1.memberIdFor} assembles the id.
     */
    int FY_WIDTH = 2;
    int HASH8_WIDTH = 8;
    int CHK_WIDTH = 1;
    int MEMBER_ID_SUFFIX_WIDTH = FY_WIDTH + HASH8_WIDTH + CHK_WIDTH;

    /**
     * The {@code '<REGION><FY><HASH8><CHK>'} base of a member id, dropping any {@code '-<n>'} collision
     * suffix, or {@code null} when the owner has no (well-formed) member id yet — i.e. one long enough to
     * carry a non-empty REGION in front of the fixed {@link #MEMBER_ID_SUFFIX_WIDTH}-character suffix.
     * Use {@link #memberIdRegion(String)} and {@link #memberIdFyDigits(String)} to decode its segments.
     */
    private static String memberIdBase(String memberId) {
        if (memberId == null) {
            return null;
        }
        int dash = memberId.indexOf('-');
        String base = dash >= 0 ? memberId.substring(0, dash) : memberId;
        return base.length() > MEMBER_ID_SUFFIX_WIDTH ? base : null;
    }

    /**
     * The leading {@code <REGION>} segment of a (non-null, well-formed) member id base: everything
     * before the fixed trailing {@code <FY><HASH8><CHK>} suffix.
     */
    private static String memberIdRegion(String base) {
        return base.substring(0, base.length() - MEMBER_ID_SUFFIX_WIDTH);
    }

    /**
     * The two-digit {@code <FY>} segment of a (non-null, well-formed) member id base: the characters
     * immediately preceding the trailing HASH8 and CHK segments.
     */
    private static String memberIdFyDigits(String base) {
        int fyEnd = base.length() - HASH8_WIDTH - CHK_WIDTH;
        return base.substring(fyEnd - FY_WIDTH, fyEnd);
    }

    /**
     * Derive an owner's duplicate-detection identity key: the lower-case SHA-256 hex digest (64
     * characters) of the normalized E.164 telephone, the lower-cased email (empty when none) and the
     * American Soundex code of the last name, joined with {@code '|'}. Two owners are duplicates only
     * when their whole identity keys are equal.
     *
     * <p>This is the single source of truth for the key: it is both returned on the DTO and used by
     * the create endpoint's 409 duplicate check, so the value clients see is exactly the value that
     * governs rejection. The "email present" rule reuses {@link #hasEmail(Owner)}, and the Soundex and
     * hashing come from {@link IdentityKeyUtils}, the same helpers the soft-match uses.
     */
    default String toIdentityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = hasEmail(owner) ? owner.getEmail() : "";
        String soundex = IdentityKeyUtils.soundex(owner.getLastName());
        return IdentityKeyUtils.sha256Hex(telephone + "|" + email + "|" + soundex);
    }

    /**
     * Whether the owner has an email address present (non-null and non-empty). Single source of truth
     * for the "is an email present" rule shared by the derived fields below.
     */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty();
    }

    /**
     * Derive an owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String toContactPreference(Owner owner) {
        return hasEmail(owner) ? "EMAIL" : "PHONE";
    }

    /**
     * Derive an owner's membership points: start at 0; add 2 when an email address is present; add 1
     * when the owner has no namesakes (namesakeCount is 0); add 2 when the owner belongs to a
     * household of 3 or more (see {@link #hasHouseholdBonus(Owner)}); add 3 when the owner's tenure
     * is at least one elapsed fiscal year (see {@link #hasTenureBonus(Owner)}). Because a newly
     * created owner has zero tenure it never earns the tenure points, so a new owner with an email
     * and no namesakes in a single-member household scores 3 points.
     */
    default Integer toMembershipPoints(Owner owner) {
        int points = 0;
        if (hasEmail(owner)) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (hasHouseholdBonus(owner)) {
            points += 2;
        }
        if (hasTenureBonus(owner)) {
            points += 3;
        }
        return points;
    }

    /**
     * Whether the owner belongs to a household of 3 or more members. The household size is the count
     * of owners sharing the owner's household id, frozen at creation (see the create endpoint).
     * Returns {@code false} when no household size is present (e.g. legacy records).
     */
    default boolean hasHouseholdBonus(Owner owner) {
        return owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3;
    }

    /**
     * Derive an owner's numeric membership level (1 to 4) from its membership points (see
     * {@link #toMembershipPoints(Owner)}): level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6
     * or more.
     *
     * <p>The level is then capped at the owner's {@code membershipLevelCap} when one is present: a new
     * owner's membership level cannot exceed one above the current maximum membership level among their
     * existing household members, so the cap (frozen at creation, see the create endpoint) is the
     * ceiling applied here. When no cap is present (e.g. an owner created with no existing household
     * member, or a legacy record) the uncapped level stands.
     */
    default Integer toMembershipLevel(Owner owner) {
        int level = uncappedMembershipLevel(owner);
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /**
     * Derive an owner's numeric membership level (1 to 4) from its membership points (see
     * {@link #toMembershipPoints(Owner)}) before any household ceiling is applied: level 1 for 0-1
     * points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more.
     */
    default Integer uncappedMembershipLevel(Owner owner) {
        int points = toMembershipPoints(owner);
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
     * Whether the owner's tenure is at least one elapsed fiscal year, i.e. the fiscal year of the
     * current date is later than the fiscal year of the {@code registrationDate} (fiscal years start
     * on 1 July; see {@link #fiscalYear(LocalDate)}). A brand-new owner (registered in the current
     * fiscal year) has zero elapsed fiscal years and so never qualifies. Returns {@code false} when
     * no registration date is present.
     */
    default boolean hasTenureBonus(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        return fiscalYear(LocalDate.now()) - fiscalYear(registrationDate) >= 1;
    }

    /**
     * Fixed city-to-region table used as the fall-back when deriving an owner's locality.
     */
    Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Fixed region-to-inclusive-postcode-range table used to derive an owner's locality from the
     * postcode: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. Each entry is {@code {low, high}}
     * inclusive.
     */
    Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derive an owner's locality (region) from the region-and-hash member id: the leading
     * {@code <REGION>} segment of the {@code memberId}, i.e. everything before its trailing FY, HASH8
     * and CHK segments (see {@link #memberIdBase(String)}). Since the region is baked into the member id
     * at creation (see the create endpoint), the locality now moves with that identity rather than being
     * recomputed from the postcode and city. When an owner has no (well-formed) member id yet (e.g.
     * legacy records), fall back to deriving the region directly via {@link #regionOf(Owner)}.
     */
    default String toLocality(Owner owner) {
        String base = memberIdBase(owner.getMemberId());
        if (base != null) {
            return memberIdRegion(base);
        }
        return regionOf(owner);
    }

    /**
     * Derive an owner's marketing segment, formatted {@code '<TIER>_<AREA>'} and one of
     * {@code 'PREMIUM_METRO'}, {@code 'PREMIUM_REGIONAL'}, {@code 'STANDARD_METRO'} or
     * {@code 'STANDARD_REGIONAL'}. TIER is {@code 'PREMIUM'} when the owner's membership level (see
     * {@link #toMembershipLevel(Owner)}) is 3 or more, otherwise {@code 'STANDARD'}. AREA is
     * {@code 'METRO'} when the owner's locality (see {@link #toLocality(Owner)}) is a known region
     * (NSW, VIC or QLD, i.e. one carrying a {@link #REGION_TIMEZONE} entry), otherwise
     * {@code 'REGIONAL'}.
     */
    default String toOwnerSegment(Owner owner) {
        String tier = toMembershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(toLocality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Fixed region-to-IANA-timezone table used to derive an owner's timezone: NSW -> Australia/Sydney,
     * VIC -> Australia/Melbourne, QLD -> Australia/Brisbane.
     */
    Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derive an owner's IANA timezone name from its locality/region (see {@link #toLocality(Owner)})
     * via the fixed {@link #REGION_TIMEZONE} table. Returns {@code null} when the region resolves to no
     * entry in the table (e.g. 'UNKNOWN'), so the timezone is then absent from the DTO.
     */
    default String toTimezone(Owner owner) {
        return REGION_TIMEZONE.get(toLocality(owner));
    }

    /**
     * Derive an owner's region, preferring the postcode: when the postcode is present and falls within
     * a known region's range (see {@link #REGION_POSTCODE_RANGES}) that region wins. When the postcode
     * is absent or in no known range, fall back to the fixed {@link #CITY_REGION} table. Returns
     * 'UNKNOWN' when neither the postcode nor the city resolves a region. This is the region baked into
     * the leading {@code <REGION>} segment of a new owner's member id and, through it, the owner's
     * locality.
     */
    default String regionOf(Owner owner) {
        // Prefer the postcode: the region whose range contains it wins. The logic is kept here (rather
        // than in a String -> String helper) because MapStruct would treat such a helper as an implicit
        // type conversion and wrongly apply it to other String properties (e.g. telephone).
        String postcode = owner.getPostcode();
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ignored) {
                // Non-numeric postcode: fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
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
