package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from a date, where the fiscal year starts on 1 July.
 *
 * <p>A date on or after 1 July belongs to the fiscal year that ends the following 30 June, so it is
 * named by that ending calendar year; a date before 1 July belongs to the fiscal year ending in its
 * own calendar year (e.g. 2026-07-01 and 2027-06-30 both fall in fiscal year 2027, while 2026-06-30
 * falls in fiscal year 2026).
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * method declared on a MapStruct mapper would be picked up as an implicit conversion and applied to
 * every matching property mapping.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * Returns the fiscal year (the ending calendar year) the given date falls in.
     */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Returns the fiscal year of {@code date} formatted as {@code "FY<YY>"} where {@code YY} is the
     * last two digits of the fiscal year (e.g. {@code "FY27"}), or {@code null} when {@code date} is
     * absent so no fiscal year can be derived.
     */
    public static String of(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", yearOf(date) % 100);
    }
}
