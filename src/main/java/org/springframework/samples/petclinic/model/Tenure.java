package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The single definition of an owner's <em>tenure</em>: the number of whole days from their
 * registration date up to today. A freshly registered owner has zero tenure; the count grows by
 * one each day thereafter.
 *
 * <p>Used by the response mapper to decide the tenure-gated top membership level. Unknown or
 * future registration dates yield zero, so they can never satisfy a tenure threshold.
 */
public final class Tenure {

    private Tenure() {
    }

    /** Whole days between {@code registrationDate} and today, or 0 when the date is unknown or
     * lies in the future. */
    public static long days(LocalDate registrationDate) {
        if (registrationDate == null) {
            return 0;
        }
        return Math.max(ChronoUnit.DAYS.between(registrationDate, LocalDate.now()), 0);
    }
}
