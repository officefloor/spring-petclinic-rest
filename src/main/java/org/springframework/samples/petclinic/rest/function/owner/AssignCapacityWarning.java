package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: {@code true} when this owner's city already holds
 * between 40 and 49 owners (inclusive, compared case-insensitively) — approaching the hard capacity
 * limit of 50 enforced by {@link CheckOwnerCityCapacity}; {@code false} otherwise. The count uses
 * the same set of owners the capacity check counts, so the warning band ends exactly where the hard
 * rejection begins.
 *
 * <p>Runs before {@link SaveOwner}, so the count reflects the owners already persisted (i.e. the
 * value before this create) and the new owner itself is not counted.
 */
public class AssignCapacityWarning {

    /** Inclusive lower bound of the "approaching capacity" band. */
    private static final int WARN_THRESHOLD = 40;

    /** The maximum number of owners permitted in a single city (the hard limit). */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city == null ? existing.getCity() == null : city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARN_THRESHOLD && count < CITY_CAPACITY);
    }
}
