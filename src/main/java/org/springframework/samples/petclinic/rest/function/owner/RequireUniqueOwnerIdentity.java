package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityDuplicateException;

/**
 * The single duplicate-detection step: rejects the create only when the new owner's WHOLE
 * derived {@link OwnerIdentity#identityKey identityKey}
 * (SHA-256 over normalizedTelephone|lowerEmail|soundex(lastName)) equals an existing owner's.
 * This is the sole duplicate check — the former separate telephone, email and household
 * duplicate checks are all subsumed by this one key.
 *
 * <p>Because the telephone is part of the key, two owners sharing a last-name soundex and
 * postcode but with different telephones have different keys and are both allowed (the second is
 * flagged a soft match by {@link AssignPossibleDuplicate}); only an exact full-key match is
 * rejected 409 via {@link OwnerIdentityDuplicateException}. Soft-deleted owners are ignored, and
 * the disposable-email blocklist has already run earlier in the pipeline.
 */
public class RequireUniqueOwnerIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityDuplicateException {
        String key = OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(),
                owner.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // never compare the new owner against itself
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner does not block a new one
            }
            String other = OwnerIdentity.identityKey(existing.getTelephone(), existing.getEmail(),
                    existing.getLastName());
            if (key.equals(other)) {
                throw new OwnerIdentityDuplicateException(key);
            }
        }
    }
}
