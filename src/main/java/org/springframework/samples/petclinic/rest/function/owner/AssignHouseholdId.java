package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's household identifier before it is saved. The id is derived from the
 * owner's last name and postcode via {@link HouseholdKey#id(String, String)}, so every owner of
 * the same household receives the same stable value. Runs after {@link BuildOwner} has produced
 * the entity and before {@link SaveOwner} persists it, mutating the built owner in place.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(HouseholdKey.id(owner.getLastName(), owner.getPostcode()));
    }
}
