package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * Single, consolidated duplicate check for {@code POST /api/owners}. It replaces the separate
 * telephone, email and household checks with one derived key: an owner's
 * {@link Owner#getIdentityKey() identityKey} = normalized telephone {@code '|'} email (or empty)
 * {@code '|'} householdId (or empty). The request is rejected with 409
 * ({@link OwnerIdentityConflictException}) only when the new owner's WHOLE identityKey equals an
 * existing owner's.
 *
 * <p>Because the telephone is part of the key, two members of the same household (same householdId)
 * with different telephones have different identityKeys and are both allowed; only an exact full-key
 * match is a duplicate. Runs after {@link BuildOwner} and {@link AssignHousehold} so the new owner
 * already carries its householdId, and before {@link SaveOwner} so the new owner is not yet in the
 * repository scan.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new OwnerIdentityConflictException(identityKey);
            }
        }
    }
}
