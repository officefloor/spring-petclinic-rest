package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerEmailException;

/**
 * Rejects a create request whose lower-cased email is already used by any other owner. Runs after
 * {@link NormalizeOwnerEmail}, so the request email is already lower-cased; an absent email is
 * allowed. A match rejects 409.
 */
public class RequireUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            if (email.equalsIgnoreCase(owner.getEmail())) {
                throw new DuplicateOwnerEmailException(email);
            }
        }
    }
}
