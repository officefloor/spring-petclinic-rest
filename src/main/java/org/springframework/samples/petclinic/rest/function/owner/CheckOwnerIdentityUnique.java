package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityDuplicateException;

/**
 * The exact-identity duplicate-detection step of {@code POST /api/owners}. Runs before
 * {@link SaveOwner} and rejects the create with a 409 via {@link OwnerIdentityDuplicateException} when
 * the new owner's derived {@code identityKey} — the {@code SHA-256} of normalized telephone, email and
 * {@code soundex(lastName)} — equals an existing (non-deleted) owner's.
 *
 * <p>This is the single duplicate check: the former separate telephone, email and household checks are
 * all expressed through the one {@link OwnerIdentity} key. Because the normalized telephone is part of
 * the key, two owners with the same last name and postcode but different telephones have different keys
 * and are both allowed here — they are instead flagged as a soft match by
 * {@link CheckPossibleDuplicate}; only an exact full-key match is a hard duplicate. The new owner is
 * still unsaved, so it is not among the owners iterated here and cannot match itself.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityDuplicateException {
        String identityKey = OwnerIdentity.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new create
            }
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new OwnerIdentityDuplicateException(identityKey);
            }
        }
    }
}
