package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner when another owner already has the same last name and
 * the same telephone number, by throwing {@link DuplicateOwnerException}, which is
 * handled as a 409 Conflict.
 */
public class CheckDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (isDuplicate(existing, owner)) {
                throw new DuplicateOwnerException("An owner with the same last name and telephone already exists");
            }
        }
    }

    private static boolean isDuplicate(Owner existing, Owner candidate) {
        return Objects.equals(existing.getLastName(), candidate.getLastName())
                && Objects.equals(existing.getTelephone(), candidate.getTelephone());
    }
}
