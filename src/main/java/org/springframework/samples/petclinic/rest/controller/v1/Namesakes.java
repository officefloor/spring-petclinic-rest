package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Counts owners that are namesakes, i.e. share the same firstName and lastName
 * (compared case-insensitively).
 */
final class Namesakes {

    private Namesakes() {
    }

    /** Number of {@code existing} owners sharing {@code candidate}'s firstName and lastName. */
    static int count(Owner candidate, Collection<Owner> existing) {
        return (int) existing.stream()
            .filter(other -> eq(other.getFirstName(), candidate.getFirstName())
                && eq(other.getLastName(), candidate.getLastName()))
            .count();
    }

    private static boolean eq(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
