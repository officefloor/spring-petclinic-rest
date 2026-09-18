package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by another owner, before
 * {@link BuildOwner} runs. Runs after {@link NormalizeOwnerTelephone} so the request holds the cleaned
 * digits that match how owner telephones are persisted. Rejects with 409 otherwise.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        if (!ownerRepository.findByTelephone(request.getTelephone()).isEmpty()) {
            throw new DuplicateTelephoneException(request.getTelephone());
        }
    }
}
