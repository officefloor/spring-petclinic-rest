package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create whose E.164 telephone is already used by any existing owner.
 * Runs after {@link NormalizeOwnerTelephone}, so the request telephone is the E.164
 * form; each stored telephone is converted to E.164 the same way before comparison.
 * A clash is rejected 409 via {@link DuplicateTelephoneException}.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        // NormalizeOwnerTelephone already converted the request value to E.164 in place.
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephones.toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
