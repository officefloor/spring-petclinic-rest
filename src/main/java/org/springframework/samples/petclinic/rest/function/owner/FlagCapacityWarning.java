package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create whose city is approaching the per-city capacity limit: when the city already
 * holds between {@link #WARNING_THRESHOLD} and {@link EnsureCityHasCapacity#CAPACITY} - 1 owners
 * (inclusive), {@code capacityWarning} is set true, otherwise false. Owners are counted
 * case-insensitively on their trimmed city (see {@link CityRegistrations}).
 *
 * <p>Runs after {@link BuildOwner} has mapped the request to an entity and before
 * {@link SaveOwner} persists it, so the count sees only the owners that existed before this
 * create; the flag is stored on the owner and returned on every later read. This is a soft
 * warning below the hard cap enforced by {@link EnsureCityHasCapacity}.
 */
public class FlagCapacityWarning {

    /** Warn once the city already holds at least this many owners, up to (but below) the hard cap. */
    static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = CityRegistrations.countIn(ownerRepository, owner.getCity());
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < EnsureCityHasCapacity.CAPACITY);
    }
}
