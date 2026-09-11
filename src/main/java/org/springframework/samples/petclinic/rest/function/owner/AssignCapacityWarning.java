package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: true when, before this owner is saved, the
 * owner's city already holds between {@link #WARNING_THRESHOLD} and
 * {@link RequireCityCapacity#CAPACITY} - 1 owners (inclusive) - i.e. it is approaching the
 * hard capacity limit of {@link RequireCityCapacity#CAPACITY}. Runs after {@link BuildOwner}
 * (so the city is set) and before {@link SaveOwner} (so the new owner is not counted),
 * counting the same way as {@link RequireCityCapacity} so the warning tracks the hard limit
 * it approaches. The hard rejection at the limit is unchanged and handled earlier by
 * {@link RequireCityCapacity}.
 */
public class AssignCapacityWarning {

    static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = ownerRepository.findAll().stream()
            .filter(existing -> city == null ? existing.getCity() == null : city.equals(existing.getCity()))
            .count();
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < RequireCityCapacity.CAPACITY);
    }
}
