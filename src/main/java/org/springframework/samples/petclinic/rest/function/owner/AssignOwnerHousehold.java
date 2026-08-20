package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the shared {@code householdId} published by {@link CheckOwnerHouseholdUnique} onto the
 * newly built owner, so a joiner carries the same identifier as the rest of its household. When
 * the request did not join a household the variable is unset and the owner keeps no household id.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, @Val HouseholdId householdId) {
        if (householdId != null) {
            owner.setHouseholdId(householdId.value());
        }
    }
}
