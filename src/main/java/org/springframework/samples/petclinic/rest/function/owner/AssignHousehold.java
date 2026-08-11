package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.OwnerIdentities;

/**
 * Stamps the deterministic {@code householdId} onto the freshly built {@link Owner}.
 *
 * <p>The identifier is computed from the owner's own last name and postcode alone (see
 * {@link OwnerIdentities#householdId(Owner)}) — the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + "|" + postcode}. It is therefore assigned to <em>every</em> owner,
 * not only those that declared {@code sharesHousehold}: owners with the same last name and postcode
 * compute the same value and share a household automatically, with no scan of existing owners and no
 * back-filling.
 *
 * <p>Runs after {@link BuildOwner} (which produces the new {@link Owner}) and before the checks that
 * key off the household — {@link CheckUniqueIdentity} (the household-duplicate block) and
 * {@link CheckPossibleDuplicate} — so the household is fixed before it is consulted.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerIdentities.householdId(owner));
    }
}
