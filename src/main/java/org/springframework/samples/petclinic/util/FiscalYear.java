package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Fiscal-year helpers for owner date-derived values. The fiscal year starts on 1 July, so a date
 * on or after 1 July belongs to the fiscal year that ends the following calendar year, and a date
 * before 1 July belongs to the one ending in its own calendar year. A fiscal year is identified by
 * the calendar year in which it ends (the Australian convention: 1 July 2025 - 30 June 2026 is
 * 'FY26').
 */
public final class FiscalYear {

    /** The month (1 July) on which each fiscal year starts. */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    private FiscalYear() {
    }

    /**
     * The fiscal year the date falls in, identified by the calendar year in which it ends: the
     * date's own year before 1 July, or the next year on or after 1 July.
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal year of {@code date} as 'FY<YY>' (e.g. 'FY26'), the last two digits of {@link #of}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed from {@code from} to {@code to}, i.e. the count of
     * 1-July boundaries crossed between them. Zero when both dates fall in the same fiscal year.
     */
    public static long elapsed(LocalDate from, LocalDate to) {
        return (long) of(to) - of(from);
    }
}
