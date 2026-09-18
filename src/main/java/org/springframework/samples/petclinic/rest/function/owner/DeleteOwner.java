package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes the loaded owner: it flags the record {@code deleted} and saves it, retaining the
 * row so {@code GET /api/owners/{id}} still returns the owner (now with {@code deleted} true). A
 * deleted owner is thereafter ignored by the create endpoint's duplicate and identity checks.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
