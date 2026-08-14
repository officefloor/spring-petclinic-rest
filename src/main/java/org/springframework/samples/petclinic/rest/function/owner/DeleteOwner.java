package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: the row is retained and flagged {@code deleted = true} rather than being
 * removed, so the owner is still readable afterwards but is ignored by the create-owner
 * duplicate/identity checks ({@link EnsureUniqueIdentity}, {@link DetectPossibleDuplicate}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
