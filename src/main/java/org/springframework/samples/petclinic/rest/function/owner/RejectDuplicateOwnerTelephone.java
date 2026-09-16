package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose telephone already belongs to another owner,
 * responding 409. Runs after {@link NormalizeOwnerTelephone}, so the request's telephone
 * is already in E.164 form; existing owners are stored in E.164 form too, so the stored
 * values are compared directly.
 */
public class RejectDuplicateOwnerTelephone {

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
