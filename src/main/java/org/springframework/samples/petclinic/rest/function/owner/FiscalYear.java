package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year arithmetic for date-derived owner values. The fiscal year starts on
 * 1 July and is named for the calendar year it ends in, so 1 July 2025 begins the
 * fiscal year 'FY26'.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** Calendar year the fiscal year containing {@code date} ends in (July onwards rolls forward). */
    public static int endYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** 'FY&lt;YY&gt;' label for {@code date}, e.g. FY26. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", endYear(date) % 100);
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to}. */
    public static int elapsed(LocalDate from, LocalDate to) {
        return endYear(to) - endYear(from);
    }
}
