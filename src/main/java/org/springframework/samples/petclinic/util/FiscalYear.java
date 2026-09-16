package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year calendar for owner date-derived values. The fiscal year starts on 1 July
 * and is named after the calendar year in which it begins, so 1 July 2026 through 30 June
 * 2027 is fiscal year 2026 ("FY26"). This single primitive backs everything derived on a
 * fiscal-year basis: the owner's {@code fiscalYear} field, the membership number's year
 * segment ({@code -M<YY>}) and the tenure count in
 * {@link MembershipPoints}.
 */
public final class FiscalYear {

    /** The month in which each fiscal year starts (1 July). */
    public static final Month START = Month.JULY;

    private FiscalYear() {
    }

    /** The fiscal year a date falls in, as the calendar year in which that fiscal year
     *  starts (e.g. 2026 for any date from 1 July 2026 to 30 June 2027). */
    public static int of(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= START.getValue() ? year : year - 1;
    }

    /** The last two digits of a date's fiscal year, zero-padded (e.g. "26"). */
    public static String shortLabel(LocalDate date) {
        return String.format("%02d", of(date) % 100);
    }

    /** A date's fiscal year as an {@code FY<YY>} label (e.g. "FY26"). */
    public static String label(LocalDate date) {
        return "FY" + shortLabel(date);
    }

    /** The whole fiscal years elapsed from {@code from} to {@code to} — the number of
     *  1 July boundaries crossed between them, negative when {@code to} precedes
     *  {@code from}. */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
