package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose telephone number is already used by any other owner. Runs
 * before {@link SaveOwner} so a duplicate never reaches the data store; the thrown escalation
 * maps to 409 Conflict.
 */
public class CheckDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new DuplicateOwnerException(
                        "An owner with the same telephone already exists");
            }
        }
    }
}
