package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year arithmetic. The fiscal year starts on 1 July and is identified by the
 * calendar year in which it ends: a date in July-December belongs to the fiscal year ending
 * the following June ({@code year + 1}); a date in January-June belongs to the fiscal year
 * ending that same June ({@code year}).
 *
 * <p>Used for the date-derived owner values (the {@code memberId}'s FY segment, the
 * {@code fiscalYear} field, and tenure), all taken from the business-day-adjusted
 * {@code registrationDate}.
 *
 * <p>A plain utility (not an OfficeFloor function).
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year (as a full calendar year, e.g. 2026) that {@code date} falls in. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal year formatted {@code FY<YY>} (the last two digits), e.g. {@code FY26}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to} (0 within the same fiscal year). */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
