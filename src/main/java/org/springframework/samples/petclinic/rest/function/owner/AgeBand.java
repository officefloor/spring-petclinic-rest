package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from an optional birth date, measured against the
 * registration date: {@code MINOR} (under 18), {@code ADULT} (18-64) or
 * {@code SENIOR} (65+). Returns {@code null} when no birth date was supplied.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int years = Period.between(birthDate, registrationDate).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return years < 65 ? "ADULT" : "SENIOR";
    }
}
