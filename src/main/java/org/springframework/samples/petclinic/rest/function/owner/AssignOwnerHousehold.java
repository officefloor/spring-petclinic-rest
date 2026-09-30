package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the owner's {@code householdId} — the deterministic identifier derived from
 * {@code (lastName, postcode)} by {@link OwnerHousehold}. Every owner receives one, so owners
 * with the same lastName and postcode automatically share a household with no lookup or
 * back-fill. Runs after the owner is built and before the household-keyed rules (duplicate
 * detection) so the householdId is known when they run.
 *
 * <p>{@code sharesHousehold} no longer creates the link (the link is implicit in the hash); it
 * only bypasses the duplicate block in {@link EnsureUniqueOwnerIdentity}.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.householdId(owner.getLastName(), owner.getPostcode()));
    }
}
