package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create request whose telephone is already used by another owner, so a
 * duplicate is a 409. Runs after {@link ValidateOwnerFields} has normalized the telephone
 * to E.164 form, comparing against every existing owner's telephone in E.164 form so that
 * national and international spellings of the same number collide.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = TelephoneE164.normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone != null && telephone.equals(TelephoneE164.normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
