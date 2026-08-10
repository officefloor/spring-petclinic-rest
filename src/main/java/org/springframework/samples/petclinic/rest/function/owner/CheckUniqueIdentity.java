package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.util.OwnerIdentities;

/**
 * Rejects a create-owner request whose whole {@code identityKey} equals an existing owner's. The
 * key — normalized telephone, email and householdId joined by {@code '|'} (see
 * {@link OwnerIdentities}) — consolidates what were three separate checks (telephone, email and
 * household): only an exact full-key match is a duplicate, reported as a 409 (see
 * {@link DuplicateIdentityException}).
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold} so the new owner's
 * {@code householdId} is already assigned and forms part of the key. Because the telephone is part
 * of the key, two members of the same household (same {@code householdId}) with different telephones
 * have different keys and are both allowed.
 */
public class CheckUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentities.identityKey(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never collide with self (should not yet be persisted, but be safe)
            }
            if (identityKey.equals(OwnerIdentities.identityKey(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
