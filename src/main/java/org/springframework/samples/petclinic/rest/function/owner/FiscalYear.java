package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year rules for date-derived owner values. The fiscal year starts on 1 July, so a date in
 * July or later belongs to the fiscal year ending the following calendar year (the Australian
 * convention): e.g. 1 July 2026 is fiscal year 2027, while 30 June 2026 is fiscal year 2026. Shared
 * by {@link AssignOwnerMemberId} (the {@code memberId}'s FY segment) and the owner mapper
 * (the {@code fiscalYear} field and tenure-based membership points) so every date-derived value uses
 * one definition.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year (the calendar year in which it ends) that {@code date} falls in. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal-year label {@code FY<YY>} (last two digits of the fiscal year) for {@code date}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }
}
