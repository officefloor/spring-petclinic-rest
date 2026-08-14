package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Consolidates all duplicate detection into a single derived {@code identityKey} (see
 * {@link IdentityKey}). Rejects a create-owner request with 409 via {@link DuplicateIdentityException}
 * only when the new owner's WHOLE identityKey equals an existing owner's identityKey; the separate
 * telephone, email and household duplicate checks are all expressed through this one key.
 *
 * <p>Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different identityKeys and are both allowed;
 * only an exact full-key match is a duplicate. Runs after {@link AssignHousehold}, so the new owner's
 * householdId is finalized before its key is derived, and before {@link SaveOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // the owner being created is not yet its own duplicate
            }
            if (IdentityKey.of(existing).equals(identityKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
