package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's age band from their {@code birthDate} relative to their
 * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64
 * and {@code SENIOR} at 65 or over. Returns {@code null} when no birth date was
 * supplied, so the response omits {@code ageBand} for owners without one.
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
        int years = Period.between(birthDate, reference).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
