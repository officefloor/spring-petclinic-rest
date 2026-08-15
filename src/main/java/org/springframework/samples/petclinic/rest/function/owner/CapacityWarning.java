package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides whether an owner's response should carry the capacity warning: it is raised once the
 * owner's city already holds between 40 and 49 owners (inclusive), signalling the city is
 * approaching the hard capacity limit of 50 enforced by {@link CheckOwnerCityCapacity}. At exactly
 * 50 the city is full (creates are rejected) and no warning is raised. Counting mirrors
 * {@link CheckOwnerCityCapacity}: the owner's city is compared case-insensitively across all
 * owners (this one included), so the create and read responses agree.
 */
final class CapacityWarning {

    /** Inclusive lower bound of the warning band. */
    private static final long WARN_FROM = 40;

    /** Inclusive upper bound of the warning band; 50 is the hard limit and is not a warning. */
    private static final long WARN_TO = 49;

    private CapacityWarning() {
    }

    /** True when the owner's city holds between 40 and 49 owners (this one included). */
    static boolean warningFor(Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        return inCity >= WARN_FROM && inCity <= WARN_TO;
    }
}
