package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July, so a date in July–December
 * belongs to the fiscal year of the following calendar year. Derived values are taken
 * from the (already business-day-adjusted) registration date.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The four-digit fiscal year containing {@code date} (July–December roll into the next year). */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal year formatted {@code FY<YY>} (last two digits). */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }
}
