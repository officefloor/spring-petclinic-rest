package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes the owner: flags it {@code deleted} and saves, retaining the row rather than removing it.
 * The record is still readable via {@code GET /api/owners/{id}} (now reporting {@code deleted} true) but
 * is ignored by the create endpoint's duplicate/identity checks (see {@link CheckIdentityUnique}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
