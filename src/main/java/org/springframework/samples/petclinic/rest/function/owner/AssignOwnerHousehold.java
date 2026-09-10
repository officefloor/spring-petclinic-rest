package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the deterministic {@code householdId} onto a newly built owner — the first 12 hex
 * characters of SHA-256 over the normalized last name and the postcode (see
 * {@link OwnerHousehold}). Because the value is derived purely from (lastName, postcode) every
 * member of a household resolves to the same identifier regardless of creation order, so no
 * back-fill of earlier members is needed.
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}. An owner with no postcode has
 * no household and keeps a {@code null} householdId. The same derivation feeds the duplicate
 * block in {@link EnsureUniqueOwnerIdentity} and the household size in
 * {@link AssignOwnerHouseholdSize}.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.idFor(owner.getLastName(), owner.getPostcode()));
    }
}
