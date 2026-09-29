package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes the owner: flags it {@code deleted} and saves the row rather than removing it, so the
 * record is retained (and still returned by {@code GET /api/owners/{id}}) while the create endpoint's
 * duplicate and identity checks ignore it.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
