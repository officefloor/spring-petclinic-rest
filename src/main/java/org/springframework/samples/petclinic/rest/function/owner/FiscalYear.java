package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year rules for date-derived owner values. The fiscal year starts on 1 July, so a date in
 * July or later belongs to the fiscal year named after that calendar year, and a date from January
 * to June belongs to the fiscal year that started on the previous 1 July.
 *
 * <p>The fiscal year is labelled {@code FY<YY>} where {@code YY} is the last two digits of the
 * fiscal year's start-year (e.g. 1 July 2026 to 30 June 2027 is {@code FY26}). Every value derived
 * from the registration date works from the business-day-adjusted date already stored on the owner.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * The calendar year in which the fiscal year containing {@code date} started: the date's own
     * year from 1 July onwards, otherwise the previous year.
     */
    public static int startYear(LocalDate date) {
        int year = date.getYear();
        return date.getMonth().compareTo(Month.JULY) >= 0 ? year : year - 1;
    }

    /**
     * The {@code FY<YY>} label for {@code date}, where {@code YY} is the last two digits of the
     * fiscal year's start-year. Returns {@code null} when {@code date} is absent.
     */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", startYear(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed between {@code from} and {@code to}, i.e. the
     * difference of their fiscal-year start-years. Zero (or negative) when both dates fall in the
     * same fiscal year, so a freshly registered owner has zero elapsed fiscal years.
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return startYear(to) - startYear(from);
    }
}
