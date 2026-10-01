package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose normalized telephone is already used by any other owner.
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}. Both the request and
 * every stored owner's telephone are reduced to E.164 through the shared
 * {@link OwnerTelephone#toE164} rule before comparison, so differently formatted numbers that
 * collapse to the same E.164 value still collide. A match throws
 * {@link OwnerTelephoneInUseException}, handled as a 409 Conflict.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneInUseException {
        String telephone = OwnerTelephone.toE164(request.getTelephone());
        if (telephone == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephone.toE164(existing.getTelephone()))) {
                throw new OwnerTelephoneInUseException(request.getTelephone());
            }
        }
    }
}
