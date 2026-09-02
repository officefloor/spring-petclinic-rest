package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * Rejects 409 when the built owner shares another owner's {@link HouseholdId} — that is, another
 * owner already exists with the same last name and postcode. Opting in with {@code sharesHousehold}
 * bypasses this block: the owner is then created as a declared household member (and, being
 * declared, is not later flagged as a possible duplicate).
 */
public class RejectDuplicateOwnerHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        if (Boolean.TRUE.equals(owner.getSharesHousehold())) {
            return;
        }
        String householdId = HouseholdId.of(owner);
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && householdId.equals(HouseholdId.of(other))) {
                throw new DuplicateOwnerIdentityException(householdId);
            }
        }
    }
}
