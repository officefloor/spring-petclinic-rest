package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for create-owner. It consolidates the former separate telephone,
 * email and household duplicate checks into one comparison of the derived
 * {@link OwnerIdentity#key(Owner) identityKey}: a request is rejected with a 409 Conflict only
 * when its WHOLE identityKey equals an existing owner's. Runs after {@link BuildOwner} has
 * normalized the telephone/email and {@link AssignOwnerHousehold} has settled the household id,
 * so every component of the key is final.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not a conflict
            }
            if (identityKey.equals(OwnerIdentity.key(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
