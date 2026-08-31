package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Counts owners already registered in a candidate's city, compared case-insensitively.
 * Used to derive the per-city sequence in the customerCode.
 */
final class CityCodes {

    private CityCodes() {
    }

    /** Lower-case a value for comparison, treating null as empty. */
    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    /** Count existing owners sharing the candidate's city. */
    static int count(Collection<Owner> existing, Owner candidate) {
        String city = normalize(candidate.getCity());
        return (int) existing.stream().filter(o -> normalize(o.getCity()).equals(city)).count();
    }
}
