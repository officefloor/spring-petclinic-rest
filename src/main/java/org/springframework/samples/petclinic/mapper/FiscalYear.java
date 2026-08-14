package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July, so a date from 1 July
 * onward belongs to the fiscal year that ends the following 30 June; a date up to 30 June belongs to
 * the fiscal year ending that 30 June. Each fiscal year is labelled by the calendar year it ends in.
 *
 * <p>All date-derived owner values sit on this basis: the {@code fiscalYear} label, the membership
 * number's year segment and the tenure count (elapsed fiscal years) are all taken from the owner's
 * business-day-adjusted {@code registrationDate}.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * The fiscal year that {@code date} falls in, identified by the calendar year it ends in: the same
     * calendar year for a date up to 30 June, the next calendar year for a date from 1 July onward.
     */
    public static int endingYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The {@code 'FY<YY>'} label of {@code owner}'s registration fiscal year (YY being the last two
     * digits of the {@link #endingYear(LocalDate) ending year}), or {@code null} when the owner has no
     * registration date.
     */
    public static String label(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return "FY" + yearSegment(registrationDate);
    }

    /**
     * The two-digit year segment of {@code date}'s fiscal year, used for both the {@code fiscalYear}
     * label and the membership number's year segment.
     */
    public static String yearSegment(LocalDate date) {
        return String.format("%02d", endingYear(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed between {@code owner}'s registration date and today, i.e.
     * the number of 1-July boundaries crossed. Zero when the owner has no registration date.
     */
    public static int elapsedYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return endingYear(LocalDate.now()) - endingYear(registrationDate);
    }
}
