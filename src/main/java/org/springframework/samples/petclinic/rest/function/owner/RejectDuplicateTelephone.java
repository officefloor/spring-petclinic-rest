package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create request whose normalized telephone is already used by any existing owner.
 * Runs after {@code NormalizeOwnerTelephone} so the request telephone is already E.164; each
 * stored telephone is converted to E.164 the same way before comparison. Responds 409 on a clash.
 */
public class RejectDuplicateTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(E164.toE164(owner.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
