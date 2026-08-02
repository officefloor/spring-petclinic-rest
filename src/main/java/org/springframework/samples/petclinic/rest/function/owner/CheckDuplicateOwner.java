package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner when another owner already has the same last name and the same
 * telephone number. Runs before {@link SaveOwner} so a duplicate never reaches the data store;
 * the thrown escalation maps to 409 Conflict.
 */
public class CheckDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        // findByLastName matches on a last-name prefix, so confirm the exact last name too.
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (Objects.equals(existing.getLastName(), owner.getLastName())
                    && Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new DuplicateOwnerException(
                        "An owner with the same last name and telephone already exists");
            }
        }
    }
}
