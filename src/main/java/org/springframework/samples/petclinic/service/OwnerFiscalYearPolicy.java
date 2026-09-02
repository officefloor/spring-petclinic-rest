package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: date-derived values are placed on a fiscal-year basis, where the fiscal year starts
 * on 1 July. A date in July&ndash;December therefore belongs to the fiscal year identified by the
 * following calendar year. Kept as a small, self-contained unit so the membership number, tenure and
 * the {@code fiscalYear} field can all share the one derivation without adding complexity elsewhere.
 */
public final class OwnerFiscalYearPolicy {

    private OwnerFiscalYearPolicy() {
    }

    /** The fiscal year {@code date} falls in, identified by the calendar year it ends in. */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derive the {@code fiscalYear} label 'FY&lt;YY&gt;' from the owner's registration date, or
     * {@code null} when no registration date is available.
     */
    public static String fiscalYear(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? null : String.format("FY%02d", fiscalYearOf(registrationDate) % 100);
    }
}
