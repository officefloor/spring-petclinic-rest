package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a new owner whose lower-cased email is already used by another owner, so the request is
 * answered with a 409 Conflict rather than persisting a duplicate. Runs after {@link ValidateNewOwner}
 * (which has already lower-cased the request email) and before {@link BuildOwner}.
 *
 * <p>Email is optional: a request without an email (null or blank) is left alone. Stored emails are
 * lower-cased the same way before comparison so seed data stored in mixed case still collides with an
 * equivalent request.
 */
public class CheckEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null
                    && normalized.equals(existingEmail.toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(normalized);
            }
        }
    }
}
