package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects a create body whose normalized telephone is already held by another owner,
 * so the endpoint responds 409. Runs after {@link NormalizeOwnerTelephone} so the
 * comparison uses the bare 10-digit value.
 */
public class RejectDuplicateOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(existing.getTelephone())) {
                throw new DuplicateOwnerTelephoneException(telephone);
            }
        }
    }
}
