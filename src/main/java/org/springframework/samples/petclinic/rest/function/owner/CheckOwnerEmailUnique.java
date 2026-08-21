package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other owner.
 * Runs after {@link ValidateOwnerFields} has lower-cased the body's email, and compares it against
 * every existing owner's email (lower-cased the same way). A request without an email is left alone.
 * Throws {@link DuplicateEmailException} (handled as 409) on a collision.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.toLowerCase();
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && normalized.equals(existingEmail.toLowerCase())) {
                throw new DuplicateEmailException(request.getEmail());
            }
        }
    }
}
