package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives the owner's age band from their birth date measured against the registration
 * date: {@code MINOR} (under 18), {@code ADULT} (18-64) or {@code SENIOR} (65+). Returns
 * {@code null} when either date is absent, so an owner with no birth date has no band.
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
