package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * The single definition of the <em>fiscal year</em> a date falls in, where the fiscal year starts
 * on 1 July and is named by the calendar year it ends in — so 1 July 2025 to 30 June 2026 is fiscal
 * year 2026, labelled {@code FY26}.
 *
 * <p>Used by the response mapper to derive an owner's {@code fiscalYear} label and the year segment
 * of their membership number, and by {@link Tenure} to count elapsed fiscal years — all from the
 * business-day-adjusted registration date stamped on the owner at creation.
 */
public final class FiscalYear {

    /** The month a fiscal year starts on. */
    private static final Month START = Month.JULY;

    private FiscalYear() {
    }

    /** The fiscal year (named by the calendar year it ends in) that contains {@code date}. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= START.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The {@code FY<YY>} label for the fiscal year containing {@code date}, where YY is the last
     * two digits of the fiscal year, or null when the date is unknown. */
    public static String label(LocalDate date) {
        return date == null ? null : String.format("FY%02d", of(date) % 100);
    }
}
