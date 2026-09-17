package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year calendar rules. The fiscal year starts on 1 July and is named by the calendar year it
 * ends in, so a date on or after 1 July belongs to the next year's fiscal year (e.g. 2025-07-01 and
 * 2026-06-30 both fall in fiscal year 2026). Pure date arithmetic with no dependency on other state,
 * so it is a shared helper rather than a step.
 */
public final class FiscalYear {

    /** The calendar month the fiscal year starts in. */
    public static final Month START_MONTH = Month.JULY;

    private FiscalYear() {
    }

    /** The fiscal year (named by the calendar year it ends in) that contains {@code date}. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= START_MONTH.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The 'FY&lt;YY&gt;' label for {@code date}, where YY is the last two digits of its fiscal year. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }
}
