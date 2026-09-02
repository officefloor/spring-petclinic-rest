package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects a create request whose normalized telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone}, so it compares 10-digit forms; a match rejects 409.
 */
public class RequireUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = request.getTelephone();
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(owner.getTelephone())) {
                throw new DuplicateOwnerTelephoneException(telephone);
            }
        }
    }
}
