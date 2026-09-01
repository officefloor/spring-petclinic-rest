package org.springframework.samples.petclinic.mapper;

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's age band from its birthDate measured against its
 * registrationDate: "MINOR" (under 18), "ADULT" (18-64) or "SENIOR" (65+).
 * Returns {@code null} when no birthDate is set.
 */
final class AgeBands {

    private AgeBands() {
    }

    /** Age band for {@code owner}, or {@code null} when birthDate is absent. */
    static String bandOf(Owner owner) {
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
