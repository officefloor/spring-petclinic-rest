package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Household assignment for {@code POST /owners}. The owner's {@code householdId} is
 * <em>deterministic</em>: it is derived from {@code (lastName, postcode)} alone (see
 * {@link OwnerHousehold#deriveId}), so every owner that shares a last name and postcode derives the
 * same id automatically — there is no longer any explicit linking of existing owners. Owners with
 * no postcode belong to no household and are left with a null {@code householdId}.
 *
 * <p>Runs within the create transaction (after {@link BuildOwner}/{@link AssignMemberId},
 * before {@link SaveOwner}). Sharing a household is no longer a duplicate: several members may
 * belong to the same household as long as their identity keys differ (see
 * {@link CheckOwnerIdentityUnique}). The id assigned here is purely a function of
 * {@code (lastName, postcode)} and is what {@link CapMembershipLevel} later uses to find the
 * household's existing members.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.deriveId(owner));
    }
}
