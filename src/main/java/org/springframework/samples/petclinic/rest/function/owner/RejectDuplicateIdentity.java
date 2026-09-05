package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose {@code identityKey} (see {@link OwnerIdentity}) equals
 * an existing owner's. This is the single, consolidated duplicate check: the former separate
 * telephone, email and household checks are now expressed through this one key. A collision is
 * rejected 409 via {@link DuplicateIdentityException}.
 *
 * <p>Runs after {@link AssignHousehold} (so the new owner's {@code householdId} — and any
 * household members it just backfilled — are set) and within the create transaction, but
 * before {@link SaveOwner}, so the new owner is not yet persisted and cannot collide with
 * itself. Because the telephone is part of the key, two members of the same household with
 * different telephones have different keys and are both allowed; only an exact full-key match
 * is a duplicate.
 */
public class RejectDuplicateIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.key(existing))) {
                throw new DuplicateIdentityException(
                        "Another owner with the same identity already exists");
            }
        }
    }
}
