package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from a birth date measured against the registration date: 'MINOR' when
 * under 18, 'ADULT' from 18 up to and including 64, and 'SENIOR' at 65 or over. The band is only
 * defined when a birth date was supplied.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** Returns the age band label for {@code birthDate} as at {@code registrationDate}, or {@code null}
     *  when either date is absent (no birth date means no band). */
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
