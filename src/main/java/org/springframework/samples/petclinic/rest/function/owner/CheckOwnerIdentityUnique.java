package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check: rejects a create-owner request whose whole derived
 * {@link OwnerIdentityKey} equals an existing owner's, throwing {@link DuplicateOwnerException} for a
 * 409. This consolidates the former separate telephone, email and household checks — a duplicate is
 * now an exact full-key match and nothing less.
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold} so the new owner's telephone, email
 * and {@code householdId} are all in their stored, normalized form before the key is derived, and
 * before {@link SaveOwner} so the not-yet-persisted new owner is compared only against existing
 * owners. Because the telephone is part of the key, two members of one household (same
 * {@code householdId}) with different telephones have different keys and are both allowed.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String identityKey = OwnerIdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never a duplicate of itself
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}
