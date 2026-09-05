package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July, so a date on or after 1 July
 * belongs to the fiscal year ending in the following calendar year. {@link #of} yields that
 * ending calendar year, {@link #label} formats it as {@code FY<YY>} (its last two digits),
 * and {@link #elapsed} counts whole fiscal years between two dates.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
