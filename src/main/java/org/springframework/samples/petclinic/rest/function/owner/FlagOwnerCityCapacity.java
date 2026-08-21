package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Stamps {@code capacityWarning} onto the newly built owner: {@code true} once the owner's city
 * already holds between {@value #WARN_THRESHOLD} and {@value #CAPACITY} minus one owners, flagging a
 * city that is approaching the hard capacity limit of {@value #CAPACITY}; otherwise {@code false}.
 * The city is compared case-insensitively, matching {@link CheckOwnerCityCapacity} which rejects a
 * city that has already reached {@value #CAPACITY}. Runs before {@link SaveOwner}, so the new owner
 * is not yet persisted and therefore not counted among the existing owners.
 */
public class FlagOwnerCityCapacity {

    /** A city warns once it already holds at least this many owners. */
    static final int WARN_THRESHOLD = 40;

    /** A city is full once it already holds this many owners; the warning band stops just below it. */
    static final int CAPACITY = CheckOwnerCityCapacity.CAPACITY;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = ownerRepository.findAll().stream()
                .map(Owner::getCity)
                .filter(existing -> city == null ? existing == null : city.equalsIgnoreCase(existing))
                .count();
        owner.setCapacityWarning(count >= WARN_THRESHOLD && count < CAPACITY);
    }
}
