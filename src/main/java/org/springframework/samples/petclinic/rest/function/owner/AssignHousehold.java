package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the new owner with its deterministic {@code householdId} (see {@link HouseholdKey}), derived
 * from the owner's last name and postcode. Every owner is assigned one — owners with the same last
 * name and postcode compute the same value and therefore share the household automatically, with no
 * linking or backfilling of existing owners.
 *
 * <p>The {@code sharesHousehold} flag no longer creates the link (the id is deterministic regardless);
 * it only bypasses the duplicate block in {@link RequireUniqueIdentity} so a declared household member
 * may be created.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(HouseholdKey.householdId(owner));
    }
}
