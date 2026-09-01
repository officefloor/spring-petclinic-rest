package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Namesake rule for owners: two owners are namesakes when they share the same
 * first name and last name, compared case-insensitively.
 */
final class Namesakes {

    private Namesakes() {
    }

    /** How many existing owners are namesakes of {@code candidate}. */
    static int count(Collection<Owner> existing, Owner candidate) {
        return (int) existing.stream().filter(other -> sameName(other, candidate)).count();
    }

    private static boolean sameName(Owner a, Owner b) {
        return equalsIgnoreCase(a.getFirstName(), b.getFirstName())
            && equalsIgnoreCase(a.getLastName(), b.getLastName());
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
