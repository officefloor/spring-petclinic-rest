package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * The single definition of an owner's age band: the completed years from a birth date to a
 * reference date, classified into named bands — {@code MINOR} under 18, {@code ADULT} 18 to
 * 64 and {@code SENIOR} 65 or older. Used by the mapping layer to derive an owner's
 * {@code ageBand} from its {@code birthDate} against the effective {@code registrationDate}.
 *
 * <p>Lives beside {@link Owner} — the same home as {@link CityRegions} and
 * {@link LuhnCheckDigit} — so any layer can depend on it without a package cycle.
 */
public enum AgeBand {

    MINOR, ADULT, SENIOR;

    /**
     * The age band for the completed years from {@code birthDate} to {@code referenceDate}:
     * {@code MINOR} under 18, {@code ADULT} 18 to 64 and {@code SENIOR} 65 or older.
     * {@code null} when either date is {@code null}.
     */
    public static AgeBand of(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null || referenceDate == null) {
            return null;
        }
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
