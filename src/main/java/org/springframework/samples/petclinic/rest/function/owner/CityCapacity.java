package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single source of truth for the per-city capacity rule: how many owners a single city may hold
 * and how its occupancy is counted.
 *
 * <p>Centralised here so the two create-time city rules can never drift apart: both
 * {@link CheckOwnerCityCapacity} (the hard cap, rejecting at {@link #CAPACITY}) and
 * {@link AssignCityCapacityWarning} (the flag raised while approaching it) count existing owners
 * through {@link #countOwnersIn} and compare the result against the constants defined here.
 */
final class CityCapacity {

    /** Maximum number of owners permitted in a single city; a request at this count is rejected. */
    static final int CAPACITY = 50;

    /** Owner count in a city at or above which (but below {@link #CAPACITY}) the warning is raised. */
    static final int WARNING_THRESHOLD = 40;

    private CityCapacity() {
    }

    /**
     * Counts existing owners whose stored city equals {@code city}, compared case-insensitively —
     * the single definition of "owners in this city" shared by the create-time city rules, so the
     * hard cap and the warning always count the same way. A null stored city never matches.
     */
    static long countOwnersIn(String city, OwnerRepository ownerRepository) {
        return ownerRepository.findAll().stream()
            .map(Owner::getCity)
            .filter(existing -> existing != null && existing.equalsIgnoreCase(city))
            .count();
    }
}
