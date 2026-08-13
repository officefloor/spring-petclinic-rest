package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Maps Owner &amp; OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * A create is flagged with {@code bulkSignupWarning=true} once more than this many owners have
     * already been created on the same registration date.
     */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    @Autowired
    protected ClinicService clinicService;

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The month the fiscal year starts on (1 July).
     */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    /**
     * Derives the fiscal year (as an integer) of the given date. The fiscal year starts on 1 July,
     * so a date on or after 1 July belongs to the following calendar year's fiscal year (e.g.
     * 2026-07-01 is fiscal year 2027) while an earlier date takes the calendar year unchanged.
     */
    private int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derives the owner's {@code fiscalYear}, formatted {@code FY<YY>} where YY is the last two
     * digits of the fiscal year (starting 1 July) of the owner's business-day-adjusted
     * {@code registrationDate}. Returns {@code null} when no registration date is present.
     */
    protected String fiscalYear(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(registrationDate) % 100);
    }

    /**
     * Derives the owner's {@code ageBand} from their {@code birthDate} relative to their
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64, and
     * {@code SENIOR} at 65 or older. Returns {@code null} when no birth date was supplied, or when
     * the registration date (against which the age is measured) is absent.
     */
    protected OwnerDto.AgeBandEnum ageBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * The country calling codes (without the leading {@code '+'}) the display formatter recognises,
     * longest first so the correct code is matched before a shorter prefix of it. Australia
     * ({@code +61}) and the North American Numbering Plan ({@code +1}) mirror the codes the create
     * endpoint normalizes against.
     */
    private static final List<String> KNOWN_COUNTRY_CODES = List.of("61", "1");

    /**
     * Derives the owner's {@code telephoneDisplay}: the stored E.164 {@code telephone} formatted for
     * humans as the country code, a space, then the national digits grouped in threes (e.g.
     * {@code +61412345678} becomes {@code +61 412 345 678}). Returns the raw value unchanged when it
     * is {@code null} or not an E.164 number ({@code '+'} followed by digits).
     */
    protected String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.matches("\\+[0-9]+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String countryCode = KNOWN_COUNTRY_CODES.stream()
            .filter(digits::startsWith)
            .findFirst()
            .orElse("");
        String national = digits.substring(countryCode.length());
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + " " + grouped;
    }

    /**
     * Derives the owner's {@code identityKey}, the single consolidated duplicate-detection key:
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. A {@code null}
     * email or householdId contributes an empty segment.
     */
    protected String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    protected OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * The inclusive 4-digit postcode range that identifies each region, keyed by region code
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derives the owner's locality (region) from the region-and-hash identity: it is the REGION
     * component of the owner's {@code customerCode} ({@code <REGION>-<HASH8>}), so the locality now
     * moves in lockstep with the identity assigned on create. When no customer code has been
     * assigned (e.g. seed data) the region is derived directly, preferring the postcode: a 4-digit
     * postcode falling in a known region's range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099)
     * yields that region, otherwise the city-to-region table (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD) is consulted, returning {@code UNKNOWN} when neither identifies a region.
     */
    protected String locality(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash > 0) {
                return customerCode.substring(0, dash);
            }
        }
        String fromPostcode = regionFromPostcode(owner.getPostcode());
        if (fromPostcode != null) {
            return fromPostcode;
        }
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        return switch (city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * The IANA timezone of each region, keyed by region code (NSW->Australia/Sydney,
     * VIC->Australia/Melbourne, QLD->Australia/Brisbane).
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's {@code timezone}: the IANA timezone name for the owner's
     * {@link #locality(Owner) locality/region}, looked up in the fixed region-to-timezone table
     * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane). Returns
     * {@code null} when the region has no known timezone (e.g. {@code UNKNOWN}).
     */
    protected String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * Resolves the region whose postcode range contains the given postcode, or {@code null} when
     * the postcode is absent, not 4 digits, or in no known range.
     */
    private String regionFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The minimum household size (counting the owner itself) that earns household membership
     * points.
     */
    private static final int HOUSEHOLD_POINTS_SIZE = 3;

    /**
     * An owner's tenure must span strictly more than this many elapsed fiscal years to earn the
     * tenure membership points. A newly created owner has zero elapsed fiscal years and so never
     * clears this threshold.
     */
    private static final int TENURE_POINTS_FISCAL_YEARS = 1;

    /**
     * Derives the owner's membership points: it starts at 0, gains 2 when an email is present,
     * gains 1 when the owner has no namesakes (namesakeCount is 0), gains 2 when the owner's
     * household has {@value #HOUSEHOLD_POINTS_SIZE} or more members, and gains 3 once the owner's
     * tenure exceeds {@value #TENURE_POINTS_FISCAL_YEARS} elapsed fiscal year(s).
     */
    protected Integer membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (householdSize(owner) >= HOUSEHOLD_POINTS_SIZE) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > TENURE_POINTS_FISCAL_YEARS) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives the owner's membership level, capped by their household. The level starts from the
     * owner's own {@link #uncappedMembershipLevel(Owner) points-based level} but may not exceed one
     * above the current maximum level among the owner's existing household members (other owners
     * sharing its {@code householdId}). When the owner has no existing household member no cap
     * applies and the points-based level is returned unchanged.
     */
    protected Integer membershipLevel(Owner owner) {
        int level = uncappedMembershipLevel(owner);
        OptionalInt maxHouseholdLevel = otherHouseholdMembers(owner)
            .mapToInt(this::uncappedMembershipLevel)
            .max();
        if (maxHouseholdLevel.isEmpty()) {
            return level;
        }
        return Math.min(level, maxHouseholdLevel.getAsInt() + 1);
    }

    /**
     * The owner's points-based membership level, before the household ceiling is applied: level 1
     * for 0-1 points, level 2 for 2-3, level 3 for 4-5, and level 4 for 6 or more (see
     * {@link #membershipPoints(Owner) membership points}). The household members' maximum against
     * which {@link #membershipLevel(Owner)} caps is measured with this uncapped value, so mutual
     * household members do not recurse into each other's caps.
     */
    private int uncappedMembershipLevel(Owner owner) {
        int points = membershipPoints(owner);
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
     * The owner's existing household members: every other owner (excluding the owner itself) sharing
     * its {@code householdId}. Empty when the owner has no household identifier or is the sole member
     * of its household.
     */
    private Stream<Owner> otherHouseholdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return Stream.empty();
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()));
    }

    /**
     * The number of owners in this owner's household — the count of owners (including this one)
     * sharing its {@code householdId}. Returns 0 when the owner has no household identifier.
     */
    private long householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 0;
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * The owner's tenure in elapsed fiscal years: the number of fiscal years (each starting 1 July)
     * between their {@code registrationDate} and the current date, i.e. the current fiscal year less
     * the registration date's fiscal year. Returns 0 when no registration date is present, so an
     * owner without a registration date is treated as having no tenure.
     */
    private long tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return (long) fiscalYearOf(LocalDate.now()) - fiscalYearOf(registrationDate);
    }

    /**
     * Flags a bulk-signup day: {@code true} once more than {@link #BULK_SIGNUP_THRESHOLD} other
     * owners already carry this owner's {@code registrationDate}. The owner itself is excluded from
     * the count, so on a create the flag reflects the owners that pre-existed it that day, matching
     * the accumulation the per-day create limit is enforced against. Returns {@code false} when the
     * owner has no registration date.
     */
    protected Boolean bulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long othersOnDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return othersOnDay > BULK_SIGNUP_THRESHOLD;
    }

    /**
     * Derives the owner's {@code checkDigit}: the Luhn check digit (0-9) computed over the digits
     * contained in the owner's {@code customerCode}. Non-digit characters are ignored. Returns
     * {@code null} when no customer code has been assigned.
     */
    protected Integer checkDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Derives the owner's {@code salutation}: the owner's {@code title} and {@code lastName}
     * separated by a single space (e.g. {@code DR Franklin}), or just the {@code lastName} when no
     * title was supplied (a {@code null} or blank title).
     */
    protected String salutation(Owner owner) {
        String title = owner.getTitle();
        String lastName = owner.getLastName();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Derives the owner's {@code selfLink}: the canonical link to this owner resource, formatted
     * {@code /api/owners/<id>} using the owner's id. Returns {@code null} when the owner has no id.
     */
    protected String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
