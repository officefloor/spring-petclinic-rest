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
 * <p>Runs within the create transaction (after {@link BuildOwner}/{@link AssignCustomerCode},
 * before {@link SaveOwner}). The duplicate guard ({@link CheckOwnerIdentityUnique}) already decided
 * whether this owner may be created in its household; {@code sharesHousehold} only bypasses that
 * guard, it no longer affects the id assigned here.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.deriveId(owner));
    }
}
