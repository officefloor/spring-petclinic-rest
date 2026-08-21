package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July and is identified by the calendar year in
 * which it ends: a date on or after 1 July belongs to the fiscal year of the following calendar year
 * (e.g. 2 July 2025 -> FY26), a date before 1 July to the current calendar year (e.g. 3 June 2026 ->
 * FY26). Fiscal-year values are derived from the business-day-adjusted registration date, so every
 * caller sees the same adjusted basis (see {@link BusinessDay}).
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year for a date as a full calendar year (the year the fiscal year ends in). */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal-year label {@code FY<YY>} for a date (e.g. {@code FY26}). */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /**
     * Whole fiscal years elapsed between two dates, as the difference of their fiscal years; zero
     * when both fall in the same fiscal year, and non-negative when {@code to} is not before
     * {@code from}.
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
