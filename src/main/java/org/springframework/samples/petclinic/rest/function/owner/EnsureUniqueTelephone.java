package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose telephone is already used by another owner,
 * responding 409. Runs after {@link NormalizeOwnerTelephone} so the comparison is
 * between normalized values, and before {@link BuildOwner} persists a duplicate.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        if (!ownerRepository.findByTelephone(request.getTelephone()).isEmpty()) {
            throw new DuplicateTelephoneException(request.getTelephone());
        }
    }
}
