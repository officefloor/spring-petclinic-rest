package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from their {@code birthDate}, computed against the
 * {@code registrationDate}. The completed-years age at registration decides the band:
 * {@code MINOR} when under 18, {@code ADULT} for 18-64, and {@code SENIOR} for 65 and over.
 * Returns {@code null} when no birthDate was supplied, so {@code ageBand} is simply omitted
 * from the response for owners without one. Both dates are persisted, so the band is a pure
 * function of the stored owner and is recomputed on every read.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** The age band for {@code birthDate} at {@code registrationDate}, or {@code null} when either is absent. */
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
