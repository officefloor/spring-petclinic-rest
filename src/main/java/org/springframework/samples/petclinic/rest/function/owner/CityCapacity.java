package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * City capacity rules for owners. A city can hold at most {@link #CAPACITY} owners; a create that
 * would exceed that is rejected (see {@link RejectCityAtCapacity}). A city that already holds
 * between {@link #WARNING_THRESHOLD} and {@code CAPACITY - 1} owners is <em>approaching</em>
 * capacity, surfaced to callers as {@code capacityWarning}.
 *
 * <p>The counted population matches {@link RejectCityAtCapacity}: every owner in the repository
 * whose {@code city} equals the given city (null-safe, same-city equality as
 * {@link AssignCustomerCode}), soft-deleted owners included.
 */
public final class CityCapacity {

    /** Maximum owners a city may hold; a create that would exceed it is rejected. */
    static final int CAPACITY = 50;

    /** A city holding at least this many owners (but below {@link #CAPACITY}) is approaching capacity. */
    private static final int WARNING_THRESHOLD = 40;

    private CityCapacity() {
    }

    /** The number of owners currently in {@code city}. */
    static long count(String city, OwnerRepository ownerRepository) {
        return ownerRepository.findAll().stream()
                .filter(o -> city == null ? o.getCity() == null : city.equals(o.getCity()))
                .count();
    }

    /**
     * Returns true when {@code city} already holds between 40 and 49 owners (approaching the hard
     * capacity limit of 50), otherwise false.
     */
    static boolean warning(String city, OwnerRepository ownerRepository) {
        long count = count(city, ownerRepository);
        return count >= WARNING_THRESHOLD && count < CAPACITY;
    }

    /** Convenience for computing the warning from an owner's city. */
    static boolean warning(Owner owner, OwnerRepository ownerRepository) {
        return warning(owner == null ? null : owner.getCity(), ownerRepository);
    }
}
