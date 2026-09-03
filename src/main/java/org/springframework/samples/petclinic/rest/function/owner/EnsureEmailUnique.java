package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Runs after {@link ValidateOwnerFields} has normalized the email to trimmed, lower-cased form:
 * rejects the request when any existing owner already has the same lower-cased email. Both sides are
 * lower-cased before comparison so differing case does not hide a duplicate. Email is optional, so an
 * absent (null) email skips the check. On a match it throws {@link DuplicateEmailException}, handled
 * globally as 409.
 */
public class EnsureEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = lower(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(lower(existing.getEmail()))) {
                throw new DuplicateEmailException(request.getEmail());
            }
        }
    }

    private static String lower(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
