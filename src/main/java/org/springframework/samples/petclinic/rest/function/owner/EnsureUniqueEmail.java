package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by an existing owner,
 * responding 409 via {@link DuplicateEmailException}. Runs after {@link NormalizeOwnerEmail} (which
 * lower-cases the request email) and before {@link BuildOwner}, comparing case-insensitively against
 * every stored owner's email. Email is optional: a request with no email skips the check, and stored
 * owners with no email never collide.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        String normalized = email.toLowerCase();
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && existingEmail.toLowerCase().equals(normalized)) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
