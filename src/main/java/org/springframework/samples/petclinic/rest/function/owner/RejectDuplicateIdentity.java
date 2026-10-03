package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block. A household is now keyed on (last name, postcode) and every owner carries a
 * deterministic {@code householdId} derived from it (see {@link AssignHousehold}), so two owners
 * are in the same household exactly when they share that id. Creating a second owner in a household
 * that already has a member is rejected as a 409 via {@link DuplicateIdentityException}.
 *
 * <p>The {@code sharesHousehold} create-time flag bypasses this block: a declared household member
 * is allowed through and created. Runs after {@link AssignHousehold} and before {@link SaveOwner}.
 */
public class RejectDuplicateIdentity {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            // Declared household member: bypass the duplicate block entirely.
            return;
        }
        HouseholdKey household = HouseholdKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                // A soft-deleted owner no longer occupies its household.
                continue;
            }
            if (household.matches(existing)) {
                throw new DuplicateIdentityException(
                        "An owner in the same household (last name and postcode) already exists");
            }
        }
    }
}
