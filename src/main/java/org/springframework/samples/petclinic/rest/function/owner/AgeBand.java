package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from their birthDate relative to registrationDate:
 * 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+). Returns null when no
 * birthDate is supplied.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = registrationDate == null ? LocalDate.now() : registrationDate;
        int years = Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
