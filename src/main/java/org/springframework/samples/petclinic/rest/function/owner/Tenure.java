package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The single definition of an owner's "tenure": the number of whole days that have elapsed
 * since the owner's registration date. A newly registered owner — one registered as of the
 * reference date — has zero tenure. Provides the measure {@link AssignMembershipLevel} uses to
 * gate the top membership level, which is reserved for long-standing owners.
 */
final class Tenure {

    private Tenure() {
    }

    /** Whole days between {@code registrationDate} and {@code asOf}; zero when the date is
     *  unset or still in the future. */
    static long days(LocalDate registrationDate, LocalDate asOf) {
        if (registrationDate == null) {
            return 0;
        }
        return Math.max(ChronoUnit.DAYS.between(registrationDate, asOf), 0);
    }
}
