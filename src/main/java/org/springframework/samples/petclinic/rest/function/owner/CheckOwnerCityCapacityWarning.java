package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link BuildOwner} and before {@link SaveOwner} on {@code POST /api/owners}. Records the
 * new owner's {@code capacityWarning}: it is {@code true} when the owner's city already holds between
 * {@value CheckOwnerCityCapacity#WARN_OWNERS_PER_CITY} and {@value CheckOwnerCityCapacity#MAX_OWNERS_PER_CITY}
 * minus one existing owners (i.e. 40-49), approaching the hard capacity limit of
 * {@value CheckOwnerCityCapacity#MAX_OWNERS_PER_CITY} enforced by {@link CheckOwnerCityCapacity}; otherwise
 * {@code false}.
 *
 * <p>The city count is the very value {@link CheckOwnerCityCapacity} already computed earlier in this
 * pipeline, handed over through the {@link CityOwnerCount}-qualified variable (the owner is still
 * unsaved when either step runs, so the count is identical at both). Reusing the one count means the
 * warning and the hard rejection cannot drift.
 */
public class CheckOwnerCityCapacityWarning {

    public void service(@Val Owner owner, @CityOwnerCount @Val Integer cityOwnerCount) {
        int count = cityOwnerCount;
        owner.setCapacityWarning(count >= CheckOwnerCityCapacity.WARN_OWNERS_PER_CITY
                && count < CheckOwnerCityCapacity.MAX_OWNERS_PER_CITY);
    }
}
