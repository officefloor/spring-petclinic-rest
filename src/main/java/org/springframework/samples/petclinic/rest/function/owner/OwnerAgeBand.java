package org.springframework.samples.petclinic.rest.function.owner;

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Age band derivation for an owner. The band is the owner's age in whole years at their
 * registration date, computed from {@code birthDate}: {@code "MINOR"} under 18, {@code "ADULT"}
 * from 18 to 64, and {@code "SENIOR"} at 65 or over. Returns {@code null} when either date is
 * absent, so the field is simply omitted from the response.
 */
public final class OwnerAgeBand {

    private OwnerAgeBand() {
    }

    /** The owner's age band at registration, or {@code null} when birthDate is not supplied. */
    public static String of(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int years = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return years < 65 ? "ADULT" : "SENIOR";
    }
}
