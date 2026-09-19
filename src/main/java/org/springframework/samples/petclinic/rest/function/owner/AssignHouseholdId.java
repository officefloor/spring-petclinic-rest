package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps a newly built owner with its deterministic {@code householdId}, derived from its last name
 * and postcode (see {@link Household}). Because the id is a pure function of those two values, owners
 * that share a last name and postcode share the id automatically — no explicit opt-in creates the
 * link. An owner without a postcode belongs to no household and is left without an id.
 *
 * <p>Runs under the request transaction, after {@link BuildOwner}, so later steps that key off the
 * household ({@link MarkDeclaredHouseholdMember}, {@link CountHouseholdMembers}) see the final id.
 * The household id is no longer part of the {@link Owner#getIdentityKey() identity key}.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Household.idFor(owner.getLastName(), owner.getPostcode()));
    }
}
