package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInUseException;

/**
 * Rejects a create request whose email is already used by any other owner. Runs after
 * {@link ValidateOwnerFields} has lower-cased the email, and before {@link BuildOwner},
 * so a duplicate is a 409 rather than a persisted record. Email is optional: a request
 * without an email is not checked. Duplicates are detected by comparing lower-cased
 * values.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerEmailInUseException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.trim().toLowerCase();
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && normalized.equals(other.trim().toLowerCase())) {
                throw new OwnerEmailInUseException(request.getEmail());
            }
        }
    }
}
