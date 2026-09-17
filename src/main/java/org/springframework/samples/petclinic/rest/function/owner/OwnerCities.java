package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared counting over the owners already stored in a given city. Both the hard capacity
 * limit and the approaching-capacity warning bucket owners by the same (exactly compared)
 * city, so they share this count.
 */
final class OwnerCities {

    private OwnerCities() {
    }

    /** The number of stored owners whose city equals {@code city} exactly. */
    static long countIn(OwnerRepository ownerRepository, String city) {
        return ownerRepository.findAll().stream()
            .map(Owner::getCity)
            .filter(existing -> existing != null && existing.equals(city))
            .count();
    }
}
