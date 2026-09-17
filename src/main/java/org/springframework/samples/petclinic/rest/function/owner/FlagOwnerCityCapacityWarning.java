package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags an owner whose city is approaching its capacity limit: sets
 * {@link Owner#getCapacityWarning() capacityWarning} true when the city already holds between
 * {@value #WARNING_THRESHOLD} and {@link RequireCityCapacity#CAPACITY} (exclusive) owners,
 * otherwise false. Runs after {@link RequireCityCapacity} has admitted the request, so the
 * count is always below the hard limit.
 */
public class FlagOwnerCityCapacityWarning {

    /** Owners in a city at or above which the approaching-capacity warning is raised. */
    static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long inCity = OwnerCities.countIn(ownerRepository, owner.getCity());
        owner.setCapacityWarning(inCity >= WARNING_THRESHOLD && inCity < RequireCityCapacity.CAPACITY);
    }
}
