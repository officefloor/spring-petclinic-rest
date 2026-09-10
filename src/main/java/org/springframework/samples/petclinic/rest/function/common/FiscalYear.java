package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July: a date on or
 * after 1 July belongs to the fiscal year ending the following calendar year, otherwise it
 * belongs to the fiscal year ending in its own calendar year. The fiscal year is named by
 * the calendar year in which it ends (so 1 July 2026 to 30 June 2027 is fiscal year 2027).
 *
 * <p>Callers pass the business-day-adjusted registration date (see {@link BusinessDays}), so
 * every fiscal-year-derived value — the {@code fiscalYear} label and the membership number's
 * year segment — follows from the adjusted date.
 */
public final class FiscalYear {

    /** The month the fiscal year starts on. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The fiscal year the given date falls in, named by the calendar year in which the
     * fiscal year ends.
     */
    public static int of(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_YEAR_START.getValue() ? year + 1 : year;
    }

    /**
     * The fiscal year label {@code FY<YY>}, where {@code YY} is the last two digits of the
     * fiscal year (see {@link #of(LocalDate)}), e.g. {@code FY27}.
     */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }
}
