package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Single source of truth for the fiscal year a date falls in, where the fiscal year starts on
 * 1 July and is named by the calendar year it ends in (1 July 2025 to 30 June 2026 is fiscal 2026).
 * Every date-derived value that used to key off the calendar year — the membership number's year
 * segment, the fiscalYear label, tenure — reads its year basis from here.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year (starting 1 July) that {@code date} falls in. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The two-digit fiscal-year segment, e.g. {@code "26"}. */
    public static String twoDigit(LocalDate date) {
        return String.format("%02d", of(date) % 100);
    }

    /** The fiscal-year label {@code "FY<YY>"}, e.g. {@code "FY26"}. */
    public static String label(LocalDate date) {
        return "FY" + twoDigit(date);
    }
}
