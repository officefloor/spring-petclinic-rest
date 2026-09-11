package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create request whose derived {@code identityKey} exactly equals an existing
 * owner's. This single check consolidates the former separate telephone, email and
 * household duplicate checks: all three are now expressed through one key
 * ({@link IdentityKeys}). Because the telephone is part of the key, two owners that differ
 * in any of telephone, email or household have different keys and are both allowed — only a
 * WHOLE-key match is a conflict, reported as a 409 by
 * {@link org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityExceptionHandler}.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKeys.forRequest(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKeys.forOwner(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
