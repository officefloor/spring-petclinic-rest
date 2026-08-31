package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Counts owners that are namesakes, i.e. carry the same first name and last name
 * compared case-insensitively.
 */
final class Namesakes {

    private Namesakes() {
    }

    /** Lower-case a value for comparison, treating null as empty. */
    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    /** Count existing owners sharing the candidate's first and last name. */
    static int count(Collection<Owner> existing, Owner candidate) {
        String firstName = normalize(candidate.getFirstName());
        String lastName = normalize(candidate.getLastName());
        return (int) existing.stream().filter(o ->
            normalize(o.getFirstName()).equals(firstName) && normalize(o.getLastName()).equals(lastName)).count();
    }
}
