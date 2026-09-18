package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Classifies an owner's age into a band, computed as the completed years between a birth date
 * and a reference date (the owner's registration date): {@code "MINOR"} under 18, {@code "ADULT"}
 * for 18-64, {@code "SENIOR"} for 65 and over. Used to derive an owner's age band from its stored
 * birth date.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * The age band of someone born on {@code birthDate} as of {@code asOf}, or {@code null} when
     * either date is absent (no birth date recorded, so no band can be derived).
     */
    public static String classify(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
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
