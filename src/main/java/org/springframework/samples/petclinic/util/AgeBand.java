package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from a birth date against a reference date: 'MINOR' when under
 * 18, 'ADULT' when 18 to 64, and 'SENIOR' when 65 or older.
 */
public final class AgeBand {

    public static final String MINOR = "MINOR";

    public static final String ADULT = "ADULT";

    public static final String SENIOR = "SENIOR";

    private AgeBand() {
    }

    /**
     * The age band for someone born on {@code birthDate} as at {@code referenceDate}, using
     * completed years.
     */
    public static String of(LocalDate birthDate, LocalDate referenceDate) {
        int years = Period.between(birthDate, referenceDate).getYears();
        if (years < 18) {
            return MINOR;
        }
        if (years < 65) {
            return ADULT;
        }
        return SENIOR;
    }
}
