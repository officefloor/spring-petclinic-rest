package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: records on the owner whether its city is approaching the hard
 * capacity limit. Sets {@code capacityWarning} true when the owner's city already holds between 40
 * and 49 owners, false otherwise. Counts the owners present before this create (the new owner is not
 * yet saved) with the same exact city comparison as {@link RequireCityCapacity}, and mutates the
 * built owner in place before it is persisted. The hard rejection at 50 has already been enforced by
 * {@link RequireCityCapacity} earlier in the pipeline, so a count of 50 or more never reaches here.
 */
public class FlagCapacityWarning {

    /** Owners already in the city at or above which the response carries a capacity warning. */
    private static final int WARNING_THRESHOLD = 40;

    /** Maximum number of owners permitted in a single city (the hard limit, enforced elsewhere). */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(existing.getCity())) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CITY_CAPACITY);
    }
}
