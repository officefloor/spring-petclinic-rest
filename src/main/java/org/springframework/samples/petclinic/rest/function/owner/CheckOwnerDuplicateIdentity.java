package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request when the owner's {@link OwnerIdentity identity key} already belongs to an
 * existing owner — the single hard-duplicate rule.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link BuildOwner} (so the owner's fields
 * are populated) and before {@link SaveOwner} (so the new owner is not yet persisted and never
 * compared against itself). The identity key is the SHA-256 of
 * {@code normalizedTelephone | lowerEmail | soundex(lastName)}; a stored owner with the same key is
 * a duplicate and the request is refused with {@link OwnerDuplicateIdentityException}, handled as a
 * 409 Conflict.
 *
 * <p>Soft-deleted owners are ignored (a deleted record never blocks a new identity). The
 * email-domain blocklist has already been applied by {@link ValidateOwnerFields} before this step,
 * so a disposable-domain request is rejected as invalid before it reaches duplicate detection.
 */
public class CheckOwnerDuplicateIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerDuplicateIdentityException {
        String identityKey = OwnerIdentity.identityKey(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the identity check
            }
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                throw new OwnerDuplicateIdentityException(identityKey);
            }
        }
    }
}
