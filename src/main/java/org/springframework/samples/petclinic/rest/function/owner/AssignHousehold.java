package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link BuildOwner}. Stamps the shared household identifier
 * derived by {@link EnsureUniqueHousehold} onto the owner being created so it is stored and returned.
 * When the request did not join an existing household the identifier is {@code null} and the owner
 * keeps none.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val HouseholdId householdId) {
        if (householdId != null && householdId.value() != null) {
            owner.setHouseholdId(householdId.value());
        }
    }
}
