package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code householdId} — a stable identifier derived from the normalized last name
 * and address (see {@link Household}). Because the derivation is deterministic, every owner at the same
 * household (including one that joined an existing household via {@code sharesHousehold}) receives the
 * same value, which is then persisted and returned.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Household.id(owner.getLastName(), owner.getAddress()));
    }
}
