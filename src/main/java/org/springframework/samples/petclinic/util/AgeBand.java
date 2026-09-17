package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * An owner's age band, derived from their birth date measured against their registration date:
 * {@link #MINOR} when under 18, {@link #ADULT} from 18 to 64, and {@link #SENIOR} at 65 or older.
 * Pure function of the owner's own fields, so it is derived at response time rather than stored on
 * the entity, and is absent when the owner has no birth date.
 */
public enum AgeBand {

    /** Under 18 at the registration date. */
    MINOR,

    /** From 18 to 64 (inclusive) at the registration date. */
    ADULT,

    /** 65 or older at the registration date. */
    SENIOR;

    /**
     * The age band for {@code owner}, or {@code null} when the owner is {@code null} or has no birth
     * date (or no registration date to measure it against).
     */
    public static AgeBand of(Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int years = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (years < 18) {
            return MINOR;
        }
        return years < 65 ? ADULT : SENIOR;
    }
}
