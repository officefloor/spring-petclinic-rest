package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Fiscal-year helpers for a year that starts on 1 July. A date on or after 1 July
 * belongs to the fiscal year named by that calendar year; an earlier date belongs to
 * the previous one.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year (its starting calendar year) that contains {@code date}. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
    }

    /** The 'FY&lt;YY&gt;' label for the fiscal year that contains {@code date}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", Math.floorMod(of(date), 100));
    }
}
