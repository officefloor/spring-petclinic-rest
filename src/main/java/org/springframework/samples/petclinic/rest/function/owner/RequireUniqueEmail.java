package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any existing
 * owner, responding 409 via {@link DuplicateEmailException}. Runs after
 * {@link NormalizeOwnerEmail} so the request email is already lower-cased; existing owner
 * emails are canonicalized the same way before comparison. An absent email is unique by
 * definition and passes through.
 */
public class RequireUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(OwnerEmails.normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
