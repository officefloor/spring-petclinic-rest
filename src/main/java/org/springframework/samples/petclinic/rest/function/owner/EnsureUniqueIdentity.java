package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate-detection step in the create-owner pipeline. Runs after the telephone and
 * email are normalized and after the disposable-email-domain blocklist has been applied.
 *
 * <p>Duplicate detection is expressed through the one {@code identityKey} — the SHA-256 hex of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, derived exactly as
 * {@link Owner#getIdentityKey()} does. A create whose whole key equals a stored owner's is rejected
 * with 409; soft-deleted owners are ignored. Because the telephone is part of the key, two owners
 * with the same lastName and postcode but different telephones have different keys and are not a
 * duplicate here — they are admitted and flagged as a soft match by {@link DetectPossibleDuplicate}.
 * The former separate household-duplicate 409 (from the computed {@code householdId}) no longer
 * applies.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = Owner.identityKey(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners no longer block a create
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
