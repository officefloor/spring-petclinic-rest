package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create request whose derived {@link IdentityKey identity key} exactly equals an existing
 * owner's — the single consolidated duplicate check that replaces the separate telephone, email and
 * household checks. Runs after the telephone and email are normalized and the household id assigned
 * (see {@link AssignHousehold}), so the built owner already carries its final key parts. Because the
 * telephone is part of the key, two members of one household with different telephones have distinct
 * keys and are both allowed; only a full-key match is a 409 via {@link DuplicateIdentityException}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.forOwner(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKey.forOwner(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
