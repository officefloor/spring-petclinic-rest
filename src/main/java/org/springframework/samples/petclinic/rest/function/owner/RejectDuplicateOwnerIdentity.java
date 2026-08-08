package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The single, consolidated duplicate check: rejects a new owner whose derived
 * {@code identityKey} (see {@link OwnerIdentity}) exactly equals an existing owner's.
 * Runs after {@link NormalizeOwnerTelephone}/{@link NormalizeOwnerEmail} (so telephone
 * and email are canonical) and after {@link AssignOwnerHousehold} (so any shared
 * {@code householdId} is set), and before {@link SaveOwner}. On a full-key match it
 * throws a checked {@link DuplicateOwnerIdentityException}, which the escalation
 * handler turns into a 409 Conflict.
 *
 * <p>Because the telephone is part of the key, two members of the same household with
 * different telephones have different identityKeys and are both allowed — only an
 * exact full-key match is a duplicate.
 */
public class RejectDuplicateOwnerIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String identityKey = OwnerIdentity.identityKey(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never conflict with the owner itself
            }
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                throw new DuplicateOwnerIdentityException(
                        "An owner with the same identity already exists");
            }
        }
    }
}
