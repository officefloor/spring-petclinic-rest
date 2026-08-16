package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects creating an owner whose E.164 telephone is already used by any other owner,
 * so the create endpoint responds 409 instead of storing a duplicate. Runs after
 * {@link ValidateOwner} (which publishes the telephone normalized to E.164) and before
 * {@link BuildOwner}, comparing against every existing owner's telephone in E.164 form.
 */
public class CheckTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(request.getTelephone());
            }
        }
    }

    private static String normalize(String telephone) {
        String e164 = TelephoneE164.normalize(telephone);
        return e164 == null ? "" : e164;
    }
}
