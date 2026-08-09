package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: flags {@code deleted} true and saves, retaining the row so the owner is still
 * readable via {@code GET /api/owners/{id}} (returned with {@code deleted} true). The create endpoint's
 * duplicate and identity checks ({@link EnsureHouseholdUnique}, {@link EnsureOwnerIdentityUnique}) then
 * treat the owner as absent, so a normally-blocking duplicate is allowed when the only match is deleted.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(Boolean.TRUE);
        ownerRepository.save(owner);
    }
}
