package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the deterministic {@code householdId} published by {@link DetermineOwnerHousehold} onto
 * the newly built owner. As the id is now derived purely from {@code (lastName, postcode)}, it is
 * always present, and every owner sharing the same last name and postcode carries the same value.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, @Val HouseholdId householdId) {
        owner.setHouseholdId(householdId.value());
    }
}
