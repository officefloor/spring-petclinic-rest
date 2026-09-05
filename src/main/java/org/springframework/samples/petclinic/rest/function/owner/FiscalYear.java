package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year helper. The fiscal year starts on 1 July and is numbered by the calendar year it ends
 * in, so 1 July 2025 – 30 June 2026 is fiscal year 2026. Every date-derived value on an owner (the
 * {@code memberId} FY segment, the {@code fiscalYear} label and tenure) is measured on this
 * basis, always from the business-day-adjusted registration date.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year (starting 1 July) containing {@code date}, numbered by the year it ends in. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The last two digits of the fiscal year of {@code date} (the {@code <YY>} identifier segment). */
    public static int yy(LocalDate date) {
        return of(date) % 100;
    }

    /** The {@code 'FY<YY>'} label for {@code date}, or {@code null} when {@code date} is {@code null}. */
    public static String label(LocalDate date) {
        return date == null ? null : String.format("FY%02d", yy(date));
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to} (the number of 1 July boundaries crossed). */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
