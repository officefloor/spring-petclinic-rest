package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.util.OwnerIdentity;

/**
 * The single duplicate-detection step for the create-owner pipeline: it rejects a request whose
 * derived {@code identityKey} - the SHA-256 over
 * {@code normalizedTelephone|lowerEmail|soundex(lastName)} - exactly equals an existing owner's.
 * This one check subsumes the former separate telephone, email and household checks.
 *
 * <p>The email-domain blocklist is applied earlier by {@link ValidateOwnerFields} (a blocklisted
 * domain is a 400 before this step runs), so identity comparison never sees a blocklisted email.
 *
 * <p>Runs after {@link BuildOwner}, so the new owner's telephone and email are normalized - the key
 * is compared against every other owner's key. A soft-deleted owner is ignored, so its key never
 * blocks a new create. Because the telephone is part of the key, two owners with the same last name
 * and postcode but different telephones have different keys and both are allowed (they become a soft
 * match later); only an exact whole-key match is a duplicate. Throws
 * {@link DuplicateIdentityException} (handled as 409) on a collision.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(owner);
        for (Owner existing : ownerRepository.findAll()) {
            // The owner being created is not yet persisted, so findAll() returns only other owners.
            // A soft-deleted owner is ignored, so its key never blocks a new create.
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.key(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
