package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code householdId}: a stable identifier shared by all owners who live in the
 * same household, i.e. share a last name and address. It is derived from the normalized last name and
 * address (see {@link OwnerIdentity#householdId}), so two owners who intentionally share a household
 * deterministically receive the same value without any lookup.
 *
 * <p>Runs after {@link BuildOwner} has mapped the request onto the entity and before
 * {@link SaveOwner}, so the assigned id is persisted and returned. It also feeds the derived
 * {@code identityKey} that {@link CheckIdentityUnique} uses for duplicate detection.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerIdentity.householdId(owner.getLastName(), owner.getAddress()));
    }
}
