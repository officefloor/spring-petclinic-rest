package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create request whose lower-cased email is already used by an existing owner.
 * Email is optional; a request without one is never a conflict. The comparison is between
 * lower-cased values (existing owner emails are stored lower-cased). A conflict is reported
 * as a 409 by
 * {@link org.springframework.samples.petclinic.rest.escalation.DuplicateEmailExceptionHandler}.
 */
public class RequireUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = normalize(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(email);
            }
        }
    }

    /** Trim and lower-case; null or blank yields null (no email to compare). */
    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
