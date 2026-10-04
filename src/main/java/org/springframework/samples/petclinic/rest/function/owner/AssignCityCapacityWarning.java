package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: {@code true} when this owner's city already held
 * between 40 and 49 owners (inclusive) when this owner was created — i.e. it is approaching the
 * per-city hard capacity of 50 — {@code false} otherwise.
 *
 * <p>Runs within the create transaction after {@link BuildOwner} (so the owner's {@code city} is
 * set) and before {@link SaveOwner} (so {@code findAll} excludes the owner being created). Counts
 * through {@link CityCapacity#countOwnersIn} — the same definition {@link CheckOwnerCityCapacity}'s
 * hard cap uses, so the two rules cannot drift. That guard already rejected the request with a 409
 * at {@link CityCapacity#CAPACITY}, so this step only ever sees counts below the hard limit.
 */
public class AssignCityCapacityWarning {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = CityCapacity.countOwnersIn(owner.getCity(), ownerRepository);
        owner.setCapacityWarning(count >= CityCapacity.WARNING_THRESHOLD && count < CityCapacity.CAPACITY);
    }
}
