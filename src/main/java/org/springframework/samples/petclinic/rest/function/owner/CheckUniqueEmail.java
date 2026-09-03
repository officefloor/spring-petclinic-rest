package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create whose lower-cased email is already used by any other owner,
 * before {@link SaveOwner} runs. Handled with 409 by {@code DuplicateEmailHandler}.
 */
public class CheckUniqueEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        String lower = email.toLowerCase();
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId()) && other.getEmail() != null
                    && lower.equals(other.getEmail().toLowerCase())) {
                throw new DuplicateEmailException(lower);
            }
        }
    }
}
