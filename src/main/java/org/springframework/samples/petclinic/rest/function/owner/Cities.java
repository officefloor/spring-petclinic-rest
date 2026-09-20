package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Owners-per-city concerns: matching two owners to the same city, counting how many owners a
 * city holds, and the capacity thresholds derived from that count. The hard per-city limit
 * ({@link EnsureCityCapacity}) and the response's capacity warning both accumulate over this
 * same count, so they share this logic rather than each re-walking the owners.
 */
final class Cities {

    /** Maximum number of owners allowed per city; the next create in a full city is rejected. */
    static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * Once a city holds this many owners, a response carries a capacity warning. Below the
     * {@link #MAX_OWNERS_PER_CITY hard limit}, so the warning flags a city approaching capacity
     * before it is closed to further creates.
     */
    static final int CAPACITY_WARNING_THRESHOLD = 40;

    private Cities() {
    }

    /**
     * Whether {@code owner} lives in the given city, comparing both in their canonical
     * (trimmed, whitespace-collapsed, lower-cased) forms.
     */
    static boolean matches(Owner owner, String city) {
        return normalize(city).equals(normalize(owner.getCity()));
    }

    /** The number of existing owners in {@code city}. */
    static int count(OwnerRepository ownerRepository, String city) {
        int count = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (matches(owner, city)) {
                count++;
            }
        }
        return count;
    }

    /** Whether {@code city} is full and cannot accept another owner. */
    static boolean isAtCapacity(OwnerRepository ownerRepository, String city) {
        return count(ownerRepository, city) >= MAX_OWNERS_PER_CITY;
    }

    /** Whether {@code city} holds between {@link #CAPACITY_WARNING_THRESHOLD} and one below the
     * hard limit inclusive, i.e. it is approaching but has not yet reached capacity. */
    static boolean isApproachingCapacity(OwnerRepository ownerRepository, String city) {
        int count = count(ownerRepository, city);
        return count >= CAPACITY_WARNING_THRESHOLD && count < MAX_OWNERS_PER_CITY;
    }

    /** Whether {@code city} is over its soft capacity — it holds at least
     * {@link #CAPACITY_WARNING_THRESHOLD} owners, the point from which it counts as filling up
     * (whether or not it has since reached the hard limit). */
    static boolean isOverSoftCapacity(OwnerRepository ownerRepository, String city) {
        return count(ownerRepository, city) >= CAPACITY_WARNING_THRESHOLD;
    }

    private static String normalize(String city) {
        if (city == null) {
            return "";
        }
        return city.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
