package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner's deterministic {@code householdId} (see {@link Households}),
 * derived from its last name and postcode, so every owner that shares a last name and
 * postcode resolves to the same stable value automatically — no coordination and no
 * opt-in required. An owner with no postcode gets no household id. Runs after
 * {@link BuildOwner} (so the entity, its name and postcode exist) and before
 * {@link AssignOwnerHouseholdSize} and {@link AssignOwnerMembershipLevelCap}, which key off
 * the household, and {@link SaveOwner}.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.idFor(owner.getLastName(), owner.getPostcode()));
    }
}
