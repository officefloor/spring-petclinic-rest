package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create request whose whole {@link IdentityKey} equals an existing owner's. This single
 * check replaces the separate telephone, email and household checks: only an exact full-key match
 * (same normalized telephone, email and householdId) is a duplicate. Responds 409 on a clash.
 */
public class RejectDuplicateIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.forRequest(request);
        for (Owner owner : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKey.forOwner(owner))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
