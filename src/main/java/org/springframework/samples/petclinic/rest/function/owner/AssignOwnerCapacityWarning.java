package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's {@code capacityWarning}: {@code true} when this owner's city
 * already holds between {@value #WARN_THRESHOLD} and {@link EnsureCityUnderCapacity#CAPACITY}
 * owners (i.e. is approaching, but has not yet reached, the hard limit), {@code false}
 * otherwise. Runs after {@link BuildOwner} (so the entity and its city exist) and before
 * {@link SaveOwner} persists the flag, so the freshly created owner is not counted among the
 * city's existing owners. City membership is counted via {@link Cities}, the same set the
 * hard {@link EnsureCityUnderCapacity capacity rejection} counts.
 */
public class AssignOwnerCapacityWarning {

    static final int WARN_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long existing = Cities.countIn(ownerRepository, owner.getCity());
        owner.setCapacityWarning(existing >= WARN_THRESHOLD && existing < EnsureCityUnderCapacity.CAPACITY);
    }
}
