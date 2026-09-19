package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Resolves whether the owner response should carry a per-city capacity warning and publishes it for
 * the responder. The warning is raised once the owner's city holds at least {@value #WARN_THRESHOLD}
 * owners but is still below {@link EnsureCityCapacity#CAPACITY}, i.e. it is approaching (but has not
 * reached) the hard limit that {@link EnsureCityCapacity} enforces. Runs before the responder so both
 * the create and read responses carry the flag.
 */
public class ResolveCapacityWarning {

    /** A city warns once it holds this many owners, approaching {@link EnsureCityCapacity#CAPACITY}. */
    static final int WARN_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository,
            @CapacityWarning Out<Boolean> capacityWarning) {
        long count = ownerRepository.countByCity(owner.getCity());
        capacityWarning.set(count >= WARN_THRESHOLD && count < EnsureCityCapacity.CAPACITY);
    }
}
