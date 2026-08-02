package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * When an email address is provided, rejects creating an owner whose email is
 * already used by another owner with a 409 conflict. Owners without an email are
 * unaffected.
 */
public class EnsureOwnerEmailUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getEmail(), email)) {
                throw new OwnerAlreadyExistsException(
                        "Owner with email " + email + " already exists");
            }
        }
    }
}
