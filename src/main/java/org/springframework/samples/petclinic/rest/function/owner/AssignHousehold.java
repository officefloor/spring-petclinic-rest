package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the owner's {@code householdId}: the deterministic identifier derived from the
 * owner's last name and postcode by {@link Households}. Because the id is a pure function
 * of {@code (lastName, postcode)}, every owner in a household computes the same value with
 * no explicit linking — the former back-fill of existing members is no longer needed.
 *
 * <p>The {@code sharesHousehold} request flag no longer affects this step: it only bypasses
 * the create-time duplicate block (see {@link EnsureUniqueHousehold}). An owner with no
 * postcode belongs to no household and keeps a null {@code householdId}.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.householdId(owner.getLastName(), owner.getPostcode()));
    }
}
