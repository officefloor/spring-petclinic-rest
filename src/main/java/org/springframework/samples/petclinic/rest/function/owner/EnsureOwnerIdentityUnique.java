package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create when another owner shares this owner's whole {@link IdentityKey}.
 * This is the single consolidated duplicate check: a shared telephone, email or household
 * on its own is allowed — only an exact full-key match is a conflict.
 */
public class EnsureOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(owner);
        boolean taken = ownerRepository.findAll().stream()
                .filter(other -> !other.isDeleted())
                .anyMatch(other -> identityKey.equals(IdentityKey.of(other)));
        if (taken) {
            throw new DuplicateIdentityException("An owner with the same identity already exists");
        }
    }
}
