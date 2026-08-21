package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the deterministic {@code householdId} onto a newly built owner. The id is a pure function
 * of the owner's last name and postcode (see {@link OwnerHousehold#id}), so it is always assigned -
 * owners with the same last name and postcode converge on the same value automatically, with no
 * lookup or coordination. Runs after {@link BuildOwner}, so the built owner (with its normalized
 * fields) is available to stamp, and before the household-duplicate check and identity check, which
 * both key off the finalized id.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.id(owner.getLastName(), owner.getPostcode()));
    }
}
