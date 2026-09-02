package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Fiscal-year basis for date-derived owner values. The fiscal year starts on 1 July and is
 * labelled by the calendar year it ends in, taken from the business-day-adjusted registration date.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year (the calendar year it ends in) of a business-day-adjusted date. */
    public static int of(LocalDate date) {
        LocalDate rolled = BusinessDay.roll(date);
        return rolled.getMonthValue() >= 7 ? rolled.getYear() + 1 : rolled.getYear();
    }

    /** {@code FY<YY>} for an owner's registration date. */
    public static String label(Owner owner) {
        return "FY" + String.format("%02d", of(owner.getRegistrationDate()) % 100);
    }

    /** Elapsed fiscal years between an owner's registration date and today. */
    public static int tenure(Owner owner) {
        LocalDate registration = owner.getRegistrationDate();
        return registration == null ? 0 : of(LocalDate.now()) - of(registration);
    }
}
