package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * An owner's tenure: the number of whole days from their registration date up to today. A newly
 * created owner, registered today, has zero tenure. Zero when the owner is {@code null} or has no
 * registration date. Depends on the current date, so unlike the other derived fields it is not a
 * pure function of the owner's own fields.
 */
public final class Tenure {

    private Tenure() {
    }

    /** The tenure of {@code owner} in whole days, or {@code 0} when it cannot be measured. */
    public static long days(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(owner.getRegistrationDate(), LocalDate.now());
    }
}
