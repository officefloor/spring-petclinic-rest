package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose email address is already used by any other owner, by
 * throwing a {@link DuplicateOwnerException}, handled as 409. Email is optional: when the
 * owner has no email, there is nothing to check.
 */
public class RejectDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        if (owner.getEmail() == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(existing, owner)) {
                continue;
            }
            if (Objects.equals(existing.getEmail(), owner.getEmail())) {
                throw new DuplicateOwnerException(
                    "An owner with the same email address already exists");
            }
        }
    }

    private static boolean isSameOwner(Owner existing, Owner candidate) {
        return existing.getId() != null && existing.getId().equals(candidate.getId());
    }
}
