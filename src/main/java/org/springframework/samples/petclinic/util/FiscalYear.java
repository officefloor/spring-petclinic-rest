package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July and is named after the
 * calendar year in which it ends, so a date from July onwards belongs to the fiscal year of the
 * following calendar year (e.g. 2026-08-10 is fiscal year 2027) while a date in January to June
 * belongs to the fiscal year of its own calendar year. Used for the owner's derived {@code
 * fiscalYear} label, the membership number's year segment and the elapsed-fiscal-year tenure, all
 * read from the business-day-adjusted registration date.
 */
public final class FiscalYear {

    /** First month (1-based) of the fiscal year: July. */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    private FiscalYear() {
    }

    /** Returns the fiscal year number containing {@code date}: the calendar year, plus one when the
     *  date falls on or after 1 July (the fiscal year is named after the year in which it ends). */
    public static int of(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? year + 1 : year;
    }

    /** Returns the fiscal-year label for {@code date}, formatted 'FY<YY>' where YY is the last two
     *  digits of the fiscal year (e.g. 'FY27'). */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /** Returns the number of whole fiscal years elapsed between {@code from} and {@code to}, i.e. the
     *  count of 1 July boundaries crossed. Zero (or negative) when both dates share a fiscal year. */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
