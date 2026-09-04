package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year helpers on a 1 July basis. The fiscal year runs 1 July to 30 June and is labelled by
 * the calendar year in which it ends: a date on or after 1 July belongs to the fiscal year ending
 * the following calendar year (e.g. {@code 2026-09-04 -> FY27}), while a date before 1 July belongs
 * to the fiscal year ending in its own calendar year (e.g. {@code 2026-03-01 -> FY26}). Every
 * date-derived value keyed on the (business-day adjusted) registration date uses this basis.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The calendar year in which the fiscal year containing {@code date} ends. */
    public static int endingYear(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The {@code FY<YY>} label for the fiscal year containing {@code date}, or {@code null} when
     *  {@code date} is {@code null}. YY is the last two digits of the fiscal year's ending year. */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", endingYear(date) % 100);
    }

    /** The number of whole fiscal years elapsed between {@code from} and {@code to} — the count of
     *  1 July boundaries crossed. */
    public static long elapsedBetween(LocalDate from, LocalDate to) {
        return (long) endingYear(to) - endingYear(from);
    }
}
