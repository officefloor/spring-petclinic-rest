package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July and is identified by the calendar year
 * in which it began, so a date in Jul-Dec belongs to that year's fiscal year and a date in
 * Jan-Jun belongs to the previous year's.
 */
public final class Fiscal {

    private Fiscal() {
    }

    /** Calendar year the fiscal year (starting 1 July) containing {@code date} began in. */
    public static int startYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
    }

    /** 'FY&lt;YY&gt;' label for the fiscal year {@code date} falls in. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", startYear(date) % 100);
    }
}
