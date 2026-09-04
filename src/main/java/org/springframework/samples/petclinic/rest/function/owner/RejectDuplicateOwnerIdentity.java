package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.IdentityKey;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Consolidated duplicate check: rejects a create body whose whole {@link IdentityKey}
 * (normalized telephone + email) already belongs to another owner, so the endpoint
 * responds 409. Runs after the normalize steps so the key is built from canonical
 * values. A full-key match implies the telephones match, so it reuses
 * {@link DuplicateOwnerTelephoneException}.
 */
public class RejectDuplicateOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String key = IdentityKey.of(request.getTelephone(), request.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(IdentityKey.of(existing.getTelephone(), existing.getEmail()))) {
                throw new DuplicateOwnerTelephoneException(request.getTelephone());
            }
        }
    }
}
