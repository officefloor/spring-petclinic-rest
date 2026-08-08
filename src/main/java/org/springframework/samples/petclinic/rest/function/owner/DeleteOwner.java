package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: flags {@code deleted} true and persists the change, retaining
 * the row. The owner stays loadable by {@link LoadOwner} (so GET still returns it with
 * {@code deleted} true), but is ignored by the create endpoint's duplicate/identity
 * checks (see {@link RejectDuplicateOwnerIdentity}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
