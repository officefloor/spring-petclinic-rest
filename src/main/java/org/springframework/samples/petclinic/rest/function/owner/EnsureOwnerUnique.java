package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creation of an owner when another owner already has the same last name and
 * the same telephone number.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (isDuplicate(owner, existing)) {
                throw new DuplicateOwnerException(
                        "An owner with the same last name and telephone already exists");
            }
        }
    }

    private static boolean isDuplicate(Owner a, Owner b) {
        return Objects.equals(a.getLastName(), b.getLastName())
                && Objects.equals(a.getTelephone(), b.getTelephone());
    }
}
