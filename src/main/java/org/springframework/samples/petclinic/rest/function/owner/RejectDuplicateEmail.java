package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create request whose lower-cased email is already used by any existing owner. Runs
 * after {@code NormalizeOwnerEmail} so the request email is already lower-cased; each stored email
 * is lower-cased the same way before comparison. A request without an email is left alone. Responds
 * 409 on a clash.
 */
public class RejectDuplicateEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            String existing = owner.getEmail();
            if (existing != null && email.equals(existing.toLowerCase())) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
