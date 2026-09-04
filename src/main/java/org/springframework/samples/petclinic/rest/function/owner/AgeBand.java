package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Age-band rule: derives the owner's band from their birth date measured against their
 * registration date — 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+). Returns null
 * when no birth date is supplied so the field is simply absent from the response.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return "MINOR";
        }
        return age < 65 ? "ADULT" : "SENIOR";
    }
}
