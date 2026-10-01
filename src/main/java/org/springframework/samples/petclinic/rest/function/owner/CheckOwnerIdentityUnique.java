package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityDuplicateException;

/**
 * The exact-identity duplicate-detection step of {@code POST /api/owners}. Runs after
 * {@link CheckOwnerHouseholdUnique} (so an undeclared second member of a household has already been
 * rejected) and before {@link SaveOwner}. Rejects the create with a 409 via
 * {@link OwnerIdentityDuplicateException} when the new owner's whole derived {@code identityKey} equals
 * an existing owner's.
 *
 * <p>This replaces the former separate telephone, email and household checks: all three are now
 * expressed through the one {@link OwnerIdentity} key. Because the normalized telephone is part of the
 * key, two members of the same household (same {@code householdId}) with different telephones have
 * different keys and are both allowed — only an exact full-key match is a duplicate. The new owner is
 * still unsaved, so it is not among the owners iterated here and cannot match itself.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityDuplicateException {
        String identityKey = OwnerIdentity.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new OwnerIdentityDuplicateException(identityKey);
            }
        }
    }
}
