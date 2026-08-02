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
 * maps to 409 Conflict.
 */
public class CheckDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional; nothing to enforce
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getEmail(), email)) {
                throw new DuplicateOwnerException(
                        "An owner with the same email already exists");
            }
        }
    }
}
