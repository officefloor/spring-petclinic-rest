package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create request whose telephone is already used by an existing owner. Runs
 * after {@link RequireOwnerFields} has normalized the telephone to E.164 form, so the
 * comparison is between E.164 values. A conflict is reported as a 409 by
 * {@link org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneExceptionHandler}.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = Telephones.toE164(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone != null && telephone.equals(Telephones.toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
