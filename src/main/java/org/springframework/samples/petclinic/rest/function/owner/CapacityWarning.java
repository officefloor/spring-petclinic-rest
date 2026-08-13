package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes the {@code capacityWarning} response flag: true once the owner's city already
 * holds between 40 and 49 owners (inclusive), signalling that it is approaching the hard
 * capacity limit of 50 enforced by {@link EnsureCityCapacity}; otherwise false.
 *
 * <p>Cities are compared case-insensitively, matching how {@link EnsureCityCapacity}
 * counts a city's owners for the hard rejection at 50.
 */
final class CapacityWarning {

    private static final int WARNING_LOWER_BOUND = 40;

    private static final int WARNING_UPPER_BOUND = 49;

    private CapacityWarning() {
    }

    static boolean forCity(Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
            .filter(existing -> city == null
                ? existing.getCity() == null
                : city.equalsIgnoreCase(existing.getCity()))
            .count();
        return inCity >= WARNING_LOWER_BOUND && inCity <= WARNING_UPPER_BOUND;
    }
}
