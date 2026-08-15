package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's {@code ageBand} from its {@code birthDate} as of its {@code registrationDate}.
 * The band is {@code MINOR} when the owner is under 18, {@code ADULT} from 18 to 64 inclusive, and
 * {@code SENIOR} at 65 or older. There is no band when no birth date was supplied.
 */
public final class AgeBand {

    /** Upper (exclusive) age for the MINOR band. */
    private static final int ADULT_AGE = 18;

    /** Lower (inclusive) age for the SENIOR band. */
    private static final int SENIOR_AGE = 65;

    private AgeBand() {
    }

    /**
     * Returns the age band for an owner born on {@code birthDate} as of {@code registrationDate}, or
     * {@code null} when {@code birthDate} is absent.
     */
    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int years = Period.between(birthDate, registrationDate).getYears();
        if (years < ADULT_AGE) {
            return "MINOR";
        }
        if (years < SENIOR_AGE) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
