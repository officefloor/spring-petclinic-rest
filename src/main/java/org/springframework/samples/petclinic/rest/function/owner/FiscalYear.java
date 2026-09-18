package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

/**
 * The single definition of the fiscal year, which starts on 1 July. A date on or after
 * 1 July belongs to the fiscal year beginning in that calendar year; a date before 1 July
 * belongs to the fiscal year that began the previous calendar year. A fiscal year is
 * identified by the calendar year in which it starts and labelled {@code "FY<YY>"} from that
 * year's last two digits (e.g. 1 July 2026 – 30 June 2027 is {@code "FY26"}).
 *
 * <p>Provides the fiscal basis for every date-derived value: the membership number's year
 * segment and the {@code fiscalYear} field (both via {@code OwnerMapper}), and an owner's
 * {@link Tenure tenure} measured in elapsed fiscal years.
 */
public final class FiscalYear {

    /** First month of the fiscal year. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /** The calendar year in which the fiscal year containing {@code date} begins. */
    static int startYear(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_YEAR_START.getValue() ? year : year - 1;
    }

    /** The last two digits of the starting calendar year of the fiscal year containing
     *  {@code date} — the {@code YY} shared by the fiscal-year label and the membership number. */
    public static int shortYear(LocalDate date) {
        return startYear(date) % 100;
    }

    /** The fiscal-year label {@code "FY<YY>"} for {@code date}, or {@code null} when the date
     *  is absent. */
    public static String label(LocalDate date) {
        return date == null ? null : String.format("FY%02d", shortYear(date));
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to} — the number of 1 July
     *  boundaries crossed — never negative. Both dates must be non-null. */
    static long between(LocalDate from, LocalDate to) {
        return Math.max((long) startYear(to) - startYear(from), 0);
    }
}
