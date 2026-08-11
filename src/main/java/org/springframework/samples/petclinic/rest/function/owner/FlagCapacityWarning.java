package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the {@code capacityWarning} flag on the freshly built {@link Owner} before it is saved:
 * {@code true} when the owner's city already contains between {@value #WARNING_THRESHOLD} and
 * {@value #WARNING_CEILING} owners (approaching the per-city capacity limit enforced by
 * {@link CheckCityCapacity}), otherwise {@code false}.
 *
 * <p>Counts the same case-insensitive city bucket as {@link CheckCityCapacity}. Runs after
 * {@link BuildOwner} (so the city is available on the built owner) and before {@link SaveOwner}, so
 * the new owner is not yet counted — the flag reflects only owners that existed before this create.
 * The hard rejection at {@link CheckCityCapacity#CITY_CAPACITY} already ran earlier in the pipeline,
 * so a request reaching this step is never at or over the limit.
 */
public class FlagCapacityWarning {

    /** Warn once the city already holds at least this many owners. */
    static final int WARNING_THRESHOLD = 40;

    /** Highest existing-owner count that still warns (below the hard cap of 50). */
    static final int WARNING_CEILING = 49;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count <= WARNING_CEILING);
    }
}
