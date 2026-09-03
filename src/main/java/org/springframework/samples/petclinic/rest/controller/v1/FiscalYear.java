package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Fiscal-year helper: the fiscal year starts on 1 July and is named by the calendar year
 * in which it ends, so a date on or after 1 July belongs to the following year's fiscal year.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year {@code date} falls in - the calendar year the fiscal year ends. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The {@code FY<YY>} label of {@code owner}'s business-day-adjusted registration date. */
    public static String label(Owner owner) {
        return String.format("FY%02d", of(owner.getRegistrationDate()) % 100);
    }

    /** Whole fiscal years elapsed between {@code owner}'s registration and {@code today}. */
    public static int tenure(Owner owner, LocalDate today) {
        return of(today) - of(owner.getRegistrationDate());
    }
}
