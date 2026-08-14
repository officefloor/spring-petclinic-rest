package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's age band from a birth date measured against the registration date.
 *
 * <p>The age in whole years is computed between {@code birthDate} and {@code registrationDate};
 * that age selects the band: {@code "MINOR"} when under 18, {@code "ADULT"} from 18 to 64
 * inclusive, and {@code "SENIOR"} at 65 or over. When no birth date is present there is no band and
 * {@code null} is returned so the field is omitted from the response.
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * method declared on a MapStruct mapper would be picked up as an implicit conversion and applied to
 * every matching property mapping.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * Returns the age band for {@code birthDate} measured against {@code registrationDate}, or
     * {@code null} when either date is absent so no band can be derived.
     */
    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        long age = ChronoUnit.YEARS.between(birthDate, registrationDate);
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
