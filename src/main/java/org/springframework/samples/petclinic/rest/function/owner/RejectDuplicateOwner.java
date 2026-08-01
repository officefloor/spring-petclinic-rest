package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose telephone number is already used by any other owner,
 * responding 409 via {@link DuplicateOwnerException}. The comparison ignores letter case and
 * surrounding or repeated whitespace. Runs after {@code build} and before {@code save}.
 */
public class RejectDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findAll()) {
            if (DuplicateMatching.sameValue(existing.getTelephone(), owner.getTelephone())) {
                throw new DuplicateOwnerException("An owner with the same telephone already exists");
            }
        }
    }
}
