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
public abstract class OwnerMapper {

    /**
     * Points added when the owner has an email address.
     */
    private static final int EMAIL_POINTS = 2;

    /**
     * Points added when the owner has no namesakes ({@code namesakeCount} is 0).
     */
    private static final int NO_NAMESAKE_POINTS = 1;

    /**
     * Points added when the owner belongs to a household of {@value #LARGE_HOUSEHOLD_SIZE} or more members.
     */
    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    /**
     * The household size (members, including the owner) at which the large-household points are awarded.
     */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    /**
     * Points added when the owner's tenure reaches {@value #TENURE_POINTS_FISCAL_YEARS} elapsed fiscal year(s).
     */
    private static final int TENURE_POINTS = 3;

    /**
     * Tenure, in elapsed fiscal years, that an owner must reach to earn the tenure points.
     */
    private static final int TENURE_POINTS_FISCAL_YEARS = 1;

    /**
     * The month (1 July) on which the fiscal year starts.
     */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() + \"-M\" + fiscalYearSuffix(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.of(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.timezoneOf(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Computes an owner's membership points. Points start at 0 and accumulate: {@value #EMAIL_POINTS} when an
     * email is present, {@value #NO_NAMESAKE_POINTS} when {@code namesakeCount} is 0,
     * {@value #LARGE_HOUSEHOLD_POINTS} for a household of {@value #LARGE_HOUSEHOLD_SIZE} or more members, and
     * {@value #TENURE_POINTS} for a tenure of at least {@value #TENURE_POINTS_FISCAL_YEARS} elapsed fiscal
     * year(s) (measured from {@code registrationDate}). Because a newly created owner has zero elapsed fiscal
     * years, a new owner never earns the tenure points.
     *
     * @param owner the owner whose points are being computed
     * @return the owner's membership points (0 or more)
     */
    protected Integer membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += NO_NAMESAKE_POINTS;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (tenureFiscalYears(owner) >= TENURE_POINTS_FISCAL_YEARS) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * Maps an owner's membership points to a numeric membership level: level 1 for 0-1 points, level 2 for
     * 2-3 points, level 3 for 4-5 points, and level 4 for 6 or more points.
     *
     * @param owner the owner whose level is being computed
     * @return the membership level (between 1 and 4 inclusive)
     */
    protected Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    /**
     * Computes an owner's tenure as the number of elapsed fiscal years between {@code registrationDate} and
     * today: the difference between the fiscal year (starting 1 July) of today and that of the registration
     * date. A newly created owner (registered in the current fiscal year) has zero tenure. Owners with no
     * {@code registrationDate}, or a registration date in a later fiscal year, are treated as zero tenure.
     *
     * @param owner the owner whose tenure is being computed
     * @return the owner's tenure in elapsed fiscal years, never negative
     */
    protected long tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long years = (long) fiscalYearStart(LocalDate.now()) - fiscalYearStart(registrationDate);
        return Math.max(years, 0);
    }

    /**
     * Derives an owner's {@code fiscalYear}, formatted {@code 'FY<YY>'}, from the (already
     * business-day-adjusted) {@code registrationDate}. The fiscal year starts on 1 July and is labelled by
     * the calendar year in which it starts, so a registration on or after 1 July belongs to that year's
     * fiscal year and an earlier one to the previous year's (e.g. {@code 2026-08-15} yields {@code 'FY26'}).
     *
     * @param owner the owner whose fiscal year is being derived
     * @return the owner's fiscal year as {@code 'FY<YY>'}
     */
    protected String fiscalYear(Owner owner) {
        return "FY" + fiscalYearSuffix(owner.getRegistrationDate());
    }

    /**
     * Returns the two-digit fiscal-year suffix ({@code '<YY>'}) for the given date: the last two digits of
     * the calendar year in which that date's fiscal year (starting 1 July) begins. This is the shared segment
     * used by both {@link #fiscalYear(Owner)} and the membership number.
     *
     * @param date the date whose fiscal-year suffix is computed
     * @return the zero-padded two-digit fiscal-year suffix
     */
    protected String fiscalYearSuffix(LocalDate date) {
        return String.format("%02d", fiscalYearStart(date) % 100);
    }

    /**
     * Returns the calendar year in which the fiscal year containing {@code date} starts. The fiscal year
     * starts on 1 July, so dates from July onward start their fiscal year in the same calendar year, while
     * dates from January to June belong to the fiscal year that started in the previous calendar year.
     *
     * @param date the date whose fiscal-year start is computed
     * @return the calendar year in which the containing fiscal year starts
     */
    protected int fiscalYearStart(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? year : year - 1;
    }

    /**
     * Computes the owner's {@code checkDigit}: a single Luhn check digit (0-9) over the digits contained in
     * the owner's {@code customerCode}. Non-digit characters (such as the letters and dashes in the code) are
     * skipped; the rightmost digit is doubled and every second digit thereafter, digits exceeding 9 after
     * doubling have 9 subtracted, and the check digit is {@code (10 - sum % 10) % 10}.
     *
     * @param owner the owner whose check digit is being computed
     * @return the Luhn check digit (between 0 and 9 inclusive) over the customer code's digits
     */
    protected Integer checkDigit(Owner owner) {
        String code = owner.getCustomerCode() == null ? "" : owner.getCustomerCode();
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
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
     * Derives an owner's preferred contact channel at read time: {@code 'EMAIL'} when an email address is
     * present, otherwise {@code 'PHONE'}.
     *
     * @param owner the owner whose contact preference is being computed
     * @return {@code 'EMAIL'} when the owner has a non-blank email, otherwise {@code 'PHONE'}
     */
    protected String contactPreference(Owner owner) {
        return (owner.getEmail() != null && !owner.getEmail().isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's age band from {@code birthDate}, measured against {@code registrationDate}: the
     * completed years between the two dates place the owner in {@code 'MINOR'} (under 18), {@code 'ADULT'}
     * (18 to 64 inclusive) or {@code 'SENIOR'} (65 or over). Returns {@code null} when the owner has no
     * {@code birthDate}, so the field is simply absent for owners created without one.
     *
     * @param owner the owner whose age band is being derived
     * @return {@code 'MINOR'}, {@code 'ADULT'} or {@code 'SENIOR'}, or {@code null} when no birth date is set
     */
    protected String ageBand(Owner owner) {
        if (owner.getBirthDate() == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int years = Period.between(owner.getBirthDate(), reference).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * Derives an owner's {@code identityKey}: the normalized telephone, the email (or an empty string when
     * absent) and the household id (or an empty string when absent), joined by {@code '|'} in that order
     * (e.g. {@code '+61412345678||a1b2c3d4e5f6a7b8'}). This is the single key used for duplicate detection,
     * so the value returned here matches the one the create endpoint compares.
     *
     * @param owner the owner whose identity key is being derived
     * @return the owner's identity key
     */
    protected String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for humans: the country code, a space, then the
     * national digits grouped in threes from the left (e.g. {@code '+61412345678'} becomes
     * {@code '+61 412 345 678'}). The raw {@code telephone} is left untouched in E.164 form. Values that are
     * {@code null} or not in E.164 form (no leading {@code '+'}) are returned unchanged.
     * <p>
     * The country code is split off using the codes the application recognises: {@code '+1'} (NANP) is a
     * single digit and any other number is assumed to carry the default {@code '+61'}-style two-digit code.
     *
     * @param owner the owner whose telephone is being formatted
     * @return the human-formatted telephone, or the raw value when it is {@code null} or not E.164
     */
    protected String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return telephone;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            if (i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }

    /**
     * Composes the owner's salutation: the {@code title}, a single space and the last name (e.g.
     * {@code 'DR Franklin'}) when a non-blank {@code title} was supplied, or just the last name when no
     * title was given.
     *
     * @param owner the owner whose salutation is being composed
     * @return {@code '<title> <lastName>'} when a title is present, otherwise the last name alone
     */
    protected String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Derives an owner's {@code selfLink}: the canonical relative URL of the owner resource, formatted
     * {@code '/api/owners/<id>'} from the owner's id. Returns {@code null} when the owner has no id yet.
     *
     * @param owner the owner whose self link is being derived
     * @return the owner's self link, or {@code null} when the owner has no id
     */
    protected String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

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
