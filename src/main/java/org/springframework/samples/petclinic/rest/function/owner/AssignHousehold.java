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
 * affects the id (it only governs whether a second member of a household is treated as an
 * acknowledged member rather than a suspected duplicate; see {@link CheckPossibleDuplicate}). An owner
 * with no postcode has no household to key on and is left with a {@code null} {@code householdId}.
 *
 * <p>The {@code householdId} is the value the household-size steps — {@link CountHouseholdMembers} and
 * {@link CapMembershipLevel} — key off. It is <em>not</em> part of the owner's
 * {@link org.springframework.samples.petclinic.model.OwnerIdentity} key, and duplicate detection (both
 * the hard {@code identityKey} check and the soft {@code soundex(lastName) + postcode} match) no longer
 * keys off it.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.of(owner).householdId());
    }
}
