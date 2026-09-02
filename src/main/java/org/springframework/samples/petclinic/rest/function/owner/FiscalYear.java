package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July and is numbered by the calendar year in
 * which it ends, so 1 July 2026 - 30 June 2027 is fiscal year 2027. Values are derived from the
 * owner's business-day-adjusted {@code registrationDate}.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** Fiscal year number for a date (its ending calendar year). */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The owner's fiscal year formatted {@code FY<YY>}, e.g. {@code FY27}. */
    public static String label(Owner owner) {
        return String.format("FY%02d", of(owner.getRegistrationDate()) % 100);
    }

    /** Two-digit fiscal year of the owner's registration, e.g. {@code "27"}. */
    public static String yy(Owner owner) {
        return String.format("%02d", of(owner.getRegistrationDate()) % 100);
    }
}
