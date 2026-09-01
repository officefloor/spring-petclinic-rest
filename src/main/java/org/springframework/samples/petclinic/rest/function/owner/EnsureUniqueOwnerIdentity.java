package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * On create, rejects the owner when its whole {@link IdentityKey} (normalized telephone,
 * email and household id) equals an existing owner's. This is the single consolidated
 * duplicate check: owners collide only on an exact full-key match, so two members of the
 * same household with different telephones are both allowed.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                    && identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException("An owner with the same identity already exists");
            }
        }
    }
}
