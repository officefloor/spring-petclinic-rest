package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from a date. The fiscal year runs 1 July to 30 June and is named by the
 * calendar year it ends in: a date on or after 1 July belongs to the fiscal year ending the following
 * June (e.g. {@code 2025-08-16} is in the fiscal year ending June 2026, labelled {@code FY26}), and a
 * date before 1 July belongs to the fiscal year ending that same June.
 *
 * <p>All date-derived owner values that were previously on a calendar-year basis now use this: the
 * {@code fiscalYear} label returned on the DTO, the membership number's year segment
 * (see {@link AssignMembershipNumber}) and the tenure count (see {@link MembershipLevel}). Every one
 * is measured from the business-day-adjusted {@code registrationDate} resolved at creation
 * (see {@link ResolveRegistrationDate}).
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The calendar year the fiscal year containing {@code date} ends in (e.g. 2026 for {@code FY26}). */
    public static int endingYearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal-year label for {@code date}, formatted {@code FY<YY>} (e.g. {@code FY26}); {@code null} when the date is absent. */
    public static String labelOf(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", endingYearOf(date) % 100);
    }

    /** Elapsed whole fiscal years between {@code from} and {@code to} (0 within the same fiscal year). */
    public static int elapsedYears(LocalDate from, LocalDate to) {
        return endingYearOf(to) - endingYearOf(from);
    }
}
