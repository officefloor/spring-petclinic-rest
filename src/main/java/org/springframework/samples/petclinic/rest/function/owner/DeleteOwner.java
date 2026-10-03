package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: flags the record {@code deleted} and saves it, retaining the row.
 * The owner remains readable via {@code GET /api/owners/{id}} (now carrying {@code deleted} true),
 * but is ignored by the create endpoint's household count and soft duplicate check (see
 * {@link CountHouseholdMembers} and {@link FlagPossibleDuplicate}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(Boolean.TRUE);
        ownerRepository.save(owner);
    }
}
