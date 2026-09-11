package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from an owner's business-day-adjusted registration
 * date. The fiscal year runs from 1 July to 30 June and is identified by the
 * calendar year in which it ends (so a date on or after 1 July belongs to the
 * fiscal year ending the following calendar year). Kept out of {@link OwnerMapper}
 * so MapStruct does not mistake it for an implicit property mapping method.
 */
public final class FiscalYearResolver {

    /** The month the fiscal year starts on (1 July). */
    private static final int FISCAL_YEAR_START_MONTH = Month.JULY.getValue();

    private FiscalYearResolver() {
    }

    /**
     * Returns the fiscal year that {@code date} falls in, identified by the
     * calendar year in which the fiscal year ends. Dates from 1 July onwards
     * belong to the fiscal year ending the following calendar year; dates up to
     * 30 June belong to the fiscal year ending in the same calendar year.
     *
     * @param date the date to evaluate
     * @return the four-digit fiscal year
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Returns the fiscal year label {@code FY<YY>}, where {@code YY} is the last
     * two digits of the {@link #fiscalYear(LocalDate) fiscal year}.
     *
     * @param date the date to evaluate, may be {@code null}
     * @return the fiscal-year label, or {@code null} when no date was supplied
     */
    public static String deriveFiscalYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYear(date) % 100);
    }

    /**
     * Returns the number of whole fiscal years elapsed between {@code from} and
     * {@code to}, i.e. the number of 1 July boundaries crossed. Zero (or negative)
     * when both dates fall in the same fiscal year (or {@code to} precedes
     * {@code from}).
     *
     * @param from the earlier date
     * @param to   the later date
     * @return the number of elapsed fiscal years
     */
    public static int elapsedFiscalYears(LocalDate from, LocalDate to) {
        return fiscalYear(to) - fiscalYear(from);
    }
}
