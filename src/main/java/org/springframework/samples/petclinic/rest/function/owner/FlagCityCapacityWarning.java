package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a city approaching its capacity limit on create: sets the owner's
 * {@code capacityWarning} to {@code true} when the owner's city already contains
 * between 40 and 49 existing owners (inclusive) — approaching the hard capacity limit
 * of 50 — otherwise {@code false}. Shares the city-matching predicate with
 * {@link RejectCityAtCapacity} via {@link OwnerCityCount}.
 *
 * <p>Runs before {@link SaveOwner}, so the owner being created is not counted, and
 * after {@link RejectCityAtCapacity}, which has already escalated a 409 for any city at
 * (or over) 50 — so by the time this step runs the count is at most 49.
 */
public class FlagCityCapacityWarning {

    private static final int WARN_THRESHOLD = 40;

    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = OwnerCityCount.sharingCity(ownerRepository, owner.getCity());
        owner.setCapacityWarning(count >= WARN_THRESHOLD && count < CITY_CAPACITY);
    }
}
