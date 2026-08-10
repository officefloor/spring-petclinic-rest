package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes the owner: flags it {@code deleted} and saves, retaining the row so the record
 * still reads back via {@code GET /api/owners/{id}} (with {@code deleted} true). The create
 * endpoint's duplicate/identity checks skip owners flagged deleted.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
