package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Fiscal-year calendar: the fiscal year starts on 1 July and is labelled by the calendar
 * year it ends in (so 2025-07-01..2026-06-30 is fiscal year 2026 / {@code FY26}). Owns the
 * single 1-July rule that the membership number, {@link TenureFactor} and the {@code fiscalYear}
 * response field all share.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year of {@code date} as a number — the calendar year the fiscal year ends in. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal-year label {@code FY<YY>} for {@code date}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }
}
