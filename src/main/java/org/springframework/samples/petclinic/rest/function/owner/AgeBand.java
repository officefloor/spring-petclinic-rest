package org.springframework.samples.petclinic.rest.function.owner;

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Response value derived from the owner's birthDate measured against the registrationDate:
 * 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+). Null when no birthDate is set.
 */
final class AgeBand {

    private AgeBand() {
    }

    static String of(Owner owner) {
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
