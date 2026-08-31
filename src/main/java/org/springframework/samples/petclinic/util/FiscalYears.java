package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Fiscal-year helper: the fiscal year starts on 1 July and is named by the calendar
 * year in which it ends, so 1 July 2025 - 30 June 2026 is fiscal year 2026.
 */
public final class FiscalYears {

    private FiscalYears() {
    }

    /** The fiscal year that contains {@code date} (the calendar year it ends in). */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The two-digit last segment of the fiscal year for {@code date}, e.g. 26 for FY26. */
    public static int yearOfCentury(LocalDate date) {
        return of(date) % 100;
    }

    /** The 'FY<YY>' label for {@code date}, e.g. FY26. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", yearOfCentury(date));
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to}. */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
