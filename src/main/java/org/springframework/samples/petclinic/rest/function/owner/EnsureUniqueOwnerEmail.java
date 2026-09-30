package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create whose lower-cased email is already used by any existing owner.
 * The email is optional, so a blank or missing value is left alone. Both the request
 * email and each stored email are lower-cased before comparison. A clash is rejected
 * 409 via {@link DuplicateEmailException}.
 */
public class EnsureUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String requestEmail = request.getEmail();
        if (requestEmail == null || requestEmail.isBlank()) {
            return;
        }
        String email = requestEmail.toLowerCase(Locale.ROOT);
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && email.equals(existingEmail.toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
