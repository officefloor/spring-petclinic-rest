package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: {@code true} when this owner's {@code city} already
 * held between 40 and 49 owners (inclusive) at the moment this owner was created — signalling the
 * city is approaching the hard capacity limit of {@value RequireOwnerCityCapacity#CITY_CAPACITY} —
 * otherwise {@code false}. The city is matched case-insensitively, the same match used by
 * {@link RequireOwnerCityCapacity} (the hard rejection at 50) and the per-city customer-code
 * sequence.
 *
 * <p>Runs after {@link BuildOwner} (so the city is set on the owner) and before {@link SaveOwner},
 * so the count reflects only the existing owners, not the one being created. The hard rejection at
 * {@value RequireOwnerCityCapacity#CITY_CAPACITY} runs earlier, so a city at capacity is already a
 * 409 and never reaches this step.
 */
public class AssignCapacityWarning {

    /** A warning is raised once the city holds at least this many owners. */
    static final long CAPACITY_WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        boolean warn = inCity >= CAPACITY_WARNING_THRESHOLD
                && inCity < RequireOwnerCityCapacity.CITY_CAPACITY;
        owner.setCapacityWarning(warn);
    }
}
