package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * Fiscal-year arithmetic for date-derived owner values. The fiscal year starts on 1 July and is
 * named by the calendar year in which it ends, so a date in July–December belongs to the next
 * calendar year's fiscal year (e.g. 2025-08-10 → fiscal year 2026 → {@code FY26}) while a date in
 * January–June keeps the calendar year (e.g. 2026-03-01 → fiscal year 2026 → {@code FY26}).
 */
public final class FiscalYear {

    /** First month (inclusive) of a new fiscal year. */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    private FiscalYear() {
    }

    /** The fiscal year (starting 1 July) that {@code date} falls in. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal-year label {@code FY<YY>} for {@code date}, YY being the last two digits. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }
}
