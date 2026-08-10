package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Runs after {@link NormalizeOwnerEmail} (so the request email is already trimmed and lower-cased) and
 * before {@link BuildOwner}. Rejects the request when any existing owner already uses the same
 * lower-cased email by throwing {@link DuplicateEmailException} (handled as 409). Email is optional: a
 * request with no email is left alone. Existing owners' emails are lower-cased before comparison so a
 * differently-cased stored value still collides.
 */
public class EnsureUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && email.equals(other.trim().toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
