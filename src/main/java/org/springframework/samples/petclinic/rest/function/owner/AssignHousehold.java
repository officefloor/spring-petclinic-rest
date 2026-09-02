package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner its deterministic {@code householdId}, derived from the last name and
 * postcode (see {@link IdentityKey#household}). Because the id is computed rather than linked, every
 * owner sharing a last name and postcode ends up with the same value automatically; an owner without
 * a postcode has no household. This no longer depends on {@code sharesHousehold}: that flag only
 * bypasses the household-duplicate block, it does not create the link.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(IdentityKey.household(owner.getLastName(), owner.getPostcode()));
    }
}
