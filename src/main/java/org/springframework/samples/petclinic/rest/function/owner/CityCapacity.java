package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a city approaching the owner {@link RequireCityCapacity#CAPACITY capacity limit}:
 * {@code true} once a city already holds between {@value #WARNING_THRESHOLD} and one below the
 * limit, otherwise {@code false}. Cities are compared exactly, as elsewhere in the pipeline.
 */
public final class CityCapacity {

    static final int WARNING_THRESHOLD = 40;

    private CityCapacity() {
    }

    public static boolean approaching(String city, OwnerRepository ownerRepository) {
        long count = ownerRepository.findAll().stream()
            .filter(existing -> city.equals(existing.getCity()))
            .count();
        return count >= WARNING_THRESHOLD && count < RequireCityCapacity.CAPACITY;
    }
}
