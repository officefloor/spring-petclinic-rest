package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: flags it {@code deleted=true} and saves, so the row is retained
 * and still loadable by id (a subsequent GET returns the owner with {@code deleted} true),
 * while the create endpoint's duplicate/identity checks skip it (see
 * {@link EnsureUniqueIdentity} and {@link AssignPossibleDuplicate}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
