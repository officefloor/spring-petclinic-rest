package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link BuildOwner} on {@code POST /api/owners} and stamps the new owner's
 * {@code householdId}.
 *
 * <p>The id is <em>deterministic</em>: it is computed by {@link OwnerHousehold#householdId()} from the
 * owner's {@code lastName} and {@code postcode} alone, so two owners with the same last name and
 * postcode receive the same {@code householdId} automatically — there is no longer any lookup of an
 * existing household to join or any minted/donated identifier, and {@code sharesHousehold} no longer
 * affects the id (it only governs whether a second member of a household is accepted; see
 * {@link CheckOwnerHouseholdUnique}). An owner with no postcode has no household to key on and is left
 * with a {@code null} {@code householdId}.
 *
 * <p>The {@code householdId} is a component of the owner's
 * {@link org.springframework.samples.petclinic.model.OwnerIdentity} key and is the single value that
 * duplicate detection and the household-size count ({@link CountHouseholdMembers}) key off.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.of(owner).householdId());
    }
}
