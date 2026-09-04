package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Signals that an owner's city is approaching the capacity limit of 50: true once the city
 * already holds between 40 and 49 owners (compared case-insensitively), otherwise false.
 */
public final class CityCapacity {

    private static final int WARN_FROM = 40;
    private static final int LIMIT = 50;

    private CityCapacity() {
    }

    public static boolean approaching(Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(o -> city != null && city.equalsIgnoreCase(o.getCity()))
                .count();
        return count >= WARN_FROM && count < LIMIT;
    }
}
