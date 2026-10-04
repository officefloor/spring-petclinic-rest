package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

/**
 * Single source of truth for an owner's fiscal year. The fiscal year starts on 1 July
 * and is named by the calendar year in which it ends, so a date from 1 July 2025 to
 * 30 June 2026 falls in fiscal year 2026.
 *
 * <p>Everything date-derived that is tracked on a fiscal-year basis — the {@code memberId}'s
 * {@code FY} segment and the membership-tenure count — reads through here, off the
 * (business-day-adjusted) registration date stored on the owner.
 */
public final class OwnerFiscalYear {

    /** The fiscal year begins on the first day of this month. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private OwnerFiscalYear() {
    }

    /** The fiscal year (the calendar year it ends in) that {@code date} falls in. */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal year of {@code date} formatted {@code FY<YY>} (e.g. {@code FY26}). */
    public static String fiscalYearLabel(LocalDate date) {
        return String.format("FY%02d", fiscalYearOf(date) % 100);
    }

    /** Number of whole fiscal years elapsed from {@code from} to {@code to} (0 when in the same fiscal year). */
    public static int elapsedFiscalYears(LocalDate from, LocalDate to) {
        return fiscalYearOf(to) - fiscalYearOf(from);
    }
}
