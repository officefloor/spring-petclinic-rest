package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * City-capacity helper for owner registration. Single source of truth for "how many
 * owners are in a city" and the hard capacity limit, so the two halves of the rule —
 * the hard rejection at the limit ({@link EnsureCityCapacity}) and the approaching-limit
 * warning ({@link AssessCityCapacity}) — cannot drift apart. A city may hold up to
 * {@link #CITY_CAPACITY} owners; the final 10 places below it are the warning band.
 */
final class CityCapacity {

    /** The maximum number of owners a city may hold; creation is rejected at or above it. */
    static final int CITY_CAPACITY = 50;

    /** The lower bound (inclusive) of the warning band: at or above this, below the limit. */
    static final int WARNING_THRESHOLD = 40;

    private CityCapacity() {
    }

    /**
     * Counts the existing owners whose city equals {@code city}, matching cities by exact
     * value. The single source of truth for "how many owners are in a city", so the hard
     * rejection and the capacity warning count the same way. The count is over the existing
     * owners (a new owner being created is not yet saved).
     */
    static long countInCity(OwnerRepository ownerRepository, String city) {
        return ownerRepository.findAll().stream()
            .filter(existing -> city != null && city.equals(existing.getCity()))
            .count();
    }
}
