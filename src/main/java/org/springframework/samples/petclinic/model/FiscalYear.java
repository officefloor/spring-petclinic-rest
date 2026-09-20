package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * The fiscal year a date falls in. The fiscal year starts on 1 July and is named by the
 * calendar year in which it ends, so 1 July 2025 - 30 June 2026 is fiscal year 2026. This is
 * the single source of the fiscal-year basis shared by the membership number's year segment
 * ({@link MembershipNumber}), the tenure count ({@link MembershipLevel}) and the owner's
 * {@code fiscalYear} field.
 */
public final class FiscalYear {

    /** The month a fiscal year starts on. */
    private static final Month START_MONTH = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The fiscal year that {@code date} falls in: its calendar year, or the next calendar
     * year once the date is on or after 1 July.
     */
    public static int of(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= START_MONTH.getValue() ? year + 1 : year;
    }

    /** The fiscal year of {@code date} formatted as {@code FY<YY>}, e.g. {@code FY26}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /**
     * The number of fiscal years elapsed between {@code from} and {@code to}: the difference
     * of their fiscal years, so a span that crosses one 1 July boundary is one elapsed
     * fiscal year.
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
