package org.springframework.samples.petclinic.rest.function.owner;

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the {@code ageBand} from an owner's {@code birthDate}, measured against its
 * {@code registrationDate}: {@code MINOR} (under 18), {@code ADULT} (18-64) or
 * {@code SENIOR} (65+). Returns {@code null} when no birth date was supplied.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(Owner owner) {
        if (owner.getBirthDate() == null) {
            return null;
        }
        int years = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return years < 65 ? "ADULT" : "SENIOR";
    }
}
