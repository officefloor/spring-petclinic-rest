package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

/**
 * The single definition of an owner's age band: the owner's age in whole years at its
 * registration date, bucketed as {@code "MINOR"} (under 18), {@code "ADULT"} (18-64) or
 * {@code "SENIOR"} (65 and over).
 *
 * <p>Lookup only; a pure function of the birth date and registration date, so the band is
 * computed on read rather than stored. Absent (null) until both dates are known.
 */
public final class AgeBand {

    /** Age (inclusive) at which an owner becomes an adult. */
    private static final int ADULT_AGE = 18;

    /** Age (inclusive) at which an owner becomes a senior. */
    private static final int SENIOR_AGE = 65;

    private AgeBand() {
    }

    /** The age band for someone born on {@code birthDate} as measured on {@code registrationDate},
     *  or {@code null} when either date is absent. */
    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int years = Period.between(birthDate, registrationDate).getYears();
        if (years < ADULT_AGE) {
            return "MINOR";
        }
        return years < SENIOR_AGE ? "ADULT" : "SENIOR";
    }
}
