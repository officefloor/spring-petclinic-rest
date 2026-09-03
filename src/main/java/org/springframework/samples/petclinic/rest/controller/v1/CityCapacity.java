package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Enforces the per-city capacity limit: a city may hold at most {@link #LIMIT} owners.
 */
final class CityCapacity {

    static final int LIMIT = 50;

    private CityCapacity() {
    }

    /**
     * Returns {@code true} when {@code candidate}'s city already holds {@link #LIMIT} or more of
     * the {@code existing} owners (city compared case-insensitively), so it cannot take another.
     */
    static boolean isAtCapacity(Owner candidate, Collection<Owner> existing) {
        long inCity = existing.stream()
            .filter(other -> candidate.getCity().equalsIgnoreCase(other.getCity()))
            .count();
        return inCity >= LIMIT;
    }
}
