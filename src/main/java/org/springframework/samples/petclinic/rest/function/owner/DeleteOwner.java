package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: flags the loaded {@link Owner} deleted and persists it, retaining the row
 * rather than removing it. A deleted owner is still readable via {@code GET} and is ignored by the
 * create pipeline's duplicate and identity checks (see {@link EnsureUniqueIdentity} and
 * {@link FlagPossibleDuplicate}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
