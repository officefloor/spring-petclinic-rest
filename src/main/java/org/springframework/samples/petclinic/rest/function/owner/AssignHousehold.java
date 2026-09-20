package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the new owner with its {@link Households#id(Owner) household id}, derived
 * deterministically from its last name and postcode. Owners sharing a last name and postcode
 * receive the same id automatically, so no existing owner needs updating and the link exists
 * without any {@code sharesHousehold} opt-in.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.id(owner));
    }
}
