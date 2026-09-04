package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Counts how many existing owners share a candidate owner's first and last name,
 * compared case-insensitively (the {@code namesakeCount}).
 */
public final class Namesakes {

    private Namesakes() {
    }

    public static int count(Owner candidate, Collection<Owner> existing) {
        int namesakes = 0;
        for (Owner other : existing) {
            if (candidate.getFirstName().equalsIgnoreCase(other.getFirstName())
                && candidate.getLastName().equalsIgnoreCase(other.getLastName())) {
                namesakes++;
            }
        }
        return namesakes;
    }
}
