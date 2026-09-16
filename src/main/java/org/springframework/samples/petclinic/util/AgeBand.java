package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's age band, computed on read from the owner's birth date measured
 * against its registration date. An owner under 18 is a {@link #MINOR}, 18 to 64 an
 * {@link #ADULT} and 65 or over a {@link #SENIOR}. When either date is absent the band
 * cannot be computed and is {@code null}.
 */
public final class AgeBand {

    /** Band for owners under 18 at their registration date. */
    public static final String MINOR = "MINOR";

    /** Band for owners aged 18 to 64 at their registration date. */
    public static final String ADULT = "ADULT";

    /** Band for owners aged 65 or over at their registration date. */
    public static final String SENIOR = "SENIOR";

    private AgeBand() {
    }

    /** The age band for the given owner, or {@code null} when the birth date or
     *  registration date is absent. */
    public static String of(Owner owner) {
        return of(owner.getBirthDate(), owner.getRegistrationDate());
    }

    /** The age band for {@code birthDate} measured against {@code asOf}, or {@code null}
     *  when either date is absent. */
    public static String of(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
        int years = Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return MINOR;
        }
        return years < 65 ? ADULT : SENIOR;
    }
}
