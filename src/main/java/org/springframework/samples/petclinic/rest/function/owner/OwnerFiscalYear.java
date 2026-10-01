package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's fiscal year — the financial year that starts on 1 July.
 *
 * <p>The fiscal year is named for the calendar year in which it <em>ends</em>: the year running
 * 1 July 2025 to 30 June 2026 is fiscal year 2026. Its label is {@code FY<YY>}, where {@code YY}
 * is the last two digits of that ending year (e.g. a date in October 2026 falls in fiscal year
 * 2027 and so has label {@code FY27}).
 *
 * <p>Every date-derived value that moved to a fiscal-year basis — the {@code fiscalYear} read
 * field, the membership number's year segment, and tenure — resolves through this class so they
 * cannot drift apart. All derive from the already-stored, business-day-adjusted
 * {@code registrationDate}.
 */
public final class OwnerFiscalYear {

    private OwnerFiscalYear() {
    }

    /**
     * The fiscal year a date falls in, named by its ending calendar year: the date's own year when
     * it is on or after 1 July, otherwise the previous year rolled forward — i.e. the date's year
     * plus one from July onwards.
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The {@code FY<YY>} label for an owner's business-day-adjusted {@code registrationDate}, or
     * {@code null} when no registration date is recorded.
     */
    public static String label(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? null : label(registrationDate);
    }

    /** The {@code FY<YY>} label for a date (e.g. {@code FY27} for October 2026). */
    public static String label(LocalDate date) {
        return String.format("FY%02d", fiscalYear(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed between two dates — the difference of their fiscal
     * years, never negative. Each crossing of a 1 July boundary counts as one elapsed fiscal year.
     */
    public static long elapsedFiscalYears(LocalDate from, LocalDate to) {
        return Math.max(fiscalYear(to) - fiscalYear(from), 0);
    }
}
