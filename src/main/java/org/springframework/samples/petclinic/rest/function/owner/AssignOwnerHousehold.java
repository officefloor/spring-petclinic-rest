package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the built owner with its household id. The id is derived deterministically from the
 * owner's last name and postcode (see {@link OwnerHouseholds}), so every owner in the same
 * household ends up with the same value automatically — no opt-in and no back-filling of
 * existing members is needed. Runs after the owner is built and mutates it in place before it
 * is saved.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHouseholds.householdId(owner.getLastName(), owner.getPostcode()));
    }
}
