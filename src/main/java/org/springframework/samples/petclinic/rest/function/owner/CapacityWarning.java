package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single definition of the approaching-capacity warning: it is raised once a city already
 * holds at least {@link #THRESHOLD} owners but has not yet reached the hard limit enforced by
 * {@link EnsureCityHasCapacity} (compared case-insensitively, see {@link SameCity}). Surfaced on
 * every owner response so callers can spot a city nearing capacity before the create is rejected.
 */
final class CapacityWarning {

    /** Owners already in a city at or above which the approaching-capacity warning is raised. */
    static final int THRESHOLD = 40;

    private CapacityWarning() {
    }

    /** Whether {@code city} already holds between {@link #THRESHOLD} and one below the limit. */
    static boolean isRaised(String city, OwnerRepository ownerRepository) {
        int inCity = SameCity.count(ownerRepository.findAll(), city);
        return inCity >= THRESHOLD && inCity < EnsureCityHasCapacity.CAPACITY;
    }
}
