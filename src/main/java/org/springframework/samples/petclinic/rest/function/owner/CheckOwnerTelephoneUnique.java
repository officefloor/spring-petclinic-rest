package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose normalized telephone is already used by any other owner.
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}. Both the request and
 * every stored owner's telephone are reduced through the shared {@link OwnerTelephone#normalize}
 * rule before comparison, so differently formatted numbers that collapse to the same digits still
 * collide. A match throws {@link OwnerTelephoneInUseException}, handled as a 409 Conflict.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneInUseException {
        String telephone = OwnerTelephone.normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephone.normalize(existing.getTelephone()))) {
                throw new OwnerTelephoneInUseException(request.getTelephone());
            }
        }
    }
}
