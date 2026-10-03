package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects a create whose identity collides with an existing owner's. Identity is the single
 * {@link Owner#getIdentityKey() identity key} — the SHA-256 of the normalized telephone,
 * lower-cased email and Soundex last name — so two owners that resolve to the same key are
 * the same person and the second is a 409 {@link DuplicateOwnerException}.
 *
 * <p>Runs after the owner is built (so its telephone and email are already normalized) and
 * before it is saved, over the existing owners only (the new owner is not yet persisted).
 * A soft-deleted owner no longer occupies its identity, so it is skipped. The email-domain
 * blocklist is applied first by {@link ValidateOwnerEmailDomain}, earlier in the pipeline,
 * so a disposable address is a 400 before this duplicate check runs.
 *
 * <p>Two owners with the same last name and postcode but different telephones no longer
 * collide here — the telephone is part of the key — so they are created and left to
 * {@link FlagPossibleDuplicate} to mark as a soft match.
 */
public class DetectDuplicateIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null) {
                continue;
            }
            // A soft-deleted owner is not a real match, so it never triggers the conflict.
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateOwnerException(identityKey, existing.getId());
            }
        }
    }
}
