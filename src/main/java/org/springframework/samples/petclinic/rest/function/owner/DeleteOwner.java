package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Delete step for {@code DELETE /api/owners/{ownerId}}: soft-deletes the owner by flagging it
 * {@code deleted} and saving, rather than removing the row. The record is retained so a later
 * {@code GET} still returns it (with {@code deleted} true); the create endpoint's duplicate and
 * identity checks then ignore owners flagged deleted.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
