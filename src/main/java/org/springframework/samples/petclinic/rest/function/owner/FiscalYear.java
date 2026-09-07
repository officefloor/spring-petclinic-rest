package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year arithmetic for date-derived owner values. The fiscal year starts on 1 July, so a date
 * on or after 1 July belongs to the fiscal year that ends in the following calendar year (e.g.
 * 2026-09-08 is in fiscal year 2027), while a date before 1 July belongs to the fiscal year that
 * ends in its own calendar year. The fiscal year is named by the calendar year in which it ends.
 * Used off the owner's business-day-adjusted {@code registrationDate} to expose {@code fiscalYear}
 * on responses and to supply the year segment of the {@link MembershipNumber} and the tenure count
 * in {@link MembershipPoints}.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * The fiscal year (named by its ending calendar year) that {@code date} falls in: the calendar
     * year for dates before 1 July, or the next calendar year for dates on or after 1 July.
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The {@code FY<YY>} label for {@code date} (YY being the last two digits of its fiscal year),
     * or {@code null} when the date is absent.
     */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", of(date) % 100);
    }
}
