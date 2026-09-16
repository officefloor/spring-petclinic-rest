package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create whose city is approaching the {@link RejectFullOwnerCity#CITY_CAPACITY} hard
 * limit, so the response carries {@code capacityWarning}. True when the city already holds
 * between {@link #CAPACITY_WARNING_THRESHOLD} and {@code CITY_CAPACITY - 1} owners (40-49)
 * inclusive; at {@code CITY_CAPACITY} the create was already rejected 409 by
 * {@link RejectFullOwnerCity}, so it never reaches here. City membership is counted
 * case-insensitively via {@link OwnerCities}, matching that rejection so the two rules see the
 * city identically. Runs after {@link BuildOwner} but before the owner is saved, so the count
 * reflects the owners that existed before this create, and mutates the built {@link Owner} in place.
 */
public class AssignOwnerCapacityWarning {

    /** Owners already in a city at or above which a further create is warned as approaching capacity. */
    static final int CAPACITY_WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int existing = OwnerCities.size(ownerRepository.findAll(), owner.getCity());
        owner.setCapacityWarning(
                existing >= CAPACITY_WARNING_THRESHOLD && existing < RejectFullOwnerCity.CITY_CAPACITY);
    }
}
