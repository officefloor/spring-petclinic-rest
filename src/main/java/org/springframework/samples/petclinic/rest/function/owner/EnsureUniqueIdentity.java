package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a new owner whose whole {@link OwnerIdentity#key(Owner) identityKey} equals an existing
 * owner's. This consolidates the former separate telephone, email and household checks into one key,
 * so an exact full-key match is the only conflict. Runs after {@link AssignHousehold} (which fixes the
 * householdId part) and before Save, so the collision is a 409, not a persisted duplicate.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateTelephoneException {
        String identityKey = OwnerIdentity.key(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.key(existing))) {
                throw new DuplicateTelephoneException(owner.getTelephone());
            }
        }
    }
}
