package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year arithmetic for date-derived owner values. The fiscal year starts on 1 July, so a
 * date on or after 1 July belongs to the fiscal year that begins in its own calendar year, while
 * a date before 1 July belongs to the fiscal year that began in the previous calendar year. A
 * fiscal year is identified by the calendar year in which it begins and labelled 'FY&lt;YY&gt;',
 * where YY is the last two digits of that starting year (e.g. any date from 2026-07-01 to
 * 2027-06-30 is 'FY26').
 */
public final class FiscalYears {

    /** The month in which the fiscal year starts (1 July). */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYears() {
    }

    /**
     * The calendar year in which {@code date}'s fiscal year begins: the date's own year from 1 July
     * onward, otherwise the previous year.
     */
    public static int startYear(LocalDate date) {
        int year = date.getYear();
        return date.getMonth().getValue() >= FISCAL_YEAR_START.getValue() ? year : year - 1;
    }

    /** Label {@code date}'s fiscal year as 'FY&lt;YY&gt;', or null when {@code date} is absent. */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", startYear(date) % 100);
    }

    /**
     * The number of fiscal years elapsed between {@code from} and {@code to}, i.e. the count of
     * 1 July boundaries crossed. Zero when both dates fall in the same fiscal year.
     */
    public static int elapsedBetween(LocalDate from, LocalDate to) {
        return startYear(to) - startYear(from);
    }
}
