package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes the owner: the record is retained and flagged {@code deleted} rather than removed,
 * so a later GET still returns it (with {@code deleted} true) and the create endpoint's duplicate
 * and identity checks can ignore it.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(Boolean.TRUE);
        ownerRepository.save(owner);
    }
}
