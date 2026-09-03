package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Flags a city approaching the per-city capacity limit: {@code true} when {@code candidate}'s
 * city already holds between {@link #WARN_THRESHOLD} and {@link CityCapacity#LIMIT} - 1 owners
 * (inclusive), so the next one after that is rejected by {@link CityCapacity}.
 */
final class CityCapacityWarning {

    static final int WARN_THRESHOLD = 40;

    private CityCapacityWarning() {
    }

    /**
     * Returns {@code true} when {@code candidate}'s city already holds at least
     * {@link #WARN_THRESHOLD} but fewer than {@link CityCapacity#LIMIT} of the
     * {@code existing} owners (city compared case-insensitively).
     */
    static boolean isApproaching(Owner candidate, Collection<Owner> existing) {
        long inCity = existing.stream()
            .filter(other -> candidate.getCity().equalsIgnoreCase(other.getCity()))
            .count();
        return inCity >= WARN_THRESHOLD && inCity < CityCapacity.LIMIT;
    }
}
