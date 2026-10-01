package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the owner with its deterministic {@code householdId}.
 *
 * <p>Runs after {@link BuildOwner} in the {@code POST /api/owners} pipeline. The id is derived purely
 * from the owner's normalized last name and postcode (see {@link OwnerHousehold#householdId}), so
 * every owner that belongs to the same household computes the same value automatically — no opt-in
 * and no database coordination are needed. An owner with no postcode belongs to no household and
 * receives a {@code null} id.
 *
 * <p>The {@code sharesHousehold} request flag no longer takes part here: it only bypasses the
 * household duplicate block in {@link CheckOwnerHouseholdUnique}; it does not create the link.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.householdId(owner.getLastName(), owner.getPostcode()));
    }
}
