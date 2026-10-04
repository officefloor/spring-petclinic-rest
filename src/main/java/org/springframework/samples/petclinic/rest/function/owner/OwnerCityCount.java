package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single home for "how many owners already live in a given city". Both
 * {@link RejectCityAtCapacity} and {@link FlagCityCapacityWarning} key off this count
 * at different thresholds; keeping the matching predicate here avoids maintaining it in
 * two places.
 */
final class OwnerCityCount {

    private OwnerCityCount() {
    }

    /**
     * Counts existing owners in {@code city}, comparing cities case-insensitively on
     * their trimmed value (a null owner city never matches). Called before the new
     * owner is saved, so it is not itself counted.
     */
    static long sharingCity(OwnerRepository ownerRepository, String city) {
        String target = city == null ? "" : city.trim();
        return ownerRepository.findAll().stream()
                .filter(existing -> existing.getCity() != null
                        && target.equalsIgnoreCase(existing.getCity().trim()))
                .count();
    }
}
