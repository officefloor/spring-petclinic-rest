package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July: a date on or after
 * 1 July belongs to the fiscal year named for that calendar year, a date before 1 July belongs to
 * the fiscal year named for the previous calendar year (e.g. 2026-03-15 is fiscal year 2025).
 *
 * <p>The inputs are the business-day-adjusted registration date, so every derived value (the
 * {@code fiscalYear} label, the membership number's year segment, tenure) shares one basis.
 */
public final class FiscalYear {

    /** First month of the fiscal year. */
    private static final Month FISCAL_START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The starting calendar year of the fiscal year that contains {@code date}: the date's own year
     * from 1 July onward, otherwise the previous year.
     */
    public static int startYear(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_START.getValue() ? year : year - 1;
    }

    /** The two-digit year segment (fiscal start year mod 100) for the fiscal year of {@code date}. */
    public static int yearSegment(LocalDate date) {
        return startYear(date) % 100;
    }

    /** The {@code 'FY<YY>'} label for the fiscal year that contains {@code date}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", yearSegment(date));
    }

    /**
     * Elapsed fiscal years between {@code from} and {@code to}: the number of 1-July boundaries
     * crossed, i.e. the difference of their fiscal start years.
     */
    public static long elapsed(LocalDate from, LocalDate to) {
        return startYear(to) - startYear(from);
    }
}
