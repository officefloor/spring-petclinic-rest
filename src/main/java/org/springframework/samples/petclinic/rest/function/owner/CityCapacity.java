package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The per-city capacity policy. A city holds at most {@link #MAX_OWNERS_PER_CITY} owners;
 * the create that would exceed it is rejected ({@link EnsureCityCapacity}). As a city
 * approaches that limit it raises a soft warning, surfaced as the read-only
 * {@code capacityWarning} flag on every owner response. Cities are counted
 * case-insensitively (see {@link Cities}).
 */
final class CityCapacity {

    /** Maximum owners a single city may hold; the next create is rejected. */
    static final long MAX_OWNERS_PER_CITY = 50;

    /**
     * A city holding at least this many owners (but not yet full) is approaching capacity
     * and raises the warning.
     */
    static final long WARNING_THRESHOLD = 40;

    private CityCapacity() {
    }

    /** Whether the city is full, so the next create must be rejected. */
    static boolean isFull(OwnerRepository repository, String city) {
        return Cities.countIn(repository, city) >= MAX_OWNERS_PER_CITY;
    }

    /**
     * Whether the city is approaching capacity: it already holds between
     * {@link #WARNING_THRESHOLD} and {@link #MAX_OWNERS_PER_CITY} - 1 owners inclusive.
     */
    static boolean warningRaised(OwnerRepository repository, String city) {
        long count = Cities.countIn(repository, city);
        return count >= WARNING_THRESHOLD && count < MAX_OWNERS_PER_CITY;
    }
}
