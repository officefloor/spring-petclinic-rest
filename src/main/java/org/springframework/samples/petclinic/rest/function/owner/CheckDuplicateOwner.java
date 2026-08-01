package org.springframework.samples.petclinic.rest.function.owner;

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
        String telephone = DuplicateKey.telephone(owner.getTelephone());
        if (telephone == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(DuplicateKey.telephone(existing.getTelephone()))) {
                throw new DuplicateOwnerException("An owner with the same telephone already exists");
            }
        }
    }
}
