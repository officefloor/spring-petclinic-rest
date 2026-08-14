package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's {@code capacityWarning}: {@code true}
 * when this owner's city already held between 40 and 49 owners (approaching the capacity limit of
 * 50) before this owner was created, otherwise {@code false}. Cities are compared
 * case-insensitively, mirroring {@link EnsureCityCapacity} which hard-rejects at 50.
 *
 * <p>Runs before {@link SaveOwner}, so the owner being created is not yet persisted and never
 * counts itself.
 */
public class AssignCapacityWarning {

    private static final int WARNING_THRESHOLD = 40;
    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city == null ? existing.getCity() == null : city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CITY_CAPACITY);
    }
}
