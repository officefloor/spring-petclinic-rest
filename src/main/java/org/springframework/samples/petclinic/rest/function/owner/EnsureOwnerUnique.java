package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * Rejects creation of an <code>Owner</code> when its telephone number is already used
 * by any other owner.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        for (Owner existing : ownerRepository.findAll()) {
            if (!isDifferentOwner(existing, owner)) {
                continue;
            }
            if (Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new OwnerAlreadyExistsException(
                        "An owner with the same telephone already exists");
            }
            if (hasEmail && Objects.equals(existing.getEmail(), owner.getEmail())) {
                throw new OwnerAlreadyExistsException(
                        "An owner with the same email already exists");
            }
        }
    }

    private static boolean isDifferentOwner(Owner existing, Owner candidate) {
        return existing.getId() == null || !existing.getId().equals(candidate.getId());
    }
}
