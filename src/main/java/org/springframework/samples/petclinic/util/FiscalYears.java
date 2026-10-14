package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year calculations. A fiscal year starts on 1 July and is named after the calendar year in
 * which it ends, so 1 July 2026 to 30 June 2027 is fiscal year 2027 ('FY27').
 */
public final class FiscalYears {

    /** The first month of a fiscal year. */
    static final Month FIRST_MONTH = Month.JULY;

    private FiscalYears() {
    }

    /**
     * The fiscal year containing a date, named after the calendar year in which it ends.
     *
     * @param date the date
     * @return the fiscal year, e.g. 2027 for 2026-07-01 through 2027-06-30
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= FIRST_MONTH.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The last two digits of the fiscal year containing a date.
     *
     * @param date the date
     * @return the two-digit fiscal year, 0-99
     */
    public static int twoDigit(LocalDate date) {
        return Math.floorMod(of(date), 100);
    }

    /**
     * Formats the fiscal year containing a date as 'FY&lt;YY&gt;'.
     *
     * @param date the date
     * @return e.g. 'FY27' for 2026-10-14, or {@code null} if {@code date} is {@code null}
     */
    public static String label(LocalDate date) {
        return date == null ? null : String.format("FY%02d", twoDigit(date));
    }

    /**
     * The number of fiscal years elapsed from one date to another, i.e. the number of fiscal-year
     * starts (1 July) passed after {@code from} up to and including {@code to}; negative when
     * {@code to} is in an earlier fiscal year.
     *
     * @param from the start date
     * @param to   the end date
     * @return the elapsed fiscal years
     */
    public static long between(LocalDate from, LocalDate to) {
        return (long) of(to) - of(from);
    }
}
