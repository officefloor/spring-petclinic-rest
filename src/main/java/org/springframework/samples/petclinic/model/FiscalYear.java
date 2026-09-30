package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * The single definition of the fiscal year for date-derived owner values. The fiscal year
 * starts on 1 July and is named by the calendar year it ends in, so a date in July to
 * December belongs to the fiscal year ending the following calendar year (e.g. 2025-08-01
 * and 2026-03-01 are both in the fiscal year ending 2026 — 'FY26').
 *
 * <p>Used to derive an owner's {@code fiscalYear} label, the FY segment of the
 * {@code memberId} and the elapsed-fiscal-year tenure that feeds membership points.
 *
 * <p>Lives beside {@link Owner} — the same home as {@link AgeBand} and {@link CityRegions} —
 * so any layer can depend on it without a package cycle.
 */
public final class FiscalYear {

    /** The fiscal year begins on the first day of this month. */
    static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The calendar year the fiscal year containing {@code date} ends in: {@code date}'s year
     * when the month is before July, otherwise the following year.
     */
    public static int endingYearOf(LocalDate date) {
        return (date.getMonthValue() >= FISCAL_YEAR_START.getValue())
                ? date.getYear() + 1
                : date.getYear();
    }

    /**
     * The {@code 'FY<YY>'} label for the fiscal year containing {@code date}, where YY is the
     * last two digits of the {@link #endingYearOf(LocalDate) ending year}. {@code null} when
     * {@code date} is {@code null}.
     */
    public static String label(LocalDate date) {
        return (date == null) ? null : String.format("FY%02d", endingYearOf(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed from {@code from} to {@code to}, i.e. the
     * difference between their {@link #endingYearOf(LocalDate) ending years}. Zero when both
     * dates fall in the same fiscal year.
     */
    public static int elapsedYears(LocalDate from, LocalDate to) {
        return endingYearOf(to) - endingYearOf(from);
    }
}
