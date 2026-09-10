package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's 'ageBand' from its birth date as of its registration date.
 * Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an
 * implicit property mapping method.
 */
public final class AgeBandResolver {

    /** Ages below this are 'MINOR'. */
    private static final int ADULT_AGE = 18;

    /** Ages at or above this are 'SENIOR'. */
    private static final int SENIOR_AGE = 65;

    private AgeBandResolver() {
    }

    /**
     * Returns the owner's age band, computed as the whole number of years between
     * {@code birthDate} and {@code registrationDate}: 'MINOR' when under 18,
     * 'ADULT' when 18 to 64 inclusive and 'SENIOR' when 65 or older.
     *
     * @param birthDate        the owner's date of birth, may be {@code null}
     * @param registrationDate the date the age is evaluated against
     * @return the age band, or {@code null} when no birth date was supplied
     */
    public static String deriveAgeBand(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < ADULT_AGE) {
            return "MINOR";
        }
        if (age < SENIOR_AGE) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
