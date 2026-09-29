package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the new owner with its deterministic {@code householdId} (see {@link HouseholdKey}), derived
 * from the owner's last name and postcode. Every owner is assigned one — owners with the same last
 * name and postcode compute the same value and therefore share the household automatically, with no
 * linking or backfilling of existing owners.
 *
 * <p>Sharing a household never creates a link (the id is deterministic regardless) and is no longer a
 * duplicate on its own: {@link RequireUniqueIdentity} rejects only an exact {@link IdentityKey} match,
 * so several distinct members may share one household and the {@code sharesHousehold} flag is not
 * required to create them.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(HouseholdKey.householdId(owner));
    }
}
