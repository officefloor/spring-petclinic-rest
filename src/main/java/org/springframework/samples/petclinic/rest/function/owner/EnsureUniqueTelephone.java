package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Rejects a create-owner request whose E.164 telephone is already used by an existing owner, responding
 * 409 via {@link DuplicateTelephoneException}. Runs after {@link NormalizeOwnerTelephone} (which
 * guarantees the request telephone is the E.164 form) and before {@link BuildOwner}, comparing against
 * every stored owner's telephone normalized to E.164 the same way. Existing telephones that cannot form
 * a valid E.164 number are skipped rather than blocking the request.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            String existingE164;
            try {
                existingE164 = E164Telephone.normalize(existing.getTelephone());
            }
            catch (InvalidTelephoneException ex) {
                continue;
            }
            if (existingE164.equals(telephone)) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
