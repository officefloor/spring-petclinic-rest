package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create request whose city is approaching capacity: the owner's capacity warning is set
 * when the city already holds between {@link #WARNING_THRESHOLD} and {@link EnsureCityCapacity#CAPACITY}
 * (exclusive) existing owners, compared case-insensitively — the same count {@link EnsureCityCapacity}
 * uses to enforce the hard limit. Runs after {@link BuildOwner} so it keys off the built owner's city,
 * and before {@link SaveOwner} so the count excludes the owner currently being created. Mutates the
 * built {@link Owner} in place.
 */
public class FlagCapacityWarning {

    /** A city warns once it already holds at least this many owners (up to, but not at, capacity). */
    static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long existing = ownerRepository.countInCity(owner.getCity());
        owner.setCapacityWarning(existing >= WARNING_THRESHOLD && existing < EnsureCityCapacity.CAPACITY);
    }
}
