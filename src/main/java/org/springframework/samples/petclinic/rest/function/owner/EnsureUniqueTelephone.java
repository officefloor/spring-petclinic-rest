package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects creating an owner whose normalized telephone is already used by another owner.
 * The telephone on the built {@link Owner} has already been normalized by {@link BuildOwner}.
 */
public class EnsureUniqueTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        boolean inUse = ownerRepository.findByTelephone(owner.getTelephone()).stream()
                .anyMatch(existing -> !existing.getId().equals(owner.getId()));
        if (inUse) {
            throw new DuplicateTelephoneException(
                    "An owner with telephone " + owner.getTelephone() + " already exists");
        }
    }
}
