package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's {@code ageBand} from a birth date as of the owner's registration date:
 * {@code "MINOR"} when under 18, {@code "ADULT"} from 18 to 64, and {@code "SENIOR"} at 65 or
 * older. Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an implicit
 * mapping method and apply it to every property.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * Return the age band for the given {@code birthDate} evaluated as of {@code asOf} (the
     * owner's registration date), or {@code null} when either date is absent so the field is
     * omitted from the response.
     *
     * @param birthDate the owner's date of birth (may be {@code null})
     * @param asOf      the date the age is computed against, i.e. the registration date
     * @return {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"}, or {@code null} when no
     *         birth date is present
     */
    public static String forBirthDate(LocalDate birthDate, LocalDate asOf) {
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
