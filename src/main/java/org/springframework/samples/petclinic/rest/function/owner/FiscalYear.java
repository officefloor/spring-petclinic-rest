package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July and is named for the
 * calendar year in which it ends: a date on or after 1 July belongs to the next
 * calendar year's fiscal year (e.g. 2025-07-01 -> FY26), a date before 1 July to
 * the current calendar year's (e.g. 2026-06-30 -> FY26).
 */
public final class FiscalYear {

    /** First month of the fiscal year (July). */
    private static final int FISCAL_START_MONTH = 7;

    private FiscalYear() {
    }

    /** The full four-digit fiscal year that {@code date} falls in. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= FISCAL_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal-year label 'FY<YY>' (two-digit) for {@code date}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }
}
