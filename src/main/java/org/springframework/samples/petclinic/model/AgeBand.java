package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from their {@code birthDate}, measured at the moment of
 * registration ({@code registrationDate}): {@link #MINOR} when under 18, {@link #ADULT}
 * from 18 to 64, {@link #SENIOR} at 65 or older.
 *
 * <p>Null when the owner has no birthDate. When the registrationDate is absent the age is
 * measured against the current date instead.
 */
public final class AgeBand {

    /** Under 18 at registration. */
    public static final String MINOR = "MINOR";

    /** 18 to 64 (inclusive) at registration. */
    public static final String ADULT = "ADULT";

    /** 65 or older at registration. */
    public static final String SENIOR = "SENIOR";

    private AgeBand() {
    }

    /** The owner's age band, or {@code null} when no birthDate was supplied. */
    public static String of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate();
        if (asOf == null) {
            asOf = LocalDate.now();
        }
        int age = Period.between(birthDate, asOf).getYears();
        if (age < 18) {
            return MINOR;
        }
        if (age < 65) {
            return ADULT;
        }
        return SENIOR;
    }
}
