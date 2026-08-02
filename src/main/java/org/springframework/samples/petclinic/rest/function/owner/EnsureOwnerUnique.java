package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerConflictException;

/**
 * Rejects creating an owner whose telephone number is already used by any other
 * owner - with a 409 via {@link OwnerConflictException}.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws OwnerConflictException {
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new OwnerConflictException(
                        "An owner with the same telephone already exists");
            }
        }
    }
}
