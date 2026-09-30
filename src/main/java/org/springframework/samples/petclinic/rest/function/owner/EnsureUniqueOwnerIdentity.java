package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check on create: rejects an owner whose whole {@code identityKey}
 * (see {@link OwnerIdentity}) equals an existing owner's. This consolidates the former
 * separate telephone, email and household checks into one key comparison. Runs after the
 * owner is built and its householdId is assigned (so the household component of the key is
 * known) and before it is saved. A clash is rejected 409 via {@link DuplicateIdentityException}.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
