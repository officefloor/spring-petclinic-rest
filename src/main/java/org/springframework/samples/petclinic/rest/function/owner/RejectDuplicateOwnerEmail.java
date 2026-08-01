package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose email address is already used by another owner,
 * responding 409 via {@link DuplicateOwnerException}. Email is optional: when the request
 * supplies no email the check is skipped. Runs after {@code build} and before {@code save}.
 */
public class RejectDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equalsIgnoreCase(existing.getEmail())) {
                throw new DuplicateOwnerException("An owner with the same email already exists");
            }
        }
    }
}
