package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: flags it {@link Owner#getDeleted() deleted} and persists the row rather
 * than removing it, so the owner is still retrievable (see {@link RespondWithOwner}) while the
 * create-owner duplicate checks ignore it (see {@link RequireUniqueIdentity}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(Boolean.TRUE);
        ownerRepository.save(owner);
    }
}
