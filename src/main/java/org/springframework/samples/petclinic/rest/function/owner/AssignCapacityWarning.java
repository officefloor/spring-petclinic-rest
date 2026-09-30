package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code capacityWarning}: true when this owner's city already holds
 * between 40 and 49 owners (approaching the hard capacity limit of 50), otherwise false. Reads
 * the per-city total published by {@link CountCityOwners} — the same accumulation the hard
 * {@link EnsureCityCapacity} rule checks — which is taken before the owner is saved so the count
 * excludes the owner being created. The hard rejection at 50 has already happened in
 * {@link EnsureCityCapacity}, so the count seen here is always below the limit and only the lower
 * threshold is tested. The flag is stamped on the owner so it is persisted and returned on read,
 * and never changes afterwards.
 */
public class AssignCapacityWarning {

    /** Number of existing owners in a city at or above which a create is warned. */
    private static final int CAPACITY_WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner,
            @Qualified("cityOwnerCount") @Val Integer cityOwnerCount) {
        owner.setCapacityWarning(cityOwnerCount >= CAPACITY_WARNING_THRESHOLD);
    }
}
