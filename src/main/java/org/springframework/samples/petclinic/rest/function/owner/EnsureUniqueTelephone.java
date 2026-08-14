package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Runs after {@link NormalizeOwnerTelephone} and before {@link BuildOwner} in the create-owner
 * pipeline. Rejects a request whose normalized telephone is already used by any existing owner
 * with a 409, so the same phone number is never registered twice.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(existing.getTelephone())) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
