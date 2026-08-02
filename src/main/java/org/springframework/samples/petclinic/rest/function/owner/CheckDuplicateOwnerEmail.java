package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose email address is already used by another owner. The email is
 * optional: when the request omits it there is nothing to enforce and the check passes. Runs
 * before {@link SaveOwner} so a duplicate never reaches the data store; the thrown escalation
 * maps to 409 Conflict. The comparison ignores letter case and surrounding or repeated
 * whitespace (see {@link DuplicateKey}).
 */
public class CheckDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional; nothing to enforce
        }
        String normalizedEmail = DuplicateKey.normalize(email);
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(DuplicateKey.normalize(existing.getEmail()), normalizedEmail)) {
                throw new DuplicateOwnerException(
                        "An owner with the same email already exists");
            }
        }
    }
}
