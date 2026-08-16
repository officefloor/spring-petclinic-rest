package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from their {@code birthDate} measured against the
 * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64 inclusive,
 * {@code SENIOR} at 65 or over. Returns {@code null} when either date is absent, so an owner with no
 * supplied birth date has no age band.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * The age band for an owner born on {@code birthDate}, computed as the completed years between
     * that date and {@code registrationDate}. Returns {@code null} when either date is missing.
     */
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
