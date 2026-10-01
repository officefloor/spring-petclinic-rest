package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose lower-cased email is already used by any other owner.
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}. Email is optional, so a
 * request without one is left alone. Both the request and every stored owner's email are reduced to
 * lower case through the shared {@link OwnerEmail#normalize} rule before comparison, so differently
 * cased addresses that are the same still collide. A match throws {@link OwnerEmailInUseException},
 * handled as a 409 Conflict.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerEmailInUseException {
        String email = OwnerEmail.normalize(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(OwnerEmail.normalize(existing.getEmail()))) {
                throw new OwnerEmailInUseException(request.getEmail());
            }
        }
    }
}
