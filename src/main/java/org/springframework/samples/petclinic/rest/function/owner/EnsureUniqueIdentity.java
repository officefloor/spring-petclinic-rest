package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects creating an owner whose whole {@code identityKey} already belongs to another owner.
 * This is the single, consolidated duplicate check: the former separate telephone, email and
 * household checks are all expressed through {@link OwnerIdentity#key(Owner)}. Runs after
 * {@link AssignHousehold} so the new owner's {@code householdId} is part of the compared key.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(owner);
        boolean inUse = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .anyMatch(existing -> OwnerIdentity.key(existing).equals(identityKey));
        if (inUse) {
            throw new DuplicateIdentityException(
                    "An owner with identityKey " + identityKey + " already exists");
        }
    }
}
