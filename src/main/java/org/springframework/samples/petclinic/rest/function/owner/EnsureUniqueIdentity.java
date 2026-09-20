package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose {@link IdentityKeys identity key} — normalized telephone,
 * email and household id combined — exactly matches an existing owner's. This is the single
 * duplicate check: telephone, email and household are no longer tested independently, so two
 * owners collide only when their whole keys are equal.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKeys.of(request, ownerRepository);
        for (Owner owner : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKeys.of(owner))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
