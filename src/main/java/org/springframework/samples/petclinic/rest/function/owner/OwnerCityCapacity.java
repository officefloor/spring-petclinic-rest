package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Per-city capacity rules shared between the create-time guard {@link EnsureOwnerCityCapacity} and
 * the read-time {@code capacityWarning} surfaced on the owner response.
 *
 * <p>A city holds at most {@value #CITY_OWNER_LIMIT} owners; once it already contains that many a new
 * owner is rejected. The warning flags the run-up to that limit: {@code true} once a city holds
 * between {@value #CITY_WARNING_THRESHOLD} and {@code CITY_OWNER_LIMIT - 1} owners (inclusive).
 * Cities are compared case-insensitively, matching how owners are counted for the hard limit.
 */
public final class OwnerCityCapacity {

    /** Maximum number of owners permitted in a single city. */
    public static final int CITY_OWNER_LIMIT = 50;

    /** A city holding this many owners (up to, but not including, the limit) raises the warning. */
    public static final int CITY_WARNING_THRESHOLD = 40;

    /**
     * The per-city soft capacity: the number of owners at which a city is first considered to be
     * running up against its limit. Equal to {@link #CITY_WARNING_THRESHOLD}; a city holding this
     * many owners or more is "over soft capacity" (see {@link #isOverSoftCapacity}).
     */
    public static final int CITY_SOFT_CAPACITY = CITY_WARNING_THRESHOLD;

    private OwnerCityCapacity() {
    }

    /**
     * Whether {@code city} is over its soft capacity: {@code true} once it already holds at least
     * {@link #CITY_SOFT_CAPACITY} owners. Unlike {@link #isWarning} this stays {@code true} at or
     * beyond the hard limit, so a read-time risk signal keeps firing for an over-full city.
     */
    public static boolean isOverSoftCapacity(OwnerRepository ownerRepository, String city) {
        return countOwnersInCity(ownerRepository, city) >= CITY_SOFT_CAPACITY;
    }

    /** Whether {@code city} already holds {@link #CITY_OWNER_LIMIT} or more owners. */
    public static boolean isAtCapacity(OwnerRepository ownerRepository, String city) {
        return countOwnersInCity(ownerRepository, city) >= CITY_OWNER_LIMIT;
    }

    /**
     * Whether {@code city} is approaching the capacity limit: {@code true} when it already holds
     * between {@value #CITY_WARNING_THRESHOLD} and {@code CITY_OWNER_LIMIT - 1} owners (inclusive).
     * At or beyond the hard limit this is {@code false} — the city is at capacity, not merely
     * approaching it.
     */
    public static boolean isWarning(OwnerRepository ownerRepository, String city) {
        int count = countOwnersInCity(ownerRepository, city);
        return count >= CITY_WARNING_THRESHOLD && count < CITY_OWNER_LIMIT;
    }

    private static int countOwnersInCity(OwnerRepository ownerRepository, String city) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city == null ? existing.getCity() == null : city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }
}
