package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose lower-cased email is already used by another owner, so the
 * pair maps to a 409. Runs after {@link NormalizeOwnerEmail} has lower-cased the request's
 * email, and before {@link BuildOwner} maps it to an entity. Email is optional: a null or
 * blank value is left alone (no uniqueness to enforce). Existing owners are compared on their
 * lower-cased email too, so case differences do not hide a collision.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && normalized.equals(other.toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
