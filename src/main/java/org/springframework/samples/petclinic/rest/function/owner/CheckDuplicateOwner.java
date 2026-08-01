package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose telephone number is already used by any other
 * owner, by throwing {@link DuplicateOwnerException}, which is handled as a 409
 * Conflict.
 */
public class CheckDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findAll()) {
            if (isDuplicateTelephone(existing, owner)) {
                throw new DuplicateOwnerException("An owner with the same telephone already exists");
            }
        }
    }

    private static boolean isDuplicateTelephone(Owner existing, Owner candidate) {
        return existing.getTelephone() != null
                && Objects.equals(existing.getTelephone(), candidate.getTelephone());
    }
}
