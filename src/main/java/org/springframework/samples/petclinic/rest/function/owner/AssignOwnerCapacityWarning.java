package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in {@code POST /api/owners} after the owner entity is built and before it is saved,
 * within the create transaction. Flags the built owner with a capacity warning when its city
 * (compared case-insensitively) is approaching the per-city capacity limit: it already holds
 * between {@link #CAPACITY_WARNING_THRESHOLD} and {@link EnsureCityHasCapacity#CITY_CAPACITY}
 * minus one owners (inclusive). The hard rejection at {@link EnsureCityHasCapacity#CITY_CAPACITY}
 * happened earlier, so at this point the count is at most that limit minus one. Because the new
 * owner has not yet been persisted, {@link OwnerRepository#countInCity(String)} counts only the
 * owners already in the city, so the flag reflects the city's occupancy without counting the
 * owner itself.
 */
public class AssignOwnerCapacityWarning {

    /** City occupancy at or above which the approaching-capacity warning is raised. */
    static final int CAPACITY_WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long cityCount = ownerRepository.countInCity(owner.getCity());
        owner.setCapacityWarning(cityCount >= CAPACITY_WARNING_THRESHOLD
                && cityCount < EnsureCityHasCapacity.CITY_CAPACITY);
    }
}
