package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose email address is already used by another owner,
 * by throwing {@link DuplicateOwnerException}, which is handled as a 409 Conflict.
 *
 * <p>The email is optional: when the candidate has no email, no check is performed.
 */
public class CheckDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String email = DuplicateKey.normalize(owner.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(DuplicateKey.normalize(existing.getEmail()))) {
                throw new DuplicateOwnerException("An owner with the same email already exists");
            }
        }
    }
}
