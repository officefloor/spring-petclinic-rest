package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The single duplicate-detection step of {@code POST /api/owners}. Rejects a create-owner request
 * whose derived {@code identityKey} exactly equals an existing owner's (see {@link
 * OwnerIdentityKey}). This subsumes the former separate telephone, email and household checks:
 * because the telephone is part of the key, only an exact full-key match — same normalized
 * telephone <em>and</em> email <em>and</em> household — is a duplicate.
 *
 * <p>Runs after {@link ValidateNewOwner} has normalized the request and before {@link BuildOwner},
 * so a duplicate is a 409 rather than a persisted record.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String identityKey = OwnerIdentityKey.forRequest(request, ownerRepository);
        for (Owner owner : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.forOwner(owner))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }
}
