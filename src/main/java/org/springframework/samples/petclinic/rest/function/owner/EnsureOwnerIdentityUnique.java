package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner whose derived {@code identityKey} exactly matches an existing owner's.
 *
 * <p>The key — the hex SHA-256 of {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}
 * (see {@link org.springframework.samples.petclinic.model.Owner#getIdentityKey()}) — is the one place
 * duplicate detection now lives, replacing the former separate telephone, email and household checks.
 * Because the telephone is part of the key, two people who share a surname and residence but have
 * different telephones have different keys: they are admitted as a soft match (flagged by
 * {@link AssignPossibleDuplicate}), not rejected here. Only an exact full-key match is a duplicate.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone}/{@link NormalizeOwnerEmail} (so the key uses canonical
 * values) and before {@link SaveOwner} so a duplicate is a 409 (Conflict) rather than a persisted row.
 */
public class EnsureOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is treated as absent
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
