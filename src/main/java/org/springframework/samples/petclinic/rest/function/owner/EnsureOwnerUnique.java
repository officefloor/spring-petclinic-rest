package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * Rejects creation of an <code>Owner</code> when another owner already has the same
 * last name and the same telephone number.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (isSameOwner(existing, owner)) {
                throw new OwnerAlreadyExistsException(
                        "An owner with the same last name and telephone already exists");
            }
        }
    }

    private static boolean isSameOwner(Owner existing, Owner candidate) {
        return Objects.equals(existing.getLastName(), candidate.getLastName())
                && Objects.equals(existing.getTelephone(), candidate.getTelephone());
    }
}
