package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Consolidated duplicate detection for {@code POST /api/owners}: rejects a create with 409 via
 * {@link DuplicateIdentityException} only when the new owner's WHOLE {@code identityKey} (see
 * {@link IdentityKey}) — {@code normalizedTelephone|email|householdId} — equals an existing owner's,
 * i.e. the same telephone, email and household. Members of the same household with a different
 * telephone (or email) have different keys, so several owners may legitimately share one household;
 * this is what lets a household accumulate members for the membership-level cap (see
 * {@link CapMembershipLevel}). Only a truly identical record is a conflict.
 *
 * <p>Runs after {@link AssignHousehold}, so the new owner's householdId is finalized before it is
 * compared, and before {@link SaveOwner}. Soft-deleted owners no longer block a create.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // the owner being created is not yet its own duplicate
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            if (IdentityKey.of(existing).equals(identityKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
