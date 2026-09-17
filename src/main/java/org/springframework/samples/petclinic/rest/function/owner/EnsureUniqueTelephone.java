package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create request whose normalized telephone is already used by any existing owner. Runs
 * after {@link NormalizeTelephone}, so the request telephone is already the canonical digits-only
 * form; each stored telephone is normalized the same way before comparison. A collision is a 409 via
 * {@link DuplicateTelephoneException}.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(TelephoneNormalizer.digitsOnly(owner.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
