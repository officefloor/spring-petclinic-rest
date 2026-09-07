package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code DELETE /api/owners/{ownerId}} that soft-deletes the owner: rather than removing the
 * row it flags the owner {@code deleted} and saves it, so the record is retained and {@code GET
 * /api/owners/{ownerId}} still returns it. A deleted owner is thereafter ignored by the create
 * endpoint's duplicate/identity checks (see {@link RequireUniqueIdentity} and
 * {@link FlagPossibleDuplicate}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
