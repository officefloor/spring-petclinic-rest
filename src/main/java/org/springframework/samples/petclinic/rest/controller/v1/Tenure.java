package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership tenure rules. An owner's tenure is the number of days elapsed since their
 * registration date; level 4 requires tenure of more than 365 days, so a newly created
 * owner (zero tenure) can never exceed level 3.
 */
final class Tenure {

    private Tenure() {
    }

    /** True when the owner has been registered for more than 365 days. */
    static boolean exceedsOneYear(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null && ChronoUnit.DAYS.between(registered, LocalDate.now()) > 365;
    }
}
