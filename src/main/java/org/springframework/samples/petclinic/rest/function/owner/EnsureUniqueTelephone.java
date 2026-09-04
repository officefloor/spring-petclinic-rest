package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create request whose (already E.164-normalized) telephone equals any existing owner's
 * stored E.164 telephone, so a duplicate number is a 409 via {@link DuplicateTelephoneException}.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(owner.getTelephone())) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
