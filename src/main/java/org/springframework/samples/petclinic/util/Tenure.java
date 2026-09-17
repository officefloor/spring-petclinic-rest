package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * An owner's tenure: the number of whole {@link FiscalYear fiscal years} elapsed between their
 * registration date and today, i.e. how many fiscal-year boundaries (1 July) have been crossed since
 * registration. A newly created owner, registered in the current fiscal year, has zero tenure. Zero
 * when the owner is {@code null} or has no registration date. Depends on the current date, so unlike
 * the other derived fields it is not a pure function of the owner's own fields.
 */
public final class Tenure {

    private Tenure() {
    }

    /** The tenure of {@code owner} in whole fiscal years, or {@code 0} when it cannot be measured. */
    public static long fiscalYears(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return 0;
        }
        return FiscalYear.of(LocalDate.now()) - FiscalYear.of(owner.getRegistrationDate());
    }
}
