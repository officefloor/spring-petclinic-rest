package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year arithmetic for the owner's date-derived values. The fiscal year starts on 1 July, so
 * a date in July through December belongs to the fiscal year named by the <em>following</em>
 * calendar year (the year the fiscal year ends in). Everything date-derived — the {@code fiscalYear}
 * label, the membership number's year segment and tenure — hangs off this single definition.
 */
public final class FiscalYears {

    private FiscalYears() {
    }

    /**
     * The fiscal year containing {@code date}, named by the calendar year the fiscal year ends in.
     * The fiscal year runs 1 July to 30 June, so July-December fall in {@code year + 1} and
     * January-June fall in {@code year} (e.g. 2026-09-06 -> 2027, 2026-03-01 -> 2026).
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The {@code FY<YY>} label for {@code date}'s fiscal year (last two digits, e.g. 'FY27'). */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /**
     * Whole fiscal years elapsed from {@code start} to {@code end}: the fiscal year of {@code end}
     * minus the fiscal year of {@code start}. A date and any later date within the same fiscal year
     * yield 0.
     */
    public static int elapsed(LocalDate start, LocalDate end) {
        return of(end) - of(start);
    }
}
