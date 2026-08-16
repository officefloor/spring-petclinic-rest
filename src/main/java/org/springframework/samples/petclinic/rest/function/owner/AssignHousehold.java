package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 upper-case hex characters of
 * SHA-256 over the normalized last name and postcode (see {@link OwnerIdentity#householdIdFor}).
 *
 * <p>The identifier is derived purely from {@code (lastName, postcode)}, so owners with the same last
 * name and postcode share it automatically, regardless of creation order and regardless of whether
 * they opted in with {@code sharesHousehold} — that flag now only bypasses the duplicate block (see
 * {@link CheckIdentityUnique}), it no longer creates the link. An owner without a postcode is not in
 * any household and is left with no identifier.
 *
 * <p>Runs after {@link BuildOwner} (which publishes the new owner) and before {@link SaveOwner}.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerIdentity.householdIdFor(owner.getLastName(), owner.getPostcode()));
    }
}
