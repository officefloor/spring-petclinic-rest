package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Canonical per-city population rule for the owner pipelines, defined once so the hard capacity
 * gate ({@link ValidateCityCapacity}) and the soft warning flag ({@link FlagCapacityWarning})
 * agree on both "how many owners does this city hold" and the limit itself. Owning the
 * {@link #CAPACITY} constant here means the warning band is derived from the hard limit rather
 * than re-encoded, so a maintainer moving the limit moves the band with it.
 *
 * <p>Like every other scan in this package (the duplicate gate, {@link Household}, {@link
 * PossibleDuplicate}), the count considers only {@link ActiveOwners active owners}: a soft-deleted
 * owner retains its row but no longer occupies the city's capacity, consistent with its exclusion
 * from the duplicate and household scans.
 */
final class CityPopulation {

    /** The hard per-city capacity limit: {@link ValidateCityCapacity} rejects a create whose city
     *  already holds this many owners. */
    static final int CAPACITY = 50;

    /** The lower bound of the warning band: {@link FlagCapacityWarning} warns once a city holds at
     *  least this many owners (and still fewer than {@link #CAPACITY}). */
    static final int WARNING_THRESHOLD = 40;

    private CityPopulation() {
    }

    /** The number of {@link ActiveOwners active owners} whose city equals {@code city}
     *  case-insensitively. {@code 0} for a {@code null} city. */
    static int count(String city, OwnerRepository ownerRepository) {
        if (city == null) {
            return 0;
        }
        int count = 0;
        for (Owner existing : ActiveOwners.list(ownerRepository)) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }

    /** Whether a city holding {@code count} owners is approaching capacity: in the warning band
     *  {@code [WARNING_THRESHOLD, CAPACITY)}, i.e. 40 to 49 for the current limit of 50. */
    static boolean approachingCapacity(int count) {
        return count >= WARNING_THRESHOLD && count < CAPACITY;
    }
}
