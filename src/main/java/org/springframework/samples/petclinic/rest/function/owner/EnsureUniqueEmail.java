package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create request whose email, lower-cased, is already used by an existing
 * owner. Email is optional, so a null or blank value is left through. Runs after
 * {@link NormalizeOwnerEmail} (so the request email is the canonical lower-cased value)
 * and before {@link BuildOwner}, so a collision is a 409 via
 * {@link DuplicateEmailException} before any owner is built or saved. Each stored email
 * is lower-cased the same way before comparing.
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
            if (existingEmail != null && email.equals(EmailNormalizer.normalize(existingEmail))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
