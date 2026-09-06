package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July, so a
 * date on or after 1 July belongs to the fiscal year that starts in that calendar
 * year, and a date before 1 July belongs to the fiscal year that started in the
 * previous calendar year. Kept as a plain static helper - rather than a method on
 * {@link OwnerMapper} - so MapStruct does not mistake it for an implicit property
 * mapping.
 *
 * <p>A fiscal year is identified by the calendar year in which it starts; its
 * label is {@code 'FY<YY>'}, where {@code YY} is the last two digits of that
 * starting year (for example a date of 15 August 2026 falls in {@code FY26}, and
 * 15 March 2026 falls in {@code FY25}).
 */
public final class FiscalYear {

    /** The month in which each fiscal year begins. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * Returns the calendar year in which the fiscal year containing {@code date}
     * starts: the date's own year when it falls on or after 1 July, otherwise the
     * previous year.
     *
     * @param date the date whose fiscal year to derive
     * @return the calendar year in which that fiscal year starts
     */
    public static int startYear(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START.getValue()
            ? date.getYear() : date.getYear() - 1;
    }

    /**
     * Returns the last two digits of the fiscal-year start year for {@code date}.
     *
     * @param date the date whose fiscal year to derive
     * @return the two-digit fiscal year (0-99)
     */
    public static int twoDigit(LocalDate date) {
        return Math.floorMod(startYear(date), 100);
    }

    /**
     * Returns the {@code 'FY<YY>'} label for the owner's (business-day-adjusted)
     * registration date.
     *
     * @param owner the owner whose fiscal year to derive
     * @return the fiscal-year label, for example {@code "FY26"}
     */
    public static String labelForOwner(Owner owner) {
        return String.format("FY%02d", twoDigit(owner.getRegistrationDate()));
    }

}
