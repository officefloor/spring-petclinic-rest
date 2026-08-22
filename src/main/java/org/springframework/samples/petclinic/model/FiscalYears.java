package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July, so July to
 * December belong to the fiscal year named by that same calendar year and January to June
 * belong to the fiscal year that started in the previous calendar year. Kept as a plain
 * static helper so the single rule is shared by the response mapper, the membership points
 * and the create audit line.
 */
public final class FiscalYears {

    private FiscalYears() {
    }

    /**
     * The calendar year in which the fiscal year containing {@code date} began.
     */
    public static int startYear(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= Month.JULY.getValue() ? year : year - 1;
    }

    /**
     * The fiscal year label {@code 'FY<YY>'} for {@code date}, where YY is the last two
     * digits of {@link #startYear(LocalDate)}.
     */
    public static String label(LocalDate date) {
        return String.format("FY%02d", startYear(date) % 100);
    }

    /**
     * Whole fiscal years elapsed between {@code from} and {@code to}, i.e. the number of
     * 1 July boundaries crossed. Zero when both dates fall in the same fiscal year.
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return startYear(to) - startYear(from);
    }
}
