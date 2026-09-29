package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create whose normalized telephone is already used by any existing owner.
 * Runs after {@link NormalizeOwnerTelephone}, so the request telephone is the 10-digit
 * normalized form; each stored telephone is normalized the same way before comparison.
 * A clash is rejected 409 via {@link DuplicateTelephoneException}.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        // NormalizeOwnerTelephone already normalized the request value in place.
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephones.normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
