package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.util.OwnerIdentity;

/**
 * The single duplicate-detection step for the create-owner pipeline: it rejects a request whose
 * derived {@code identityKey} (normalizedTelephone|email|householdId) exactly equals an existing
 * owner's. This one check subsumes the former separate telephone, email and household checks.
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold}, so the new owner's telephone and
 * email are normalized and its household id is finalized (including any back-fill of existing
 * members) - the key is therefore compared against every other owner's finalized key. Because the
 * telephone is part of the key, two household members with different telephones have different keys
 * and both are allowed; only an exact whole-key match is a duplicate. Throws
 * {@link DuplicateIdentityException} (handled as 409) on a collision.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(owner);
        for (Owner existing : ownerRepository.findAll()) {
            // The owner being created is not yet persisted, so findAll() returns only other owners.
            if (identityKey.equals(OwnerIdentity.key(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
