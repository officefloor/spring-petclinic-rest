package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared counting of owners already registered in a given city, used by the create-pipeline
 * rules that accumulate per city: the hard capacity limit ({@link EnsureCityHasCapacity}) and
 * the approaching-capacity warning ({@link FlagCapacityWarning}). Cities are compared
 * case-insensitively on their trimmed value, so incidental formatting differences neither hide
 * nor inflate the count, and both rules count the same owners.
 */
final class CityRegistrations {

    private CityRegistrations() {
    }

    /** Count stored owners whose city matches the given city, compared case-insensitively on the trimmed value. */
    static int countIn(OwnerRepository ownerRepository, String city) {
        String normalized = normalize(city);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalized.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        return count;
    }

    private static String normalize(String city) {
        return city == null ? "" : city.trim().toLowerCase();
    }
}
