package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: flags {@code deleted} true and re-saves, retaining the row so
 * {@code GET /api/owners/{id}} still returns the owner (now with {@code deleted} true). The
 * create endpoint's duplicate/identity checks ignore owners flagged deleted.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(Boolean.TRUE);
        ownerRepository.save(owner);
    }
}
