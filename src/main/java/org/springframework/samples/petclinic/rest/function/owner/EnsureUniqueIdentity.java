package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Consolidated duplicate detection for create-owner: rejects a request whose whole derived
 * {@code identityKey} equals that of an existing owner, responding 409 via
 * {@link DuplicateIdentityException}.
 *
 * <p>The key (see {@link Owner#getIdentityKey()}) is
 * {@code normalizedTelephone|email|householdId}, so the former separate telephone, email and
 * household checks are now a single full-key comparison. Because the telephone is part of the
 * key, two members of the same household (same {@code householdId}) with different telephones
 * have different identity keys and are both allowed; only an exact full-key match is a duplicate.
 *
 * <p>Runs after {@link AssignHousehold} (so the built owner already carries its
 * {@code householdId}, if any) and before {@link SaveOwner} persists anything. The new owner
 * is not yet in the repository, so it is never compared against itself.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
