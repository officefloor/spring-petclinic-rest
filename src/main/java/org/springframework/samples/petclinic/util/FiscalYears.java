package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives fiscal-year values from an owner's business-day-adjusted {@code registrationDate}.
 *
 * <p>The fiscal year starts on 1 July and is labelled by the calendar year in which it ends, so a
 * date on or after 1 July belongs to the next calendar year's fiscal year (e.g. 2 July 2025 and
 * 30 June 2026 both fall in fiscal year 2026, {@code "FY26"}).
 */
public final class FiscalYears {

    /** The month (1-based) on which the fiscal year begins: 1 July. */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    private FiscalYears() {
    }

    /**
     * @param date any date.
     * @return the fiscal year (as a full calendar year) the date falls in: {@code year + 1} from
     *         1 July onward, otherwise {@code year}.
     */
    public static int fiscalYearOf(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? year + 1 : year;
    }

    /**
     * @param date any date.
     * @return the last two digits of the fiscal year, zero-padded (e.g. {@code "26"}).
     */
    public static String yearSegment(LocalDate date) {
        return String.format("%02d", fiscalYearOf(date) % 100);
    }

    /**
     * @param owner the pet owner.
     * @return the fiscal year of the owner's {@code registrationDate}, formatted {@code "FY<YY>"},
     *         or {@code null} when the owner has no registrationDate.
     */
    public static String labelFor(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return "FY" + yearSegment(registrationDate);
    }
}
