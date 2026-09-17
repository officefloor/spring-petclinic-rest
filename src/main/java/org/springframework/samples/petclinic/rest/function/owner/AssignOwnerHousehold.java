package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner its deterministic {@code householdId}, derived from its last name and
 * postcode (see {@link OwnerHouseholds}). Every owner is keyed into a household automatically:
 * owners with the same last name and postcode share the value without opting in. The
 * {@code sharesHousehold} request directive no longer creates this link — it only bypasses the
 * duplicate block in {@link RequireUniqueIdentity}.
 *
 * <p>Runs after {@link BuildOwner}, so it works with the built {@link Owner} entity, and before
 * {@link AssignOwnerHouseholdSize}, which counts the household using this value.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHouseholds.of(owner));
    }
}
