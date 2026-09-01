package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a new owner whose lower-cased email (normalized by {@link Owner#setEmail}) already belongs to
 * another owner. Runs after Build and before Save so the conflict is a 409, not a persisted duplicate.
 */
public class EnsureUniqueEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateEmailException {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(existing.getEmail())) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
