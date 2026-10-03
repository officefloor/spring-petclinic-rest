package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: {@code true} when the owner's city already
 * holds between {@link CityCapacity#WARNING_THRESHOLD} and
 * {@link CityCapacity#CITY_CAPACITY}-1 owners (inclusive), i.e. it is approaching the hard
 * capacity limit, otherwise {@code false}. The count comes from
 * {@link CityCapacity#countInCity} — the same single source of truth the hard rejection in
 * {@link EnsureCityCapacity} uses, so the warning and the rejection cannot drift apart. The
 * count is over the existing owners (the new owner is not yet saved); since
 * {@link EnsureCityCapacity} runs earlier in the pipeline and rejects at the limit, by the
 * time this step runs the count is always below it.
 */
public class AssessCityCapacity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long inCity = CityCapacity.countInCity(ownerRepository, owner.getCity());
        owner.setCapacityWarning(
            inCity >= CityCapacity.WARNING_THRESHOLD && inCity < CityCapacity.CITY_CAPACITY);
    }
}
