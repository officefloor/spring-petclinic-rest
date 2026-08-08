package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.time.Period;
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
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(formatIdentityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(formatAgeBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(formatOwnerSegment(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "possibleDuplicate", expression = "java(owner.getPossibleDuplicateOf() != null)")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives an owner's age band from the birth date, computed against the registration date
     * (falling back to the current date when the registration date is absent): {@code MINOR} when
     * under 18, {@code ADULT} from 18 to 64, and {@code SENIOR} at 65 or older. Returns
     * {@code null} when no birth date is present, so the field is omitted from the response.
     */
    default OwnerDto.AgeBandEnum formatAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(owner.getBirthDate(), asOf).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives an owner's segment formatted {@code '<TIER>_<AREA>'}. TIER is {@code PREMIUM} when
     * the membership level is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the
     * locality is a known region ({@code NSW}, {@code VIC} or {@code QLD}), otherwise {@code REGIONAL}.
     * Returns {@code null} when the owner is {@code null}.
     */
    default OwnerDto.OwnerSegmentEnum formatOwnerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean premium = formatMembershipLevel(owner) >= 3;
        String locality = formatLocality(owner);
        boolean metro = "NSW".equals(locality) || "VIC".equals(locality) || "QLD".equals(locality);
        if (premium) {
            return metro ? OwnerDto.OwnerSegmentEnum.PREMIUM_METRO : OwnerDto.OwnerSegmentEnum.PREMIUM_REGIONAL;
        }
        return metro ? OwnerDto.OwnerSegmentEnum.STANDARD_METRO : OwnerDto.OwnerSegmentEnum.STANDARD_REGIONAL;
    }

    /**
     * Derives an owner's duplicate-detection identity key as the lower-case SHA-256 hex digest over
     * {@code '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'} from the stored (already
     * E.164-normalized) telephone, lower-cased email and the Soundex code of the last name. A
     * {@code null} telephone or email contributes the empty string in its position.
     */
    default String formatIdentityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        return org.springframework.samples.petclinic.util.OwnerIdentity.identityKey(
            owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * Derives an owner's preferred contact channel: {@code EMAIL} when an email address is present
     * (non-null and non-blank), otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives an owner's locality (region) from the memberId identity: it is the {@code REGION}
     * segment of the {@code '<REGION><FY><HASH8><CHK>'} memberId (everything preceding its
     * fixed-length trailing FY, HASH8 and CHK segments). When the memberId is absent it falls back to
     * resolving the region directly, preferring the postcode range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099) and then the fixed city-to-region table ({@code Sydney -> NSW},
     * {@code Melbourne -> VIC}, {@code Brisbane -> QLD}), yielding {@code "UNKNOWN"} when neither
     * resolves.
     */
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = memberIdRegion(owner.getMemberId());
        if (region != null) {
            return region;
        }
        String fromPostcode = regionForPostcode(owner.getPostcode());
        if (fromPostcode != null) {
            return fromPostcode;
        }
        return switch (owner.getCity() == null ? "" : owner.getCity()) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Derives an owner's IANA timezone from the locality (region) via the fixed region-to-timezone
     * table: {@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}. Returns {@code null} when the region is not one of these,
     * so the field is omitted from the response.
     */
    default String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return switch (formatLocality(owner)) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }

    /**
     * Resolves a region from a 4-digit postcode by inclusive range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099). Returns {@code null} when the postcode is absent, not exactly 4 digits, or in
     * no known range, signalling that the caller should fall back to the city-to-region table.
     */
    private String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        if (value >= 2000 && value <= 2099) {
            return "NSW";
        }
        if (value >= 3000 && value <= 3099) {
            return "VIC";
        }
        if (value >= 4000 && value <= 4099) {
            return "QLD";
        }
        return null;
    }

    /**
     * Computes an owner's membership points, starting at 0: plus 2 when an email is present, plus 1
     * when the owner's namesake count is 0, plus 2 for a household of 3 or more, plus 3 when the
     * owner's tenure exceeds 365 days.
     */
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (unique) {
            points += 1;
        }
        boolean largeHousehold = owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = owner.getRegistrationDate() != null
            && fiscalYearStart(LocalDate.now()) - fiscalYearStart(owner.getRegistrationDate()) >= 1;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives an owner's membership level, a number from 1 to 4, from the owner's membership points:
     * 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points.
     */
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = formatMembershipPoints(owner);
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
     * Formats an owner's fiscal year as {@code 'FY<YY>'}, where YY is the two-digit FY segment carried
     * inside the memberId ({@code '<REGION><FY><HASH8><CHK>'}): the last two digits of the starting
     * calendar year of the fiscal year (starting 1 July) of the business-day-adjusted registration
     * date (e.g. a memberId formed on 5 August 2026 yields {@code "FY26"} and one on 3 March 2026
     * yields {@code "FY25"}). Returns {@code null} when the memberId is absent.
     */
    default String formatFiscalYear(Owner owner) {
        if (owner == null) {
            return null;
        }
        String fiscalYear = memberIdFiscalYear(owner.getMemberId());
        return fiscalYear == null ? null : "FY" + fiscalYear;
    }

    /**
     * The core of a memberId with any {@code '-<n>'} collision suffix stripped, or {@code null} when
     * the memberId is absent. The core is the raw {@code '<REGION><FY><HASH8><CHK>'} whose trailing FY
     * (2 digits), HASH8 (8 hex) and CHK (1 digit) are fixed-length, so its segments can be read by
     * position.
     */
    private String memberIdCore(String memberId) {
        if (memberId == null) {
            return null;
        }
        int dash = memberId.indexOf('-');
        return dash > 0 ? memberId.substring(0, dash) : memberId;
    }

    /**
     * The {@code REGION} segment of a memberId (everything preceding its fixed-length trailing FY,
     * HASH8 and CHK segments), or {@code null} when the memberId is absent or too short to carry one.
     */
    private String memberIdRegion(String memberId) {
        String core = memberIdCore(memberId);
        if (core == null || core.length() <= 11) {
            return null;
        }
        return core.substring(0, core.length() - 11);
    }

    /**
     * The two-digit {@code FY} segment of a memberId (the two digits preceding its fixed-length HASH8
     * and CHK segments), or {@code null} when the memberId is absent or too short to carry one.
     */
    private String memberIdFiscalYear(String memberId) {
        String core = memberIdCore(memberId);
        if (core == null || core.length() < 11) {
            return null;
        }
        return core.substring(core.length() - 11, core.length() - 9);
    }

    /**
     * Returns the starting calendar year of the fiscal year (which starts on 1 July) that contains
     * the given date: the date's own year when it falls on or after 1 July, otherwise the previous
     * year.
     */
    private int fiscalYearStart(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
    }

    /**
     * Formats an owner's stored E.164 telephone for humans as the country code, a space, and the
     * national digits grouped in threes (e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}).
     * The raw {@code telephone} value stays in E.164 form. Returns the stored value unchanged when it
     * is {@code null} or not a well-formed E.164 string (a {@code '+'} followed by digits).
     */
    default String formatTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.matches("\\+[0-9]+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength = countryCodeLength(digits);
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            grouped.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return grouped.toString();
    }

    /**
     * Determines the length of the country-code segment of an E.164 number's digits (its digits
     * without the leading {@code '+'}): 1 for the NANP ({@code '+1'}) and 2 otherwise (e.g. Australia's
     * {@code '+61'}), mirroring the country codes the application normalises to.
     */
    private int countryCodeLength(String digits) {
        return digits.startsWith("1") ? 1 : 2;
    }

    /**
     * Formats an owner's canonical self link as {@code "/api/owners/" + id} from the stored id.
     * Returns {@code null} when the owner or its id is absent, so the field is omitted from the
     * response.
     */
    default String formatSelfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Formats an owner's display name as {@code "LastName, FirstName"} from the stored names.
     */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's salutation as the title and last name separated by a single space
     * (e.g. {@code "DR Franklin"}) when a title is present, or just the last name when no title
     * is given (null or blank). Returns {@code null} when the owner is {@code null}.
     */
    default String formatSalutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private String initial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + ".";
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
