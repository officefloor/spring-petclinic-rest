package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the response with {@code capacityWarning}: {@code true} when this owner's city already contains
 * between {@link #WARNING_THRESHOLD 40} and 49 owners (inclusive) — i.e. it is approaching, but has not
 * yet reached, the per-city {@link CheckCityCapacity#CAPACITY capacity limit of 50} — otherwise
 * {@code false}. The per-city count is taken over all owners sharing this owner's
 * {@link OwnerCity#normalize normalized} city via {@link OwnerCity#countInCity}, the same accumulation
 * path as the hard {@link CheckCityCapacity capacity check}. Computed per request and mutated onto the
 * loaded owner in place, not persisted; the hard rejection at 50 is unchanged.
 */
public class FlagCityCapacityWarning {

    /**
     * Lowest per-city owner count at which the warning is raised (inclusive); the city is "approaching
     * capacity" once it holds this many owners but is not yet {@link CheckCityCapacity#CAPACITY full}.
     */
    static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = OwnerCity.countInCity(owner.getCity(), ownerRepository);
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CheckCityCapacity.CAPACITY);
    }
}
