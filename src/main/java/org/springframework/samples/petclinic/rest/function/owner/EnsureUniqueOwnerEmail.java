package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerEmailException;

/**
 * Runs in {@code POST /api/owners} after the telephone-uniqueness check, reading the
 * already-normalized body as a variable. When the request carries an email, rejects it if
 * another owner already uses the same address (compared lower-cased), throwing
 * {@link DuplicateOwnerEmailException} for a 409 before any entity is built or persisted.
 * An absent email is left alone, so email stays optional.
 */
public class EnsureUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {

        String email = request.getEmail(); // already normalized (lower-cased) upstream
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && email.equals(OwnerEmail.normalize(existingEmail))) {
                throw new DuplicateOwnerEmailException(email);
            }
        }
    }
}
