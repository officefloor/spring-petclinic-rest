package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

import net.officefloor.plugin.variable.Val;

/**
 * Step of {@code POST /api/owners} that runs after {@link ValidateOwnerFields} has normalized the
 * telephone. Rejects the request when any existing owner already uses that normalized telephone,
 * so a duplicate is a 409 Conflict rather than a second owner sharing a phone number.
 */
public class RejectDuplicateTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findByTelephone(telephone)) {
            if (telephone.equals(existing.getTelephone())) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
