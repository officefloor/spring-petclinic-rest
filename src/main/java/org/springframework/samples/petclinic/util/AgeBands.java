package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Age-band helper: classifies a birth date relative to a reference date as
 * 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+).
 */
public final class AgeBands {

    private AgeBands() {
    }

    /** The age band of {@code birthDate} as of {@code asOf}, or null when either date is absent. */
    public static String of(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
        int years = Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return (years < 65) ? "ADULT" : "SENIOR";
    }
}
