package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

/**
 * Age band for a pet owner, derived from the owner's {@code birthDate} against its
 * {@code registrationDate}: the completed years between the two dates place the owner in
 * {@code MINOR} (under 18), {@code ADULT} (18-64) or {@code SENIOR} (65 or over). Computed against
 * the registration date (the effective, business-day-rolled date stored on the owner) rather than
 * the current date, so the band is stable for the life of the record. Birth date is optional, so an
 * absent birth date (or an absent registration date) yields {@code null} and the field is omitted
 * from responses. Derived purely from the owner's own state, so it carries no stored data and is
 * seed-independent. Used by the owner mapper to expose {@code ageBand} on responses.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * The age band for an owner born on {@code birthDate} as at {@code registrationDate}, or
     * {@code null} when either date is absent.
     */
    public static String of(LocalDate birthDate, LocalDate registrationDate) {
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
