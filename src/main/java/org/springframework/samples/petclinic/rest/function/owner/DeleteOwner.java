package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes the loaded owner: flags it {@code deleted} and saves, retaining the record
 * so a later {@code GET} still returns it (with {@code deleted} true) and the create
 * endpoint's duplicate and identity checks can ignore it. Runs after {@link LoadOwner}.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
