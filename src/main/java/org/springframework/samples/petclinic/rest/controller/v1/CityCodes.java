package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Per-city sequence rule for owner customer codes: how many existing owners
 * already live in a candidate's city, compared case-insensitively.
 */
final class CityCodes {

    private CityCodes() {
    }

    /** How many existing owners share {@code candidate}'s city. */
    static int countInCity(Collection<Owner> existing, Owner candidate) {
        return (int) existing.stream().filter(other -> sameCity(other, candidate)).count();
    }

    private static boolean sameCity(Owner a, Owner b) {
        return a.getCity() != null && a.getCity().equalsIgnoreCase(b.getCity());
    }
}
