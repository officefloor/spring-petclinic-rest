package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Runs after {@link NormalizeOwnerEmail} and before {@link BuildOwner} in the create-owner
 * pipeline. Email is optional: a request that omits it (null or blank) passes through untouched.
 * When present, its lower-cased value is compared against the lower-cased email of every existing
 * owner; a match is rejected with a 409 so the same email is never registered twice.
 */
public class EnsureUniqueEmail {

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
                throw new DuplicateEmailException(email);
            }
        }
    }
}
