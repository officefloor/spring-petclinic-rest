package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's age band from their birth date measured against their registration
 * date: 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+). Returns null when either
 * date is absent, so the response field is simply omitted.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate registrationDate = owner.getRegistrationDate();
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
