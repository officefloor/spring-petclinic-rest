package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The household duplicate block. Because the household is keyed on (last name, postcode) through the
 * deterministic {@link Households#id(Owner) householdId}, a second owner in a household that already
 * has a member is a duplicate and is rejected (409).
 *
 * <p>The request's {@code sharesHousehold} flag bypasses this block: a declared household member is
 * created even though its household already exists. Runs after {@link AssignHousehold} so the new
 * owner's {@code householdId} is set; existing owners are compared by their own computed household.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return; // declared household member: allowed to join an existing household
        }
        String householdId = owner.getHouseholdId();
        boolean inUse = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> !Boolean.TRUE.equals(existing.getDeleted())) // ignore soft-deleted owners
                .anyMatch(existing -> householdId.equals(Households.id(existing)));
        if (inUse) {
            throw new DuplicateIdentityException(
                    "An owner in household " + householdId + " already exists");
        }
    }
}
