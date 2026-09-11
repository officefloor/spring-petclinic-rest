package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year rules for owner date-derived values.
 *
 * <p>The fiscal year starts on 1 July, so a date in July–December falls in the fiscal
 * year that ends in the <em>following</em> calendar year. A fiscal year is identified by
 * the calendar year in which it ends and labelled {@code FY<YY>} (the last two digits of
 * that ending year); e.g. 1 July 2026 – 30 June 2027 is {@code FY27}.
 *
 * <p>Callers pass the business-day-adjusted registration date (an owner's stored
 * {@code registrationDate} is already adjusted by {@link BusinessDays}).
 */
public final class FiscalYears {

    private FiscalYears() {
    }

    /** The fiscal year (its ending calendar year) that {@code date} falls in. */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The two-digit fiscal-year segment {@code <YY>} for {@code date} (0–99). */
    public static int yearSegment(LocalDate date) {
        return fiscalYear(date) % 100;
    }

    /** The fiscal-year label {@code FY<YY>} for {@code date}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", yearSegment(date));
    }
}
