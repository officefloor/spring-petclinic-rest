package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create request whose (already normalized, lower-cased) email matches an existing owner's,
 * so an email is unique across owners. Runs after {@link NormalizeOwnerEmail} — the request now holds
 * a lower-cased value — and before {@link BuildOwner}; a match is a 409 via
 * {@link DuplicateEmailException}. Email is optional, so a request without one is left alone. Existing
 * emails are lower-cased the same way before comparison so seed data stored in other cases still
 * counts as a duplicate.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && email.equals(existingEmail.toLowerCase())) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
