package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives fiscal-year values. The fiscal year starts on 1 July, so a date on or after 1 July belongs
 * to the next calendar year's fiscal year (e.g. 2025-07-01 through 2026-06-30 is fiscal year 2026).
 *
 * <p>The owner's stored {@code registrationDate} has already been rolled forward to a business day
 * (see {@link BusinessDay}), so values derived here are on the business-day-adjusted basis.
 */
public final class FiscalYears {

    /** Month (1-based) on which the fiscal year starts. */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    private FiscalYears() {
    }

    /** The four-digit fiscal year that {@code date} falls in. */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year label, formatted {@code 'FY<YY>'} where {@code YY} is the last two
     * digits of the fiscal year the {@code registrationDate} falls in; {@code null} when no date is
     * on record.
     */
    public static String labelOf(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        int yy = Math.floorMod(yearOf(registrationDate), 100);
        return String.format("FY%02d", yy);
    }

    /** Fiscal years elapsed since the owner's registration date; 0 when no date is on record. */
    public static long elapsedSinceRegistration(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return (long) yearOf(LocalDate.now()) - yearOf(registrationDate);
    }
}
