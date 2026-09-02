package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's age band from {@code birthDate} measured against the
 * {@code registrationDate}: {@code MINOR} (under 18), {@code ADULT} (18-64)
 * or {@code SENIOR} (65+). Returns {@code null} when no birth date is present.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        int years = Period.between(birthDate, reference).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return years < 65 ? "ADULT" : "SENIOR";
    }
}
