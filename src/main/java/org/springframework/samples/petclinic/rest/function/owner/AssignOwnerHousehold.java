package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in {@code POST /api/owners} after the owner entity is built and before it is saved, within
 * the create transaction. Stamps the built owner with the deterministic
 * {@link Household#id(String, String) household id} for its lastName and postcode. Because the id
 * is derived from those two fields alone, every owner in a household computes the same value
 * automatically — no backfill of existing members is needed and no opt-in is required.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Household.id(owner.getLastName(), owner.getPostcode()));
    }
}
