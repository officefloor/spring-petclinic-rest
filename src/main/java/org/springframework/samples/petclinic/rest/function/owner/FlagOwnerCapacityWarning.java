package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the owner with {@code capacityWarning} when the owner's city is approaching capacity.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link BuildOwner} and before
 * {@link SaveOwner}, so the new owner is not yet persisted and therefore not counted. When the
 * owner's city already holds between {@value #WARNING_THRESHOLD} and {@code CITY_CAPACITY - 1}
 * owners (inclusive) the warning is set to {@code true}; otherwise {@code false}. It shares the
 * {@link OwnerCities#countInCity} count and {@link OwnerCities#CITY_CAPACITY} limit with
 * {@link CheckOwnerCityCapacity}, which hard-rejects (409) once a city reaches capacity — so by the
 * time this step runs the count is always below the limit. The value is frozen at creation time and
 * returned unchanged on later reads.
 */
public class FlagOwnerCapacityWarning {

    /** At or above this many owners already in the city, the next owner carries the warning. */
    private static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long ownersInCity = OwnerCities.countInCity(ownerRepository, owner.getCity());
        owner.setCapacityWarning(
                ownersInCity >= WARNING_THRESHOLD && ownersInCity < OwnerCities.CITY_CAPACITY);
    }
}
