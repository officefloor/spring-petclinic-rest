package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId} before it is saved. The identifier is
 * derived purely from the owner's last name and postcode (see {@link Household}), so every owner in
 * the same household receives the same value automatically — no request has to opt in, and the
 * {@code sharesHousehold} hint no longer creates the link (it only bypasses the duplicate block in
 * {@link CheckOwnerIdentityUnique}).
 *
 * <p>Runs after {@link BuildOwner} has produced the {@link Owner} and before {@link SaveOwner}
 * persists it. Mutates the entity in place so {@link SaveOwner} stores the id.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Household.idFor(owner));
    }
}
