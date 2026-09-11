package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInUseException;

/**
 * Rejects a create request whose telephone is already used by any other owner. Runs
 * after {@link ValidateOwnerFields} has normalized the telephone to E.164, and before
 * {@link BuildOwner}, so a duplicate is a 409 rather than a persisted record. Duplicates
 * are detected by comparing E.164 values.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneInUseException {
        String telephone = TelephoneE164.toE164(request.getTelephone());
        if (telephone == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(TelephoneE164.toE164(existing.getTelephone()))) {
                throw new OwnerTelephoneInUseException(request.getTelephone());
            }
        }
    }
}
