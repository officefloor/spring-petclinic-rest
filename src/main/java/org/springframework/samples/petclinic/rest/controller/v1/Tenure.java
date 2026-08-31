package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYears;

/**
 * Membership tenure rules. An owner's tenure is the number of fiscal years elapsed since
 * their registration date (the fiscal year starts on 1 July); level 4 requires a tenure of
 * more than one fiscal year, so a newly created owner (zero tenure) can never exceed level 3.
 */
final class Tenure {

    private Tenure() {
    }

    /** True when more than one fiscal year has elapsed since the owner registered. */
    static boolean exceedsOneYear(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null && FiscalYears.elapsed(registered, LocalDate.now()) > 1;
    }
}
