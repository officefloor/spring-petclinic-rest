package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives an owner's fiscal year from their (business-day-adjusted) registrationDate.
 *
 * <p>The fiscal year starts on 1 July and is labelled by the calendar year in which it
 * ends: a date on or after 1 July falls in the fiscal year ending the following 30 June.
 * For example 1 July 2025 and 12 September 2025 both fall in fiscal year 2026, while 30
 * June 2025 falls in fiscal year 2025.
 *
 * <p>{@link #of(Owner)} formats this as {@code 'FY<YY>'} (the two-digit ending year, e.g.
 * {@code 'FY26'}); {@link #yearOf(LocalDate)} exposes the four-digit ending year for
 * callers that build their own segments (such as the membership number).
 */
public final class FiscalYear {

    /** The month on whose first day the fiscal year begins. */
    private static final int START_MONTH = Month.JULY.getValue();

    private FiscalYear() {
    }

    /** The fiscal year label ({@code 'FY<YY>'}) for the owner, or null when unregistered. */
    public static String of(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return "FY" + String.format("%02d", yearOf(registrationDate) % 100);
    }

    /** The fiscal year (the calendar year it ends in) that {@code date} falls in. */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= START_MONTH ? date.getYear() + 1 : date.getYear();
    }
}
