package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Classifies an owner's age into a band as at a reference date. The age is the number of
 * whole years elapsed from the birth date to the reference date (an owner's registration
 * date), so the band reflects how old the owner was when they registered.
 *
 * <p>The bands are {@code MINOR} for under 18, {@code ADULT} for 18 to 64 inclusive, and
 * {@code SENIOR} for 65 and over.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * @param birthDate the owner's date of birth
     * @param asAt the reference date the age is measured against
     * @return {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"}
     */
    public static String of(LocalDate birthDate, LocalDate asAt) {
        int years = Period.between(birthDate, asAt).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
