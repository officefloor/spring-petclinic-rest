package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from a birth date measured against the
 * registration date: MINOR (under 18), ADULT (18-64) or SENIOR (65+).
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        int years = Period.between(birthDate, registrationDate).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return years < 65 ? "ADULT" : "SENIOR";
    }
}
