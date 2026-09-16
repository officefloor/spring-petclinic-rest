package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * The single definition of an owner's <em>age band</em> on their registration date, derived from
 * their birth date: {@code MINOR} (under 18), {@code ADULT} (18 to 64) or {@code SENIOR} (65 or
 * over). The age is the number of whole years between the two dates.
 *
 * <p>Returned by the response mapper alongside the owner's other derived fields; absent (null)
 * when either date is missing, since without a birth date there is no band to report.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** The age band on {@code registrationDate} for an owner born on {@code birthDate}, or null
     * when either date is unknown. */
    public static String on(LocalDate birthDate, LocalDate registrationDate) {
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
