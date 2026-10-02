package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a new owner whose E.164 telephone is already used by another owner, so the request is
 * answered with a 409 Conflict rather than persisting a duplicate. Runs after {@link ValidateNewOwner}
 * (which has already normalized the request telephone to E.164) and before {@link BuildOwner}.
 *
 * <p>Stored telephones are normalized to E.164 the same way before comparison so seed data that was
 * not stored in E.164 form still collides with an equivalent request.
 */
public class CheckTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            String existingTelephone = existing.getTelephone();
            if (existingTelephone != null
                    && telephone.equals(OwnerTelephone.toE164(existingTelephone))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
