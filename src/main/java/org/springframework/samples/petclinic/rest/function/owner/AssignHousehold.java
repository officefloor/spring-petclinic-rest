package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code householdId} — a deterministic identifier derived from the normalized last
 * name and the postcode (see {@link Household}). Because the derivation is deterministic, every owner
 * with the same last name and postcode receives the same value, which is then persisted and returned;
 * an owner admitted to an existing household via {@code sharesHousehold} simply computes the same id.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Household.id(owner.getLastName(), owner.getPostcode()));
    }
}
