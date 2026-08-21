package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Capacity-warning rule for the create-owner flow. Signals that an owner's city is approaching the
 * hard limit of 50 enforced by {@link CheckOwnerCityCapacity}: the response's {@code capacityWarning}
 * is true when the city already holds between 40 and 49 other owners (city compared
 * case-insensitively), otherwise false.
 *
 * <p>Counts the same population the hard check does - every other owner already in the city,
 * excluding the owner being described - so the count partitions cleanly: 40-49 warns here, 50 or
 * more is rejected by {@link CheckOwnerCityCapacity}.
 */
final class CapacityWarning {

    private static final int WARNING_THRESHOLD = 40;

    private static final int CITY_CAPACITY = 50;

    private CapacityWarning() {
    }

    /**
     * Whether the owner's city already holds between {@value #WARNING_THRESHOLD} and
     * {@code CITY_CAPACITY - 1} other owners (excluding this owner).
     */
    static boolean warning(Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
            return false;
        }
        Integer id = owner.getId();
        long count = ownerRepository.findAll().stream()
            .filter(o -> city.equalsIgnoreCase(o.getCity()))
            .filter(o -> id == null || !id.equals(o.getId()))
            .count();
        return count >= WARNING_THRESHOLD && count < CITY_CAPACITY;
    }
}
