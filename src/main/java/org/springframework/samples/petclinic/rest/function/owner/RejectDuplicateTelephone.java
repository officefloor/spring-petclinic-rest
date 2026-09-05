package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose E.164 telephone is already used by any other owner. Runs after
 * {@link NormalizeTelephone} (so the request telephone is already in E.164 form) and before the owner
 * is built and saved. Each existing owner's telephone is normalized to E.164 the same way before
 * comparison, so numbers stored in any format are matched by their E.164 value. A collision is rejected
 * via {@link DuplicateTelephoneException}, which the global handler turns into a 409.
 */
public class RejectDuplicateTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(TelephoneE164.normalizeOrNull(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(request.getTelephone());
            }
        }
    }
}
