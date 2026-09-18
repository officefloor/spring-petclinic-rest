package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year arithmetic for date-derived values. The fiscal year starts on 1 July and is
 * named for the calendar year in which it ends: a date on or after 1 July belongs to the
 * fiscal year ending the following calendar year, while an earlier date belongs to the fiscal
 * year ending in its own calendar year (e.g. both 2025-08-01 and 2026-03-01 fall in FY26).
 */
public final class FiscalYear {

    /** The month a fiscal year starts (1 July). */
    private static final int START_MONTH = Month.JULY.getValue();

    private FiscalYear() {
    }

    /** The calendar year in which the fiscal year containing {@code date} ends. */
    public static int of(LocalDate date) {
        return (date.getMonthValue() >= START_MONTH) ? date.getYear() + 1 : date.getYear();
    }

    /** The two-digit fiscal year for {@code date} (e.g. {@code 26} for FY26). */
    public static int twoDigit(LocalDate date) {
        return of(date) % 100;
    }

    /** The fiscal year label {@code FY<YY>} for {@code date} (e.g. {@code "FY26"}). */
    public static String label(LocalDate date) {
        return String.format("FY%02d", twoDigit(date));
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to}. */
    public static int elapsedBetween(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
