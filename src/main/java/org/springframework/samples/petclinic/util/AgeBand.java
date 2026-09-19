package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from a date of birth as at a reference date (the owner's
 * registration date). The band is {@link #MINOR} below {@link #ADULT_AGE}, {@link #ADULT}
 * from there up to {@link #SENIOR_AGE}, and {@link #SENIOR} from {@code SENIOR_AGE}.
 */
public enum AgeBand {

    MINOR, ADULT, SENIOR;

    /** Age (inclusive) at which an owner is no longer a minor. */
    public static final int ADULT_AGE = 18;

    /** Age (inclusive) at which an owner becomes a senior. */
    public static final int SENIOR_AGE = 65;

    /**
     * Return the age band for someone born on {@code birthDate} as at {@code asOf}, or
     * {@code null} when either date is absent.
     */
    public static AgeBand of(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
        int age = Period.between(birthDate, asOf).getYears();
        if (age < ADULT_AGE) {
            return MINOR;
        }
        if (age < SENIOR_AGE) {
            return ADULT;
        }
        return SENIOR;
    }
}
