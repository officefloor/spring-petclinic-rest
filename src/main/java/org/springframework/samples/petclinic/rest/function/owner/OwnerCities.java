package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Per-city capacity rules for owner registration.
 *
 * <p>Each city has a hard limit on how many owners it may hold. The count defined here is the single
 * source of truth shared by {@link CheckOwnerCityCapacity} (which hard-rejects once the city is at
 * {@link #CITY_CAPACITY}) and {@link FlagOwnerCapacityWarning} (which flags when the city is in the
 * approaching-capacity band just below it), so the limit and the warning band cannot drift apart.
 */
public final class OwnerCities {

    /** Maximum number of owners permitted per city; the next owner is rejected at this count. */
    public static final int CITY_CAPACITY = 50;

    private OwnerCities() {
    }

    /**
     * The number of owners already registered in the given city. Single source of truth for the
     * per-city count, shared by the capacity reject and the capacity-warning rules so their counts
     * cannot drift apart; each rule keeps only its own threshold and action. Counts persisted owners
     * only, so a step calling this before {@code SaveOwner} does not count the owner being created.
     */
    public static long countInCity(OwnerRepository ownerRepository, String city) {
        return ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equals(existing.getCity()))
                .count();
    }
}
