package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose telephone number is already used by any other owner, by
 * throwing a {@link DuplicateOwnerException}, handled as 409.
 */
public class RejectDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        if (owner.getTelephone() == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(existing, owner)) {
                continue;
            }
            if (DuplicateMatching.matches(existing.getTelephone(), owner.getTelephone())) {
                throw new DuplicateOwnerException(
                    "An owner with the same telephone number already exists");
            }
        }
    }

    private static boolean isSameOwner(Owner existing, Owner candidate) {
        return existing.getId() != null && existing.getId().equals(candidate.getId());
    }
}
