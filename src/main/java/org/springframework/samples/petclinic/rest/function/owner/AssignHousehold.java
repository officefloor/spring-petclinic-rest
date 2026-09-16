package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps a newly built owner with its deterministic {@code householdId} — the first 12 hex
 * characters of SHA-256 over its normalized last name and postcode (see
 * {@link Households#householdId}). Because the id is derived purely from
 * {@code (lastName, postcode)}, every owner in the same household resolves to the same id
 * automatically, with no explicit linking; an owner without a postcode has no household
 * ({@code householdId} unset). Runs after {@link BuildOwner} (so the owner's fields are
 * set) and before {@link SaveOwner} (so the id is stored and returned), mutating the built
 * owner in place.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.householdId(owner.getLastName(), owner.getPostcode()));
    }
}
