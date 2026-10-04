package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Single source of truth for the fiscal-year rule shared across the owner pipeline and the
 * read-time {@code OwnerMapper}. The fiscal year starts on <strong>1 July</strong> and is
 * identified by the calendar year in which it <em>ends</em>: a date on or after 1 July belongs to
 * the fiscal year named for the following calendar year, while a date in January-June belongs to
 * the fiscal year named for its own calendar year (e.g. 2026-07-01 and 2027-06-30 are both
 * {@code FY27}; 2026-06-30 is {@code FY26}).
 *
 * <p>Centralised here (as {@link BusinessDay} is for the business-day roll) so every date-derived
 * value agrees on one definition: {@link AssignMemberId} builds the member id's fiscal-year
 * segment from it, while {@code OwnerMapper} derives the exposed {@code fiscalYear} label and
 * counts tenure in elapsed fiscal years from it. Callers pass the already business-day-adjusted
 * {@code registrationDate} the pipeline stored, so the fiscal year follows the adjusted date.
 */
public final class FiscalYear {

    /** The month the fiscal year starts on (1 July). */
    private static final int FISCAL_START_MONTH = 7;

    private FiscalYear() {
    }

    /**
     * The fiscal year containing {@code date}, identified by the calendar year in which it ends: the
     * date's own year when the date falls before 1 July, otherwise the following year.
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= FISCAL_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The last two digits of the fiscal year containing {@code date}, zero-padded (e.g. {@code 27}
     * for a fiscal year of 2027, {@code 09} for 2009). Shared so every rendering of the fiscal year
     * (the {@code FY<YY>} label, the member id's {@code FY} segment) derives its year segment the
     * same way.
     */
    public static String yy(LocalDate date) {
        return String.format("%02d", of(date) % 100);
    }

    /**
     * The {@code FY<YY>} label for the fiscal year containing {@code date}, where {@code YY} is the
     * last two digits of the fiscal year (e.g. {@code FY27}).
     */
    public static String label(LocalDate date) {
        return "FY" + yy(date);
    }

    /**
     * The number of whole fiscal years elapsed between {@code from} and {@code to}: the difference
     * between the fiscal year of each date, i.e. the count of 1-July boundaries crossed. Returns 0
     * when {@code to} is not after {@code from}.
     */
    public static long elapsed(LocalDate from, LocalDate to) {
        return Math.max(of(to) - of(from), 0);
    }
}
