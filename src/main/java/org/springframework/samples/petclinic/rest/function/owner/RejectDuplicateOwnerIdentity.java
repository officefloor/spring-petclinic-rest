package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * Rejects 409 when the built owner shares another owner's {@link IdentityKey}. This one check
 * replaces the former separate telephone, email and household duplicate checks: an owner collides
 * only on an exact match of the telephone-and-email identity, so two members of one household with
 * different telephones (or different emails) are both allowed.
 */
public class RejectDuplicateOwnerIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String telephone = IdentityKey.telephone(owner);
        String email = IdentityKey.email(owner);
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && !Boolean.TRUE.equals(other.getDeleted())
                    && telephone.equals(IdentityKey.telephone(other))
                    && email.equals(IdentityKey.email(other))) {
                throw new DuplicateOwnerIdentityException(IdentityKey.of(owner));
            }
        }
    }
}
