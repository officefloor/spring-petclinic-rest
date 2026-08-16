package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailDuplicateException;

/**
 * Rejects the create when the email published by {@link NormalizeOwnerEmail} is already used
 * by another owner. Email is optional: a null or blank value skips the check. Both the request
 * email and existing owners' emails are compared lower-cased, so addresses differing only in
 * case collide. A match is rejected 409 via {@link OwnerEmailDuplicateException}.
 */
public class RequireUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerEmailDuplicateException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.toLowerCase();
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && other.toLowerCase().equals(normalized)) {
                throw new OwnerEmailDuplicateException(normalized);
            }
        }
    }
}
