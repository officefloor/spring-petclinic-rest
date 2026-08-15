package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: {@code true} when this owner's city already contained
 * between 40 and 49 owners (inclusive) at the time this owner was created — i.e. it is approaching,
 * but has not yet reached, the hard per-city capacity limit of 50 enforced by {@link CheckCityCapacity}.
 * Otherwise {@code false}.
 *
 * <p>City is compared case-insensitively and counted via {@link OwnerRepository#findAll()}, matching
 * exactly how {@link CheckCityCapacity} counts a city's owners, so the warning band and the hard cap
 * stay aligned. Runs after {@link BuildOwner} and before {@link SaveOwner}, within the same write
 * transaction; it counts the owners persisted so far for that city (excluding this new, not-yet-saved
 * one). The pipeline only reaches this step once {@link CheckCityCapacity} has passed, so the count is
 * at most 49 here.
 */
public class AssignCapacityWarning {

    private static final int WARNING_THRESHOLD = 40;
    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CAPACITY);
    }
}
