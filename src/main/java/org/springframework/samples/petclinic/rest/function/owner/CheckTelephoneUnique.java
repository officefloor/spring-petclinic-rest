package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a new owner whose normalized telephone is already used by another owner, so the request is
 * answered with a 409 Conflict rather than persisting a duplicate. Runs after {@link ValidateNewOwner}
 * (which has already stripped the request telephone to its 10 digits) and before {@link BuildOwner}.
 *
 * <p>Stored telephones are normalized the same way before comparison so seed data that was not stored
 * in digits-only form still collides with an equivalent request.
 */
public class CheckTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            String existingTelephone = existing.getTelephone();
            if (existingTelephone != null
                    && telephone.equals(OwnerTelephone.normalize(existingTelephone))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
