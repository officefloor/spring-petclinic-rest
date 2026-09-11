package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}, derived from the normalized last
 * name and postcode by {@link Households}. Because the id is a pure function of
 * (lastName, postcode), every owner with the same last name and postcode maps to the same
 * value automatically — there is no opt-in and no back-fill of existing rows. {@code
 * sharesHousehold} no longer creates the link; it only bypasses the duplicate block in
 * {@link RequireUniqueIdentity}. {@link IdentityKeys} folds the same value into an owner's
 * identity key, so the stored {@code householdId} always matches its household component.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.idFor(owner.getLastName(), owner.getPostcode()));
    }
}
