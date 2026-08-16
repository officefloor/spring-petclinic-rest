package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from the owner's birth date, evaluated against the
 * registration date: {@code MINOR} when under 18, {@code ADULT} from 18 to 64, and
 * {@code SENIOR} at 65 or over.
 *
 * <p>The age is the completed number of whole years between the birth date and the
 * registration date. When either date is absent no band can be derived and
 * {@code null} is returned, which the API renders as an absent {@code ageBand}.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic mapping method and apply it to
 * unrelated fields; the mapper references it only through an explicit expression.
 */
public final class AgeBandDeriver {

    private AgeBandDeriver() {
    }

    /**
     * Returns the age band for an owner born on {@code birthDate} as measured against
     * {@code registrationDate}, or {@code null} when either date is absent.
     */
    public static String ageBand(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
