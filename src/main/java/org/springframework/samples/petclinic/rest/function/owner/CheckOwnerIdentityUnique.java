package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose derived {@code identityKey} exactly equals an existing owner's.
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}. This single check
 * replaces the former separate telephone, email and household duplicate checks: all three are now
 * folded into the one {@link OwnerIdentity#identityKey} value (normalized telephone, email and
 * household id). Because the telephone is part of the key, two members of the same household with
 * different telephones have different keys and are both allowed; only an exact full-key match is a
 * duplicate. A match throws {@link OwnerIdentityDuplicateException}, handled as a 409 Conflict.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerIdentityDuplicateException {
        String identityKey = OwnerIdentity.forRequest(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                throw new OwnerIdentityDuplicateException(identityKey);
            }
        }
    }
}
