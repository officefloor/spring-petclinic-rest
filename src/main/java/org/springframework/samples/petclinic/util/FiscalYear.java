package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July, so a date in July
 * through December belongs to the fiscal year that ends in the following calendar year, while a
 * date in January through June belongs to the fiscal year ending in its own calendar year. Fiscal
 * years are numbered by the calendar year they end in (e.g. 1 July 2026 through 30 June 2027 is
 * fiscal year 2027, labelled {@code FY27}).
 */
public final class FiscalYear {

    /** Month (1-12) on whose first day the fiscal year begins. */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    private FiscalYear() {
    }

    /** The fiscal year {@code date} falls in, numbered by the calendar year it ends in. */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /** The {@code FY<YY>} label for {@code date}, where {@code YY} is the last two digits of its
     *  fiscal year, zero-padded (e.g. {@code FY27}). */
    public static String labelOf(LocalDate date) {
        return String.format("FY%02d", yearOf(date) % 100);
    }

    /** The number of whole fiscal years elapsed from {@code from} to {@code to}, i.e. the fiscal
     *  year of {@code to} minus the fiscal year of {@code from}. */
    public static int elapsedYears(LocalDate from, LocalDate to) {
        return yearOf(to) - yearOf(from);
    }
}
