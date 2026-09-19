package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared city-membership logic. An owner belongs to the city named by their {@code city}
 * field, matched exactly, so every rule that reasons about a city's population (e.g.
 * capacity) counts the same set of owners.
 */
final class Cities {

    private Cities() {
    }

    /**
     * The number of existing owners in the given city.
     */
    static long countIn(OwnerRepository ownerRepository, String city) {
        return ownerRepository.findAll().stream()
                .filter(existing -> city.equals(existing.getCity()))
                .count();
    }
}
