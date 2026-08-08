package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The consolidated duplicate check, run after {@link AssignOwnerHousehold} (so the
 * deterministic {@code householdId} is set) and before {@link SaveOwner}. Duplicate
 * detection is expressed entirely through the {@code identityKey} (see
 * {@link OwnerIdentity}): a new owner is rejected as a 409 Conflict only when its whole
 * {@code identityKey} equals an existing owner's — an identical owner is a hard
 * duplicate.
 *
 * <p>Because the telephone is part of that key, two genuinely distinct members of the
 * same household (same computed {@code householdId} but different telephones) have
 * different keys and are <em>both allowed</em>: a household legitimately holds several
 * members, which is what the household-derived membership-level ceiling
 * ({@link CapOwnerMembershipLevel}) keys off. Sharing a household is therefore no longer
 * a conflict in itself; only an exact identity match is.
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
