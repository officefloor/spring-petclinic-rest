package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year arithmetic for the owner's date-derived values. The fiscal year starts on 1 July:
 * a date on or after 1 July belongs to the fiscal year identified by <em>that</em> calendar year,
 * and a date before 1 July belongs to the fiscal year identified by the <em>previous</em> calendar
 * year. So 1 Jul 2026 – 30 Jun 2027 is fiscal year 2026, labelled {@code FY26}.
 *
 * <p>The single definition of the fiscal-year basis, shared by the owner mapper: the {@code fiscalYear}
 * field ({@link #label}), the membership number's year segment ({@link #yy}) and the membership-points
 * tenure ({@link #between}) all derive from here so they cannot drift apart. Every input is the
 * business-day-adjusted {@code registrationDate}, so all three reflect the same effective date.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The four-digit fiscal year the date falls in — the calendar year the 1 July year began. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /** The two-digit fiscal year (e.g. {@code 26}), used as the membership number's year segment. */
    public static int yy(LocalDate date) {
        return of(date) % 100;
    }

    /** The {@code FY<YY>} label for the date, e.g. {@code FY26} for any date in fiscal year 2026. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", yy(date));
    }

    /** Whole fiscal years elapsed from {@code start} to {@code end} (fiscal year of end minus of start). */
    public static long between(LocalDate start, LocalDate end) {
        return (long) of(end) - of(start);
    }
}
