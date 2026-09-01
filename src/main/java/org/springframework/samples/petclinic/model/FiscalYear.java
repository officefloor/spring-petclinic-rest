package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year helper. The fiscal year runs from 1 July to 30 June and is
 * identified by the calendar year in which it starts.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** Calendar year of the fiscal year that contains {@code date}. */
    public static int startYearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /** The fiscal year of {@code date} formatted as {@code FY<YY>}. */
    public static String labelOf(LocalDate date) {
        return String.format("FY%02d", startYearOf(date) % 100);
    }

    /** Whole fiscal years elapsed from {@code date} up to today. */
    public static long elapsedSince(LocalDate date) {
        return startYearOf(LocalDate.now()) - startYearOf(date);
    }
}
