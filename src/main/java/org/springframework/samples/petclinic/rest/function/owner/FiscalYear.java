package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year calendar: the fiscal year starts on 1 July and is named by the calendar year
 * in which it starts. Every value derives from the business-day-adjusted date, so a date that
 * falls on a weekend or holiday is rolled forward first (see {@link BusinessDay}).
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** Calendar year the fiscal year containing {@code date} starts in. */
    public static int startYearOf(LocalDate date) {
        LocalDate adjusted = BusinessDay.roll(date);
        return adjusted.getMonthValue() >= 7 ? adjusted.getYear() : adjusted.getYear() - 1;
    }

    /** Fiscal-year label 'FY<YY>' for {@code date}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", startYearOf(date) % 100);
    }

    /** Two-digit fiscal year for {@code date}, for use as an identifier segment. */
    public static int shortYear(LocalDate date) {
        return startYearOf(date) % 100;
    }

    /** Whole fiscal years elapsed between {@code date} and today. */
    public static int elapsedTo(LocalDate date, LocalDate today) {
        return startYearOf(today) - startYearOf(date);
    }
}
