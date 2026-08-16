package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Fiscal-year arithmetic for owner date-derived values. The fiscal year starts on 1 July and is
 * named by the calendar year in which it ends (1 July 2025 to 30 June 2026 is {@code FY26}), so a
 * date in July to December belongs to the following calendar year's fiscal year.
 *
 * <p>Used for the {@code fiscalYear} field, the memberId's FY segment and the fiscal-year tenure
 * count, so every fiscal-year-derived value stays consistent.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** First month of the fiscal year. */
    public static final Month START = Month.JULY;

    /** The calendar year in which the fiscal year containing {@code date} ends. */
    public static int endingYear(LocalDate date) {
        return date.getMonthValue() >= START.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The last two digits of the fiscal year, zero-padded (e.g. {@code "26"}). */
    public static String twoDigit(LocalDate date) {
        return String.format("%02d", Math.floorMod(endingYear(date), 100));
    }

    /** The fiscal year label, {@code 'FY<YY>'} (e.g. {@code "FY26"}). */
    public static String label(LocalDate date) {
        return "FY" + twoDigit(date);
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to} (0 when both fall in one fiscal year). */
    public static int elapsed(LocalDate from, LocalDate to) {
        return endingYear(to) - endingYear(from);
    }

    /** The owner's fiscal year, derived from its (business-day-adjusted) registration date. */
    public static String forOwner(Owner owner) {
        return label(owner.getRegistrationDate());
    }
}
