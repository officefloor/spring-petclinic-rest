package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects creating an owner whose E.164 telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone}, so the request already carries the E.164
 * value and is compared as-is; existing owners' telephones are converted to E.164 via the
 * shared {@link OwnerTelephones} rule before comparing, making the check independent of how
 * a stored number was originally formatted. A match is a 409 via
 * {@link DuplicateTelephoneException}.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephones.toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
