package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link BuildOwner}. Assigns the deterministic {@code householdId} derived from the owner's
 * last name and postcode ({@link OwnerIdentity#householdId}). Because the id is a pure function of
 * (lastName, postcode), every owner with the same last name and postcode receives the same value with no
 * back-fill: they simply share the household automatically. This runs for every owner — {@code
 * sharesHousehold} no longer decides whether the link is created, it only bypasses the duplicate block
 * upstream in {@link EnsureUniqueOwnerIdentity}.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerIdentity.householdId(owner.getLastName(), owner.getPostcode()));
    }
}
