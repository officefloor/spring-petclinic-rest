package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The single duplicate check, run before {@link SaveOwner}. Duplicate detection is
 * expressed entirely through the {@code identityKey} (see {@link OwnerIdentity}): a new
 * owner is rejected as a 409 Conflict only when its whole {@code identityKey} equals an
 * existing owner's — an identical owner is a hard duplicate. There is no separate
 * household-duplicate block; the computed {@code householdId} no longer feeds duplicate
 * detection.
 *
 * <p>Because the telephone is part of that key, two owners with the same last name and
 * postcode but different telephones (or emails) have different keys and are <em>both
 * allowed</em>: they are admitted and instead flagged a soft possible duplicate (see
 * {@link AssignOwnerPossibleDuplicate}). Only an exact identity match is a conflict.
 *
 * <p>Soft-deleted owners (flagged {@code deleted}) are skipped entirely: they no longer
 * block a create, so a normally-blocking duplicate is admitted when the only matching
 * owner has been deleted.
 */
public class RejectDuplicateOwnerIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String identityKey = OwnerIdentity.identityKey(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never conflict with the owner itself
            }
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                throw new DuplicateOwnerIdentityException(
                        "An owner with the same identity already exists");
            }
        }
    }
}
