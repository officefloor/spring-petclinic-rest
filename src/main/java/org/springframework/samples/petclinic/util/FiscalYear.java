package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * The fiscal-year basis for date-derived owner values. The fiscal year starts on 1 July,
 * so a date in July&ndash;December belongs to the fiscal year ending the following
 * calendar year, and a date in January&ndash;June to the fiscal year ending that same
 * year. This class owns the definition so every fiscal-year-derived value &mdash; the
 * {@code fiscalYear} label, the membership number's year segment and tenure in elapsed
 * fiscal years &mdash; stays in step.
 */
public final class FiscalYear {

    /** The first month of a fiscal year (1 July). */
    private static final int START_MONTH = 7;

    private FiscalYear() {
    }

    /**
     * The fiscal year a date falls in, identified by the calendar year it ends in: the
     * date's own year for January&ndash;June, or the next year for July&ndash;December.
     *
     * @param date the date to classify
     * @return the ending calendar year of the containing fiscal year
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The {@code FY<YY>} label for a date's fiscal year, where {@code <YY>} is the last two
     * digits of its {@link #of(LocalDate) ending year} (e.g. {@code FY26}).
     *
     * @param date the date to label
     * @return the fiscal-year label
     */
    public static String labelOf(LocalDate date) {
        return String.format("FY%02d", shortYearOf(date));
    }

    /**
     * The last two digits of a date's {@link #of(LocalDate) fiscal year}, for use as a
     * year segment (e.g. {@code 26}).
     *
     * @param date the date
     * @return the two-digit fiscal year
     */
    public static int shortYearOf(LocalDate date) {
        return of(date) % 100;
    }

    /**
     * The number of whole fiscal years elapsed between two dates: the difference between
     * their {@link #of(LocalDate) fiscal years}, never negative for a {@code from} on or
     * before {@code to}.
     *
     * @param from the earlier date
     * @param to   the later date
     * @return the count of fiscal years elapsed
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
