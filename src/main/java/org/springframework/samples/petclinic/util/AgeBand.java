package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from their birth date, measured against the date the
 * owner was registered.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+); null when no birth date. */
    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int years = Period.between(birthDate, registrationDate).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
