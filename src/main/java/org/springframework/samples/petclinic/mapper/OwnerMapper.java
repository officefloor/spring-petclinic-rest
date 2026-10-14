package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.OwnerPostcode;
import org.springframework.samples.petclinic.rest.validation.OwnerTelephone;
import org.springframework.samples.petclinic.util.FiscalYears;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    Map<String, String> REGION_TIMEZONES = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "selfLink", expression = "java(toSelfLink(owner))")
    @Mapping(target = "checkDigit", expression = "java(toCheckDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(toMembershipNumber(owner))")
    @Mapping(target = "fiscalYear", expression = "java(toFiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(toMembershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(Boolean.TRUE.equals(owner.getBulkSignupWarning()))")
    @Mapping(target = "capacityWarning", expression = "java(Boolean.TRUE.equals(owner.getCapacityWarning()))")
    @Mapping(target = "possibleDuplicate", expression = "java(owner.getPossibleDuplicateOf() != null)")
    @Mapping(target = "ageBand", expression = "java(toAgeBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(toTelephoneDisplay(owner))")
    @Mapping(target = "timezone", expression = "java(toTimezone(owner))")
    @Mapping(target = "salutation", expression = "java(toSalutation(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * '&lt;title&gt; &lt;lastName&gt;', or just the last name when the owner has no title.
     */
    default String toSalutation(Owner owner) {
        return owner.getTitle() == null ? owner.getLastName() : owner.getTitle() + " " + owner.getLastName();
    }

    /**
     * The stored E.164 telephone formatted for humans, e.g. '+61 412 345 678'.
     */
    default String toTelephoneDisplay(Owner owner) {
        return OwnerTelephone.format(owner.getTelephone());
    }

    /**
     * The owner's age in completed years on the registration date: 'MINOR' under 18, 'ADULT' 18-64,
     * 'SENIOR' 65 or over. Absent when the owner has no birth date or registration date.
     */
    default String toAgeBand(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        long age = ChronoUnit.YEARS.between(owner.getBirthDate(), owner.getRegistrationDate());
        if (age < 18) {
            return "MINOR";
        }
        return age < 65 ? "ADULT" : "SENIOR";
    }

    /**
     * The REGION part of the owner's customer code ('&lt;REGION&gt;-&lt;HASH8&gt;', optionally followed by a
     * '-&lt;n&gt;' de-duplication suffix); for owners without a
     * customer code, the region derived by {@link OwnerPostcode#regionOf(String, String)}.
     */
    default String toLocality(Owner owner) {
        String code = owner.getCustomerCode();
        int dash = code == null ? -1 : code.indexOf('-');
        if (dash > 0) {
            return code.substring(0, dash);
        }
        return OwnerPostcode.regionOf(owner.getPostcode(), owner.getCity());
    }

    /**
     * The IANA timezone of the owner's {@link #toLocality(Owner) locality} from {@link #REGION_TIMEZONES};
     * absent for any other locality.
     */
    default String toTimezone(Owner owner) {
        return REGION_TIMEZONES.get(toLocality(owner));
    }

    /**
     * 'EMAIL' when the owner has an email address, otherwise 'PHONE'.
     */
    default String toContactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    /** Membership points for an owner with an email address. */
    int EMAIL_MEMBERSHIP_POINTS = 2;

    /** Membership points for an owner without namesakes. */
    int NO_NAMESAKE_MEMBERSHIP_POINTS = 1;

    /** Membership points for a household of at least {@link #HOUSEHOLD_MEMBERSHIP_SIZE} members. */
    int HOUSEHOLD_MEMBERSHIP_POINTS = 2;

    /** The household size earning {@link #HOUSEHOLD_MEMBERSHIP_POINTS}. */
    int HOUSEHOLD_MEMBERSHIP_SIZE = 3;

    /** Membership points for a tenure of at least {@link #TENURE_MEMBERSHIP_FISCAL_YEARS} elapsed fiscal years. */
    int TENURE_MEMBERSHIP_POINTS = 3;

    /** {@link #TENURE_MEMBERSHIP_POINTS} requires a tenure of at least this many elapsed fiscal years. */
    int TENURE_MEMBERSHIP_FISCAL_YEARS = 1;

    /** The highest membership level. */
    int MAX_MEMBERSHIP_LEVEL = 4;

    /**
     * The membership points of a newly created owner. A new owner has zero tenure, so this never
     * includes {@link #TENURE_MEMBERSHIP_POINTS}.
     */
    default Integer toMembershipPoints(Owner owner) {
        return membershipPoints(owner, 0);
    }

    /**
     * The membership level of a newly created owner, derived from {@link #toMembershipPoints(Owner)}
     * and capped at the owner's membership level cap, when it has one.
     */
    default Integer toMembershipLevel(Owner owner) {
        int level = membershipLevel(toMembershipPoints(owner));
        return owner.getMembershipLevelCap() == null ? level : Math.min(level, owner.getMembershipLevelCap());
    }

    /**
     * Starts at 0, adds {@link #EMAIL_MEMBERSHIP_POINTS} when an email is present,
     * {@link #NO_NAMESAKE_MEMBERSHIP_POINTS} when the owner has no namesakes,
     * {@link #HOUSEHOLD_MEMBERSHIP_POINTS} for a household of {@link #HOUSEHOLD_MEMBERSHIP_SIZE} or more
     * members and {@link #TENURE_MEMBERSHIP_POINTS} for a tenure of at least
     * {@link #TENURE_MEMBERSHIP_FISCAL_YEARS} elapsed fiscal years (see {@link FiscalYears#between}).
     */
    default int membershipPoints(Owner owner, long tenureFiscalYears) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_MEMBERSHIP_POINTS;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += NO_NAMESAKE_MEMBERSHIP_POINTS;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= HOUSEHOLD_MEMBERSHIP_SIZE) {
            points += HOUSEHOLD_MEMBERSHIP_POINTS;
        }
        if (tenureFiscalYears >= TENURE_MEMBERSHIP_FISCAL_YEARS) {
            points += TENURE_MEMBERSHIP_POINTS;
        }
        return points;
    }

    /**
     * Maps membership points to a level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and
     * {@link #MAX_MEMBERSHIP_LEVEL} for 6 or more.
     */
    default int membershipLevel(int points) {
        return Math.min(points / 2 + 1, MAX_MEMBERSHIP_LEVEL);
    }

    /**
     * 'FY&lt;YY&gt;' for the fiscal year (starting 1 July) containing the business-day adjusted
     * registration date; absent when the owner has no registration date.
     */
    default String toFiscalYear(Owner owner) {
        return FiscalYears.label(owner.getRegistrationDate());
    }

    /**
     * Builds the membership number '&lt;customerCode&gt;-M&lt;YY&gt;', where YY is the last two digits of
     * the fiscal year containing the registration date.
     */
    default String toMembershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            FiscalYears.twoDigit(owner.getRegistrationDate()));
    }

    /**
     * The Luhn check digit over the digits of the customer code, ignoring non-digit characters.
     */
    default Integer toCheckDigit(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return null;
        }
        int sum = 0;
        boolean doubled = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubled) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubled = !doubled;
        }
        return (10 - sum % 10) % 10;
    }

    /**
     * The relative URI of the owner resource, '/api/owners/&lt;id&gt;'; absent for an unsaved owner.
     */
    default String toSelfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    default String toInitials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private static String initial(String name) {
        return name == null || name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "membershipLevelCap", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
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
