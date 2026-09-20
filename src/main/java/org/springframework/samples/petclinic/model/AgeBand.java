package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * The age band an owner falls into, derived from their date of birth relative to their
 * registration date: {@link #MINOR} when under 18, {@link #ADULT} from 18 to 64 and
 * {@link #SENIOR} at 65 and over.
 */
public enum AgeBand {

    /** Under 18 at the registration date. */
    MINOR,

    /** From 18 to 64 (inclusive) at the registration date. */
    ADULT,

    /** 65 or over at the registration date. */
    SENIOR;

    /** The boundary, in whole years, between {@link #MINOR} and {@link #ADULT}. */
    private static final int ADULT_AGE = 18;

    /** The boundary, in whole years, between {@link #ADULT} and {@link #SENIOR}. */
    private static final int SENIOR_AGE = 65;

    /**
     * The age band for the given owner, computed from their birth date against their
     * registration date, or {@code null} when either date is absent.
     */
    public static AgeBand of(Owner owner) {
        return of(owner.getBirthDate(), owner.getRegistrationDate());
    }

    /**
     * The age band for someone born on {@code birthDate} as of {@code asOf}, or
     * {@code null} when either date is absent.
     */
    public static AgeBand of(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
        int years = Period.between(birthDate, asOf).getYears();
        if (years < ADULT_AGE) {
            return MINOR;
        }
        if (years < SENIOR_AGE) {
            return ADULT;
        }
        return SENIOR;
    }
}
