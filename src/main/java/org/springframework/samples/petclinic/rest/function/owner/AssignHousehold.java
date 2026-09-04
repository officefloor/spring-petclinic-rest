package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 hex characters of SHA-256 over
 * the version-2 region code, the normalized last name and postcode (see
 * {@link HouseholdId#derive(String, String, String)}).
 * Every owner receives one, so owners sharing a last name and postcode automatically share the id —
 * there is no explicit linking and no dependency on {@code sharesHousehold} (that flag now only
 * bypasses the identity duplicate block in {@link EnsureUniqueIdentity}).
 *
 * <p>Runs after {@link BuildOwner} within the create transaction and before {@link AssignHouseholdSize},
 * which counts the members sharing this computed id, and {@link SaveOwner}, which persists it.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(HouseholdId.of(owner));
    }
}
